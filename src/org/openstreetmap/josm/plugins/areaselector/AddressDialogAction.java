// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Predicates;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 */
public class AddressDialogAction extends MapMode implements MouseListener {

    public static Logger log = LogManager.getLogger(AddressDialogAction.class);

    public static final String PLUGIN_NAME = "areaselector";

    public AddressDialogAction(MapFrame mapFrame) {
        super(/* I18n: Add tag to element */ tr("Tag Element"), "addressdialog", tr("Select an item to tag."),
                /* I18n: Add tag to building */
                Shortcut.registerShortcut("tools:tagbuilding",
                        tr("Tools: {0}", tr("Tag Building")), KeyEvent.VK_B, Shortcut.ALT_CTRL), mapFrame, getCursor());
    }

    private static Cursor getCursor() {
        return ImageProvider.getCursor("crosshair", "addressdialog");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openstreetmap.josm.actions.mapmode.MapMode#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);

        try {
            showAddressDialogFor(e.getPoint());
        } catch (Exception th) {
            log.warn("show Address Dialog failed", th);
            new BugReportDialog(th);
        }
    }

    public void showAddressDialogFor(Point point) {
        MapView mapView = Main.map.mapView;
        List<OsmPrimitive> elements = mapView.getNearestNodesOrWays(point, Predicates.<OsmPrimitive>alwaysTrue());

        OsmPrimitive element;
        if (!elements.isEmpty()) {
            element = elements.get(0);
            log.info("Found object " + element);
            Main.getLayerManager().getEditDataSet().setSelected(element);
            new AddressDialog(element).showAndSave();
        } else {
            log.info("Found no objects");
        }
    }

    @Override
    public void enterMode() {
        if (!isEnabled()) {
            return;
        }
        super.enterMode();
        Main.map.mapView.setCursor(getCursor());
        Main.map.mapView.addMouseListener(this);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
    }

}
