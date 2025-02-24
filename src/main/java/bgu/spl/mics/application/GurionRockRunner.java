package bgu.spl.mics.application;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import bgu.spl.mics.*;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        // TODO: Parse configuration file.
        // TODO: Initialize system components and services.
        // TODO: Start the simulation.

        String configFilePath = args[0];
        String folderPath = new File(configFilePath).getParent();
        StatisticalFolder statistics = new StatisticalFolder();
        FusionSlam fusionSlam = FusionSlam.getInstance(statistics);
        ErrorData errorData = new ErrorData();

        //init camara
        List<Camera> cameras = new ArrayList<>();
        JsonObject robotObject = FileReaderUtil.readJson(configFilePath);
        JsonArray camerasArray = robotObject.getAsJsonObject("Cameras").getAsJsonArray("CamerasConfigurations");
        String cameraDataPath = Paths.get(folderPath, robotObject.getAsJsonObject("Cameras").get("camera_datas_path").getAsString()).normalize().toString();
         for (int i = 0; i < camerasArray.size(); i++) {
            JsonObject cameraConfig = camerasArray.get(i).getAsJsonObject();
            int id = cameraConfig.get("id").getAsInt();
            int frequency = cameraConfig.get("frequency").getAsInt();
            String key = cameraConfig.get("camera_key").getAsString();

            //create camera
            Camera camera = new Camera(id, frequency,key, cameraDataPath);
            cameras.add(camera);
         }

        //init lidars
        List<LiDarWorkerTracker> lidars = new ArrayList<>();
        JsonArray LidarsArray = robotObject.getAsJsonObject("LidarWorkers").getAsJsonArray("LidarConfigurations");


        String lidarDataString =Paths.get(folderPath, robotObject.getAsJsonObject("LidarWorkers").get("lidars_data_path").getAsString()).normalize().toString();


        LiDarDataBase lidarDataBase = LiDarDataBase.getInstance(lidarDataString);

        for (int i = 0; i < LidarsArray.size(); i++) {
            JsonObject lidarConfig = LidarsArray.get(i).getAsJsonObject();
            int id = lidarConfig.get("id").getAsInt();
            int frequency = lidarConfig.get("frequency").getAsInt();

            //create lidarWorker
            LiDarWorkerTracker lidar = new LiDarWorkerTracker(id, frequency,lidarDataBase);
            lidars.add(lidar);
         }

         //init gpsimu
         String poseJsonFile = Paths.get(folderPath, robotObject.get("poseJsonFile").getAsString()).normalize().toString();
            GPSIMU gpsimu = new GPSIMU(poseJsonFile);

        int duration = robotObject.get("Duration").getAsInt();
        int tickTime = robotObject.get("TickTime").getAsInt();

        CountDownLatch latch = new CountDownLatch(cameras.size() + lidars.size() + 1);
        
        //init services
        List<MicroService> camServices = new ArrayList<>();
        for (Camera camera : cameras) {
            CameraService cameraService = new CameraService(camera, statistics,latch,errorData);
            camServices.add(cameraService);
        }

        List<LiDarService> lidarServices = new ArrayList<>();
        for (LiDarWorkerTracker lidar : lidars) {
            LiDarService lidarService = new LiDarService(lidar, statistics,latch,errorData);
            lidarServices.add(lidarService);
        }

        PoseService poseService = new PoseService(gpsimu,latch,errorData);
        TimeService timeService = new TimeService(tickTime, duration, statistics,latch);


        List<Thread> threads = new ArrayList<>();
        for (MicroService service : camServices) {
            Thread thread = new Thread(service);
            threads.add(thread);
        }
        for (MicroService service : lidarServices) {
            Thread thread = new Thread(service);
            threads.add(thread);
        }
        threads.add(new Thread(poseService));
        threads.add(new Thread(timeService));
        AtomicInteger numServices = new AtomicInteger(threads.size()-1);
        FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam, numServices,latch,errorData);
        threads.add(new Thread(fusionSlamService));
        // wait till all services are initialized
         
 
        //start all threads after all services are initialized
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Generate output file
        if(errorData.getError()!=null)
            ErrorOutPutHandler.generateErrorOutputFile(configFilePath, fusionSlam, statistics, errorData);
        else
        OutputHandler.generateOutputFile(configFilePath, fusionSlam, statistics);
    }
}
