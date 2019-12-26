@echo off

set GOARCH=386
set GOOS=windows

go build -ldflags -H=windowsgui -o target/win32.exe
ResourceHacker.exe -open target/win32.exe -save target/win32.exe -action addskip -res telekit.ico -mask ICONGROUP,MAINICON,

md "..\telekit-ui\build\native\" >nul 2>&1
copy /y "target\win32.exe" "..\telekit-ui\build\native\win32.exe"
