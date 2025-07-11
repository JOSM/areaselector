/**
Marvin Project <2007-2013>
http://www.marvinproject.org

License information:
http://marvinproject.sourceforge.net/en/license.html

Discussion group:
https://groups.google.com/forum/#!forum/marvin-project
 */

package org.marvinproject.image.color.selectColor;

import marvin.gui.MarvinAttributesPanel;
import marvin.image.MarvinImage;
import marvin.image.MarvinImageMask;
import marvin.plugin.MarvinAbstractImagePlugin;
import marvin.util.MarvinAttributes;

/**
 * Thresholding with a range based on thresholding plugin
 * @author Gabriel Ambrosio Archanjo
 * @author Paul Woelfel
 */
public class SelectColor extends MarvinAbstractImagePlugin{

	protected MarvinAttributesPanel	attributesPanel;
	protected MarvinAttributes		attributes;
	protected int 					r,g,b,
	range;

	@Override
	public void load(){

		// Attributes
		attributes = getAttributes();
		attributes.set("r", 75);
		attributes.set("g", 75);
		attributes.set("b", 75);

		attributes.set("range", 10);
	}

	@Override
	public MarvinAttributesPanel getAttributesPanel(){
		if(attributesPanel == null){
			attributesPanel = new MarvinAttributesPanel();
			attributesPanel.addLabel("lblRed", "Red");
			attributesPanel.addTextField("txtRed", "r", attributes);
			attributesPanel.addLabel("lblGreen", "Green");
			attributesPanel.addTextField("txtGreen", "g", attributes);
			attributesPanel.addLabel("lblBlue", "Blue");
			attributesPanel.addTextField("txtBlue", "b", attributes);
			attributesPanel.addLabel("lblRange", "Range");
			attributesPanel.addTextField("txtRange", "range", attributes);
		}
		return attributesPanel;
	}

	@Override
	public void process
	(
			MarvinImage imageIn,
			MarvinImage imageOut,
			MarvinAttributes attributesOut,
			MarvinImageMask mask,
			boolean previewMode
			)
	{
		r = (Integer)attributes.get("r");
		g = (Integer)attributes.get("g");
		b = (Integer)attributes.get("b");
		range = (Integer)attributes.get("range");

		boolean[][] bmask = mask.getMaskArray();

		selectColor(imageIn, imageOut, bmask);

	}

	protected void selectColor(MarvinImage imageIn, MarvinImage imageOut, boolean[][] mask){
		int rMin=(r-range<0?0:r-range), rMax=(r+range>255?255:r+range);
		int gMin=(g-range<0?0:g-range), gMax=(g+range>255?255:g+range);
		int bMin=(b-range<0?0:b-range), bMax=(b+range>255?255:b+range);

		for(int y=0; y<imageIn.getHeight(); y++){
			for(int x=0; x<imageIn.getWidth(); x++){
				if(mask != null && !mask[x][y]){
					continue;
				}

				if(imageIn.getIntComponent0(x,y) > rMin && imageIn.getIntComponent0(x,y) < rMax &&
						imageIn.getIntComponent1(x,y) > gMin && imageIn.getIntComponent1(x,y) < gMax &&
						imageIn.getIntComponent2(x,y) > bMin && imageIn.getIntComponent2(x,y) < bMax){
					imageOut.setIntColor(x, y, imageIn.getAlphaComponent(x,y), 255,255,255);
				}
				else{
					imageOut.setIntColor(x, y, imageIn.getAlphaComponent(x,y), 0,0,0);

				}
			}
		}
	}




}
