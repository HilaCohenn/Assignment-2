package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;


public class TrackedObjectsEvent implements Event {
    private List<TrackedObject> trackedObjects;
    private String senderName;

    public TrackedObjectsEvent(String senderName, List<TrackedObject> trackedObjects){
        this.lastTrackedObjects = trackedObjects;
        this.senderName = senderName;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }
    public String getsenderName() {
        return senderName;
    }

}
