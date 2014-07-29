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
	
	protected static String lastHouseNum="",lastStreetName="", lastCity="", lastPostCode="",lastCountry="",lastBuilding="yes";
	
	public static final String TAG_HOUSENAME="addr:housename",TAG_HOUSENUM="addr:housenumber",TAG_STREETNAME="addr:street",TAG_CITY="addr:city",TAG_POSTCODE="addr:postCode",TAG_COUNTRY="addr:country",TAG_BUILDING="building";
	
	
    protected String houseNum, streetName, city, postCode, country, houseName,building;
    protected JTextField houseNumField, streetNameField, cityField, postCodeField, countryField, houseNameField, buildingField;
    
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
        buildingField=new JTextField(lastBuilding);

        addLabelled(tr("House name:"), houseNameField);
        addLabelled(tr("House number:"), houseNumField);
        addLabelled(tr("Street:"), streetNameField);
        addLabelled(tr("City:"), cityField);
        addLabelled(tr("Post code:"), postCodeField);
        addLabelled(tr("Country:"), countryField);
        addLabelled(tr("Building:"), buildingField);
        
        setContent(panel);
        setupDialog();
        this.setSize(400, 300);
    }



    public final void saveValues() {
    	houseName = houseNameField.getText();
    	lastHouseNum = houseNum = houseNumField.getText();
        lastStreetName = streetName = streetNameField.getText();
        lastCity = city = cityField.getText();
        lastPostCode = postCode = postCodeField.getText();
        lastCountry = country = countryField.getText();
        lastBuilding = building = buildingField.getText();
        
       
        updateTag(TAG_HOUSENAME, houseName);
        updateTag(TAG_HOUSENUM, houseNum);
        updateTag(TAG_STREETNAME, streetName);
        updateTag(TAG_CITY, city);
        updateTag(TAG_POSTCODE, postCode);
        updateTag(TAG_COUNTRY, country);
        updateTag(TAG_BUILDING, building);
        
    }
    
    public void updateTag(String tag,String value){
    	if(tag==null||tag.isEmpty()){
    		if(way.keySet().contains(tag)){
    			way.remove(tag);
    		}
    	}else {
    		way.put(tag, value);
    	}
    }

    public final String getHouseNum() {
        return houseNumField.getText();
    }

    public final String getStreetName() {
        return streetNameField.getText();
    }
}
