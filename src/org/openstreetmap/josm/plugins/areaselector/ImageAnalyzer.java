/*
 * Created on Jul 23, 2014
 * Author: Paul Woelfel
 * Email: paul@woelfel.at
 */
package org.openstreetmap.josm.plugins.areaselector;

import java.awt.BorderLayout;
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_highgui;
import org.openstreetmap.josm.data.osm.Way;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 */
public class ImageAnalyzer {

	protected static Logger log = Logger.getLogger(ImageAnalyzer.class);

	protected IplImage cvImg;

	protected Mat src, grey;

	protected static final String tempFile = "cvload.png";

	protected int cannyThreshold = 30;

	protected static final int cannyMin = 10, cannyMax = 200;

	protected double ratio = 3;
	
	protected static final int ratioMin = 100, ratioMax=500;

	
	public ImageAnalyzer(String filename) {
		log.info("Loading from " + filename);
		cvImg = cvLoadImage(filename);
		init();
	}

	public ImageAnalyzer(BufferedImage bufImg) throws IOException {
		ImageIO.write(bufImg, "PNG", new File(tempFile));
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

	public void initUI() {
		final JFrame mainWindow = new JFrame("Image Analyzer");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainWindow.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		Dimension size = new Dimension(cvImg.width(), cvImg.height());
		panel.setPreferredSize(size);
		panel.setSize(size);
		Mat mat = applyCanny();

		final ImageIcon icon = new ImageIcon(mat.getBufferedImage());

		JLabel label = new JLabel();

		label.setIcon(icon);
		panel.add(label);
		// mainWindow.getContentPane().removeAll();
		mainWindow.getContentPane().add(panel);
		
		
		JPanel sliderPanel = new JPanel();
		final JLabel thresholdLabel = new JLabel("Threshold: " + cannyThreshold);
		sliderPanel.add(thresholdLabel);

		final JSlider thresholdSlider = new JSlider(cannyMin, cannyMax);
		thresholdSlider.setValue(cannyThreshold);
		
		sliderPanel.add(thresholdSlider);
		
		final JLabel ratioLabel=new JLabel("Ratio: "+ratio);
		final JSlider ratioSlider=new JSlider(ratioMin,ratioMax);
		ratioSlider.setValue((int) (ratio*100));
		sliderPanel.add(ratioLabel);
		sliderPanel.add(ratioSlider);
		
		ChangeListener changeListener=new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				cannyThreshold = thresholdSlider.getValue();
				thresholdLabel.setText("Threshold: " +cannyThreshold);
				
				ratio=((double)ratioSlider.getValue())/100;
				ratioLabel.setText("Ratio: "+ratio);
				
				Mat canny = applyCanny();
				icon.setImage(canny.getBufferedImage());
				mainWindow.repaint();
			}
		};
		
		thresholdSlider.addChangeListener(changeListener);
		ratioSlider.addChangeListener(changeListener);

		mainWindow.getContentPane().add(sliderPanel, BorderLayout.NORTH);

		mainWindow.setVisible(true);
		mainWindow.setSize(1200, 800);
	}

	public BufferedImage enhanceContrast() {

		return null;
	}

	public Way getArea(Point point) {

		log.info("done.");

		return null;
	}

	public Mat applyCanny() {

		Mat cannySrc = grey.clone();
		// / Reduce noise with a kernel 3x3
		blur(cannySrc, cannySrc, new Size(3, 3));

		Mat cannyDst = cannySrc.clone();

		// / Canny detector
		Canny(cannySrc, cannyDst, cannyThreshold, cannyThreshold * ratio);

		// ImgUtils.imshow("Canny",cannyDst);
		
		return cannyDst;
	}
	
	public Mat applyInRange(){
		
		Mat colorSrc=src.clone();
		
		Mat colorDst=new Mat(new Size(colorSrc.cols(),colorSrc.rows()), CV_8U);
		CvMat colorDstCV=colorDst.asCvMat();
		
//		Mat colorMin=new Mat(3);
//		Mat colorMax=new Mat(3);
		
//		colorMin.asCvMat()
		
//		Scalar colorMin,colorMax;
//		colorMin=new Scalar(3);
//		double[] colorStart={130,132,179};
//		
//		colorMin.put(colorStart);
//		colorMax=new Scalar(3);
//		double[] colorEnd={170,173,219};
//		colorMin.put(colorEnd);
//		
//		Mat mat=new Mat(new Size(3,1), CV_8U);
//		
//		int[] colorStartInt={130,132,179};
//		mat.create(3, colorStartInt, CV_8U);
//		
//		
//		
//		log.info("colormin: "+colorMin);
//		
//		CvMat colorSrcCV=colorSrc.asCvMat();
//		
//		log.info("src: "+colorSrcCV.size()+" dest: "+colorDstCV.size()+" dst type: "+colorDstCV.type()+" CV_8U: "+CV_8U);
//		
		//inRange(colorSrc, colorSrc, colorDst, colorDst);
		
//		cvInRangeS(colorSrc.asCvMat(), colorMin.asCvScalar(), colorMax.asCvScalar(), colorDstCV);
		
		Mat matColStart=new Mat(new Scalar(130, 132, 179, 1)),matColEnd=new Mat(new Scalar(170,173,219, 1));
		
		inRange(colorSrc,matColStart,matColEnd,colorDst);
		
		ImgUtils.imshow("in range", colorDst);
		
		
		colorDst=new Mat(colorDstCV.asIplImage());
		
		//opencv_highgui.imshow("Test", colorDst);
		
		//ImgUtils.imshow("in range", colorDst);
		
		return colorDst;
	}
	
	public void detectLines(Mat src){
		// http://docs.opencv.org/doc/tutorials/imgproc/imgtrans/hough_lines/hough_lines.html
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		cvReleaseImage(cvImg);
		super.finalize();
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.DEBUG);

		// baseimage.png 419 308
		if (args.length < 3) {
			log.warn("Usage: ImageAnalyzer basefile x y");
		} else {
			ImageAnalyzer imgAnalyzer = new ImageAnalyzer(args[0]);
			//imgAnalyzer.initUI();
			//imgAnalyzer.getArea(new Point(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
			Mat mat=imgAnalyzer.applyInRange();
			ImgUtils.imshow("in range", mat);
		}

	}



}
