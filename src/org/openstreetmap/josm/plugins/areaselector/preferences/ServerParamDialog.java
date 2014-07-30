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
	private static final long serialVersionUID = -3229680217088662218L;
	
	private String[] m_astrCannyThreshold = new String[] {"20", "25", "30", "35", "40"};
	
	private ServerParam m_oParam;
	
    private JPanel m_oPanel = new JPanel(new GridBagLayout());
    private JTextField m_oName = new JTextField();
    private JTextField m_oDescription = new JTextField();
    private JTextArea m_oUrl = new JTextArea(5,5);
    private JComboBox<String> m_oCannyThreshold;
    
    
    private JScrollPane m_oScrollpaneUrl;
    
    public ServerParam getServerParam() {
    	return m_oParam;
    }
    
    private void addLabelled(String str, Component c) {
        JLabel label = new JLabel(str);
        m_oPanel.add(label, GBC.std());
        label.setLabelFor(c);
        m_oPanel.add(c, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    }
    
    private void addGap() {
        JPanel p = new JPanel();
        p.setMinimumSize(new Dimension(10,0));
        m_oPanel.add(p, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    }
    
    private void load() {
        m_oName.setText(m_oParam.getName());
        m_oDescription.setText(m_oParam.getDescription());
        m_oUrl.setText(m_oParam.getUrl());
        loadComboBox( m_oCannyThreshold, m_oParam.getCannyThreshold(), m_astrCannyThreshold);
    }
    
    private void save() {
    	m_oParam.setName(m_oName.getText());
    	m_oParam.setDescription(m_oDescription.getText());
    	m_oParam.setUrl(m_oUrl.getText());
    	m_oParam.setCannyThreshold(saveComboBox(m_oCannyThreshold, m_astrCannyThreshold));
    	
    }
    
    private void loadComboBox( JComboBox<?> c, String strValue, String[] astrValues ) {
        int pos = 0;
        for ( String str: astrValues ) {
        	if (strValue.equals(str)) {
            	c.setSelectedIndex(pos);
            	return;
        	}
        	pos++;
        }
    }
    
    private String saveComboBox( JComboBox<?> c, String[] astrValues ) {
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
        
        m_oCannyThreshold = new JComboBox<>(m_astrCannyThreshold);
                
        load();
        
        addLabelled(tr("Name:"), m_oName);
        addLabelled(tr("Description:"), m_oDescription);
        addGap();
        m_oUrl.setLineWrap(true);
        m_oScrollpaneUrl = new JScrollPane(m_oUrl);
        addLabelled(tr("URL:"), m_oScrollpaneUrl);
        addGap();
        addLabelled(tr("Canny Threshold:"), m_oCannyThreshold);
               
        
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
