/*
 * Created on Jul 31, 2014
 * Author: Paul Woelfel
 * Email: paul@woelfel.at
 */
package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

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
        txtColorThreshold.setText("15");
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
        txtToleranceDist.setText("3");
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
        txtToleranceAngle.setText("0.4");
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
        txtThinningIterations.setText("0.4");
        txtThinningIterations.setColumns(10);
        add(txtThinningIterations);
    }

    /**
     * get the current color threshold from the text field
     * @return the color threshold
     */
    public int getColorThreshold(){
        try {
            return Integer.parseInt(txtColorThreshold.getText());
        }catch (NumberFormatException e){
            return ImageAnalyzer.DEFAULT_COLORTHRESHOLD;
        }
    }

    /**
     * Set the color threshold
     * @param ct color threshold
     */
    public void setColorThreshold(int ct){
        txtColorThreshold.setText(Integer.toString(ct));
    }


    /**
     * get the current tolerance distance from the text field
     * @return the tolerance distance
     */
    public double getToleranceDist(){
        try {
            return Double.parseDouble(txtToleranceDist.getText());
        }catch (NumberFormatException e){
            return ImageAnalyzer.DEFAULT_TOLERANCEDIST;
        }
    }

    /**
     * Set the tolerance distance
     * @param ct tolerance distance
     */
    public void setToleranceDist(double ct){
        txtToleranceDist.setText(Double.toString(ct));
    }

    /**
     * get the current tolerance angle from the text field
     * @return the tolerance angle
     */
    public double getToleranceAngle(){
        try {
            return Double.parseDouble(txtToleranceAngle.getText());
        }catch (NumberFormatException e){
            return ImageAnalyzer.DEFAULT_TOLERANCEANGLE;
        }
    }

    /**
     * Set the tolerance angle
     * @param ct tolerance angle
     */
    public void setToleranceAngle(double ct){
        txtToleranceAngle.setText(Double.toString(ct));
    }

    /**
     * return if the the show address dialog checkbox is selected
     * @return true if selected
     */
    public boolean getShowAddressDialog() {
        return ckbxShowAddressDialog.isSelected();
    }

    /**
     * select the checkbox to show Address dialog
     * @param show true if selected
     */
    public void setShowAddressDialog(boolean show){
        ckbxShowAddressDialog.setSelected(show);
    }

    /**
     * return if the the merge nodes checkbox is selected
     * @return true if selected
     */
    public boolean getMergeNodes() {
        return ckbxMergeNodes.isSelected();
    }

    /**
     * select the checkbox to merge Nodes
     * @param merge true if selected
     */
    public void setMergeNodes(boolean merge){
        ckbxMergeNodes.setSelected(merge);
    }
    
	/**
	 * get Thinning Iterations
	 * @return
	 */
	public int getThinningIterations() {
		try{
			return Integer.parseInt(txtThinningIterations.getText());
		}catch(NumberFormatException e){
			return ImageAnalyzer.DEFAULT_THINNING_ITERATIONS;
		}
	}
	
	/**
	 * set text field to value of thinning Iterations
	 * @param thinningIterations
	 */
	public void setThinningIterations(int thinningIterations) {
		txtThinningIterations.setText(Integer.toString(thinningIterations));
	}
}
