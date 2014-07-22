README 
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

This plugin was developed by Paul Woelfel (r00tat). You can contact me on github.

This software is licenses under GPL v3. 

The icons currently in use are from the [Tracer Plugin](http://wiki.openstreetmap.org/wiki/JOSM/Plugins/Tracer). 
