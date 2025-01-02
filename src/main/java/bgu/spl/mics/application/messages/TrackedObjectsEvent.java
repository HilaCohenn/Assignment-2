package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import java.util.List;
import bgu.spl.mics.application.objects.TrackedObject;


public class TrackedObjectsEvent implements Event<Boolean> {
    private List<TrackedObject> trackedObjects;
    private String senderName;

    public TrackedObjectsEvent(String senderName, List<TrackedObject> trackedObjects){
        this.trackedObjects = trackedObjects;
        this.senderName = senderName;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }
    public String getsenderName() {
        return senderName;
    }

}
