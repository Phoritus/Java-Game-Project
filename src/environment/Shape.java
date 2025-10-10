package src.environment;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;

/**
 * Utility class for creating common shapes used in lighting and effects
 */
public class Shape {
  
  /**
   * Creates a circular Area for lighting effects
   * @param centerX Center X coordinate
   * @param centerY Center Y coordinate
   * @param diameter Diameter of the circle
   * @return Area object representing the circle
   */
  public static Area createCircleArea(double centerX, double centerY, double diameter) {
    double x = centerX - (diameter / 2);
    double y = centerY - (diameter / 2);
    java.awt.Shape circleShape = new Ellipse2D.Double(x, y, diameter, diameter);
    return new Area(circleShape);
  }
  
  /**
   * Creates a rectangular Area
   * @param x Top-left X coordinate
   * @param y Top-left Y coordinate
   * @param width Width of rectangle
   * @param height Height of rectangle
   * @return Area object representing the rectangle
   */
  public static Area createRectangleArea(double x, double y, double width, double height) {
    java.awt.Shape rectShape = new Rectangle2D.Double(x, y, width, height);
    return new Area(rectShape);
  }
  
  /**
   * Creates a rectangular Area from Rectangle object
   * @param rect Rectangle object
   * @return Area object representing the rectangle
   */
  public static Area createRectangleArea(Rectangle rect) {
    return new Area(rect);
  }
  
  /**
   * Creates a darkness overlay with a light circle in the center
   * @param screenWidth Width of the screen
   * @param screenHeight Height of the screen
   * @param lightCenterX Center X of the light circle
   * @param lightCenterY Center Y of the light circle
   * @param lightRadius Radius of the light circle
   * @return Area representing darkness with a circular light hole
   */
  public static Area createDarknessWithLight(int screenWidth, int screenHeight, 
                                               double lightCenterX, double lightCenterY, 
                                               double lightRadius) {
    // Create full screen darkness
    Area darknessArea = createRectangleArea(0, 0, screenWidth, screenHeight);
    
    // Create light circle
    Area lightArea = createCircleArea(lightCenterX, lightCenterY, lightRadius * 2);
    
    // Subtract light from darkness to create "hole"
    darknessArea.subtract(lightArea);
    
    return darknessArea;
  }
}
