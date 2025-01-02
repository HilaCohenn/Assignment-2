package bgu.spl.mics.application.objects;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    // TODO: Define fields and methods.

    private final String Id;
    private final String description;
    private List<CloudPoint> coordinates;

    public LandMark(String Id, String description, List<CloudPoint> coordinates){
        this.Id = Id;
        this.description = description;
        this.coordinates = coordinates;

        // work on turning TrackedObject into a LandMark
    }



    public String getId() {
        return Id;
    }

    public String getDescription() {
        return description;
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<CloudPoint> coordinates) {
        this.coordinates = coordinates;
    }


}
