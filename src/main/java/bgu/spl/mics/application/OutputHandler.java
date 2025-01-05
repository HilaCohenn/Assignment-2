package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class OutputHandler {

    public static void generateOutputFile(String configFilePath, FusionSlam fusionSlam, StatisticalFolder statistics) {
        // Determine the output file path
        String outputFilePath = Paths.get(new File(configFilePath).getParent(), "output_file.json").toString();

        // Collect data
        JsonObject outputJson = collectData(fusionSlam, statistics);

        // Write to output file
        writeToFile(outputJson, outputFilePath);
    }

    private static JsonObject collectData(FusionSlam fusionSlam, StatisticalFolder statistics) {
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