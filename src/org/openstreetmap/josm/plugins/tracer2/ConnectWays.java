/**
 *  Tracer2 - plug-in for JOSM to capture contours
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openstreetmap.josm.plugins.tracer2;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.Pair;

public class ConnectWays {
    static double s_dMinDistance		= 0.000006;		// Minimal distance, for objects
    static double s_dMinDistanceN2N		= 0.0000005;	// Minimal distance, when nodes are merged
    static double s_dMinDistanceN2oW	= 0.000001;		// Minimal distance, when node is connected to other way
    static double s_dMinDistanceN2tW	= 0.000001;		// Minimal distance, when other node is connected this way
    final static double MAX_ANGLE	= 30;				// Minimal angle, when other node is connected this way
    
    static Way s_oWay;
    static Way s_oWayOld;
    static List<Way> s_oWays;
    static List<Node> s_oNodes;
    
//    static ServerParam s_oParam;
//    static boolean s_bCtrl;
//    static boolean s_bAlt;
//    
    static boolean s_bAddNewWay;
    
    public static final String BUILDING_TAG="building";
    
    static ImageryLayer background;
    
    protected static Logger log=Logger.getLogger(ConnectWays.class);
    
    
    
    protected static ImageryLayer getBackground(){
    	for (Layer layer : Main.map.mapView.getAllLayers()){
    		if(layer.isBackgroundLayer()&&layer.isVisible()&& layer.getOpacity()>0.6f){
    			if(layer instanceof ImageryLayer){
    			background = (ImageryLayer) layer;
    			break;
    			}
    		}
    	}
    	
    	if(background==null){
    		for (Layer layer : Main.map.mapView.getAllLayers()){
    			if(layer instanceof ImageryLayer){
        			background = (ImageryLayer) layer;
        			break;
        		}
        	}
    	}
    	return background;
    }
    
    private static void calcDistance()
    {
    	
    	// TODO what is the correct tilesize and Resolution?!?
    	double dTileSize = 0.0004;
    	double dResolution = 2048;
    	
    	if(background!=null && background instanceof ImageryLayer){
    		ImageryLayer imgLayer=(ImageryLayer)background;
    		ImageryInfo info=imgLayer.getInfo();
    		dResolution=info.getPixelPerDegree();
    		log.info("dResolution "+dResolution);
    		// TODO get tile size !??
    	}
    	
    	double dMin = dTileSize / dResolution;
    	
    	s_dMinDistance = dMin * 30;
    	s_dMinDistanceN2N = dMin * 2.5;
    	s_dMinDistanceN2oW = dMin * 5;
    	s_dMinDistanceN2tW = dMin * 5;
    }
    
    private static void getWays(Way way) {
    	BBox bbox = new BBox(way);
    	bbox.addPrimitive(way,s_dMinDistance);
        s_oWays = Main.main.getCurrentDataSet().searchWays(bbox);
    }
    
    private static List<Way> getWaysOfNode(Node node) {
    	List<Way> ways;
    	ways = OsmPrimitive.getFilteredList(node.getReferrers(), Way.class);
    	return ways;
    }
    
    private static void getNodes(Way way) {
    	BBox bbox = new BBox(way);
    	bbox.addPrimitive(way,s_dMinDistance);
    	s_oNodes = Main.main.getCurrentDataSet().searchNodes(bbox);
    }
    
    private static double calcAlpha(LatLon oP1, Node n) {
    	LatLon oP2 = n.getCoor();
    	
    	double dAlpha = Math.atan((oP2.getY() - oP1.getY()) / (oP2.getX() - oP1.getX())) * 180 / Math.PI + (oP1.getX() > oP2.getX() ? 180 : 0);
    	return checkAlpha(dAlpha);
    }
    
    private static Double checkAlpha(Double dAlpha) {
        if (dAlpha > 180) {
            return dAlpha - 360;
        }
        if (dAlpha <= -180) {
            return dAlpha + 360;
        }
        return dAlpha;
    }
    
    private static boolean isNodeInsideWay(LatLon pos, Way way) {
    	List<Node> listNode = way.getNodes();
    	
    	double dAlpha;
    	double dAlphaOld = calcAlpha(pos, listNode.get(listNode.size()-1));
    	double dSumAlpha = 0;
    	
    	for (Node n : listNode) {
    		dAlpha = calcAlpha(pos, n);
    		dSumAlpha += checkAlpha( dAlpha - dAlphaOld );
    		dAlphaOld = dAlpha;
    	}
    	dSumAlpha = Math.abs(dSumAlpha);
    	
    	return dSumAlpha > 359 && dSumAlpha < 361;
    }
    
    private static Way getOldWay(LatLon pos) {
    	int i;
    	
    	for (i = 0; i < s_oWays.size(); i++) {
    		Way way = s_oWays.get(i);
    		if (!isSameTag(way)) {
    			continue;
    		}
    		if (isNodeInsideWay(pos, way)) {
    			s_oWays.remove(way);
    			return way;
    		}
    	}
    	return null;
    }
    
    /**
     * Try connect way to other buildings.
     * @param way Way to connect.
     * @return Commands.
     */
    public static Command connect(Way newWay, LatLon pos) {
        LinkedList<Command> cmds = new LinkedList<Command>();
        LinkedList<Command> cmds2 = new LinkedList<Command>();
        
//        s_oParam = param;
//        s_bCtrl = ctrl;
//        s_bAlt = alt;
        
        getBackground();
        calcDistance();
        getNodes(newWay);
        getWays(newWay);
        
        
        s_oWayOld = getOldWay(pos);
        
        if (s_oWayOld == null) {
        	s_bAddNewWay = true;
        	cmds.add(new AddCommand(newWay));
        	s_oWayOld = newWay;
        	s_oWay = new Way( newWay );
        } else {
            int i;
            Way tempWay;
            s_bAddNewWay = false;
            
            //Main.main.getCurrentDataSet().setSelected(m_wayOld);
            
            tempWay = new Way(s_oWayOld);
            
            for (i = 0; i < newWay.getNodesCount(); i++) {
            	tempWay.addNode(tempWay.getNodesCount(), newWay.getNode(i));
            }
            i++;
            for (i = 0; i < s_oWayOld.getNodesCount() - 1; i++) {
            	tempWay.removeNode( s_oWayOld.getNode(i) );
            }
            //cmds.add(new ChangeCommand(m_wayOld, tempWay));
            for (i = 0; i < s_oWayOld.getNodesCount() - 1; i++) {
            	Node n = s_oWayOld.getNode(i);
            	List<Way> ways = getWaysOfNode(n);
            	if (ways.size()<=1) {
                	cmds2.add(new DeleteCommand( s_oWayOld.getNode(i) ));
            	}
            	s_oNodes.remove(s_oWayOld.getNode(i));
            }
            s_oWay = tempWay;
        }
        
        {
        	cmds2.addAll(connectTo());

        	cmds.add(new ChangeCommand(s_oWayOld, trySplitWayByAnyNodes(s_oWay)));
        }
        cmds.addAll(cmds2);
        
//        TracerDebug oTracerDebug = new TracerDebug();
//        oTracerDebug.OutputCommands(cmds);
        
        Command cmd = new SequenceCommand(tr("Merge objects nodes"), cmds);
        
        return cmd;
    }
    
    
    /**
     * Try connect way to other buildings.
     * @param way Way to connect.
     * @return Commands.
     */
    public static List<Command> connectTo() {
        Map<Way, Way> modifiedWays = new HashMap<Way, Way>();
        LinkedList<Command> cmds = new LinkedList<Command>();
        Way way = new Way(s_oWay);
        for (int i = 0; i < way.getNodesCount() - 1; i++) {
            Node n = way.getNode(i);
            System.out.println("-------");
            System.out.println("Node: " + n);
            LatLon ll = n.getCoor();
            //BBox bbox = new BBox(
            //        ll.getX() - MIN_DISTANCE,
            //        ll.getY() - MIN_DISTANCE,
            //        ll.getX() + MIN_DISTANCE,
            //        ll.getY() + MIN_DISTANCE);

            // bude se node slucovat s jinym?
            double minDistanceSq = s_dMinDistanceN2N;
            //List<Node> nodes = Main.main.getCurrentDataSet().searchNodes(bbox);
            Node nearestNode = null;
            for (Node nn : s_oNodes) {
            	System.out.println("Node: " + nn);
                if (!nn.isUsable() || way.containsNode(nn) || s_oWay.containsNode(nn) || !isInSameTag(nn)) {
                    continue;
                }
                double dist = nn.getCoor().distance(ll);
                if (dist < minDistanceSq) {
                    minDistanceSq = dist;
                    nearestNode = nn;
                }
            }
            
            System.out.println("Nearest: " + nearestNode + " distance: " + minDistanceSq);
            if (nearestNode == null) {
                tryConnectNodeToAnyWay(n, modifiedWays);
            } else {
                System.out.println("+add Node distance: " + minDistanceSq);
                cmds.addAll(mergeNodes(n, nearestNode));
            }
        }
        
        for (Map.Entry<Way, Way> e : modifiedWays.entrySet()) {
            cmds.add(new ChangeCommand(e.getKey(), e.getValue()));
        }
        
        //cmds.addFirst(new ChangeCommand(way, trySplitWayByAnyNodes(newWay)));
        
        List<Command> cmd = cmds;
        return cmd;
    }

    /**
     * Merges two nodes
     * @param n1 First node
     * @param n2 Second node
     * @param way Way containing first node
     * @return List of Commands.
     */
    private static List<Command> mergeNodes(Node n1, Node n2){
        List<Command> cmds = new LinkedList<Command>();
        cmds.add(new MoveCommand(n2,
                 (n1.getEastNorth().getX() - n2.getEastNorth().getX())/2,
                 (n1.getEastNorth().getY() - n2.getEastNorth().getY())/2
                 ));
        
        Way newWay = new Way(s_oWay);
        
        int j = s_oWay.getNodes().indexOf(n1);
        newWay.addNode(j, n2);
        if (j == 0) {
            // first + last point
        	newWay.addNode(newWay.getNodesCount(), n2);
        }
        
        newWay.removeNode(n1);
 //       cmds.add(new ChangeCommand(m_way, newWay));
        s_oWay = new Way(newWay);
        
        cmds.add(new DeleteCommand(n1));
        return cmds;
    }

    /**
     * Try connect node "node" to way of other building.
     *
     * Zkusi zjistit, zda node neni tak blizko nejake usecky existujici budovy,
     * ze by mel byt zacnenen do teto usecky. Pokud ano, provede to.
     *
     * @param node Node to connect.
     * @throws IllegalStateException
     * @throws IndexOutOfBoundsException
     * @return List of Commands.
     */
	private static void tryConnectNodeToAnyWay(Node node, Map<Way, Way> m)
            throws IllegalStateException, IndexOutOfBoundsException {

        //List<Command> cmds = new LinkedList<Command>();

        LatLon ll = node.getCoor();
        //BBox bbox = new BBox(
        //        ll.getX() - MIN_DISTANCE_TW,
        //        ll.getY() - MIN_DISTANCE_TW,
        //        ll.getX() + MIN_DISTANCE_TW,
        //        ll.getY() + MIN_DISTANCE_TW);

        // node nebyl slouceny s jinym
        // hledani pripadne blizke usecky, kam bod pridat
        //List<Way> ways = Main.main.getCurrentDataSet().searchWays(bbox);
        double minDist = Double.MAX_VALUE;
        Way nearestWay = null;
        int nearestNodeIndex = 0;
        for (Way ww : s_oWays) {
        	System.out.println("Way: " + ww);
            if (!ww.isUsable() || ww.containsNode(node) || !isSameTag(ww)) {
                continue;
            }

            if (m.get(ww) != null) {
                ww = m.get(ww);
            }

            for (Pair<Node, Node> np : ww.getNodePairs(false)) {
                //double dist1 = TracerGeometry.distanceFromSegment(ll, np.a.getCoor(), np.b.getCoor());
                double dist = distanceFromSegment2(ll, np.a.getCoor(), np.b.getCoor());
                //System.out.println(" distance: " + dist1 + "  " + dist);
                
                if (dist < minDist) {
                    minDist = dist;
                    nearestWay = ww;
                    nearestNodeIndex = ww.getNodes().indexOf(np.a);
                }
            }
        }
        System.out.println("Nearest way: " + nearestWay + " distance: " + minDist);
        if (minDist < s_dMinDistanceN2oW) {
            Way newNWay = new Way(nearestWay);

            newNWay.addNode(nearestNodeIndex + 1, node);
            System.out.println("New way:" + newNWay);
            System.out.println("+add WayOld.Node distance: " + minDist);
            m.put(nearestWay, newNWay);
            s_oWays.remove(newNWay);
            s_oWays.add(nearestWay);
        }
    }

    private static double distanceFromSegment2(LatLon c, LatLon a, LatLon b) {
    	double x;
    	double y;
    	
        StraightLine oStraightLine1 = new StraightLine(
        		new Point2D.Double(a.getX(),a.getY()),
        		new Point2D.Double(b.getX(),b.getY())); 
        StraightLine oStraightLine2 = new StraightLine(
        		new Point2D.Double(c.getX(),c.getY()),
        		new Point2D.Double(c.getX() + (a.getY()-b.getY()),c.getY() - (a.getX()-b.getX())));
        Point2D.Double oPoint = oStraightLine1.GetIntersectionPoint(oStraightLine2);
    	
        if ((oPoint.x > a.getX() && oPoint.x > b.getX()) || (oPoint.x < a.getX() && oPoint.x < b.getX()) || 
        		(oPoint.y > a.getY() && oPoint.y > b.getY()) || (oPoint.y < a.getY() && oPoint.y < b.getY())) {
        	return 100000;
        }
        
        x=c.getX()-oPoint.getX();
        y=c.getY()-oPoint.getY();
        
        return Math.sqrt((x*x)+(y*y));
    }
    
    /**
     * Try split way by any existing building nodes.
     *
     * Zkusi zjistit zda nejake usecka z way by nemela prochazet nejakym existujicim bodem,
     * ktery je ji velmi blizko. Pokud ano, tak puvodni usecku rozdeli na dve tak, aby
     * prochazela takovym bodem.
     *
     * @param way Way to split.
     * @throws IndexOutOfBoundsException
     * @throws IllegalStateException
     * @return Modified way
     */
    private static Way trySplitWayByAnyNodes(Way way)
            throws IndexOutOfBoundsException, IllegalStateException {

        // projdi kazdou novou usecku a zjisti, zda by nemela vest pres existujici body
        int i = 0;
        while (i < way.getNodesCount()) {
            // usecka n1, n2
            LatLon n1 = way.getNodes().get(i).getCoor();
            LatLon n2 = way.getNodes().get((i + 1) % way.getNodesCount()).getCoor();
            System.out.println(way.getNodes().get(i) + "-----" + way.getNodes().get((i + 1) % way.getNodesCount()));
            double minDistanceSq = Double.MAX_VALUE;
//            double maxAngle = MAX_ANGLE;
            //List<Node> nodes = Main.main.getCurrentDataSet().searchNodes(new BBox(
            //    Math.min(n1.getX(), n2.getX()) - minDistanceSq,
            //    Math.min(n1.getY(), n2.getY()) - minDistanceSq,
            //    Math.max(n1.getX(), n2.getX()) + minDistanceSq,
            //    Math.max(n1.getY(), n2.getY()) + minDistanceSq
            //));

            Node nearestNode = null;
            for (Node nod : s_oNodes) {
                if (!nod.isUsable() || way.containsNode(nod) || !isInSameTag(nod)) {
                    continue;
                }
                LatLon nn = nod.getCoor();
                //double dist = TracerGeometry.distanceFromSegment(nn, n1, n2);
                double dist = distanceFromSegment2(nn, n1, n2);
//                double angle = TracerGeometry.angleOfLines(n1, nn, nn, n2);
                //System.out.println("Angle: " + angle + " distance: " + dist + " Node: " + nod);
                if (!n1.equalsEpsilon(nn) && !n2.equalsEpsilon(nn) && dist < minDistanceSq){ // && Math.abs(angle) < maxAngle) {
                	minDistanceSq = dist;
//                	maxAngle = angle;
                    nearestNode = nod;
                }
            }
            System.out.println("Nearest_: " + nearestNode + " distance: " + minDistanceSq);
            if (nearestNode == null || minDistanceSq >= s_dMinDistanceN2tW) {
                // tato usecka se nerozdeli
                i++;
                System.out.println("");
                continue;
            } else {
                // rozdeleni usecky
                way.addNode(i + 1, nearestNode);
                i++;
                System.out.println("+add Way.Node distance: " + minDistanceSq);
                System.out.println("");
                //i++;
                continue; // i nezvetsuji, treba bude treba rozdelit usecku znovu
            }
        }
        return way;
    }

    private static boolean isInSameTag(Node n) {
        for (OsmPrimitive op : n.getReferrers()) {
            if (op instanceof Way) {
                if (isSameTag((Way) op)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Determines if the specified primitive denotes a building.
     * @param p The primitive to be tested
     * @return True if building key is set and different from no,entrance
     */
    protected static final boolean isSameTag(OsmPrimitive p) {
        String v = p.get(BUILDING_TAG);
        
        return v != null && !v.equals("no");
    }

}
