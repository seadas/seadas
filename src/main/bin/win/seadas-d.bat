@echo off

set SEADAS_HOME=${installer:sys.installationDir}

"%SEADAS_HOME%\jre\bin\java.exe" ^
    -Xmx${installer:maxHeapSize} ^
    -Dceres.context=seadas ^
    -Dseadas.debug=true ^
    "-Dseadas.home=%SEADAS_HOME%" ^
    -jar "%SEADAS_HOME%\bin\ceres-launcher.jar" -d %*

exit /B %ERRORLEVEL%
