package bgu.spl.mics.application.services;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.*;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;


/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private final Camera camera;
    private final ConcurrentHashMap<Event<?>,Future<?>> eventFutures;
    

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService" + camera.getId());
        this.camera = camera;
        this.eventFutures = new ConcurrentHashMap<>();
    
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int curentTime= tick.getTick();//getTime
            StampedDetectedObjects detectedObjects = camera.getDetectedObjectsbyTime(curentTime);
            if(detectedObjects != null){
                DetectObjectsEvent e = new DetectObjectsEvent(detectedObjects);
                eventFutures.put(e,sendEvent(e));
            }
            });
    //this.subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) -> {
    //this.camera.status=STATUS.DOWN;//check errors
    //terminate();
    //});
     // this.subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast terminate) -> {
       //     this.camera.status=STATUS.DOWN;
         //   terminate();
        //});
    }
}
