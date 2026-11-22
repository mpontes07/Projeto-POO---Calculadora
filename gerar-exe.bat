@echo off
setlocal
cd /d "%~dp0"

where jpackage >nul 2>&1
if errorlevel 1 (
    echo JDK not found. Install a Windows JDK 21 or newer and add it to PATH.
    pause
    exit /b 1
)

if not exist "Calculadora.jar" (
    echo Calculadora.jar was not found.
    pause
    exit /b 1
)

if not exist "app" mkdir "app"
copy /Y "Calculadora.jar" "app\Calculadora.jar" >nul
if not exist "saida" mkdir "saida"
jpackage --type app-image --name Calculadora --input "app" --main-jar "Calculadora.jar" --main-class CalculadoraApp --dest "saida"
if errorlevel 1 (
    echo Packaging failed. Check that jpackage is from a Windows JDK.
    pause
    exit /b 1
)

echo.
echo Created: %CD%\saida\Calculadora\Calculadora.exe
pause
