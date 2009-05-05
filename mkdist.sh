rm -fr dist/*
mkdir dist

jar cvf dist/tigtag.jar -C ./bin .
jar uvf dist/tigtag.jar -C ./resources .

jar cvf dist/kui.jar -C ../kui/bin .
jar uvf dist/kui.jar -C ../kui/resources .

cp ../kui/lib/*.jar dist
cp lib/*.jar dist
cp script/*.bat dist
cp script/*.sh dist

chmod ugo+x dist/tigtag.sh

zip -r tigtag_dist.zip dist/*

cp tigtag_dist.zip /home/www/virtual/kari.dy.fi/tigtag
cp orig/sample.png /home/www/virtual/kari.dy.fi/tigtag
