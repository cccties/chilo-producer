@echo off
java -jar chilo-epub3-maker.jar -publish html -2 -course %1 -input-path ./ -output-path ./ -output-name %2
