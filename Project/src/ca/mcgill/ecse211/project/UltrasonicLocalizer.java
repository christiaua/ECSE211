package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.odometer.*;
import ca.mcgill.ecse211.poller.*;
import lejos.hardware.Sound;

/**
 * Ultrasonic Localizer
 * 
 * @author Edward huang
 * @author Sophie Deng
 */
public class UltrasonicLocalizer {

  private Odometer odo;
  public static final double D = 30;
  public static final double THRESHOLD = 1;
  private Poller poller = null;
  private static int angleCorrection = 0;
  private static Navigation navigation;

  /**
   * Constructor
   * 
   * @param poller
   * @param nav
   * @throws OdometerExceptions
 * @throws PollerException 
   */
  public UltrasonicLocalizer(Navigation nav) throws OdometerExceptions, PollerException {
    this.odo = Odometer.getOdometer();
    this.poller = Poller.getPoller();
    UltrasonicLocalizer.navigation = nav;
  }

  /**
   * This method is used to find the angle of the robot assuming it is located in the bottom left
   * corner of the grid, and make it turn to 0 degrees. It does so by using the ultrasonic sensor
   * values and detecting the two relative positions where the sensor values falls under a given
   * constant, and sets the midpoint between these values to be the 45 degree angle.
   */
  public void fallingEdge() {
    double[] fallingEdgeAngle = new double[2]; // angle at which falling edges are detected
    double temp1; // holds temp angle values of when the ultrasonic sensor
    double temp2; // when the distance detected falls within the threshold and when it
                  // falls out of the threshold
    double actualHeading;
    double dtheta;

    navigation.rotate(360, true);

    for (int i = 0; i < 2; i++) {
      while (true) {
        if (poller.getLastDistance() > (D + THRESHOLD) && poller.getDistance() < (D + THRESHOLD)) {
          temp1 = odo.getXYT()[2];
          while (true) {
            if (poller.getDistance() < D - THRESHOLD) {
              temp2 = odo.getXYT()[2];
              Sound.beep(); // falling edge detected;
              break;
            }
            try {
              Thread.sleep(25);
            } catch (Exception e) {
            }
          }
          fallingEdgeAngle[i] = (temp1 + temp2) / 2;
          navigation.stop();
          if (i == 0) {
            navigation.rotate(-360, true);
          }
          break;
        }
        try {
          Thread.sleep(40);
        } catch (Exception e) {
        }
      }
    }

    if (fallingEdgeAngle[0] < fallingEdgeAngle[1]) {
      dtheta = 225 + angleCorrection - (fallingEdgeAngle[0] + fallingEdgeAngle[1]) / 2;
    } else {
      dtheta = 45 + angleCorrection - (fallingEdgeAngle[0] + fallingEdgeAngle[1]) / 2;
    }

    actualHeading = odo.getXYT()[2] + dtheta;
    odo.setTheta(actualHeading);
    navigation.turnTo(0);
  }

  /**
   * This method is used to find the angle of the robot assuming it is located in the bottom left
   * corner of the grid, and make it turn to 0 degrees. It does so by using the ultrasonic sensor
   * values and detecting the two relative positions where the sensor values rises above a given
   * constant, and sets the midpoint between these values to be the 225 degree angle.
   */
  public void risingEdge() {
    double[] risingEdgeAngle = new double[2]; // angle at which falling edges are detected
    double temp1; // holds temp angle values of when the ultrasonic sensor
    double temp2; // when the distance detected falls within the threshold and when it
                  // falls out of the threshold
    double actualHeading;
    double dtheta;

    navigation.rotate(360, true);

    for (int i = 0; i < 2; i++) {
      while (true) {
        if (poller.getLastDistance() < (D - THRESHOLD) && poller.getDistance() > (D - THRESHOLD)) {
          temp1 = odo.getXYT()[2];
          while (true) {
            if (poller.getDistance() > D + THRESHOLD) {
              temp2 = odo.getXYT()[2];

              Sound.beep(); // falling edge detected;

              break;
            }
            try {
              Thread.sleep(25);
            } catch (Exception e) {
            }
          }
          risingEdgeAngle[i] = (temp1 + temp2) / 2;
          navigation.stop();
          if (i == 0) {
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

    if (risingEdgeAngle[0] > risingEdgeAngle[1]) {
      dtheta = 225 + angleCorrection - (risingEdgeAngle[0] + risingEdgeAngle[1]) / 2;
    } else {
      dtheta = 45 + angleCorrection - (risingEdgeAngle[0] + risingEdgeAngle[1]) / 2;
    }

    actualHeading = odo.getXYT()[2] + dtheta;
    odo.setTheta(actualHeading);
    navigation.turnTo(0);
  }
}
