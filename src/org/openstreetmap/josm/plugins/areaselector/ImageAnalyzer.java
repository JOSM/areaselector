// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.areaselector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.enhance.EnhanceImageOps;
import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.feature.detect.edge.EdgeContour;
import boofcv.alg.feature.shapes.ShapeFittingOps;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.filter.derivative.GradientSobel;
import boofcv.alg.misc.ImageStatistics;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.VisualizeImageData;
import boofcv.struct.ConnectRule;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;
import georegression.metric.UtilAngle;
import georegression.struct.point.Point2D_I32;
import marvin.image.MarvinColorModelConverter;
import marvin.image.MarvinImage;
import marvin.plugin.MarvinAbstractImagePlugin;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;

/**
 * @author Paul Woelfel (paul@woelfel.at)
 */
public class ImageAnalyzer {

	protected static Logger log = LogManager.getLogger(ImageAnalyzer.class);

	public static final String IMG_TYPE = "PNG";

	protected static int fileCount = 0;

	protected BufferedImage baseImage, workImage;

	protected MarvinImage workMarvin;

	protected Point point;

	public static final String KEY_DEBUG = "areaselector.debug";
	protected boolean debug = false;

	// Algorithm params
	public static final int DEFAULT_COLORTHRESHOLD = 14;
	public static final String KEY_COLORTHRESHOLD = "areaselector.colorthreshold";
	protected int colorThreshold = DEFAULT_COLORTHRESHOLD;

	// Polynomial fitting tolerances
	// default for border = 1
	public static final double DEFAULT_TOLERANCEDIST = 0.5d;
	public static final String KEY_TOLERANCEDIST = "areaselector.tolerancedist";
	double toleranceDist = DEFAULT_TOLERANCEDIST; // original: 2

	// default for border = Math.PI / 8
	public static final double DEFAULT_TOLERANCEANGLE = 0.42d;
	public static final String KEY_TOLERANCEANGLE = "areaselector.toleranceangle";
	double toleranceAngle = DEFAULT_TOLERANCEANGLE; // original Math.PI/10

	// gaussian blur radius = 10
	public static final int DEFAULT_BLURRADIUS = 10;
	public static final String KEY_BLURRADIUS = "areaselector.blurradius";
	protected int blurRadius = DEFAULT_BLURRADIUS;

	// default thinning iterations = 3
	public static final int DEFAULT_THINNING_ITERATIONS = 2;
	public static final String KEY_THINNING_ITERATIONS = "areaselector.thinning_iterations";
	protected int thinningIterations = DEFAULT_THINNING_ITERATIONS;

	protected boolean useHSV = false;
	public static final String KEY_HSV = "areaselector.use_hsv";

	public static final String KEY_ALGORITHM = "areaselector.algorithm";
	public static final int ALGORITHM_AUTO = 0, ALGORITHM_BOOFCV = 1, ALGORITHM_CUSTOM = 2;
	public static final int DEFAULT_ALGORITHM = ALGORITHM_AUTO;
	protected int algorithm = ALGORITHM_AUTO;

	public ImageAnalyzer(String filename, Point point) {
		this(getImgFromFile(filename), point);
	}

	public ImageAnalyzer(BufferedImage bufImg, Point point) {
		baseImage = bufImg;
		this.point = point;
		init();
	}

	protected void init() {
		readPreferences();
		if (debug) {
			BufferedImage buf = deepCopy(baseImage);
			Graphics2D g2d = buf.createGraphics();
			g2d.setColor(Color.red);
			g2d.setFont(new Font("default", Font.BOLD, 16));
			g2d.drawString("X", point.x, point.y);
			saveImgToFile(buf, "baseimage");
		}
	}

	public Polygon getArea() {

		log.info("Using following params for algorithm: colorThreshold="+colorThreshold+
				" toleranceDist="+toleranceDist+" toleranceAngle="+toleranceAngle);

		workImage = deepCopy(baseImage);

		Polygon polygon = null;

		if (algorithm == ALGORITHM_AUTO || algorithm == ALGORITHM_BOOFCV) {
			polygon = detectCannyArea(workImage, point);
		}

		if (polygon != null || algorithm == ALGORITHM_BOOFCV) {
			// canny detection was successful
			return polygon;
		} else {
			log.info("Falling back to custom detection");
			// get color at that point
			Color pointColor = new Color(workImage.getRGB(point.x, point.y));

			log.info("point color: " + pointColor);

			if (useHSV) {
				workImage = selectColor(workImage, pointColor);
			} else {
				workImage = selectMarvinColor(workImage, pointColor);
			}
			if (debug) saveImgToFile(workImage, "colorExtracted");

			workImage = invert(workImage);
			if (debug) saveImgToFile(workImage, "inverted");

			workImage = skeletonize(workImage);
			if (debug) saveImgToFile(workImage, "skeleton");


			polygon = detectArea(workImage, point);
		}

		return polygon;
	}

	/**
	 * select a color from a image
	 * @param workImage image to work with
	 * @param pointColor color to select
	 * @return BufferedImage with selected color as white and the rest as black
	 */
	public BufferedImage selectMarvinColor(BufferedImage workImage, Color pointColor) {

		log.info("extracting marvin color");

		int r = pointColor.getRed(), g = pointColor.getGreen(), b = pointColor.getBlue();

		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("range", colorThreshold);
		attributes.put("r", r);
		attributes.put("g", g);
		attributes.put("b", b);

		MarvinImage src = new MarvinImage(workImage);

		log.info("Applying gaus filter");
		MarvinImage gaus = applyPlugin("org.marvinproject.image.blur.gaussianBlur", src);
		if (debug) saveImgToFile(gaus, "gaussian");

		log.info("searching for the correct color");
		MarvinImage colorSelected = applyPlugin("org.marvinproject.image.color.selectColor", gaus, attributes);

		colorSelected.update();

		return colorSelected.getBufferedImage();
	}

	/**
	 * erode and dilate an image
	 * @param workImage image to transform
	 * @return transformed image
	 */
	public BufferedImage erodeAndDilate(BufferedImage workImage) {
		log.info("Erode and Dilate");
		ImageFloat32 input = ConvertBufferedImage.convertFromSingle(workImage, null, ImageFloat32.class);
		ImageUInt8 binary = new ImageUInt8(input.width, input.height);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(input);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(input, binary, (float) mean, true);

		// remove small blobs through erosion and dilation
		// The null in the input indicates that it should internally declare the work image it needs
		// this is less efficient, but easier to code.
		ImageUInt8 filtered = BinaryImageOps.erode4(binary, 1, null);
		filtered = BinaryImageOps.dilate4(filtered, 1, null);

		return VisualizeBinaryData.renderBinary(filtered, null);
	}

	/**
	 * dilate an image
	 * @param workImage image to transform
	 * @return transformed image
	 */
	public BufferedImage dilate(BufferedImage workImage) {
		log.info("Dilate");
		ImageFloat32 input = ConvertBufferedImage.convertFromSingle(workImage, null, ImageFloat32.class);
		ImageUInt8 binary = new ImageUInt8(input.width, input.height);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(input);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(input, binary, (float) mean, true);

		// remove small blobs through erosion and dilation
		// The null in the input indicates that it should internally declare the work image it needs
		// this is less efficient, but easier to code.
		ImageUInt8 filtered;
		filtered = BinaryImageOps.dilate4(binary, 1, null);

		return VisualizeBinaryData.renderBinary(filtered, null);
	}

	public BufferedImage binarize(BufferedImage workImage) {
		log.info("Binarize");

		ImageFloat32 input = ConvertBufferedImage.convertFromSingle(workImage, null, ImageFloat32.class);
		ImageUInt8 binary = new ImageUInt8(input.width, input.height);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(input);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(input, binary, (float) mean, true);

		return VisualizeBinaryData.renderBinary(binary, null);
	}

	/**
	 * Invert the RGB color of each pixel
	 */
	public BufferedImage invert(BufferedImage workImage) {
		BufferedImage img = new BufferedImage(workImage.getWidth(), workImage.getHeight(), BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				Color c = new Color(workImage.getRGB(x, y));
				c = new Color(255-c.getRed(), 255-c.getGreen(), 255-c.getBlue());
				img.setRGB(x, y, c.getRGB());
			}
		}

		return img;
	}

	/**
	 * apply gaussian filter
	 * @param image image to filter
	 * @return filtered image
	 */
	public BufferedImage gaussian(BufferedImage image) {
		log.info("gaussian filter");
		ImageFloat32 input = ConvertBufferedImage.convertFrom(image, (ImageFloat32) null);
		ImageFloat32 output = GeneralizedImageOps.createSingleBand(ImageFloat32.class, input.width, input.height);

		GBlurImageOps.gaussian(input, output, -1, blurRadius, null);

		return VisualizeImageData.colorizeSign(output, null, -1);
	}


	/**
	 * Selectively displays only pixels which have a similar hue and saturation values to what is provided.
	 * This is intended to be a simple example of color based segmentation.  Color based segmentation can be done
	 * in RGB color, but is more problematic due to it not being intensity invariant.  More robust techniques
	 * can use Gaussian models instead of a uniform distribution, as is done below.
	 */
	public BufferedImage selectColor(BufferedImage image, Color rgbColor) {
		log.info("selecting color");
		float[] color = new float[3];
		ColorHsv.rgbToHsv(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue(), color);

		log.info("HSV color: H = " + color[0]+" S = "+color[1]+" V = "+color[2]);
		float hue = color[0];
		float saturation = color[1];

		MultiSpectral<ImageFloat32> input = ConvertBufferedImage.convertFromMulti(image, null, true, ImageFloat32.class);
		MultiSpectral<ImageFloat32> hsv = new MultiSpectral<>(ImageFloat32.class, input.width, input.height, 3);

		// Convert into HSV
		ColorHsv.rgbToHsv_F32(input, hsv);

		// Euclidean distance squared threshold for deciding which pixels are members of the selected set
		float maxDist2 = 0.4f*0.4f;

		// Extract hue and saturation bands which are independent of intensity
		ImageFloat32 H = hsv.getBand(0);
		ImageFloat32 S = hsv.getBand(1);

		// Adjust the relative importance of Hue and Saturation.
		// Hue has a range of 0 to 2*PI and Saturation from 0 to 1.
		float adjustUnits = (float) (Math.PI/2.0);

		// step through each pixel and mark how close it is to the selected color
		BufferedImage output = new BufferedImage(input.width, input.height, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < hsv.height; y++) {
			for (int x = 0; x < hsv.width; x++) {
				// Hue is an angle in radians, so simple subtraction doesn't work
				float dh = UtilAngle.dist(H.unsafe_get(x, y), hue);
				float ds = (S.unsafe_get(x, y)-saturation)*adjustUnits;

				// this distance measure is a bit naive, but good enough for to demonstrate the concept
				float dist2 = dh*dh + ds*ds;
				if (dist2 <= maxDist2) {
					output.setRGB(x, y, image.getRGB(x, y));
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

		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image, (ImageUInt8) null);
		if (debug) saveImgToFile(ConvertBufferedImage.convertTo(gray, null), "histogram_gray");

		ImageUInt8 adjusted = new ImageUInt8(gray.width, gray.height);

		int[] histogram = new int[256];
		int[] transform = new int[256];

		ImageStatistics.histogram(gray, histogram);
		EnhanceImageOps.equalize(histogram, transform);
		EnhanceImageOps.applyTransform(gray, transform, adjusted);

		ImageUInt8 binary = new ImageUInt8(gray.width, gray.height);
		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(adjusted);
		// create a binary image by thresholding
		ThresholdImageOps.threshold(adjusted, binary, (int) mean, true);

		EnhanceImageOps.equalizeLocal(gray, 50, adjusted, histogram, transform);

		return ConvertBufferedImage.convertTo(adjusted, null);
	}

	/**
	 * When an image is sharpened the intensity of edges are made more extreme while flat regions remain unchanged.
	 */
	public BufferedImage sharpen(BufferedImage image) {
		log.info("sharpen");
		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image, (ImageUInt8) null);
		if (debug) saveImgToFile(ConvertBufferedImage.convertTo(gray, null), "gray");

		ImageUInt8 adjusted = new ImageUInt8(gray.width, gray.height);

		EnhanceImageOps.sharpen4(gray, adjusted);
		if (debug) saveImgToFile(ConvertBufferedImage.convertTo(adjusted, null), "sharpen4");

		return ConvertBufferedImage.convertTo(adjusted, null);
	}

	/**
	 * apply a sobel operator
	 * @param image Image to process
	 * @return processed Image
	 */
	public BufferedImage sobel(BufferedImage image) {
		ImageUInt8 input = ConvertBufferedImage.convertFrom(image, (ImageUInt8) null);
		ImageSInt16 derivX = new ImageSInt16(input.width, input.height);
		ImageSInt16 derivY = new ImageSInt16(input.width, input.height);
		GradientSobel.process(input, derivX, derivY, null);
		if (debug) saveImgToFile(ConvertBufferedImage.convertTo(derivX, null), "derivX");
		if (debug) saveImgToFile(ConvertBufferedImage.convertTo(derivY, null), "derivY");
		ImageSInt16 combined = new ImageSInt16(input.width, input.height);

		for (int y = 0; y < input.height; y++) {
			for (int x = 0; x < input.width; x++) {
				combined.set(x, y, (derivX.get(x, y) | derivY.get(x, y)));
			}
		}
		if (debug) saveImgToFile(ConvertBufferedImage.convertTo(combined, null), "combined");
		return image;
	}

	public BufferedImage canny(BufferedImage image) {
		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image, (ImageUInt8) null);
		ImageUInt8 edgeImage = new ImageUInt8(gray.width, gray.height);

		// Create a canny edge detector which will dynamically compute the threshold based on maximum edge intensity
		// It has also been configured to save the trace as a graph.  This is the graph created while performing
		// hysteresis thresholding.
		CannyEdge<ImageUInt8, ImageSInt16> canny = FactoryEdgeDetectors.canny(2, true, true, ImageUInt8.class, ImageSInt16.class);

		// The edge image is actually an optional parameter.  If you don't need it just pass in null
		canny.process(gray, 0.1f, 0.3f, edgeImage);

		// First get the contour created by canny
		List<EdgeContour> edgeContours = canny.getContours();
		// The 'edgeContours' is a tree graph that can be difficult to process.  An alternative is to extract
		// the contours from the binary image, which will produce a single loop for each connected cluster of pixels.
		// Note that you are only interested in external contours.
		List<Contour> contours = BinaryImageOps.contour(edgeImage, ConnectRule.EIGHT, null);
		BufferedImage visualBinary = VisualizeBinaryData.renderBinary(edgeImage, null);
		if (debug) {
			BufferedImage visualCannyContour = VisualizeBinaryData.renderContours(edgeContours, null,
					gray.width, gray.height, null);
			BufferedImage visualEdgeContour = VisualizeBinaryData.renderExternal(contours, null,
					gray.width, gray.height, null);
			saveImgToFile(visualBinary, "canny_binary");
			saveImgToFile(visualCannyContour, "canny_contour");
			saveImgToFile(visualEdgeContour, "canny_edge");
		}

		return visualBinary;
	}

	/**
	 * skeletonize an image
	 * Algorithm is based on http://homepages.inf.ed.ac.uk/rbf/HIPR2/thin.htm
	 * @param image input image
	 * @return skeletonized image
	 */
	public BufferedImage skeletonize(BufferedImage image) {

		boolean[][] img = new boolean[image.getHeight()][image.getWidth()];

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				img[y][x] = ((image.getRGB(x, y) >> 16) & 0xFF) > 127;
			}
		}

		for (int j = 0; j < thinningIterations; j++) {

			int[] thinningKernel1 = {
					0, 0, 0,
					-1, 1, -1,
					1, 1, 1
			};

			int[] thinningKernel2 = {
					-1, 0, 0,
					1, 1, 0,
					-1, 1, -1
			};

			for (int i = 0; i < 4; i++) {
				img = applyKernelDiff(img, thinningKernel1);
				img = applyKernelDiff(img, thinningKernel2);

				thinningKernel1 = rotateCW(thinningKernel1);
				thinningKernel2 = rotateCW(thinningKernel2);

			}

			if (debug) saveImgToFile(img, "thin");
		}

		for (int j = 0; j < thinningIterations; j++) {

			// filter small lines
			int[][] noiseReductionKernel1 =
				{
						{
							1, 1, 1,
							-1, 1, -1,
							0, 0, 0
						},
						{
							-1, 1, 1,
							0, 1, 1,
							0, 0, -1
						} // rotation of 45 degrees
				};

			int[][] noiseReductionKernel2 =
				{
						{
							0, -1, -1,
							0, 1, 0,
							0, 0, 0
						},
						{
							0, 0, -1,
							0, 1, -1,
							0, 0, 0
						} // rotation of 45 degrees
				};

			for (int i = 0; i < 8; i++) {
				img = applyKernelDiff(img, noiseReductionKernel1[i % 2]);
				noiseReductionKernel1[i % 2] = rotateCW(noiseReductionKernel1[i % 2]);
				img = applyKernelDiff(img, noiseReductionKernel2[i % 2]);
				noiseReductionKernel2[i % 2] = rotateCW(noiseReductionKernel2[i % 2]);
			}

			if (debug) saveImgToFile(img, "noisereduction");
		}

		return convertBinaryMatrixToImage(img);
	}

	/**
	 * thin an image with the given kernel
	 *  <p>1 in a kernel means the pixel has to be white<br>
	 * 0 in a kernel means the pixel has to be black<br >
	 * -1 in a kernel means the pixel will be ignored</p>
	 */
	public boolean[][] applyKernelDiff(boolean[][] src, int[] kernel) {
		// apply the kernel to get edge pixels
		boolean[][] dest = applyKernel(src, kernel);

		// and now erase the found pixels
		boolean[][] erased = new boolean[src.length][src[0].length];
		for (int y = 0; y < dest.length; y++) {
			for (int x = 0; x < dest[y].length; x++) {

				if (dest[y][x]) {
					erased[y][x] = false;
				} else {
					erased[y][x] = src[y][x];
				}
			}
		}

		return erased;
	}

	/**
	 * apply a kernel to an image
	 * <p>1 in a kernel means the pixel has to be white<br>
	 * 0 in a kernel means the pixel has to be black<br >
	 * -1 in a kernel means the pixel will be ignored</p>
	 */
	public boolean[][] applyKernel(boolean[][] src, int[] kernel) {

		boolean[][] dest = new boolean[src.length][src[0].length];

		int m = (int) Math.sqrt(kernel.length);
		if (m*m != kernel.length) throw new RuntimeException("the matrix must have equal rows and columns");
		int half = m/2;

		for (int y = half; y < src.length-half; y++) { // loop through rows
			for (int x = half; x < src[y].length-half; x++) { // loop through cols

				boolean white = true;

				for (int kernelY = 0; white && kernelY < m; kernelY++) {
					for (int kernelX = 0; white && kernelX < m; kernelX++) {
						// check if pixel should be white

						if (kernel[kernelY*m+kernelX] != -1) {

							// get r like Color class does
							if (kernel[kernelY*m+kernelX] == 1) {
								if (!src[y-half+kernelY][x-half+kernelX]) {
									white = false;
								}
							} else {
								if (src[y-half+kernelY][x-half+kernelX]) {
									white = false;
								}
							}
						}
					}
				}

				if (white) {
					dest[y][x] = true;
				} else {
					dest[y][x] = false;
				}

			}
		}

		return dest;
	}

	/**
	 * convert a kernel to a readable format
	 * @param kernel kernel to show
	 * @return String representing the kernel
	 */
	public static String kernelToString(int[] kernel) {
		StringBuilder sb = new StringBuilder();
		int row = (int) Math.sqrt(kernel.length);
		for (int i = 0; i < kernel.length; i++) {
			sb.append(kernel[i]);
			if ((i+1) % row == 0) {
				sb.append("\n");
			} else {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	public static String binaryImageToString(BufferedImage image) {
		StringBuilder sb = new StringBuilder();

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (((image.getRGB(x, y) >> 16) & 0xFF) > 127) {
					sb.append("1");
				} else {
					sb.append("0");
				}
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * rotate a matrix counterwise
	 */
	static int[] rotateCW(int[] mat) {
		final int M = (int) Math.sqrt(mat.length);
		final int N = M;
		int[] ret = new int[mat.length];
		for (int r = 0; r < M; r++) {
			for (int c = 0; c < N; c++) {
				ret[c*N+M-1-r] = mat[r*M+c];
			}
		}
		return ret;
	}

	/**
	 * detect a Polygon around a point with Canny Edge detection from boofcv
	 * @param image Image to analyze for polygons
	 * @param point point to search the polygon
	 * @return Polygon if found
	 */
	public Polygon detectCannyArea(BufferedImage image, Point point) {

		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image, (ImageUInt8) null);
		ImageUInt8 edgeImage = new ImageUInt8(gray.width, gray.height);

		// Create a canny edge detector which will dynamically compute the threshold based on maximum edge intensity
		// It has also been configured to save the trace as a graph.  This is the graph created while performing
		// hysteresis thresholding.
		CannyEdge<ImageUInt8, ImageSInt16> canny = FactoryEdgeDetectors.canny(2, true, true, ImageUInt8.class, ImageSInt16.class);

		// The edge image is actually an optional parameter.  If you don't need it just pass in null
		canny.process(gray, 0.1f, 0.3f, edgeImage);

		// First get the contour created by canny
		// List<EdgeContour> edgeContours = canny.getContours();
		// The 'edgeContours' is a tree graph that can be difficult to process.  An alternative is to extract
		// the contours from the binary image, which will produce a single loop for each connected cluster of pixels.
		// Note that you are only interested in external contours.
		List<Contour> contours = BinaryImageOps.contour(edgeImage, ConnectRule.EIGHT, null);


		Polygon innerPolygon = searchPolygon(contours, image);
		if (innerPolygon != null) {
			log.info("Detected polygon with canny algorithm "+innerPolygon);
		}

		return innerPolygon;
	}

	/**
	 * detect a Polygon around a point
	 * @param image Image to analyze for polygons
	 * @param point point to search the polygon
	 * @return Polygon if found
	 */
	public Polygon detectArea(BufferedImage image, Point point) {

		ImageFloat32 input = ConvertBufferedImage.convertFromSingle(image, null, ImageFloat32.class);
		ImageUInt8 binary = new ImageUInt8(input.width, input.height);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(input);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(input, binary, (float) mean, true);

		// reduce noise with some filtering
		//        ImageUInt8 filtered = BinaryImageOps.erode8(binary, 1, null);
		//        filtered = BinaryImageOps.dilate8(filtered, 1, null);

		// Find the contour around the shapes
		List<Contour> contours = BinaryImageOps.contour(binary, ConnectRule.FOUR, null);

		Polygon innerPolygon = searchPolygon(contours, image);

		return innerPolygon;
	}

	/**
	 * search for a matching polygon
	 *
	 * @param contours contours detected by boofcv
	 * @param input input image
	 * @return Polygon if found, otherwise null
	 */
	public Polygon searchPolygon(List<Contour> contours, BufferedImage input) {
		List<Polygon> polygons = new ArrayList<>();

		BufferedImage polygonImage = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);

		// Fit a polygon to each shape and draw the results
		Graphics2D g2 = polygonImage.createGraphics();
		g2.setStroke(new BasicStroke(2));

		for (Contour c : contours) {
			// Fit the polygon to the found external contour.  Note loop = true
			List<PointIndex_I32> vertexes = ShapeFittingOps.fitPolygon(c.external, true, toleranceDist, toleranceAngle, 100);

			Polygon poly = toPolygon(vertexes);
			if (poly.contains(point)) {

				polygons.add(poly);
				if (debug) {
					g2.setColor(Color.RED);
					VisualizeShapes.drawPolygon(vertexes, true, g2);

					// handle internal contours now
					g2.setColor(Color.BLUE);
				}
				for (List<Point2D_I32> internal : c.internal) {
					vertexes = ShapeFittingOps.fitPolygon(internal, true, toleranceDist, toleranceAngle, 100);
					poly = toPolygon(vertexes);
					if (poly.contains(point)) {
						polygons.add(poly);
						if (debug) {
							VisualizeShapes.drawPolygon(vertexes, true, g2);
						}
					}
				}
			}
		}

		if (debug) saveImgToFile(polygonImage, "polygons");

		log.info("Found "+polygons.size()+" matching polygons");


		Polygon innerPolygon = null;
		// let's see if we have outer polygons
		// if so, remove them, we only want inner shapes
		for (Polygon p: polygons) {
			if (innerPolygon == null || innerPolygon.getBounds().contains(p.getBounds())) {
				// found a inner polygon which contains the point
				innerPolygon = p;
			}
		}
		if (innerPolygon != null) {

			log.info("Best matching polygon is: "+polygonToString(innerPolygon));


			polygonImage = deepCopy(baseImage);
			g2 = polygonImage.createGraphics();

			g2.setColor(Color.RED);
			g2.setStroke(new BasicStroke(2));
			g2.drawPolygon(innerPolygon);

			if (debug) saveImgToFile(polygonImage, "polygon");

			workImage = polygonImage;
		}
		return innerPolygon;
	}

	public Polygon toPolygon(List<PointIndex_I32> points) {
		int npoints = points.size();
		int[] xpoints = new int[npoints], ypoints = new int[npoints];
		int i = 0;
		for (PointIndex_I32 point : points) {
			xpoints[i] = point.x;
			ypoints[i] = point.y;
			i++;
		}

		return new Polygon(xpoints, ypoints, npoints);
	}

	/**
	 * Return the String info of a Polygon
	 * @param p Polygon to return as String
	 */
	public static String polygonToString(Polygon p) {
		if (p == null) return "";
		StringBuilder sb = new StringBuilder();
		sb.append("Polygon (");
		sb.append(p.npoints);
		sb.append(" points) [");
		for (int i = 0; i < p.npoints; i++) {
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

	public boolean saveImgToFile(MarvinImage buf, String filename) {
		buf.update();
		return saveImgToFile(buf.getBufferedImage(), filename);
	}

	public BufferedImage convertBinaryMatrixToImage(boolean[][] image) {
		BufferedImage buf = new BufferedImage(image[0].length, image.length, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < image.length; y++) {
			for (int x = 0; x < image[y].length; x++) {
				buf.setRGB(x, y, (image[y][x] ? Color.white : Color.black).getRGB());
			}
		}
		return buf;
	}

	public boolean saveImgToFile(boolean[][]image, String filename) {
		return saveImgToFile(convertBinaryMatrixToImage(image), filename);
	}

	public boolean saveImgToFile(BufferedImage buf, String filename) {
		try {
			File folder = new File("test");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			if (!folder.isDirectory()) {
				log.warn("test is not a folder, but a file");
				return false;
			} else {
				ImageIO.write(buf, IMG_TYPE,
						new File("test/"+(fileCount < 10 ? "0" : "")+(fileCount++)+"_"+filename+"."+IMG_TYPE.toLowerCase()));
				return true;
			}
		} catch (Exception e) {
			log.warn("unable to save image", e);
		}
		return false;
	}

	public static BufferedImage getImgFromFile(String filename) {
		try {
			return ImageIO.read(new File(filename+"."+IMG_TYPE.toLowerCase()));
		} catch (IOException e) {
			log.warn("unable to read file "+filename, e);
		}
		return null;
	}

	/**
	 * find out if marvin fits our needs.
	 */
	public void testMarvin() {
		MarvinImage mImg = new MarvinImage(getImgFromFile("test/boundary_in"));
		MarvinImage inverted = applyPlugin("org.marvinproject.image.color.invert", mImg);
		MarvinImage blackAndWhite = MarvinColorModelConverter.rgbToBinary(inverted, 127);
		MarvinImage boundary = applyPlugin("org.marvinproject.image.morphological.boundary", blackAndWhite);
		ImgUtils.imshow("boundary", boundary);
	}

	/**
	 * load the plugin, process it with that image and return it. The original image is not modified.
	 * @param pluginName the plugin jar name
	 * @param img image to processed
	 * @return the modified image
	 */
	public MarvinImage applyPlugin(String pluginName, MarvinImage img) {
		return applyPlugin(pluginName, img, null);
	}

	/**
	 * load the plugin, process it with that image and return it. The original image is not modified.
	 * @param pluginName the plugin jar name
	 * @param img image to processed
	 * @param attributes attributes to set
	 * @return the modified image
	 */
	public MarvinImage applyPlugin(String pluginName, MarvinImage img, HashMap<String, Object> attributes) {
		MarvinImage dest = img.clone();
		MarvinImagePlugin plugin = MarvinPluginLoader.loadImagePlugin(pluginName);

		if (attributes != null) {
			for (String key : attributes.keySet()) {
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
	public MarvinImagePlugin getPlugin(String pluginClassName) {
		MarvinImagePlugin plugin = null;

		try {
			Class<?> classObject = Class.forName(pluginClassName);
			Object classInstance = classObject.newInstance();

			if (classInstance instanceof MarvinImagePlugin) {
				// this is a correct plugin
				plugin = (MarvinAbstractImagePlugin) classInstance;
				plugin.load();
			} else {
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
		this.toleranceAngle = (toleranceAngle > Math.PI/2 ? DEFAULT_TOLERANCEANGLE : toleranceAngle);
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
	 * @return the thinningIterations
	 */
	public int getThinningIterations() {
		return thinningIterations;
	}

	/**
	 * @param thinningIterations the thinningIterations to set
	 */
	public void setThinningIterations(int thinningIterations) {
		this.thinningIterations = thinningIterations;
	}

	public void readPreferences() {
		debug = new BooleanProperty(KEY_DEBUG, false).get();
		colorThreshold = new IntegerProperty(KEY_COLORTHRESHOLD, DEFAULT_COLORTHRESHOLD).get();
		toleranceDist = new DoubleProperty(KEY_TOLERANCEDIST, DEFAULT_TOLERANCEDIST).get();
		toleranceAngle = new DoubleProperty(KEY_TOLERANCEANGLE, DEFAULT_TOLERANCEANGLE).get();
		blurRadius = new IntegerProperty(KEY_BLURRADIUS, DEFAULT_BLURRADIUS).get();
		thinningIterations = new IntegerProperty(KEY_THINNING_ITERATIONS, DEFAULT_THINNING_ITERATIONS).get();
		useHSV = new BooleanProperty(KEY_HSV, false).get();
		algorithm = new IntegerProperty(KEY_ALGORITHM, DEFAULT_ALGORITHM).get();
	}

	public static void main(String[] args) {
		ConsoleAppender console = ConsoleAppender.newBuilder().setName("console").setLayout(
				PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c:%L: %m %x%n").build())
				.build();

		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		LoggerConfig config = ctx.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		config.addAppender(console, Level.DEBUG, null);
		config.setLevel(Level.DEBUG);
		ctx.updateLoggers();

		// test/baseimage.png 419 308
		if (args.length < 3) {
			log.warn("Usage: ImageAnalyzer basefile x y");
		} else {
			Point point = new Point(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			ImageAnalyzer imgAnalyzer = new ImageAnalyzer(args[0], point);

			Polygon polygon = imgAnalyzer.getArea();
			log.info("got polygon "+polygonToString(polygon));
		}
	}
}
