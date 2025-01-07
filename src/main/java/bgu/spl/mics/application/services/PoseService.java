package bgu.spl.mics.application.services;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.*;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.ErrorData;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private GPSIMU gpsimu;
    private final ConcurrentHashMap<Event<?>,Future<?>> eventFutures;
    private CountDownLatch latch;
    private ErrorData errorData;
    
    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu, CountDownLatch latch, ErrorData errorData) {
        super("PoseService");
        this.gpsimu = gpsimu;
        this.eventFutures = new ConcurrentHashMap<>();
        this.latch = latch;
        this.errorData = errorData;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTime= tick.getTick();
            this.gpsimu.updateTick(currentTime);
            Pose poses = this.gpsimu.getCurrentPose();
            if(poses != null){
                PoseEvent e = new PoseEvent(currentTime, poses);
                eventFutures.put(e,sendEvent(e));
                this.errorData.addPose(poses);
            }
            if(this.gpsimu.status==STATUS.DOWN){
                terminate();
            }
        });

        this.subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) -> {
        this.gpsimu.status=STATUS.DOWN;//check errors
        terminate();
        });
        this.subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminates) -> {
        if(terminates.getSender().equals("TimeService")){
           this.gpsimu.status=STATUS.DOWN;
           terminate();
        }
        });
        latch.countDown();
    }
}
