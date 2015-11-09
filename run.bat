@echo off

set PROG=%0

call :CLEARVALS

set STR=%~dp0

:GETOPTS
	if /I "%1" == "" goto usage
	if /I "%1" == "-h" goto usage
	if /I "%1" == "-d" call :DRYRUN
	if /I "%1" == "-c" call :SETCOURSE %2; shift
	if /I "%1" == "-i" call :SETINPUTDIR %2; shift
	if /I "%1" == "-t" call :SETOUTPUTTYPE %2; shift
	if /I "%1" == "-o" call :SETOUTPUTDIR %2; shift
	if /I "%1" == "-f" call :SETOUTPUTNAME %2; shift
	shift
if not (%1)==() goto GETOPTS

goto MAIN

:usage
	echo "%PROG% -c <name of your folder> -t <book type> [ -i <input dir> -o <output_dir> -f <output_name> ]"
	goto ALLDONE

:CLEARVALS
	set RUNMODE=
	set COURSE=
	set DRYRUN=
	set COURSE=
	set INPUTDIR=
	set OUTPUTTYPE=
	set OUTPUTDIR=
	set OUTPUTNAME=
	set STR=
	set COURSEBASE=
	set INPUTDIR2=
	set FONTDIRECTORY=
	exit /b

:DRYRUN
	set RUNMODE=dry
	exit /b

:SETCOURSE
	set COURSE=%1
	exit /b

:SETINPUTDIR
	set INPUTDIR=-input-path %1
	set INPUTDIR2=%1
	exit /b

:SETOUTPUTTYPE
	set OUTPUTTYPE=%1
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
	echo "dry-run:java -jar chilo-epub3-maker.jar -c %COURSE% %OUTPUTTYPE% %INPUTDIR% %OUTPUTDIR% %OUTPUTNAME%"
) else (
	if "%OUTPUTTYPE%"=="epub" (
		goto EPUB
	) else if "%OUTPUTTYPE%"=="web" (
		goto WEB
	) else (
		goto usage
	)
)

:EPUB
	xcopy page_templates\templates_epub chiloPro\common\templates  /D /S /R /Y /I /K
	java -jar chilo-epub3-maker.jar -course %COURSE% %INPUTDIR% %OUTPUTDIR% %OUTPUTNAME%
	rd /s /q chiloPro\common\templates
	goto ALLDONE

:WEB
	xcopy page_templates\templates_epub chiloPro\common\templates  /D /S /R /Y /I /K
	xcopy page_templates\templates_web chiloPro\common\web-templates  /D /S /R /Y /I /K
	mkdir .\escape
	for /F "delims=" %%Z IN ('findstr /n /r "." chilo-epub3-maker.xml ^| findstr /r "CourseBaseDir"') DO (SET COURSEBASE=%%Z)
	call set FONTDIRECTORY=.\%%COURSEBASE:~29,-8%%\%%COURSE%%\%%INPUTDIR2%%\common\fonts
	if exist "%STR%%FONTDIRECTORY:~2%" (call move %%FONTDIRECTORY%% ".\escape\fonts")
	java -jar chilo-epub3-maker.jar -publish html -course %COURSE% %INPUTDIR% %OUTPUTDIR% %OUTPUTNAME%
	rd /s /q chiloPro\common\templates
	rd /s /q chiloPro\common\web-templates
	if exist "%STR%escape\fonts" (call move ".\escape\fonts" %%FONTDIRECTORY%%)
	rmdir /S /Q .\escape
	goto ALLDONE

:ALLDONE
	call :CLEARVALS

