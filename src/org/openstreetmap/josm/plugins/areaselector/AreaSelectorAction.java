/**
 * 
 */
package org.openstreetmap.josm.plugins.areaselector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author Paul Woelfel
 * 
 */
public class AreaSelectorAction extends MapMode implements MouseListener {

	protected Logger log = Logger.getLogger(AreaSelectorAction.class.getCanonicalName());
	
	protected Point clickPoint=null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AreaSelectorAction(MapFrame mapFrame) {
		super(tr("Area Selection"), "tracer-sml", tr("Select an area i.e. building from an underlying image."), Shortcut.registerShortcut("tools:areaselector",
				tr("Tools: {0}", tr("Area Selector")), KeyEvent.VK_A, Shortcut.ALT_CTRL), mapFrame, getCursor());
	}

	private static Cursor getCursor() {
		return ImageProvider.getCursor("crosshair", "tracer-sml");
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

	public void updateMapFrame(MapFrame oldFrame, MapFrame newFrame) {
		// TODO something should change now?!?
		// or not, we just use Main to get the current mapFrame
	}

	/**
	 * Invoked when the mouse button has been clicked (pressed and released) on a component.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {

		log.info("mouse clicked " + e);

		if (!Main.map.mapView.isActiveLayerDrawable()) {
			return;
		}
		requestFocusInMapView();
		updateKeyModifiers(e);
		if (e.getButton() == MouseEvent.BUTTON1) {
			try {
				clickPoint=e.getPoint();
				createArea();
			} catch (Throwable th) {
				log.error("failed to add area", th);
			}

		}
	}
	
	
	public BufferedImage getLayeredImage(){
		MapView mapView = Main.map.mapView;
		// Collection<Layer> layers=mapView.getAllLayers();
		// Layer activeLayer=mapView.getActiveLayer();

		BufferedImage bufImage = new BufferedImage(mapView.getWidth(), mapView.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D imgGraphics = bufImage.createGraphics();

		for (Layer layer : mapView.getAllLayers()) {
			layer.paint(imgGraphics, mapView, mapView.getRealBounds());
		}
		
		return bufImage;
	}
	

	public void createArea() {

		MapView mapView = Main.map.mapView;
		
		BufferedImage bufImage = getLayeredImage();

		ImageAnalyzer imgAnalyzer = new ImageAnalyzer(bufImage, clickPoint);
		Polygon polygon = imgAnalyzer.getArea();

		if (polygon != null) {
			Way way = createWayFromPolygon(mapView, polygon);

			way.put(AddressDialog.TAG_BUILDING, "yes");


			Collection<Command> cmds = new LinkedList<Command>();
			List<Node> nodes = way.getNodes();
			for (int i = 0; i < nodes.size() - 1; i++) {

				cmds.add(new AddCommand(nodes.get(i)));
			}
			// w.setKeys(ToolSettings.getTags());
			cmds.add(new AddCommand(way));

			Command c = new SequenceCommand(tr("Created area"), cmds);
			Main.main.undoRedo.add(c);
			Main.main.getCurrentDataSet().setSelected(way);
			showAddressDialog(way);
		}

	}

	public Way showAddressDialog(Way way) {
		
		AddressDialog dialog = new AddressDialog(way);
		dialog.showDialog();
		if (dialog.getValue() == 1){
			dialog.saveValues();
			Collection<Command> cmds = new LinkedList<Command>();
			cmds.add(new ChangeCommand(way, way));
			Command c = new SequenceCommand(tr("updated building info"), cmds);
			Main.main.undoRedo.add(c);
			Main.main.getCurrentDataSet().setSelected(way);
		}
		return way;
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

}
