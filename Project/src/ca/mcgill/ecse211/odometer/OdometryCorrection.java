/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import ca.mcgill.ecse211.poller.*;
import ca.mcgill.ecse211.project.Navigation;
import ca.mcgill.ecse211.project.Project;
import lejos.hardware.Sound;

public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;
  private static final double DIST_BETWEEN_SENSORS = 12.5;
  private static final double WHEEL_RAD = 2.2;
  private Odometer odometer;
  private Poller poller;
  private Navigation navigation;
  private int tacho_at_first_detection, tacho_at_second_detection;
  
  private static boolean enabled = true;

  /**
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
 * @throws PollerException 
   */
  public OdometryCorrection() throws OdometerExceptions, PollerException {

    this.odometer = Odometer.getOdometer();
    this.poller = Poller.getPoller();
    this.navigation = new Navigation();

  }
  
  public void enable() {
	  enabled = true;
  }
  
  public void disable() {
	  enabled = false;
  }

  /**
   * Here is where the odometer correction code should be run.
   * 
   * @throws OdometerExceptions
   */
  // run method (required for Thread)
  public void run() {
    long correctionStart, correctionEnd;

    while (enabled) {
      correctionStart = System.currentTimeMillis();
      
      if(poller.getCurrentRedReading("left") < 0.33 && !navigation.isTurning()) {
    	  tacho_at_first_detection = navigation.getTacho("left");
    	  Sound.beep();
    	  while(true){
    		  if(navigation.isTurning()){
    			  Sound.beepSequence();
    			  break;
    		  }
    		  if(poller.getCurrentRedReading("right") < 0.33 || poller.getLastRedReading("right") < 0.33){
    			  tacho_at_second_detection = navigation.getTacho("left");
    	    	  correctAngle("left");
    	    	  Sound.beep();
    	    	  break;
    		  }
    	  }
      }
      else if(poller.getCurrentRedReading("right") < 0.33) {
    	  tacho_at_first_detection = navigation.getTacho("left");
    	  Sound.beep();
    	  while(true){
    		  if(navigation.isTurning()){
    			  Sound.beepSequence();
    			  break;
    		  }
    		  if(poller.getLastRedReading("left") < 0.33 || poller.getCurrentRedReading("left")  < 0.33){
    			  tacho_at_second_detection = navigation.getTacho("left");
    			  correctAngle("right");
    			  Sound.beep();
    	    	  break;
    		  }
    	  }
      }

      // this ensure the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here
        }
      }
    }
  }
  
  private void correctAngle(String sensor){
	  	double angleCorrection;
		double dtacho = tacho_at_second_detection - tacho_at_first_detection;
		double dist_between_detections = dtacho * WHEEL_RAD * Math.PI / 180;
		double currentAngle = odometer.getXYT()[2];
		
		if(sensor.equals("left")){
			angleCorrection = Math.toDegrees(Math.atan2(dist_between_detections, DIST_BETWEEN_SENSORS));
		}
		else{
			angleCorrection = Math.toDegrees(-Math.atan2(dist_between_detections, DIST_BETWEEN_SENSORS));
		}
		
		Project.lcd.drawString(angleCorrection + "    ", 0, 5);
		
		//Assume angle is within 45 degree precision
		if(angleCorrection > 3){
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
  
}