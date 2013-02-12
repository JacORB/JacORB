#!/bin/sh
bn=${0##*/}
rm -f output/test_output.log
rm -f output/test_output.dif.log
touch output/test_output.log
echo "Input#: $#" > output/test_output.log
echo "Input: $*" >> output/test_output.log

(( cnt = 0 ))
for x in "$@" ; do
  (( cnt = cnt + 1 ))
  echo "$cnt: $x" >> output/test_output.log
done
exp=$(cksum expect_output.template | awk '{print $2}')
test=$(cksum output/test_output.log | awk '{print $2}')

if [[ $exp != $test ]] ; then
  diff output/test_output.log expect_output.template > output/test_output.dif.log 2>&1
  echo "FAILED: test has failed" >> output/test_output.log
  exit 1
else
  echo "PASSED: test has passed" >> output/test_output.log
  exit 0
fi
