
CLASSPATH=$SPARKCQL_OUT_DIR/cqlengine/server/libs/osa.spark-cql.cqlengine.server-shaded.jar 

INPFILES=(
inpS0.txt 
)

OUTFILES=(
outVisDemo1.txt
outVisDemo2.txt
)

umask 111

for name in ${INPFILES[@]}
do
  rm -f /tmp/$name
  cp $name /tmp/$name
done

for name in ${OUTFILES[@]}
do
  rm -f $name.dif
  rm -f $name.suc
  rm -f /tmp/$name.suc
done


rm -f /tmp/tmp/CEPMetadata.xml

JAVASTR="java -ea -classpath ${CLASSPATH} VisDemo"
${JAVASTR}
