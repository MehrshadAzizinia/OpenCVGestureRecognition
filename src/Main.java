import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

public class Main {
    static {
        // Load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Open the default camera (0) or specify another camera index
        VideoCapture camera = new VideoCapture(0);
        
        if (!camera.isOpened()) {
            System.out.println("Error: Camera not detected");
            return;
        }

        Mat frame = new Mat();
        
        while (true) {
            if (camera.read(frame)) {  // Capture frame
                HighGui.imshow("USB Camera", frame);  // Display frame
            }
            
            if (HighGui.waitKey(30) == 27) { // Press 'ESC' to exit
                break;
            }
        }

        camera.release(); // Release camera resource
        HighGui.destroyAllWindows(); // Close all windows
    }
}

