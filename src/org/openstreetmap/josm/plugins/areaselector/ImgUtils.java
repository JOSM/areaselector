/*
 * Created on Jul 23, 2014
 * Author: Paul Woelfel
 * Email: paul@woelfel.at
 */
package org.openstreetmap.josm.plugins.areaselector;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import marvin.image.MarvinImage;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 */
public class ImgUtils {
	public static void imshow(String title,IplImage cvImg){
		imshow(title,cvImg.getBufferedImage());
	}
	
	public static void imshow(String title,Mat mat){
		imshow(title,mat.getBufferedImage());
	}
	
	/** 
	 * display a BufferedImage
	 * @param title title of dialog
	 * @param bufImage image to display
	 */
	public static  void imshow(String title,BufferedImage bufImage){
		ImageIcon icon = new ImageIcon(bufImage);	
		JOptionPane.showMessageDialog(null, title, title, JOptionPane.INFORMATION_MESSAGE, icon);
	}

	/**
	 * Show a Marvin Image
	 * @param title title of dialog
	 * @param img Image to display 
	 */
	public static void imshow(String title, MarvinImage img) {
		imshow(title,img.getBufferedImage());
	}

}
