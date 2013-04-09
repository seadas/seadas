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
echo "Welcome to the SeaDAS command-line interface!"
echo "The following command-line tools are available:"
echo "  gpt.sh            - General Graph Processing Tool"
echo "  pconvert.sh       - General product conversion and quicklook generation"
echo "  binning.sh        - General level 3 binning processor"
echo "  flhmci.sh         - General FLH / MCI processor"
echo "  meris-cloud.sh    - Envisat/MERIS cloud probability processor"
echo "  meris-smac.sh     - Envisat/MERIS atmospheric correction (SMAC)"
echo "  aatsr-sst.sh      - Envisat/AATSR sea surface temperaure processor"
echo "  seadas-d.sh        - SeaDAS application launcher for debugging"
echo "Typing the name of the tool will output its usage information."
echo ""
