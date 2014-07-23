/*
 * Created on Jul 23, 2014
 * Author: Paul Woelfel
 * Email: paul@woelfel.at
 */
package org.openstreetmap.josm.plugins.areaselector;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

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
	
	public static  void imshow(String title,BufferedImage bufImage){
		ImageIcon icon = new ImageIcon(bufImage);	
		JOptionPane.showMessageDialog(null, title, title, JOptionPane.INFORMATION_MESSAGE, icon);
	}

}
