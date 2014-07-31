/**
 *  This file has been taken and slightly modified from the Tracer2 JOSM plugin.
 */

package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.areaselector.AreaSelectorAction;
import org.openstreetmap.josm.plugins.areaselector.AreaSelectorPlugin;
import org.openstreetmap.josm.tools.GBC;

public class AreaSelectorPreference extends DefaultTabPreferenceSetting {
	
	AreaSelectorPlugin areaSelectorPlugin;
	PreferencesPanel prefPanel;
	
	public AreaSelectorPreference(AreaSelectorPlugin plugin) {
    	super("areaselector", tr("Area Selector") + " - " + tr("Preferences"), tr("Select tile map service or imagery preferences."));

        areaSelectorPlugin = plugin;
    }
    
    @Override
    public void addGui(PreferenceTabbedPane gui) {
    	prefPanel = new PreferencesPanel();
        
    	AreaSelectorAction areaSelectorAction=areaSelectorPlugin.getAreaSelectorAction();
    	prefPanel.setColorThreshold(areaSelectorAction.getColorThreshold());
    	prefPanel.setToleranceDist(areaSelectorAction.getToleranceDist());
    	prefPanel.setToleranceAngle(areaSelectorAction.getToleranceAngle());
    	
        
        createPreferenceTabWithScrollPane(gui, prefPanel);
    }
    
    protected void addLabelled(JPanel panel, String str, Component c) {
        JLabel label = new JLabel(str);
        
        GBC gbc=GBC.std();
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        panel.add(label, gbc);
        label.setLabelFor(c);
        panel.add(c, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    }
    
    
    
    @Override
    public boolean ok() {
    	AreaSelectorAction areaSelectorAction=areaSelectorPlugin.getAreaSelectorAction();
    	areaSelectorAction.setColorThreshold(prefPanel.getColorThreshold());
    	areaSelectorAction.setToleranceDist(prefPanel.getToleranceDist());
    	areaSelectorAction.setToleranceAngle(prefPanel.getToleranceAngle());
        return false;
    }

}
