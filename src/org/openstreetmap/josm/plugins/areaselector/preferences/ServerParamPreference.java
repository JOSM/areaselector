/**
 *  This file has been taken and slightly modified from the Tracer2 JOSM plugin.
 */

package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.areaselector.AreaSelectorPlugin;
import org.openstreetmap.josm.tools.GBC;

public class ServerParamPreference extends DefaultTabPreferenceSetting {
	
	AreaSelectorPlugin m_oPlugin;
	
    public ServerParamPreference(AreaSelectorPlugin plugin) {
    	super("areaselector", tr("Area Selector") + " - " + tr("Preferences"), tr("Select tile map service or imagery preferences."));

        m_oPlugin = plugin;
    }
    
    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel p = gui.createPreferenceTab(this);
        ServerParamPanel spp = new ServerParamPanel(m_oPlugin.m_oParamList);
        spp.refresh();
        JScrollPane sp = new JScrollPane(spp);
        p.add(sp, GBC.eol().fill(GridBagConstraints.BOTH));
    }
    
    @Override
    public boolean ok() {
        m_oPlugin.m_oParamList.save();
        return false;
    }

}
