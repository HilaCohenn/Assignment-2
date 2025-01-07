package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
        messageBus.subscribeBroadcast(testBroadcast.getClass(), microService1);
        messageBus.sendBroadcast(testBroadcast);
        Message message = messageBus.awaitMessage(microService1);
        assertTrue(message instanceof Broadcast, "microService1 should receive a broadcast message");
        assertEquals(testBroadcast, message, "The received broadcast should be the testBroadcast");
    }

    @Test
    void testSubscribeAndSendEvent() throws InterruptedException {
        messageBus.subscribeEvent((Class<? extends Event<String>>) testEvent.getClass(), microService1);
        messageBus.sendEvent(testEvent);
        Message message = messageBus.awaitMessage(microService1);
        assertTrue(message instanceof Event, "microService1 should receive an event message");
        assertEquals(testEvent, message, "The received event should be the testEvent");
    }

    @Test
    void testComplete() {
        messageBus.subscribeEvent((Class<? extends Event<String>>) testEvent.getClass(), microService1);
        Future<String> future = messageBus.sendEvent(testEvent);
        assertNotNull(future, "The future object should not be null");
        messageBus.complete(testEvent, "Completed");
        assertTrue(future.isDone(), "The future object should be completed");
        assertEquals("Completed", future.get(), "The result of the future should be 'Completed'");
    }
}