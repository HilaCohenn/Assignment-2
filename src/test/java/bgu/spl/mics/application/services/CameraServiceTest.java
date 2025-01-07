// package bgu.spl.mics.application.services;

// import bgu.spl.mics.*;
// import bgu.spl.mics.application.messages.*;
// import bgu.spl.mics.application.objects.*;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import java.util.concurrent.CountDownLatch;
// import static org.junit.jupiter.api.Assertions.*;

// class CameraServiceTest {

//     private Camera camera;
//     private CameraService cameraService;
//     private MessageBus messageBus;
//     private CountDownLatch latch = new CountDownLatch(0);
//     private ErrorData errorData = new ErrorData();

//     @BeforeEach
//     void setUp() {
//         camera = new Camera(1, 2, "camera1", "src/test/example_input/camera_data.json");
//         cameraService = new CameraService(camera, new StatisticalFolder(), latch, errorData);
//         messageBus = MessageBusImpl.getInstance();
//         messageBus.register(cameraService);
//     }

//     @Test
//     void testHandleTickBroadcast() {
//         // Preconditions:
//         // 1. The camera status is set to UP (not ERROR or DOWN).
//         // 2. The CameraService is subscribed to TickBroadcasts.
//         assertEquals(STATUS.UP, camera.getStatus());
//         int currentTime = 6;
//         StampedDetectedObjects detectedObjects = camera.getDetectedObjectsbyTime(currentTime);
//         assertNotNull(detectedObjects);
//         assertFalse(detectedObjects.getDetectedObjects().isEmpty(), "Detected objects should not be empty");
//         TickBroadcast tick = new TickBroadcast(currentTime);
//         messageBus.sendBroadcast(tick);

//         // Postconditions:
//         // 1. CameraService processes the broadcast.
//         // 2. A DetectObjectsEvent is sent for detected objects.
//         // 3. The detected object data is processed correctly.

//         try {
//             Message message = messageBus.awaitMessage(cameraService);
//             assertTrue(message instanceof DetectObjectsEvent);

//             DetectObjectsEvent event = (DetectObjectsEvent) message;
//             assertNotNull(event.getDetectedObjects());
//             assertEquals(detectedObjects.getDetectedObjects().size(), event.getDetectedObjects().getDetectedObjects().size());
//         } catch (InterruptedException e) {
//             fail("CameraService should not be interrupted while waiting for a message.");
//         }

//         // Invariants:
//         // 1. The CameraService must remain subscribed to TickBroadcasts until terminated.
//         // 2. The message queue for CameraService should only contain relevant messages.
//     }

//     @Test
//     void testCameraErrorStatus() {
//         int currentTime = 10;
//         StampedDetectedObjects detectedObjects = camera.getDetectedObjectsbyTime(currentTime);
//         assertNotNull(detectedObjects);
//         assertFalse(detectedObjects.getDetectedObjects().isEmpty(), "Detected objects should not be empty");
//         detectedObjects.getDetectedObjects().add(new DetectedObject("ERROR", "Critical Error"));
//         TickBroadcast tick = new TickBroadcast(currentTime);
//         messageBus.sendBroadcast(tick);

//         // Postconditions:
//         // 1. CameraService processes the broadcast.
//         // 2. A CrashedBroadcast is sent for the error.
//         // 3. The error data is processed correctly.

//         try {
//             Message message = messageBus.awaitMessage(cameraService);
//             assertTrue(message instanceof CrashedBroadcast);

//             CrashedBroadcast broadcast = (CrashedBroadcast) message;
//             assertEquals("CameraService1", broadcast.getSender());
//             assertEquals("Critical Error", broadcast.getDescription());
//             assertEquals(STATUS.ERROR, camera.getStatus());
//         } catch (InterruptedException e) {
//             fail("CameraService should not be interrupted while waiting for a message.");
//         }

//         // Invariants:
//         // 1. The CameraService must remain subscribed to TickBroadcasts until terminated.
//         // 2. The message queue for CameraService should only contain relevant messages.
//     }
// }