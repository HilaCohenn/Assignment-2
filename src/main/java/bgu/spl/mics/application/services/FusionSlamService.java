package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
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
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam;
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
        });

        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            // TODO Implement this
        });

    }
}
