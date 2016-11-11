// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
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

	protected JCheckBox ckbxAustriaAdressHelper;

	private JComboBox<String> algorithm;

	protected JComponent ref;

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

		String[] algorithms = {tr("Auto"), tr("Boofcv - high resolution images"), tr("Custom - low resolution images")};
		algorithm = new JComboBox<>(algorithms);
		algorithm.setSelectedIndex(0);

		// CHECKSTYLE.OFF: LineLength
		this.addInput(
				tr("Choose wich algorithm should be used. \"Auto\" tries to find an area with Boofcv and uses the custom algorithm as a fallback."),
				tr("Algorithm"), algorithm);

		txtToleranceDist = new JTextField();
		this.addInput(
				tr("Maximum distance in meters between a point and the line to be considered as a member of this line (Default: {0}).",
						ImageAnalyzer.DEFAULT_TOLERANCEDIST), tr("Distance Tolerance"), txtToleranceDist);

		txtToleranceAngle = new JTextField();
		this.addInput(
				tr("Lines with a smaller angle (degrees) than this will be combined to one line (Default {0}).",
						Math.floor(Math.toDegrees(ImageAnalyzer.DEFAULT_TOLERANCEANGLE))), tr("Angle Tolerance"), txtToleranceAngle);

		txtColorThreshold = new JTextField();
		this.addInput(
				tr("The color threshold defines how much a color may differ from the selected color. The red, green and blue values must be in the range of (selected - threshold) to (selected + threshold). (Default: {0}).",
						ImageAnalyzer.DEFAULT_COLORTHRESHOLD), tr("Color Threshold"), txtColorThreshold);
		// CHECKSTYLE.ON: LineLength

		ckbxHSV = new JCheckBox("<html><p><b>" + tr("Use HSV based algorithm") + "</b></p></html>");
		this.addCheckbox(tr("Use hue and saturation instead of RGB distinction to select matching colors."), ckbxHSV);

		txtThinningIterations = new JTextField();
		this.addInput(tr("How often thinning operation should be applied (Default {0}).", ImageAnalyzer.DEFAULT_THINNING_ITERATIONS),
				tr("Thinning Iterations"), txtThinningIterations);

		ckbxShowAddressDialog = new JCheckBox("<html><p><b>" + tr("show address dialog") + "</b></p></html>");
		this.addCheckbox(tr("Show Address Dialog after mapping an area"), ckbxShowAddressDialog);

		ckbxMergeNodes = new JCheckBox("<html><p><b>" + tr("merge nodes") + "</b></p></html>");
		this.addCheckbox(tr("Merge nodes with existing nodes"), ckbxMergeNodes);

		ckbxAustriaAdressHelper = new JCheckBox("<html><p><b>" + tr("use austria address helper") + "</b></p></html>");
		this.addCheckbox(tr("Automatically try to find the correct address via Austria Address Helper plugin"), ckbxAustriaAdressHelper);

		debug = new JCheckBox("<html><p><b>" + tr("Debug") + "</b></p></html>");
		this.addCheckbox(tr("Debugging mode will write images for each processing step."), debug);

	}

	protected void addInput(String description, String title, JComponent input) {
		SpringLayout sl_panel = (SpringLayout) this.getLayout();

		JLabel lblTitle = new JLabel("<html><p><b>" + title + "</b></p></html>");
		this.northConstraint(lblTitle);
		sl_panel.putConstraint(SpringLayout.WEST, lblTitle, 20, SpringLayout.WEST, this);
		this.add(lblTitle);

		sl_panel.putConstraint(SpringLayout.NORTH, input, -6, SpringLayout.NORTH, lblTitle);
		sl_panel.putConstraint(SpringLayout.EAST, input, -140, SpringLayout.EAST, this);

		if (input instanceof JTextField) {
			((JTextField) input).setColumns(10);
		}
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

	protected JLabel addDescription(String description) {

		JLabel lblDescription = new JLabel("<html><p>" + description + "</p></html>");
		SpringLayout sl_panel = (SpringLayout) this.getLayout();
		sl_panel.putConstraint(SpringLayout.NORTH, lblDescription, 4, SpringLayout.SOUTH, ref);
		sl_panel.putConstraint(SpringLayout.WEST, lblDescription, 20, SpringLayout.WEST, this);
		sl_panel.putConstraint(SpringLayout.EAST, lblDescription, -20, SpringLayout.EAST, this);
		this.add(lblDescription);

		ref = lblDescription;
		return lblDescription;
	}

	protected void northConstraint(JComponent component) {
		SpringLayout sl_panel = (SpringLayout) this.getLayout();
		if (ref == this) {
			sl_panel.putConstraint(SpringLayout.NORTH, component, 4, SpringLayout.NORTH, ref);
		} else {
			sl_panel.putConstraint(SpringLayout.NORTH, component, 12, SpringLayout.SOUTH, ref);
		}
	}

	/**
	 * save the preferences
	 */
	public void savePreferences() {
		new IntegerProperty(ImageAnalyzer.KEY_COLORTHRESHOLD, ImageAnalyzer.DEFAULT_COLORTHRESHOLD).parseAndPut(txtColorThreshold.getText());
		new IntegerProperty(ImageAnalyzer.KEY_THINNING_ITERATIONS, ImageAnalyzer.DEFAULT_THINNING_ITERATIONS).parseAndPut(txtThinningIterations.getText());
		new IntegerProperty(ImageAnalyzer.KEY_ALGORITHM, ImageAnalyzer.DEFAULT_ALGORITHM).put(algorithm.getSelectedIndex());

		try {
			new DoubleProperty(ImageAnalyzer.KEY_TOLERANCEANGLE, ImageAnalyzer.DEFAULT_TOLERANCEANGLE).put(Math.toRadians(Double.parseDouble(txtToleranceAngle.getText())));
		} catch (NumberFormatException e) {
			Main.debug(e);
		}
		new DoubleProperty(ImageAnalyzer.KEY_TOLERANCEDIST, ImageAnalyzer.DEFAULT_TOLERANCEDIST).parseAndPut(txtToleranceDist.getText());
		new BooleanProperty(AreaSelectorAction.KEY_MERGENODES, true).put(ckbxMergeNodes.isSelected());
		new BooleanProperty(AreaSelectorAction.KEY_SHOWADDRESSDIALOG, true).put(ckbxShowAddressDialog.isSelected());
		new BooleanProperty(ImageAnalyzer.KEY_HSV, false).put(ckbxHSV.isSelected());
		new BooleanProperty(ImageAnalyzer.KEY_DEBUG, false).put(ckbxHSV.isSelected());
		new BooleanProperty(AreaSelectorAction.KEY_AAH, false).put(ckbxAustriaAdressHelper.isSelected());
	}

	/**
	 * @param prefs
	 *            the prefs to set
	 */
	public void readPreferences() {
		txtColorThreshold.setText(Integer.toString(new IntegerProperty(ImageAnalyzer.KEY_COLORTHRESHOLD, ImageAnalyzer.DEFAULT_COLORTHRESHOLD).get()));
		txtThinningIterations.setText(Integer.toString(new IntegerProperty(ImageAnalyzer.KEY_THINNING_ITERATIONS, ImageAnalyzer.DEFAULT_THINNING_ITERATIONS).get()));
		txtToleranceAngle.setText(Double.toString(Math.floor(Math.toDegrees(new DoubleProperty(ImageAnalyzer.KEY_TOLERANCEANGLE, ImageAnalyzer.DEFAULT_TOLERANCEANGLE).get()))));
		txtToleranceDist.setText(Double.toString(new DoubleProperty(ImageAnalyzer.KEY_TOLERANCEDIST, ImageAnalyzer.DEFAULT_TOLERANCEDIST).get()));

		ckbxMergeNodes.setSelected(new BooleanProperty(AreaSelectorAction.KEY_MERGENODES, true).get());
		ckbxShowAddressDialog.setSelected(new BooleanProperty(AreaSelectorAction.KEY_SHOWADDRESSDIALOG, true).get());
		ckbxHSV.setSelected(new BooleanProperty(ImageAnalyzer.KEY_HSV, false).get());
		ckbxAustriaAdressHelper.setSelected(new BooleanProperty(AreaSelectorAction.KEY_AAH, false).get());

		int algorithmIdx = new IntegerProperty(ImageAnalyzer.KEY_ALGORITHM, ImageAnalyzer.DEFAULT_ALGORITHM).get();
		algorithm.setSelectedIndex(algorithmIdx < algorithm.getMaximumRowCount() ? algorithmIdx : ImageAnalyzer.DEFAULT_ALGORITHM);

		debug.setSelected(new BooleanProperty(ImageAnalyzer.KEY_DEBUG, false).get());

	}
}
