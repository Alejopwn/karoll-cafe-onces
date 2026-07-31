@echo off
title Creador de Instalador Comuneros POS
echo =================================================================
echo   CREADOR DE INSTALADOR .EXE NATIVO - RESTAURANTE COMUNEROS
echo =================================================================
echo.
echo Requisitos:
echo - Tener instalado JDK 17 o superior en este PC.
echo - El comando 'jpackage' debe estar accesible en la terminal.
echo.

:: 1. Limpiar carpetas temporales anteriores
if exist installer-input rmdir /s /q installer-input
if exist installer-output rmdir /s /q installer-output

mkdir installer-input
mkdir installer-output

echo [+] Copiando archivos necesarios...
copy dist\Restaurante_comuneros.jar installer-input\ > nul
xcopy /E /I /Y librerias installer-input\librerias > nul

echo.
echo [+] Generando instalador de Windows (.exe)...
echo     Esto puede tardar un momento ya que empaqueta el JRE...
echo.

:: 2. Ejecutar jpackage
:: Nota: Si tienes un archivo de icono (.ico) para el programa, 
:: puedes agregar la línea: --icon src\Img\logo.ico ^
jpackage ^
  --type exe ^
  --input installer-input ^
  --dest installer-output ^
  --name "ComunerosPOS" ^
  --main-jar Restaurante_comuneros.jar ^
  --main-class restaurante.Restaurante ^
  --app-version 1.0.0 ^
  --vendor "Restaurante Comuneros" ^
  --win-dir-chooser ^
  --win-shortcut ^
  --win-menu

if %ERRORLEVEL% EQU 0 (
    echo.
    echo =================================================================
    echo   [OK] !INSTALADOR CREADO EXITOSAMENTE!
    echo   Revisa la carpeta: installer-output\
    echo =================================================================
) else (
    echo.
    echo =================================================================
    echo   [ERROR] Hubo un problema al generar el instalador.
    echo   Asegurate de tener instalado Wix Toolset (para empaquetar .exe)
    echo   o que jpackage este bien configurado en tu PATH de Windows.
    echo =================================================================
)

:: 3. Limpiar carpeta de entrada temporal
if exist installer-input rmdir /s /q installer-input

pause
