package ca.mcgill.ecse211.lab5;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.Sound;
import lejos.utility.Stopwatch;

/**
 * This class controls the robot motors
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 * @author Sophie Deng
 */
public class Navigation {
	private static final int FORWARD_SPEED = 175;
	private static final int ROTATE_SPEED = 75;
	private static final double TILE_SIZE = 30.48;
	private static final double RING_SIZE = 10;
	private double leftRadius;
	private double rightRadius;
	private double track;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private Odometer odo = null;
	public int[] currentDest = {0,0};
	private EV3MediumRegulatedMotor sensorMotor;
	private UltrasonicController cont;
	public boolean isNavigating = false;

	/**
	 * This method is meant to drive the robot in a square of size 2x2 Tiles. It is to run in parallel
	 * with the odometer and Odometer correcton classes allow testing their functionality.
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @param leftRadius
	 * @param rightRadius
	 * @param width
	 */
	public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3MediumRegulatedMotor sensorMotor,
			double radius, double track, UltrasonicController cont) throws OdometerExceptions{
		this.leftRadius = radius;
		this.rightRadius = radius;
		this.track = track;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odo = Odometer.getOdometer(leftMotor, rightMotor, track, rightRadius);
		this.cont = cont;
		this.sensorMotor = sensorMotor;
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
	 * Rotates the robot by certain angle. Positive angle is clockwise
	 * @param angle
	 * @param immediateReturn
	 */
	public void rotate(double angle, boolean immediateReturn) {
		setSpeed(ROTATE_SPEED);
		if(angle < 0) {
			leftMotor.rotate(-convertAngle(leftRadius, track, Math.abs(angle)), true);
			rightMotor.rotate(convertAngle(rightRadius, track, Math.abs(angle)), immediateReturn);
			return;
		}
		leftMotor.rotate(convertAngle(leftRadius, track, Math.abs(angle)), true);
		rightMotor.rotate(-convertAngle(rightRadius, track, Math.abs(angle)), immediateReturn);
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
	 * This method controls the robot to move towards x, y (tile position) while searching for rings
	 * @param x
	 * @param y
	 */
	public void travelToWhileSearching(double x, double y){
		sensorMotor.rotateTo(0, false);
		double angleToTurnTo;
		double currentDistance;
		double[] currentPosition = odo.getXYT();

		isNavigating = true;

		//Turn to angle needed to reach waypoint
		angleToTurnTo = calculateAngle(x, y, odo);
		turnTo(angleToTurnTo);
		currentPosition = odo.getXYT();
		currentDistance = calculateDistance(x * TILE_SIZE, y * TILE_SIZE, currentPosition[0], currentPosition[1]);
		moveForward(currentDistance, true);

		while(true){

			if(RingDetector.ringDetected()){
				isNavigating = false;
				//exit navigating mode
				stop();
				if(RingDetector.targetDetected()){
					Sound.beepSequenceUp();
					moveForward(-5, false);
					break;
				}

				turn(-90);
				moveForward(TILE_SIZE / 2, false);
				turn(90);
				moveForward(TILE_SIZE, false);
				turn(90);
				moveForward(TILE_SIZE / 2, false);
				
				angleToTurnTo = calculateAngle(x, y, odo);
				turnTo(angleToTurnTo);
				currentPosition = odo.getXYT();
				currentDistance = calculateDistance(x * TILE_SIZE, y * TILE_SIZE, currentPosition[0], currentPosition[1]);
				moveForward(currentDistance, true);
				
				isNavigating = false;
			}


			//if in navigating mode, motors arent moving and near the destination,
			//exit navigating mode
			if(!leftMotor.isMoving() && !rightMotor.isMoving()){
				break;
			}
		}
	}

	/**
	 * This method controls the robot to move towards x, y (tile position) while using the US sensor to avoid obstacles
	 * @param x
	 * @param y
	 */
	public void travelToAvoidance(double x, double y){
		double angleToTurnTo;
		double currentDistance;
		double[] currentPosition = odo.getXYT();

		isNavigating = true;

		setSpeed(FORWARD_SPEED);
		
		angleToTurnTo = calculateAngle(x, y, odo);
		turnTo(angleToTurnTo);
		currentPosition = odo.getXYT();
		currentDistance = calculateDistance(x * TILE_SIZE, y * TILE_SIZE, currentPosition[0], currentPosition[1]);
		moveForward(currentDistance, true);

		while(true){

			//if distance < 10cm, enter wall following mode
			if(Lab5.usData[0]*100.0 < 10){
				stop();
				turn(-90);
				moveForward(TILE_SIZE / 2, false);
				turn(90);
				moveForward(TILE_SIZE, false);
				turn(90);
				moveForward(TILE_SIZE / 2, false);
				
				angleToTurnTo = calculateAngle(x, y, odo);
				turnTo(angleToTurnTo);
				currentPosition = odo.getXYT();
				currentDistance = calculateDistance(x * TILE_SIZE, y * TILE_SIZE, currentPosition[0], currentPosition[1]);
				moveForward(currentDistance, true);
			}
			
			//if in navigating mode, motors arent moving and near the destination,
			//exit navigating mode
			if(!leftMotor.isMoving() && !rightMotor.isMoving()){
				break;
			}
		}
	}


	/**
	 * This method turns the robot in place to the absolute angle theta.
	 * @param theta
	 */
	public void turnTo(double theta){
		double currentTheta = odo.getXYT()[2];
		double dtheta = theta - currentTheta;
		double minAngle;

		//calculate minimum angle needed to turn to get to desired angle
		if(Math.abs(dtheta) > 180){
			if(dtheta > 0)
				minAngle = dtheta - 360;
			else
				minAngle = dtheta + 360;
		}
		else
			minAngle = dtheta;

		//set motor speed
		setSpeed(ROTATE_SPEED);

		//turn by minimum angle
		this.leftMotor.rotate(convertAngle(leftRadius, track, minAngle), true);
		this.rightMotor.rotate(-convertAngle(rightRadius, track, minAngle), false);
	}

	/**
	 * This method checks if a wheel is turning
	 * @return
	 */
	public boolean isNavigating(){
		return (leftMotor.isMoving() || leftMotor.isMoving());
	}

	/**
	 * This method calculates the angle which the robot has to turn to reach tile position x, y
	 * using the data from the odometer which is passed into the method.
	 * 
	 * @param x
	 * @param y
	 * @param odo
	 * @return angle
	 */
	public static double calculateAngle(double x, double y, Odometer odo){
		double angle, dx, dy, h;
		double[] currentPosition;

		currentPosition = odo.getXYT();
		dx = x * TILE_SIZE - currentPosition[0];
		dy = y * TILE_SIZE - currentPosition[1];
		h = Math.sqrt(dx * dx + dy * dy);

		if(dx >= 0)
			angle = Math.acos(dy/h)  / Math.PI * 180;
		else
			angle = -Math.acos(dy/h) / Math.PI * 180;

		if(angle < 0){
			return angle + 360;
		}
		else{
			return angle;
		}
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
	 * This method calculates the Euclidean distance given x, y initial and x, y final.
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private static double calculateDistance(double x1, double y1, double x2, double y2) {
		double distance;
		distance = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		return distance;
	}


	/**
	 * This method calculates the minimum difference between two angles a1 and a2.
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static double angleDiff(double a1, double a2) {
		double diff = a1 - a2;
		if(diff > 180){
			return Math.abs(diff - 360);
		}
		else if(diff < -180){
			return diff + 360;
		}
		else{
			return Math.abs(diff);
		}
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

	/**
	 * makes robot go backwards
	 * 
	 */
	public void backwards() {
		setSpeed(FORWARD_SPEED);
		leftMotor.backward();
		rightMotor.backward();
	}
	
	public void turn(int angle){
		this.leftMotor.rotate(convertAngle(leftRadius, track, angle), true);
		this.rightMotor.rotate(-convertAngle(rightRadius, track, angle), false);
	}
}
