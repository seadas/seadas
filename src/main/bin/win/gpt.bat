@echo off

set SEADAS_HOME=${installer:sys.installationDir}

"%SEADAS_HOME%\jre\bin\java.exe" ^
    -Xmx1024M ^
    -Dceres.context=seadas ^
    "-Dbeam.mainClass=org.esa.beam.framework.gpf.main.Main" ^
    "-Dbeam.home=%SEADAS_HOME%" ^
    "-Dncsa.hdf.hdflib.HDFLibrary.hdflib=%SEADAS_HOME%\modules\lib-hdf-${hdf.version}\lib\jhdf.dll" ^
    "-Dncsa.hdf.hdf5lib.H5.hdf5lib=%SEADAS_HOME%\modules\lib-hdf-${hdf.version}\lib\jhdf5.dll" ^
    -jar "%SEADAS_HOME%\bin\ceres-launcher.jar" %*

exit /B %ERRORLEVEL%
