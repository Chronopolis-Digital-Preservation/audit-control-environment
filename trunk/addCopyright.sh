#!/bin/sh
for i in $(find . -name \*.java -a ! -name package-info.java ); do
cat $1 $i > $i.tmp;
mv $i.tmp $i;
done
