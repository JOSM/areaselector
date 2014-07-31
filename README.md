JOSM Area Selector Plugin 
======

JOSM area selector is a plugin to help creating buildings and other areas from existing imagery. Currently creating buildings is a manual task which takes much time. This plugin should help on that task. 

## Installation
Checkout the JOSM source, compile it and checkout the plugin source:

    svn co http://svn.openstreetmap.org/applications/editors/josm josm
    cd josm
    ant clean
    ant dist
    cd plugins
    git clone https://github.com/r00tat/JOSM-areaselector.git areaselector
    cd areaselector
    ant clean
    ant
    cp ../../dist/areaselector.jar ~/.josm/plugins/
    
Now Restart JOSM and activate the areaselector plugin in your preferences. 
Under Tools you should now see a new Tool called "Area Selection". 
    

## License

This plugin was developed by Paul Woelfel (r00tat) and Thomas Konrad (thomaskonrad). You can contact us on github.

This software is licensed under GPL v3. 

### Third party resources

For image manipulation [Marvin Project](http://marvinproject.sourceforge.net/) is used. The code was rebundled as jar files to make it easier to include. The MarvinPluginLoader was extended to search for already loaded classes first, before loading Plugins directly from jar files. The source is included in the jar files. 

The second image manipulation library, which is also used to detect polygons is [boofcv](http://boofcv.org/). This software is licensed under Apache License, Version 2.0. 

The icons are based on [Tracer Plugin](http://wiki.openstreetmap.org/wiki/JOSM/Plugins/Tracer). 
