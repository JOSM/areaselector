// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

/**
 * based on AdressDialog from building_tools.<br>
 * http://wiki.openstreetmap.org/wiki/JOSM/Plugins/BuildingsTools
 * @author Paul Woelfel (paul@woelfel.at)
 */
@SuppressWarnings("serial")
public class AddressDialog extends ExtendedDialog {
    protected static String lhousenum, lstreetname;
    protected static int inc = 0;
    protected JTextField housenum = new JTextField();
    protected JTextField streetname = new JTextField();
    protected JSpinner incSpinner;
    
    protected static final String[] BUTTON_TEXTS = new String[] {tr("OK"), tr("Cancel")};
    protected static final String[] BUTTON_ICONS = new String[] {"ok.png", "cancel.png"};

    protected final JPanel panel = new JPanel(new GridBagLayout());
    
    protected Way way;

    protected final void addLabelled(String str, Component c) {
        JLabel label = new JLabel(str);
        panel.add(label, GBC.std());
        label.setLabelFor(c);
        panel.add(c, GBC.eol().fill(GBC.HORIZONTAL));
    }



    public AddressDialog(Way way) {
    	super(Main.parent, tr("Building address"), BUTTON_TEXTS, true);
    	
    	this.way=way;
    	
    	contentInsets = new Insets(15, 15, 5, 15);
        setButtonIcons(BUTTON_ICONS);

        setContent(panel);
        setDefaultButton(1);
       

        addLabelled(tr("House number:"), housenum);
        addLabelled(tr("Street Name:"), streetname);
        housenum.setText(nextHouseNum());
        streetname.setText(lstreetname);

        SpinnerNumberModel inc_model = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
        incSpinner = new JSpinner(inc_model);
        incSpinner.setValue(inc);
        addLabelled(tr("House number increment:"), incSpinner);

        setContent(panel);
        setupDialog();
    }

    protected static String nextHouseNum() {
        if (lhousenum == null)
            return "";
        try {
            Integer num = NumberFormat.getInstance().parse(lhousenum).intValue() + inc;
            return num.toString();
        } catch (ParseException e) {
            return lhousenum;
        }
    }

    public final void saveValues() {
        lhousenum = housenum.getText();
        lstreetname = streetname.getText();
        inc = (Integer) incSpinner.getValue();
        
        way.put("addr:housenumber", lhousenum);
        way.put("street", lstreetname);
        
    }

    public final String getHouseNum() {
        return housenum.getText();
    }

    public final String getStreetName() {
        return streetname.getText();
    }
}
