// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;
import org.openstreetmap.josm.tools.GBC;

/**
 * based on AdressDialog from building_tools.<br>
 * http://wiki.openstreetmap.org/wiki/JOSM/Plugins/BuildingsTools
 * @author Paul Woelfel (paul@woelfel.at)
 */
@SuppressWarnings("serial")
public class AddressDialog extends ExtendedDialog {
	
	public static final String TAG_HOUSENAME="name",TAG_HOUSENUM="addr:housenumber",TAG_STREETNAME="addr:street",TAG_CITY="addr:city",TAG_POSTCODE="addr:postcode",TAG_COUNTRY="addr:country",TAG_BUILDING="building";

    public static final String PREF=AreaSelectorAction.PLUGIN_NAME+".last.";

    public static final String PREF_HOUSENUM=PREF+"housenum",
        PREF_STREETNAME=PREF+"streetname",
        PREF_CITY=PREF+"street",
        PREF_POSTCODE=PREF+"postcode",
        PREF_COUNTRY=PREF+"country",
        PREF_BUILDING=PREF+"building",
        PREF_TAGS=PREF+"tags";
	
	
    protected String houseNum, streetName, city, postCode, country, houseName,building,tags;
    
    protected JTextField houseNumField;
    protected AutoCompletingComboBox streetNameField, cityField, postCodeField, countryField, houseNameField, buildingField,tagsField;
    
    protected static final String[] BUTTON_TEXTS = new String[] {tr("OK"), tr("Cancel")};
    protected static final String[] BUTTON_ICONS = new String[] {"ok.png", "cancel.png"};

    protected final JPanel panel = new JPanel(new GridBagLayout());
    
    protected OsmPrimitive way;
    
    protected static Collection<AutoCompletionListItem> aciTags;

    protected final void addLabelled(String str, Component c) {
        JLabel label = new JLabel(str);
        panel.add(label, GBC.std());
        label.setLabelFor(c);
        panel.add(c, GBC.eol().fill(GBC.HORIZONTAL));
    }



    public AddressDialog(OsmPrimitive way) {
    	super(Main.parent, tr("Building address"), BUTTON_TEXTS, true);
    	
    	this.way=way;
    	
    	contentInsets = new Insets(15, 15, 5, 15);
        setButtonIcons(BUTTON_ICONS);

        setContent(panel);
        setDefaultButton(1);
        
        AutoCompletionManager acm = Main.main.getCurrentDataSet().getAutoCompletionManager();

       
        houseNameField = new AutoCompletingComboBox();
        houseNameField.setPossibleACItems(acm.getValues(TAG_HOUSENAME));
        houseNameField.setEditable(true);
        
        
        houseNumField=new JTextField(Main.pref.get(PREF_HOUSENUM));
        
        
        streetNameField = new AutoCompletingComboBox();
        streetNameField.setPossibleACItems(acm.getValues(TAG_STREETNAME));
        streetNameField.setEditable(true);
        streetNameField.setSelectedItem(Main.pref.get(PREF_STREETNAME));

        
        cityField = new AutoCompletingComboBox();
        cityField.setPossibleACItems(acm.getValues(TAG_CITY));
        cityField.setEditable(true);
        cityField.setSelectedItem(Main.pref.get(PREF_CITY));
        
        
        
        postCodeField = new AutoCompletingComboBox();
        postCodeField.setPossibleACItems(acm.getValues(TAG_POSTCODE));
        postCodeField.setEditable(true);
        postCodeField.setSelectedItem(Main.pref.get(PREF_POSTCODE));
        
        countryField = new AutoCompletingComboBox();
        countryField.setPossibleACItems(acm.getValues(TAG_COUNTRY));
        countryField.setEditable(true);
        countryField.setSelectedItem(Main.pref.get(PREF_COUNTRY));
        
        
        buildingField = new AutoCompletingComboBox();
        buildingField.setPossibleACItems(acm.getValues(TAG_BUILDING));
        buildingField.setEditable(true);
        buildingField.setSelectedItem(Main.pref.get(PREF_BUILDING));
        
        
        if(aciTags==null){
    		aciTags=new ArrayList<AutoCompletionListItem>();
    	}
        
        tagsField = new AutoCompletingComboBox();
        tagsField.setPossibleACItems(aciTags);
        tagsField.setEditable(true);
        tagsField.setSelectedItem(Main.pref.get(PREF_TAGS));
        
        


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

    protected String getAutoCompletingComboBoxValue(AutoCompletingComboBox box)
    {
       Object item = box.getSelectedItem();
       if (item != null) {
          if (item instanceof String) {
             return (String) item;
          }
          if (item instanceof AutoCompletionListItem) {
             return ((AutoCompletionListItem) item).getValue();
          }
          return item.toString();
       } else {
          return "";
       }
    }


    public final void saveValues() {
    	houseName = getAutoCompletingComboBoxValue(houseNameField);
    	houseNum = houseNumField.getText();
        streetName = getAutoCompletingComboBoxValue(streetNameField);
        city = getAutoCompletingComboBoxValue(cityField);
        postCode = getAutoCompletingComboBoxValue(postCodeField);
        country = getAutoCompletingComboBoxValue(countryField);
        building = getAutoCompletingComboBoxValue(buildingField);
        tags = getAutoCompletingComboBoxValue(tagsField);

        Main.pref.put(PREF_HOUSENUM, houseNum);
        Main.pref.put(PREF_STREETNAME, streetName);
        Main.pref.put(PREF_CITY, city);
        Main.pref.put(PREF_POSTCODE, postCode);
        Main.pref.put(PREF_COUNTRY, country);
        Main.pref.put(PREF_BUILDING, building);
        Main.pref.put(PREF_TAGS, tags);

        
       
        updateTag(TAG_HOUSENAME, houseName);
        updateTag(TAG_HOUSENUM, houseNum);
        updateTag(TAG_STREETNAME, streetName);
        updateTag(TAG_CITY, city);
        updateTag(TAG_POSTCODE, postCode);
        updateTag(TAG_COUNTRY, country);
        updateTag(TAG_BUILDING, building);
        
        if(!tags.isEmpty()){
        	AutoCompletionListItem aci=new AutoCompletionListItem(tags);
        	if(!aciTags.contains(aci)){
        		aciTags.add(aci);
        	}
        	
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

    public OsmPrimitive showAndSave(){
    	this.showDialog();
		if (this.getValue() == 1){
			this.saveValues();
			Collection<Command> cmds = new LinkedList<Command>();
			cmds.add(new ChangeCommand(way, way));
			Command c = new SequenceCommand(tr("updated building info"), cmds);
			Main.main.undoRedo.add(c);
			Main.main.getCurrentDataSet().setSelected(way);
		}
		return way;
    }
}
