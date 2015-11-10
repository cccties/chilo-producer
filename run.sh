#!/bin/sh

usage()
{
	echo "usage: $0 -c <name of your folder> -t <book type> [ -i <input dir> -o <output_dir> -f <output_name> ]"
	exit 0
}


### parse command line options
RUNMODE=real
while getopts dc:t:i:o:f: OPT
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
	"t" )  
		OUTPUT_TYPE=$OPTARG
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

if [ X${OUTPUT_TYPE} = X ]; then
	echo "no book type specified."
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
	if [ X${OUTPUT_TYPE} = Xepub ]; then
		cp -r ./page_templates/templates_epub ./chiloPro/common/templates
		java -jar ./chilo-epub3-maker.jar -course ${COURSE} -input-path ${INPUT_DIR} -output-path ${OUTPUT_DIR} ${ONAME_OPT}
		rm -rf ./chiloPro/common/templates
	elif [ X${OUTPUT_TYPE} = Xweb ]; then
		cp -r ./page_templates/templates_epub ./chiloPro/common/templates
		cp -r ./page_templates/templates_web ./chiloPro/common/web-templates
		mkdir ./escape

		COURSEBASE_DIR=`sed -n '/CourseBaseDir/p' chilo-epub3-maker.xml | sed -e 's/<[^>]*>//g'`
		FONT_DIR=$(cd $COURSEBASE_DIR/$COURSE && cd $INPUT_DIR/common && pwd)

		if [ -e $FONT_DIR/fonts ]; then
			mv $FONT_DIR/fonts ./escape/
		fi

		java -jar ./chilo-epub3-maker.jar -publish html -course ${COURSE} -input-path ${INPUT_DIR} -output-path ${OUTPUT_DIR} ${ONAME_OPT}
		rm -rf ./chiloPro/common/templates
		rm -rf ./chiloPro/common/web-templates

		if [ -e ./escape/fonts ]; then
			mv ./escape/fonts $FONT_DIR 
		fi

		rm -rf ./escape
	else
		usage
	fi
else
	echo "DRY-RUN: java -jar ./chilo-epub3-maker.jar -course ${COURSE} -type ${OUTPUT_TYPE} -input-path ${INPUT_DIR} -output-path ${OUTPUT_DIR} ${ONAME_OPT}"
fi
