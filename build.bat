Pushd "%~dp0"
xcopy bin\data build\libs\data /s /d
xcopy bin\shaders build\libs\shaders /s /d
gradle build