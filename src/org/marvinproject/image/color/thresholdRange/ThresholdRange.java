/**
Marvin Project <2007-2013>
http://www.marvinproject.org

License information:
http://marvinproject.sourceforge.net/en/license.html

Discussion group:
https://groups.google.com/forum/#!forum/marvin-project
 */

package org.marvinproject.image.color.thresholdRange;

import marvin.gui.MarvinAttributesPanel;
import marvin.image.MarvinImage;
import marvin.image.MarvinImageMask;
import marvin.plugin.MarvinAbstractImagePlugin;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinAttributes;
import marvin.util.MarvinPluginLoader;

/**
 * Thresholding with a range based on thresholding plugin
 * @author Gabriel Ambrosio Archanjo
 * @author Paul Woelfel
 *
 */
public class ThresholdRange extends MarvinAbstractImagePlugin{

	private MarvinAttributesPanel	attributesPanel;
	private MarvinAttributes		attributes;
	private int 					thresholdMin,
	thresholdMax,
	neighborhood,
	range;

	private MarvinImagePlugin pluginGray;

	@Override
	public void load(){

		// Attributes
		attributes = getAttributes();
		attributes.set("thresholdMin", 125);
		attributes.set("thresholdMax", 150);

		attributes.set("neighborhood", -1);
		attributes.set("range", -1);

		pluginGray = MarvinPluginLoader.loadImagePlugin("org.marvinproject.image.color.grayScale.jar");
	}

	@Override
	public MarvinAttributesPanel getAttributesPanel(){
		if(attributesPanel == null){
			attributesPanel = new MarvinAttributesPanel();
			attributesPanel.addLabel("lblThreshold", "Threshold Min");
			attributesPanel.addTextField("txtThreshold", "thresholdMin", attributes);
			attributesPanel.addLabel("lblThreshold", "Threshold Max");
			attributesPanel.addTextField("txtThreshold", "thresholdMax", attributes);
			attributesPanel.addLabel("lblNeighborhood", "Neighborhood");
			attributesPanel.addTextField("txtNeighborhood", "neighborhood", attributes);
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
		thresholdMin = (Integer)attributes.get("thresholdMin");
		thresholdMax = (Integer)attributes.get("thresholdMax");
		neighborhood = (Integer)attributes.get("neighborhood");
		range = (Integer)attributes.get("range");

		pluginGray.process(imageIn, imageOut, attributesOut, mask, previewMode);

		boolean[][] bmask = mask.getMaskArray();

		if(neighborhood == -1 && range == -1){
			hardThreshold(imageIn, imageOut, bmask);
		}
		else{
			contrastThreshold(imageIn, imageOut);
		}

	}

	private void hardThreshold(MarvinImage imageIn, MarvinImage imageOut, boolean[][] mask){
		for(int y=0; y<imageIn.getHeight(); y++){
			for(int x=0; x<imageIn.getWidth(); x++){
				if(mask != null && !mask[x][y]){
					continue;
				}

				if(imageIn.getIntComponent0(x,y) > thresholdMin && imageIn.getIntComponent0(x,y) < thresholdMax){
					imageOut.setIntColor(x, y, imageIn.getAlphaComponent(x,y), 0,0,0);
				}
				else{
					imageOut.setIntColor(x, y, imageIn.getAlphaComponent(x,y), 255,255,255);
				}
			}
		}
	}

	private void contrastThreshold
	(
			MarvinImage imageIn,
			MarvinImage imageOut
			){
		range = 1;
		for (int x = 0; x < imageIn.getWidth(); x++) {
			for (int y = 0; y < imageIn.getHeight(); y++) {
				if(checkNeighbors(x,y, neighborhood, neighborhood, imageIn)){
					imageOut.setIntColor(x,y,0,0,0);
				}
				else{
					imageOut.setIntColor(x,y,255,255,255);
				}
			}
		}
	}

	private boolean checkNeighbors(int x, int y, int neighborhoodX, int neighborhoodY, MarvinImage img){

		// TODOD modify for min and max
		int color;
		int z=0;

		color = img.getIntComponent0(x, y);

		for(int i=0-neighborhoodX; i<=neighborhoodX; i++){
			for(int j=0-neighborhoodY; j<=neighborhoodY; j++){
				if(i == 0 && j == 0){
					continue;
				}

				if(color < getSafeColor(x+i,y+j, img)-range && getSafeColor(x+i,y+j, img) != -1){
					z++;
				}
			}
		}

		if(z > (neighborhoodX*neighborhoodY)*0.5){
			return true;
		}

		return false;
	}

	private int getSafeColor(int x, int y, MarvinImage img){

		if(x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()){
			return img.getIntComponent0(x, y);
		}
		return -1;
	}
}
