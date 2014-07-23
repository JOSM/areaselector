/*
 * Created on Jul 23, 2014
 * Author: Paul Woelfel
 * Email: paul@woelfel.at
 */
package org.openstreetmap.josm.plugins.areaselector;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_highgui;
import org.bytedeco.javacpp.opencv_imgproc;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Way;


import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_highgui.*;


/**
 * @author Paul Woelfel (paul@woelfel.at)
 */
public class ImageAnalyzer {
	
	protected static Logger log=Logger.getLogger(ImageAnalyzer.class);
	
	protected IplImage cvImg;
	
	protected static final String tempFile="cvload.png";
	
	
	public ImageAnalyzer(String filename){
		log.info("Loading from "+filename);
		cvImg=cvLoadImage(filename);
	}
	
	public ImageAnalyzer(BufferedImage bufImg) throws IOException{
		ImageIO.write(bufImg, "PNG", new File(tempFile));
		cvImg=cvLoadImage(tempFile);
		new File(tempFile).delete();
		
	}
	
	public BufferedImage enhanceContrast(){
		
		return null;
	}
	
	public Way getArea(Point point){
		
		
//		inRange(cvImg,new Scalar(130,132,179), new Scalar(170,173,219));
		//opencv_core.cvFlip(cvImg,cvImg,opencv_imgproc.CV_RGBA2GRAY);
		
		if(cvImg==null){
			log.warn("cvIMG is null!!");
		}
		

		log.info("createing mat");
		
		this.imshow("cvImg",cvImg);
		Mat src=new Mat(cvImg);
		
		
		Mat dst=src.clone();
		cvtColor(src,dst,CV_BGR2GRAY);
//		
		this.imshow("grey scale",dst);
		
		

		return null;
	}
	
	
	public void imshow(String title,IplImage cvImg){
		this.imshow(title,cvImg.getBufferedImage());
	}
	
	public void imshow(String title,Mat mat){
		this.imshow(title,mat.getBufferedImage());
	}
	
	public void imshow(String title,BufferedImage bufImage){
		ImageIcon icon = new ImageIcon(bufImage);	
		JOptionPane.showMessageDialog(Main.parent, title, title, JOptionPane.INFORMATION_MESSAGE, icon);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.DEBUG);
		
		// baseimage.png 419 308
		if(args.length<3){
			log.warn("Usage: ImageAnalyzer basefile x y");
		}else {
			ImageAnalyzer imgAnalyzer=new ImageAnalyzer(args[0]);
			imgAnalyzer.getArea(new Point(Integer.parseInt(args[1]),Integer.parseInt(args[2])));
		}
		
	}

}
