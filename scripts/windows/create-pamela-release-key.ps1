$ErrorActionPreference = "Stop"

$root = Join-Path $env:USERPROFILE "PamelaSigning"
New-Item -ItemType Directory -Force -Path $root | Out-Null
$keystore = Join-Path $root "pamela-release.jks"
$base64File = Join-Path $root "PAMELA_KEYSTORE_BASE64.txt"

if (Test-Path $keystore) {
  throw "Ya existe $keystore. No se sobrescribirá una clave existente."
}

$keytool = Get-Command keytool -ErrorAction SilentlyContinue
if (-not $keytool) {
  throw "No se encontró keytool. Instalá un JDK (por ejemplo Temurin 21) y volvé a ejecutar."
}

Write-Host "Se creará la clave permanente de firma de Pamela."
Write-Host "Guardá el archivo JKS en un respaldo seguro. No lo subas al repositorio."
Write-Host "Keytool solicitará la contraseña del almacén y de la clave."
& $keytool.Source -genkeypair -keystore $keystore -storetype JKS -alias pamela-release -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Pamela Android Release"
if ($LASTEXITCODE -ne 0) { throw "Falló la generación del keystore." }

$bytes = [System.IO.File]::ReadAllBytes($keystore)
[System.IO.File]::WriteAllText($base64File, [Convert]::ToBase64String($bytes), [System.Text.Encoding]::ASCII)

Write-Host ""
Write-Host "Keystore creado: $keystore"
Write-Host "Base64 para el secreto GitHub: $base64File"
Write-Host "No compartas el JKS ni sus contraseñas por chat."
Write-Host "Fingerprint SHA-256 del certificado (keytool pedirá la contraseña):"
& $keytool.Source -list -v -keystore $keystore -alias pamela-release
if ($LASTEXITCODE -ne 0) { throw "No se pudo leer el certificado." }
