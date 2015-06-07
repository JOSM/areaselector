package org.openstreetmap.josm.plugins.areaselector;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.areaselector.preferences.AreaSelectorPreference;

/**
 * This is the main class for the AreaSelector plugin.
 * 
 */
public class AreaSelectorPlugin extends Plugin{
    
    AreaSelectorAction areaSelectorAction;
    
    AddressDialogAction addressDialogAction;
    
    public AreaSelectorPlugin(PluginInformation info) {
        super(info);
        
        ConsoleAppender console = ConsoleAppender.newBuilder().setLayout(
                PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c:%L: %m %x%n").build())
                .build();

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        LoggerConfig config = ctx.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        config.addAppender(console, Level.DEBUG, null);
        config.setLevel(Level.DEBUG);
        ctx.updateLoggers();
        
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
