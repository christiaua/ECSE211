package ca.mcgill.ecse211.project;

import lejos.hardware.Sound;
import ca.mcgill.ecse211.odometer.*;
import ca.mcgill.ecse211.poller.*;
import ca.mcgill.ecse211.project.Navigation.Side;

/**
 * This class does the light localization
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 */
public class LightLocalizer {
  private Odometer odo;

  private static final int MAX_DISTANCE = 70;
  private Poller poller;
  private double x;
  private double y;
  private static final double D = 10;
  private double dthetaY;
  private Navigation navigation;
  private static final double TILE_SIZE = 30.48;
  private static final double FIELD_WIDTH = 8 * TILE_SIZE;
  private static final double FIELD_HEIGHT = 8 * TILE_SIZE;

  /**
   * constructor
   * 
   * @param lightPoller
   * @param nav
   * @throws OdometerExceptions
   * @throws PollerException
   */
  public LightLocalizer(Navigation nav) throws OdometerExceptions, PollerException {
    this.poller = Poller.getPoller();
    this.odo = Odometer.getOdometer();
    this.navigation = nav;
  }

  /**
   * Performs localization using the robot's light sensors. 
   * Moves to the corner of the starting tile when done.
   * @param SC The starting corner (0, 1, 2, or 3).
   */
  public void moveToOrigin(int SC) {
    int lineCount = 0;
    double[] theta = new double[4];
    double thetaY;
    double thetaX;

    Navigation.turnTo(45);

    Navigation.moveForward(MAX_DISTANCE, true);

    while (true) {
      // checks if robot crosses a line while moving
      if (navigation.isNavigating() && poller.getLightSensorData(Side.LEFT, true) < 0.33) {
        Navigation.stop();
        Sound.beep();
        break;
      }
      // checks if robot stopped moving without seeing a line
      if (!navigation.isNavigating()) {
        // go backwards until seeing a line
        Navigation.backwards();
      }
    }

    Navigation.moveForward(-14, false);

    // perform a 360
    Navigation.rotate(360, true);

    while (navigation.isNavigating()) {
      if (poller.getLightSensorData(Side.LEFT, false)
          - poller.getLightSensorData(Side.LEFT, true) > 0.1) {
        Sound.beep();
        theta[lineCount] = odo.getXYT()[2];
        lineCount++;
      }
    }

    thetaY = angleDiff(theta[1], theta[3]);
    thetaX = angleDiff(theta[0], theta[2]);

    // calculate and set new odometer values
    x = -D * Math.cos(thetaY / 2 * Math.PI / 180);
    y = -D * Math.cos(thetaX / 2 * Math.PI / 180);
    odo.setX(x);
    odo.setY(y);
    dthetaY = -90 - theta[3] + thetaY / 2;
    odo.update(0, 0, (dthetaY - 15));
    Navigation.travelTo(0, 0);
    Navigation.turnTo(0);

    switch (SC) {
      case 0:
        odo.setXYT(TILE_SIZE, TILE_SIZE, 0);
        break;
      case 1:
        odo.setXYT(FIELD_WIDTH - TILE_SIZE, TILE_SIZE, 270);
        break;
      case 2:
        odo.setXYT(FIELD_WIDTH - TILE_SIZE, FIELD_HEIGHT - TILE_SIZE, 180);
        break;
      case 3:
        odo.setXYT(TILE_SIZE, FIELD_HEIGHT - TILE_SIZE, 90);
        break;
    }
  }


  /**
   * This method takes two angles as parameters and returns their positive minimum angle difference
   * in degrees.
   * 
   * @param a1 First angle in degrees.
   * @param a2 Second angle in degrees.
   * @return
   */
  public static double angleDiff(double a1, double a2) {
    double diff = a1 - a2;
    if (diff > 180) {
      return Math.abs(diff - 360);
    } else if (diff < -180) {
      return diff + 360;
    } else {
      return Math.abs(diff);
    }
  }
}
