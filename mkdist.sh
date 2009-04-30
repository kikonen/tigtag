rm -fr dist/*
mkdir dist

jar cvf dist/tick.jar -C ./bin .
jar uvf dist/tick.jar -C ./resources .

jar cvf dist/kui.jar -C ../kui/bin .
jar uvf dist/kui.jar -C ../kui/resources .

cp ../kui/lib/*.jar dist
cp lib/*.jar dist

echo "java -cp log4j-1.2.14.jar:TableLayout.jar:jhighlight-1.0.jar:kui.jar:tick.jar org.kari.tick.TickMain \$@" > dist/tick.sh
echo "start javaw -cp log4j-1.2.14.jar;TableLayout.jar;jhighlight-1.0.jar;kui.jar;tick.jar org.kari.tick.TickMain %1 %2 %3 %4 %5 %6 %7 %8 %9" > dist/tick.bat

chmod ugo+x dist/tick.sh

zip -r tick_dist.zip dist/*
