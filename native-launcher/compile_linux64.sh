#!/usr/bin/env sh

GOARCH=amd64
GOOS=linux

go build -o target/linux64

mkdir -p "../telekit-desktop/build/native/"
cp "target/linux64" "../telekit-desktop/build/native/linux64"
