#!/bin/sh

usage()
{
	echo "usage: $0 -c <course name> [ -i <input dir> -o <output_dir> -f <output_name> ]"
	exit 0
}


### parse command line options
RUNMODE=real
while getopts dc:i:o:f: OPT
do
        case $OPT in
	"d" )
		RUNMODE=dry
		;;
	"c" )  
		COURSE=$OPTARG
		;;
	"i" )  
		INPUT_DIR=$OPTARG
		;;
	"o" )  
		OUTPUT_DIR=$OPTARG
		;;
	"f" )
		OUTPUT_NAME=$OPTARG
		;;
	"*" )
		usage
		;;
	esac
done

### check parameters

if [ X${COURSE} = X ]; then
	echo "no course specified."
	usage
fi

if [ X${INPUT_DIR} = X ]; then
	INPUT_DIR=./
fi

if [ X${OUTPUT_DIR} = X ]; then
	OUTPUT_DIR=./
fi

if [ X${OUTPUT_NAME} != X ]; then
	ONAME_OPT="-output-name ${OUTPUT_NAME}"
fi

if [ X${RUNMODE} = Xreal ]; then
	java -jar ./chilo-epub3-maker.jar -course ${COURSE} -input-path ${INPUT_DIR} -output-path ${OUTPUT_DIR} ${ONAME_OPT}
else
	echo "DRY-RUN: java -jar ./chilo-epub3-maker.jar -course ${COURSE} -input-path ${INPUT_DIR} -output-path ${OUTPUT_DIR} ${ONAME_OPT}"
fi
