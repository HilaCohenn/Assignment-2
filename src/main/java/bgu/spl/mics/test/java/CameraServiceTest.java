package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;

class CameraServiceTest {

    private Camera camera;
    private CameraService cameraService;
    private MessageBus messageBus;

    @BeforeEach
    void setUp() {
        camera = new Camera(1); // Camera with ID 1
        cameraService = new CameraService(camera);
        messageBus = MessageBusImpl.getInstance();
        messageBus.register(cameraService);
    }

    @Test
    void testHandleTickBroadcast() {
        // Preconditions:
        // 1. The camera status is set to operational (not ERROR or DOWN).
        // 2. The CameraService is subscribed to TickBroadcasts.

        camera.status = STATUS.OPERATIONAL;
        camera.addDetectedObjects(5, new StampedDetectedObjects(5, new DetectedObject("Object1", "Description1")));

        TickBroadcast tick = new TickBroadcast(5);
        messageBus.sendBroadcast(tick);

        // Postconditions:
        // 1. CameraService processes the broadcast.
        // 2. A DetectObjectsEvent is sent for detected objects.
        // 3. The detected object data is processed correctly.

        try {
            Message message = messageBus.awaitMessage(cameraService);
            assertTrue(message instanceof DetectObjectsEvent);

            DetectObjectsEvent event = (DetectObjectsEvent) message;
            assertEquals("CameraService1", event.getSource());
            assertNotNull(event.getStampedDetectedObjects());
            assertEquals(1, event.getStampedDetectedObjects().getDetectedObjects().size());
        } catch (InterruptedException e) {
            fail("CameraService should not be interrupted while waiting for a message.");
        }

        // Invariants:
        // 1. The CameraService must remain subscribed to TickBroadcasts until terminated.
        // 2. The message queue for CameraService should only contain relevant messages.
    }

    @Test
    void testHandleCrashedBroadcast() {
        // Preconditions:
        // 1. The camera is operational initially.
        // 2. The CameraService is subscribed to CrashedBroadcasts.

        camera.status = STATUS.OPERATIONAL;
        CrashedBroadcast crashedBroadcast = new CrashedBroadcast("CameraService1", "Critical failure detected");

        messageBus.sendBroadcast(crashedBroadcast);

        // Postconditions:
        // 1. The camera status is set to DOWN upon receiving the broadcast.
        // 2. The CameraService terminates correctly.

        try {
            cameraService.initialize();
            messageBus.awaitMessage(cameraService);
        } catch (IllegalStateException | InterruptedException e) {
            assertEquals(STATUS.DOWN, camera.status);
        }

        // Invariants:
        // 1. CameraService should not process any more messages after termination.
        // 2. The camera status should remain consistent after handling the broadcast.
    }

    @Test
    void testCameraErrorStatus() {
        // Preconditions:
        // 1. The camera is operational initially.
        // 2. Detected objects contain one with ID "ERROR".

        camera.addDetectedObjects(10, new StampedDetectedObjects(10, new DetectedObject("ERROR", "Critical Error")));

        TickBroadcast tick = new TickBroadcast(10);
        messageBus.sendBroadcast(tick);

        // Postconditions:
        // 1. The camera status is set to ERROR.
        // 2. A CrashedBroadcast is sent.
        // 3. The CameraService terminates.

        try {
            cameraService.initialize();
            messageBus.awaitMessage(cameraService);
        } catch (IllegalStateException | InterruptedException e) {
            assertEquals(STATUS.ERROR, camera.status);
        }

        // Invariants:
        // 1. The camera cannot process any further events or broadcasts after entering the ERROR state.
        // 2. The CrashedBroadcast must include accurate error details.
    }
}
