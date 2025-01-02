package bgu.spl.mics.application.objects;

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
    
    private FusionSlam(){
        landmarks = new ArrayList<>();
        poses = new ArrayList<>();
    }

    public static FusionSlam getInstance() {
        return SingletonHolder.instance;
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

    public static boolean landMarkExists(String id){
        for (LandMark landMark : landmarks){
            if (landMark.getId().equals(id)){
                return true;
            }
        }
        return false;
    }

    public static void updateLandmark(TrackedObject trackedObject){
        for (LandMark landMark : landmarks){
            if (landMark.getId().equals(trackedObject.getId())){
                updateCoordinates(landMark, trackedObject.getCloudPoints());
                landMark.setCoordinates(CoordinateTransformer(trackedObject.getCoordinates()));
            }
        }
    }

    public static void addLandMark(TrackedObject trackedObject){
        Pose pose = getPoseByTime(trackedObject.getTime());
        LandMark landMark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), CoordinateTransformer(trackedObject.getCoordinates(), pose));
        landmarks.add(landMark);


    }

    public List<CloudPoint> updateCoordinates(LandMark landmark, List<CloudPoint> coordinates) {
        List<CloudPoint> landmarks = new ArrayList<>();
        for (CloudPoint cloudPoint : coordinates){
            for (CloudPoint points : landmark.getCoordinates()){
                cloudPoint.setX((cloudPoint.getX()+points.getX())/2);
                cloudPoint.setY((cloudPoint.getY()+points.getY())/2);
        }
    }

    public Pose getPoseByTime (int time){
        for (Pose pose : poses){
            if (pose.getTime() == time){
                return pose;
            }
        }
        return null;
    }
    public void addPose(Pose pose){
        poses.add(pose);
    }

    public static List<CloudPoint> CoordinateTransformer(List<CloudPoint> cloudPoints, Pose pose) {
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
}

}

