package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
//import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;
//import ca.mcgill.ecse211.odometer.Odometer;
//import ca.mcgill.ecse211.odometer.OdometerExceptions;
//import lejos.hardware.Button;
//import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

/**
 * This class does the navigation to the desired points
 * 
 * @author Sophie Deng
 */
public class Navigation {

	private static Odometer odometer;

	// private double newheading;
	private final static int FORWARD_SPEED = 120;
	private final static int ROTATE_SPEED = 50;

	private final static double TILE_SIZE = 30.48;


	static RegulatedMotor leftMotor;
	static RegulatedMotor rightMotor;
	public static double wheelRadius = 2.2;
	static double TRACK = 9.9;


	/**
	 * Constructor
	 */
	public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double wheelRad, double track) throws OdometerExceptions {
		Navigation.odometer = Odometer.getOdometer();
		Navigation.leftMotor = leftMotor;
		Navigation.rightMotor = rightMotor;
		Navigation.wheelRadius = wheelRad;
		Navigation.TRACK = track;
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.setAcceleration(500);
		}
	}

	/**
	 * check if the robot is navigating
	 * @return
	 */
	public boolean isNavigating(){

		return leftMotor.isMoving() || rightMotor.isMoving();

	}

	/**
	 * This method makes the robot go forward by one tile size
	 * 
	 */
	public void moveForward(){
		stop();
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistance(wheelRadius, TILE_SIZE), true);
		rightMotor.rotate(convertDistance(wheelRadius, TILE_SIZE), true);

	}

	/**
	 * This method makes the robot move a distance in x and a distance in y
	 * 
	 */
	public void travelTo(double dx, double dy) {
		stop();
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		
		//go left or right
		turnTo(90);
		leftMotor.rotate(convertDistance(wheelRadius, dx), true);
		rightMotor.rotate(convertDistance(wheelRadius, dx), false);

		turnTo(0);
		leftMotor.rotate(convertDistance(wheelRadius, Math.abs(dy)), true);
		rightMotor.rotate(convertDistance(wheelRadius, Math.abs(dy)), false);

	}

	/**
	 * This method rotates the robot. It does not tell it when to stop
	 * @param left motor speed and right motor speed
	 */
	public void rotateWheels(int leftSpeed, int rightSpeed) {
		stop();
		leftMotor.setSpeed(leftSpeed);
		rightMotor.setSpeed(rightSpeed);
		
		//rotate left motor
		if(leftSpeed < 0) {
			leftMotor.backward();
		}
		else {
			leftMotor.forward();
		}
		
		//rotate right motor
		if(rightSpeed < 0) {
			rightMotor.backward();
		}
		else {
			rightMotor.forward();
		}
	}

	/**
	 * This method rotates the robot to the wanted angle
	 * 
	 * @param angle
	 *  */
	public void turnTo(double angle) {
		stop();
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		double currentAngle = odometer.getXYT()[2];
		double angleDif = angle - currentAngle;

		//compute minimum angle
		if(angleDif > 180) {
			angleDif *= 1;
			angleDif = (360 - angle) + currentAngle;
		}
		else if(angleDif < -180) {
			angleDif *= -1;
			angleDif = (360 - currentAngle) + angle;
		}

		if(angleDif > 0) {
			//rotate clockwise
			leftMotor.rotate(convertAngle(wheelRadius, TRACK, Math.abs(angleDif)), true);
			rightMotor.rotate(-convertAngle(wheelRadius, TRACK, Math.abs(angleDif)), false);
		}
		else if(angleDif < 0) {
			//rotate counterclockwise
			leftMotor.rotate(-convertAngle(wheelRadius, TRACK, Math.abs(angleDif)), true);
			rightMotor.rotate(convertAngle(wheelRadius, TRACK, Math.abs(angleDif)), false);
		}
	}

	/**
	 * Stops both right and left motors
	 * 
	 */
	public void stop() {
		leftMotor.stop();
		rightMotor.stop();		

	}

	/**
	 * This method computes how much the wheels should turn to get to an angle
	 * 
	 * @param radius
	 * @param distance
	 * @return
	 */
	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	/**
	 * This method allows the conversion of a distance to the total rotation of each wheel need to
	 * cover that distance.
	 * 
	 * @param radius
	 * @param distance
	 * @return
	 */
	public static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
}
