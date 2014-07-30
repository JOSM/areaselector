/**
 *  This file has been taken and slightly modified from the Tracer2 JOSM plugin.
 */

package org.openstreetmap.josm.plugins.areaselector.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.widgets.JMultilineLabel;
import org.openstreetmap.josm.tools.GBC;

public class ServerParam {
	
    protected boolean m_bEnabled;
    private String m_strName = "Name";
    private String m_strDescription = "";
    private String m_strUrl = "";
    private String m_strCannyThreshold = "30";
    
    protected JMenuItem m_oMenuItem;
    
    public boolean isEnabled() {
        return m_bEnabled;
    }
    public void setEnabled(boolean enabled) {
    	if (!m_bEnabled ^ enabled)
            return;
        m_bEnabled = enabled;
    }
    
    public String getName() {
        return m_strName;
    }
    public void setName(String name) {
        m_strName = name;
    }
    
    public String getDescription() {
        return m_strDescription;
    }
    public void setDescription(String description) {
        m_strDescription = description;
    }
    
    public String getUrl() {
        return m_strUrl;
    }
    public void setUrl(String url) {
        m_strUrl = url;
    }
    
    public String getCannyThreshold() {
    	return m_strCannyThreshold;
    }
    
    public void setCannyThreshold(String cannyThreshold) {
        m_strCannyThreshold = cannyThreshold;
    }
    
    public ServerParam() {
        m_bEnabled = false;
    }
    
    public ServerParam(String name) {
        this();
        m_strName = name;
    }
    
    public String serialize() {
        StringBuilder oBuilder = new StringBuilder();
        oBuilder.append("name=").append(m_strName).append('\n');
        oBuilder.append("description=").append(m_strDescription).append('\n');
        oBuilder.append("url=").append(m_strUrl).append('\n');
        oBuilder.append("cannyThreshold=").append(m_strCannyThreshold).append('\n');
        oBuilder.append("enabled=").append(m_bEnabled).append('\n');
        oBuilder.append('\n');
        return oBuilder.toString();
    }
    
    public static ServerParam unserialize(String str) {
    	ServerParam oParam = new ServerParam();
        String[] lines = str.split("\n");
        for (String line : lines) {
            String[] parts = line.split("=", 2);
            if (parts[0].equals("name"))
                oParam.m_strName = parts[1];
            else if (parts[0].equals("description"))
                oParam.m_strDescription = parts[1];
            else if (parts[0].equals("url"))
                oParam.m_strUrl = parts[1];
            else if (parts[0].equals("cannyThreshold"))
                oParam.m_strCannyThreshold = parts[1];
            else if (parts[0].equals("enabled"))
                oParam.m_bEnabled = parts[1].equalsIgnoreCase("true");
        }
        return oParam;
    }
    
    protected void showErrorMessage(String message, String details) {
        final JPanel p = new JPanel(new GridBagLayout());
        p.add(new JMultilineLabel(message),GBC.eol());
        if (details != null) {
            JTextArea info = new JTextArea(details, 20, 60);
            info.setCaretPosition(0);
            info.setEditable(false);
            p.add(new JScrollPane(info), GBC.eop());
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(Main.parent, p, tr("Area Selector error"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
}
