package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private final int time;
    private final String id;
    private final List<CloudPoint> cloudPoints; // in the json file its 2 doubles - need to create a cloud point

    public StampedCloudPoints(int time,String id, List<CloudPoint> cloudPoints) {
        this.time = time;
        this.id = id;
        this.cloudPoints = cloudPoints;
    }

    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public List<CloudPoint> getCloudPoints() {
        return cloudPoints;
    }
}

