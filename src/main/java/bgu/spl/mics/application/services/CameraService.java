package bgu.spl.mics.application.services;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.ErrorData;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.application.messages.*;

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
    private StatisticalFolder statistics;
    private CountDownLatch latch;
    private StampedDetectedObjects lastDetectedObjects;
    private ErrorData errorData;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera,StatisticalFolder statistics,CountDownLatch latch,ErrorData errorData) {
        super("CameraService" + camera.getId());
        this.camera = camera;
        this.eventFutures = new ConcurrentHashMap<>();
        this.statistics = statistics;
        this.latch=latch;
        this.lastDetectedObjects=null;
        this.errorData = errorData;
    
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int curentTime= tick.getTick();
            StampedDetectedObjects detectedObjects = camera.getDetectedObjectsbyTime(curentTime);
            if(camera.status==STATUS.DOWN)
            {
                terminate();
            }
            else
            {
            if(detectedObjects != null){
                for(DetectedObject detect: detectedObjects.getDetectedObjects()){
                    if(detect.getId().equals("ERROR"))
                    {
                        this.camera.status=STATUS.ERROR;
                        sendBroadcast(new CrashedBroadcast(this.getName(), detect.getDescription()));
                        terminate();
                        this.errorData.addCamaraFrame(this.getName(),this.lastDetectedObjects);
                    }
                }
                DetectObjectsEvent e = new DetectObjectsEvent(this.getName(),detectedObjects);
                statistics.addToDetectedObjcts(detectedObjects.getDetectedObjects().size());
                Future<?> future = sendEvent(e);
                if (future != null) {
                    eventFutures.put(e, future);
                }
                this.lastDetectedObjects=detectedObjects;
            }
        }
        });
        this.subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) -> {
        this.camera.status=STATUS.DOWN;
        terminate();
        });
        this.subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminates) -> {
           if(terminates.getSender().equals("TimeService")){ 
           this.camera.status=STATUS.DOWN;
           terminate();}
        });
        latch.countDown();
    }
}
