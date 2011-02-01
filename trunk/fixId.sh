#!/bin/sh
for i in $(find ./ -name \*.java -exec grep -L \$Id {} \;) ; do
cat $i | sed -e 's/^package/\/\/ \$Id\$\n\npackage/' > $i.tmp
mv $i.tmp $i
done
