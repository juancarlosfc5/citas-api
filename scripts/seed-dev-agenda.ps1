# Carga la agenda sintética de desarrollo (2026-09-29 a 2026-10-15) en el MySQL local de docker compose.
# Uso desde la raíz del workspace: .\citas-api\scripts\seed-dev-agenda.ps1   (idempotente)
$ErrorActionPreference = "Stop"
$Workspace = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$SqlFile = Join-Path $PSScriptRoot "seed-dev-agenda.sql"
Set-Location $Workspace
Get-Content -Raw -Encoding UTF8 $SqlFile |
  docker compose exec -T mysql sh -c 'mysql --default-character-set=utf8mb4 -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE" 2>/dev/null'
if ($LASTEXITCODE -ne 0) { throw "La semilla falló (código $LASTEXITCODE)." }
Write-Host "Agenda semilla cargada." -ForegroundColor Green
