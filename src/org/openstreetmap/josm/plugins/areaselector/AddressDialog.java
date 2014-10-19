// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
public class AddressDialog extends ExtendedDialog implements ChangeListener {

	public static final String TAG_HOUSENAME="name",TAG_HOUSENUM="addr:housenumber",TAG_STREETNAME="addr:street",TAG_CITY="addr:city",TAG_POSTCODE="addr:postcode",TAG_COUNTRY="addr:country",TAG_BUILDING="building";

    public static final String PREF=AreaSelectorAction.PLUGIN_NAME+".last.";

    public static final String PREF_HOUSENUM=PREF+"housenum",
        PREF_STREETNAME=PREF+"streetname",
        PREF_CITY=PREF+"street",
        PREF_POSTCODE=PREF+"postcode",
        PREF_COUNTRY=PREF+"country",
        PREF_BUILDING=PREF+"building",
        PREF_TAGS=PREF+"tags",
        PREF_HOUSENUM_CHANGE=PREF+"housenum.change",
        PREF_DIALOG_X=PREF+"dialog.x",
        PREF_DIALOG_Y=PREF+"dialog.y";


    protected String houseNum, streetName, city, postCode, country, houseName,building,tags;

    protected JTextField houseNumField;
    protected ButtonGroup houseNumChange;

    protected AutoCompletingComboBox streetNameField, cityField, postCodeField, countryField, houseNameField, buildingField,tagsField;

    protected static final String[] BUTTON_TEXTS = new String[] {tr("OK"), tr("Cancel")};
    protected static final String[] BUTTON_ICONS = new String[] {"ok.png", "cancel.png"};

    protected final JPanel panel = new JPanel(new GridBagLayout());

    protected OsmPrimitive way;

    protected static Collection<AutoCompletionListItem> aciTags;

    protected int changeNum=0;

//    protected static Logger log = Logger.getLogger(AddressDialog.class);

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

        houseNumField=new JTextField();
        houseNumField.setPreferredSize(new Dimension(100, 24));

        String numChange=Main.pref.get(PREF_HOUSENUM_CHANGE, "0");
        try{
        	changeNum=Integer.parseInt(numChange);
        	if(changeNum!=0){
        		houseNumField.setText(Integer.toString(Integer.parseInt(Main.pref.get(PREF_HOUSENUM, ""))+changeNum));
        	}

        }catch(NumberFormatException e){}

        JPanel houseNumPanel=new JPanel();
        houseNumPanel.add(houseNumField);


        houseNumChange = new ButtonGroup();

        for(int i=-2;i<=2;i++){
        	JRadioButton radio=new JRadioButton((i==0?tr("empty"):((i>0?"+":"")+Integer.toString(i))));
        	radio.setActionCommand(Integer.toString(i));

        	if(changeNum==i){
        		radio.setSelected(true);
        	}
        	radio.addChangeListener(this);
        	houseNumChange.add(radio);
        	houseNumPanel.add(radio);
        }



        JButton skip=new JButton(tr("skip"));
        skip.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(changeNum!=0){
					try {
						houseNumField.setText(Integer.toString(Integer.parseInt(houseNumField.getText())+changeNum));
					}catch(NumberFormatException ex){}
				}
			}
		});
        houseNumPanel.add(skip);


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





        JLabel houseNumLabel = new JLabel(tr("House number:"));
        houseNumLabel.setLabelFor(houseNameField);
        panel.add(houseNumLabel, GBC.std());
        panel.add(houseNumPanel, GBC.eol().fill(GBC.HORIZONTAL));

        addLabelled(tr("Street:"), streetNameField);
        addLabelled(tr("City:"), cityField);
        addLabelled(tr("Post code:"), postCodeField);
        addLabelled(tr("Country:"), countryField);
        addLabelled(tr("Building:"), buildingField);
        addLabelled(tr("Tags:"), tagsField);

        addLabelled(tr("Name:"), houseNameField);


        setContent(panel);
        setupDialog();
        this.setSize(630, 350);

        try{
        	this.setLocation(Integer.parseInt(Main.pref.get(PREF_DIALOG_X)), Integer.parseInt(Main.pref.get(PREF_DIALOG_Y)));
        }catch(NumberFormatException e){}
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
        Main.pref.put(PREF_HOUSENUM_CHANGE, houseNumChange.getSelection().getActionCommand());


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

    public void setHouseNumChange(int num){
    	Main.pref.put(PREF_HOUSENUM_CHANGE, Integer.toString(num));
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

		Main.pref.put(PREF_DIALOG_X, Integer.toString(this.getLocation().x));
	    Main.pref.put(PREF_DIALOG_Y, Integer.toString(this.getLocation().y));

		return way;
    }



	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 * a radio button changed it's event
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		try {
			JRadioButton rb=(JRadioButton)e.getSource();
			changeNum=Integer.parseInt(rb.getActionCommand());

		}catch(NumberFormatException ex){}
	}
}
