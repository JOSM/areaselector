/**
 *  This file has been taken and slightly modified from the Tracer2 JOSM plugin.
 */

package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.areaselector.AreaSelectorAction;
import org.openstreetmap.josm.plugins.areaselector.AreaSelectorPlugin;

public class AreaSelectorPreference extends DefaultTabPreferenceSetting {

    AreaSelectorPlugin areaSelectorPlugin;
    PreferencesPanel prefPanel;
    

    public AreaSelectorPreference(AreaSelectorPlugin plugin) {
        super("areaselector", 
        		"<html><p><b>"+tr("Area Selector - Preferences") + "</b></p></html>", 
        		tr("Settings for the area detection algorithm."));

        areaSelectorPlugin = plugin;
    }

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        prefPanel = new PreferencesPanel();

        AreaSelectorAction areaSelectorAction=areaSelectorPlugin.getAreaSelectorAction();
        prefPanel.setPrefs(areaSelectorAction.getPrefs());

        createPreferenceTabWithScrollPane(gui, prefPanel);
    }


    @Override
    public boolean ok() {
        AreaSelectorAction areaSelectorAction=areaSelectorPlugin.getAreaSelectorAction();
        areaSelectorAction.setPrefs(prefPanel.getPrefs());
        return false;
    }
}
