package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GurionRockRunner {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java GurionRockRunner <config-file-path>");
            System.exit(1);
        }

        String configFilePath = args[0];
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            System.err.println("Configuration file not found: " + configFilePath);
            System.exit(1);
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
            String folderPath = configFile.getParent();

            // Debugging: Print the configuration file content
            System.out.println("Configuration: " + config.toString());

            // Initialize components
            JsonObject robotObject = config.getAsJsonObject("robot");
            if (robotObject == null) {
                throw new IllegalArgumentException("Missing 'robot' configuration");
            }

            String poseJsonFile = robotObject.has("poseJsonFile") ? 
                Paths.get(folderPath, robotObject.get("poseJsonFile").getAsString()).normalize().toString() : null;
            if (poseJsonFile == null) {
                throw new IllegalArgumentException("Missing 'poseJsonFile' in 'robot' configuration");
            }
            GPSIMU gpsimu = new GPSIMU(poseJsonFile);

            int duration = robotObject.has("Duration") ? robotObject.get("Duration").getAsInt() : 0;
            int tickTime = robotObject.has("TickTime") ? robotObject.get("TickTime").getAsInt() : 0;
            if (duration == 0 || tickTime == 0) {
                throw new IllegalArgumentException("Missing 'Duration' or 'TickTime' in 'robot' configuration");
            }

            // Initialize cameras
            List<Camera> cameras = new ArrayList<>();
            JsonObject camerasObject = robotObject.getAsJsonObject("Cameras");
            if (camerasObject == null) {
                throw new IllegalArgumentException("Missing 'Cameras' configuration");
            }
            JsonArray camerasArray = camerasObject.getAsJsonArray("CamerasConfigurations");
            String cameraDataPath = Paths.get(folderPath, camerasObject.get("camera_datas_path").getAsString()).normalize().toString();
            for (int i = 0; i < camerasArray.size(); i++) {
                JsonObject cameraConfig = camerasArray.get(i).getAsJsonObject();
                int id = cameraConfig.get("id").getAsInt();
                int frequency = cameraConfig.get("frequency").getAsInt();
                String cameraKey = cameraConfig.get("camera_key").getAsString();
                Camera camera = new Camera(id, frequency, cameraKey, cameraDataPath);
                cameras.add(camera);
            }

            // Initialize lidars
            List<LiDarWorkerTracker> lidars = new ArrayList<>();
            JsonArray lidarsArray = robotObject.getAsJsonArray("LidarConfigurations");
            String lidarDataString = Paths.get(folderPath, robotObject.get("lidars_data_path").getAsString()).normalize().toString();
            LiDarDataBase lidarDataBase = LiDarDataBase.getInstance(lidarDataString);
            for (int i = 0; i < lidarsArray.size(); i++) {
                JsonObject lidarConfig = lidarsArray.get(i).getAsJsonObject();
                int id = lidarConfig.get("id").getAsInt();
                int frequency = lidarConfig.get("frequency").getAsInt();
                LiDarWorkerTracker lidar = new LiDarWorkerTracker(id, frequency, lidarDataBase);
                lidars.add(lidar);
            }

            StatisticalFolder statistics = new StatisticalFolder();
            FusionSlam fusionSlam = FusionSlam.getInstance(statistics);

            List<MicroService> camServices = new ArrayList<>();
            for (Camera camera : cameras) {
                CameraService cameraService = new CameraService(camera, statistics);
                camServices.add(cameraService);
            }

            List<LiDarService> lidarServices = new ArrayList<>();
            for (LiDarWorkerTracker lidar : lidars) {
                LiDarService lidarService = new LiDarService(lidar, statistics);
                lidarServices.add(lidarService);
            }

            // Start services
            for (MicroService service : camServices) {
                new Thread(service).start();
            }
            for (MicroService service : lidarServices) {
                new Thread(service).start();
            }

            // Run simulation
            // This is a placeholder for the actual simulation logic
            Thread.sleep(duration * tickTime);

            // Generate output file
            OutputHandler.generateOutputFile(configFilePath, fusionSlam, statistics);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}