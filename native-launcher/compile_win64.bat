@echo off

set GOARCH=amd64
set GOOS=windows

go build -ldflags -H=windowsgui -o target/win64.exe
ResourceHacker.exe -open target/win64.exe -save target/win64.exe -action addskip -res telekit.ico -mask ICONGROUP,MAINICON,

md "..\telekit-ui\build\native\" >nul 2>&1
copy /y "target\win64.exe" "..\telekit-ui\build\native\win64.exe"
