package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;


/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    // Singleton instance holder

    private static class SingletonHolder {
            private static FusionSlam instance = new FusionSlam();
    }
    private List<LandMark> landmarks;
    private List<Pose> poses;
    private List<TrackedObject> toBeProcessed;
    private StatisticalFolder statistics;
    
    private FusionSlam(){
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
        this.statistics = null;
    }

    public static FusionSlam getInstance(StatisticalFolder statistics) {
        FusionSlam inst= SingletonHolder.instance;
        inst.setStatistics(statistics);
        return inst;
    }

    private void setStatistics(StatisticalFolder statistics){
        if(this.statistics == null){
            this.statistics = statistics;
        }
    }   

    public void processLandMark(List<TrackedObject> trackedObjects){
        for(TrackedObject trackedObject : trackedObjects){
            if (!landMarkExists(trackedObject.getId())){
                addLandMark(trackedObject);
            }
            else {
                updateLandmark(trackedObject);
            }
        }
    }

    public boolean landMarkExists(String id){
        for (LandMark landMark : landmarks){
            if (landMark.getId().equals(id)){
                return true;
            }
        }
        return false;
    }

    public void updateLandmark(TrackedObject trackedObject){
        for (LandMark landMark : landmarks){
            if (landMark.getId().equals(trackedObject.getId())){
                updateCoordinates(landMark, trackedObject.getCloudPoints());
                Pose pose = getPoseByTime(trackedObject.getTime());
                if (pose == null){
                    toBeProcessed.add(trackedObject);
                    return;
                }
                landMark.setCoordinates(coordinateTransformer(trackedObject.getCloudPoints(), pose));
            }
        }
    }

    public void addLandMark(TrackedObject trackedObject){
        Pose pose = getPoseByTime(trackedObject.getTime());
        if (pose == null){
            toBeProcessed.add(trackedObject);
            return;
        }
        LandMark landMark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), coordinateTransformer(trackedObject.getCloudPoints(), pose));
        landmarks.add(landMark);
        this.statistics.addToLandMark(1);
    }

    public List<CloudPoint> updateCoordinates(LandMark landmark, List<CloudPoint> coordinates) {
        List<CloudPoint> newCoordinates = new ArrayList<>();
        for (CloudPoint cloudPoint : coordinates){
            for (CloudPoint points : landmark.getCoordinates()){
                cloudPoint.setX((cloudPoint.getX()+points.getX())/2);
                cloudPoint.setY((cloudPoint.getY()+points.getY())/2);
        }
    }
    return newCoordinates;
}

    public Pose getPoseByTime (int time){
        for (Pose pose : poses){
            if (pose.getTime() == time){
                return pose;
            }
        }
        return null;
    }

    public void processPose (Pose pose){
        private List<TrackedObject> newLandMarks = new ArrayList<>();
        for (TrackedObject trackedObject : toBeProcessed){
            if (trackedObject.getTime() == pose.getTime()){
                newLandMarks.add(trackedObject);
                toBeProcessed.remove(trackedObject);
            }
        }
        processLandMark(newLandMarks);
        poses.add(pose);
    }

    public static List<CloudPoint> coordinateTransformer(List<CloudPoint> cloudPoints, Pose pose) {
        List<CloudPoint> transformedPoints = new ArrayList<>();

        // Extract the pose details
        double robotX = pose.getX();
        double robotY = pose.getY();
        double yawRadians = Math.toRadians(pose.getYaw());

        // Compute cosine and sine of the yaw angle
        double cosYaw = Math.cos(yawRadians);
        double sinYaw = Math.sin(yawRadians);

        // Transform each CloudPoint
        for (CloudPoint point : cloudPoints) {
            double localX = point.getX();
            double localY = point.getY();

            // Apply the transformation
            double globalX = (cosYaw * localX) - (sinYaw * localY) + robotX;
            double globalY = (sinYaw * localX) + (cosYaw * localY) + robotY;

            // Create a new CloudPoint for the transformed coordinate
            transformedPoints.add(new CloudPoint(globalX, globalY));
        }

        return transformedPoints;
    }

    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    public List<Pose> getPoses() {
        return poses;
    }
    
}




