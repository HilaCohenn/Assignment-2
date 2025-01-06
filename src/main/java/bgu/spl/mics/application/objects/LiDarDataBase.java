package bgu.spl.mics.application.objects;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;


import java.io.FileReader;
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
        Gson gson = new Gson();
        try {
        FileReader lidars = new FileReader(filePath);
        Type listType = new TypeToken<List<StampedCloudPoints>>(){}.getType();
        List<JsonObject> jsonObjects = gson.fromJson(lidars, listType);
        for (JsonObject jsonObject : jsonObjects) {
            int time = jsonObject.get("time").getAsInt();
            String id = jsonObject.get("id").getAsString();
            JsonArray cloudPointsdata = jsonObject.getAsJsonArray("cloudPoints");
            List<CloudPoint> points = new ArrayList<>();
                for (JsonElement p : cloudPointsdata) {
                JsonArray pointArray = p.getAsJsonArray();
                    double x = pointArray.get(0).getAsDouble();
                    double y = pointArray.get(1).getAsDouble();
                    points.add(new CloudPoint(x, y));
                }
                this.cloudPoints.add(new StampedCloudPoints(time,id,points));
            }
        } catch (Exception e) {
        }
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

