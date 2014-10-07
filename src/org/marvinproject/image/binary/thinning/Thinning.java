/**
Marvin Project <2007-2013>
http://www.marvinproject.org

License information:
http://marvinproject.sourceforge.net/en/license.html

Discussion group:
https://groups.google.com/forum/#!forum/marvin-project
 */

package org.marvinproject.image.binary.thinning;

import java.util.List;

import marvin.gui.MarvinAttributesPanel;
import marvin.image.MarvinImage;
import marvin.image.MarvinImageMask;
import marvin.plugin.MarvinAbstractImagePlugin;
import marvin.util.MarvinAttributes;

import org.apache.log4j.Logger;

/**
 * 
 * @author Paul Woelfel
 * 
 */
public class Thinning extends MarvinAbstractImagePlugin {

	private MarvinAttributesPanel attributesPanel;

	private MarvinAttributes attributes;

	protected List<Boolean> kernelArray;

//	private MarvinImagePlugin pluginGray;
	
	public static final Logger log=Logger.getLogger(Thinning.class);

	public void load() {

		// Attributes
		attributes = getAttributes();
		attributes.set("kernel", null);
		
	}

	public MarvinAttributesPanel getAttributesPanel() {
		if (attributesPanel == null) {
			attributesPanel = new MarvinAttributesPanel();
			
		}
		return attributesPanel;
	}

	@SuppressWarnings("unchecked")
	public void process(MarvinImage imageIn, MarvinImage imageOut, MarvinAttributes attributesOut, MarvinImageMask mask, boolean previewMode) {
		kernelArray = (List<Boolean>) attributes.get("kernel");
		log.warn("attributes: "+attributes);

//		pluginGray.process(imageIn, imageOut, attributesOut, mask, previewMode);

		boolean kernel[] = new boolean[kernelArray.size()];

		for (int i = 0; i < kernelArray.size(); i++) {
			kernel[i] = kernelArray.get(i).booleanValue();
		}

		applyKernel(imageIn, imageOut, kernel);

	}

	protected void applyKernel(MarvinImage imageIn, MarvinImage imageOut, boolean[] kernel) {
		int m=(int)Math.sqrt(kernel.length);
		if(m*m!=kernel.length) throw new RuntimeException("the matrix must have equal rows and columns");
		int half=m/2;
		
		for (int y = half; y < imageIn.getHeight()-half; y++) { // loop through rows
			for (int x = half; x < imageIn.getWidth()-half; x++) { // loop through cols

				boolean white=true;
				
				for(int kernelY=0; white==true && kernelY<m; kernelY++){
					for(int kernelX=0; white==true && kernelY<m; kernelY++){
						// check if pixel should be white
						if(kernel[kernelY*m+kernelX]==true && imageIn.getIntComponent0(x-half+kernelX,y-half+kernelY)<127){
							white=false;
						}
					}
				}
				
				if(white){
					imageOut.setIntColor(x,y, imageIn.getAlphaComponent(x,y), 255,255,255);
				}else {
					imageOut.setIntColor(x,y, imageIn.getAlphaComponent(x,y), 0,0,0);
				}
				
			}
		}
	}

	protected int getSafeColor(int x, int y, MarvinImage img) {

		if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
			return img.getIntComponent0(x, y);
		}
		return -1;
	}
}
