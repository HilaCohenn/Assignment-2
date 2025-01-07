package bgu.spl.mics.application.services;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.ErrorData;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.messages.*;




/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {

    private FusionSlam fusionSlam;
    private AtomicInteger numServices;
    private AtomicInteger numServicesTerminated = new AtomicInteger(0);
    private CountDownLatch latch;
    private ErrorData errorData;
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam, AtomicInteger numServices, CountDownLatch latch, ErrorData errorData) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam;
        this.numServices = numServices;
        this.latch = latch;
        this.errorData = errorData;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        // TODO Implement this

        this.subscribeEvent(TrackedObjectsEvent.class, (TrackedObjectsEvent trackedObjectsEvent) -> {
            fusionSlam.processLandMark(trackedObjectsEvent.getTrackedObjects());
            complete(trackedObjectsEvent, true);
        });

        this.subscribeEvent(PoseEvent.class, (PoseEvent poseEvent) -> {
            fusionSlam.processPose (poseEvent.getPose());
            complete(poseEvent, true);
            
        });

        this.subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) -> {
            terminate();
            this.errorData.setError(crashed.getDescription());
            this.errorData.setFaultySensor(crashed.getSender());
        });

        this.subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminate) -> {
            if(!terminate.getSender().equals("TimeService"))
            {
                numServicesTerminated.incrementAndGet();
            }

            System.out.println(numServicesTerminated + " services terminated out of " + numServices);
            if (numServicesTerminated.get() == numServices.get()){
                System.out.println("FusionSlamService terminated");
                terminate();
            }
        });

        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            // TODO Implement this
        });
        latch.countDown();
    }
}
