package bgu.spl.mics.application.objects;
import java.util.List;

import bgu.spl.mics.FileReaderUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    public STATUS status;
    private List<Pose> poses;

      public GPSIMU(String poseFilePath) {
        JsonArray jsonArray = FileReaderUtil.readJsonArray(poseFilePath); 
        if (jsonArray == null) {
           System.err.println("Error: Could not load poses from file.");
            this.status = STATUS.ERROR;
            return;
        }

        Gson gson = new Gson();
        for (JsonElement element : jsonArray) {
            Pose pose = gson.fromJson(element, Pose.class); 
            this.poses.add(pose);
        }

        if (poses.isEmpty()) {
            System.err.println("Error: Pose list is empty.");
            this.status = STATUS.ERROR;
        }
    }


    public void updateTick(int tick) {
        this.currentTick = tick;

        //check if it's the end of the poses = status down
        boolean tickExists = false;
        for (Pose pose : this.poses) {
            if (pose.getTime() == currentTick) {
                tickExists = true;
                break;
            }
        }
        if (!tickExists) {
            this.status = STATUS.DOWN;
        }
    }


    public Pose getCurrentPose() {
        if(this.status == STATUS.ERROR || this.status == STATUS.DOWN){
            return null;
        }
        for (Pose pose : this.poses) {
            if (pose.getTime() == currentTick) {
                return pose;
            }
        }
        return null;
    }
}
