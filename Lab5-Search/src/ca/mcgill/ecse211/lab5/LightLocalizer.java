package ca.mcgill.ecse211.lab5;

import lejos.hardware.Sound;
import ca.mcgill.ecse211.odometer.*;

/**
 * This class does the light localization
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 */
public class LightLocalizer {
	private Odometer odo;

	private static final int MAX_DISTANCE = 50;
	private ColPoller lightPoller;
	private double x;
	private double y;
	private static final double D = 14;
	private double dthetaY;
	private Navigation navigation;
	private static final double TILE_SIZE = 30.48;
	
	/**
	 * constructor
	 * @param lightPoller
	 * @param nav
	 * @throws OdometerExceptions
	 */
	public LightLocalizer(ColPoller lightPoller, Navigation nav) throws OdometerExceptions{
		this.lightPoller = lightPoller;
		this.odo = Odometer.getOdometer();
		this.navigation = nav;
	}
	
	/**
	 * makes the robot turn to 45 and goes until it finds the first intersection AKA (1,1)
	 */
	public void moveToOrigin(){
		int lineCount = 0;
		double[] theta = new double[4];
		double thetaY;
		double thetaX;
		
		navigation.turnTo(45);
		
		navigation.moveForward(MAX_DISTANCE, true);
		
		while(true){
			//checks if robot crosses a line while moving
			if(navigation.isNavigating() && lightPoller.getCurrentRedReading() < 0.33){
				navigation.stop();
				Sound.beep();
				break;
			}
			//checks if robot stopped moving without seeing a line
			if(!navigation.isNavigating()) {
				//go backwards until seeing a line
				navigation.backwards();
			}
		}
		
		navigation.moveForward(-D, false);
		
		//perform a 360
		navigation.rotate(360, true);
		
		while(navigation.isNavigating()) {
			if(lightPoller.getLastRedReading() - lightPoller.getCurrentRedReading() > 0.1) {
				Sound.beep();
				theta[lineCount] = odo.getXYT()[2];
				lineCount++;
			}
		}
			
		thetaY = angleDiff(theta[1],theta[3]);
		thetaX = angleDiff(theta[0],theta[2]);
		
		//calculate and set new odometer values
		
		x = -D * Math.cos(thetaY/2 * Math.PI/180);
		y = -D * Math.cos(thetaX/2 * Math.PI/180);	
		odo.setX(x);
		odo.setY(y);
		dthetaY = -90 - theta[3] + thetaY/2;
		odo.update(0,0,(dthetaY + 5));
		navigation.travelToWhileSearching(0,0);
		navigation.turnTo(0);

		odo.setXYT(TILE_SIZE, TILE_SIZE, 0);
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
}
