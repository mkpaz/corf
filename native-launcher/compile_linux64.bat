@echo off

set GOARCH=amd64
set GOOS=linux

go build -o target/linux64

md "..\telekit-desktop\build\native\" >nul 2>&1
copy /y "target\linux64" "..\telekit-desktop\build\native\linux64"
