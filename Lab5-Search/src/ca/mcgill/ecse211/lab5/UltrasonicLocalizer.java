package ca.mcgill.ecse211.lab5;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

import java.util.Arrays;

import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.Sound;


public class UltrasonicLocalizer {
	
	private Odometer odo;
	private static final int ROTATE_SPEED = 50;
	private static EV3LargeRegulatedMotor leftMotor = Lab5.leftMotor;
	private static EV3LargeRegulatedMotor rightMotor = Lab5.rightMotor;
	private static double TRACK = Lab5.TRACK;
	private static double RAD = Lab5.WHEEL_RAD;
	public static final double D = 30;
	public static final double THRESHOLD = 1;
	private ColPoller usPoller;
	private static int angleCorrection = 0;
	
	public UltrasonicLocalizer(ColPoller usPoller) throws OdometerExceptions{
		this.odo = Odometer.getOdometer();
		this.usPoller = usPoller;
	}
	/**
	 * This method is used to find the angle of the robot assuming 
	 * it is located in the bottom left corner of the grid, and make it turn to 0 degrees.
	 * It does so by using the ultrasonic sensor values and detecting the two relative positions where 
	 * the sensor values falls under a given constant, and sets the midpoint between these values 
	 * to be the 45 degree angle.
	 */
	public void fallingEdge(){
		double[] fallingEdgeAngle = new double[2]; //angle at which falling edges are detected
		double temp1; //holds temp angle values of when the ultrasonic sensor
		double temp2; //when the distance detected falls within the threshold and when it
					  //falls out of the threshold
		double actualHeading;
		double dtheta;
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(convertAngle(RAD, TRACK, 360), true);
		rightMotor.rotate(-convertAngle(RAD, TRACK, 360), true);
		
		for(int i = 0; i < 2; i++){
			while(true){
				if(usPoller.lastDistance[0] > (D + THRESHOLD) && usPoller.lastDistance[1] > (D + THRESHOLD) && usPoller.distance < (D + THRESHOLD)){
					temp1 = odo.getXYT()[2];
					while(true){
						if(usPoller.distance < D - THRESHOLD){
							temp2 = odo.getXYT()[2];
							
							Sound.beep(); //falling edge detected;
							
							break;
						}
						try{
					        Thread.sleep(25);
					    }catch (Exception e){
					    } 
					}
					fallingEdgeAngle[i] = (temp1 + temp2) / 2;
					leftMotor.stop();
					rightMotor.stop();
					if(i == 0){
						leftMotor.rotate(-convertAngle(RAD, TRACK, 360), true);
						rightMotor.rotate(convertAngle(RAD, TRACK, 360), true);
					}
					break;
				}
				try {
			        Thread.sleep(25);
			      } catch (Exception e) {
			      } 
			}
		}
		
		if(fallingEdgeAngle[0] < fallingEdgeAngle[1]){
			dtheta = 225 + angleCorrection - (fallingEdgeAngle[0] + fallingEdgeAngle[1])/2;
		}
		else{
			dtheta = 45 + angleCorrection - (fallingEdgeAngle[0] + fallingEdgeAngle[1])/2;
		}
		
		actualHeading = odo.getXYT()[2] + dtheta;
		odo.setTheta(actualHeading);
		turnTo(0);
	}
	
	/**
	 * This method is used to find the angle of the robot assuming 
	 * it is located in the bottom left corner of the grid, and make it turn to 0 degrees.
	 * It does so by using the ultrasonic sensor values and detecting the two relative positions where 
	 * the sensor values rises above a given constant, and sets the midpoint between these values 
	 * to be the 225 degree angle.
	 */
	public void risingEdge(){
		double[] risingEdgeAngle = new double[2]; //angle at which falling edges are detected
		double temp1; //holds temp angle values of when the ultrasonic sensor
		double temp2; //when the distance detected falls within the threshold and when it
					  //falls out of the threshold
		double actualHeading;
		double dtheta;
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(convertAngle(RAD, TRACK, 360), true);
		rightMotor.rotate(-convertAngle(RAD, TRACK, 360), true);
		
		for(int i = 0; i < 2; i++){
			while(true){
				if(usPoller.lastDistance[0] < (D - THRESHOLD) && usPoller.lastDistance[1] < (D - THRESHOLD) && usPoller.distance > (D - THRESHOLD)){
					temp1 = odo.getXYT()[2];
					while(true){
						if(usPoller.distance > D + THRESHOLD){
							temp2 = odo.getXYT()[2];
							
							Sound.beep(); //falling edge detected;
							
							break;
						}
						try{
					        Thread.sleep(25);
					    }catch (Exception e){
					    } 
					}
					risingEdgeAngle[i] = (temp1 + temp2) / 2;
					leftMotor.startSynchronization();
					leftMotor.stop();
					rightMotor.stop();
					leftMotor.endSynchronization();
					if(i == 0){
						leftMotor.rotate(-convertAngle(RAD, TRACK, 360), true);
						rightMotor.rotate(convertAngle(RAD, TRACK, 360), true);
					}
					break;
				}
				try {
			        Thread.sleep(25);
			      } catch (Exception e) {
			      } 
			}
		}
		
		if(risingEdgeAngle[0] > risingEdgeAngle[1]){
			dtheta = 225 + angleCorrection - (risingEdgeAngle[0] + risingEdgeAngle[1])/2;
		}
		else{
			dtheta = 45 + angleCorrection - (risingEdgeAngle[0] + risingEdgeAngle[1])/2;
		}
		
		actualHeading = odo.getXYT()[2] + dtheta;
		odo.setTheta(actualHeading);
		turnTo(0);
	}
	
	private static int convertAngle(double radius, double width, double angle) {
	    return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	private static int convertDistance(double radius, double distance) {
	    return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	
	/**
	   * This method turns the robot in place to the absolute angle theta.
	   * @param theta
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
	/*
	private void medianFilter(int[] data , int windowSize) {
		median
		java.util.Arrays.sort(data);
	
}*/
	
}