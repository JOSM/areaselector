/**
 * 
 */
package org.openstreetmap.josm.plugins.areaselector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;


/**
 * @author ignacio_palermo
 *
 */
public class AreaSelectorAction extends MapMode implements MouseListener  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AreaSelectorAction(MapFrame mapFrame){
        super(
        		tr("OSM Export"), 
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


	
	public void updateMapFrame(MapFrame oldFrame, MapFrame newFrame){
		// TODO something should change now?!?
		
	}
	
	
    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
	@Override
    public void mouseClicked(MouseEvent e) {
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
	}

    /**
     * Invoked when a mouse button has been released on a component.
     */
	@Override
    public void mouseReleased(MouseEvent e) {
	}

    /**
     * Invoked when the mouse enters a component.
     */
	@Override
    public void mouseEntered(MouseEvent e) {
	}

    /**
     * Invoked when the mouse exits a component.
     */
	@Override
    public void mouseExited(MouseEvent e) {
	}
	
	public void createArea(Point clickPoint){
    	// TODO do something
		JOptionPane.showMessageDialog(Main.parent, "You clicked on "+clickPoint.x+" "+clickPoint.y);
	}

}
