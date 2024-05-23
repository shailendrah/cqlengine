#/bin/sh

if [ $# -eq 0 ]
then
  echo "Usage : debuglog.sh keyword/all on/off"
  echo " keywords - REFCOUNT_DEBUG PARTWIN_DEBUG STORAGE_DEBUG"
  exit 1
fi

keyword=$1
flag=0
if [ $2 = on ]; then
  flag=1
fi

# Keywords
if [ $keyword = all ]; then
  set REFCOUNT_DEBUG PARTWIN_DEBUG STORAGE_DEBUG
else
  set $keyword
fi

cd $SPARK_CQL/cqlengine
for key in $@; do 
  echo "looking for $key"
  find . -exec grep -q $key '{}' \; -print | perl $SPARK_CQL/cqlengine/utl/setup_log.pl -q $key $flag
done
  
