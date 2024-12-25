package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;

public class TickBroadcast extends Broadcast{

    private final int tick;
    private final int TickTime;
    
    public TickBroadcast(int tick, int TickTime){
        this.tick = tick;
        this.TickTime = TickTime;
    }

    public int getTick(){
        return tick;
    }

    public int getTime(){
        return tick*TickTime;
    }    
}