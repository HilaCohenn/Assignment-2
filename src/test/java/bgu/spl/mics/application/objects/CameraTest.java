package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.services.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {

    private Camera camera;
    private CameraService cameraService;
    private MessageBus messageBus;
    private CountDownLatch latch = new CountDownLatch(1);
    private ErrorData errorData = new ErrorData();

    @BeforeEach
    void setUp() {
        // Initialize the camera with test data
        List<StampedDetectedObjects> detectedObjectsList = new ArrayList<>();
        detectedObjectsList.add(new StampedDetectedObjects(2, List.of(
                new DetectedObject("Wall_1", "Wall")
        )));
        detectedObjectsList.add(new StampedDetectedObjects(4, List.of(
                new DetectedObject("Wall_3", "Wall"),
                new DetectedObject("Chair_Base_1", "Chair Base")
        )));
        detectedObjectsList.add(new StampedDetectedObjects(6, List.of(
                new DetectedObject("Wall_4", "Wall"),
                new DetectedObject("Circular_Base_1", "Circular Base")
        )));
        detectedObjectsList.add(new StampedDetectedObjects(7, List.of(
                new DetectedObject("Door", "Door")
        )));
        detectedObjectsList.add(new StampedDetectedObjects(8, List.of(
                new DetectedObject("Wall_5", "Wall")
        )));
        detectedObjectsList.add(new StampedDetectedObjects(10, List.of(
                new DetectedObject("Wall_1", "Wall")
        )));
        detectedObjectsList.add(new StampedDetectedObjects(12, List.of(
                new DetectedObject("Wall_3", "Wall")
        )));
        detectedObjectsList.add(new StampedDetectedObjects(14, List.of(
                new DetectedObject("Wall_5", "Wall")
        )));
        detectedObjectsList.add(new StampedDetectedObjects(16, List.of(
                new DetectedObject("Wall_4", "Wall")
        )));
        detectedObjectsList.add(new StampedDetectedObjects(18, List.of(
                new DetectedObject("Chair_Base_1", "Chair Base")
        )));
        detectedObjectsList.add(new StampedDetectedObjects(20, List.of(
                new DetectedObject("Circular_Base_1", "Circular Base")
        )));

        camera = new Camera(1, 2, detectedObjectsList);
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

    @Test
    void testGetDetectedObjectsByDifferentTime() {
        int objectTime = 4;
        int cameraFrequency = 2;
        int queryTime = objectTime + cameraFrequency;

        StampedDetectedObjects expectedObjects = new StampedDetectedObjects(objectTime, List.of(
                new DetectedObject("Wall_3", "Wall"),
                new DetectedObject("Chair_Base_1", "Chair Base")
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