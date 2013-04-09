@echo off

set SEADAS_HOME=${installer:sys.installationDir}

echo.
@echo Welcome to the BEAM command-line interface!
@echo The following command-line tools are available:
@echo   gpt.bat            - General Graph Processing Tool
@echo   pconvert.bat       - General product conversion and quicklook generation
@echo   binning.bat        - General level 3 binning processor
@echo   flhmci.bat         - General FLH / MCI processor
echo "  meris-cloud.bat    - Envisat/MERIS cloud probability processor"
@echo   meris-smac.bat     - Envisat/MERIS atmospheric correction (SMAC)
@echo   aatsr-sst.bat      - Envisat/AATSR sea surface temperaure processor
@echo   seadas-d.bat       - SeaDAS application launcher for debugging
@echo Typing the name of the tool will output its usage information.
echo.

cd "%SEADAS_HOME%\bin"

prompt $G$S
