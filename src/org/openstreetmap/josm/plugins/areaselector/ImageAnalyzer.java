/*
 * Created on Jul 23, 2014
 * Author: Paul Woelfel
 * Email: paul@woelfel.at
 */
package org.openstreetmap.josm.plugins.areaselector;

import static org.bytedeco.javacpp.opencv_core.cvReleaseImage;
import static org.bytedeco.javacpp.opencv_core.inRange;
import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.Canny;
import static org.bytedeco.javacpp.opencv_imgproc.GaussianBlur;
import static org.bytedeco.javacpp.opencv_imgproc.HoughLinesP;
import static org.bytedeco.javacpp.opencv_imgproc.blur;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import marvin.image.MarvinImage;
import marvin.plugin.MarvinAbstractImagePlugin;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.openstreetmap.josm.data.osm.Way;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 */
public class ImageAnalyzer {

	protected static Logger log = Logger.getLogger(ImageAnalyzer.class);

	protected IplImage cvImg;

	protected Mat src, grey;
	
	protected BufferedImage baseImage;

	public static final String IMG_TYPE="PNG";
	
	protected static final String tempFile = "cvload."+IMG_TYPE.toLowerCase();

	protected int cannyThreshold = 30;

	protected static final int cannyMin = 10, cannyMax = 200;

	protected double ratio = 3;

	protected static final int ratioMin = 100, ratioMax = 500;

	protected int colorThreshold = 15;
	
	protected static final int colorMin = 0, colorMax=50;
	
	
	public ImageAnalyzer(String filename) {
		log.info("Loading from " + filename);
		cvImg = cvLoadImage(filename);
		baseImage=cvImg.getBufferedImage();
		init();
	}

	public ImageAnalyzer(BufferedImage bufImg) throws IOException {
		baseImage=bufImg;
		ImageIO.write(bufImg, IMG_TYPE, new File(tempFile));
		cvImg = cvLoadImage(tempFile);
		new File(tempFile).delete();
		init();
	}

	protected void init() {
		if (cvImg == null) {
			log.warn("cvIMG is null!!");
		}

		log.info("createing mat");

		// ImgUtils.imshow("cvImg",cvImg);
		src = new Mat(cvImg);

		log.info("creating grey");
		grey = src.clone();
		cvtColor(src, grey, CV_BGR2GRAY);
	}

	public void initUI(Point point) {
		
		final Point colorPoint=point;
		
		final JFrame mainWindow = new JFrame("Image Analyzer");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainWindow.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		Dimension size = new Dimension(cvImg.width(), cvImg.height());
		panel.setPreferredSize(size);
		panel.setSize(size);
		getArea(colorPoint);

		final ImageIcon icon = new ImageIcon(getImgFromFile("test/colorPlusCanny"));

		JLabel label = new JLabel();

		label.setIcon(icon);
		panel.add(label);
		// mainWindow.getContentPane().removeAll();
		mainWindow.getContentPane().add(panel);

		JPanel sliderPanel = new JPanel();
		
		final JSlider colorThresholdSlider=new JSlider(colorMin,colorMax);
		colorThresholdSlider.setValue(colorThreshold);
		final JLabel colorLabel = new JLabel("Color Threshold: "+colorThreshold);
		sliderPanel.add(colorLabel);
		sliderPanel.add(colorThresholdSlider);
		
		final JLabel thresholdLabel = new JLabel("Threshold: " + cannyThreshold);
		sliderPanel.add(thresholdLabel);

		final JSlider thresholdSlider = new JSlider(cannyMin, cannyMax);
		thresholdSlider.setValue(cannyThreshold);

		sliderPanel.add(thresholdSlider);

		final JLabel ratioLabel = new JLabel("Ratio: " + ratio);
		final JSlider ratioSlider = new JSlider(ratioMin, ratioMax);
		ratioSlider.setValue((int) (ratio * 100));
		sliderPanel.add(ratioLabel);
		sliderPanel.add(ratioSlider);

		ChangeListener changeListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				cannyThreshold = thresholdSlider.getValue();
				thresholdLabel.setText("Threshold: " + cannyThreshold);

				ratio = ((double) ratioSlider.getValue()) / 100;
				ratioLabel.setText("Ratio: " + ratio);
				
				colorThreshold=colorThresholdSlider.getValue();
				colorLabel.setText("Color Threshold: "+colorThreshold);
				

				getArea(colorPoint);
				icon.setImage(getImgFromFile("test/colorPlusCanny"));
				mainWindow.repaint();
			}
		};

		thresholdSlider.addChangeListener(changeListener);
		ratioSlider.addChangeListener(changeListener);
		colorThresholdSlider.addChangeListener(changeListener);

		mainWindow.getContentPane().add(sliderPanel, BorderLayout.NORTH);

		mainWindow.setVisible(true);
		mainWindow.setSize(1200, 800);
	}

	public BufferedImage enhanceContrast() {

		return null;
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

		Color startColor = new Color(r < colorThreshold ? 0 : r - colorThreshold, g < colorThreshold ? 0 : g - colorThreshold, b < colorThreshold ? 0
				: b - colorThreshold);

		Color endColor = new Color(r + colorThreshold > 255 ? 255 : r + colorThreshold, g + colorThreshold > 255 ? 255 : g + colorThreshold, b
				+ colorThreshold > 255 ? 255 : b + colorThreshold);
		
		
		log.info("range color: "+startColor+" "+endColor);

		Mat inRange = applyInRange(startColor, endColor);
		
		saveImgToFile(inRange.getBufferedImage(),"test/colorExtracted");
		
//		ImgUtils.imshow("inRange with extracted color at point " + point, inRange);
		
		// TODO: filter small points out
		log.info("run a gaussion filter");
		Mat gaus=inRange.clone();
		GaussianBlur(inRange, gaus, new Size(3,3), 0);
		
		saveImgToFile(gaus.getBufferedImage(),"test/gaus1");
		
		Mat canny= applyCanny(gaus);
		
		saveImgToFile(canny.getBufferedImage(),"test/colorPlusCanny");
		
		detectLines(canny);
		
//		ImgUtils.imshow("canny on InRange", canny);

		log.info("done.");

		return null;
	}

	public Mat applyCanny(Mat src) {
		
		log.info("Applying canny filter");

		Mat cannySrc = src.clone();
		// / Reduce noise with a kernel 3x3
		blur(cannySrc, cannySrc, new Size(3, 3));

		Mat cannyDst = cannySrc.clone();

		// / Canny detector
		Canny(cannySrc, cannyDst, cannyThreshold, cannyThreshold * ratio);

		// ImgUtils.imshow("Canny",cannyDst);

		return cannyDst;
	}

	public Mat applyInRange(Color fromColor, Color toColor) {
		
		log.info("Searching for color "+fromColor+" to "+toColor);

		// from: 130, 132, 179
		// to: 170,173,219

		Mat colorSrc = src.clone();

		Mat colorDst;

		colorDst = colorSrc.clone();

		// CAUTION: inRange uses BGR intead of RGB!!!!
		
		Mat matColStart = new Mat(new Scalar(fromColor.getBlue(), fromColor.getGreen(), fromColor.getRed(),1 )), matColEnd = new Mat(new Scalar(
				toColor.getBlue(), toColor.getGreen(), toColor.getRed(),1));

		inRange(colorSrc, matColStart, matColEnd, colorDst);

		return colorDst;
	}

	public Mat detectLines(Mat orig) {
		// http://docs.opencv.org/doc/tutorials/imgproc/imgtrans/hough_lines/hough_lines.html
		
		Mat src=orig.clone();
		Mat dst=src.clone();
		
//		CvMemStorage storage = cvCreateMemStorage(0);
//		
//		CvSeq lines = new CvSeq();
//		
//		lines = cvHoughLines2(dst, storage, CV_HOUGH_PROBABILISTIC, 1, Math.PI / 180, 40, 50, 10);
		
		HoughLinesP(src, dst, 1, Math.PI/180, 80, 30, 10);
		
		log.info("original hough file:"+src);
		
		log.info("hough lines result: "+dst);
		
		return dst;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		cvReleaseImage(cvImg);
		super.finalize();
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
		MarvinImage marvinImg=new MarvinImage(baseImage);
		MarvinImage greyImg=applyPlugin("org.marvinproject.image.color.grayScale", marvinImg);
		
		ImgUtils.imshow("Marvin Test", greyImg.getBufferedImage());
		
		MarvinImage robertsImg=applyPlugin("org.marvinproject.image.edge.roberts.jar", greyImg);
		
		ImgUtils.imshow("Marvin Edge", greyImg);
		
		
	}
	
	/**
	 * load the plugin, process it with that image and return it. The original image is not modified.
	 * @param img
	 * @return
	 */
	public MarvinImage applyPlugin(String pluginName, MarvinImage img){
		MarvinImage dest=img.clone();
		MarvinImagePlugin plugin=MarvinPluginLoader.loadImagePlugin(pluginName);
		plugin.process(img, dest);
		dest.update();
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
			//imgAnalyzer.initUI(point);
//			imgAnalyzer.getArea(point);
			// Mat mat = imgAnalyzer.applyInRange();
			// ImgUtils.imshow("in range", mat);
			
			imgAnalyzer.testMarvin();
		}

	}

}
