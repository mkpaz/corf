#!/usr/bin/env sh

JVM_OPTIONS=-Xmx256M
DIR=$(dirname "$0")
IFS="$(printf '\n\t')"

export TELEKIT_APP_DIR="${DIR}"
"${DIR}/app/bin/java" ${JVM_OPTIONS} -m telekit.desktop/org.telekit.desktop.Launcher
