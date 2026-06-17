#!/usr/bin/env sh
# Best-effort sync of agent configuration from repo-config.json repositories.
set -u

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || true)"
if [ -z "$ROOT" ]; then
  exit 0
fi

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"

if command -v powershell.exe >/dev/null 2>&1; then
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$SCRIPT_DIR/sync-agent-config.ps1" -ProjectRoot "$ROOT"
  exit 0
fi

if command -v pwsh >/dev/null 2>&1; then
  pwsh -NoProfile -File "$SCRIPT_DIR/sync-agent-config.ps1" -ProjectRoot "$ROOT"
  exit 0
fi

echo "[sync-agent-config] PowerShell is required on this system." >&2
exit 0
