#Requires -Version 5.1
<#
.SYNOPSIS
  Downloads agent configuration from repositories listed in repo-config.json
  and installs skills, rules, and subagents into the project.
#>
param(
    [string]$ProjectRoot = ""
)

$ErrorActionPreference = "Continue"

function Write-SyncLog {
    param([string]$Message)
    Write-Host "[sync-agent-config] $Message"
}

function Get-RepoSlug {
    param([string]$GitUrl)
    $slug = $GitUrl -replace '\.git$', ''
    return ($slug -split '[/\\:]' | Where-Object { $_ } | Select-Object -Last 1)
}

function ConvertTo-HttpsGitUrl {
    param([string]$GitUrl)

    if ($GitUrl -match '^git@([^:]+):(.+)$') {
        return "https://$($Matches[1])/$($Matches[2])"
    }

    return $GitUrl
}

function Update-AgentRepository {
    param(
        [string]$GitUrl,
        [string]$Branch,
        [string]$CloneDir
    )

    if (-not (Test-Path (Join-Path $CloneDir ".git"))) {
        Write-SyncLog "Cloning $GitUrl (branch: $Branch)..."
        git clone --depth 1 --branch $Branch $GitUrl $CloneDir 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            return
        }

        $httpsUrl = ConvertTo-HttpsGitUrl -GitUrl $GitUrl
        if ($httpsUrl -ne $GitUrl) {
            Write-SyncLog "SSH clone failed. Retrying with HTTPS..."
            if (Test-Path $CloneDir) {
                Remove-Item -Recurse -Force $CloneDir
            }
            git clone --depth 1 --branch $Branch $httpsUrl $CloneDir 2>&1 | Out-Null
        }

        if ($LASTEXITCODE -ne 0) {
            throw "git clone failed with exit code $LASTEXITCODE"
        }
        return
    }

    Write-SyncLog "Updating $(Split-Path -Leaf $CloneDir) (branch: $Branch)..."
    git -C $CloneDir fetch --depth 1 origin $Branch 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "git fetch failed with exit code $LASTEXITCODE"
    }
    git -C $CloneDir checkout $Branch 2>&1 | Out-Null
    git -C $CloneDir reset --hard "origin/$Branch" 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "git reset failed with exit code $LASTEXITCODE"
    }
}

function Sync-DirectoryContents {
    param(
        [string]$SourceDir,
        [string]$TargetDir,
        [string]$Label
    )

    if (-not (Test-Path $SourceDir)) {
        return
    }

    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null

    Get-ChildItem -Path $SourceDir -Force | ForEach-Object {
        $destination = Join-Path $TargetDir $_.Name
        if ($_.PSIsContainer) {
            Copy-Item -Path $_.FullName -Destination $destination -Recurse -Force
        }
        else {
            Copy-Item -Path $_.FullName -Destination $destination -Force
        }
    }

    Write-SyncLog "Synced $Label -> $TargetDir"
}

function Sync-FlatAgentFiles {
    param(
        [string]$SourceDir,
        [string]$TargetDir,
        [string]$Label,
        [string[]]$Extensions = @(".md")
    )

    if (-not (Test-Path $SourceDir)) {
        return
    }

    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null

    Get-ChildItem -Path $SourceDir -Recurse -File -Force |
        Where-Object { $Extensions -contains $_.Extension.ToLowerInvariant() } |
        ForEach-Object {
            $targetFile = Join-Path $TargetDir $_.Name
            if (Test-Path $targetFile) {
                $baseName = [System.IO.Path]::GetFileNameWithoutExtension($_.Name)
                $extension = $_.Extension
                $targetFile = Join-Path $TargetDir ("{0}-{1}{2}" -f $baseName, $_.Directory.Name, $extension)
            }
            Copy-Item -Path $_.FullName -Destination $targetFile -Force
        }

    Write-SyncLog "Synced $Label -> $TargetDir"
}

if ([string]::IsNullOrWhiteSpace($ProjectRoot)) {
    $ProjectRoot = (git rev-parse --show-toplevel 2>$null)
}

if ([string]::IsNullOrWhiteSpace($ProjectRoot) -or -not (Test-Path $ProjectRoot)) {
    Write-SyncLog "Not inside a git repository. Skipping."
    exit 0
}

$hooksPath = ".agent-config/githooks"
$configuredHooksPath = git -C $ProjectRoot config --get core.hooksPath 2>$null
if ($configuredHooksPath -ne $hooksPath) {
    git -C $ProjectRoot config core.hooksPath $hooksPath | Out-Null
    Write-SyncLog "Configured core.hooksPath=$hooksPath for this repository."
}

$configPath = Join-Path $ProjectRoot "repo-config.json"
if (-not (Test-Path $configPath)) {
    Write-SyncLog "repo-config.json not found. Skipping."
    exit 0
}

try {
    $config = Get-Content -Raw -Path $configPath | ConvertFrom-Json
}
catch {
    Write-SyncLog "Invalid repo-config.json: $($_.Exception.Message)"
    exit 0
}

if (-not $config.repositories) {
    Write-SyncLog "No repositories configured. Skipping."
    exit 0
}

$cacheRoot = Join-Path $ProjectRoot ".agent-config\cache\agent-config-repos"
New-Item -ItemType Directory -Force -Path $cacheRoot | Out-Null

$projectAgentsDir = Join-Path $ProjectRoot ".agents"
$projectRulesDir = Join-Path $ProjectRoot ".cursor\rules"
$projectAgentsCursorDir = Join-Path $ProjectRoot ".cursor\agents"

New-Item -ItemType Directory -Force -Path $projectAgentsDir | Out-Null
New-Item -ItemType Directory -Force -Path $projectRulesDir | Out-Null
New-Item -ItemType Directory -Force -Path $projectAgentsCursorDir | Out-Null

foreach ($repository in @($config.repositories)) {
    $gitUrl = $repository.'git-url'
    if ([string]::IsNullOrWhiteSpace($gitUrl)) {
        Write-SyncLog "Skipping repository entry without git-url."
        continue
    }

    $branch = if ($repository.branch) { $repository.branch } else { "main" }
    $slug = Get-RepoSlug -GitUrl $gitUrl
    $cloneDir = Join-Path $cacheRoot $slug

    try {
        Update-AgentRepository -GitUrl $gitUrl -Branch $branch -CloneDir $cloneDir
    }
    catch {
        Write-SyncLog "Failed to sync repository '$gitUrl': $($_.Exception.Message)"
        continue
    }

    $remoteAgentsDir = Join-Path $cloneDir ".agents"
    if (-not (Test-Path $remoteAgentsDir)) {
        Write-SyncLog "No .agents folder in $slug. Skipping content copy."
        continue
    }

    Sync-DirectoryContents `
        -SourceDir (Join-Path $remoteAgentsDir "skills") `
        -TargetDir (Join-Path $projectAgentsDir "skills") `
        -Label "$slug skills"

    Sync-DirectoryContents `
        -SourceDir (Join-Path $remoteAgentsDir "rules") `
        -TargetDir (Join-Path $projectRulesDir "imported\.agents\rules") `
        -Label "$slug rules"

    $subagentSources = @(
        (Join-Path $remoteAgentsDir "subagents"),
        (Join-Path $remoteAgentsDir "agents")
    )

    foreach ($subagentSource in $subagentSources) {
        Sync-FlatAgentFiles `
            -SourceDir $subagentSource `
            -TargetDir $projectAgentsCursorDir `
            -Label "$slug subagents"
    }
}

Write-SyncLog "Done."
exit 0
