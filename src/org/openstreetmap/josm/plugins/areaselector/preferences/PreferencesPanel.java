// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPreset;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresetType;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresets;
import org.openstreetmap.josm.plugins.areaselector.AreaSelectorAction;
import org.openstreetmap.josm.plugins.areaselector.ImageAnalyzer;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Logging;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

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

	private JLabel lblPresetName;

	private JCheckBox ckbxShowAddressDialog;

	private JCheckBox ckbxMergeNodes;

	private JCheckBox ckbxHSV;

	protected JCheckBox ckbxDebug;

	protected JCheckBox ckbxAustriaAdressHelper;

	private JComboBox<String> algorithm;

	private JCheckBox ckbxReplaceBuilding;

	private JCheckBox ckbxAddSourceTag;

	private JCheckBox ckbxApplyPresetDirectly;

	private Map<String, JRadioButton> taggingStyleOptions;

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
		// Column of components, with some spacing between rows
		this.setLayout(new GridBagLayout());

		////////// Polygon generation
		JPanel polygonGenerationPanel = new JPanel(new GridLayout(0, 1, 0, 5));

		String[] algorithms = {tr("Auto"), tr("Boofcv - high resolution images"), tr("Custom - low resolution images")};
		algorithm = new JComboBox<>(algorithms);
		algorithm.setSelectedIndex(0);
		this.addInput(
			polygonGenerationPanel,
			tr("Algorithm"),
			algorithm,
			tr("Choose wich algorithm should be used. \"Auto\" tries to find an area with Boofcv and uses the custom algorithm as a fallback."),
			false
		);

		txtThinningIterations = new JTextField();
		this.addInput(
			polygonGenerationPanel,
			tr("Thinning Iterations"),
			txtThinningIterations,
			tr("How often thinning operation should be applied (Default {0}).", ImageAnalyzer.DEFAULT_THINNING_ITERATIONS),
			false
		);

		// CHECKSTYLE.OFF: LineLength

		txtToleranceDist = new JTextField();
		this.addInput(
			polygonGenerationPanel,
			tr("Distance Tolerance"),
			txtToleranceDist,
			tr("Maximum distance in meters between a point and the line to be considered as a member of this line (Default: {0}).", ImageAnalyzer.DEFAULT_TOLERANCEDIST),
			false
		);

		txtToleranceAngle = new JTextField();
		this.addInput(
			polygonGenerationPanel,
			tr("Angle Tolerance"),
			txtToleranceAngle,
			tr("Lines with a smaller angle (degrees) than this will be combined to one line (Default {0}).", Math.floor(Math.toDegrees(ImageAnalyzer.DEFAULT_TOLERANCEANGLE))),
			false
		);

		txtColorThreshold = new JTextField();
		this.addInput(
			polygonGenerationPanel,
			tr("Color Threshold"),
			txtColorThreshold,
			tr(
				"The color threshold defines how much a color may differ from the selected color. The red, green and blue values must be in the range of (selected - threshold) to (selected + threshold). (Default: {0}).",
				ImageAnalyzer.DEFAULT_COLORTHRESHOLD
			),
			false
		);
		// CHECKSTYLE.ON: LineLength

		ckbxHSV = new JCheckBox(tr("Use HSV based algorithm"));
		this.addCheckbox(
			polygonGenerationPanel,
			ckbxHSV,
			tr("Use hue and saturation instead of RGB distinction to select matching colors."),
			false
		);

		this.addSection("Polygon generation", polygonGenerationPanel);

		////////// Polygon processing
		JPanel polygonProcessing = new JPanel(new GridLayout(0, 1, 0, 5));

		ckbxMergeNodes = new JCheckBox(tr("merge nodes"));
		this.addCheckbox(
			polygonProcessing,
			ckbxMergeNodes,
			tr("Merge nodes with existing nodes"),
			false
		);

		ckbxReplaceBuilding = new JCheckBox(tr("Replace existing buildings"));
		this.addCheckbox(
			polygonProcessing,
			ckbxReplaceBuilding,
			tr("Replace an existing building with the new one."),
			false
		);

		this.addSection("Polygon processing", polygonProcessing);

		////////// Tagging
		JPanel tagging = new JPanel(new GridLayout(0, 1, 0, 5));
		ButtonGroup taggingStyle = new ButtonGroup();

		taggingStyleOptions = new HashMap<>();

		JRadioButton none = new JRadioButton(tr("None"));
		taggingStyle.add(none);
		this.addRadioButton(
			tagging,
			none,
			tr("Don't apply any tags, only create the polygon")
		);
		taggingStyleOptions.put("none", none);

		JRadioButton presetSearchDialog = new JRadioButton(tr("Preset search dialog"));
		taggingStyle.add(presetSearchDialog);
		this.addRadioButton(
			tagging,
			presetSearchDialog,
			tr("Don't apply any tags, only create the polygon")
		);
		taggingStyleOptions.put("presetSearchDialog", presetSearchDialog);

		JRadioButton specificPreset = new JRadioButton(tr("Specific preset"));
		taggingStyle.add(specificPreset);
		this.addRadioButton(
			tagging,
			specificPreset,
			tr("Don't apply any tags, only create the polygon")
		);
		taggingStyleOptions.put("specificPreset", specificPreset);

		// Preset selection controls and current selected preset
		lblPresetName = new JLabel();
		lblPresetName.setBorder(BorderFactory.createEmptyBorder(0, 45, 0, 0));
		tagging.add(lblPresetName, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
		JPanel row = new JPanel(new GridLayout(1, 1));
		JButton selectPresetButton = new JButton("Select preset");
		selectPresetButton.addActionListener(e -> TaggingPresetSelectionSearch.show(selectedPreset -> {
			if (selectedPreset == null) {
				return;
			}
			if (!selectedPreset.types.contains(TaggingPresetType.CLOSEDWAY)) {
				JOptionPane.showMessageDialog(
					MainApplication.getMap(),
					tr("Selected preset is not suitable for a closed way, select another one."),
					tr("Area Selector"),
					JOptionPane.WARNING_MESSAGE
				);
				return;
			}
			lblPresetName.setText(selectedPreset.getRawName());
			lblPresetName.setIcon(selectedPreset.getIcon(Action.LARGE_ICON_KEY));
		}));
		selectPresetButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		row.add(selectPresetButton);
		row.setBorder(BorderFactory.createEmptyBorder(0, 43, 0, 0));
		tagging.add(row, GBC.eol().fill(GridBagConstraints.HORIZONTAL));

		ckbxApplyPresetDirectly = new JCheckBox(tr("Apply preset tags directly"));
		this.addCheckbox(
			tagging,
			ckbxApplyPresetDirectly,
			tr("Don't show the preset dialog, but apply all non-empty tags of the preset directly to the created area"),
			true
		);

		JRadioButton address = new JRadioButton(tr("Address"));
		taggingStyle.add(address);
		this.addRadioButton(
			tagging,
			address,
			tr("Don't apply any tags, only create the polygon")
		);
		taggingStyleOptions.put("address", address);

		ckbxShowAddressDialog = new JCheckBox(tr("Show address dialog"));
		this.addCheckbox(
			tagging,
			ckbxShowAddressDialog,
			tr("Show Address Dialog after mapping an area"),
			true
		);

		ckbxAustriaAdressHelper = new JCheckBox(tr("Use austria address helper"));
		this.addCheckbox(
			tagging,
			ckbxAustriaAdressHelper,
			tr("Automatically try to find the correct address via Austria Address Helper plugin"),
			true
		);

		this.addSection("Tagging", tagging);

		ckbxAddSourceTag = new JCheckBox(tr("Add source tag"));
		this.addCheckbox(
			tagging,
			ckbxAddSourceTag,
			tr("Add source tag."),
			false
		);

		////////// Other
		JPanel other = new JPanel(new GridLayout(0, 1, 0, 5));
		ckbxDebug = new JCheckBox(tr("Debug"));
		this.addCheckbox(
			other,
			ckbxDebug,
			tr("Debugging mode will write images for each processing step."),
			false
		);
		this.addSection("Other", other);


		// Fill up remaining space
		this.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.BOTH));
	}

	/**
	 * Add section with a label
	 */
	protected void addSection(String name, JPanel section) {
		final JLabel lbl = new JLabel("<html><p><b>" + name + "</b></p></html>");
		lbl.setLabelFor(section);
		this.add(lbl, GBC.eol().fill(GridBagConstraints.HORIZONTAL).insets(0, 30, 0, 10));
		this.add(section, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
	}

	/**
	 * Adds a a title, with an input next to it and a description below them
	 */
	protected void addInput(JPanel target, String title, JComponent input, String description, boolean nested) {
		JLabel label = new JLabel("<html><p>" + title + "</p></html>");
		JPanel row = new JPanel(new GridLayout(1, 2));
		if (input instanceof JTextField) {
			((JTextField) input).setColumns(10);
		}
		row.add(label);
		row.add(input);
		row.setToolTipText(description);
		if (nested) {
			row.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
		}
		target.add(row, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
	}

	protected void addCheckbox(JPanel target, JCheckBox checkbox, String description, boolean nested) {
		JPanel row = new JPanel(new GridLayout(1, 1));
		row.add(checkbox);
		row.setToolTipText(description);
		if (nested) {
			row.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
		}
		target.add(row, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
	}

	protected void addRadioButton(JPanel target, JRadioButton radioButton, String description) {
		JPanel row = new JPanel(new GridLayout(1, 2));
		row.add(radioButton);
		row.setToolTipText(description);
		target.add(row, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
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
			Logging.debug(e);
		}
		new DoubleProperty(ImageAnalyzer.KEY_TOLERANCEDIST, ImageAnalyzer.DEFAULT_TOLERANCEDIST).parseAndPut(txtToleranceDist.getText());
		new BooleanProperty(AreaSelectorAction.KEY_MERGENODES, true).put(ckbxMergeNodes.isSelected());
		new BooleanProperty(AreaSelectorAction.KEY_SHOWADDRESSDIALOG, true).put(ckbxShowAddressDialog.isSelected());
		new BooleanProperty(ImageAnalyzer.KEY_HSV, false).put(ckbxHSV.isSelected());
		new BooleanProperty(ImageAnalyzer.KEY_DEBUG, false).put(ckbxDebug.isSelected());
		new BooleanProperty(AreaSelectorAction.KEY_AAH, false).put(ckbxAustriaAdressHelper.isSelected());
		new BooleanProperty(AreaSelectorAction.KEY_REPLACEBUILDINGS, true).put(ckbxReplaceBuilding.isSelected());
		new BooleanProperty(AreaSelectorAction.KEY_ADDSOURCETAG, false).put(ckbxAddSourceTag.isSelected());
		new BooleanProperty(AreaSelectorAction.KEY_APPLYPRESETDIRECTLY, false).put(ckbxApplyPresetDirectly.isSelected());

		String taggingStyleSetting = "none";
		for (Map.Entry<String, JRadioButton> entry : taggingStyleOptions.entrySet()) {
			if (entry.getValue().isSelected()) {
				taggingStyleSetting = entry.getKey();
			}
		}
		new StringProperty(AreaSelectorAction.KEY_TAGGINGSTYLE, "none").put(taggingStyleSetting);

		new StringProperty(AreaSelectorAction.KEY_TAGGINGPRESETNAME, "").put(lblPresetName.getText());
	}

	public void readPreferences() {
		txtColorThreshold.setText(Integer.toString(new IntegerProperty(ImageAnalyzer.KEY_COLORTHRESHOLD, ImageAnalyzer.DEFAULT_COLORTHRESHOLD).get()));
		txtThinningIterations.setText(Integer.toString(new IntegerProperty(ImageAnalyzer.KEY_THINNING_ITERATIONS, ImageAnalyzer.DEFAULT_THINNING_ITERATIONS).get()));
		txtToleranceAngle.setText(Double.toString(Math.floor(Math.toDegrees(new DoubleProperty(ImageAnalyzer.KEY_TOLERANCEANGLE, ImageAnalyzer.DEFAULT_TOLERANCEANGLE).get()))));
		txtToleranceDist.setText(Double.toString(new DoubleProperty(ImageAnalyzer.KEY_TOLERANCEDIST, ImageAnalyzer.DEFAULT_TOLERANCEDIST).get()));
		String taggingPresetName = new StringProperty(AreaSelectorAction.KEY_TAGGINGPRESETNAME, "").get();
		lblPresetName.setText(taggingPresetName);
		for (TaggingPreset t : TaggingPresets.getTaggingPresets()) {
			if (t.getRawName().equals(taggingPresetName)) {
				lblPresetName.setIcon(t.getIcon(Action.LARGE_ICON_KEY));
				break;
			}
		}

		ckbxMergeNodes.setSelected(new BooleanProperty(AreaSelectorAction.KEY_MERGENODES, true).get());
		ckbxShowAddressDialog.setSelected(new BooleanProperty(AreaSelectorAction.KEY_SHOWADDRESSDIALOG, true).get());
		ckbxHSV.setSelected(new BooleanProperty(ImageAnalyzer.KEY_HSV, false).get());
		ckbxAustriaAdressHelper.setSelected(new BooleanProperty(AreaSelectorAction.KEY_AAH, false).get());

		int algorithmIdx = new IntegerProperty(ImageAnalyzer.KEY_ALGORITHM, ImageAnalyzer.DEFAULT_ALGORITHM).get();
		algorithm.setSelectedIndex(algorithmIdx < algorithm.getMaximumRowCount() ? algorithmIdx : ImageAnalyzer.DEFAULT_ALGORITHM);
		ckbxDebug.setSelected(new BooleanProperty(ImageAnalyzer.KEY_DEBUG, false).get());
		ckbxReplaceBuilding.setSelected(new BooleanProperty(AreaSelectorAction.KEY_REPLACEBUILDINGS, true).get());
		ckbxAddSourceTag.setSelected(new BooleanProperty(AreaSelectorAction.KEY_ADDSOURCETAG, false).get());
		ckbxApplyPresetDirectly.setSelected(new BooleanProperty(AreaSelectorAction.KEY_APPLYPRESETDIRECTLY, false).get());

		String taggingStyleSetting = new StringProperty(AreaSelectorAction.KEY_TAGGINGSTYLE, "none").get();
		for (Map.Entry<String, JRadioButton> entry : taggingStyleOptions.entrySet()) {
			entry.getValue().setSelected(taggingStyleSetting.equals(entry.getKey()));
		}
	}
}
