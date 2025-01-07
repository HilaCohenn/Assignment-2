package bgu.spl.mics.application.objects;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.services.CameraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import static org.junit.jupiter.api.Assertions.*;

class CameraServiceTest {

    private Camera camera;
    private CameraService cameraService;
    private MessageBus messageBus;

    @BeforeEach
    void setUp() {
        camera = new Camera(1, 2, "example_input\\camera_data.json"); // Camera with ID 1

        // make sure the path is correct

        cameraService = new CameraService(camera, null);
        messageBus = MessageBusImpl.getInstance();
        messageBus.register(cameraService);
    }

    @Test
    void testHandleTickBroadcast() {
        // Preconditions:
        // 1. The camera status is set to UP (not ERROR or DOWN).
        // 2. The CameraService is subscribed to TickBroadcasts.
        assertEquals(STATUS.UP, camera.getStatus());

        // Simulate detected objects at time 5
        int currentTime = 5;
        StampedDetectedObjects detectedObjects = camera.getDetectedObjectsbyTime(currentTime);
        assertNotNull(detectedObjects);

        TickBroadcast tick = new TickBroadcast(currentTime);
        messageBus.sendBroadcast(tick);

        // Postconditions:
        // 1. CameraService processes the broadcast.
        // 2. A DetectObjectsEvent is sent for detected objects.
        // 3. The detected object data is processed correctly.

        try {
            Message message = messageBus.awaitMessage(cameraService);
            assertTrue(message instanceof DetectObjectsEvent);

            DetectObjectsEvent event = (DetectObjectsEvent) message;
            assertNotNull(event.getDetectedObjects());
            assertEquals(detectedObjects.getDetectedObjects().size(), event.getDetectedObjects().getDetectedObjects().size());
        } catch (InterruptedException e) {
            fail("CameraService should not be interrupted while waiting for a message.");
        }

        // Invariants:
        // 1. The CameraService must remain subscribed to TickBroadcasts until terminated.
        // 2. The message queue for CameraService should only contain relevant messages.
    }

    @Test
    void testCameraErrorStatus() {
        // Preconditions:
        // 1. The camera is operational initially.
        // 2. Detected objects contain one with ID "ERROR".

        // Simulate detected objects at time 10 with an error
        int currentTime = 10;
        StampedDetectedObjects detectedObjects = camera.getDetectedObjectsbyTime(currentTime);
        assertNotNull(detectedObjects);

        // Manually add an error object to the detected objects list
        detectedObjects.getDetectedObjects().add(new DetectedObject("ERROR", "Critical Error"));

        TickBroadcast tick = new TickBroadcast(currentTime);
        messageBus.sendBroadcast(tick);

        // Postconditions:
        // 1. The camera status is set to ERROR.
        // 2. A CrashedBroadcast is sent.
        // 3. The CameraService terminates.

        try {
            Message message = messageBus.awaitMessage(cameraService);
            assertTrue(message instanceof CrashedBroadcast);

            CrashedBroadcast broadcast = (CrashedBroadcast) message;
            assertEquals("CameraService1", broadcast.getSender());
            assertEquals("Critical Error", broadcast.getDescription());
            assertEquals(STATUS.ERROR, camera.getStatus());
        } catch (InterruptedException e) {
            fail("CameraService should not be interrupted while waiting for a message.");
        }

        // Invariants:
        // 1. The camera cannot process any further events or broadcasts after entering the ERROR state.
        // 2. The CrashedBroadcast must include accurate error details.
    }
}