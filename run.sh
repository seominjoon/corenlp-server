#!/usr/bin/env bash

mkdir bin
CP="$HOME/lib/java/gson-2.7.jar:$HOME/lib/java/stanford-corenlp/*:$PWD/bin"
javac -cp $CP src/Server.java -d bin/
java -cp $CP Server $1