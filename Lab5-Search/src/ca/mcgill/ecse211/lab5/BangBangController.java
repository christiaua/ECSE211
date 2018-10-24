package ca.mcgill.ecse211.lab5;

import lejos.hardware.motor.*;

/**
 * This class follows an obstacle with the bang bang controller
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 */
public class BangBangController implements UltrasonicController {
  private static final int FILTER_OUT = 20;

  public final int bandCenter;
  private final int bandwidth;
  private final int motorLow;
  private final int motorHigh;
  private int distance;
  private int filterControl;
  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;

  /**
   * BangBang Controller constructor
   * 
   * @param bandCenter
   * @param bandwidth
   * @param motorLow
   * @param motorHigh
   * @param leftMotor
   * @param rightMotor
   */
  public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh,
      EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
    // Default Constructor
    this.bandCenter = bandCenter;
    this.bandwidth = bandwidth;
    this.motorLow = motorLow;
    this.motorHigh = motorHigh;
    this.filterControl = 0;
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
  }

  @Override
  public void processUSData(int distance) {
    this.distance = distance;
    if (distance >= 255 && filterControl < FILTER_OUT) {
      // bad value, do not set the distance var, however do increment the
      // filter value
      filterControl++;
    } else if (distance >= 255) {
      // We have repeated large values, so there must actually be nothing
      // there: leave the distance alone
      this.distance = distance;
    } else {
      // distance went below 255: reset filter and leave
      // distance alone.
      filterControl = 0;
      this.distance = distance;
    }
    // process a movement based on the us distance passed in (BANG-BANG style)
    if (Math.abs(this.distance - bandCenter) <= bandwidth) {
      // check if the difference between distance and bandCenter is within the bandwidth
      this.leftMotor.setSpeed(motorHigh);
      this.rightMotor.setSpeed(motorHigh);
      this.leftMotor.forward();
      this.rightMotor.forward();
    } else if (this.distance - bandCenter < 0) {
      // check if robot is too close to the wall
      this.leftMotor.setSpeed(motorLow); // set left motor speed to motorLow
      this.rightMotor.setSpeed(motorHigh); // set right motor speed to motorHigh
      this.leftMotor.forward();
      this.rightMotor.forward();
    } else if (this.distance - bandCenter > 0) {
      // check if robot is too far from the wall
      this.leftMotor.setSpeed(motorHigh); // set left motor speed to motorHigh
      this.rightMotor.setSpeed(motorLow); // set right motor speed to motorLow
      this.leftMotor.forward();
      this.rightMotor.forward();
    }
  }

  @Override
  public int readUSDistance() {
    return this.distance;
  }
}
