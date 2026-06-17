#Requires -Version 5.1
$ErrorActionPreference = "Continue"

$projectRoot = git rev-parse --show-toplevel
if (-not $projectRoot) {
    throw "Run this script from inside the git repository."
}

$hooksPath = ".agent-config/githooks"
git -C $projectRoot config core.hooksPath $hooksPath

Write-Host "Configured core.hooksPath=$hooksPath for this repository."
Write-Host "Git hooks will sync agent config on commit, push, pull, checkout, and merge."
