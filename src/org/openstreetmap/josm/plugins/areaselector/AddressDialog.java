// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
	
	protected static String lastHouseNum="",lastStreetName="", lastCity="", lastPostCode="",lastCountry="";
	
	public static final String TAG_HOUSENAME="addr:housename",TAG_HOUSENUM="addr:housenumber",TAG_STREETNAME="addr:street",TAG_CITY="addr:city",TAG_POSTCODE="addr:postCode",TAG_COUNTRY="addr:country";
	
	
    protected String houseNum, streetName, city, postCode, country, houseName;
    protected JTextField houseNumField, streetNameField, cityField, postCodeField, countryField, houseNameField;
    
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
       
        houseNameField=new JTextField();
        houseNumField=new JTextField(lastHouseNum);
        streetNameField=new JTextField(lastStreetName);
        cityField=new JTextField(lastCity);
        postCodeField=new JTextField(lastPostCode);
        countryField=new JTextField(lastCountry);

        addLabelled(tr("House name:"), houseNameField);
        addLabelled(tr("House number:"), houseNumField);
        addLabelled(tr("Street:"), streetNameField);
        addLabelled(tr("City:"), cityField);
        addLabelled(tr("Post code:"), postCodeField);
        addLabelled(tr("Country:"), countryField);
        
        setContent(panel);
        setupDialog();
    }



    public final void saveValues() {
    	houseName = houseNameField.getText();
    	houseNum = houseNumField.getText();
        streetName = streetNameField.getText();
        city = cityField.getText();
        postCode = postCodeField.getText();
        country = countryField.getText();
        
        way.put(TAG_HOUSENAME, houseName);
        way.put(TAG_HOUSENUM, houseNum);
        way.put(TAG_STREETNAME, streetName);
        way.put(TAG_CITY, city);
        way.put(TAG_POSTCODE, postCode);
        way.put(TAG_COUNTRY, country);
        
        
    }

    public final String getHouseNum() {
        return houseNumField.getText();
    }

    public final String getStreetName() {
        return streetNameField.getText();
    }
}
