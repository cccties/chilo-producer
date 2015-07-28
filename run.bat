@echo off

set PROG=%0

call :CLEARVALS

:GETOPTS
	if /I "%1" == "" goto usage
	if /I "%1" == "-h" goto usage
	if /I "%1" == "-d" call :DRYRUN
	if /I "%1" == "-c" call :SETCOURSE %2; shift
	if /I "%1" == "-i" call :SETINPUTDIR %2; shift
	if /I "%1" == "-o" call :SETOUTPUTDIR %2; shift
	if /I "%1" == "-f" call :SETOUTPUTNAME %2; shift
	shift
if not (%1)==() goto GETOPTS

goto MAIN

:usage
	echo "%PROG% -c <course name> [ -i <input dir> -o <output_dir> -f <output_name> ]"
	goto ALLDONE

:CLEARVALS
	set RUNMODE=
	set COURSE=
	set DRYRUN=
	set COURSE=
	set INPUTDIR=
	set OUTPUTDIR=
	set OUTPUTNAME=
	exit /b

:DRYRUN
	set RUNMODE=dry
	exit /b

:SETCOURSE
	set COURSE=%1
	exit /b

:SETINPUTDIR
	set INPUTDIR=-input-path %1
	exit /b

:SETOUTPUTDIR
	set OUTPUTDIR=-output-path %1
	exit /b

:SETOUTPUTNAME
	set OUTPUTNAME=-output-name %1
	exit /b

:MAIN

if "%COURSE%"=="" goto usage


if "%RUNMODE%"=="dry" (
	echo "dry-run:java -jar chilo-epub3-maker.jar -c %COURSE% %INPUTDIR% %OUTPUTDIR% %OUTPUTNAME%"
) else (
	java -jar chilo-epub3-maker.jar -course %COURSE% %INPUTDIR% %OUTPUTDIR% %OUTPUTNAME%
)

:ALLDONE
	call :CLEARVALS

