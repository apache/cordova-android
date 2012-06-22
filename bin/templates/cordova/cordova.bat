@ECHO OFF
IF NOT DEFINED JAVA_HOME GOTO MISSING
FOR %%X in (java.exe ant.bat android.bat) do (
    SET FOUND=%%~$PATH:X
    IF NOT DEFINED FOUND GOTO MISSING
)
cscript %~dp0\cordova.js %*
GOTO END
:MISSING
ECHO Missing one of the following:
ECHO JDK: http://java.oracle.com
ECHO Android SDK: http://developer.android.com
ECHO Apache ant: http://ant.apache.org
EXIT /B 1
:END
