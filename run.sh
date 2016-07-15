#!/bin/sh

usage()
{
	echo "usage: $0 -c <name of your directory> -s <name of the applied book template> [ -i <input dir> -o <output_dir> -f <output_name> ]"
	exit 0
}


### parse command line options
RUNMODE=real
while getopts dc:s:i:o:f: OPT
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
	"s" )  
		BOOK_TEMPLATE=$OPTARG
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

if [ X${BOOK_TEMPLATE} = X ]; then
	echo "no template name specified."
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
		echo "DRY-RUN: java -jar ./chilo-epub3-maker.jar -course ${COURSE} -input-path ${INPUT_DIR} -output-path ${OUTPUT_DIR} ${ONAME_OPT}"
		
elif [ X${RUNMODE} = Xreal ]; then
#	if [ X${BOOK_TEMPLATE} = Xepub ]; then
		COURSEBASE_DIR=`sed -n '/CourseBaseDir/p' chilo-epub3-maker.xml | sed -e 's/<[^>]*>//g' | tr -d '\r' | tr -d '\n'`
		cp -R ./book_templates/${BOOK_TEMPLATE}/page_templates ./${COURSEBASE_DIR}/common/templates
		cp -R ./book_templates/${BOOK_TEMPLATE}/styles ./${COURSEBASE_DIR}/${COURSE}/${INPUT_DIR}/common/
		cp -R ./book_templates/${BOOK_TEMPLATE}/images ./${COURSEBASE_DIR}/common/
		
		java -jar ./chilo-epub3-maker.jar -course ${COURSE} -input-path ${INPUT_DIR} -output-path ${OUTPUT_DIR} ${ONAME_OPT}
		
		rm -rf ./${COURSEBASE_DIR}/common/templates
		rm -rf ./${COURSEBASE_DIR}/${COURSE}/${INPUT_DIR}/common/styles
		rm -rf ./${COURSEBASE_DIR}/common/images
		
else
	usage
fi
