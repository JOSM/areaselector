JOSM Area Selector Plugin 
======

JOSM area selector is a plugin to help creating buildings and other areas from existing imagery. Currently creating buildings is a manual task which takes much time. This plugin should help on that task. 

## Documentation

You can find out more about how to use the plugin and how it works in the [project Wiki](https://github.com/JOSM/JOSM-areaselector/wiki).

If you want to know how to configure the plugin, you can look at the [Configuration Wiki page](https://github.com/JOSM/JOSM-areaselector/wiki/Configuration).

## Building from source
Checkout the JOSM source, compile it and checkout the plugin source:

    svn co http://svn.openstreetmap.org/applications/editors/josm josm
    cd josm/core
    ant clean dist
    cd ../plugins
    rm -rf areaselector
    git clone https://github.com/JOSM/JOSM-areaselector.git areaselector
    cd areaselector
    ant clean install
    
Now Restart JOSM and activate the areaselector plugin in your preferences. 
Under Tools you should now see a new Tool called "Area Selector". 

Details about plugin development can be found [in the JOSM wiki](https://josm.openstreetmap.de/wiki/DevelopersGuide/DevelopingPlugins).

## License

This plugin was developed by Paul Woelfel (r00tat) and Thomas Konrad (thomaskonrad). You can contact us on github.

This software is licensed under [GPL v3](https://www.gnu.org/licenses/gpl-3.0.en.html). 

### Third party resources

For image manipulation [Marvin Project](http://marvinproject.sourceforge.net/) is used. The code was rebundled as jar files to make it easier to include. The MarvinPluginLoader was extended to search for already loaded classes first, before loading Plugins directly from jar files. The source is included in the jar files. 

The second image manipulation library, which is also used to detect polygons is [boofcv](http://boofcv.org/). This software is licensed under Apache License, Version 2.0. 

The icons are based on [Tracer Plugin](https://wiki.openstreetmap.org/wiki/JOSM/Plugins/Tracer). 
