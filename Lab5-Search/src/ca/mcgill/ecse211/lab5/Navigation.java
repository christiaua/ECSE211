/*
 * SquareDriver.java
 */
package ca.mcgill.ecse211.lab5;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import ca.mcgill.ecse211.lab5.Lab5;
import ca.mcgill.ecse211.lab5.Navigation;
import ca.mcgill.ecse211.odometer.*;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.lcd.TextLCD;

/**
 * This class is used to drive the robot on the demo floor.
 */
public class Navigation {
  private static final int FORWARD_SPEED = 125;
  private static final int ROTATE_SPEED = 125;
  private static final double TILE_SIZE = 30.48;
  private double leftRadius;
  private double rightRadius;
  private double track;
  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;
  public boolean isNavigating = false;
  private Odometer odo = null;
  public int[] currentDest = {0,0};
  private EV3MediumRegulatedMotor sensorMotor;
  private UltrasonicController cont;
 
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
   * This method controls the robot to move towards x, y (tile position)
   * @param x
   * @param y
   */
  public void travelTo(int x, int y){
	  int angleAtDetection;
	  double angleToTurnTo;
	  double currentDistance;
	  double[] currentPosition = odo.getXYT();
	  
	  isNavigating = true;
	  
	  //Save current destination
	  currentDest[0] = x;
	  currentDest[1] = y;
	  
	  //Turn to angle needed to reach waypoint
	  angleToTurnTo = calculateAngle(x, y, odo);
	  turnTo(angleToTurnTo);
	  
	  //sleep
	  try {
	        Thread.sleep(1000);
	      } catch (Exception e) {
	      }
	  
	  //set motor speed
	  this.leftMotor.setSpeed(FORWARD_SPEED);
	  this.rightMotor.setSpeed(FORWARD_SPEED);
	  
	  while(true){
		  //update current euclidean distance away from waypoint
		  currentPosition = odo.getXYT();
		  currentDistance = calculateDistance(x * TILE_SIZE, y * TILE_SIZE, currentPosition[0], currentPosition[1]);

		  if((int)(Lab5.usData[0] * 100) < Lab5.wallFollowingBandCenter){
	    	  //exit navigating mode
	    	  isNavigating = false;
	    	  this.leftMotor.stop(true);
	    	  this.rightMotor.stop(false);
	    	  
	    	  //save the angle at which the ultrasonic sensor detected an obstacle
	    	  angleAtDetection = this.sensorMotor.getTachoCount();
	    	  
	    	  //rotate sensor towards obstacle
	    	  sensorMotor.setSpeed(75);
	    	  sensorMotor.rotateTo(-90, true);
	    	  
	    	  try {
	    	        Thread.sleep(1000);
	    	      } catch (Exception e) {
	    	      }

	    	  //rotate the robot by 90 degrees + angle of the sensor at obstacle detection
	    	  leftMotor.rotate(-Navigation.convertAngle(Lab5.WHEEL_RAD, Lab5.TRACK, 90 + angleAtDetection), true);
	    	  rightMotor.rotate(Navigation.convertAngle(Lab5.WHEEL_RAD, Lab5.TRACK, 90 + angleAtDetection), false);
	    	  
	    	  try {
	  	        Thread.sleep(1000);
	  	      } catch (Exception e) {
	  	      }   	  
		      
	    	  do{
	    		  //pass distance to bang bang controller and allow it to control the robot
		    	  cont.processUSData((int)(Lab5.usData[0]*100.0));
		          
		          try {
		              Thread.sleep(50);
		          } catch (Exception e) {
		          }
		         
		          //if the angle the robot is currently at is within 10 of the angle it has to
		          //be to reach the current destination, exit wall following mode
		          if(Navigation.angleDiff(
			    		  Navigation.calculateAngle(currentDest[0], currentDest[1], odo), odo.getXYT()[2]) < 10){
		        	  Sound.beep();
		        	  break;
		          }
		      } while(true);
	    	  
	    	  //after exiting wall following mode, stop motors and rotate
	    	  //ultrasonic sensor back to 0 degrees
	    	  this.leftMotor.stop(true);
	    	  this.rightMotor.stop(false);
	    	  sensorMotor.rotateTo(0, false);
	    	  
	    	  //reenter navigating mode
	    	  isNavigating = true;
	    	  sensorMotor.setSpeed(75);
	      }
		  
		//if in navigating mode, motors arent moving and not close enough to the
		  //current destination, turn in the correct direction to get to destination
		  //and move forward
		  if(isNavigating && currentDistance > 1 && !leftMotor.isMoving() && !rightMotor.isMoving()){
			  angleToTurnTo = calculateAngle(x, y, odo);
			  turnTo(angleToTurnTo);
			  this.leftMotor.rotate(convertDistance(leftRadius, currentDistance), true);
			  this.rightMotor.rotate(convertDistance(rightRadius, currentDistance), true);
		  }
		  //if in navigating mode, motors arent moving and near the destination,
		  //exit navigating mode
		  else if(isNavigating && currentDistance < 1 && !leftMotor.isMoving() && !rightMotor.isMoving()){
			  isNavigating = false;
			  break;
		  }
		  
		  try{
			  Thread.sleep(25);
		  } catch (InterruptedException e){
		    	 
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
	  this.leftMotor.setSpeed(ROTATE_SPEED);
	  this.rightMotor.setSpeed(ROTATE_SPEED);
	  
	  //turn by minimum angle
	  this.leftMotor.rotate(convertAngle(leftRadius, track, minAngle), true);
	  this.rightMotor.rotate(-convertAngle(rightRadius, track, minAngle), false);
  }
  
  /**
   * This method allows access to the isNavigating boolean.
   * @return
   */
  public boolean isNavigating(){
	  return this.isNavigating();
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
}
