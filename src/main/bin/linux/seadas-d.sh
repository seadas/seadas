#! /bin/sh

export SEADAS_HOME=${installer:sys.installationDir}

if [ -z "$SEADAS_HOME" ]; then
    echo
    echo Error: SEADAS_HOME not found in your environment.
    echo Please set the SEADAS_HOME variable in your environment to match the
    echo location of the SeaDAS 7.x installation.
    echo
    exit 2
fi

. "$SEADAS_HOME/bin/detect_java.sh"

"$app_java_home/bin/java" \
    -Xmx${installer:maxHeapSize} \
    -Dceres.context=seadas \
    -Dseadas.debug=true \
    "-Dseadas.home=$SEADAS_HOME" \
    -jar "$SEADAS_HOME/bin/ceres-launcher.jar" -d "$@"

exit $?


