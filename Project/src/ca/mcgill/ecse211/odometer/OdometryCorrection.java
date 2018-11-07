/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import ca.mcgill.ecse211.poller.*;

public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;
  private static final double DIST_BETWEEN_SENSORS = 10;
  private Odometer odometer;
  private Poller poller;
  private double x_at_first_detection, y_at_first_detection, x_at_second_detection, y_at_second_detection;

  /**
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
   */
  public OdometryCorrection(Poller poller) throws OdometerExceptions {

    this.odometer = Odometer.getOdometer();
    this.poller = poller;

  }

  /**
   * Here is where the odometer correction code should be run.
   * 
   * @throws OdometerExceptions
   */
  // run method (required for Thread)
  public void run() {
    long correctionStart, correctionEnd;

    while (true) {
      correctionStart = System.currentTimeMillis();

      if(poller.getLastRedReading(1) - poller.getCurrentRedReading(1) > 0.1) {
    	  x_at_first_detection = odometer.getXYT()[0];
    	  y_at_first_detection = odometer.getXYT()[1];
    	  while(true){
    		  if(poller.getLastRedReading(2) - poller.getCurrentRedReading(2) > 0.1){
    			  x_at_second_detection = odometer.getXYT()[0];
    	    	  y_at_second_detection = odometer.getXYT()[1];
    	    	  break;
    		  }
    	  }
    	  correctAngle(1);
      }
      else if(poller.getLastRedReading(2) - poller.getCurrentRedReading(2) > 0.1) {
    	  x_at_first_detection = odometer.getXYT()[0];
    	  y_at_first_detection = odometer.getXYT()[1];
    	  while(true){
    		  if(poller.getLastRedReading(1) - poller.getCurrentRedReading(1) > 0.1){
    			  x_at_second_detection = odometer.getXYT()[0];
    	    	  y_at_second_detection = odometer.getXYT()[1];
    	    	  break;
    		  }
    	  }
    	  correctAngle(2);
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
  
  private void correctAngle(int sensor){
	  	double angleCorrection;
		double dx = x_at_second_detection - x_at_first_detection;
		double dy = y_at_second_detection - y_at_first_detection;
		double dist_between_detections = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
		double currentAngle = odometer.getXYT()[2];
		
		if(sensor == 1){
			angleCorrection = Math.toDegrees(Math.atan2(dist_between_detections, DIST_BETWEEN_SENSORS));
		}
		else{
			angleCorrection = Math.toDegrees(-Math.atan2(dist_between_detections, DIST_BETWEEN_SENSORS));
		}
		
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
