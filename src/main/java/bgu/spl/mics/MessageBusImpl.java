package bgu.spl.mics;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
    private static class SingletonHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }
    private final ConcurrentMap<MicroService, BlockingQueue<Message>> microServiceQueues;
	private final ConcurrentMap<Class<? extends Message>, ConcurrentLinkedQueue<MicroService>> subscriptions;
	private final ConcurrentMap<Event<?>, Future<?>> eventFutures;
    private final ConcurrentMap<Class<? extends Event<?>>, AtomicInteger> roundRobinCounters;

	private MessageBusImpl() {
        microServiceQueues = new ConcurrentHashMap<>();
        subscriptions = new ConcurrentHashMap<>();
        eventFutures = new ConcurrentHashMap<>();
       	roundRobinCounters = new ConcurrentHashMap<>();
    }

	public static MessageBusImpl getInstance() {
        return SingletonHolder.instance;
    }

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        subscriptions.putIfAbsent(type, new ConcurrentLinkedQueue<>());
        subscriptions.get(type).add(m);
        roundRobinCounters.putIfAbsent(type, new AtomicInteger(0));
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        subscriptions.putIfAbsent(type, new ConcurrentLinkedQueue<>());
        subscriptions.get(type).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
        Future<T> future = (Future<T>) eventFutures.remove(e);
        if (future != null) {
            future.resolve(result);
        }
     }

	@Override
	public void sendBroadcast(Broadcast b) {
	    ConcurrentLinkedQueue<MicroService> microServices = subscriptions.get(b.getClass());
        if (microServices != null) {
            for (MicroService m : microServices) {
                microServiceQueues.get(m).add(b);
            }
        }
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
     ConcurrentLinkedQueue<MicroService> microServices = subscriptions.get(e.getClass());
     if (microServices == null || microServices.isEmpty()) {
         return null;
     }
     int index = roundRobinCounters.get(e.getClass()).getAndIncrement() % microServices.size();
     MicroService m = microServices.stream().skip(index).findFirst().orElse(null);
     if (m != null) {
        microServiceQueues.get(m).add(e);
        Future<T> future = new Future<>();
        eventFutures.put(e, future);
        return future;
     }
     return null;
	}

	@Override
	public void register(MicroService m) {
		microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());

	}

	@Override
	public synchronized void unregister(MicroService m) {
        BlockingQueue<Message> queue = microServiceQueues.remove(m);
        subscriptions.values().forEach(queues -> queues.remove(m));
		if (queue != null) {
        for (Message message : queue) {
            if (message instanceof Event) {
                Future<?> future = eventFutures.remove(message);
                if (future != null) {
                    future.resolve(null); // Resolve with null or an appropriate default value
                }
            }
        }
    }
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
        BlockingQueue<Message> queue = microServiceQueues.get(m);
        if (queue == null) {
            throw new IllegalStateException("MicroService is not registered");
        }
        return queue.take();
	}

	

}
