/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import ca.mcgill.ecse211.poller.*;
import ca.mcgill.ecse211.project.Project;
import lejos.hardware.Sound;

/**
 * This class is used to correct the heading of the robot using information from an odometer.
 * @author Edward Huang
 *
 */
public class OdometryCorrection{
  private static final double DIST_BETWEEN_SENSORS = 7.9;
  private static final double WHEEL_RAD = 2.075;
  private Odometer odometer;

  /**
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
   */
  public OdometryCorrection() throws OdometerExceptions{
	  this.odometer = Odometer.getOdometer();
  }
  
  /**
   * Corrects the angle of the robot given the tacho count of the motors on the detection of a line
   * by the left light sensor, and the detection of a line by the right sensor.
   * @param tacho_at_left_detection The tacho count of the motors on the detection of a line
   * by the left light sensor.
   * @param tacho_at_right_detection The tacho count of the motors on the detection of a line
   * by the right light sensor.
   */
  public void correctAngle(int tacho_at_left_detection, int tacho_at_right_detection){
		double dtacho = Math.abs(tacho_at_left_detection - tacho_at_right_detection);
		double dist_between_detections = dtacho * WHEEL_RAD * Math.PI / 180;
		double currentAngle = odometer.getXYT()[2];
		double angleCorrection = Math.toDegrees(Math.atan2(dist_between_detections, DIST_BETWEEN_SENSORS));
		
		if(tacho_at_left_detection < tacho_at_right_detection){
			//positive
			if(angleCorrection < 20) {
				Project.lcd.drawString(angleCorrection + "    ", 0, 3);
				//Assume angle is within 45 degree precision
				//odometer.update(0,0, angleCorrection);
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
		else{
			//negative
			if(angleCorrection < 20) {
				Project.lcd.drawString(angleCorrection + "    ", 0, 3);
				//Assume angle is within 45 degree precision
				//odometer.update(0,0, angleCorrection);
				if((currentAngle < 45 && currentAngle > 0) || (currentAngle < 360 && currentAngle > 315)){
					odometer.setTheta(360 - angleCorrection);
				}			
				else if(currentAngle < 135 && currentAngle > 45){
					odometer.setTheta(90 - angleCorrection);
				}
				else if(currentAngle < 225 && currentAngle > 135){
					odometer.setTheta(180 - angleCorrection);
				}
				else if(currentAngle < 315 && currentAngle > 225){
					odometer.setTheta(270 - angleCorrection);
				}
			}
			
		}
		


	}
  
}