@echo off

set GOARCH=amd64
set GOOS=linux

go build -o target/linux64

md "..\telekit-ui\build\native\" >nul 2>&1
copy /y "target\linux64" "..\telekit-ui\build\native\linux64"
