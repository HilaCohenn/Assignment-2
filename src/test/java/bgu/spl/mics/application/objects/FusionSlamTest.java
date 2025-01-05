package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    public void setUp() {
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.getLandmarks().clear(); // Clear existing landmarks
        fusionSlam.getPoses().clear(); // Clear existing poses
    }

    @Test
    public void testTransformTrackedObjectsToLandmarks() {
        // Preconditions:
        // 1. The landmarks list should be empty before processing.
        // 2. The poses list should be empty before processing.
        assertTrue(fusionSlam.getLandmarks().isEmpty(), "Landmarks list should be empty before processing");
        assertTrue(fusionSlam.getPoses().isEmpty(), "Poses list should be empty before processing");

        // Create cloud points
        List<CloudPoint> cloudPoints = new ArrayList<>();
        cloudPoints.add(new CloudPoint(1.0f, 2.0f));
        cloudPoints.add(new CloudPoint(3.0f, 4.0f));

        // Create tracked objects
        TrackedObject trackedObject1 = new TrackedObject("1", 1, "Object 1", cloudPoints);
        TrackedObject trackedObject2 = new TrackedObject("2", 2, "Object 2", cloudPoints);

        // Add poses
        fusionSlam.addPose(new Pose(0.0f, 0.0f, 0.0f, 1));
        fusionSlam.addPose(new Pose(1.0f, 1.0f, 45.0f, 2));

        // Process landmarks
        List<TrackedObject> trackedObjects = new ArrayList<>();
        trackedObjects.add(trackedObject1);
        trackedObjects.add(trackedObject2);
        fusionSlam.processLandMark(trackedObjects);

        // Postconditions:
        // 1. The landmarks list should contain 2 landmarks after processing.
        // 2. Landmark with ID 1 should exist.
        // 3. Landmark with ID 2 should exist.
        assertEquals(2, fusionSlam.getLandmarks().size(), "Landmarks list should contain 2 landmarks after processing");
        assertTrue(fusionSlam.landMarkExists("1"), "Landmark with ID 1 should exist");
        assertTrue(fusionSlam.landMarkExists("2"), "Landmark with ID 2 should exist");

        // Invariants:
        // 1. Landmark ID should not be null.
        // 2. Landmark description should not be null.
        // 3. Landmark coordinates should not be null.
        for (LandMark landmark : fusionSlam.getLandmarks()) {
            assertNotNull(landmark.getId(), "Landmark ID should not be null");
            assertNotNull(landmark.getDescription(), "Landmark description should not be null");
            assertNotNull(landmark.getCoordinates(), "Landmark coordinates should not be null");
            assertFalse(landmark.getCoordinates().isEmpty(), "Landmark coordinates should not be empty");
        }
    }

    @Test
    public void testTransformTrackedObjectsToLandmarksWithEmptyList() {
        // Preconditions:
        // 1. The landmarks list should be empty before processing.
        // 2. The poses list should be empty before processing.
        assertTrue(fusionSlam.getLandmarks().isEmpty(), "Landmarks list should be empty before processing");
        assertTrue(fusionSlam.getPoses().isEmpty(), "Poses list should be empty before processing");

        // Process an empty list of tracked objects
        List<TrackedObject> trackedObjects = new ArrayList<>();
        fusionSlam.processLandMark(trackedObjects);

        // Postconditions:
        // 1. The landmarks list should remain empty after processing an empty list.
        assertTrue(fusionSlam.getLandmarks().isEmpty(), "Landmarks list should remain empty after processing an empty list");

        // Invariants:
        // 1. Landmark ID should not be null.
        // 2. Landmark description should not be null.
        // 3. Landmark coordinates should not be null.
        for (LandMark landmark : fusionSlam.getLandmarks()) {
            assertNotNull(landmark.getId(), "Landmark ID should not be null");
            assertNotNull(landmark.getDescription(), "Landmark description should not be null");
            assertNotNull(landmark.getCoordinates(), "Landmark coordinates should not be null");
            assertFalse(landmark.getCoordinates().isEmpty(), "Landmark coordinates should not be empty");
        }
    }
}