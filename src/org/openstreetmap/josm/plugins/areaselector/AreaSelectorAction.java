// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openstreetmap.josm.actions.MergeNodesAction;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.PseudoCommand;
import org.openstreetmap.josm.command.RemoveNodesCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPreset;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresetItem;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresetSearchDialog;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresetType;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresets;
import org.openstreetmap.josm.gui.tagging.presets.items.Check;
import org.openstreetmap.josm.gui.tagging.presets.items.CheckGroup;
import org.openstreetmap.josm.gui.tagging.presets.items.ComboMultiSelect;
import org.openstreetmap.josm.gui.tagging.presets.items.Key;
import org.openstreetmap.josm.gui.tagging.presets.items.KeyedItem;
import org.openstreetmap.josm.gui.tagging.presets.items.Text;
import org.openstreetmap.josm.plugins.austriaaddresshelper.AustriaAddressHelperAction;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author Paul Woelfel
 *
 */
public class AreaSelectorAction extends MapMode implements MouseListener {

    protected int colorThreshold = ImageAnalyzer.DEFAULT_COLORTHRESHOLD,
            thinningIterations = ImageAnalyzer.DEFAULT_THINNING_ITERATIONS;
    protected double toleranceDist = ImageAnalyzer.DEFAULT_TOLERANCEDIST,
            toleranceAngle = ImageAnalyzer.DEFAULT_TOLERANCEANGLE;

    protected boolean showAddressDialog = true, mergeNodes = true, useAustriaAdressHelper = false,
            replaceBuildings = true, addSourceTag = false, applyPresetDirectly = false;
    protected String taggingStyle = "none";
    protected String taggingPresetName = "";

    public static final String PLUGIN_NAME = "areaselector";

    public static final String KEY_SHOWADDRESSDIALOG = PLUGIN_NAME + ".showaddressdialog",
            KEY_TAGGINGSTYLE = PLUGIN_NAME + ".taggingstyle",
            KEY_TAGGINGPRESETNAME = PLUGIN_NAME + ".taggingpresetname",
            KEY_MERGENODES = PLUGIN_NAME + ".mergenodes", KEY_AAH = PLUGIN_NAME + ".austriaadresshelper",
            KEY_REPLACEBUILDINGS = PLUGIN_NAME + ".replacebuildings",
            KEY_ADDSOURCETAG = PLUGIN_NAME + ".addsourcetag",
            KEY_APPLYPRESETDIRECTLY = PLUGIN_NAME + ".applypresetdirectly";

    protected Logger log = LogManager.getLogger(AreaSelectorAction.class.getCanonicalName());

    protected Point clickPoint = null;

    public AreaSelectorAction(MapFrame mapFrame) {
        super(tr("Area Selection"), "areaselector", tr("Select an area (e.g. building) from an underlying image."),
                Shortcut.registerShortcut("tools:areaselector", tr("Tools: {0}", tr("Area Selector")), KeyEvent.VK_A,
                        Shortcut.ALT_CTRL),
                getCursor());

        // load prefs
        this.readPrefs();
    }

    protected void readPrefs() {
        this.mergeNodes = new BooleanProperty(KEY_MERGENODES, true).get();
        this.showAddressDialog = new BooleanProperty(KEY_SHOWADDRESSDIALOG, true).get();
        this.taggingStyle = new StringProperty(KEY_TAGGINGSTYLE, "none").get();
        this.taggingPresetName = new StringProperty(KEY_TAGGINGPRESETNAME, "").get();
        useAustriaAdressHelper = new BooleanProperty(KEY_AAH, false).get();
        replaceBuildings = new BooleanProperty(KEY_REPLACEBUILDINGS, true).get();
        addSourceTag = new BooleanProperty(KEY_ADDSOURCETAG, false).get();
        applyPresetDirectly = new BooleanProperty(KEY_APPLYPRESETDIRECTLY, false).get();
    }

    private static Cursor getCursor() {
        return ImageProvider.getCursor("crosshair", "areaselector");
    }

    @Override
    public void enterMode() {
        if (!isEnabled()) {
            return;
        }
        super.enterMode();
        MainApplication.getMap().mapView.setCursor(getCursor());
        MainApplication.getMap().mapView.addMouseListener(this);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        MainApplication.getMap().mapView.removeMouseListener(this);
    }

    public void updateMapFrame(MapFrame oldFrame, MapFrame newFrame) {
        // or not, we just use Main to get the current mapFrame
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on
     * a component.
     */
    @Override
    public void mouseClicked(MouseEvent e) {

        log.info("mouse clicked " + e);

        if (!MainApplication.getMap().mapView.isActiveLayerDrawable()) {
            return;
        }
        requestFocusInMapView();
        updateKeyModifiers(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            clickPoint = e.getPoint();
            LatLon coordinates = MainApplication.getMap().mapView.getLatLon(clickPoint.x, clickPoint.y);
            new Notification("<strong>" + tr("Area Selector") + "</strong><br />" + tr("Trying to detect an area at:")
            + "<br>" + coordinates.getX() + ", " + coordinates.getY()).setIcon(JOptionPane.INFORMATION_MESSAGE)
            .show();
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        createArea();
                    } catch (Exception ex) {
                        log.error("failed to add area", ex);
                        new BugReportDialog(ex);
                    }
                }
            });

        }
    }

    public BufferedImage getLayeredImage() {
        MapView mapView = MainApplication.getMap().mapView;

        BufferedImage bufImage = new BufferedImage(mapView.getWidth(), mapView.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D imgGraphics = bufImage.createGraphics();
        imgGraphics.setClip(0, 0, mapView.getWidth(), mapView.getHeight());

        for (Layer layer : mapView.getLayerManager().getVisibleLayersInZOrder()) {
            if (layer.isVisible() && layer.isBackgroundLayer()) {
                Composite translucent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) layer.getOpacity());
                imgGraphics.setComposite(translucent);
                mapView.paintLayer(layer, imgGraphics);
            }
        }

        return bufImage;
    }

    public void createArea() {
        ////////// Polygon generation
        MapView mapView = MainApplication.getMap().mapView;

        BufferedImage bufImage = getLayeredImage();

        ImageAnalyzer imgAnalyzer = new ImageAnalyzer(bufImage, clickPoint);

        // adjust distance to pixel instead of meters
        double distMeters = new DoubleProperty(ImageAnalyzer.KEY_TOLERANCEDIST, ImageAnalyzer.DEFAULT_TOLERANCEDIST)
                .get();

        double toleranceInPixel = distMeters * 100 / mapView.getDist100Pixel();
        // log.info("tolerance in m: "+distMeters + " in pixel:
        // "+toleranceInPixel + " 100px in m: "+mapView.getDist100Pixel());
        imgAnalyzer.setToleranceDist(toleranceInPixel);

        Polygon polygon = imgAnalyzer.getArea();
        if (polygon == null) {
            JOptionPane.showMessageDialog(MainApplication.getMap(), tr("Unable to detect a polygon where you clicked."),
                tr("Area Selector"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        Way way = createWayFromPolygon(mapView, polygon);
        Way newWay = null;

        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        Collection<Command> cmds = new LinkedList<>();
        List<Node> nodes = way.getNodes();
        for (int i = 0; i < nodes.size() - 1; i++) {
            cmds.add(new AddCommand(ds, nodes.get(i)));
        }
        cmds.add(new AddCommand(ds, way));
        UndoRedoHandler.getInstance().add(new SequenceCommand(/* I18n: Name of the command that adds the generated way */ tr("create closed way"), cmds));

        if (replaceBuildings) {
            Way existingWay = MainApplication.getMap().mapView.getNearestWay(clickPoint, OsmPrimitive::isUsable);
            if (existingWay != null && way.getBBox().bounds(existingWay.getBBox().getCenter())) {
                log.info("existing way is inside of new building: " + existingWay.toString() + " is in " + way.toString());
                UndoRedoHandler.getInstance().add(new SequenceCommand(tr("replace building"), replaceWay(existingWay, way)));
                way = existingWay;
            }
        }

        ds.setSelected(way);

        if (mergeNodes) {
            mergeNodes(way);
        }

        if (useAustriaAdressHelper) {
            Map<String, String> newAddress = fetchAddress(way);
            if (newAddress != null) {
                newWay = new Way(way);
                newWay.setKeys(newAddress);
                log.info("Found attributes: {}", newWay.getKeys());
                if (!showAddressDialog) {
                    final List<Command> commands = new ArrayList<>();
                    commands.add(new ChangeCommand(way, newWay));
                    UndoRedoHandler.getInstance().add(
                            new SequenceCommand(trn("Add address", "Add addresses", commands.size()), commands));
                }
            }
        }

        if (!showAddressDialog && addSourceTag) {
            ArrayList<String> sources = new ArrayList<>();
            for (Layer layer : mapView.getLayerManager().getVisibleLayersInZOrder()) {
                if (layer.isVisible() && layer.isBackgroundLayer()) {
                    sources.add(layer.getName());
                }
            }
            Collections.reverse(sources);
            String source = sources.stream().map(Object::toString).collect(Collectors.joining("; "));
            if (!source.isEmpty()) {
                way.put(AddressDialog.TAG_SOURCE, source);
            }
        }

        ////////// Tagging
        if (taggingStyle.equals("none")) {
            // Do nothing
        } else if (taggingStyle.equals("presetSearchDialog")) {
            if (getLayerManager().getActiveData() == null) {
                JOptionPane.showMessageDialog(
                    MainApplication.getMap(),
                    tr("There is no active data layer, cannot show preset search dialog."),
                    tr("Area Selector"),
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            TaggingPresetSearchDialog.getInstance().showDialog();
        } else if (taggingStyle.equals("specificPreset")) {
            if (taggingPresetName.isEmpty()) {
                JOptionPane.showMessageDialog(
                    MainApplication.getMap(),
                    tr("No preset name configured in the settings, enter one."),
                    tr("Area Selector"),
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Find the configured preset
            TaggingPreset taggingPresetToApply = null;
            for (TaggingPreset taggingPreset : TaggingPresets.getTaggingPresets()) {
                if (taggingPreset.getRawName().equals(taggingPresetName)) {
                    taggingPresetToApply = taggingPreset;
                    break;
                }
            }
            if (taggingPresetToApply == null) {
                JOptionPane.showMessageDialog(
                    MainApplication.getMap(),
                    tr("Could not find configured tagging preset: '{0}'.", taggingPresetName),
                    tr("Area Selector"),
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Check that tagging preset can be applied to closed ways
            if (!taggingPresetToApply.types.contains(TaggingPresetType.CLOSEDWAY)) {
                JOptionPane.showMessageDialog(
                    MainApplication.getMap(),
                    tr("Selected preset is not suitable for a closed way, select another one: '{0}'.", taggingPresetName),
                    tr("Area Selector"),
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (applyPresetDirectly) {
                // Directly apply the tags of the configured preset
                Collection<Tag> tags = getTagsForPreset(taggingPresetToApply);
                Collection<Command> commands = new ArrayList<>();
                for (Tag tag : tags) {
                    commands.add(new ChangePropertyCommand(way, tag.getKey(), tag.getValue()));
                }

                if (commands.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        MainApplication.getMap(),
                        tr("Tagging preset '{0}' does not have any tags to apply.", taggingPresetToApply.getRawName()),
                        tr("Area Selector"),
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                // Apply the command
                Command cmd = SequenceCommand.wrapIfNeeded(tr("Apply tagging preset default values: {0}", taggingPresetToApply.getRawName()), commands);
                UndoRedoHandler.getInstance().add(cmd);
            } else {
                // Show the dialog of the configured taggin preset
                taggingPresetToApply.showAndApply(Collections.singleton(way));
            }
        } else if (taggingStyle.equals("address")) {
            way.put(AddressDialog.TAG_BUILDING, new StringProperty(AddressDialog.PREF_BUILDING, "yes").get());
            if (showAddressDialog) {
                if (newWay == null) {
                    newWay = way;
                }
                new AddressDialog(newWay, way).showAndSave();
            }
        } else {
            JOptionPane.showMessageDialog(
                MainApplication.getMap(),
                tr("Unknown tagging style setting: '{0}'.", taggingStyle),
                tr("Area Selector"),
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Get all tags that should be applied directly for a given TaggingPreset
     */
    public Collection<Tag> getTagsForPreset(TaggingPreset taggingPreset) {
        Collection<Tag> tags = new ArrayList<>();
        for (TaggingPresetItem item : taggingPreset.data) {
            // Determine key (used for most single-tag TaggingPresetItem's)
            String key = null;
            if (item instanceof KeyedItem) {
                key = ((KeyedItem) item).key;
            }

            // Detect item type
            if (item instanceof Text) {
                addTag(tags, key, ((Text) item).default_);
            } else if (item instanceof Key) {
                addTag(tags, key, ((Key) item).value);
            } else if (item instanceof Check) {
                addTag(tags, key, ((Check) item).default_);
            } else if (item instanceof CheckGroup) {
                for (Check check : ((CheckGroup) item).checks) {
                    addTag(tags, check.key, check.default_);
                }
            } else if (item instanceof ComboMultiSelect) {
                addTag(tags, key, ((ComboMultiSelect) item).default_);
            }
            // Ignore other TaggingPresetItem's: like spacing/links/etc.
        }

        return tags;
    }

    /**
     * Add a Tag to a Collection only when the key/value have proper values
     */
    private void addTag(Collection<Tag> tags, String key, String value) {
        if (key == null || key.equals("") || value == null || value.equals("")) {
            return;
        }
        tags.add(new Tag(key, value));
    }

    /**
     * fetch Address using Austria Address Helper
     */
    public Map<String, String> fetchAddress(OsmPrimitive selectedObject) {
        try {
            log.info("trying to fetch address ");
            return AustriaAddressHelperAction.loadAddress(selectedObject);
        } catch (Throwable th) {
            if (th instanceof NoClassDefFoundError) {
                log.warn("Austria Address Helper not loaded");
            } else {
                log.warn("Something went wrong with Austria Adress Helper", th);
            }
        }
        return null;
    }

    /**
     * replace an existing way with the new detected one
     * @param existingWay old way
     * @param newWay new way
     * @return replaced way
     */
    public List<Command> replaceWay(Way existingWay, Way newWay){
        if (existingWay == null || newWay == null){
            return null;
        }
        ArrayList<Command> cmds = new ArrayList<>();

        cmds.add(new RemoveNodesCommand(existingWay, new HashSet<>(existingWay.getNodes())));

        int i = 1;
        for (Node existingNode : existingWay.getNodes()){
            if (i < existingWay.getNodesCount() && existingNode.getParentWays().size() == 1 && !existingNode.isDeleted() && !existingNode.isNew()){
                //				log.info("delete node "+existingNode.toString());
                cmds.add(new DeleteCommand(existingNode));
            }
            i++;
        }

        for (Node newNode : newWay.getNodes()){
            existingWay.addNode(newNode);
        }
        if(newWay.isNew() && !newWay.isDeleted()){
            cmds.add(new DeleteCommand(newWay));
        }

        return cmds;
    }

    public Way createWayFromPolygon(MapView mapView, Polygon polygon) {
        Way way = new Way();

        Node firstNode = null;
        for (int i = 0; i < polygon.npoints; i++) {
            Node node = new Node(mapView.getLatLon(polygon.xpoints[i], polygon.ypoints[i]));
            if (firstNode == null) {
                firstNode = node;
            }
            way.addNode(node);
        }

        if (polygon.npoints > 1 && firstNode != null) {
            way.addNode(firstNode);
        }
        return way;
    }

    /**
     * Merge Nodes on way to existing nodes
     */
    public Way mergeNodes(Way way) {
        List<Node> deletedNodes = new ArrayList<>();
        for (int i = 0; i < way.getNodesCount(); i++) {
            Node node = way.getNode(i);

            List<Node> selectedNodes = new ArrayList<>();
            selectedNodes.add(node);
            MapView mapView = MainApplication.getMap().mapView;
            List<Node> nearestNodes = mapView.getNearestNodes(mapView.getPoint(selectedNodes.get(0)),
                    selectedNodes, OsmPrimitive::isUsable);

            for (Node n : nearestNodes) {
                if (!way.containsNode(n) && !deletedNodes.contains(n)) {
                    selectedNodes.add(n);
                }
            }
            if (selectedNodes.size() > 1) {
                Node targetNode = MergeNodesAction.selectTargetNode(selectedNodes);
                Node targetLocationNode = MergeNodesAction.selectTargetLocationNode(selectedNodes);
                Command c = MergeNodesAction.mergeNodes(selectedNodes, targetNode, targetLocationNode);

                if (c != null) {
                    UndoRedoHandler.getInstance().add(c);
                    for (PseudoCommand subCommand : c.getChildren()) {
                        if (subCommand instanceof DeleteCommand) {
                            DeleteCommand dc = (DeleteCommand) subCommand;
                            // check if a deleted node is in the way
                            for (OsmPrimitive p : dc.getParticipatingPrimitives()) {
                                if (p instanceof Node) {
                                    deletedNodes.add((Node) p);
                                }
                            }
                        }
                    }
                }
            }
        }

        return (Way) MainApplication.getLayerManager().getEditDataSet().getPrimitiveById(way.getId(), OsmPrimitiveType.WAY);
    }

    /**
     * merge node with neighbor nodes
     *
     * @param node
     *            node to merge
     * @return true if node was merged
     */
    public Command mergeNode(Node node) {
        List<Node> selectedNodes = new ArrayList<>();
        selectedNodes.add(node);
        MapView mapView = MainApplication.getMap().mapView;
        List<Node> nearestNodes = mapView.getNearestNodes(mapView.getPoint(selectedNodes.get(0)),
                selectedNodes, OsmPrimitive::isUsable);
        selectedNodes.addAll(nearestNodes);
        Node targetNode = MergeNodesAction.selectTargetNode(selectedNodes);
        Node targetLocationNode = MergeNodesAction.selectTargetLocationNode(selectedNodes);
        return MergeNodesAction.mergeNodes(selectedNodes, targetNode, targetLocationNode);
    }

    public void setPrefs() {
        this.readPrefs();
    }
}
