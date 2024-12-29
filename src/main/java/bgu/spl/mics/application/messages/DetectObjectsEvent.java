package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;


public class DetectObjectsEvent implements Event {
    private StampedDetectedObjects detectedObjects; 
    private int senderId;

    public DetectObjectsEvent(int senderId, StampedDetectedObjects detectedObjects){
        this.detectedObjects = detectedObjects;
        this.senderId = senderId;
    }

    public StampedDetectedObjects getDetectedObjects() {
        return detectedObjects;
    }
    public int getSenderId() {
        return senderId;
    }
  
//id or name?
}
