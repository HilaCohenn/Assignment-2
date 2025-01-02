package bgu.spl.mics.application.messages;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.Event;

public class PoseEvent implements Event<Boolean> {
    private int time;
    private Pose pose;
    //sender?
    public PoseEvent(int time, Pose pose){
        this.time = time;
        this.pose = pose;
    }
    public int getTime() {
        return time;
    }
    public Pose getPose() {
        return pose;
    }

}
