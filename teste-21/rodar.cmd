@echo off
rem Abre o Pitiupi com os plugins Chat e Vinte e Um (21) ja compilados.
setlocal
cd /d "%~dp0"
set "PITIUPI_HOME=%~dp0dados"

set "JAVA_EXE="
where java >nul 2>nul && set "JAVA_EXE=java"
if not defined JAVA_EXE (
  for /d %%D in ("%USERPROFILE%\.jdks\*") do if exist "%%D\bin\java.exe" set "JAVA_EXE=%%D\bin\java.exe"
)
if not defined JAVA_EXE (
  echo Java nao encontrado. Instale o JDK 17+ ^(winget install Microsoft.OpenJDK.17^) e rode de novo.
  pause
  exit /b 1
)

echo Usando: %JAVA_EXE%
"%JAVA_EXE%" -version
"%JAVA_EXE%" -Dfile.encoding=UTF-8 -jar pitiupi.jar
if errorlevel 1 pause
