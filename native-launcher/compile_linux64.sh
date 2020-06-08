#!/usr/bin/env sh

GOARCH=amd64
GOOS=linux

go build -o target/linux64

mkdir -p "../telekit-ui/build/native/"
cp "target/linux64" "../telekit-ui/build/native/linux64"
