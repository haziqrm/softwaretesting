package com.example.coursework1.service;

import com.example.coursework1.model.Position;
import com.example.coursework1.model.RestrictedArea;
import com.example.coursework1.repository.RestrictedAreaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * INTEGRATION TEST: Tests actual RestrictedAreaService production class

 * Requirement: SR1 - No-Fly Zone Compliance
 * Test Level: Integration (tests service + repository + region service interaction)
 * Coverage Target: 85-90% of RestrictedAreaService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("INTEGRATION: RestrictedAreaService Production Class")
class RestrictedAreaServiceIntegrationTest {

    @Mock
    private RestrictedAreaRepository mockRepository;

    private RegionService regionService;
    private RestrictedAreaService restrictedAreaService;

    // George Square test data
    private List<Position> georgeSquareVertices;
    private RestrictedArea georgeSquare;

    @BeforeEach
    void setUp() {
        // Create REAL RegionService (not mocked)
        regionService = new RegionService();

        // Create REAL RestrictedAreaService with mocked repository
        restrictedAreaService = new RestrictedAreaService(mockRepository, regionService);

        // Setup George Square test data (approximate boundaries)
        georgeSquareVertices = Arrays.asList(
                new Position(-3.1895, 55.9435),  // Southwest corner
                new Position(-3.1875, 55.9435),  // Southeast corner
                new Position(-3.1875, 55.9455),  // Northeast corner
                new Position(-3.1895, 55.9455),  // Northwest corner
                new Position(-3.1895, 55.9435)   // Close polygon
        );

        georgeSquare = new RestrictedArea();
        georgeSquare.setName("George Square");
        georgeSquare.setId(1);
        georgeSquare.setVertices(georgeSquareVertices);
    }

    // ========================================
    // TEST: isInRestrictedArea() method
    // ========================================

    @Test
    @DisplayName("Position inside George Square should be detected")
    void testPositionInsideGeorgeSquare() {
        // Arrange
        Position insidePosition = new Position(-3.1885, 55.9445);  // Center of George Square
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act - CALLING PRODUCTION METHOD!
        boolean isInRestricted = restrictedAreaService.isInRestrictedArea(insidePosition);

        // Assert
        assertTrue(isInRestricted, "Position inside George Square should be detected");
        verify(mockRepository, atLeastOnce()).fetchRestrictedAreas();
    }

    @Test
    @DisplayName("Position outside restricted areas should not be detected")
    void testPositionOutsideRestrictedAreas() {
        // Arrange
        Position outsidePosition = new Position(-3.1850, 55.9400);  // Well outside
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean isInRestricted = restrictedAreaService.isInRestrictedArea(outsidePosition);

        // Assert
        assertFalse(isInRestricted, "Position outside should not be detected as restricted");
    }

    @Test
    @DisplayName("Position on boundary should be detected")
    void testPositionOnBoundary() {
        // Arrange
        Position boundaryPosition = new Position(-3.1895, 55.9435);  // Exact corner
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean isInRestricted = restrictedAreaService.isInRestrictedArea(boundaryPosition);

        // Assert
        assertTrue(isInRestricted, "Position on boundary should be detected");
    }

    @Test
    @DisplayName("Null position should return false (not crash)")
    void testNullPosition() {
        // Arrange
        //when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean isInRestricted = restrictedAreaService.isInRestrictedArea(null);

        // Assert
        assertFalse(isInRestricted, "Null position should return false gracefully");
    }

    // ========================================
    // TEST: pathCrossesRestrictedArea() method
    // ========================================

    @Test
    @DisplayName("Path completely outside should be safe")
    void testPathOutsideRestrictedAreas() {
        // Arrange
        Position from = new Position(-3.1850, 55.9400);  // Outside
        Position to = new Position(-3.1840, 55.9420);    // Also outside
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean crosses = restrictedAreaService.pathCrossesRestrictedArea(from, to);

        // Assert
        assertFalse(crosses, "Path outside restricted areas should be safe");
    }

    @Test
    @DisplayName("Path through George Square should be detected")
    void testPathThroughGeorgeSquare() {
        // Arrange
        Position from = new Position(-3.1900, 55.9445);  // West of George Square
        Position to = new Position(-3.1870, 55.9445);    // East of George Square (through it)
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean crosses = restrictedAreaService.pathCrossesRestrictedArea(from, to);

        // Assert
        assertTrue(crosses, "Path through George Square should be detected");
    }

    @Test
    @DisplayName("Path with start inside restricted area")
    void testPathStartsInsideRestrictedArea() {
        // Arrange
        Position from = new Position(-3.1885, 55.9445);  // Inside George Square
        Position to = new Position(-3.1850, 55.9400);    // Outside
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean crosses = restrictedAreaService.pathCrossesRestrictedArea(from, to);

        // Assert
        assertTrue(crosses, "Path starting inside restricted area should be detected");
    }

    @Test
    @DisplayName("Path with end inside restricted area")
    void testPathEndsInsideRestrictedArea() {
        // Arrange
        Position from = new Position(-3.1850, 55.9400);  // Outside
        Position to = new Position(-3.1885, 55.9445);    // Inside George Square
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean crosses = restrictedAreaService.pathCrossesRestrictedArea(from, to);

        // Assert
        assertTrue(crosses, "Path ending inside restricted area should be detected");
    }

    @Test
    @DisplayName("Null from position should return false")
    void testNullFromPosition() {
        // Arrange
        Position to = new Position(-3.1850, 55.9400);
        //when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean crosses = restrictedAreaService.pathCrossesRestrictedArea(null, to);

        // Assert
        assertFalse(crosses, "Null from position should return false gracefully");
    }

    @Test
    @DisplayName("Null to position should return false")
    void testNullToPosition() {
        // Arrange
        Position from = new Position(-3.1850, 55.9400);
        //when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean crosses = restrictedAreaService.pathCrossesRestrictedArea(from, null);

        // Assert
        assertFalse(crosses, "Null to position should return false gracefully");
    }

    @Test
    @DisplayName("Both positions null should return false")
    void testBothPositionsNull() {
        // Arrange
        //when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean crosses = restrictedAreaService.pathCrossesRestrictedArea(null, null);

        // Assert
        assertFalse(crosses, "Both null positions should return false gracefully");
    }

    // ========================================
    // TEST: flightPathCrossesRestrictedArea() method
    // ========================================

    @Test
    @DisplayName("Multi-segment flight path through restricted area")
    void testFlightPathCrossesRestrictedArea() {
        // Arrange
        List<Position> flightPath = Arrays.asList(
                new Position(-3.1900, 55.9440),
                new Position(-3.1885, 55.9445),  // This goes through George Square
                new Position(-3.1870, 55.9450)
        );
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean crosses = restrictedAreaService.flightPathCrossesRestrictedArea(flightPath);

        // Assert
        assertTrue(crosses, "Flight path through restricted area should be detected");
    }

    @Test
    @DisplayName("Flight path completely outside restricted areas")
    void testFlightPathOutsideRestrictedAreas() {
        // Arrange
        List<Position> flightPath = Arrays.asList(
                new Position(-3.1850, 55.9400),
                new Position(-3.1845, 55.9405),
                new Position(-3.1840, 55.9410)
        );
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean crosses = restrictedAreaService.flightPathCrossesRestrictedArea(flightPath);

        // Assert
        assertFalse(crosses, "Flight path outside should not be detected");
    }

    @Test
    @DisplayName("Null flight path should return false")
    void testNullFlightPath() {
        // Arrange
        //when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        boolean crosses = restrictedAreaService.flightPathCrossesRestrictedArea(null);

        // Assert
        assertFalse(crosses, "Null flight path should return false");
    }

    @Test
    @DisplayName("Empty flight path should return false")
    void testEmptyFlightPath() {
        // Act
        boolean crosses = restrictedAreaService.flightPathCrossesRestrictedArea(new ArrayList<>());

        // Assert
        assertFalse(crosses, "Empty flight path should return false");
    }

    // ========================================
    // TEST: getRestrictedAreaNameForPath() method
    // ========================================

    @Test
    @DisplayName("Get name of restricted area crossed by path")
    void testGetRestrictedAreaNameForPath() {
        // Arrange
        Position from = new Position(-3.1900, 55.9445);
        Position to = new Position(-3.1870, 55.9445);
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        String areaName = restrictedAreaService.getRestrictedAreaNameForPath(from, to);

        // Assert
        assertEquals("George Square", areaName, "Should return George Square name");
    }

    @Test
    @DisplayName("Path outside returns null")
    void testGetRestrictedAreaNameForPathOutside() {
        // Arrange
        Position from = new Position(-3.1850, 55.9400);
        Position to = new Position(-3.1840, 55.9410);
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(georgeSquare));

        // Act
        String areaName = restrictedAreaService.getRestrictedAreaNameForPath(from, to);

        // Assert
        assertNull(areaName, "Path outside should return null");
    }

    // ========================================
    // TEST: getRestrictedAreaNames() method
    // ========================================

    @Test
    @DisplayName("Get all restricted area names")
    void testGetRestrictedAreaNames() {
        // Arrange
        RestrictedArea bristoSquare = new RestrictedArea();
        bristoSquare.setName("Bristo Square");
        bristoSquare.setId(2);

        when(mockRepository.fetchRestrictedAreas()).thenReturn(
                Arrays.asList(georgeSquare, bristoSquare)
        );

        // Act
        List<String> names = restrictedAreaService.getRestrictedAreaNames();

        // Assert
        assertEquals(2, names.size());
        assertTrue(names.contains("George Square"));
        assertTrue(names.contains("Bristo Square"));
    }

    // ========================================
    // TEST: Edge Cases
    // ========================================

    @Test
    @DisplayName("Empty restricted areas list")
    void testEmptyRestrictedAreas() {
        // Arrange
        Position position = new Position(-3.1885, 55.9445);
        when(mockRepository.fetchRestrictedAreas()).thenReturn(new ArrayList<>());

        // Act
        boolean isInRestricted = restrictedAreaService.isInRestrictedArea(position);

        // Assert
        assertFalse(isInRestricted, "With no restricted areas, should return false");
    }

    @Test
    @DisplayName("Restricted area with null vertices")
    void testRestrictedAreaWithNullVertices() {
        RestrictedArea invalidArea = new RestrictedArea();
        invalidArea.setName("Invalid Area");
        invalidArea.setVertices(null);

        Position position = new Position(-3.1885, 55.9445);
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(invalidArea));

        boolean isInRestricted = restrictedAreaService.isInRestrictedArea(position);

        // Assert
        assertFalse(isInRestricted, "Area with null vertices should be skipped");
    }

    @Test
    @DisplayName("Restricted area with empty vertices list")
    void testRestrictedAreaWithEmptyVertices() {
        // Arrange
        RestrictedArea invalidArea = new RestrictedArea();
        invalidArea.setName("Invalid Area");
        invalidArea.setVertices(new ArrayList<>());  // Empty list

        Position position = new Position(-3.1885, 55.9445);
        when(mockRepository.fetchRestrictedAreas()).thenReturn(Arrays.asList(invalidArea));

        // Act
        boolean isInRestricted = restrictedAreaService.isInRestrictedArea(position);

        assertFalse(isInRestricted, "Area with empty vertices should be skipped");
    }
}