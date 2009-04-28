rm -fr dist/*
mkdir dist

jar cvf dist/tick.jar -C ./bin .
jar uvf dist/tick.jar -C ./resources .

jar cvf dist/kui.jar -C ../kui/bin .
jar uvf dist/kui.jar -C ../kui/resources .

cp ../kui/lib/*.jar dist

echo "java -cp log4j-1.2.14.jar:TableLayout.jar:kui.jar:tick.jar org.kari.tick.TickMain \$@" > dist/tick.sh
chmod ugo+x dist/tick.sh

zip -r tick_dist.zip dist/*
