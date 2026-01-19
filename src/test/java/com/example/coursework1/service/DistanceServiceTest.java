package com.example.coursework1.service;

import com.example.coursework1.dto.DistanceRequest;
import com.example.coursework1.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UNIT TEST: Tests actual DistanceService production class
 *
 * Requirement: CR1 - Positioning accuracy (≤0.00015 degrees)
 * Test Level: Unit (tests single service in isolation)
 * Coverage Target: 95-100% of DistanceService
 */
@DisplayName("UNIT: DistanceService Production Class")
class DistanceServiceTest {

    private DistanceService distanceService;
    private static final double THRESHOLD = 0.00015;
    private static final double TOLERANCE = 1e-9;  // For floating-point comparisons

    @BeforeEach
    void setUp() {
        // Create REAL DistanceService (production class)
        distanceService = new DistanceService();
    }

    // ========================================
    // TEST: calculateDistance() method
    // ========================================

    @Test
    @DisplayName("Calculate distance between two positions")
    void testCalculateDistance() {
        // Arrange
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.00015, 0.0);
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act - CALLING PRODUCTION METHOD!
        double distance = distanceService.calculateDistance(request);

        // Assert
        assertEquals(0.00015, distance, TOLERANCE,
                "Distance should be exactly 0.00015");
    }

    @Test
    @DisplayName("Calculate distance with Pythagorean theorem")
    void testCalculateDistancePythagorean() {
        // Arrange - 3-4-5 triangle scaled down
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.00003, 0.00004);  // 3:4 ratio
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        double distance = distanceService.calculateDistance(request);

        // Assert - should be 5 (0.00005 in our scale)
        double expected = 0.00005;  // sqrt(3² + 4²) = 5
        assertEquals(expected, distance, TOLERANCE);
    }

    @Test
    @DisplayName("Distance of zero (same position)")
    void testZeroDistance() {
        // Arrange
        Position pos = new Position(-3.1869, 55.9445);
        DistanceRequest request = new DistanceRequest(pos, pos);

        // Act
        double distance = distanceService.calculateDistance(request);

        // Assert
        assertEquals(0.0, distance, TOLERANCE, "Distance to same position should be 0");
    }

    @Test
    @DisplayName("Distance with negative coordinates")
    void testDistanceWithNegativeCoordinates() {
        // Arrange
        Position pos1 = new Position(-3.1869, 55.9445);
        Position pos2 = new Position(-3.1868, 55.9446);
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        double distance = distanceService.calculateDistance(request);

        // Assert
        assertTrue(distance > 0, "Distance should be positive");
    }

    @Test
    @DisplayName("Distance is symmetric (pos1->pos2 == pos2->pos1)")
    void testDistanceSymmetry() {
        // Arrange
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0001, 0.0001);
        DistanceRequest request1 = new DistanceRequest(pos1, pos2);
        DistanceRequest request2 = new DistanceRequest(pos2, pos1);

        // Act
        double distance1 = distanceService.calculateDistance(request1);
        double distance2 = distanceService.calculateDistance(request2);

        // Assert
        assertEquals(distance1, distance2, TOLERANCE,
                "Distance should be symmetric");
    }

    // ========================================
    // TEST: isClose() method
    // ========================================

    @Test
    @DisplayName("Positions exactly at threshold should NOT be close (due to tolerance)")
    void testIsCloseExactlyAtThreshold() {
        // Arrange
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(THRESHOLD, 0.0);
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        boolean close = distanceService.isClose(request);

        // Assert
        // Note: isClose uses THRESHOLD - TOLERANCE, so exactly at THRESHOLD is NOT close
        assertFalse(close, "Distance exactly at threshold should NOT be close (uses THRESHOLD - TOLERANCE)");
    }

    @Test
    @DisplayName("Positions just below threshold should be close")
    void testIsCloseJustBelowThreshold() {
        // Arrange
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(THRESHOLD - 0.00001, 0.0);
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        boolean close = distanceService.isClose(request);

        // Assert
        assertTrue(close, "Distance below threshold should be close");
    }

    @Test
    @DisplayName("Positions just above threshold should NOT be close")
    void testIsCloseJustAboveThreshold() {
        // Arrange
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.000151, 0.0);  // Clearly above
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        boolean close = distanceService.isClose(request);

        // Assert
        assertFalse(close, "Distance above threshold should NOT be close");
    }

    @Test
    @DisplayName("Same position should be close")
    void testIsCloseSamePosition() {
        // Arrange
        Position pos = new Position(-3.1869, 55.9445);
        DistanceRequest request = new DistanceRequest(pos, pos);

        // Act
        boolean close = distanceService.isClose(request);

        // Assert
        assertTrue(close, "Same position should be close");
    }

    @Test
    @DisplayName("Very far positions should NOT be close")
    void testIsCloseFarPositions() {
        // Arrange
        Position pos1 = new Position(-3.1869, 55.9445);
        Position pos2 = new Position(-3.1500, 55.9700);  // ~3.5km away
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        boolean close = distanceService.isClose(request);

        // Assert
        assertFalse(close, "Very far positions should NOT be close");
    }

    @Test
    @DisplayName("Minimum representable distance should be close")
    void testIsCloseMinimumDistance() {
        // Arrange
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(1e-10, 1e-10);  // Tiny distance << THRESHOLD
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        boolean close = distanceService.isClose(request);

        // Assert
        assertTrue(close, "Minimum distance well below threshold should be close");
    }

    // ========================================
    // TEST: Boundary Value Analysis
    // ========================================

    @Test
    @DisplayName("BVA: Distance just below threshold minus tolerance")
    void testBoundaryValueAnalysisEpsilon() {
        // Arrange
        // isClose() checks: distance <= THRESHOLD - TOLERANCE
        // So we need: distance < 0.00015 - 1e-12
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(THRESHOLD - 2e-12, 0.0);  // Safely below
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        boolean close = distanceService.isClose(request);

        // Assert
        assertTrue(close, "Distance well below threshold should be close");
    }

    @Test
    @DisplayName("BVA: Test with Edinburgh coordinates")
    void testBoundaryValueAnalysisEdinburghCoords() {
        // Arrange - Appleton Tower area
        Position pos1 = new Position(-3.1869, 55.9445);
        Position pos2 = new Position(-3.18705, 55.9445);  // One step east
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        double distance = distanceService.calculateDistance(request);
        boolean close = distanceService.isClose(request);

        // Assert
        assertEquals(0.00015, distance, TOLERANCE, "Distance should be one step (0.00015)");
        // Note: At exactly THRESHOLD, isClose returns false due to (THRESHOLD - TOLERANCE)
        assertFalse(close, "Distance exactly at threshold is NOT close (implementation uses THRESHOLD - TOLERANCE)");
    }

    // ========================================
    // TEST: Integration with Different Directions
    // ========================================

    @Test
    @DisplayName("Horizontal distance (longitude change only)")
    void testHorizontalDistance() {
        // Arrange
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0001, 0.0);  // East
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        double distance = distanceService.calculateDistance(request);

        // Assert
        assertEquals(0.0001, distance, TOLERANCE);
    }

    @Test
    @DisplayName("Vertical distance (latitude change only)")
    void testVerticalDistance() {
        // Arrange
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 0.0001);  // North
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        double distance = distanceService.calculateDistance(request);

        // Assert
        assertEquals(0.0001, distance, TOLERANCE);
    }

    @Test
    @DisplayName("Diagonal distance (45 degrees)")
    void testDiagonalDistance() {
        // Arrange
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.00010607, 0.00010607);  // 45° = sqrt(2)/2 each
        DistanceRequest request = new DistanceRequest(pos1, pos2);

        // Act
        double distance = distanceService.calculateDistance(request);

        double expected = 0.00015;  // Approximately
        assertEquals(expected, distance, 0.000001);
    }
}