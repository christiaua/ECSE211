/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import ca.mcgill.ecse211.poller.*;
import ca.mcgill.ecse211.project.Navigation;
import ca.mcgill.ecse211.project.Project;
import lejos.hardware.Sound;

public class OdometryCorrection{
  private static final double DIST_BETWEEN_SENSORS = 12.5;
  private static final double WHEEL_RAD = 2.2;
  private Odometer odometer;

  /**
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
 * @throws PollerException 
   */
  public OdometryCorrection() throws OdometerExceptions{
	  this.odometer = Odometer.getOdometer();
  }
  
  public void correctAngle(int tacho_at_left_detection, int tacho_at_right_detection){
	  	double angleCorrection;
		double dtacho = Math.abs(tacho_at_left_detection - tacho_at_right_detection);
		double dist_between_detections = dtacho * WHEEL_RAD * Math.PI / 180;
		double currentAngle = odometer.getXYT()[2];
		
		if(tacho_at_left_detection < tacho_at_right_detection){
			angleCorrection = Math.toDegrees(Math.atan2(dist_between_detections, DIST_BETWEEN_SENSORS));
		}
		else{
			angleCorrection = Math.toDegrees(-Math.atan2(dist_between_detections, DIST_BETWEEN_SENSORS));
		}
		
		Project.lcd.drawString(angleCorrection + "    ", 0, 5);
		
		//Assume angle is within 45 degree precision
		if((currentAngle < 45 && currentAngle > 0) || (currentAngle < 360 && currentAngle > 315)){
			odometer.setTheta(angleCorrection);
		}			
		else if(currentAngle < 135 && currentAngle > 45){
			odometer.setTheta(90 + angleCorrection);
		}
		else if(currentAngle < 225 && currentAngle > 135){
			odometer.setTheta(180 + angleCorrection);
		}
		else if(currentAngle < 315 && currentAngle > 225){
			odometer.setTheta(270 + angleCorrection);
		}
	}
  
}