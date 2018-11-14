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
  private static final double DIST_SENSOR_AND_WHEELBASE = 14;
  private static final double ANGLE_BETWEEN_SENSOR_WHEELBASE = Math.toRadians(63.48);
  private static final double TILE_SIZE = 30.48;
  private static final double WHEEL_RAD = 2.2;
  private double tacho_at_last_correction;
  private double angle_at_last_correction;
  private Odometer odometer;
  private Navigation navigation;

  /**
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
 * @throws PollerException 
   */
  public OdometryCorrection() throws OdometerExceptions{
	  this.odometer = Odometer.getOdometer();
	  tacho_at_last_correction = 0;
	  this.navigation = new Navigation();
  }
  
  public void correctAngle(int tacho_at_left_detection, int tacho_at_right_detection){
	  	double angleCorrection;
	  	double angleError;
	  	double d, dx, dy;
		double dtacho = Math.abs(tacho_at_left_detection - tacho_at_right_detection);
		double dist_between_detections = dtacho * WHEEL_RAD * Math.PI / 180;
		double currentAngle = odometer.getXYT()[2];
		
		if(tacho_at_left_detection < tacho_at_right_detection){
			angleCorrection = Math.toDegrees(Math.atan2(dist_between_detections, DIST_BETWEEN_SENSORS));
			d = (tacho_at_right_detection - tacho_at_last_correction) * WHEEL_RAD * Math.PI / 180;
		}
		else{
			angleCorrection = Math.toDegrees(-Math.atan2(dist_between_detections, DIST_BETWEEN_SENSORS));
			d = (tacho_at_left_detection - tacho_at_last_correction) * WHEEL_RAD * Math.PI / 180;
		}
		
		//Assume angle is within 45 degree precision
		if(angleCorrection < 20) {
			if((currentAngle < 45 && currentAngle > 0) || (currentAngle < 360 && currentAngle > 315)){
				angleError = Math.toRadians(angleCorrection - odometer.getXYT()[2]);
				odometer.setTheta(angleCorrection);
				if(angle_at_last_correction < 45 && angle_at_last_correction > 0){
					dx = Math.sin(angleError) * d;
					dy = Math.sin(Math.abs(angleError)) * DIST_BETWEEN_SENSORS;
					odometer.update(dx, dy, 0);
					//odometer.update(dx, 0, 0);
					//odometer.setY(dy);
				}
				else{
					tacho_at_last_correction = navigation.getTacho("left");
				}
				angle_at_last_correction = 0;
			}			
			else if(currentAngle < 135 && currentAngle > 45){
				angleError = Math.toRadians(90 + angleCorrection - odometer.getXYT()[2]);
				odometer.setTheta(90 + angleCorrection);
				if(angle_at_last_correction < 45 && angle_at_last_correction > 0){
					dy = -Math.sin(angleError) * d;
					dx = Math.sin(Math.abs(angleError)) * DIST_BETWEEN_SENSORS;
					odometer.update(dx, dy, 0);
					//odometer.update(0, dy, 0);
					//odometer.setX(dx);
				}
				else{
					tacho_at_last_correction = navigation.getTacho("left");
				}
				angle_at_last_correction = 90;
			}
			else if(currentAngle < 225 && currentAngle > 135){
				angleError = Math.toRadians(180 + angleCorrection - odometer.getXYT()[2]);
				odometer.setTheta(180 + angleCorrection);
				if(angle_at_last_correction < 45 && angle_at_last_correction > 0){
					dx = -Math.sin(angleError) * d;
					dy = -Math.sin(Math.abs(angleError)) * DIST_BETWEEN_SENSORS;
					odometer.update(dx, dy, 0);
					//odometer.update(dx, 0, 0);
					//odometer.setY(dy);
				}
				else{
					tacho_at_last_correction = navigation.getTacho("left");
				}
				angle_at_last_correction = 180;
			}
			else if(currentAngle < 315 && currentAngle > 225){
				angleError = Math.toRadians(270 + angleCorrection - odometer.getXYT()[2]);
				odometer.setTheta(270 + angleCorrection);
				if(angle_at_last_correction < 45 && angle_at_last_correction > 0){
					dy = Math.sin(angleError) * d;
					dx = -Math.sin(Math.abs(angleError)) * DIST_BETWEEN_SENSORS;
					odometer.update(dx, dy, 0);
					//odometer.update(0, dy, 0);
					//odometer.setX(dx);
				}
				else{
					tacho_at_last_correction = navigation.getTacho("left");
				}
				angle_at_last_correction = 270;
			}
		}
	}
  
}