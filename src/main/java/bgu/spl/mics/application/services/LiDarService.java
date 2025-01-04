package bgu.spl.mics.application.services;

import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.*;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.concurrent.ConcurrentHashMap;
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
    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LiDarService" + LiDarWorkerTracker.getId());
        this.LiDarWorkerTracker = LiDarWorkerTracker;
        this.eventFutures = new ConcurrentHashMap<>();
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
            if (recentObjects != null) {
                for(TrackedObject detect: recentObjects){
                    if(detect.getId().equals("ERROR"))
                    {
                        this.LiDarWorkerTracker.status=STATUS.ERROR;
                        sendBroadcast(new CrashedBroadcast(this.getName(), detect.getDescription()));
                        terminate();
                    }
                }
                TrackedObjectsEvent e = new TrackedObjectsEvent(this.getName(), recentObjects);
                eventFutures.put(e,sendEvent(e));
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
            this.LiDarWorkerTracker.status=STATUS.DOWN;
            terminate();
         });
    }
}
