package bgu.spl.mics.application.objects;



import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import bgu.spl.mics.FileReaderUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;



/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    //
// id: int – The ID of the camera.
// frequency: int – The time interval at which the camera sends new events (If the time in
// the Objects is 2 then the camera sends it at time 2 + Frequency).
// status: enum – Up, Down, Error.
// detectedObjectsList: List of Stamped DetectedObject – Time-stamped objects the
// camera detected.

private final int id;
private final int frequency;
public STATUS status;
private final List<StampedDetectedObjects> detectedObjectsList;

/**
 * Constructor for Camera.
 *
 * @param frequency The time interval at which the camera sends new events.
 */ 
public Camera(int id, int frequency,String path) {
    this.id = id;
    this.frequency = frequency;
    this.status = STATUS.UP;
    this.detectedObjectsList = new ArrayList<>();
    initDetectedObjects(path);
    
}
private void initDetectedObjects (String path){
    Gson gson = new Gson();
    JsonObject o = FileReaderUtil.readJson(path);
    String name = "camera" + this.id;
    // Check if the camera exists in the JSON object
    if (o.has(name)) {
        JsonArray cameraData = o.getAsJsonArray(name);
        // Iterate over the array of camera data
        for (int i = 0; i < cameraData.size(); i++) {
            JsonObject cameraEntry = cameraData.get(i).getAsJsonObject();
            int time = cameraEntry.get("time").getAsInt();
        // Get the detected objects and parse them into DetectedObject list
            Type objectListType = new TypeToken<List<DetectedObject>>() {}.getType();
            List<DetectedObject> detectedObjects = gson.fromJson(cameraEntry.getAsJsonArray("detectedObjects"), objectListType);
        // Create StampedDetectedObjects instance and add it to the list
            StampedDetectedObjects stampedDetectedObjects = new StampedDetectedObjects(time, detectedObjects);
            this.detectedObjectsList.add(stampedDetectedObjects);
        }
    }
}

public int getId() {
    return id;
}

public STATUS getStatus() {
    return status;

}

public  StampedDetectedObjects getDetectedObjectsbyTime(int time) {
    StampedDetectedObjects detectedObjects =null;
    for (StampedDetectedObjects detectedObject : this.detectedObjectsList) {
        if (detectedObject.getTime() == time - frequency) { 
            detectedObjects=detectedObject; 
        }
    }
    return detectedObjects;
    }
}