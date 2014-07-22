/**
 * 
 */
package org.openstreetmap.josm.plugins.areaselector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;


/**
 * @author Paul Woelfel
 *
 */
public class AreaSelectorAction extends MapMode implements MouseListener  {
	
	protected Logger log=Logger.getLogger(AreaSelectorAction.class.getCanonicalName());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AreaSelectorAction(MapFrame mapFrame){
        super(
        		tr("Area Selection"), 
        		"tracer-sml",
        		tr("Select an area from an underlying image."),
        		Shortcut.registerShortcut("tools:areaselector", tr("Tools: {0}", tr("Area Selector")), KeyEvent.VK_A, Shortcut.ALT_CTRL), 
        		mapFrame,
        		getCursor()
        		);
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

	
	public void updateMapFrame(MapFrame oldFrame, MapFrame newFrame){
		// TODO something should change now?!?
		// or not, we just use Main to get the current mapFrame
	}
	
	
    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
	@Override
    public void mouseClicked(MouseEvent e) {
		
		log.info("mouse clicked "+e);
		
		if (!Main.map.mapView.isActiveLayerDrawable()) {
            return;
        }
        requestFocusInMapView();
        updateKeyModifiers(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            createArea(e.getPoint());

        }
	}

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
	@Override
    public void mousePressed(MouseEvent e) {
		log.info("mouse pressed "+e);
	}

    /**
     * Invoked when a mouse button has been released on a component.
     */
	@Override
    public void mouseReleased(MouseEvent e) {
		log.info("mouse released "+e);
	}

    /**
     * Invoked when the mouse enters a component.
     */
	@Override
    public void mouseEntered(MouseEvent e) {
		log.info("mouse entered" +e);
	}

    /**
     * Invoked when the mouse exits a component.
     */
	@Override
    public void mouseExited(MouseEvent e) {
		log.info("mouse exited "+e);
	}
	
	public void createArea(Point clickPoint){
		// TODO do something
		
		MapView mapView=Main.map.mapView;
//		Collection<Layer> layers=mapView.getAllLayers();
//		Layer activeLayer=mapView.getActiveLayer();
		
		BufferedImage bufImage=new BufferedImage(mapView.getWidth(), mapView.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D imgGraphics=bufImage.createGraphics();
		
		for (Layer layer : mapView.getAllLayers()){
			layer.paint(imgGraphics, mapView, mapView.getRealBounds());
		}
		
		
		// X marks the spot
		imgGraphics.setColor(Color.RED);
		imgGraphics.setFont(new Font("Arial", Font.BOLD, 12));
		imgGraphics.drawString("X", clickPoint.x, clickPoint.y);
		
		ImageIcon icon = new ImageIcon(bufImage);
		
		
		JOptionPane.showMessageDialog(Main.parent, "You clicked on "+clickPoint.x+" "+clickPoint.y, "AreaSelection", JOptionPane.INFORMATION_MESSAGE, icon);
	}

}
