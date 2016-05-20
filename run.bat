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
	if /I "%1" == "-s" call :SETBOOKTEMPLATE %2; shift
	if /I "%1" == "-o" call :SETOUTPUTDIR %2; shift
	if /I "%1" == "-f" call :SETOUTPUTNAME %2; shift
	shift
if not (%1)==() goto GETOPTS

goto MAIN

:usage
	echo "%PROG% -c <name of your directory> -s <name of the applied book template> [ -i <input dir> -o <output dir> -f <output name> ]"
	goto ALLDONE

:CLEARVALS
	set RUNMODE=
	set COURSE=
	set DRYRUN=
	set COURSE=
	set INPUTDIR=
	set BOOKTEMPLATE=
	set OUTPUTDIR=
	set OUTPUTNAME=
	set STR=
	set INPUTDIR2=
	set COURSEBASE_DIR=
	set COURSEBASE_DIR2=
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

:SETBOOKTEMPLATE
	set BOOKTEMPLATE=%1
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
	echo "dry-run:java -jar chilo-epub3-maker.jar -c %COURSE% %BOOKTEMPLATE% %INPUTDIR% %OUTPUTDIR% %OUTPUTNAME%"
) else (
	if "%BOOKTEMPLATE%"=="" (
		goto usage
	) else if "%BOOKTEMPLATE%"=="shift" (
		goto usage
	)else (
		goto EPUB
	)
)

:EPUB
	for /F "delims=" %%Z IN ('findstr /n /r "." chilo-epub3-maker.xml ^| findstr /r "CourseBaseDir"') DO (SET COURSEBASE_DIR=%%Z)
rem	Remove xml tags
	for %%Y IN ( %COURSEBASE_DIR:~29,-8% ) DO (SET COURSEBASE_DIR2=%%Y)

	xcopy book_templates\%BOOKTEMPLATE%\page_templates %COURSEBASE_DIR2%\common\templates /D /S /R /Y /I /K
	xcopy book_templates\%BOOKTEMPLATE%\styles %COURSEBASE_DIR2%\%COURSE%\%INPUTDIR2%\common\styles /D /S /R /Y /I /K
	xcopy book_templates\%BOOKTEMPLATE%\images %COURSEBASE_DIR2%\common\images /D /S /R /Y /I /K

	java -jar chilo-epub3-maker.jar -course %COURSE% %INPUTDIR% %OUTPUTDIR% %OUTPUTNAME%

	rd /s /q %COURSEBASE_DIR2%\common\templates
	rd /s /q %COURSEBASE_DIR2%\%COURSE%\%INPUTDIR2%\common\styles
	rd /s /q %COURSEBASE_DIR2%\common\images

	goto ALLDONE

:ALLDONE
	call :CLEARVALS

