xcopy bin\data build\libs\data /s /d /exclude:build_exclude.txt
xcopy bin\shaders build\libs\shaders /s /d
gradle build