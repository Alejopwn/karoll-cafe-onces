@echo off
title Compilador de Restaurante Comuneros
echo =======================================================
echo           COMPILANDO PROYECTO JAVA
echo =======================================================
echo.

echo [+] Cerrando procesos abiertos para prevenir bloqueos de archivos...
taskkill /F /IM Comuneros.exe > nul 2>&1
taskkill /F /IM java.exe > nul 2>&1
taskkill /F /IM javaw.exe > nul 2>&1
timeout /t 1 /nobreak > nul 2>&1

echo [+] Limpiando directorios anteriores...
if exist build rd /s /q build
if exist dist rd /s /q dist

echo [+] Creando directorios necesarios...
mkdir build\classes
mkdir dist

echo [+] Compilando archivos Java...
javac -encoding UTF-8 -d build\classes -cp "librerias/*" src/restaurante/*.java src/Modelo/*.java src/Vista/*.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ [ERROR] Hubo un problema al compilar los archivos.
    pause
    exit /b %ERRORLEVEL%
)

echo [+] Copiando recursos e imagenes...
mkdir build\classes\Img 2>nul
powershell -Command "Copy-Item -Path src/Img/* -Destination build/classes/Img/ -Force"

echo [+] Creando manifiesto temporal...
(
echo Manifest-Version: 1.0
echo Main-Class: restaurante.Restaurante
echo Class-Path: librerias/AbsoluteLayout.jar librerias/HikariCP-5.1.0.jar librerias/flatlaf-3.5.2.jar librerias/itextpdf-5.5.1.jar librerias/jbcrypt-0.4.jar librerias/mysql-connector-java-8.0.19.jar librerias/slf4j-api-2.0.13.jar librerias/sqlite-jdbc-3.46.0.0.jar AbsoluteLayout.jar HikariCP-5.1.0.jar flatlaf-3.5.2.jar itextpdf-5.5.1.jar jbcrypt-0.4.jar mysql-connector-java-8.0.19.jar slf4j-api-2.0.13.jar sqlite-jdbc-3.46.0.0.jar
) > temp_manifest.mf

echo [+] Generando archivo JAR...
jar cfm dist\Restaurante_comuneros.jar temp_manifest.mf -C build\classes .
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ [ERROR] Hubo un problema al crear el archivo JAR.
    del temp_manifest.mf
    pause
    exit /b %ERRORLEVEL%
)

del temp_manifest.mf
echo.
echo =======================================================
echo  ✅ [OK] Compilacion y empaquetamiento completado!
echo  JAR creado en: dist\Restaurante_comuneros.jar
echo =======================================================
echo.
