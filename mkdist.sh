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
cp script/*.jnlp dist
cp resources/icon/tigtag_32.png dist

chmod ugo+x dist/tigtag.sh

# key for 
KEYSTORE=tigtag.keystore
keytool -genkeypair \
  -dname "cn=Kari Ikonen, ou=TigTag, o=KI, c=FI" \
  -alias tigtag -keypass tigtag -keystore $KEYSTORE \
  -storepass tigtag -validity 180

FILES=`ls dist/*.jar`
for file in $FILES; do
  jarsigner -keystore $KEYSTORE -storepass tigtag -keypass tigtag $file tigtag 
done

zip -r tigtag_dist.zip dist/*

WWW=/home/www/virtual/kari.dy.fi/tigtag
rm -fr $WWW/*
cp tigtag_dist.zip $WWW
cp orig/sample.png $WWW
cd $WWW
unzip tigtag_dist.zip

