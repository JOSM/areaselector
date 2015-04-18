/*
 * Created on Jul 31, 2014
 * Author: Paul Woelfel
 * Email: paul@woelfel.at
 */
package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.openstreetmap.josm.plugins.areaselector.AreaSelectorAction;
import org.openstreetmap.josm.plugins.areaselector.ImageAnalyzer;

/**
 * Area Selector Preferences JPanel
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

        SpringLayout sl_panel = new SpringLayout();
        this.setLayout(sl_panel);

//        JComboBox<String> boxSourceSelection = new JComboBox<String>();
//        sl_panel.putConstraint(SpringLayout.NORTH, boxSourceSelection, 10, SpringLayout.NORTH, this);
//        sl_panel.putConstraint(SpringLayout.WEST, boxSourceSelection, 267, SpringLayout.WEST, this);
//        this.add(boxSourceSelection);
//
//        JLabel lblSourceSelection = new JLabel("Image Source");
//        sl_panel.putConstraint(SpringLayout.NORTH, lblSourceSelection, 4, SpringLayout.NORTH, boxSourceSelection);
//        sl_panel.putConstraint(SpringLayout.WEST, lblSourceSelection, 40, SpringLayout.WEST, this);
//        lblSourceSelection.setLabelFor(boxSourceSelection);
//        this.add(lblSourceSelection);

        JLabel lblAlgorithmSettings = new JLabel("<html><p><b>"+tr("Algorithm Settings")+"</b></p></html>");
        sl_panel.putConstraint(SpringLayout.NORTH, lblAlgorithmSettings, 20, SpringLayout.NORTH, this);
        sl_panel.putConstraint(SpringLayout.WEST, lblAlgorithmSettings, 40, SpringLayout.WEST, this);
        this.add(lblAlgorithmSettings);

        JLabel lblColorThresholdExplanation = new JLabel("<html><p>"+tr("The color threshold defines how far the color can be of the original color to be selected (Default: {0}).",ImageAnalyzer.DEFAULT_COLORTHRESHOLD)+"</p></html>");
        sl_panel.putConstraint(SpringLayout.NORTH, lblColorThresholdExplanation, 17, SpringLayout.SOUTH, lblAlgorithmSettings);
        sl_panel.putConstraint(SpringLayout.WEST, lblColorThresholdExplanation, 0, SpringLayout.WEST, lblAlgorithmSettings);
        sl_panel.putConstraint(SpringLayout.EAST, lblColorThresholdExplanation, -40, SpringLayout.EAST, this);
        this.add(lblColorThresholdExplanation);

        JLabel lblColorThreshold = new JLabel(tr("Color Threshold"));
        sl_panel.putConstraint(SpringLayout.NORTH, lblColorThreshold, 10, SpringLayout.SOUTH, lblColorThresholdExplanation);
        sl_panel.putConstraint(SpringLayout.WEST, lblColorThreshold, 0, SpringLayout.WEST, lblAlgorithmSettings);
        this.add(lblColorThreshold);

        txtColorThreshold = new JTextField();
        sl_panel.putConstraint(SpringLayout.NORTH, txtColorThreshold, -6, SpringLayout.NORTH, lblColorThreshold);
        sl_panel.putConstraint(SpringLayout.WEST, txtColorThreshold, 64, SpringLayout.EAST, lblColorThreshold);
        this.add(txtColorThreshold);
        txtColorThreshold.setColumns(10);

        JLabel lblToleranceDistExplanation = new JLabel("<html><p>"+tr("Maximum distance away each point in the sequence can be from a line, in pixels (Default: {0}).",ImageAnalyzer.DEFAULT_TOLERANCEDIST)+"</p></html>");
        sl_panel.putConstraint(SpringLayout.NORTH, lblToleranceDistExplanation, 25, SpringLayout.SOUTH, lblColorThreshold);
        sl_panel.putConstraint(SpringLayout.WEST, lblToleranceDistExplanation, 0, SpringLayout.WEST, lblAlgorithmSettings);
        sl_panel.putConstraint(SpringLayout.EAST, lblToleranceDistExplanation, -40, SpringLayout.EAST, this);
        this.add(lblToleranceDistExplanation);

        JLabel lblToleranceDistance = new JLabel(tr("Tolerance Distance"));
        sl_panel.putConstraint(SpringLayout.NORTH, lblToleranceDistance, 15, SpringLayout.SOUTH, lblToleranceDistExplanation);
        sl_panel.putConstraint(SpringLayout.WEST, lblToleranceDistance, 0, SpringLayout.WEST, lblAlgorithmSettings);
        this.add(lblToleranceDistance);

        txtToleranceDist = new JTextField();
        sl_panel.putConstraint(SpringLayout.NORTH, txtToleranceDist, -6, SpringLayout.NORTH, lblToleranceDistance);
        sl_panel.putConstraint(SpringLayout.EAST, txtToleranceDist, 0, SpringLayout.EAST, txtColorThreshold);
        this.add(txtToleranceDist);
        txtToleranceDist.setColumns(10);

        JLabel lblToleranceAngleExplanation = new JLabel("<html><p>"+tr("Tolerance for fitting angles, in radians (Default {0}).",ImageAnalyzer.DEFAULT_TOLERANCEANGLE)+"</p></html>");
        sl_panel.putConstraint(SpringLayout.NORTH, lblToleranceAngleExplanation, 40, SpringLayout.NORTH, lblToleranceDistance);
        sl_panel.putConstraint(SpringLayout.WEST, lblToleranceAngleExplanation, 0, SpringLayout.WEST, lblAlgorithmSettings);
        sl_panel.putConstraint(SpringLayout.EAST, lblToleranceAngleExplanation, -40, SpringLayout.EAST, this);
        this.add(lblToleranceAngleExplanation);

        JLabel lblToleranceAngle = new JLabel(tr("Tolerance Angle"));
        sl_panel.putConstraint(SpringLayout.NORTH, lblToleranceAngle, 10, SpringLayout.SOUTH, lblToleranceAngleExplanation);
        sl_panel.putConstraint(SpringLayout.WEST, lblToleranceAngle, 0, SpringLayout.WEST, lblAlgorithmSettings);
        this.add(lblToleranceAngle);

        txtToleranceAngle = new JTextField();
        sl_panel.putConstraint(SpringLayout.NORTH, txtToleranceAngle, -6, SpringLayout.NORTH, lblToleranceAngle);
        sl_panel.putConstraint(SpringLayout.EAST, txtToleranceAngle, 0, SpringLayout.EAST, txtColorThreshold);
        this.add(txtToleranceAngle);
        txtToleranceAngle.setColumns(10);

        JLabel lbluserInterfaceSettings = new JLabel("<html><p><b>"+tr("Plugin Settings")+"</b></p></html>");
        sl_panel.putConstraint(SpringLayout.WEST, lbluserInterfaceSettings, 0, SpringLayout.WEST, lblAlgorithmSettings);
        add(lbluserInterfaceSettings);

        JLabel lblShowAddressDialog = new JLabel(tr("Show Address Dialog after mapping an area"));
        sl_panel.putConstraint(SpringLayout.NORTH, lblShowAddressDialog, 23, SpringLayout.SOUTH, lbluserInterfaceSettings);
        sl_panel.putConstraint(SpringLayout.WEST, lblShowAddressDialog, 0, SpringLayout.WEST, lblAlgorithmSettings);
        add(lblShowAddressDialog);

        ckbxShowAddressDialog = new JCheckBox(tr("show address dialog"));
        sl_panel.putConstraint(SpringLayout.WEST, ckbxShowAddressDialog, 84, SpringLayout.EAST, lblShowAddressDialog);
        sl_panel.putConstraint(SpringLayout.SOUTH, ckbxShowAddressDialog, 0, SpringLayout.SOUTH, lblShowAddressDialog);
        add(ckbxShowAddressDialog);

        JLabel lblMergeNodesWithNeighbor = new JLabel(tr("Merge nodes with neighbors"));
        sl_panel.putConstraint(SpringLayout.NORTH, lblMergeNodesWithNeighbor, 21, SpringLayout.SOUTH, lblShowAddressDialog);
        sl_panel.putConstraint(SpringLayout.WEST, lblMergeNodesWithNeighbor, 0, SpringLayout.WEST, lblAlgorithmSettings);
        add(lblMergeNodesWithNeighbor);

        ckbxMergeNodes = new JCheckBox(tr("merge nodes"));
        sl_panel.putConstraint(SpringLayout.WEST, ckbxMergeNodes, 0, SpringLayout.WEST, ckbxShowAddressDialog);
        sl_panel.putConstraint(SpringLayout.SOUTH, ckbxMergeNodes, 0, SpringLayout.SOUTH, lblMergeNodesWithNeighbor);
        add(ckbxMergeNodes);
        
        JLabel lblThinningIterationsExplanation = new JLabel("<html><p>"+tr("How often thinning operation should be applied (Default {0}).",ImageAnalyzer.DEFAULT_THINNING_ITERATIONS)+"</p></html>");
        sl_panel.putConstraint(SpringLayout.NORTH, lblThinningIterationsExplanation, 40, SpringLayout.NORTH, lblToleranceAngle);
        sl_panel.putConstraint(SpringLayout.WEST, lblThinningIterationsExplanation, 0, SpringLayout.WEST, lblAlgorithmSettings);
        add(lblThinningIterationsExplanation);
        
        JLabel lblThinningIterations = new JLabel(tr("Thinning Iterations"));
        sl_panel.putConstraint(SpringLayout.NORTH, lbluserInterfaceSettings, 40, SpringLayout.SOUTH, lblThinningIterations);
        sl_panel.putConstraint(SpringLayout.NORTH, lblThinningIterations, 10, SpringLayout.SOUTH, lblThinningIterationsExplanation);
        sl_panel.putConstraint(SpringLayout.WEST, lblThinningIterations, 0, SpringLayout.WEST, lblAlgorithmSettings);
        add(lblThinningIterations);
        
        txtThinningIterations = new JTextField();
        sl_panel.putConstraint(SpringLayout.NORTH, txtThinningIterations, 10, SpringLayout.SOUTH, lblThinningIterationsExplanation);
        sl_panel.putConstraint(SpringLayout.EAST, txtThinningIterations, 0, SpringLayout.EAST, txtColorThreshold);
        txtThinningIterations.setColumns(10);
        add(txtThinningIterations);
        
        JLabel lblHSV = new JLabel(tr("Use hue and saturation instead of RGB distinction"));
        sl_panel.putConstraint(SpringLayout.NORTH, lblHSV, 20, SpringLayout.SOUTH, lblMergeNodesWithNeighbor);
        sl_panel.putConstraint(SpringLayout.WEST, lblHSV, 0, SpringLayout.WEST, lblMergeNodesWithNeighbor);
        add(lblHSV);
        
        ckbxHSV = new JCheckBox(tr("use HSV based algorithm"));
        sl_panel.putConstraint(SpringLayout.WEST, ckbxHSV, 0, SpringLayout.WEST, ckbxMergeNodes);
        sl_panel.putConstraint(SpringLayout.SOUTH, ckbxHSV, 0, SpringLayout.SOUTH, lblHSV);
        add(ckbxHSV);
    }

    

	/**
	 * @return the prefs
	 */
	public HashMap<String, String> getPrefs() {
		// make sure prefs are up to date
		prefs.put(ImageAnalyzer.KEY_COLORTHRESHOLD, txtColorThreshold.getText());
		prefs.put(ImageAnalyzer.KEY_THINNING_ITERATIONS, txtThinningIterations.getText());
		prefs.put(ImageAnalyzer.KEY_TOLERANCEANGLE, txtToleranceAngle.getText());
		prefs.put(ImageAnalyzer.KEY_TOLERANCEDIST, txtToleranceDist.getText());
		prefs.put(AreaSelectorAction.KEY_MERGENODES, ckbxMergeNodes.isSelected() ? "true":"false");
		prefs.put(AreaSelectorAction.KEY_SHOWADDRESSDIALOG, ckbxShowAddressDialog.isSelected() ? "true":"false");
		prefs.put(ImageAnalyzer.KEY_HSV, ckbxHSV.isSelected() ? "true":"false");
		return prefs;
	}

	/**
	 * @param prefs the prefs to set
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
			txtToleranceAngle.setText(prefs.get(ImageAnalyzer.KEY_TOLERANCEANGLE));
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
	}
}
