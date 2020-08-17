// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 */
public class BugReportDialog extends ExtendedDialog {

    protected static final String[] BUTTON_TEXTS = new String[] {tr("OK")};

    protected static final String[] BUTTON_ICONS = new String[] {"ok"};

    public static final String ISSUESLINK = "https://github.com/JOSM/JOSM-areaselector/issues";

    protected static Logger log = LogManager.getLogger(BugReportDialog.class);

    public BugReportDialog(Throwable ex) {
        super(MainApplication.getMainFrame(), tr("Error Report"), BUTTON_TEXTS, true);
        setButtonIcons(BUTTON_ICONS);
        JPanel panel = new JPanel(new BorderLayout());
        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<h1>" + tr("Something went wrong!") + "</h1>");
        html.append("<p>" + tr("Please file a bug report on the github project page under")
        + " <a href=\""+ISSUESLINK+"\">"+ISSUESLINK+"</a>.<br><br></p>");
        html.append("<p>" +
                tr("Let us know what you did and what happend add the following text to the bug report, so we can find the source of the issue.")
        + "<br><br></p>");

        html.append("</html>");
        JLabel content = new JLabel(html.toString());
        content.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(ISSUESLINK));
                } catch (IOException | URISyntaxException ex) {
                    log.error("could not open URI", ex);
                }
            }
        });

        panel.add(content, BorderLayout.NORTH);
        JTextArea stacktrace = new JTextArea();
        stacktrace.setText("```\n"+getStacktraceAsString(ex)+"\n```");
        panel.add(new JScrollPane(stacktrace), BorderLayout.CENTER);
        setContent(panel);

        this.showDialog();
    }

    public static String getStacktraceAsString(Throwable th) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        return sw.toString();
    }

    public static String getStackTraceAsHtml(Throwable th) {
        String exception = getStacktraceAsString(th);
        return exception.replaceAll("\n", "<br>\n");
    }

}
