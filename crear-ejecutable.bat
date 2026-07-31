@echo off
title Generador de Ejecutable Comuneros POS
echo =======================================================
echo     GENERANDO APLICACION PORTABLE PARA WINDOWS (.EXE)
echo =======================================================
echo.

echo [+] Cerrando procesos abiertos para prevenir bloqueos de archivos...
taskkill /F /IM Comuneros.exe > nul 2>&1
taskkill /F /IM java.exe > nul 2>&1
taskkill /F /IM javaw.exe > nul 2>&1
timeout /t 1 /nobreak > nul 2>&1

echo [+] Limpiando directorios anteriores...
if exist package-temp rd /s /q package-temp
if exist Comuneros-POS rd /s /q Comuneros-POS

echo [+] Preparando carpeta temporal de empaquetado...
mkdir package-temp

echo [+] Copiando JAR principal...
copy dist\Restaurante_comuneros.jar package-temp\

echo [+] Copiando librerias de dependencia...
copy librerias\*.jar package-temp\

echo [+] Compilando aplicacion nativa con JRE integrado...
jpackage --type app-image --name "Comuneros" --input package-temp --main-jar Restaurante_comuneros.jar --main-class restaurante.Restaurante --dest Comuneros-POS --add-modules java.base,java.desktop,java.sql,java.naming,java.management,jdk.httpserver

echo [+] Copiando base de datos activa y utilidades al ejecutable...
if exist restaurante.db (
    copy restaurante.db Comuneros-POS\Comuneros\ > nul
)
if exist abrir-puerto-android.bat (
    copy abrir-puerto-android.bat Comuneros-POS\Comuneros\ > nul
)

echo.
echo =======================================================
echo  [OK] Proceso de empaquetado finalizado.
echo  Busque la carpeta 'Comuneros-POS\Comuneros'
echo  Ahi dentro encontrara 'Comuneros.exe'.
echo =======================================================
echo.

rd /s /q package-temp
pause
