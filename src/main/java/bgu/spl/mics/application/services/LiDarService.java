package bgu.spl.mics.application.services;

import bgu.spl.mics.application.objects.ErrorData;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.*;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;
import java.util.List;
import bgu.spl.mics.application.messages.*;


/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    private final LiDarWorkerTracker LiDarWorkerTracker;
    private final ConcurrentHashMap<Event<?>,Future<?>> eventFutures;
    private StatisticalFolder statistics;
    private CountDownLatch latch;
    private List<TrackedObject> lastTrackedObject;
    private ErrorData errorData;
    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker,StatisticalFolder statistics, CountDownLatch latch,ErrorData errorData) {
        super("LiDarService" + LiDarWorkerTracker.getId());
        this.LiDarWorkerTracker = LiDarWorkerTracker;
        this.eventFutures = new ConcurrentHashMap<>();
        this.statistics = statistics;
        this.latch = latch;
        this.lastTrackedObject=new ArrayList<>();
        this.errorData = errorData;

    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            List<TrackedObject> recentObjects = LiDarWorkerTracker.getTrackedObjectsbyTime(tick.getTick());
            if(this.LiDarWorkerTracker.status==STATUS.DOWN){
                terminate();
            }
            if (recentObjects != null && !recentObjects.isEmpty()) {
                for(TrackedObject detect: recentObjects){
                    if(detect.getId().equals("ERROR"))
                    {
                        this.LiDarWorkerTracker.status=STATUS.ERROR;
                        sendBroadcast(new CrashedBroadcast(this.getName(), this.getName()+" disconnected"));
                        terminate();
                        this.errorData.addLidarDetection(getName(), recentObjects);
                    }
                }
                TrackedObjectsEvent e = new TrackedObjectsEvent(this.getName(), recentObjects);
                statistics.addToTrackedObjects(recentObjects.size());
                eventFutures.put(e,sendEvent(e));
                this.lastTrackedObject=recentObjects;
            }
        }
        );
        this.subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent detect) -> {
            LiDarWorkerTracker.addToLastTrackedObjects(detect.getDetectedObjects());
            complete(detect, true);
            
        });
        this.subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) -> {
            this.LiDarWorkerTracker.status=STATUS.DOWN;
            terminate();
        });

        this.subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminates) -> {
            if(terminates.getSender().equals("TimeService")){
            this.LiDarWorkerTracker.status=STATUS.DOWN;
            terminate();
         }});
        latch.countDown();
    }
}
