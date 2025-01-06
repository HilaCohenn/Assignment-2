package bgu.spl.mics.application.objects;
import java.util.List;
import java.util.ArrayList;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private final int id;
    private final int frequency;
    public STATUS status;
    private List<TrackedObject> lastTrackedObjects;
    private LiDarDataBase LiDarDataBase;
    private boolean addedAll = false;


    public LiDarWorkerTracker(int id, int frequency, LiDarDataBase LiDarDataBase) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.LiDarDataBase = LiDarDataBase;
        this.lastTrackedObjects = new ArrayList<>();
        
    }

    public int getId() {
        return id;
    }

    public List<TrackedObject> getTrackedObjectsbyTime(int time){
        // find and send the tracked objects at the given time 
        // remove from the list
        if(addedAll&&lastTrackedObjects.isEmpty()){
            this.status = STATUS.DOWN;
        }
        List<TrackedObject> currentTime = new ArrayList<>();
        List<TrackedObject> copyTrackedObjects = new ArrayList<>(this.lastTrackedObjects);
        for (TrackedObject trackedObject : copyTrackedObjects) {
            if (trackedObject.getTime() <= time - this.frequency) {
                currentTime.add(trackedObject);
                lastTrackedObjects.remove(trackedObject);
            }
        }
        return currentTime;
    }

    public void addToLastTrackedObjects(StampedDetectedObjects detectedObjects){
        // adds the detected objects to the lastTrackedObjects list
        for (DetectedObject object : detectedObjects.getDetectedObjects()) {
            StampedCloudPoints cloudPoints = this.LiDarDataBase.getCloudPoint(object, detectedObjects.getTime());
            if (cloudPoints == null) {
                System.out.println("cloudPoints ");;
            }
            
            TrackedObject trackedObject = new TrackedObject(object.getId(), detectedObjects.getTime(), object.getDescription(), cloudPoints.getCloudPoints());
            lastTrackedObjects.add(trackedObject);
            StampedCloudPoints lastCloudPoint = this.LiDarDataBase.getlast();
            if(trackedObject.getTime() == lastCloudPoint.getTime()&&trackedObject.getId().equals(lastCloudPoint.getId())){
                addedAll = true;
            }
        }
    }

 
    
}
