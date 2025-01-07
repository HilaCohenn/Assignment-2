package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.MicroService;
/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    int TickTime;
    int Duration;
    boolean isTerminated=false;
    StatisticalFolder statistics;
    CountDownLatch latch;
    

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration, StatisticalFolder statistics,CountDownLatch latch) {
        super("TimeService");
        this.TickTime = TickTime *100 ;
        this.Duration = Duration;
        this.statistics = statistics;
        this.latch = latch;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        try{
        latch.await();
        }
        catch (InterruptedException e) {
            //e.printStackTrace();
        }
        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            try{
            int currentTime= tick.getTick();
            Thread.sleep(TickTime);
            sendBroadcast(new TickBroadcast(currentTime+1)); 
            statistics.setsystemRuntime(currentTime+1);
            if(currentTime+1==Duration){
                terminate();
            }
            }
            catch (InterruptedException e) {
           // e.printStackTrace();
            }
        });
        this.subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) -> {
        terminate();
        });

        this.subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminates) -> {
           if(terminates.getSender().equals("TimeService")||terminates.getSender().equals("FusionSlamService"))
               terminate();
        });
        try{
        Thread.sleep(TickTime);
        //check that everybody is ready
        sendBroadcast(new TickBroadcast(1));
        statistics.setsystemRuntime(1);        }
        catch (InterruptedException e) {
            //e.printStackTrace();
       }
    }
}


 
