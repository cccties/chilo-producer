#!/bin/sh

usage()
{
	echo "usage: $0 -s <name of your directory> -t <name of the applied book template> [ -i <input dir> -o <output_dir> -f <output_name> -w ]"
	exit 0
}


### parse command line options
RUNMODE=real
WEKO=
BOOK_TEMPLATE="-template cardview"
while getopts ds:t:i:o:f:w OPT
do
        case $OPT in
	"d" )
		RUNMODE=dry
		;;
	"s" )  
		SERIES=$OPTARG
		;;
	"i" )  
		INPUT_DIR=$OPTARG
		;;
	"t" )  
		BOOK_TEMPLATE="-template $OPTARG"
		;;
	"o" )  
		OUTPUT_DIR=$OPTARG
		;;
	"f" )
		OUTPUT_NAME=$OPTARG
		;;
	"w" )
		WEKO="-weko"
		;;
	"*" )
		usage
		;;
	esac
done

### excel file and home directory

shift $((OPTIND - 1))
if [ X$1 != X ]; then
	SERIES=$1
fi

if type greadlink > /dev/null 2>&1; then
	HOME_DIR="$(dirname $(greadlink -f $0))"
elif readlink -f $0  > /dev/null 2>&1; then
	HOME_DIR="$(dirname $(readlink -f $0))"
else
	HOME_DIR="$(dirname $0)"
fi

### check parameters

if [ X${SERIES} = X ]; then
	echo "no series specified."
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

if [ X${RUNMODE} = Xdry ]; then
	echo "DRY-RUN: java -jar ${HOME_DIR}/chilo-epub3-maker.jar -series ${SERIES} -input-path ${INPUT_DIR} -output-path ${OUTPUT_DIR} ${BOOK_TEMPLATE} ${ONAME_OPT} -home ${HOME_DIR} ${WEKO}"
		
elif [ X${RUNMODE} = Xreal ]; then
	java -jar ${HOME_DIR}/chilo-epub3-maker.jar -series ${SERIES} -input-path ${INPUT_DIR} -output-path ${OUTPUT_DIR} ${BOOK_TEMPLATE} ${ONAME_OPT} -home ${HOME_DIR} ${WEKO}
else
	usage
fi
