#! /bin/sh

export SEADAS_HOME=${installer:sys.installationDir}

if [ -z "$SEADAS_HOME" ]; then
    echo
    echo Error: SEADAS_HOME not found in your environment.
    echo Please set the SEADAS_HOME variable in your environment to match the
    echo location of the SeaDAS 7.x installation
    echo
    exit 2
fi

export PATH=$PATH:$SEADAS_HOME/bin

echo ""
echo "Welcome to the BEAM command-line interface!"
echo "The following command-line tools are available:"
echo "  gpt.command            - General Graph Processing Tool"
echo "  pconvert.command       - General product conversion and quicklook generation"
echo "  binning.command        - General level 3 binning processor"
echo "  flhmci.command         - General FLH / MCI processor"
echo "  meris-cloud.command    - Envisat/MERIS cloud probability processor"
echo "  meris-smac.command     - Envisat/MERIS atmospheric correction (SMAC)"
echo "  aatsr-sst.command      - Envisat/AATSR sea surface temperaure processor"
echo "  seadas-d.command       - SeaDAS application launcher for debugging"
echo "Typing the name of the tool will output its usage information."
echo ""
