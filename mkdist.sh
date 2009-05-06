DIST_VERSION=$1
DIST_PUBLIC=$2
DIST_FILE=tigtag_${DIST_VERSION}.zip

if [ "$DIST_VERSION" == "" ]; then
    echo "USAGE: mkdist.sh VERSION [public]"
    echo "Example: mkdist.sh 0.32a public"
    exit
fi

rm -f tigtag_*.zip
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

zip -r $DIST_FILE dist/*
mkdir releases
cp $DIST_FILE releases

if [ "$DIST_PUBLIC" == "public" ]; then
    WWW=/home/www/virtual/kari.dy.fi/tigtag
    rm -fr $WWW/*
    cp $DIST_FILE $WWW
    cp orig/sample.png $WWW
    cd $WWW
    unzip $DIST_FILE
else
    echo "NOTE: NOT PUBLISHED"
fi

echo "DONE: $DIST_FILE"

