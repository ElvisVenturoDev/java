# ============================================================================
#  tunel.example.ps1 - Plantilla del tunel SSH hacia el RDS privado.
#
#  Copia este archivo como tunel.ps1 y completa los valores reales.
#  tunel.ps1 esta excluido del control de versiones: no subas tus IPs,
#  endpoints ni rutas de llaves al repositorio.
#
#  Uso:   .\tunel.ps1            (usa la configuracion de abajo)
#         .\tunel.cmd            (doble clic / consola, evita ExecutionPolicy)
#
#  Deja esta ventana abierta mientras usas la aplicacion.
#  La app se conecta a 127.0.0.1:3306 y el trafico viaja:
#     tu PC -> SSH (22) -> EC2 -> RDS (3306)
# ============================================================================

# ------------------------- CONFIGURA ESTO UNA VEZ ---------------------------
$Ec2Host = "IP_O_DNS_DE_LA_EC2"                  # IP publica o DNS de la EC2 (web)
$SshUser = "ubuntu"                              # ubuntu, ec2-user, admin...
$KeyPath = "C:\ruta\a\tu-llave.pem"              # llave SSH que funciona con esta EC2
$SshPort = 22

$RdsHost = "mi-instancia.rds.amazonaws.com"      # endpoint del RDS privado
$RdsPort = 3306

$LocalPort = 3307                                # puerto en tu PC que usara la app
# ---------------------------------------------------------------------------

$ErrorActionPreference = "Stop"

Write-Host "== Tunel SSH hacia RDS ==" -ForegroundColor Cyan
Write-Host "  PC 127.0.0.1:$LocalPort  ->  $SshUser@$Ec2Host  ->  ${RdsHost}:$RdsPort"

if ($Ec2Host -like "IP_O_DNS*") {
    Write-Host "ERROR: edita tunel.ps1 y pon la IP/DNS publico de tu EC2 en `$Ec2Host." -ForegroundColor Red
    exit 1
}
if (-not (Test-Path -LiteralPath $KeyPath)) {
    Write-Host "ERROR: no existe la llave SSH en: $KeyPath" -ForegroundColor Red
    exit 1
}
if (-not (Get-Command ssh -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: no se encontro 'ssh'. Instala el cliente OpenSSH de Windows." -ForegroundColor Red
    exit 1
}

# Aviso si el puerto local ya esta ocupado (tunel previo, MySQL local, etc.)
$ocupado = Get-NetTCPConnection -LocalPort $LocalPort -State Listen -ErrorAction SilentlyContinue
if ($ocupado) {
    Write-Host "AVISO: el puerto $LocalPort ya esta en uso en tu PC." -ForegroundColor Yellow
    Write-Host "       Cierra el proceso que lo usa o cambia `$LocalPort (y local.port en la app)." -ForegroundColor Yellow
    $r = Read-Host "       Continuar igualmente? (s/N)"
    if ($r -notmatch '^[sSyY]') { exit 1 }
}

Write-Host "Conectando... (Ctrl+C para cerrar el tunel)" -ForegroundColor Green
ssh -i "$KeyPath" -p $SshPort -N -o ExitOnForwardFailure=yes `
    -o ServerAliveInterval=30 -o ServerAliveCountMax=3 `
    -L "${LocalPort}:${RdsHost}:${RdsPort}" "$SshUser@$Ec2Host"

# Si ssh termina, avisa del codigo de salida.
if ($LASTEXITCODE -ne 0) {
    Write-Host "El tunel se cerro con codigo $LASTEXITCODE." -ForegroundColor Red
    Write-Host "Revisa: IP de la EC2, usuario SSH, permisos de la llave .pem y que el puerto $SshPort este abierto." -ForegroundColor Yellow
}
