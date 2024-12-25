package bgu.spl.mics.application.objects;

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

private static int idCounter=0;
private final int id;
private final int frequency;
private Status status;
private final List<Stamped<DetectedObject>> detectedObjectsList;

/**
 * Constructor for Camera.
 *
 * @param frequency The time interval at which the camera sends new events.
 */ 
public Camera(int frequency) {
    this.id = ++idCounter;
    this.frequency = frequency;
    this.status = Status.UP;
    this.detectedObjectsList = new ArrayList<>();

}

public int getId() {
    return id;
}


public Status getStatus() {
    return status;

}

public  List<Stamped<DetectedObject>> getDetectedObjectsbyTime(int time) {
    List<Stamped<DetectedObject>> detectedObjects = new ArrayList<>();
    for (Stamped<DetectedObject> detectedObject : detectedObjectsList) {
        if (detectedObject.getTime() <= time || detectedObject.getTime() >= time - frequency) {
            detectedObjects.add(detectedObject);
        }
    }
    return detectedObjects;
    
}
