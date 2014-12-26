#!/bin/bash

ant clean && ant && cp ../../dist/areaselector.jar ~/Library/JOSM/plugins/ && java -jar /Applications/josm-latest.jar

