package org.openstreetmap.josm.plugins.areaselector;


import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.areaselector.preferences.AreaSelectorPreference;

/**
 * This is the main class for the sumoconvert plugin.
 * 
 */
public class AreaSelectorPlugin extends Plugin{
	
	AreaSelectorAction areaSelectorAction;
	
	AddressDialogAction addressDialogAction;
	
	public AreaSelectorPlugin(PluginInformation info) {
        super(info);
        
        ConsoleAppender console = new ConsoleAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c:%L: %m %x%n"),
				ConsoleAppender.SYSTEM_OUT);

		// BasicConfigurator.configure(console);
		Logger.getRootLogger().addAppender(console);
		Logger.getRootLogger().setLevel(Level.INFO);

        
        areaSelectorAction=new AreaSelectorAction(Main.map);
        MainMenu.add(Main.main.menu.moreToolsMenu, areaSelectorAction);
        
        addressDialogAction=new AddressDialogAction(Main.map);
        MainMenu.add(Main.main.menu.moreToolsMenu, addressDialogAction);
    }
    
    /**
     * Called when the JOSM map frame is created or destroyed. 
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {             
        areaSelectorAction.updateMapFrame(oldFrame, newFrame);
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new AreaSelectorPreference(this);
    }

	/**
	 * @return the areaSelectorAction
	 */
	public AreaSelectorAction getAreaSelectorAction() {
		return areaSelectorAction;
	}


}
