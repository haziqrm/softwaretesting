package com.example.coursework1.service;

import com.example.coursework1.dto.Region;
import com.example.coursework1.dto.RegionRequest;
import com.example.coursework1.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UNIT TEST: Tests actual RegionService production class
 *
 * Tests point-in-polygon detection using ray-casting algorithm
 *
 * Test Level: Unit (tests single service in isolation)
 * Coverage Target: 90-95% of RegionService
 */
@DisplayName("UNIT: RegionService Production Class")
class RegionServiceTest {

    private RegionService regionService;

    // Test region: Simple square
    private Region squareRegion;
    private List<Position> squareVertices;

    @BeforeEach
    void setUp() {
        // Create REAL RegionService (production class)
        regionService = new RegionService();

        // Create a simple square region for testing
        squareVertices = Arrays.asList(
                new Position(0.0, 0.0),      // Southwest corner
                new Position(0.001, 0.0),    // Southeast corner
                new Position(0.001, 0.001),  // Northeast corner
                new Position(0.0, 0.001),    // Northwest corner
                new Position(0.0, 0.0)       // Close polygon (must match first)
        );

        squareRegion = new Region("Test Square", squareVertices);
    }

    // ========================================
    // TEST: isInRegion() - Basic Cases
    // ========================================

    @Test
    @DisplayName("Point inside square region should be detected")
    void testPointInsideSquare() {
        // Arrange
        Position insidePoint = new Position(0.0005, 0.0005);  // Center of square
        RegionRequest request = new RegionRequest(insidePoint, squareRegion);

        // Act - CALLING PRODUCTION METHOD!
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertTrue(isInside, "Point in center of square should be inside");
    }

    @Test
    @DisplayName("Point outside square region should NOT be detected")
    void testPointOutsideSquare() {
        // Arrange
        Position outsidePoint = new Position(0.002, 0.002);  // Outside square
        RegionRequest request = new RegionRequest(outsidePoint, squareRegion);

        // Act
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertFalse(isInside, "Point outside square should not be inside");
    }

    @Test
    @DisplayName("Point on boundary should be detected as inside")
    void testPointOnBoundary() {
        // Arrange
        Position boundaryPoint = new Position(0.0005, 0.0);  // On bottom edge
        RegionRequest request = new RegionRequest(boundaryPoint, squareRegion);

        // Act
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertTrue(isInside, "Point on boundary should be considered inside");
    }

    @Test
    @DisplayName("Point at exact corner should be detected")
    void testPointAtCorner() {
        // Arrange
        Position cornerPoint = new Position(0.0, 0.0);  // Southwest corner
        RegionRequest request = new RegionRequest(cornerPoint, squareRegion);

        // Act
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertTrue(isInside, "Point at corner should be inside");
    }

    // ========================================
    // TEST: Complex Polygons
    // ========================================

    @Test
    @DisplayName("Point inside George Square-like polygon")
    void testGeorgeSquarePolygon() {
        // Arrange - Approximate George Square boundaries
        List<Position> georgeSquareVertices = Arrays.asList(
                new Position(-3.1895, 55.9435),
                new Position(-3.1875, 55.9435),
                new Position(-3.1875, 55.9455),
                new Position(-3.1895, 55.9455),
                new Position(-3.1895, 55.9435)  // Close polygon
        );
        Region georgeSquare = new Region("George Square", georgeSquareVertices);

        Position insidePoint = new Position(-3.1885, 55.9445);  // Center
        RegionRequest request = new RegionRequest(insidePoint, georgeSquare);

        // Act
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertTrue(isInside, "Point in center of George Square should be inside");
    }

    @Test
    @DisplayName("Point outside George Square-like polygon")
    void testOutsideGeorgeSquare() {
        // Arrange
        List<Position> georgeSquareVertices = Arrays.asList(
                new Position(-3.1895, 55.9435),
                new Position(-3.1875, 55.9435),
                new Position(-3.1875, 55.9455),
                new Position(-3.1895, 55.9455),
                new Position(-3.1895, 55.9435)
        );
        Region georgeSquare = new Region("George Square", georgeSquareVertices);

        Position outsidePoint = new Position(-3.1850, 55.9400);  // Far outside
        RegionRequest request = new RegionRequest(outsidePoint, georgeSquare);

        // Act
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertFalse(isInside, "Point outside George Square should not be inside");
    }

    // ========================================
    // TEST: Error Cases and Validation
    // ========================================

    @Test
    @DisplayName("Region with less than 4 vertices should throw exception")
    void testRegionWithTooFewVertices() {
        // Arrange
        List<Position> tooFewVertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(0.001, 0.0),
                new Position(0.0, 0.001)
                // Only 3 vertices - invalid!
        );
        Region invalidRegion = new Region("Invalid", tooFewVertices);
        Position point = new Position(0.0005, 0.0005);
        RegionRequest request = new RegionRequest(point, invalidRegion);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            regionService.isInRegion(request);
        }, "Should throw exception for region with < 4 vertices");
    }

    @Test
    @DisplayName("Region not closed (first != last) should throw exception")
    void testRegionNotClosed() {
        // Arrange
        List<Position> notClosedVertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(0.001, 0.0),
                new Position(0.001, 0.001),
                new Position(0.0, 0.001)
                // Missing closing vertex!
        );
        Region invalidRegion = new Region("Not Closed", notClosedVertices);
        Position point = new Position(0.0005, 0.0005);
        RegionRequest request = new RegionRequest(point, invalidRegion);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            regionService.isInRegion(request);
        }, "Should throw exception for non-closed polygon");
    }

    @Test
    @DisplayName("Null vertices should throw exception")
    void testNullVertices() {
        // Arrange
        Region invalidRegion = new Region("Null Vertices", null);
        Position point = new Position(0.0005, 0.0005);
        RegionRequest request = new RegionRequest(point, invalidRegion);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            regionService.isInRegion(request);
        }, "Should throw exception for null vertices");
    }

    // ========================================
    // TEST: Edge Cases
    // ========================================

    @Test
    @DisplayName("Point very close to but outside boundary")
    void testPointVeryCloseToButOutside() {
        // Arrange
        Position nearBoundary = new Position(-0.0000001, 0.0005);  // Just outside left edge
        RegionRequest request = new RegionRequest(nearBoundary, squareRegion);

        // Act
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertFalse(isInside, "Point just outside boundary should not be inside");
    }

    @Test
    @DisplayName("Point very close to but inside boundary")
    void testPointVeryCloseToButInside() {
        // Arrange
        Position nearBoundary = new Position(0.0000001, 0.0005);  // Just inside left edge
        RegionRequest request = new RegionRequest(nearBoundary, squareRegion);

        // Act
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertTrue(isInside, "Point just inside boundary should be inside");
    }

    @Test
    @DisplayName("Point on vertical edge")
    void testPointOnVerticalEdge() {
        // Arrange
        Position onEdge = new Position(0.0, 0.0005);  // On left edge
        RegionRequest request = new RegionRequest(onEdge, squareRegion);

        // Act
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertTrue(isInside, "Point on vertical edge should be inside");
    }

    @Test
    @DisplayName("Point on horizontal edge")
    void testPointOnHorizontalEdge() {
        // Arrange
        Position onEdge = new Position(0.0005, 0.001);  // On top edge
        RegionRequest request = new RegionRequest(onEdge, squareRegion);

        // Act
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertTrue(isInside, "Point on horizontal edge should be inside");
    }

    // ========================================
    // TEST: Different Polygon Shapes
    // ========================================

    @Test
    @DisplayName("Triangular region (minimum valid polygon)")
    void testTriangularRegion() {
        // Arrange - Triangle with 4 vertices (closed)
        List<Position> triangleVertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(0.001, 0.0),
                new Position(0.0005, 0.001),
                new Position(0.0, 0.0)  // Close
        );
        Region triangle = new Region("Triangle", triangleVertices);

        Position insidePoint = new Position(0.0005, 0.0003);  // Inside triangle
        RegionRequest request = new RegionRequest(insidePoint, triangle);

        // Act
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertTrue(isInside, "Point inside triangle should be detected");
    }

    @Test
    @DisplayName("Pentagon region")
    void testPentagonRegion() {
        // Arrange - Pentagon
        List<Position> pentagonVertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(0.001, 0.0),
                new Position(0.0012, 0.0007),
                new Position(0.0005, 0.0012),
                new Position(-0.0002, 0.0007),
                new Position(0.0, 0.0)  // Close
        );
        Region pentagon = new Region("Pentagon", pentagonVertices);

        Position insidePoint = new Position(0.0005, 0.0005);
        RegionRequest request = new RegionRequest(insidePoint, pentagon);

        // Act
        boolean isInside = regionService.isInRegion(request);

        // Assert
        assertTrue(isInside, "Point inside pentagon should be detected");
    }

    // ========================================
    // TEST: Concave Polygons
    // ========================================

    @Test
    @DisplayName("Concave polygon (L-shape)")
    void testConcavePolygon() {
        // Arrange - L-shaped polygon
        List<Position> lShapeVertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(0.001, 0.0),
                new Position(0.001, 0.0005),
                new Position(0.0005, 0.0005),
                new Position(0.0005, 0.001),
                new Position(0.0, 0.001),
                new Position(0.0, 0.0)  // Close
        );
        Region lShape = new Region("L-Shape", lShapeVertices);

        // Point in bottom-right section (inside L)
        Position insidePoint = new Position(0.00075, 0.00025);
        RegionRequest requestInside = new RegionRequest(insidePoint, lShape);

        // Point in top-right section (outside L - in the notch)
        Position outsidePoint = new Position(0.00075, 0.00075);
        RegionRequest requestOutside = new RegionRequest(outsidePoint, lShape);

        // Act
        boolean isInside = regionService.isInRegion(requestInside);
        boolean isOutside = regionService.isInRegion(requestOutside);

        // Assert
        assertTrue(isInside, "Point inside L-shape should be detected");
        assertFalse(isOutside, "Point in notch of L-shape should be outside");
    }
}