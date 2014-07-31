/*
 * Created on Jul 23, 2014
 * Author: Paul Woelfel
 * Email: paul@woelfel.at
 */
package org.openstreetmap.josm.plugins.areaselector;

import georegression.metric.UtilAngle;
import georegression.struct.point.Point2D_I32;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.enhance.EnhanceImageOps;
import boofcv.alg.feature.shapes.ShapeFittingOps;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.feature.ImageLinePanel;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.VisualizeImageData;
import boofcv.struct.ConnectRule;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 */
public class ImageAnalyzer {

	protected static Logger log = Logger.getLogger(ImageAnalyzer.class);
	
	public static final String IMG_TYPE="PNG";
	
	protected static int fileCount=0;

	protected BufferedImage baseImage,workImage;
	
	protected MarvinImage workMarvin;

	protected Point point;
	
	protected boolean debug=false;
	
	
	// Algorithm params
	
	public static final int DEFAULT_COLORTHRESHOLD= 15;
	protected int colorThreshold = DEFAULT_COLORTHRESHOLD;
	
	
	// Polynomial fitting tolerances
	public static final double DEFAULT_TOLERANCEDIST=3;
	double toleranceDist = DEFAULT_TOLERANCEDIST; // original: 2
	
	public static final double DEFAULT_TOLERANCEANGLE = Math.PI / 8;
	double toleranceAngle= DEFAULT_TOLERANCEANGLE; // original Math.PI/10
	
	// gaussian blur radius
	public static final int DEFAULT_BLURRADIUS = 10;
	protected int blurRadius = DEFAULT_BLURRADIUS;
	
	
	
	
	public ImageAnalyzer(String filename, Point point) {
		log.info("Loading from " + filename);
		baseImage = getImgFromFile(filename);
		this.point=point;
		init();
	}

	public ImageAnalyzer(BufferedImage bufImg, Point point){
		baseImage=bufImg;
		this.point=point;
		init();
	}

	protected void init() {
		if(debug) saveImgToFile(baseImage,"baseimage");
		
	}

	public void initUI() {
		
		final JFrame mainWindow = new JFrame("Image Analyzer");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainWindow.setLayout(new BorderLayout());
		getArea();

		
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
		
		
		final JTextArea toleranceDistTextArea=new JTextArea(1,5);
		toleranceDistTextArea.setText(""+toleranceDist);
		final JLabel toleranceDistLabel = new JLabel("Tolerance Dist: ");
		textAreaPanel.add(toleranceDistLabel);
		textAreaPanel.add(toleranceDistTextArea);
		
		final JTextArea toleranceAngleTextArea=new JTextArea(1,5);
		toleranceAngleTextArea.setText(""+(toleranceAngle));
		final JLabel toleranceAngleLabel = new JLabel("Tolerance Angle: ");
		textAreaPanel.add(toleranceAngleLabel);
		textAreaPanel.add(toleranceAngleTextArea);
		
		
		JButton refreshButton=new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				colorThreshold=Integer.parseInt(colorThresholdTextArea.getText());
				
				
				toleranceDist=Double.parseDouble(toleranceDistTextArea.getText());
				
				toleranceAngle=Double.parseDouble(toleranceAngleTextArea.getText());
				

				getArea();
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


	public Polygon getArea() {
		
		log.info("Using following params for algorithm: colorThreshold="+colorThreshold+" toleranceDist="+toleranceDist+" toleranceAngle="+toleranceAngle);

		// get color at that point
		workImage = deepCopy(baseImage);

		Color pointColor = new Color(workImage.getRGB(point.x, point.y));
		// 150, 152, 199
//		pointColor=new Color(150,152,199);
		
		log.info("point color: " + pointColor);
		
		
		workImage=selectMarvinColor(workImage,pointColor);
		if(debug) saveImgToFile(workImage, "colorExtracted");
		
//		workImage=histogram(workImage);
//		if(debug) saveImgToFile(workImage,"histogram");

//		workImage=sharpen(workImage);
//		if(debug) saveImgToFile(workImage,"sharpen");
//
//		workImage=binarize(workImage);
//		if(debug) saveImgToFile(workImage,"binarize");

		
//		workImage=erodeAndDilate(workImage);
//		if(debug) saveImgToFile(workImage,"erodeDilate");
		
//		workImage=gaussian(workImage);
//		if(debug) saveImgToFile(workImage, "test/gaus");
		
		
		

		
//		lines=detectLines(workImage);
		
		Polygon polygon=detectArea(workImage,point);
		


		log.info("done.");

		return polygon;
	}
	
	/**
	 * select a color from a image
	 * @param workImage image to work with
	 * @param pointColor color to select
	 * @return BufferedImage with selected color as white and the rest as black
	 */
	public BufferedImage selectMarvinColor(BufferedImage workImage, Color pointColor){

		log.info("extracting marvin color");

		int r = pointColor.getRed(), g = pointColor.getGreen(), b = pointColor.getBlue();

		HashMap<String,Object> attributes=new HashMap<String,Object>();
		attributes.put("range", colorThreshold);
		attributes.put("r", r);
		attributes.put("g", g);
		attributes.put("b", b);
		
		MarvinImage src=new MarvinImage(workImage);
		
		log.info("Applying gaus filter");
		MarvinImage gaus=applyPlugin("org.marvinproject.image.blur.gaussianBlur", src);
		if(debug) saveImgToFile(gaus,"gaussian");
		
		log.info("searching for the correct color");
		MarvinImage colorSelected=applyPlugin("org.marvinproject.image.color.selectColor", gaus, attributes);
//		ImgUtils.imshow("selected color",colorSelected);
		
		colorSelected.update();
		
		return colorSelected.getBufferedImage();
	}
	
	/**
	 * erode and dilate an image
	 * @param workImage image to transform
	 * @return transformed image
	 */
	public BufferedImage erodeAndDilate(BufferedImage workImage){
		log.info("Erode and Dilate");
		ImageFloat32 input = ConvertBufferedImage.convertFromSingle(workImage, null, ImageFloat32.class);
		ImageUInt8 binary = new ImageUInt8(input.width,input.height);
//		ImageSInt32 label = new ImageSInt32(input.width,input.height);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(input);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(input,binary,(float)mean,true);

		// remove small blobs through erosion and dilation
		// The null in the input indicates that it should internally declare the work image it needs
		// this is less efficient, but easier to code.
		ImageUInt8 filtered = BinaryImageOps.erode4(binary, 1, null);
		filtered = BinaryImageOps.dilate4(filtered, 1, null);
		
		return VisualizeBinaryData.renderBinary(filtered, null);
	}
	
	public BufferedImage binarize(BufferedImage workImage){
		log.info("Binarize");
		
		ImageFloat32 input = ConvertBufferedImage.convertFromSingle(workImage, null, ImageFloat32.class);
		ImageUInt8 binary = new ImageUInt8(input.width,input.height);
//		ImageSInt32 label = new ImageSInt32(input.width,input.height);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(input);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(input,binary,(float)mean,true);

		return VisualizeBinaryData.renderBinary(binary, null);
	}
	
	/**
	 * apply gaussian filter
	 * @param image image to filter
	 * @return filtered image
	 */
	public BufferedImage gaussian(BufferedImage image){
		log.info("gaussian filter");
		ImageFloat32 input=null;
		input = ConvertBufferedImage.convertFrom(image, (ImageFloat32)null);
		
		ImageFloat32 output=GeneralizedImageOps.createSingleBand(ImageFloat32.class, input.width, input.height);
		
//		BlurImageOps.gaussian(input, output, -1, blurRadius, null);
		GBlurImageOps.gaussian(input, output, -1, blurRadius, null);
		
		return VisualizeImageData.colorizeSign(output,null,-1);
//		return VisualizeImageData.colorizeGradient(derivX, derivY, maxAbsValue)
	}
	
	
	/**
	 * Selectively displays only pixels which have a similar hue and saturation values to what is provided.
	 * This is intended to be a simple example of color based segmentation.  Color based segmentation can be done
	 * in RGB color, but is more problematic due to it not being intensity invariant.  More robust techniques
	 * can use Gaussian models instead of a uniform distribution, as is done below.
	 * @return 
	 */
	public BufferedImage selectColor( BufferedImage image , Color rgbColor) {
		log.info("selecting color");
		float[] color = new float[3];
		ColorHsv.rgbToHsv(rgbColor.getRed(),rgbColor.getGreen(),rgbColor.getBlue(), color);


		log.info("HSV color: H = " + color[0]+" S = "+color[1]+" V = "+color[2]);
		float hue=color[0];
		float saturation=color[1];
		
		
		MultiSpectral<ImageFloat32> input = ConvertBufferedImage.convertFromMulti(image,null,true,ImageFloat32.class);
		MultiSpectral<ImageFloat32> hsv = new MultiSpectral<ImageFloat32>(ImageFloat32.class,input.width,input.height,3);

		// Convert into HSV
		ColorHsv.rgbToHsv_F32(input,hsv);

		// Euclidean distance squared threshold for deciding which pixels are members of the selected set
		float maxDist2 = 0.4f*0.4f;

		// Extract hue and saturation bands which are independent of intensity
		ImageFloat32 H = hsv.getBand(0);
		ImageFloat32 S = hsv.getBand(1);

		// Adjust the relative importance of Hue and Saturation.
		// Hue has a range of 0 to 2*PI and Saturation from 0 to 1.
		float adjustUnits = (float)(Math.PI/2.0);

		// step through each pixel and mark how close it is to the selected color
		BufferedImage output = new BufferedImage(input.width,input.height,BufferedImage.TYPE_INT_RGB);
		for( int y = 0; y < hsv.height; y++ ) {
			for( int x = 0; x < hsv.width; x++ ) {
				// Hue is an angle in radians, so simple subtraction doesn't work
				float dh = UtilAngle.dist(H.unsafe_get(x,y),hue);
				float ds = (S.unsafe_get(x,y)-saturation)*adjustUnits;

				// this distance measure is a bit naive, but good enough for to demonstrate the concept
				float dist2 = dh*dh + ds*ds;
				if( dist2 <= maxDist2 ) {
					output.setRGB(x,y,image.getRGB(x,y));
				}
			}
		}

		return output;
	}
	
	
	/**
	 * histogram adjustment
	 * @param image buffered image to adjust histogram
	 * @return adjusted image
	 */
	public BufferedImage histogram(BufferedImage image) {
		log.info("Histogram adjustment");
		
		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
		ImageUInt8 adjusted = new ImageUInt8(gray.width, gray.height);
//		if(debug) saveImgToFile(ConvertBufferedImage.convertTo(adjusted,null),"gray");
 
		int histogram[] = new int[256];
		int transform[] = new int[256];
 
 
		ImageStatistics.histogram(gray,histogram);
		EnhanceImageOps.equalize(histogram, transform);
		EnhanceImageOps.applyTransform(gray, transform, adjusted);

		if(debug) saveImgToFile(ConvertBufferedImage.convertTo(adjusted,null),"histogram_global");
 
//		EnhanceImageOps.equalizeLocal(gray, 50, adjusted, histogram, transform);
//
//		if(debug) saveImgToFile(ConvertBufferedImage.convertTo(adjusted,null),"histogram_local");
 
		return ConvertBufferedImage.convertTo(adjusted,null);
	}
 
	/**
	 * When an image is sharpened the intensity of edges are made more extreme while flat regions remain unchanged.
	 * @return 
	 */
	public BufferedImage sharpen(BufferedImage image) {	
		log.info("sharpen");
		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
		if(debug) saveImgToFile(ConvertBufferedImage.convertTo(gray,null),"gray");
		
		ImageUInt8 adjusted = new ImageUInt8(gray.width, gray.height);
 
		EnhanceImageOps.sharpen4(gray, adjusted);
		if(debug) saveImgToFile(ConvertBufferedImage.convertTo(adjusted,null),"sharpen4");
 
//		EnhanceImageOps.sharpen8(gray, adjusted);
//		if(debug) saveImgToFile(ConvertBufferedImage.convertTo(adjusted,null),"sharpen8");
 
		return ConvertBufferedImage.convertTo(adjusted,null);
	}
 
	
	
	/**
	 * detect a Polygon around a point
	 * @param image Image to analyze for polygons
	 * @param point point to search the polygon
	 * @return Polygon if found
	 */
	public Polygon detectArea(BufferedImage image,Point point){
		
		List <Polygon> polygons=new ArrayList<Polygon>();
		ImageFloat32 input = ConvertBufferedImage.convertFromSingle(image, null, ImageFloat32.class);
		ImageUInt8 binary = new ImageUInt8(input.width,input.height);
		BufferedImage polygonImage = new BufferedImage(input.width,input.height,BufferedImage.TYPE_INT_RGB);

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
		Graphics2D g2 = polygonImage.createGraphics();
		g2.setStroke(new BasicStroke(2));

		for( Contour c : contours ) {
			// Fit the polygon to the found external contour.  Note loop = true
			List<PointIndex_I32> vertexes = ShapeFittingOps.fitPolygon(c.external,true,
					toleranceDist,toleranceAngle,100);
			
			Polygon poly=toPolygon(vertexes);
			if(poly.contains(point)){
				
				
				polygons.add(poly);
	
				g2.setColor(Color.RED);
				VisualizeShapes.drawPolygon(vertexes,true,g2);
	
				// handle internal contours now
				g2.setColor(Color.BLUE);
				for( List<Point2D_I32> internal : c.internal ) {
					vertexes = ShapeFittingOps.fitPolygon(internal,true,toleranceDist,toleranceAngle,100);
					poly=toPolygon(vertexes);
					if(poly.contains(point)){
						polygons.add(poly);
						VisualizeShapes.drawPolygon(vertexes,true,g2);
					}
				}
			}
		}
		
		if(debug) saveImgToFile(polygonImage, "polygons");
		
		log.info("Found "+polygons.size()+" matching polygons");
		
		
		Polygon innerPolygon=null;
		// let's see if we have outer polygons
		// if so, remove them, we only want inner shapes
		for(Polygon p: polygons){
			if(innerPolygon==null||innerPolygon.getBounds().contains(p.getBounds())){
				// found a inner polygon which contains the point
				innerPolygon=p;
			}
		}
		if(innerPolygon!=null){
			
		
			log.info("Best matching polygon is: "+polygonToString(innerPolygon));
		
		
			polygonImage=deepCopy(baseImage);
			g2=polygonImage.createGraphics();
//			g2.setColor(Color.WHITE);
//			g2.fillRect(0, 0, polygonImage.getWidth(), polygonImage.getHeight());
			
			g2.setColor(Color.RED);
			g2.setStroke(new BasicStroke(2));
			g2.drawPolygon(innerPolygon);
			
			if(debug) saveImgToFile(polygonImage,"polygon");
			
			
		}
		
		workImage=polygonImage;
		
		
		
		return innerPolygon;
	}
	
	public Polygon toPolygon(List<PointIndex_I32> points){
		int npoints=points.size();
		int [] xpoints = new int[npoints], ypoints = new int [npoints];
		int i=0;
		for(PointIndex_I32 point : points){
			xpoints[i]=point.x;
			ypoints[i]=point.y;
			i++;
		}
		
		return new Polygon(xpoints,ypoints,npoints);
	}
	
	/**
	 * Return the String info of a Polygon
	 * @param p Polygon to return as String
	 * @return 
	 */
	public static String polygonToString(Polygon p){
		if(p==null)  return "";
		StringBuilder sb=new StringBuilder();
		sb.append("Polygon (");
		sb.append(p.npoints);
		sb.append(" points) [");
		for (int i=0;i<p.npoints;i++){
			sb.append(" (");
			sb.append(p.xpoints[i]);
			sb.append(",");
			sb.append(p.ypoints[i]);
			sb.append(")");
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	/**
	 * make a deep copy of buffered image
	 * @param bi buffered image
	 * @return copy of the original
	 */
	static BufferedImage deepCopy(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
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
			ImageIO.write(buf, IMG_TYPE, new File("test/"+(fileCount<10?"0":"")+(fileCount++)+"_"+filename+"."+IMG_TYPE.toLowerCase()));
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
	 * @return the colorThreshold
	 */
	public int getColorThreshold() {
		return colorThreshold;
	}

	/**
	 * @param colorThreshold the colorThreshold to set
	 */
	public void setColorThreshold(int colorThreshold) {
		this.colorThreshold = colorThreshold;
	}

	/**
	 * @return the toleranceDist
	 */
	public double getToleranceDist() {
		return toleranceDist;
	}

	/**
	 * @param toleranceDist the toleranceDist to set
	 */
	public void setToleranceDist(double toleranceDist) {
		this.toleranceDist = toleranceDist;
	}

	/**
	 * @return the toleranceAngle
	 */
	public double getToleranceAngle() {
		return toleranceAngle;
	}

	/**
	 * @param toleranceAngle the toleranceAngle to set
	 */
	public void setToleranceAngle(double toleranceAngle) {
		this.toleranceAngle = toleranceAngle;
	}

	/**
	 * @return the blurRadius
	 */
	public int getBlurRadius() {
		return blurRadius;
	}

	/**
	 * @param blurRadius the blurRadius to set
	 */
	public void setBlurRadius(int blurRadius) {
		this.blurRadius = blurRadius;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConsoleAppender console = new ConsoleAppender(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c:%L: %m %x%n"),
				ConsoleAppender.SYSTEM_OUT);

		// BasicConfigurator.configure(console);
		Logger.getRootLogger().addAppender(console);

		Logger.getRootLogger().setLevel(Level.DEBUG);
		

		// test/baseimage.png 419 308
		if (args.length < 3) {
			log.warn("Usage: ImageAnalyzer basefile x y");
		} else {
			Point point=new Point(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			ImageAnalyzer imgAnalyzer = new ImageAnalyzer(args[0],point);
			imgAnalyzer.initUI();

			Polygon polygon=imgAnalyzer.getArea();
			log.info("got polygon "+polygonToString(polygon));

		}

	}

}
