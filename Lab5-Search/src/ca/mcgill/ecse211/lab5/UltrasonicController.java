package ca.mcgill.ecse211.lab5;

/**
 * This class is the controller for the ultrasonic sensor
 */
public interface UltrasonicController {

  /**
   * Process data
   * 
   * @param distance
   */
  public void processUSData(int distance);

  /**
   * Read data
   * 
   * @return distance
   */
  public int readUSDistance();
}
