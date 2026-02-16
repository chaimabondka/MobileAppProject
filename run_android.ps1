# Lance l'app Android depuis un chemin SANS ESPACES (obligatoire avec ce projet).
# Cree une jonction C:\EventBookingApp si besoin, puis lance Flutter depuis la.

$linkPath = "C:\EventBookingApp"
$projectRoot = $PSScriptRoot

if (-not (Test-Path $linkPath)) {
    Write-Host "Creation de la jonction $linkPath -> $projectRoot" -ForegroundColor Yellow
    cmd /c mklink /J $linkPath $projectRoot
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Erreur: impossible de creer la jonction. Lancez PowerShell en tant qu'Administrateur." -ForegroundColor Red
        Write-Host "Ou copiez le projet vers C:\EventBookingApp et ouvrez ce dossier dans Cursor." -ForegroundColor Yellow
        exit 1
    }
}

Write-Host "Lancement depuis $linkPath (chemin sans espaces)..." -ForegroundColor Green
Set-Location $linkPath
$device = flutter devices 2>$null | Select-String "emulator"
if (-not $device) {
    Write-Host "Demarrage de l'emulateur..." -ForegroundColor Yellow
    Start-Process -FilePath "flutter" -ArgumentList "emulators", "--launch", "Medium_Phone_API_36.1" -Wait -NoNewWindow
    Start-Sleep -Seconds 15
}
flutter run -d android
