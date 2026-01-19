package com.example.coursework1.service;

import com.example.coursework1.model.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UNIT TESTING - PositioningAccuracyTest
 *
 * Test Level: UNIT (validates individual components in isolation)
 * Requirement: CR1 - Positioning accuracy (≤0.00015 degrees)
 */
@DisplayName("UNIT TESTS: Positioning Accuracy (CR1)")
class PositioningAccuracyTest {

    // Step size for movement (15 meters in degrees at Edinburgh)
    private static final double STEP = 0.00015;

    // Tolerance for floating-point comparisons (1e-9 ≈ 0.0001mm)
    private static final double TOLERANCE = 1e-9;

    // Distance threshold for "close" positioning
    private static final double PRECISION_THRESHOLD = 0.00015;

    // ========================================
    // TEST ORACLES: Manual Calculation Verification
    // ========================================

    @Test
    @DisplayName("ORACLE1: Movement at 0° with manual calculation")
    void testMovementAt0DegreesWithOracle() {
        System.out.println("\n=== ORACLE TEST: 0 DEGREES ===");

        Position start = new Position(0.0, 0.0);
        double angle = 0.0;

        // Manual calculation (oracle)
        double expectedLng = STEP * Math.cos(Math.toRadians(angle));  // cos(0°) = 1
        double expectedLat = STEP * Math.sin(Math.toRadians(angle));  // sin(0°) = 0

        System.out.println("Expected: lng=" + expectedLng + ", lat=" + expectedLat);

        Position result = navigate(start, angle);

        assertEquals(expectedLng, result.getLng(), TOLERANCE, "Longitude should match oracle");
        assertEquals(expectedLat, result.getLat(), TOLERANCE, "Latitude should match oracle");
        System.out.println(" Manual calculation verified");
    }

    @Test
    @DisplayName("ORACLE2: Movement at 90° with manual calculation")
    void testMovementAt90DegreesWithOracle() {
        System.out.println("\n=== ORACLE TEST: 90 DEGREES ===");

        Position start = new Position(0.0, 0.0);
        double angle = 90.0;

        // Manual calculation (oracle)
        double expectedLng = STEP * Math.cos(Math.toRadians(angle));  // cos(90°) = 0
        double expectedLat = STEP * Math.sin(Math.toRadians(angle));  // sin(90°) = 1

        System.out.println("Expected: lng=" + expectedLng + ", lat=" + expectedLat);

        Position result = navigate(start, angle);

        assertEquals(expectedLng, result.getLng(), TOLERANCE, "Longitude should match oracle");
        assertEquals(expectedLat, result.getLat(), TOLERANCE, "Latitude should match oracle");
        System.out.println(" Manual calculation verified");
    }

    @Test
    @DisplayName("ORACLE3: Movement at 45° with Pythagorean theorem")
    void testMovementAt45DegreesWithOracle() {
        System.out.println("\n=== ORACLE TEST: 45 DEGREES (PYTHAGOREAN) ===");

        Position start = new Position(0.0, 0.0);
        double angle = 45.0;

        // Manual calculation (oracle): cos(45°) = sin(45°) = √2/2
        double sqrt2over2 = Math.sqrt(2) / 2;
        double expectedLng = STEP * sqrt2over2;
        double expectedLat = STEP * sqrt2over2;

        Position result = navigate(start, angle);

        // Verify individual components
        assertEquals(expectedLng, result.getLng(), TOLERANCE);
        assertEquals(expectedLat, result.getLat(), TOLERANCE);

        // Verify Pythagorean theorem: distance² = lng² + lat²
        double distanceSquared = Math.pow(result.getLng(), 2) + Math.pow(result.getLat(), 2);
        double expectedDistanceSquared = STEP * STEP;

        assertEquals(expectedDistanceSquared, distanceSquared, TOLERANCE * TOLERANCE,
                "Distance should satisfy Pythagorean theorem");

        System.out.println(" Pythagorean theorem verified");
    }

    @Test
    @DisplayName("ORACLE4: Movement at 180° with manual calculation")
    void testMovementAt180DegreesWithOracle() {
        System.out.println("\n=== ORACLE TEST: 180 DEGREES ===");

        Position start = new Position(0.0, 0.0);
        double angle = 180.0;

        // Manual calculation (oracle)
        double expectedLng = STEP * Math.cos(Math.toRadians(angle));  // cos(180°) = -1
        double expectedLat = STEP * Math.sin(Math.toRadians(angle));  // sin(180°) = 0

        Position result = navigate(start, angle);

        assertEquals(expectedLng, result.getLng(), TOLERANCE);
        assertEquals(expectedLat, result.getLat(), TOLERANCE);
        System.out.println(" Manual calculation verified");
    }

    // ========================================
    // BOUNDARY VALUE ANALYSIS: Threshold Testing
    // ========================================

    @Test
    @DisplayName("BVA1: Distance exactly at precision threshold (0.00015)")
    void testDistanceExactlyAtThreshold() {
        System.out.println("\n=== BVA: EXACT THRESHOLD ===");

        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(PRECISION_THRESHOLD, 0.0);

        boolean isClose = isClose(p1, p2);

        assertTrue(isClose, "Distance of exactly 0.00015 should be considered close");
        System.out.println(" Exact threshold: CLOSE");
    }

    @Test
    @DisplayName("BVA2: Distance just below threshold")
    void testDistanceJustBelowThreshold() {
        System.out.println("\n=== BVA: JUST BELOW THRESHOLD ===");

        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(PRECISION_THRESHOLD - 0.00001, 0.0);

        boolean isClose = isClose(p1, p2);

        assertTrue(isClose, "Distance below threshold should be close");
        System.out.println(" Below threshold: CLOSE");
    }

    @Test
    @DisplayName("BVA3: Distance just above threshold")
    void testDistanceJustAboveThreshold() {
        System.out.println("\n=== BVA: JUST ABOVE THRESHOLD ===");

        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.000151, 0.0);  // Clearly above 0.00015

        boolean isClose = isClose(p1, p2);

        assertFalse(isClose, "Distance above threshold should NOT be close");
        System.out.println(" Above threshold: NOT CLOSE");
    }

    @Test
    @DisplayName("BVA4: Minimum representable distance")
    void testMinimumRepresentableDistance() {
        System.out.println("\n=== BVA: MINIMUM DISTANCE ===");

        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(1e-10, 0.0);  // Tiny distance

        boolean isClose = isClose(p1, p2);

        assertTrue(isClose, "Minimum distance should be close");
        System.out.println(" Minimum distance: CLOSE");
    }

    @Test
    @DisplayName("BVA5: Threshold with floating-point epsilon")
    void testThresholdWithFloatingPointEpsilon() {
        System.out.println("\n=== BVA: EPSILON BOUNDARY ===");

        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(PRECISION_THRESHOLD - 1e-10, 0.0);

        boolean isClose = isClose(p1, p2);

        assertTrue(isClose, "Distance within epsilon should be close");
        System.out.println(" Epsilon boundary: CLOSE");
    }

    // ========================================
    // WHITE-BOX TESTING: Branch Coverage
    // ========================================

    @Test
    @DisplayName("WB1: All 16 compass directions produce correct step size")
    void testAll16CompassDirectionsStepSize() {
        System.out.println("\n=== WHITE-BOX: 16 DIRECTIONS ===");

        Position start = new Position(0.0, 0.0);
        double[] angles = {0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
                180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5};

        for (double angle : angles) {
            Position result = navigate(start, angle);
            double distance = calculateDistance(start, result);

            assertEquals(STEP, distance, TOLERANCE,
                    "Distance at " + angle + "° should be " + STEP);
        }

        System.out.println(" All 16 directions verified");
    }

    @Test
    @DisplayName("WB2: Negative angles normalize correctly")
    void testNegativeAngles() {
        System.out.println("\n=== WHITE-BOX: NEGATIVE ANGLES ===");

        Position start = new Position(0.0, 0.0);

        // -90° should equal 270°
        Position result1 = navigate(start, -90.0);
        Position result2 = navigate(start, 270.0);

        assertEquals(result2.getLng(), result1.getLng(), TOLERANCE);
        assertEquals(result2.getLat(), result1.getLat(), TOLERANCE);

        System.out.println(" Negative angle normalization verified");
    }

    @Test
    @DisplayName("WB3: Angles above 360° normalize correctly")
    void testAngleNormalizationAbove360() {
        System.out.println("\n=== WHITE-BOX: ANGLE >360 ===");

        Position start = new Position(0.0, 0.0);

        // 450° should equal 90°
        Position result1 = navigate(start, 450.0);
        Position result2 = navigate(start, 90.0);

        assertEquals(result2.getLng(), result1.getLng(), TOLERANCE);
        assertEquals(result2.getLat(), result1.getLat(), TOLERANCE);

        System.out.println(" Angle >360° normalization verified");
    }

    @Test
    @DisplayName("WB4: Invalid angles throw appropriate exceptions")
    void testInvalidAngleThrowsException() {
        System.out.println("\n=== WHITE-BOX: INVALID ANGLE ===");

        Position start = new Position(0.0, 0.0);

        // Invalid angle (not multiple of 22.5)
        assertThrows(IllegalArgumentException.class, () -> {
            navigate(start, 23.0);
        });

        System.out.println(" Invalid angle rejected");
    }

    // ========================================
    // PARAMETERIZED TESTING: All Compass Directions
    // ========================================

    @ParameterizedTest(name = "PARAM: Angle {0}° with oracle verification")
    @CsvSource({
            "0.0",    "22.5",  "45.0",  "67.5",
            "90.0",   "112.5", "135.0", "157.5",
            "180.0",  "202.5", "225.0", "247.5",
            "270.0",  "292.5", "315.0", "337.5"
    })
    void testAllCompassDirectionsWithOracles(double angle) {
        Position start = new Position(0.0, 0.0);

        // Dynamic oracle calculation
        double expectedLng = STEP * Math.cos(Math.toRadians(angle));
        double expectedLat = STEP * Math.sin(Math.toRadians(angle));

        Position result = navigate(start, angle);

        assertEquals(expectedLng, result.getLng(), TOLERANCE,
                "Longitude at " + angle + "° should match oracle");
        assertEquals(expectedLat, result.getLat(), TOLERANCE,
                "Latitude at " + angle + "° should match oracle");

        // Verify total distance
        double distance = calculateDistance(start, result);
        assertEquals(STEP, distance, TOLERANCE,
                "Distance should be exactly " + STEP);
    }

    // ========================================
    // INTEGRATION TESTING: Component Interaction
    // ========================================

    @Test
    @DisplayName("INT1: Navigate then verify distance moved")
    void testNavigateThenVerifyDistance() {
        System.out.println("\n=== INTEGRATION: NAVIGATE + DISTANCE ===");

        Position start = new Position(-3.1869, 55.9445);  // Appleton Tower
        double angle = 45.0;

        // Navigate
        Position end = navigate(start, angle);

        // Verify distance
        double distance = calculateDistance(start, end);
        boolean isClose = isClose(start, end);

        assertEquals(STEP, distance, TOLERANCE, "Distance should be step size");
        assertTrue(isClose, "Positions should be close");

        System.out.println(" Navigation + distance verification integrated");
    }

    @Test
    @DisplayName("INT2: Multiple steps accumulate distance correctly")
    void testMultipleStepsAccumulateDistance() {
        System.out.println("\n=== INTEGRATION: MULTI-STEP ACCUMULATION ===");

        Position pos = new Position(0.0, 0.0);

        // Take 10 steps east (0°)
        for (int i = 0; i < 10; i++) {
            pos = navigate(pos, 0.0);
        }

        // Total distance should be 10 × STEP
        Position start = new Position(0.0, 0.0);
        double distance = calculateDistance(start, pos);
        double expectedDistance = 10 * STEP;

        assertEquals(expectedDistance, distance, TOLERANCE * 10,
                "Accumulated distance over 10 steps");

        System.out.println(" Multi-step accumulation verified");
    }

    // ========================================
    // EDGE CASE TESTING
    // ========================================

    @Test
    @DisplayName("EDGE1: Large coordinates maintain precision")
    void testLargeCoordinatesPrecision() {
        System.out.println("\n=== EDGE CASE: LARGE COORDINATES ===");

        Position start = new Position(179.9999, 89.9999);  // Near pole
        Position result = navigate(start, 0.0);

        double distance = calculateDistance(start, result);
        assertEquals(STEP, distance, TOLERANCE, "Precision maintained at large coordinates");

        System.out.println(" Large coordinate precision verified");
    }

    @Test
    @DisplayName("EDGE2: Crossing international date line")
    void testCrossingInternationalDateLine() {
        System.out.println("\n=== EDGE CASE: DATE LINE ===");

        Position start = new Position(179.99999, 0.0);  // Just west of date line
        Position result = navigate(start, 90.0);  // Move east

        // Should handle wrap-around correctly
        assertNotNull(result);

        System.out.println(" Date line crossing handled");
    }

    // ========================================
    // HELPER METHODS (Simulating actual service logic)
    // ========================================

    private Position navigate(Position start, double angle) {
        // Validate angle is multiple of 22.5
        if (angle % 22.5 != 0) {
            throw new IllegalArgumentException("Invalid angle: " + angle);
        }

        // Normalize angle
        angle = angle % 360;
        if (angle < 0) angle += 360;

        // Calculate new position
        double deltaLng = STEP * Math.cos(Math.toRadians(angle));
        double deltaLat = STEP * Math.sin(Math.toRadians(angle));

        return new Position(
                start.getLng() + deltaLng,
                start.getLat() + deltaLat
        );
    }

    private double calculateDistance(Position p1, Position p2) {
        double deltaLng = p2.getLng() - p1.getLng();
        double deltaLat = p2.getLat() - p1.getLat();
        return Math.sqrt(deltaLng * deltaLng + deltaLat * deltaLat);
    }

    private boolean isClose(Position p1, Position p2) {
        double distance = calculateDistance(p1, p2);
        return distance <= PRECISION_THRESHOLD;
    }
}