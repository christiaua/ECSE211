package ca.mcgill.ecse211.lab5;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import ca.mcgill.ecse211.odometer.*;

public class LightLocalizer {
	private Odometer odo;
	private static final int FORWARD_SPEED = 50;
	private static final int ROTATE_SPEED = 50;
	private static EV3LargeRegulatedMotor leftMotor = Lab5.leftMotor;
	private static EV3LargeRegulatedMotor rightMotor = Lab5.rightMotor;
	private static double TRACK = Lab5.TRACK;
	private static double RAD = Lab5.WHEEL_RAD;
	private static final int MAX_DISTANCE = 50;
	private static final double TILE_SIZE = 30.48;
	private ColPoller lightPoller;
	private double x;
	private double y;
	private static final double D = 12.5;
	//private double dthetaX;
	private double dthetaY;
	
	
	
	
	public LightLocalizer(ColPoller lightPoller) throws OdometerExceptions{
		this.lightPoller = lightPoller;
		this.odo = Odometer.getOdometer();
	}
	
	public void moveToOrigin(){
		int lineCount = 0;
		double[] theta = new double[4];
		double thetaY;
		double thetaX;
		
		turnTo(45);
		
		//set to move to 
		leftMotor.startSynchronization();
		leftMotor.rotate(convertDistance(RAD, MAX_DISTANCE), true);
		rightMotor.rotate(convertDistance(RAD, MAX_DISTANCE) , true);
		leftMotor.endSynchronization();
		
		while(true){
			//checks if robot crosses a line while moving
			if(leftMotor.isMoving() && leftMotor.isMoving() && Math.abs(lightPoller.getLastRedReading() - lightPoller.getCurrentRedReading()) > 0.1){
				leftMotor.startSynchronization();
				leftMotor.stop();
				rightMotor.stop();
				leftMotor.endSynchronization();
				
				Sound.beep();
				
				break;
			}
			//checks if robot stopped moving without seeing a line
			if(!leftMotor.isMoving() && !leftMotor.isMoving()) {
				//go backwards until seeing a line
				leftMotor.backward();
				rightMotor.backward();
			}
		}
		
		leftMotor.rotate(-convertDistance(RAD, 5), true);
		rightMotor.rotate(-convertDistance(RAD, 5) , false);
		
		//turn to face corner
		
		turnTo(225);
		
		//perform a 360
		
		leftMotor.rotate(convertAngle(RAD, TRACK, 360), true);
		rightMotor.rotate(-convertAngle(RAD, TRACK, 360) , true);
		
	
		
		while(leftMotor.isMoving() && leftMotor.isMoving()) {
			if(lightPoller.getLastRedReading() - lightPoller.getCurrentRedReading() > 0.1) {
				Sound.beep();
				theta[lineCount] = odo.getXYT()[2];
				lineCount++;
			}
		}
			
		//thetaY = angleDiff(theta[1],theta[3]);
		thetaY = angleDiff(theta[3],theta[1]);
		thetaX = angleDiff(theta[2],theta[0]);
		
		//calculate and set new odometer values
		
		x = -D * Math.cos(thetaY/2 * Math.PI/180);
		y = -D * Math.cos(thetaX/2 * Math.PI/180);
		
		
		
		odo.setX(x);
		odo.setY(y);
		
		
			
		//correct odometer angle
		
		//dthetaX = thetaX/2 - theta[2];
		//dthetaY = thetaY - theta[3];
		dthetaY = 90 - theta[3] + thetaY/2;
		
		//odo.update(0,0,(dthetaX + dthetaY)/2);
		odo.update(0,0,(dthetaY + 5));
		
		travelTo(0,0);
		turnTo(0);
		
	
		
	}
	
	public void travelTo(int x, int y){
		  double angleToTurnTo;
		  double currentDistance;
		  double[] currentPosition = odo.getXYT();
		  
		  
	
		  //Turn to angle needed to reach waypoint
		  angleToTurnTo = calculateAngle(x, y, odo);
		  turnTo(angleToTurnTo);
		  
		  //sleep
		  try {
		        Thread.sleep(1000);
		      } catch (Exception e) {
		      }
		  
		  //set motor speed
		  leftMotor.setSpeed(FORWARD_SPEED);
		  rightMotor.setSpeed(FORWARD_SPEED);
		  
		  currentPosition = odo.getXYT();
		  currentDistance = calculateDistance(x * TILE_SIZE, y * TILE_SIZE, currentPosition[0], currentPosition[1]);
		  
		  leftMotor.rotate(convertDistance(RAD, currentDistance), true);
		  rightMotor.rotate(convertDistance(RAD, currentDistance), false);
			 		
		  
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
	 * This method calculates the angle which the robot has to turn to reach tile position x, y
	 * using the data from the odometer, which is passed into the method.
	 * 
	 * @param x
	 * @param y
	 * @param odo
	 * @return angle
	 */
	public static double calculateAngle(int x, int y, Odometer odo){
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
	 * This method takes two angles as parameters and returns their positive 
	 * minimum angle difference in degrees.
	 * 
	 * @param a1 First angle in degrees.
	 * @param a2 Second angle in degrees.
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
	 * This method makes the robot turn to the angle specified in the parameter.
	 * 
	 * @param theta Angle to turn to.
	 */
	private void turnTo(double theta){
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
		 	leftMotor.setSpeed(ROTATE_SPEED);
		 	rightMotor.setSpeed(ROTATE_SPEED);
		  
		 	//turn by minimum angle
		 	leftMotor.rotate(convertAngle(RAD, TRACK, minAngle), true);
		 	rightMotor.rotate(-convertAngle(RAD, TRACK, minAngle), false);
	}
	
	/**
	 * This method is used to convert an angle by which the robot has to turn 
	 * to the appropriate angle by which to turn its wheels.
	 * 
	 * @param radius The radius of the robot's wheels.
	 * @param width The distance between the robot's wheels
	 * @param angle
	 * @return
	 */
	private static int convertAngle(double radius, double width, double angle) {
	    return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	/**
	 * This method is used to convert a distance by which a wheel has to move forward 
	 * to the angle it has to turn.  
	 * 
	 * @param radius The radius of the wheel.
	 * @param distance The distance to move forward.
	 * @return
	 */
	private static int convertDistance(double radius, double distance) {
	    return (int) ((180.0 * distance) / (Math.PI * radius));
	}
}
