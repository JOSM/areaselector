package org.openstreetmap.josm.plugins.areaselector;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * This is the main class for the sumoconvert plugin.
 * 
 */
public class AreaSelectorPlugin extends Plugin{
	
	AreaSelectorAction areaSelectorAction;
    
	public AreaSelectorPlugin(PluginInformation info) {
        super(info);
        areaSelectorAction=new AreaSelectorAction(Main.map);
        MainMenu.add(Main.main.menu.moreToolsMenu, areaSelectorAction);
    }
    
    /**
     * Called when the JOSM map frame is created or destroyed. 
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {             
        areaSelectorAction.updateMapFrame(oldFrame, newFrame);
    }

//    @Override
//    public PreferenceSetting getPreferenceSetting() {
//        return new PreferenceEditor();
//    }
}
