/*
 * Created on Jul 31, 2014
 * Author: Paul Woelfel
 * Email: paul@woelfel.at
 */
package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.openstreetmap.josm.plugins.areaselector.AreaSelectorAction;
import org.openstreetmap.josm.plugins.areaselector.ImageAnalyzer;

/**
 * Area Selector Preferences JPanel
 * 
 * @author Paul Woelfel (paul@woelfel.at)
 */
public class PreferencesPanel extends JPanel {

	private JTextField txtColorThreshold;

	private JTextField txtToleranceDist;

	private JTextField txtToleranceAngle;

	private JTextField txtThinningIterations;

	private JCheckBox ckbxShowAddressDialog;

	private JCheckBox ckbxMergeNodes;

	private JCheckBox ckbxHSV;
	
	protected JCheckBox debug;

	protected JComponent ref;

	protected HashMap<String, String> prefs;

	/**
	 * Constructs a new {@code PreferencesPanel}.
	 */
	public PreferencesPanel() {
		super();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		ref = this;

		SpringLayout sl_panel = new SpringLayout();
		this.setLayout(sl_panel);
		
				txtToleranceDist = new JTextField();
		this.addInput(
				tr("Maximum pixels a point can be away from a line and still be considered as a member of this line (Default: {0}).",
						ImageAnalyzer.DEFAULT_TOLERANCEDIST), tr("Distance Tolerance"), txtToleranceDist);

		txtToleranceAngle = new JTextField();
		this.addInput(
				tr("Lines with a smaller angle (degrees) than this will be combined to one line (Default {0}).",
						Math.floor(Math.toDegrees(ImageAnalyzer.DEFAULT_TOLERANCEANGLE))), tr("Angle Tolerance"), txtToleranceAngle);

		txtColorThreshold = new JTextField();
		this.addInput(
				tr("The color threshold defines how much a color may differ from the selected color. The red, green and blue values must be in the range of (selected - threshold) to (selected + threshold). (Default: {0}).",
						ImageAnalyzer.DEFAULT_COLORTHRESHOLD), tr("Color Threshold"), txtColorThreshold);

		ckbxHSV = new JCheckBox("<html><p><b>" + tr("Use HSV based algorithm") + "</b></p></html>");
		this.addCheckbox(tr("Use hue and saturation instead of RGB distinction to select matching colors."), ckbxHSV);

		txtThinningIterations = new JTextField();
		this.addInput(tr("How often thinning operation should be applied (Default {0}).", ImageAnalyzer.DEFAULT_THINNING_ITERATIONS),
				tr("Thinning Iterations"), txtThinningIterations);

		ckbxShowAddressDialog = new JCheckBox("<html><p><b>" + tr("show address dialog") + "</b></p></html>");
		this.addCheckbox(tr("Show Address Dialog after mapping an area"), ckbxShowAddressDialog);

		ckbxMergeNodes = new JCheckBox("<html><p><b>" + tr("merge nodes") + "</b></p></html>");
		this.addCheckbox(tr("Merge nodes with existing nodes"), ckbxMergeNodes);
		
		debug = new JCheckBox("<html><p><b>" + tr("Debug") + "</b></p></html>");
		this.addCheckbox(tr("Debugging mode will write images for each processing step."), debug);

	}

	protected void addInput(String description, String title, JTextField input) {
		SpringLayout sl_panel = (SpringLayout) this.getLayout();
		
		JLabel lblTitle = new JLabel("<html><p><b>" + title + "</b></p></html>");
		this.northConstraint(lblTitle);
		sl_panel.putConstraint(SpringLayout.WEST, lblTitle, 20, SpringLayout.WEST, this);
		this.add(lblTitle);

		sl_panel.putConstraint(SpringLayout.NORTH, input, -6, SpringLayout.NORTH, lblTitle);
		sl_panel.putConstraint(SpringLayout.EAST, input, -140, SpringLayout.EAST, this);

		input.setColumns(10);
		this.add(input);

		ref = lblTitle;
		
		this.addDescription(description);
	}

	protected void addCheckbox(String description, JCheckBox checkbox) {
		SpringLayout sl_panel = (SpringLayout) this.getLayout();
		
		sl_panel.putConstraint(SpringLayout.WEST, checkbox, 0, SpringLayout.WEST, ref);
		this.northConstraint(checkbox);
		this.add(checkbox);

		ref = checkbox;
		
		this.addDescription(description);
	}
	
	protected JLabel addDescription(String description){
		
		JLabel lblDescription = new JLabel("<html><p>" + description + "</p></html>");
		SpringLayout sl_panel = (SpringLayout) this.getLayout();
		sl_panel.putConstraint(SpringLayout.NORTH, lblDescription, 10, SpringLayout.SOUTH, ref);
		sl_panel.putConstraint(SpringLayout.WEST, lblDescription, 20, SpringLayout.WEST, this);
		sl_panel.putConstraint(SpringLayout.EAST, lblDescription, -20, SpringLayout.EAST, this);
		this.add(lblDescription);
		
		ref = lblDescription;
		return lblDescription;
	}
	
	protected void northConstraint(JComponent component){
		SpringLayout sl_panel = (SpringLayout) this.getLayout();
		if (ref == this){
			sl_panel.putConstraint(SpringLayout.NORTH, component, 5, SpringLayout.NORTH, ref);
		}else {
			sl_panel.putConstraint(SpringLayout.NORTH, component, 20, SpringLayout.SOUTH, ref);
		}
	}

	/**
	 * @return the prefs
	 */
	public HashMap<String, String> getPrefs() {
		// make sure prefs are up to date
		prefs.put(ImageAnalyzer.KEY_COLORTHRESHOLD, txtColorThreshold.getText());
		prefs.put(ImageAnalyzer.KEY_THINNING_ITERATIONS, txtThinningIterations.getText());
		try {
			prefs.put(ImageAnalyzer.KEY_TOLERANCEANGLE, Double.toString(Math.toRadians(Double.parseDouble(txtToleranceAngle.getText()))));
		} catch (NumberFormatException e) {
		}
		prefs.put(ImageAnalyzer.KEY_TOLERANCEDIST, txtToleranceDist.getText());
		prefs.put(AreaSelectorAction.KEY_MERGENODES, ckbxMergeNodes.isSelected() ? "true" : "false");
		prefs.put(AreaSelectorAction.KEY_SHOWADDRESSDIALOG, ckbxShowAddressDialog.isSelected() ? "true" : "false");
		prefs.put(ImageAnalyzer.KEY_HSV, ckbxHSV.isSelected() ? "true" : "false");
		prefs.put(ImageAnalyzer.KEY_DEBUG, debug.isSelected() ? "true" : "false");
		return prefs;
	}

	/**
	 * @param prefs
	 *            the prefs to set
	 */
	public void setPrefs(HashMap<String, String> prefs) {
		this.prefs = prefs;
		 if(prefs.containsKey(ImageAnalyzer.KEY_COLORTHRESHOLD)){
		 txtColorThreshold.setText(prefs.get(ImageAnalyzer.KEY_COLORTHRESHOLD));
		 }
		 if(prefs.containsKey(ImageAnalyzer.KEY_THINNING_ITERATIONS)){
		 txtThinningIterations.setText(prefs.get(ImageAnalyzer.KEY_THINNING_ITERATIONS));
		 }
		 if(prefs.containsKey(ImageAnalyzer.KEY_TOLERANCEANGLE)){
		 try{
		 txtToleranceAngle.setText(Double.toString(Math.floor(Math.toDegrees(Double.parseDouble(prefs.get(ImageAnalyzer.KEY_TOLERANCEANGLE))))));
		 }catch(NumberFormatException e){}
		 }
		 if(prefs.containsKey(ImageAnalyzer.KEY_TOLERANCEDIST)){
		 txtToleranceDist.setText(prefs.get(ImageAnalyzer.KEY_TOLERANCEDIST));
		 }
		 if(prefs.containsKey(AreaSelectorAction.KEY_MERGENODES)){
		 ckbxMergeNodes.setSelected(prefs.get(AreaSelectorAction.KEY_MERGENODES).compareTo("true")==0);
		 }else {
		 ckbxMergeNodes.setSelected(true);
		 }
		 if(prefs.containsKey(AreaSelectorAction.KEY_SHOWADDRESSDIALOG)){
		 ckbxShowAddressDialog.setSelected(prefs.get(AreaSelectorAction.KEY_SHOWADDRESSDIALOG).compareTo("true")==0);
		 }else {
		 ckbxShowAddressDialog.setSelected(true);
		 }
		 if(prefs.containsKey(ImageAnalyzer.KEY_HSV)){
		 ckbxHSV.setSelected(prefs.get(ImageAnalyzer.KEY_HSV).compareTo("true")==0);
		 }else {
		 ckbxHSV.setSelected(false);
		 }
		if (prefs.containsKey(ImageAnalyzer.KEY_DEBUG)) {
			debug.setSelected(prefs.get(ImageAnalyzer.KEY_DEBUG).compareTo("true") == 0);
		} else {
			debug.setSelected(false);
		}
	}
}
