rm -fr dist/*
mkdir dist

jar cvf dist/tigtag.jar -C ./bin .
jar uvf dist/tigtag.jar -C ./resources .

jar cvf dist/kui.jar -C ../kui/bin .
jar uvf dist/kui.jar -C ../kui/resources .

cp ../kui/lib/*.jar dist
cp lib/*.jar dist

echo "java -cp log4j-1.2.14.jar:TableLayout.jar:jhighlight-1.0.jar:kui.jar:tigtag.jar org.kari.tick.TickMain \$@" > dist/tigtag.sh
echo "start javaw -cp log4j-1.2.14.jar;TableLayout.jar;jhighlight-1.0.jar;kui.jar;tigtag.jar org.kari.tick.TickMain %1 %2 %3 %4 %5 %6 %7 %8 %9" > dist/tigtag.bat

chmod ugo+x dist/tigtag.sh

zip -r tigtag_dist.zip dist/*
