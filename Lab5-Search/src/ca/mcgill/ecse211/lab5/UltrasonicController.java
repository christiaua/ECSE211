package ca.mcgill.ecse211.lab5;

/**
 * This class is the controller for the ultrasonic sensor
 */
public interface UltrasonicController {

	public void processUSData(int distance);

	public int readUSDistance();
}
