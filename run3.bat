@echo off

if "%2"=="" (
          goto no-output-name
) else (
          goto output-name
)

:no-output-name
java -jar chilo-epub3-maker.jar -course %1  -input-path ./ -output-path ./
goto END

:output-name
java -jar chilo-epub3-maker.jar -course %1  -input-path ./ -output-path ./ -output-name %2
goto END

:END