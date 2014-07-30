/**
 *  This file has been taken and slightly modified from the Tracer2 JOSM plugin.
 */

package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

public class ServerParamDialog extends ExtendedDialog {
    /**
	 * 
	 */
	protected static final long serialVersionUID = -3229680217088662218L;
	
	protected String[] m_astrColorThreshold = new String[] {"5", "10", "15", "20", "25"};
	protected String[] m_astrToleranceDist = new String[] {"1", "2", "3", "4", "5"};
	protected String[] m_astrToleranceAngle = new String[] {"0.1", "0.2", "0.3", "0.4", "0.5"};
	
	protected ServerParam m_oParam;
	
    protected JPanel m_oPanel = new JPanel(new GridBagLayout());
    protected JTextField m_oName = new JTextField();
    protected JTextField m_oDescription = new JTextField();
    protected JTextArea m_oUrl = new JTextArea(5,5);
    protected JTextField m_oColorThreshold;
    protected JTextField m_oToleranceDist;
    protected JTextField m_oToleranceAngle;
    
    
    protected JScrollPane m_oScrollpaneUrl;
    
    public ServerParam getServerParam() {
    	return m_oParam;
    }
    
    protected void addLabelled(String str, Component c) {
        JLabel label = new JLabel(str);
        m_oPanel.add(label, GBC.std());
        label.setLabelFor(c);
        m_oPanel.add(c, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    }
    
    protected void addGap() {
        JPanel p = new JPanel();
        p.setMinimumSize(new Dimension(10,0));
        m_oPanel.add(p, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    }
    
    protected void load() {
        m_oName.setText(m_oParam.getName());
        m_oDescription.setText(m_oParam.getDescription());
        m_oUrl.setText(m_oParam.getUrl());
        m_oColorThreshold.setText(m_oParam.getColorThreshold());
        m_oToleranceDist.setText( m_oParam.getToleranceDist());
        m_oToleranceAngle.setText(m_oParam.getToleranceAngle());
    }
    
    protected void save() {
    	m_oParam.setName(m_oName.getText());
    	m_oParam.setDescription(m_oDescription.getText());
    	m_oParam.setUrl(m_oUrl.getText());
    	m_oParam.setColorThreshold(m_oColorThreshold.getText());
    	m_oParam.setToleranceDist(m_oToleranceDist.getText());
    	m_oParam.setToleranceAngle(m_oToleranceAngle.getText());
    	
    }
    
    protected void loadComboBox( JComboBox<?> c, String strValue, String[] astrValues ) {
        int pos = 0;
        for ( String str: astrValues ) {
        	if (strValue.equals(str)) {
            	c.setSelectedIndex(pos);
            	return;
        	}
        	pos++;
        }
    }
    
    protected String saveComboBox( JComboBox<?> c, String[] astrValues ) {
        return astrValues[c.getSelectedIndex()];
    }
    
    public ServerParamDialog(ServerParam param) {
        super(Main.parent, tr("Area Selector") + " - " + tr("Parameter for server request"),
                new String[] { tr("OK"), tr("Cancel") },
                true);
        if (param == null) {
        	m_oParam = new ServerParam();
        } else {
        	m_oParam = param;
        }
        
        contentInsets = new Insets(15, 15, 5, 15);
        setButtonIcons(new String[] { "ok.png", "cancel.png" });
        
        m_oColorThreshold = new JTextField();
        m_oToleranceDist = new JTextField();
        m_oToleranceAngle = new JTextField();    	
                
        load();
        
        addLabelled(tr("Name:"), m_oName);
        addLabelled(tr("Description:"), m_oDescription);
        addGap();
        m_oUrl.setLineWrap(true);
        m_oScrollpaneUrl = new JScrollPane(m_oUrl);
        addLabelled(tr("URL:"), m_oScrollpaneUrl);
        addGap();
        addLabelled(tr("Color Threshold:"), m_oColorThreshold);
        addLabelled(tr("Tolerance Distance:"), m_oToleranceDist);
        addLabelled(tr("Tolerance Angle:"), m_oToleranceAngle);
               
        
        setMinimumSize(new Dimension(500, 0));
        
        setContent(m_oPanel);
        setupDialog();
    }
    
    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        if (evt.getActionCommand().equals(tr("OK"))) {
            save();
        } else {
        	m_oParam = null;
        }
        super.buttonAction(buttonIndex, evt);
    }
}
