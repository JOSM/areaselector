/*
 * Created on Jul 23, 2014
 * Author: Paul Woelfel
 * Email: paul@woelfel.at
 */
package org.openstreetmap.josm.plugins.areaselector;

import georegression.struct.line.LineSegment2D_F32;
import georegression.struct.point.Point2D_I32;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import marvin.image.MarvinColorModelConverter;
import marvin.image.MarvinImage;
import marvin.plugin.MarvinAbstractImagePlugin;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.osm.Way;

import boofcv.abst.feature.detect.line.DetectLineSegmentsGridRansac;
import boofcv.alg.feature.shapes.ShapeFittingOps;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.detect.line.FactoryDetectLineAlgs;
import boofcv.gui.feature.ImageLinePanel;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.struct.ConnectRule;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageUInt8;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 */
public class ImageAnalyzer {

	protected static Logger log = Logger.getLogger(ImageAnalyzer.class);

	protected BufferedImage baseImage,workImage;
	
	protected MarvinImage src, greyImage,workMarvin;

	public static final String IMG_TYPE="PNG";
	
	protected int cannyThreshold = 30;

	protected static final int cannyMin = 10, cannyMax = 200;

	protected double ratio = 3;

	protected static final int ratioMin = 100, ratioMax = 500;

	protected int colorThreshold = 15;
	
	protected static final int colorMin = 0, colorMax=50;
	
	
	// default is 40
	protected int regionSize=35;
	
	// edge default is 30, but not used in the detector
	// angle default is 2.38
	protected double thresholdEdge=30, thresholdAngle=1.1;
	
	protected List<LineSegment2D_F32> lines;
	
	
	// Polynomial fitting tolerances
	static double toleranceDist = 2;
	static double toleranceAngle= Math.PI/10;
	
	
	public ImageAnalyzer(String filename) {
		log.info("Loading from " + filename);
		baseImage = getImgFromFile(filename);
		init();
	}

	public ImageAnalyzer(BufferedImage bufImg) throws IOException {
		baseImage=bufImg;
		init();
	}

	protected void init() {
		src=new MarvinImage(baseImage);
		
		log.info("creating grey");
		greyImage=applyPlugin("org.marvinproject.image.color.grayScale", src);
		
	}

	public void initUI(Point point) {
		
		final Point colorPoint=point;
		
		
		
		final JFrame mainWindow = new JFrame("Image Analyzer");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainWindow.setLayout(new BorderLayout());
		getArea(colorPoint);

		
		final ImageLinePanel gui = new ImageLinePanel();
		gui.setBackground(workImage);
//		gui.setLineSegments(lines);
		gui.setPreferredSize(new Dimension(baseImage.getWidth(),baseImage.getHeight()));

		mainWindow.getContentPane().add(gui);

		JPanel textAreaPanel = new JPanel();
		
		final JTextArea colorThresholdTextArea=new JTextArea(1,5);
		colorThresholdTextArea.setText(""+colorThreshold);
		final JLabel colorLabel = new JLabel("Color Threshold: ");
		textAreaPanel.add(colorLabel);
		textAreaPanel.add(colorThresholdTextArea);
		
		final JTextArea regionSizeTextArea=new JTextArea(1,5);
		regionSizeTextArea.setText(""+regionSize);
		final JLabel regionSizeLabel = new JLabel("Region Size: ");
		textAreaPanel.add(regionSizeLabel);
		textAreaPanel.add(regionSizeTextArea);
		
		final JTextArea thresholdEdgeTextArea=new JTextArea(1,5);
		thresholdEdgeTextArea.setText(""+thresholdEdge);
		final JLabel thresholdEdgeLabel = new JLabel("Threshold Edge: ");
		textAreaPanel.add(thresholdEdgeLabel);
		textAreaPanel.add(thresholdEdgeTextArea);
		
		final JTextArea thresholdAngleTextArea=new JTextArea(1,5);
		thresholdAngleTextArea.setText(""+(thresholdAngle));
		final JLabel thresholdAngleLabel = new JLabel("Threshold Angle: ");
		textAreaPanel.add(thresholdAngleLabel);
		textAreaPanel.add(thresholdAngleTextArea);
		
		
		JButton refreshButton=new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				colorThreshold=Integer.parseInt(colorThresholdTextArea.getText());
				
				regionSize=Integer.parseInt(regionSizeTextArea.getText());
				
				thresholdEdge=Double.parseDouble(thresholdEdgeTextArea.getText());
				
				thresholdAngle=Double.parseDouble(thresholdAngleTextArea.getText());
				

				getArea(colorPoint);
				gui.setBackground(workImage);
//				gui.setLineSegments(lines);
				mainWindow.repaint();
			}
		});
		
		textAreaPanel.add(refreshButton);
		
		mainWindow.getContentPane().add(textAreaPanel, BorderLayout.NORTH);

		mainWindow.setVisible(true);
		mainWindow.setSize(1200, 800);
	}


	public Way getArea(Point point) {

		// get color at that point
		BufferedImage bufImg = src.getBufferedImage();

		Color pointColor = new Color(bufImg.getRGB(point.x, point.y));
		// orignal color at point is
		// r=236,g=202,b=201
		
		
		
		// fake the color
		// 150, 152, 199
		//pointColor=new Color(150,152,199);

		// let's create a threshold

		log.info("point color: " + pointColor);

		int r = pointColor.getRed(), g = pointColor.getGreen(), b = pointColor.getBlue();

		HashMap<String,Object> attributes=new HashMap<String,Object>();
		attributes.put("range", colorThreshold);
		attributes.put("r", r);
		attributes.put("g", g);
		attributes.put("b", b);
		
		log.info("Applying gaus filter");
		MarvinImage gaus=applyPlugin("org.marvinproject.image.blur.gaussianBlur", src);
		
		log.info("searching for the correct color");
		MarvinImage colorSelected=applyPlugin("org.marvinproject.image.color.selectColor", gaus, attributes);
//		ImgUtils.imshow("selected color",colorSelected);
//		saveImgToFile(colorSelected,"test/colorExtracted");
		
		log.info("trying Edge detection");
//		
		MarvinImage blackAndWhite=MarvinColorModelConverter.rgbToBinary(colorSelected, 127);
//		saveImgToFile(blackAndWhite,"test/blackAndWhite");
		
		
		
		
//		MarvinImage sobel=applyPlugin("org.marvinproject.image.edge.sobel", blackAndWhite);
//		saveImgToFile(sobel,"test/sobel");
		boolean [][] erosionMatrix = new boolean[][]
				{
					{true,true,true,true,true},
					{true,true,true,true,true},
					{true,true,true,true,true},
					{true,true,true,true,true},
					{true,true,true,true,true},
				};
		
		HashMap<String,Object> erosionAttributes=new HashMap<>();
		erosionAttributes.put("matrix", erosionMatrix);
		
		MarvinImage erosion=blackAndWhite;
		
//		for(int i =0 ; i < 10; i++){
			erosion=applyPlugin("org.marvinproject.image.morphological.erosion",erosion,erosionAttributes);
//		}
//		saveImgToFile(erosion,"test/erosion");
		
//		MarvinImage dilation = applyPlugin("org.marvinproject.image.morphological.dilation",erosion,erosionAttributes);
//		saveImgToFile(erosion,"test/dilation");
		
//		MarvinImage roberts=applyPlugin("org.marvinproject.image.edge.roberts", erosion);
//		saveImgToFile(roberts,"test/roberts");
		
//		MarvinImage prewitt=applyPlugin("org.marvinproject.image.edge.prewitt", colorSelected);
//		saveImgToFile(prewitt,"test/prewitt");
		
//		log.info("detecting boundaries");
//		MarvinImage inverted=applyPlugin("org.marvinproject.image.color.invert", colorSelected);
//		MarvinImage boundary=applyPlugin("org.marvinproject.image.morphological.boundary", roberts);
//		saveImgToFile(boundary,"test/boundary");
		
//		MarvinImage boundaryInverted=applyPlugin("org.marvinproject.image.color.invert",MarvinColorModelConverter.binaryToRgb(boundary));
//		saveImgToFile(boundaryInverted,"test/boundary_inverted");
		
		workMarvin=erosion;
		workMarvin.update();
		workImage=workMarvin.getBufferedImage();
		
//		lines=detectLines(workImage);
		
		detectAreas(workImage);
		
		
//		Mat canny= applyCanny(gaus);
//		
//		saveImgToFile(canny,"test/colorPlusCanny");
//		
//		detectLines(canny);
//		
////		ImgUtils.imshow("canny on InRange", canny);

		log.info("done.");

		return null;
	}
	
	
	public List<LineSegment2D_F32> detectLines(BufferedImage image){
		ImageFloat32 input = ConvertBufferedImage.convertFromSingle(image, null, ImageFloat32.class );

		// Comment/uncomment to try a different type of line detector
		DetectLineSegmentsGridRansac<ImageFloat32,ImageFloat32> detector = FactoryDetectLineAlgs.lineRansac(regionSize, thresholdEdge, thresholdAngle, true, ImageFloat32.class, ImageFloat32.class);

		List<LineSegment2D_F32> found = detector.detect(input);
		return found;
		// display the results
//		ImageLinePanel gui = new ImageLinePanel();
//		gui.setBackground(image);
//		gui.setLineSegments(found);
//		gui.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
//
//		ShowImages.showWindow(gui,"Found Line Segments");
	}
	
	
	public List<List<PointIndex_I32>> detectAreas(BufferedImage image){
		
		List <List<PointIndex_I32>> polygons=new ArrayList<List<PointIndex_I32>>();
		ImageFloat32 input = ConvertBufferedImage.convertFromSingle(image, null, ImageFloat32.class);
		ImageUInt8 binary = new ImageUInt8(input.width,input.height);
		BufferedImage polygon = new BufferedImage(input.width,input.height,BufferedImage.TYPE_INT_RGB);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(input);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(input, binary, (float) mean, true);

		// reduce noise with some filtering
		ImageUInt8 filtered = BinaryImageOps.erode8(binary, 1, null);
		filtered = BinaryImageOps.dilate8(filtered, 1, null);

		// Find the contour around the shapes
		List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT,null);

		// Fit a polygon to each shape and draw the results
		Graphics2D g2 = polygon.createGraphics();
		g2.setStroke(new BasicStroke(2));

		for( Contour c : contours ) {
			// Fit the polygon to the found external contour.  Note loop = true
			List<PointIndex_I32> vertexes = ShapeFittingOps.fitPolygon(c.external,true,
					toleranceDist,toleranceAngle,100);
			polygons.add(vertexes);

			g2.setColor(Color.RED);
			VisualizeShapes.drawPolygon(vertexes,true,g2);

			// handle internal contours now
			g2.setColor(Color.BLUE);
			for( List<Point2D_I32> internal : c.internal ) {
				vertexes = ShapeFittingOps.fitPolygon(internal,true,toleranceDist,toleranceAngle,100);
				polygons.add(vertexes);
				VisualizeShapes.drawPolygon(vertexes,true,g2);
			}
		}
		
		saveImgToFile(polygon, "test/polygon");
		
		workImage=polygon;
		
		return polygons;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
	
	public boolean saveImgToFile(MarvinImage buf,String filename){
		buf.update();
		return saveImgToFile(buf.getBufferedImage(),filename);
	}
	
	public boolean saveImgToFile(BufferedImage buf,String filename){
		try {
			ImageIO.write(buf, IMG_TYPE, new File(filename+"."+IMG_TYPE.toLowerCase()));
			return true;
		} catch (IOException e) {
			log.warn("unable to save image",e);
		}
		return false;
	}
	
	public BufferedImage getImgFromFile(String filename){
		try {
			return ImageIO.read(new File(filename+"."+IMG_TYPE.toLowerCase()));
		} catch (IOException e) {
			log.warn("unable to read file "+filename,e);
		}
		return null;
	}
	
	/**
	 * find out if marvin fits our needs.
	 */
	public void testMarvin(){
//		MarvinImage greyImg=applyPlugin("org.marvinproject.image.color.grayScale", src);
		
		//ImgUtils.imshow("Marvin Test", greyImg.getBufferedImage());
		
//		HashMap<String,Object> attributes=new HashMap<String,Object>();
//		attributes.put("range", colorThreshold);
//		attributes.put("r", 100);
//		MarvinImage colorSelected=applyPlugin("org.marvinproject.image.color.thresholdRange", src, attributes);
//		ImgUtils.imshow("selected color",colorSelected);
		
//		MarvinImage robertsImg=applyPlugin("org.marvinproject.image.edge.roberts.jar", greyImg);
//		
//		ImgUtils.imshow("Marvin Edge", robertsImg);
		
		
		MarvinImage mImg=new MarvinImage(getImgFromFile("test/boundary_in"));
		MarvinImage inverted=applyPlugin("org.marvinproject.image.color.invert", mImg);
		//ImgUtils.imshow("inverted", inverted.getBufferedImage());
		MarvinImage blackAndWhite=MarvinColorModelConverter.rgbToBinary(inverted, 127);
		MarvinImage boundary=applyPlugin("org.marvinproject.image.morphological.boundary", blackAndWhite);
		ImgUtils.imshow("boundary", boundary);
		
		
		
	}
	
	/**
	 * load the plugin, process it with that image and return it. The original image is not modified.
	 * @param pluginName the plugin jar name
	 * @param img image to processed
	 * @return the modified image
	 */
	public MarvinImage applyPlugin(String pluginName, MarvinImage img){
		return applyPlugin(pluginName, img,null);
	}
	
	/**
	 * load the plugin, process it with that image and return it. The original image is not modified.
	 * @param pluginName the plugin jar name
	 * @param img image to processed
	 * @param attributes attributes to set
	 * @return the modified image
	 */
	public MarvinImage applyPlugin(String pluginName, MarvinImage img,HashMap<String,Object> attributes){
		MarvinImage dest=img.clone();
		MarvinImagePlugin plugin=MarvinPluginLoader.loadImagePlugin(pluginName);
		
		if(attributes!=null){
			for(String key : attributes.keySet()){
				plugin.setAttribute(key, attributes.get(key));
			}
		}
		
		plugin.process(img, dest);
		
		// do not update every time, only when needed
		//dest.update();
		return dest;
	}
	
	/**
	 * create a instance of a marvin plugin
	 * @param pluginClassName full class name of the plugin
	 * @return instance of the plugin
	 */
	public MarvinImagePlugin getPlugin(String pluginClassName){
		MarvinImagePlugin plugin=null;
		
			try {
				Class<?> classObject=Class.forName(pluginClassName);
				Object classInstance=classObject.newInstance();
				
				if(classInstance instanceof MarvinImagePlugin){
					// this is a correct plugin
					plugin=(MarvinAbstractImagePlugin) classInstance;
					plugin.load();
				}else {
					log.error("The class "+pluginClassName+" is not a MarvinPlugin");
				}
				
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				log.error("could not load marvin plugin", e);
			}
		
		return plugin;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.DEBUG);

		// test/baseimage.png 419 308
		if (args.length < 3) {
			log.warn("Usage: ImageAnalyzer basefile x y");
		} else {
			Point point=new Point(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			ImageAnalyzer imgAnalyzer = new ImageAnalyzer(args[0]);
			imgAnalyzer.initUI(point);
//			imgAnalyzer.getArea(point);
			// Mat mat = imgAnalyzer.applyInRange();
			// ImgUtils.imshow("in range", mat);
			
//			imgAnalyzer.testMarvin();
		}

	}

}
