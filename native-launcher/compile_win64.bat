@echo off

set GOARCH=amd64
set GOOS=windows

go build -ldflags -H=windowsgui -o target/win64.exe
ResourceHacker.exe -open target/win64.exe -save target/win64.exe -action addskip -res telekit.ico -mask ICONGROUP,MAINICON,

md "..\telekit-desktop\build\native\" >nul 2>&1
copy /y "target\win64.exe" "..\telekit-desktop\build\native\win64.exe"
