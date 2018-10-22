package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.Sound;

/**
 * This class does the ultrasonic localization
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 */
public class UltrasonicLocalizer {
	
	private Odometer odo;
	private Navigation navigation;
	public static final double D = 25;
	public static final double THRESHOLD = 1;
	private UltrasonicPoller usPoller;
	private static int angleCorrection = 0;
	
	
	/**
	 * Constructor
	 * @param usPoller
	 * @param nav
	 * @throws OdometerExceptions
	 */
	public UltrasonicLocalizer(UltrasonicPoller usPoller, Navigation nav) throws OdometerExceptions{
		this.odo = Odometer.getOdometer();
		this.usPoller = usPoller;
		this.navigation = nav;
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
		
		navigation.rotate(360, true);
		
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
					navigation.stop();
					if(i == 0){
						navigation.rotate(-360, true);
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
		
		navigation.rotate(360, true);
		
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
					navigation.stop();
					if(i == 0){
						navigation.rotate(-360, true);
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
		 	navigation.rotate(minAngle, false);
	}
}
