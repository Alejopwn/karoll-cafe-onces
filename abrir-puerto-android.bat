@echo off
title Abrir Puerto 8080 para Android
echo =====================================================
echo  Abriendo Puerto 8080 para Panel Android de Meseros
echo =====================================================
echo.

:: Verificar si se ejecuta como administrador
net session >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [!] Este script necesita permisos de Administrador.
    echo [!] Haciendo clic derecho y ejecutando como Admin...
    powershell -Command "Start-Process '%~f0' -Verb RunAs"
    exit /b
)

echo [+] Agregando regla al Firewall de Windows...
netsh advfirewall firewall add rule name="Comuneros POS - Panel Android" dir=in action=allow protocol=TCP localport=8080
netsh advfirewall firewall add rule name="Comuneros POS - Panel Android OUT" dir=out action=allow protocol=TCP localport=8080

if %ERRORLEVEL% EQU 0 (
    echo.
    echo =====================================================
    echo  [OK] Puerto 8080 abierto correctamente!
    echo  Ahora el celular puede acceder al panel en:
    for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4"') do (
        set IP=%%a
        setlocal EnableDelayedExpansion
        set IP=!IP: =!
        echo  http://!IP!:8080
        endlocal
    )
    echo =====================================================
) else (
    echo [ERROR] No se pudo abrir el puerto.
)

echo.
pause
