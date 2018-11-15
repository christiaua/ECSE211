/**
 * This class is meant as a skeleton for the odometer class to be used.
 * 
 * @author Rodrigo Silva
 * @author Dirk Dubois
 * @author Derek Yu
 * @author Karim El-Baba
 * @author Michael Smith
 */

package ca.mcgill.ecse211.odometer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Odometer computes the x,y position and the angle of the robot
 * based on how much the wheels turned.
 * 
 * @author Sophie Deng
 * @author Edris Jebran
 */


public class Odometer extends OdometerData implements Runnable {

  private OdometerData odoData;
  private static Odometer odo = null; // Returned as singleton

  // Motors and related variables
  private int leftMotorTachoCount;
  private int rightMotorTachoCount;
  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;

  private final double TRACK;
  private final double WHEEL_RAD;

  private static final long ODOMETER_PERIOD = 25; // odometer update period in ms

  // class variables to be used to calculate distance

  public static int lastTachoL; // Tacho/rotation speed of left motor at last sample
  public static int lastTachoR; // tacho of right motor at last sample
  public double PIE = Math.PI;

  /**
   * This is the default constructor of this class. It initiates all motors and variables once.It
   * cannot be accessed externally.
   * 
   * @param leftMotor Left motor of the robot
   * @param rightMotor Right motor of the robot
   * @throws OdometerExceptions
   */
  private Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
      final double TRACK, final double WHEEL_RAD) throws OdometerExceptions {
    odoData = OdometerData.getOdometerData(); // Allows access to x,y,z
    // manipulation methods
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;

    // Reset the values of x, y and z to 0
    odoData.setXYT(0, 0, 0);

    this.leftMotorTachoCount = 0;
    this.rightMotorTachoCount = 0;

    this.TRACK = TRACK;
    this.WHEEL_RAD = WHEEL_RAD;
  }

  /**
   * This method is meant to ensure only one instance of the odometer is used throughout the code.
   * 
   * @param leftMotor
   * @param rightMotor
   * @return new or existing Odometer Object
   * @throws OdometerExceptions
   */
  public synchronized static Odometer getOdometer(EV3LargeRegulatedMotor leftMotor,
      EV3LargeRegulatedMotor rightMotor, final double TRACK, final double WHEEL_RAD)
      throws OdometerExceptions {
    if (odo != null) { // Return existing object
      return odo;
    } else { // create object and return it
      odo = new Odometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
      return odo;
    }
  }

  /**
   * This class is meant to return the existing Odometer Object. It is meant to be used only if an
   * odometer object has been created
   * 
   * @return error if no previous odometer exists
   */
  public synchronized static Odometer getOdometer() throws OdometerExceptions {
    if (odo == null) {
      throw new OdometerExceptions("No previous Odometer exits.");
    }
    return odo;
  }

  /**
   * This method is where the logic for the odometer will run. Use the methods provided from the
   * OdometerData class to implement the odometer.
   * 
   * run method (required for Thread)
   */
  public void run() {
    long updateStart, updateEnd;
    while (true) {
      updateStart = System.currentTimeMillis();

      lastTachoL = leftMotorTachoCount; // uses last measurment for last tacho
      lastTachoR = rightMotorTachoCount;

      leftMotorTachoCount = leftMotor.getTachoCount();
      rightMotorTachoCount = rightMotor.getTachoCount();

      // Calculate new robot position based on tachometer counts
      double distL, distR, deltaD, deltaT, dX, dY;
      distL = PIE * odo.WHEEL_RAD * (leftMotorTachoCount - lastTachoL) / 180;// odo.WHEEL_RAD is
                                                                             // wheel radius
      distR = PIE * odo.WHEEL_RAD * (rightMotorTachoCount - lastTachoR) / 180;
      deltaD = (distR + distL) / 2.0; // displacement

      lastTachoL = leftMotorTachoCount; // saving tacho counts for next iteration
      lastTachoR = rightMotorTachoCount;
      double currTheta = odo.getXYT()[2] * PIE / 180; // gives current Theta in radians

      deltaT = (distL - distR) / odo.TRACK; // odo.track is WB, gives deltaT in radians
      currTheta += deltaT;
      dX = Math.sin(currTheta) * deltaD; // robot's displacement in the x direction
      dY = Math.cos(currTheta) * deltaD;// robot's displacement in the y direction


      odo.update(dX, dY, deltaT * 180 / PIE); // since Theta is stored in radians

      // this ensures that the odometer only runs once every period
      updateEnd = System.currentTimeMillis();
      if (updateEnd - updateStart < ODOMETER_PERIOD) {
        try {
          Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
        } catch (InterruptedException e) {
          // there is nothing to be done
        }
      }
    }
  }
}
