package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bgu.spl.mics.application.messages.PoseEvent;

public class ErrorData {
    private String error;
    private String faultySensor;
    private Map<String, StampedDetectedObjects> camaraFrames;
    private Map<String,List<TrackedObject>> lidarDetection;
    private List<Pose> poses;

    public ErrorData() {
        this.error = null;
        this.faultySensor = null;
        this.camaraFrames = new HashMap<String, StampedDetectedObjects>();
        this.lidarDetection = new HashMap<String,List<TrackedObject>>();
        this.poses = new ArrayList<>();
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setFaultySensor(String faultySensor) {
        this.faultySensor = faultySensor;
    }

    public void addCamaraFrame(String camaraId, StampedDetectedObjects detectedObjects) {
        this.camaraFrames.put(camaraId, detectedObjects);
    }

    public void addLidarDetection(String lidarId, List<TrackedObject> trackedObjects) {
        this.lidarDetection.put(lidarId, trackedObjects);
    }

    public void addPose(Pose pose) {
        this.poses.add(pose);
    }

    public String getError() {
        return this.error;
    }

    public String getFaultySensor() {
        return this.faultySensor;
    }

    public List<Pose> getPose() {
        return this.poses;
    }

    public Map<String, StampedDetectedObjects> getCamaraFrames() {
        return this.camaraFrames;
    }

    public Map<String,List<TrackedObject>> getLidarDetection() {
        return this.lidarDetection;
    }

}
