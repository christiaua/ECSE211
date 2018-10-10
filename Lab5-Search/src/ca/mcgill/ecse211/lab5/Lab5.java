package ca.mcgill.ecse211.lab5;

import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;

/**
 * 
 * @author
 */
public class Lab5 {
  private static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  private static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  private static final Port usPort = LocalEV3.get().getPort("S1");
  private static final Port colorPort = LocalEV3.get().getPort("S2");

  public static void main(String[] args) {
    // Setup ultrasonic sensor
    // There are 4 steps involved:
    // 1. Create a port object attached to a physical port (done already above)
    // 2. Create a sensor instance and attach to port
    // 3. Create a sample provider instance for the above and initialize operating mode
    // 4. Create a buffer for the sensor data
    @SuppressWarnings("resource") // Because we don't bother to close this resource
    SensorModes usSensor = new EV3UltrasonicSensor(usPort);
    SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
                                                              // this instance
    float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data are
                                                         // returned

    @SuppressWarnings("resource")
    SensorModes colorSensor = new EV3ColorSensor(colorPort);
    SampleProvider colorValue = colorSensor.getMode("Red"); // colorValue provides samples from this
                                                            // instance
    float[] colorData = new float[colorValue.sampleSize()]; // colorData is the buffer in which data
                                                            // are returned

    while (Button.waitForAnyPress() != Button.ID_ENTER) {
      System.exit(0);
    }
  }
}
