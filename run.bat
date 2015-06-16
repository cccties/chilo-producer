@echo off

if "%2"=="" (
          java -jar chilo-epub3-maker.jar -course %1  -input-path ./ -output-path ./
) else (
          java -jar chilo-epub3-maker.jar -course %1  -input-path ./ -output-path ./ -output-name %2
)