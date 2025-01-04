package bgu.spl.mics.application.objects;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.*;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private List<StampedCloudPoints> cloudPoints;

    private static class LiDarDataBaseHolder {
        private static LiDarDataBase instance;

        private static void instanceValue(String path) {
            instance = new LiDarDataBase(path);
        }
    }
    public static LiDarDataBase getInstance(String filePath) {
        if (LiDarDataBaseHolder.instance == null) {
            synchronized (LiDarDataBase.class) {
                if (LiDarDataBaseHolder.instance == null) {
                    LiDarDataBaseHolder.instanceValue(filePath);
                }
            }
        }
        return LiDarDataBaseHolder.instance;
    }
    private LiDarDataBase(String path) {
        this.cloudPoints = new ArrayList<>();
        initCloudPoints(path);
    }
    private void initCloudPoints(String filePath){
        JsonArray jsonArray = FileReaderUtil.readJson(filePath).getAsJsonArray();

        // Get the objects and parse them into cloudPoints list
        Gson gson = new Gson();
        Type objectListType = new TypeToken<List<StampedCloudPoints>>() {}.getType();
        this.cloudPoints = gson.fromJson(jsonArray, objectListType);
    }

    //get the cloud points of a specific object
    public StampedCloudPoints getCloudPoint(DetectedObject d,int time) {
            for (StampedCloudPoints cloudPoint : this.cloudPoints) {
            if (cloudPoint.getId().equals(d.getId())&&cloudPoint.getTime()==time) {
               return cloudPoint;
            }
        }
    return null;
}
    public StampedCloudPoints getlast()
    {
        return this.cloudPoints.get(this.cloudPoints.size()-1);
    }
}

