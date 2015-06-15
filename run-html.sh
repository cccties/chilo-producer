#!/bin/sh
if [ X$2 != X ]; then
java -jar chilo-epub3-maker.jar -publish html -2 -course $1  -input-path ./ -output-path ./ -output-name $2
else
java -jar chilo-epub3-maker.jar -publish html -2 -course $1  -input-path ./ -output-path ./ 
fi
