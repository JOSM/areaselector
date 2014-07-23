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
import org.bytedeco.javacpp.opencv_core.IplImage;
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

	protected float ratio = 3;

	protected static final int cannyMin = 10, cannyMax = 200;

	public ImageAnalyzer(String filename) {
		log.info("Loading from " + filename);
		cvImg = cvLoadImage(filename);
		init();
		initUI();
	}

	public ImageAnalyzer(BufferedImage bufImg) throws IOException {
		ImageIO.write(bufImg, "PNG", new File(tempFile));
		cvImg = cvLoadImage(tempFile);
		new File(tempFile).delete();
		init();
		initUI();
	}

	protected void init() {
		if (cvImg == null) {
			log.warn("cvIMG is null!!");
		}

		log.info("createing mat");

		// ImgUtils.imshow("cvImg",cvImg);
		Mat src = new Mat(cvImg);

		log.info("creating grey");
		grey = src.clone();
		cvtColor(src, grey, CV_BGR2GRAY);
	}

	protected void initUI() {
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
		final JLabel sliderLabel = new JLabel("" + cannyThreshold);
		sliderPanel.add(sliderLabel);

		final JSlider slider = new JSlider(cannyMin, cannyMax);
		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				cannyThreshold = slider.getValue();
				sliderLabel.setText(""+cannyThreshold);
				Mat canny = applyCanny();
				icon.setImage(canny.getBufferedImage());
				mainWindow.repaint();
			}
		});

		sliderPanel.add(slider);

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

	protected Mat applyCanny() {

		Mat cannySrc = grey.clone();
		// / Reduce noise with a kernel 3x3
		blur(cannySrc, cannySrc, new Size(3, 3));

		Mat cannyDst = cannySrc.clone();

		// / Canny detector
		Canny(cannySrc, cannyDst, cannyThreshold, cannyThreshold * ratio);

		// ImgUtils.imshow("Canny",cannyDst);

		return cannyDst;
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
			imgAnalyzer.getArea(new Point(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
		}

	}

}
