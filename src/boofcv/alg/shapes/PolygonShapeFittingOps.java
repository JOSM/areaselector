/*
 * Copyright (c) 2011-2015, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.shapes;

import java.util.ArrayList;
import java.util.List;

import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_I32;

import boofcv.alg.shapes.polyline.RefinePolyLineCorner;
import boofcv.alg.shapes.polyline.SplitMergeLineFitAngleLoop;
import boofcv.alg.shapes.polyline.SplitMergeLineFitAngleSegment;
import boofcv.struct.PointIndex_I32;
import georegression.struct.point.Point2D_I32;

/**
 * Functions for fitting shapes to sequences of points. Points sequences are often found by computing a shape's
 * contour or edge.
 *
 * @see boofcv.alg.feature.detect.edge.CannyEdge
 * @see boofcv.alg.filter.binary.BinaryImageOps#contour(boofcv.struct.image.ImageUInt8, boofcv.struct.ConnectRule, boofcv.struct.image.ImageSInt32)
 *
 * @author Peter Abeles
 */
public class PolygonShapeFittingOps {

	/**
	 * <p>Fits a polygon to the provided sequence of connected points.  The found polygon is returned as a list of
	 * vertices.  Each point in the original sequence is guaranteed to be within "toleranceDist' of a line segment.</p>
	 *
	 * <p>Internally a split-and-merge algorithm is used.  See referenced classes for more information. Consider
	 * using internal algorithms directly if this function is a performance bottleneck.</p>
	 *
	 * @see SplitMergeLineFitAngleLoop
	 * @see SplitMergeLineFitAngleSegment
	 *
	 * @param sequence Ordered and connected list of points.
	 * @param loop If true the sequence is a connected at both ends, otherwise it is assumed to not be.
	 * @param toleranceDist Maximum distance away each point in the sequence can be from a line, in pixels.  Try 2.
	 * @param toleranceAngle Tolerance for fitting angles, in radians. Try 0.1
	 * @param iterations Maximum number of iterations done to improve the fit. Can be 0. Try 50.
	 * @return Vertexes in the fit polygon.
	 */
	public static List<PointIndex_I32> fitPolygon(List<Point2D_I32> sequence,  boolean loop,
			double toleranceDist, double toleranceAngle, int iterations) {
		GrowQueue_I32 splits;

		if( loop ) {
			SplitMergeLineFitAngleLoop alg = new SplitMergeLineFitAngleLoop(toleranceDist,toleranceAngle,iterations);
			alg.process(sequence);
			splits = alg.getSplits();
			RefinePolyLineCorner refine = new RefinePolyLineCorner(true,10);
			refine.fit(sequence,splits);
		} else {
			SplitMergeLineFitAngleSegment alg = new SplitMergeLineFitAngleSegment(toleranceDist,toleranceAngle,iterations);
			alg.process(sequence);
			splits = alg.getSplits();
			RefinePolyLineCorner refine = new RefinePolyLineCorner(false,10);
			refine.fit(sequence,splits);
		}

		FastQueue<PointIndex_I32> output = new FastQueue<PointIndex_I32>(PointIndex_I32.class,true);
		indexToPointIndex(sequence,splits,output);



		return new ArrayList<PointIndex_I32>(output.toList());
	}


	/**
	 * Converts the list of indexes in a sequence into a list of {@link PointIndex_I32}.
	 * @param sequence Sequence of points.
	 * @param indexes List of indexes in the sequence.
	 * @param output Output list of {@link PointIndex_I32}.
	 */
	public static void indexToPointIndex( List<Point2D_I32> sequence , GrowQueue_I32 indexes ,
			FastQueue<PointIndex_I32> output ) {
		output.reset();

		for( int i = 0; i < indexes.size; i++ ) {
			int index = indexes.data[i];
			Point2D_I32 p = sequence.get(index);

			PointIndex_I32 o = output.grow();
			o.x = p.x;
			o.y = p.y;
			o.index = index;
		}
	}
}