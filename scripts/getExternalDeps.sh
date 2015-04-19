#!/usr/bin/env bash

git clone git@github.com:sammthomson/ChuLiuEdmonds.git
cd ChuLiuEdmonds
mvn install
cd ..
rm -rf ChuLiuEdmonds



if [[ $? == 0 ]]        # success
then
    :                   # do nothing
else                    # something went wrong
    echo "SOME PROBLEM OCCURED";            # echo file with problems
fi
