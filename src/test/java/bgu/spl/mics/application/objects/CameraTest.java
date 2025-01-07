package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.services.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class CameraServiceTest {

    private Camera camera;
    private CameraService cameraService;
    private MessageBus messageBus;
    private CountDownLatch latch = new CountDownLatch(1);
    private ErrorData errorData = new ErrorData();

    @BeforeEach
    void setUp() {
        camera = new Camera(1, 2, "camera1", "src/test/example_input/camera_data.json");
        cameraService = new CameraService(camera, new StatisticalFolder(), latch, errorData);
        messageBus = MessageBusImpl.getInstance();
        messageBus.register(cameraService);
    }

    @Test
    void testGetDetectedObjectsByTime() {
        // Test data from camera_data.json
        int objectTime = 2;
        int cameraFrequency = 2;
        int queryTime = objectTime + cameraFrequency;

        StampedDetectedObjects expectedObjects = new StampedDetectedObjects(objectTime, List.of(
                new DetectedObject("Wall_1", "Wall")
        ));

        StampedDetectedObjects actualObjects = camera.getDetectedObjectsbyTime(queryTime);

        assertNotNull(actualObjects, "The detected objects should not be null");
        assertEquals(expectedObjects.getTime(), actualObjects.getTime(), "The times should match");
        assertEquals(expectedObjects.getDetectedObjects().size(), actualObjects.getDetectedObjects().size(), "The number of detected objects should match");

        for (int i = 0; i < expectedObjects.getDetectedObjects().size(); i++) {
            DetectedObject expected = expectedObjects.getDetectedObjects().get(i);
            DetectedObject actual = actualObjects.getDetectedObjects().get(i);
            assertEquals(expected.getId(), actual.getId(), "The detected object IDs should match");
            assertEquals(expected.getDescription(), actual.getDescription(), "The detected object descriptions should match");
        }
    }
}