#!/usr/bin/csh
echo "hallo"
echo $1
sed -f ren.sed $1 > tmp
mv -f tmp $1

