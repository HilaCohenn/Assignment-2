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

    public void addLandMark(LandMark landMark){
        landmarks.add(landMark);
    }

    public void addPose(Pose pose){
        poses.add(pose);
    }
}

