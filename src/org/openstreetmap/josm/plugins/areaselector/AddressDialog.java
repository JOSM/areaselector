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
	
	protected static String lastHouseNum="",lastStreetName="", lastCity="", lastPostCode="",lastCountry="",lastBuilding="yes",lastTags="";
	
	public static final String TAG_HOUSENAME="name",TAG_HOUSENUM="addr:housenumber",TAG_STREETNAME="addr:street",TAG_CITY="addr:city",TAG_POSTCODE="addr:postCode",TAG_COUNTRY="addr:country",TAG_BUILDING="building";
	
	
    protected String houseNum, streetName, city, postCode, country, houseName,building,tags;
    protected JTextField houseNumField, streetNameField, cityField, postCodeField, countryField, houseNameField, buildingField,tagsField;
    
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
        tagsField=new JTextField(lastTags);

        addLabelled(tr("Name:"), houseNameField);
        addLabelled(tr("House number:"), houseNumField);
        addLabelled(tr("Street:"), streetNameField);
        addLabelled(tr("City:"), cityField);
        addLabelled(tr("Post code:"), postCodeField);
        addLabelled(tr("Country:"), countryField);
        addLabelled(tr("Building:"), buildingField);
        addLabelled(tr("Tags:"), tagsField);
        
        setContent(panel);
        setupDialog();
        this.setSize(400, 350);
    }



    public final void saveValues() {
    	houseName = houseNameField.getText();
    	lastHouseNum = houseNum = houseNumField.getText();
        lastStreetName = streetName = streetNameField.getText();
        lastCity = city = cityField.getText();
        lastPostCode = postCode = postCodeField.getText();
        lastCountry = country = countryField.getText();
        lastBuilding = building = buildingField.getText();
        lastTags = tags = tagsField.getText();
        
       
        updateTag(TAG_HOUSENAME, houseName);
        updateTag(TAG_HOUSENUM, houseNum);
        updateTag(TAG_STREETNAME, streetName);
        updateTag(TAG_CITY, city);
        updateTag(TAG_POSTCODE, postCode);
        updateTag(TAG_COUNTRY, country);
        updateTag(TAG_BUILDING, building);
        
        if(!tags.isEmpty()){
        	String[] alltags=tags.split(" *[,;] *");
        	for(int i=0;i<alltags.length;i++){
        		String[] kv=alltags[i].split(" *= *");
        		if(kv.length>=2){
        			updateTag(kv[0],kv[1]);
        		}
        	}
        }
        
    }
    
    public void updateTag(String tag,String value){
    	if(value==null||value.isEmpty()){
    		if(way.get(tag)!=null){
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
