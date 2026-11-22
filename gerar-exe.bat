@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>&1
if errorlevel 1 (
    echo JDK not found. Install a Windows JDK 21 or newer and add it to PATH.
    pause
    exit /b 1
)
where jar >nul 2>&1
if errorlevel 1 (
    echo The JDK jar tool was not found in PATH.
    pause
    exit /b 1
)
where jpackage >nul 2>&1
if errorlevel 1 (
    echo The JDK jpackage tool was not found in PATH.
    pause
    exit /b 1
)

if not exist "build\classes" mkdir "build\classes"
if not exist "build\app" mkdir "build\app"
javac -encoding UTF-8 -d "build\classes" "CalculadoraApp.java"
if errorlevel 1 goto :error

if exist "build\app\Calculadora.jar" del /q "build\app\Calculadora.jar"
jar --create --file "build\app\Calculadora.jar" --main-class CalculadoraApp -C "build\classes" .
if errorlevel 1 goto :error

if not exist "saida" mkdir "saida"
jpackage --type app-image --name Calculadora --input "build\app" --main-jar "Calculadora.jar" --main-class CalculadoraApp --dest "saida"
if errorlevel 1 goto :error

echo.
echo Created complete app image in %CD%\saida\Calculadora
pause
exit /b 0

:error
echo.
echo Build failed. Check the JDK installation and the messages above.
pause
exit /b 1
