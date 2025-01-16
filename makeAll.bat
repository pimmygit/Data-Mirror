set DMGPATH=%cd%
set LIBPATH=%DMGPATH%\libs\tray.jar
set JAVA_HOME="C:\Program Files (x86)\Java\jdk1.6.0_45\bin"

IF EXIST build (
	rmdir /S/Q build
)
IF EXIST classes (
	rmdir /S/Q classes
)

mkdir %DMGPATH%\build
mkdir %DMGPATH%\build\log
mkdir %DMGPATH%\build\props
mkdir %DMGPATH%\build\libs
mkdir %DMGPATH%\classes

copy images %DMGPATH%\build\
copy libs\tray.* %DMGPATH%\build\libs\

copy %DMGPATH%\Stuff\DMUtility.bat %DMGPATH%\build\
copy %DMGPATH%\Stuff\DMUtility.exe %DMGPATH%\build\
copy %DMGPATH%\Stuff\dmgutil.prp %DMGPATH%\build\props\

%JAVA_HOME%\javac -O -d %DMGPATH%\classes -classpath %LIBPATH% %DMGPATH%\src\gui\*.java %DMGPATH%\src\utils\*.java %DMGPATH%\src\log\*.java %DMGPATH%\src\log\exception\*.java

cd %DMGPATH%\classes

%JAVA_HOME%\jar cvf %DMGPATH%\build\libs\DMGUtility.jar *

cd ..

REM chmod -R 777 BUILD

PAUSE