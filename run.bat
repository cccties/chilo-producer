@echo off

set PROG=%0

call :CLEARVALS

set STR=%~dp0

:SETDEFAULTTEMPLATE
	set BOOKTEMPLATE=-template cardview

:GETOPTS
	if /I "%1" == "" (
		goto usage
	) else if /I "%1" == "-h" (
		goto usage
	) else if /I "%1" == "-d" (
		call :DRYRUN
	) else if /I "%1" == "-s" (
		call :SETSERIES %2
		shift
	) else if /I "%1" == "-i" (
		call :SETINPUTDIR %2
		shift
	) else if /I "%1" == "-t" (
		call :SETBOOKTEMPLATE %2
		shift
	) else if /I "%1" == "-o" (
		call :SETOUTPUTDIR %2
		shift
	) else if /I "%1" == "-f" (
		call :SETOUTPUTNAME %2
		shift
	) else if /I "%1" == "-w" (
		call :SETWEKO
	) else (
		call :SETDRAGANDDROP
		call :SETSERIES %1
	)
	shift
if not (%1)==() goto GETOPTS

goto MAIN

:usage
	echo "%PROG% -s <name of your directory> -t <name of the applied book template> [ -i <input dir> -o <output dir> -f <output name> ]"
	goto ALLDONE

:CLEARVALS
	set RUNMODE=
	set SERIES=
	set INPUTDIR=
	set BOOKTEMPLATE=
	set OUTPUTDIR=
	set OUTPUTNAME=
	set WEKO=
	set DRAGANDDROP=
	exit /b

:DRYRUN
	set RUNMODE=dry
	exit /b

:SETSERIES
	set SERIES=%1
	exit /b

:SETINPUTDIR
	set INPUTDIR=-input-path %1
	exit /b

:SETBOOKTEMPLATE
	set BOOKTEMPLATE=-template %1
	exit /b

:SETOUTPUTDIR
	set OUTPUTDIR=-output-path %1
	exit /b

:SETOUTPUTNAME
	set OUTPUTNAME=-output-name %1
	exit /b

:SETWEKO
	set WEKO=-weko
	exit /b

:SETDRAGANDDROP
	set DRAGANDDROP=on
	exit /b

:MAIN

if "%SERIES%"=="" goto usage

if "%RUNMODE%"=="dry" (
	echo "dry-run:java -jar %STR%\chilo-epub3-maker.jar -series %SERIES% %INPUTDIR% %OUTPUTDIR% %BOOKTEMPLATE% %OUTPUTNAME%" -home %STR% %WEKO%
	goto ALLDONE
)

:EPUB
	java -jar %STR%\chilo-epub3-maker.jar -series %SERIES% %INPUTDIR% %OUTPUTDIR% %BOOKTEMPLATE% %OUTPUTNAME% -home %STR% %WEKO%

	goto ALLDONE

:ALLDONE

if "%DRAGANDDROP%"=="on" (
	PAUSE
)

	call :CLEARVALS
