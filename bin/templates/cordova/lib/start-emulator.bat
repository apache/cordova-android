@ECHO OFF
SET full_path=%~dp0
IF EXIST %full_path%cordova.js (
    cscript "%full_path%cordova.js" start-emulator %* //nologo
) ELSE (
    ECHO. 
    ECHO ERROR: Could not find 'cordova.js' in cordova/lib, aborting...>&2
    EXIT /B 1
)