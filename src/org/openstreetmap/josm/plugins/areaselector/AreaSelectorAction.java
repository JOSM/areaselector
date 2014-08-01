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

import javax.swing.JPanel;

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
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
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
	
	protected ImageryLayer background;
	
	protected MapView backgroundView;
	protected JPanel backgroundPanel;
	
	
	public static final double MIN_OPACITY=0.5;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AreaSelectorAction(MapFrame mapFrame) {
		super(tr("Area Selection"), "areaselector", tr("Select an area i.e. building from an underlying image."), Shortcut.registerShortcut("tools:areaselector",
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
	
	
	/**
	 * create a image from all background layers
	 * @return
	 */
	public BufferedImage getLayeredImage(){
		MapView mapView = Main.map.mapView;
		// Collection<Layer> layers=mapView.getAllLayers();
		// Layer activeLayer=mapView.getActiveLayer();

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
	
	/**
	 * try to get the background image from the most upper background.<br>
	 * If no layer which works is found, all layers are imaged.
	 * @return background image to analyze
	 */
	public BufferedImage getOptimizedImage(){
		log.info("Searching for optimized image");
		BufferedImage bgImage=null;
		
		MapView mapView = Main.map.mapView;
		// Collection<Layer> layers=mapView.getAllLayers();
		// Layer activeLayer=mapView.getActiveLayer();

//		Layer[] layers=mapView.getAllLayers().toArray(new Layer[0]);
//		
//		for (int i=layers.length-1;i>=0;i--) {
//			Layer layer=layers[i];
//			if(layer.isVisible() && layer.isBackgroundLayer()&&layer.getOpacity()>MIN_OPACITY && layer instanceof ImageryLayer){
//				// found a layer which is visible and and imagery background layer
//				ImageryLayer bLayer=(ImageryLayer)layer;
//				ImageryInfo info=bLayer.getInfo();
//				
//				if(background==null || !info.equalsBaseValues(background.getInfo())){
////					try {
//						log.info("found layer! "+info.getName());
////						backgroundPanel=new JPanel();
////						backgroundPanel.setSize(Main.map.mapView.getSize());
////						backgroundView=new MapView(backgroundPanel, null);
//						
//						if(layer instanceof TMSLayer){
//							TMSLayer tms=(TMSLayer)layer;
//							log.info("Current zoom level:"+tms.currentZoomLevel
//									+" max zoom: "+bLayer.getInfo().getMinZoom());
//							
//							
//						}
//						//min zoom is 0, which is way to zoomed
////						mapView.zoomTo(Main.map.mapView.getEastNorth(clickPoint.x, clickPoint.y), bLayer.getInfo().getMinZoom());
//						
//						
//						
////						if(layer instanceof TMSLayer){
////							background= new ZoomedTMSLayer(bLayer.getInfo(),backgroundView);
////							
////						}else {
////							background=bLayer.getClass().getConstructor(ImageryInfo.class).newInstance(info);
////						}
////						
//						break;
////					} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
////						log.warn("could not create Background Layer from existing layer",e);
////					}
//				}
//				
//			}
//		}
		
//		if(background!=null){
//			log.info("zooming in");
//			backgroundPanel.setSize(Main.map.mapView.getSize());
//			backgroundView.setSize(Main.map.mapView.getSize());
//			
//			// zoom to corect position
//			int maxZoom=background.getInfo().getMaxZoom();
//			backgroundView.zoomTo(Main.map.mapView.getEastNorth(clickPoint.x, clickPoint.y), maxZoom);
//			
//						
//			if(background instanceof ZoomedTMSLayer){
//				ZoomedTMSLayer tms=(ZoomedTMSLayer) background;
//				tms.setZoomLevel(maxZoom);
//				// TODO wait for all tiles to be loaded. currently no tiles will be loaded
//				log.info("loading tiles");
//				tms.loadAllTiles(true);
//				
//			}
//			
//			
//			Composite translucent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) background.getOpacity());
//			bgImage = new BufferedImage(backgroundView.getWidth(), backgroundView.getHeight(), BufferedImage.TYPE_INT_ARGB);
//			Graphics2D imgGraphics = bgImage.createGraphics();
//			imgGraphics.setComposite(translucent);
//			background.paint(imgGraphics, mapView, mapView.getRealBounds());
//		}
		
		mapView.zoomTo(Main.map.mapView.getEastNorth(clickPoint.x, clickPoint.y), 0.2);
		
		
		// if no optimized image could be produced, use all layers
		if(bgImage==null){
			bgImage=getLayeredImage();
		}
		return bgImage;
	}
	

	/**
	 * search for the polygon, where the mouse clicked
	 */
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

			Command c = new SequenceCommand(tr("Created area"), cmds);
			Main.main.undoRedo.add(c);
			Main.main.getCurrentDataSet().setSelected(way);
			
			// TODO not sure if x,y is the correct coordindate, it's just a point in the polygon
//			ConnectWays.connect(way, mapView.getLatLon(clickPoint.x, clickPoint.y));
			
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
