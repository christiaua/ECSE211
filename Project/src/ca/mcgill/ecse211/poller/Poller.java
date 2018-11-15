package ca.mcgill.ecse211.poller;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.odometer.OdometryCorrection;
import ca.mcgill.ecse211.poller.RingDetector.ColourType;
import ca.mcgill.ecse211.project.Navigation;
import ca.mcgill.ecse211.project.Navigation.Side;

/**
 * This class gets the samples for the light sensors
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 * @author Sophie Deng
 */
public class Poller implements Runnable {

  // Class control variables
  private volatile static int numberOfIntances = 0;
  private static final int MAX_INSTANCES = 1;
  private static Poller poller = null;
  private static RingDetector ringDetector;
  private static USSensorData sensorData;

  private Navigation navigation;
  private OdometryCorrection odometryCorrector;
  
  //TODO: Is there a better way to do this?s
  private static int tachoL = -1000; // -1000 dummy value so that we know if tachoL/tachoR hasn't
                                     // been set yet
  private static int tachoR = -1000;

  // initialize us sensor
  private static final Port usPort = LocalEV3.get().getPort("S4");
  static SensorModes usSensor = new EV3UltrasonicSensor(usPort);
  static SampleProvider us = usSensor.getMode("Distance");
  static float[] usData = new float[us.sampleSize()];

  private double unfilteredDistance;

  // initialize color sensors
  private static final Port colorSensorPort = LocalEV3.get().getPort("S1");
  static SensorModes colorSensor = new EV3ColorSensor(colorSensorPort);
  static SampleProvider rgbSample = colorSensor.getMode("RGB");
  static float[] rgbData = new float[rgbSample.sampleSize()];

  // initialize line detecting sensors
  private static final Port lsPort1 = LocalEV3.get().getPort("S2");
  static SensorModes lightSensor1 = new EV3ColorSensor(lsPort1);
  static SampleProvider redSample1 = lightSensor1.getMode("Red");
  static float[] redData1 = new float[redSample1.sampleSize()];

  private static final Port lsPort2 = LocalEV3.get().getPort("S3");
  static SensorModes lightSensor2 = new EV3ColorSensor(lsPort2);
  static SampleProvider redSample2 = lightSensor2.getMode("Red");
  static float[] redData2 = new float[redSample2.sampleSize()];

  private float lastRedReading1, lastRedReading2;
  private float currentRedReading1, currentRedReading2;

  private boolean correctionEnabled = true;

  /**
   * Constructor
   * 
   * @throws PollerException
   * @throws OdometerExceptions
   */
  public Poller(Navigation nav) throws PollerException, OdometerExceptions {
    ringDetector = RingDetector.getRingDetector();
    sensorData = USSensorData.getSensorData();
    this.navigation = nav;
    this.odometryCorrector = new OdometryCorrection();
  }

  /**
   * Sets the correction
   * 
   * @param isEnabled
   */
  public void enableCorrection(boolean isEnabled) {
    correctionEnabled = isEnabled;
  }

  /**
   * Gets the one instance of the class
   * 
   * @return the Poller
   * @throws PollerException
   * @throws OdometerExceptions
   */
  public static Poller getPoller(Navigation nav) throws PollerException, OdometerExceptions {
    if (poller != null) { // Return existing object
      return poller;
    } else if (numberOfIntances < MAX_INSTANCES) { // create object and return it
      poller = new Poller(nav);
      numberOfIntances += 1;
      return poller;
    } else {
      throw new PollerException("Only one intance of the Poller can be created."); // TODO: Does
                                                                                   // this ever
                                                                                   // occur?
    }
  }

  public static Poller getPoller() throws PollerException {
    if (poller != null) { // Return existing object
      return poller;
    } else {
      throw new PollerException("No Poller.");
    }
  }

  /**
   * Get the samples and process them
   */
  public void run() {
    while (true) {
      redSample1.fetchSample(redData1, 0);
      redSample2.fetchSample(redData2, 0);

      if (correctionEnabled) {
        if (!navigation.isTurning() && redData1[0] < 0.33 && tachoL == -1000) {
          tachoL = navigation.getTacho(Side.LEFT);
        }
        if (!navigation.isTurning() && redData2[0] < 0.33 && tachoR == -1000) {
          tachoR = navigation.getTacho(Side.RIGHT);
        }
        if (!navigation.isTurning() && tachoL != -1000 && tachoR != -1000) {
          odometryCorrector.correctAngle(tachoL, tachoR);
          tachoL = -1000;
          tachoR = -1000;
          Sound.beep();
        }
      }
      
      lastRedReading1 = currentRedReading1;
      currentRedReading1 = redData1[0];

      lastRedReading2 = currentRedReading2;
      currentRedReading2 = redData2[0];

      rgbSample.fetchSample(rgbData, 0);
      ringDetector.processRGBData(rgbData[0], rgbData[1], rgbData[2]);

      us.fetchSample(usData, 0); // acquire data
      unfilteredDistance = (usData[0] * 100.0); // extract from buffer, cast to int

      sensorData.updateDistance(unfilteredDistance);
      try {
        Thread.sleep(10);
      } catch (Exception e) {
      }
    }
  }

  /**
   * Get red value of specified sensor
   * 
   * @param side The light sensor from which to get data from.
   * @param current 
   * @return sensor reading
   */
  public float getLightSensorData(Side side, boolean current) {
    if(current){
      if(side == Side.LEFT) {
        return currentRedReading1;
      } else {
        return currentRedReading2;
      }
    } else {
      if(side == Side.LEFT) {
        return lastRedReading1;
      } else {
        return lastRedReading2;
      }
    }
  }

  /**
   * Get the filtered ultrasonic sensor reading.
   * 
   * @return distance The filtered ultrasonic sensor reading.
   */
  public double getDistance() {
    return sensorData.getDistance();
  }

  /**
   * Get the previous filtered ultrasonic sensor reading.
   * 
   * @return distance The previous filtered ultrasonic sensor reading.
   */
  public double getLastDistance() {
    return sensorData.getLastDistance();
  }

  /**
   * Get the colour of the detected ring.
   * 
   * @return colour The colour of the detected ring.
   */
  public ColourType getColour() {
    return ringDetector.getColourType();
  }
}
