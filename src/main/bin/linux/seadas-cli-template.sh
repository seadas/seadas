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

. "$SEADAS_HOME/bin/detect_java.sh"

"$app_java_home/bin/java" \
    -Xmx${installer:maxHeapSize} \
    -Dceres.context=seadas \
    "-Dseadas.mainClass=${seadas.mainClass}" \
    "-Dseadas.processorClass=${seadas.processorClass}" \
    "-Dseadas.home=$SEADAS_HOME" \
    "-Dncsa.hdf.hdflib.HDFLibrary.hdflib=$SEADAS_HOME/modules/lib-hdf-${hdf.version}/lib/libjhdf.so" \
    "-Dncsa.hdf.hdf5lib.H5.hdf5lib=$SEADAS_HOME/modules/lib-hdf-${hdf.version}/lib/libjhdf5.so" \
    -jar "$SEADAS_HOME/bin/ceres-launcher.jar" "$@"

exit $?
