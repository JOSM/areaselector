/**
 * 
 */
package org.openstreetmap.josm.plugins.areaselector;

import static org.openstreetmap.josm.tools.I18n.tr;

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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.TMSLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author Paul Woelfel
 * 
 */
public class AreaSelectorAction extends MapMode implements MouseListener {
	
	protected int colorThreshold=ImageAnalyzer.DEFAULT_COLORTHRESHOLD;
	protected double toleranceDist=ImageAnalyzer.DEFAULT_TOLERANCEDIST,toleranceAngle=ImageAnalyzer.DEFAULT_TOLERANCEANGLE;
	
	public static final String PLUGIN_NAME="areaselector";
	
	public static final String PREF_COLORTHRESHOLD=PLUGIN_NAME+".colorthreshold",
			PREF_TOLERANCEDIST=PLUGIN_NAME+".tolerancedist",
			PREF_TOLERANCEANGLE=PLUGIN_NAME+".toleranceangle";
	

	protected Logger log = Logger.getLogger(AreaSelectorAction.class.getCanonicalName());
	
	protected Point clickPoint=null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AreaSelectorAction(MapFrame mapFrame) {
		super(tr("Area Selection"), "areaselector", tr("Select an area (e.g. building) from an underlying image."), Shortcut.registerShortcut("tools:areaselector",
				tr("Tools: {0}", tr("Area Selector")), KeyEvent.VK_A, Shortcut.ALT_CTRL), mapFrame, getCursor());
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
		Main.map.mapView.setCursor(getCursor());
		Main.map.mapView.addMouseListener(this);
	}

	@Override
	public void exitMode() {
		super.exitMode();
		Main.map.mapView.removeMouseListener(this);
	}

	public void updateMapFrame(MapFrame oldFrame, MapFrame newFrame) {
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
		
		zoomToBestFactor();

		BufferedImage bufImage = new BufferedImage(mapView.getWidth(), mapView.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D imgGraphics = bufImage.createGraphics();

		Layer[] layers=mapView.getAllLayers().toArray(new Layer[0]);
		
		for (int i=layers.length-1;i>=0;i--) {
			Layer layer=layers[i];
			if(layer.isVisible() && layer.isBackgroundLayer()){
				Composite translucent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) layer.getOpacity());
				imgGraphics.setComposite(translucent);
				layer.paint(imgGraphics, mapView, mapView.getRealBounds());
			}
		}
		
		return bufImage;
	}
	

	public void createArea() {

		MapView mapView = Main.map.mapView;
		
		BufferedImage bufImage = getLayeredImage();

		ImageAnalyzer imgAnalyzer = new ImageAnalyzer(bufImage, clickPoint);
		
		imgAnalyzer.setColorThreshold(colorThreshold);
		imgAnalyzer.setToleranceDist(toleranceDist);
		imgAnalyzer.setToleranceAngle(toleranceAngle);
		
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

			Command c = new SequenceCommand(/* I18n: Name of command */ tr("Created area"), cmds);
			Main.main.undoRedo.add(c);
			Main.main.getCurrentDataSet().setSelected(way);
			
			// TODO ConnectWays extends an area instead of snaping it togeter
//			ConnectWays.connect(way, mapView.getLatLon(clickPoint.x, clickPoint.y));
			
			showAddressDialog(way);
		}else {
			JOptionPane.showMessageDialog(Main.map, tr("Unable to detect a polygon where you clicked."), tr("Area Selector"), JOptionPane.WARNING_MESSAGE);
		}

	}

	public OsmPrimitive showAddressDialog(Way way) {
		return new AddressDialog(way).showAndSave();
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
	
	protected double getScaleFactor(int zoom, TileSource tileSource) {
        if (!Main.isDisplayingMapView()) return 1;
        MapView mv = Main.map.mapView;
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
        double x1 = tileSource.lonToTileX(topLeft.lon(), zoom);
        double y1 = tileSource.latToTileY(topLeft.lat(), zoom);
        double x2 = tileSource.lonToTileX(botRight.lon(), zoom);
        double y2 = tileSource.latToTileY(botRight.lat(), zoom);

        int screenPixels = mv.getWidth()*mv.getHeight();
        double tilePixels = Math.abs((y2-y1)*(x2-x1)*tileSource.getTileSize()*tileSource.getTileSize());
        if (screenPixels == 0 || tilePixels == 0) return 1;
        return screenPixels/tilePixels;
    }
	
	
	protected void zoomToBestFactor(){
		MapView mapView=Main.map.mapView;
		
		Layer[] layers=mapView.getAllLayers().toArray(new Layer[0]);
		
		EastNorth newCenter= mapView.getEastNorth(clickPoint.x, clickPoint.y);
		
		for (int i=layers.length-1;i>=0;i--) {
			Layer layer=layers[i];
			if(layer.isVisible() && layer.isBackgroundLayer()){
		
				if (layer instanceof TMSLayer) {
					// Only TMS supported for now
					TMSLayer tmslayer=((TMSLayer) layer);
					ImageryInfo info = tmslayer.getInfo();
					TileSource tileSource = TMSLayer.getTileSource(info);
					
					// Zoom to Maximum Zoom Level
					//((TMSLayer) layer).setZoomLevel(tileSource.getMaxZoom());
					double new_factor = Math.sqrt(getScaleFactor(tileSource.getMaxZoom(), tileSource));
					mapView.zoomToFactor(newCenter, new_factor);
			        mapView.zoomToFactor(new_factor);
			        
			        mapView.repaint();
			        
					// For now we break at the first found TMS layer
			        break;
				}
			}
		}

	}


	/**
	 * @return the colorThreshold
	 */
	public int getColorThreshold() {
		// refresh from prefs
		try{
		this.colorThreshold=Integer.parseInt(Main.pref.get(PREF_COLORTHRESHOLD, Integer.toString(ImageAnalyzer.DEFAULT_COLORTHRESHOLD)));
		}catch(Throwable th){}
		return colorThreshold;
	}

	/**
	 * @param colorThreshold the colorThreshold to set
	 */
	public void setColorThreshold(int colorThreshold) {
		Main.pref.put(PREF_COLORTHRESHOLD, Integer.toString(colorThreshold));
		this.colorThreshold = colorThreshold;
	}

	/**
	 * @return the toleranceDist
	 */
	public double getToleranceDist() {
		try{
			this.toleranceDist=Integer.parseInt(Main.pref.get(PREF_TOLERANCEDIST, Double.toString(ImageAnalyzer.DEFAULT_TOLERANCEDIST)));
		}catch(Throwable th){}
		return toleranceDist;
	}

	/**
	 * @param toleranceDist the toleranceDist to set
	 */
	public void setToleranceDist(double toleranceDist) {
		Main.pref.put(PREF_TOLERANCEDIST, Double.toString(toleranceDist));
		this.toleranceDist = toleranceDist;
	}

	/**
	 * @return the toleranceAngle
	 */
	public double getToleranceAngle() {
		try{
			this.toleranceAngle=Integer.parseInt(Main.pref.get(PREF_TOLERANCEANGLE, Double.toString(ImageAnalyzer.DEFAULT_TOLERANCEANGLE)));
		}catch(Throwable th){}
		return toleranceAngle;
	}

	/**
	 * @param toleranceAngle the toleranceAngle to set
	 */
	public void setToleranceAngle(double toleranceAngle) {
		Main.pref.put(PREF_TOLERANCEANGLE, Double.toString(toleranceAngle));
		this.toleranceAngle = toleranceAngle;
	}

}
