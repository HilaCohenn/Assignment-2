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

    public static void generateErrorOutputFile(String configFilePath, FusionSlam fusionSlam, StatisticalFolder statistics, String error, String faultySensor, Map<String, List<DetectedObject>> lastFrames, List<Pose> poses) {
        if (!errorOccurred) {
            synchronized (ErrorOutPutHandler.class) {
                if (!errorOccurred) {
                    errorOccurred = true;
                    // Determine the output file path
                    String outputFilePath = Paths.get(new File(configFilePath).getParent(), "output_file.json").toString();

                    // Collect data
                    JsonObject outputJson = collectData(fusionSlam, statistics, error, faultySensor, lastFrames, poses);

                    // Write to output file
                    writeToFile(outputJson, outputFilePath);
                }
            }
        }
    }

    private static JsonObject collectData(FusionSlam fusionSlam, StatisticalFolder statistics, String error, String faultySensor, Map<String, List<DetectedObject>> lastFrames, List<Pose> poses) {
        JsonObject outputJson = new JsonObject();

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

        // Add error information
        outputJson.addProperty("Error", error);
        outputJson.addProperty("faultySensor", faultySensor);

        // Add last frames
        JsonObject lastFramesJson = new JsonObject();
        JsonObject camerasJson = new JsonObject();
        JsonObject lidarJson = new JsonObject();
        for (Map.Entry<String, List<DetectedObject>> entry : lastFrames.entrySet()) {
            JsonArray detectedObjectsJson = new JsonArray();
            for (DetectedObject detectedObject : entry.getValue()) {
                JsonObject detectedObjectJson = new JsonObject();
                detectedObjectJson.addProperty("id", detectedObject.getId());
                detectedObjectJson.addProperty("description", detectedObject.getDescription());
                detectedObjectsJson.add(detectedObjectJson);
            }
            if (entry.getKey().startsWith("Camera")) {
                camerasJson.add(entry.getKey(), detectedObjectsJson);
            } else {
                lidarJson.add(entry.getKey(), detectedObjectsJson);
            }
        }
        lastFramesJson.add("cameras", camerasJson);
        lastFramesJson.add("lidar", lidarJson);
        outputJson.add("lastFrames", lastFramesJson);

        // Add poses
        JsonArray posesJson = new JsonArray();
        for (Pose pose : poses) {
            JsonObject poseJson = new JsonObject();
            poseJson.addProperty("x", pose.getX());
            poseJson.addProperty("y", pose.getY());
            poseJson.addProperty("yaw", pose.getYaw());
            posesJson.add(poseJson);
        }
        outputJson.add("poses", posesJson);

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