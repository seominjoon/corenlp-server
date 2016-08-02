#!/usr/bin/env bash

JAVA_LIB_DIR=$HOME/lib/java/

# Download CoreNLP library
wget http://nlp.stanford.edu/software/stanford-corenlp-full-2015-12-09.zip -O $JAVA_LIB_DIR/stanford-corenlp.zip
unzip $JAVA_LIB_DIR/stanford-corenlp.zip

# Download Gson jar
wget http://repo1.maven.org/maven2/com/google/code/gson/gson/2.7/gson-2.7.jar $JAVA_LIB_DIR/gson-2.7.jar
