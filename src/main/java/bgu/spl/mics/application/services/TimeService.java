package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.MicroService;
/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    int TickTime;
    int Duration;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.TickTime = TickTime;
        this.Duration = Duration;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        for (int i = 1; i <= Duration; i++) {
            try {
                
                Thread.sleep(TickTime);
            } catch (InterruptedException e) {
                e.printStackTrace();//catch the exception Thread.currentThread().interrup
            }
            sendBroadcast(new TickBroadcast(i)); 
        }
        terminate();
    }
}
