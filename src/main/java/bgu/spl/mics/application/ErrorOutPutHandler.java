package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.io.File;
import java.util.List;
import java.util.Map;

public class ErrorOutPutHandler {

    private static volatile boolean errorOccurred = false;

    public static void generateErrorOutputFile(String configFilePath, FusionSlam fusionSlam, StatisticalFolder statistics, ErrorData errorData) {
        if (!errorOccurred) {
            synchronized (ErrorOutPutHandler.class) {
                if (!errorOccurred) {
                    errorOccurred = true;
                    // Determine the output file path
                    String outputFilePath = Paths.get(new File(configFilePath).getParent(), "output_file.json").toString();

                    // Collect data
                    JsonObject outputJson = collectData(fusionSlam, statistics, errorData);

                    // Write to output file
                    writeToFile(outputJson, outputFilePath);
                }
            }
        }
    }

    private static JsonObject collectData(FusionSlam fusionSlam, StatisticalFolder statistics, ErrorData errorData) {
        JsonObject outputJson = new JsonObject();
        outputJson.addProperty("error", errorData.getError());
        outputJson.addProperty("faultySensor", errorData.getFaultySensor());
       
         // Add camera frames
         JsonObject cameraFramesJson = new JsonObject();
         for (Map.Entry<String, StampedDetectedObjects> entry : errorData.getCamaraFrames().entrySet()) {
             JsonObject frameJson = new JsonObject();
             frameJson.addProperty("time", entry.getValue().getTime());
             JsonArray detectedObjectsJson = new JsonArray();
             for (DetectedObject detectedObject : entry.getValue().getDetectedObjects()) {
                 JsonObject detectedObjectJson = new JsonObject();
                 detectedObjectJson.addProperty("id", detectedObject.getId());
                 detectedObjectJson.addProperty("description", detectedObject.getDescription());
                 detectedObjectsJson.add(detectedObjectJson);
             }
             frameJson.add("detectedObjects", detectedObjectsJson);
             cameraFramesJson.add(entry.getKey(), frameJson);
         }
         outputJson.add("cameraFrames", cameraFramesJson);

                 // Add lidar detections
        JsonObject lidarDetectionsJson = new JsonObject();
        for (Map.Entry<String, List<TrackedObject>> entry : errorData.getLidarDetection().entrySet()) {
            JsonArray trackedObjectsJson = new JsonArray();
            for (TrackedObject trackedObject : entry.getValue()) {
                JsonObject trackedObjectJson = new JsonObject();
                trackedObjectJson.addProperty("id", trackedObject.getId());
                trackedObjectJson.addProperty("description", trackedObject.getDescription());
                trackedObjectsJson.add(trackedObjectJson);
            }
            lidarDetectionsJson.add(entry.getKey(), trackedObjectsJson);
        }
        outputJson.add("lidarDetections", lidarDetectionsJson);

                // Add poses
                JsonArray posesJson = new JsonArray();
                for (Pose pose : errorData.getPose()) {
                    JsonObject poseJson = new JsonObject();
                    poseJson.addProperty("time", pose.getTime());
                    poseJson.addProperty("x", pose.getX());
                    poseJson.addProperty("y", pose.getY());
                    posesJson.add(poseJson);
                }
                outputJson.add("poses", posesJson);
        // Add system runtime
        outputJson.addProperty("systemRuntime", statistics.getSystemRuntime().get());

        // Add number of detected objects
        outputJson.addProperty("numDetectedObjects", statistics.getNumDetectedObjects().get());

        // Add number of tracked objects
        outputJson.addProperty("numTrackedObjects", statistics.getNumTrackedObjects().get());

        // Add number of landmarks
        outputJson.addProperty("numLandmarks", statistics.getNumLandmarks().get());
        // Add landmarks
        JsonObject landmarksJson = new JsonObject();
        for (LandMark landmark : fusionSlam.getLandmarks()) {
            JsonObject landmarkJson = new JsonObject();
            landmarkJson.addProperty("id", landmark.getId());
            landmarkJson.addProperty("description", landmark.getDescription());

            JsonArray coordinatesJson = new JsonArray();
            for (CloudPoint point : landmark.getCoordinates()) {
                JsonObject pointJson = new JsonObject();
                pointJson.addProperty("x", point.getX());
                pointJson.addProperty("y", point.getY());
                coordinatesJson.add(pointJson);
            }
            landmarkJson.add("coordinates", coordinatesJson);

            landmarksJson.add(landmark.getId(), landmarkJson);
        }
        outputJson.add("landMarks", landmarksJson);

                return outputJson;
    }

    private static void writeToFile(JsonObject jsonObject, String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            file.write(gson.toJson(jsonObject));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}