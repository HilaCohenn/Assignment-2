 package bgu.spl.mics.application.objects;

 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import static org.junit.jupiter.api.Assertions.*;
 import static org.junit.jupiter.api.Assertions.assertTrue;
 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.junit.jupiter.api.Assertions.assertNotNull;



 class MessageBusImplTest {

     private MessageBusImpl messageBus;
     private MicroService microService1;
     private MicroService microService2;
     private Event<String> testEvent;
     private Broadcast testBroadcast;

     @BeforeEach
     void setUp() {
         messageBus = MessageBusImpl.getInstance();
         microService1 = new MicroService("TestService1") {
             @Override
             protected void initialize() {
             }
         };
         microService2 = new MicroService("TestService2") {
             @Override
             protected void initialize() {
             }
         };
         testEvent = new Event<String>() {
         };
         testBroadcast = new Broadcast() {
         };

         messageBus.register(microService1);
         messageBus.register(microService2);
     }

     @Test
     void testSubscribeAndSendBroadcast() throws InterruptedException {
         // Preconditions:
         // 1. microService1 and microService2 are registered.
         // 2. microService1 is subscribed to the testBroadcast.

         messageBus.subscribeBroadcast(testBroadcast.getClass(), microService1);

         // Send the broadcast
         messageBus.sendBroadcast(testBroadcast);

         // Postconditions:
         // 1. microService1 should receive the broadcast.
         // 2. microService2 should not receive the broadcast (since it's not subscribed).

         Message message = messageBus.awaitMessage(microService1);
         assertTrue(message instanceof Broadcast, "microService1 should receive a broadcast message");
         assertEquals(testBroadcast, message, "The received broadcast should be the testBroadcast");

         // Invariants:
         // 1. microService1 remains subscribed to testBroadcast.
         // 2. The message queue for microService1 should only contain relevant messages.
     }

     @Test
     void testSubscribeAndSendEvent() throws InterruptedException {
         // Preconditions:
         // 1. microService1 and microService2 are registered.
         // 2. microService1 is subscribed to the testEvent.

         messageBus.subscribeEvent(testEvent.getClass(), microService1);

         // Send the event
         messageBus.sendEvent(testEvent);

         // Postconditions:
         // 1. microService1 should receive the event.
         // 2. microService2 should not receive the event (since it's not subscribed).

         Message message = messageBus.awaitMessage(microService1);
         assertTrue(message instanceof Event, "microService1 should receive an event message");
         assertEquals(testEvent, message, "The received event should be the testEvent");

         // Invariants:
         // 1. microService1 remains subscribed to testEvent.
         // 2. The message queue for microService1 should only contain relevant messages.
     }

     @Test
     void testComplete() {
         // Preconditions:
         // 1. microService1 and microService2 are registered.
         // 2. microService1 is subscribed to the testEvent.

         messageBus.subscribeEvent(testEvent.getClass(), microService1);
         messageBus.sendEvent(testEvent);

         // Postconditions:
         // 1. The future object associated with the event should be resolved.

         Future<String> future = messageBus.sendEvent(testEvent);
         assertNotNull(future, "The future object should not be null");

         messageBus.complete(testEvent, "Completed");

         assertTrue(future.isDone(), "The future object should be completed");
         assertEquals("Completed", future.get(), "The result of the future should be 'Completed'");

         // Invariants:
         // 1. The event should be marked as completed.
         // 2. The message queue for microService1 should be empty after completion.
     }
 }