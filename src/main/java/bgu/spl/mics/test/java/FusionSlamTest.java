package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;

public class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    public void setUp() {
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.getLandmarks().clear(); // Clear existing landmarks
        fusionSlam.getPoses().clear(); // Clear existing poses
    }

    @Test
    public void testProcessLandMark() {
        // Preconditions:
        // 1. The landmarks list should be empty before processing.
        // 2. The poses list should be empty before processing.
        assertTrue(fusionSlam.getLandmarks().isEmpty(), "Landmarks list should be empty before processing");
        assertTrue(fusionSlam.getPoses().isEmpty(), "Poses list should be empty before processing");

        // Create cloud points
        List<CloudPoint> cloudPoints = new ArrayList<>();
        cloudPoints.add(new CloudPoint(1.0, 2.0));
        cloudPoints.add(new CloudPoint(3.0, 4.0));

        // Create tracked objects
        TrackedObject trackedObject1 = new TrackedObject("1", 1, "Object 1", cloudPoints);
        TrackedObject trackedObject2 = new TrackedObject("2", 2, "Object 2", cloudPoints);

        // Add poses
        fusionSlam.addPose(new Pose(1, 0.0, 0.0, 0.0));
        fusionSlam.addPose(new Pose(2, 1.0, 1.0, 45.0));

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
        }
    }
}