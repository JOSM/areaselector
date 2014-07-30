#!/bin/bash

ant clean && ant && cp ../../dist/areaselector.jar ~/.josm/plugins/ && java -jar /Applications/josm-latest.jar

