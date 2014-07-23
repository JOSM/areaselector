/**
 * @file CannyDetector_Demo.cpp
 * @brief Sample code showing how to detect edges using the Canny Detector
 * @author OpenCV team
 */

#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include <stdlib.h>
#include <stdio.h>

using namespace cv;

/// Global variables

Mat src;
Mat dst;
const char* window_name = "Edge Map";



/**
 * @function main
 */
int main( int, char** argv )
{
  /// Load an image
  src = imread( argv[1] );

  if( !src.data )
    { return -1; }

  /// Create a matrix of the same type and size as src (for dst)
  dst.create( src.size(), src.type() );

  inRange(src, Scalar(130,132,179), Scalar(170,173,219), dst); //150,153,199

  /// Create a window
  namedWindow( window_name, WINDOW_AUTOSIZE );

  imshow( window_name, dst );

  /// Wait until user exit program by pressing a key
  waitKey(0);

  return 0;
}