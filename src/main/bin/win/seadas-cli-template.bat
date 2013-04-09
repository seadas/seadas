@echo off

set SEADAS_HOME=${installer:sys.installationDir}

"%SEADAS_HOME%\jre\bin\java.exe" ^
    -Xmx${installer:maxHeapSize} ^
    -Dceres.context=seadas ^
    "-Dseadas.mainClass=${seadas.mainClass}" ^
    "-Dseadas.processorClass=${seadas.processorClass}" ^
    "-Dseadas.home=%SEADAS_HOME%" ^
    "-Dncsa.hdf.hdflib.HDFLibrary.hdflib=%SEADAS_HOME%\modules\lib-hdf-${hdf.version}\lib\jhdf.dll" ^
    "-Dncsa.hdf.hdf5lib.H5.hdf5lib=%SEADAS_HOME%\modules\lib-hdf-${hdf.version}\lib\jhdf5.dll" ^
    -jar "%SEADAS_HOME%\bin\ceres-launcher.jar" %*

exit /B %ERRORLEVEL%
