#!/bin/sh

(cd build; cd ..) > /dev/null 2>&1 || (echo "Couldn't find the build/ folder. Have you compiled your program? :-]"; exit 1)

cp="build;jade/lib/jade.jar;lib/json-simple-1.1.1.jar;lib/junit-4.10.jar;lib/hamcrest-code-1.1.jar"

if [ $# -eq 1 ]
    then
        java -classpath $cp Jade -gui -name medical-emergency-helicopters "$1" false
        exit 0
elif [ $# -eq 2 ]
    then
        java -classpath $cp Jade -gui -name medical-emergency-helicopters "$1" "$2"
        exit 0
else
    echo "Usage:  $0 <json-file> [ <test> ]"
    echo ""
    echo "        json-file:  path to file (from test_files directory)"
    echo "                    containing helicopters, hospitals and patients"
    echo ""
    echo "        test:       FALSE (default) if logger should document execution"
    echo "                    TRUE if testing only (no logging)"
    exit 1
fi
