import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;
import java.util.ArrayList;
import java.util.List;
import org.opencv.video.BackgroundSubtractorMOG2;



public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load OpenCV library
        
    }

    public static void main(String[] args) {
        VideoCapture camera = new VideoCapture(0);
        

        if (!camera.isOpened()) {
            System.out.println("Error: Camera not detected");
            return;
        }

        Mat frame = new Mat();

        while (true) {
            if (camera.read(frame)) {
                Mat processedFrame = preprocessImage(frame);

                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(processedFrame, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    Rect boundingBox = Imgproc.boundingRect(contour);
                    if (boundingBox.area() > 5000) { // Filter out small contours
                        Imgproc.rectangle(frame, boundingBox.tl(), boundingBox.br(), new Scalar(0, 255, 0), 2);

                        // Find convex hull
                        MatOfInt hull = new MatOfInt();
                        Imgproc.convexHull(contour, hull);

                        // Find convexity defects
                        MatOfInt4 defects = new MatOfInt4();
                        if (hull.toArray().length > 3) {
                            Imgproc.convexityDefects(contour, hull, defects);

                            int fingers = countFingers(contour, defects);
                            Imgproc.putText(frame, "Fingers: " + fingers, new Point(boundingBox.x, boundingBox.y - 10),
                                    Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                        }
                    }
                }

                HighGui.imshow("Hand Gesture Recognition", frame);
            }

            if (HighGui.waitKey(30) == 27) { // Press 'ESC' to exit
              break;
            }
        }

        camera.release();
        HighGui.destroyAllWindows();
    }

    private static Mat preprocessImage(Mat frame) {
    	Mat hsv = new Mat();
        Mat skinMask = new Mat();
        Mat fgMask = new Mat();
        BackgroundSubtractorMOG2 backgroundSubtractor = org.opencv.video.Video.createBackgroundSubtractorMOG2();

        // Convert to HSV color space
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

        // Skin color filter
        Scalar lower = new Scalar(0, 20, 70);
        Scalar upper = new Scalar(20, 255, 255);
        Core.inRange(hsv, lower, upper, skinMask);

        // Apply background subtraction
        backgroundSubtractor.apply(frame, fgMask);

        // Combine both masks
        Mat result = new Mat();
        Core.bitwise_and(skinMask, fgMask, result);

        return result;
    }

    private static int countFingers(MatOfPoint contour, MatOfInt4 defects) {
        int fingerCount = 0;
        List<Integer> defectList = defects.toList();
        List<Point> contourPoints = contour.toList();

        for (int i = 0; i < defectList.size(); i += 4) {
            Point start = contourPoints.get(defectList.get(i));
            Point end = contourPoints.get(defectList.get(i + 1));
            Point far = contourPoints.get(defectList.get(i + 2));

            double a = Math.sqrt(Math.pow(end.x - start.x, 2) + Math.pow(end.y - start.y, 2));
            double b = Math.sqrt(Math.pow(far.x - start.x, 2) + Math.pow(far.y - start.y, 2));
            double c = Math.sqrt(Math.pow(far.x - end.x, 2) + Math.pow(far.y - end.y, 2));

            double angle = Math.acos((b * b + c * c - a * a) / (2 * b * c)) * 180 / Math.PI;

            if (angle < 60) {
                fingerCount++;
            }
        }

        return fingerCount + 1; // Adding 1 for the thumb
    }
}
