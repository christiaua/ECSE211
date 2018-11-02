package testLineDetection;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
/**
 * This class controls the robot motors
 * 
 * @author Edward Huang
 */
public class Controller {
	private static final int FORWARD_SPEED = 175;
	private double leftRadius;
	private double rightRadius;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	/**
	 * Creates an object meant to control the robot motors
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @param leftRadius
	 * @param rightRadius
	 * @param width
	 */
	public Controller(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double radius){
		this.leftRadius = radius;
		this.rightRadius = radius;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}

	/**
	 * Moves the robot forwards a distance. A negative distance implies going backwards
	 * @param distance
	 * @param immediateReturn
	 */
	public void moveForward(double distance, boolean immediateReturn){
		stop();
		setSpeed(FORWARD_SPEED);
		if(distance < 0) {
			leftMotor.rotate(-convertDistance(leftRadius, Math.abs(distance)), true);
			rightMotor.rotate(-convertDistance(rightRadius, Math.abs(distance)), immediateReturn);
			return;
		}
		leftMotor.rotate(convertDistance(leftRadius, distance), true);
		rightMotor.rotate(convertDistance(rightRadius, distance), immediateReturn);
	}

	/**
	 * This method allows the conversion of a distance to the total rotation of each wheel need to
	 * cover that distance.
	 * 
	 * @param radius
	 * @param distance
	 * @return
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 * Stops both right and left motors
	 * 
	 */
	public void stop() {
		leftMotor.stop(true);
		rightMotor.stop(false);		
	}

	/**
	 * set speed for both motors
	 * 
	 */
	public void setSpeed(int speed) {
		this.leftMotor.setSpeed(speed);
		this.rightMotor.setSpeed(speed);
	}
}
