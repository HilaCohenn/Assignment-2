package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;


public class DetectObjectsEvent implements Event {
    private StampedDetectedObjects detectedObjects; 
    private String senderName;

    public DetectObjectsEvent(String senderName, StampedDetectedObjects detectedObjects){
        this.detectedObjects = detectedObjects;
        this.senderName = senderName;
    }

    public StampedDetectedObjects getDetectedObjects() {
        return detectedObjects;
    }
    public String getsenderName() {
        return senderName;
    }

}
