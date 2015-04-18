#!/usr/bin/env bash
ME=`basename $0` # for usage message

if [ "$#" -ne 0 ]; then 	# number of args
    echo "USAGE: "
    echo "$ME"
    exit
fi



git clone git@github.com:sammthomson/ChuLiuEdmonds.git
cd ChuLiuEdmonds
mvn install




if [[ $? == 0 ]]        # success
then
    :                   # do nothing
else                    # something went wrong
    echo "SOME PROBLEM OCCURED";            # echo file with problems
fi
