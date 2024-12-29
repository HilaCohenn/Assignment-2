package bgu.spl.mics;



import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public  class FileReaderUtil<T>{


    public static JsonObject readJson(String filePath) {
        try(FileReader reader=new FileReader(filePath)){
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            return jsonObject;
        }
        catch (Exception e) {
            System.out.println("Error reading file: " + filePath);
        }
        return null;
    }

    public static JsonArray readJsonArray(String filePath) {
        try(FileReader reader=new FileReader(filePath)){
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            return jsonArray;
        }
        catch (Exception e) {
            System.out.println("Error reading file: " + filePath);
        }
        return null;
    }

     public static void writeJson(String json, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Convert object to JSON and write to file
            writer.write(json);
        } catch (IOException e) {
            System.out.println("Error writing file: " + filePath);
        }
    }


}