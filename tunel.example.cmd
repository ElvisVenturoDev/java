@echo off
REM Plantilla: copia este archivo como tunel.cmd (junto a tu tunel.ps1).
REM Abre el tunel SSH sin problemas de ExecutionPolicy.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tunel.ps1"
if errorlevel 1 pause
