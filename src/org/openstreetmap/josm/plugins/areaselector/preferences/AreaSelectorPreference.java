/**
 *  This file has been taken and slightly modified from the Tracer2 JOSM plugin.
 */

package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.areaselector.AreaSelectorPlugin;
import org.openstreetmap.josm.tools.GBC;

public class AreaSelectorPreference extends DefaultTabPreferenceSetting {
	
	AreaSelectorPlugin areaSelectorPlugin;
	
	JTextArea colorThresholdTextArea, toleranceDistTextArea, toleranceAngleTextArea;
	
    public AreaSelectorPreference(AreaSelectorPlugin plugin) {
    	super("areaselector", tr("Area Selector") + " - " + tr("Preferences"), tr("Select tile map service or imagery preferences."));

        areaSelectorPlugin = plugin;
    }
    
    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel prefPanel = new JPanel(new GridBagLayout());
        
        colorThresholdTextArea= new JTextArea();
        addLabelled(prefPanel,"Color Threshold",colorThresholdTextArea);
        
        toleranceDistTextArea= new JTextArea();
        addLabelled(prefPanel,"Tolerance Dist",toleranceDistTextArea);
        
        toleranceAngleTextArea= new JTextArea();
        addLabelled(prefPanel,"Tolerance Angle",toleranceAngleTextArea);
        
        createPreferenceTabWithScrollPane(gui, prefPanel);
    }
    
    protected void addLabelled(JPanel panel, String str, Component c) {
        JLabel label = new JLabel(str);
        panel.add(label, GBC.std().insets(5,10,0,0));
        label.setLabelFor(c);
        panel.add(c, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    }
    
    
    
    @Override
    public boolean ok() {
        // TODO save params
        return false;
    }

}
