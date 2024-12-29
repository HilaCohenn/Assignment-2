package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;


public class DetectObjectsEvent implements Event {
    private StampedDetectedObjects detectedObjects;

    public DetectObjectsEvent(StampedDetectedObjects detectedObjects){
        this.detectedObjects = detectedObjects;
    }

    public StampedDetectedObjects getDetectedObjects() {
        return detectedObjects;
    }

}
