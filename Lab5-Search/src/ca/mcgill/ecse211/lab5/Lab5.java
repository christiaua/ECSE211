package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;


/**
 * This class runs the lab 5
 * 
 * @author Sophie Deng
 * @author Edward Huang
 */
public class Lab5 {

  // CUSTOM VARIABLES
  private static final int LLx = 3;
  private static final int LLy = 3;
  private static final int URx = 7;
  private static final int URy = 7;
  private static final int TR = 2; // 1 BLUE, 2 GREEN, 3 YELLOW, 4 ORANGE
  private static final int SC = 0;
  private static final int[][] CORNERS = {{1, 1}, {1, 7}, {7, 7}, {7, 1}};


  // Motor Objects, and Robot related parameters
  public static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  public static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  public static final EV3MediumRegulatedMotor sensorMotor =
      new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

  public static final TextLCD lcd = LocalEV3.get().getTextLCD();
  public static final double WHEEL_RAD = 2.2;// 2.2 OG
  public static final double TRACK = 12.3;// 17 OG
  public static String mode = " ";
  private static final double TILE_SIZE = 30.48;

  private static final int wallFollowingHighSpeed = 115;
  private static final int wallFollowingLowSpeed = 50;
  public static final int wallFollowingBandCenter = 10;
  private static final int wallFollowingBandWidth = 1;

  public static BangBangController bangbangcontroller =
      new BangBangController(wallFollowingBandCenter, wallFollowingBandWidth, wallFollowingLowSpeed,
          wallFollowingHighSpeed, leftMotor, rightMotor);

  // initialize us sensor
  private static final Port usPort = LocalEV3.get().getPort("S4");
  static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
  static SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples
                                                                   // from this instance
  static float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data
                                                              // are returned

  // initialize color sensors
  private static final Port colorSensorPort = LocalEV3.get().getPort("S1");
  static SensorModes colorSensor = new EV3ColorSensor(colorSensorPort);
  static SampleProvider colorS = colorSensor.getMode("RGB");
  static float[] rgbData = new float[colorS.sampleSize()];

  private static final Port color2Port = LocalEV3.get().getPort("S3");
  static SensorModes color2Sensor = new EV3ColorSensor(color2Port);
  static SampleProvider colorS2 = color2Sensor.getMode("RGB");
  static float[] rgbData2 = new float[colorS2.sampleSize()];

  // initialize line detecting sensor
  private static final Port lsPort = LocalEV3.get().getPort("S2");
  static SensorModes lightSensor = new EV3ColorSensor(lsPort);
  static SampleProvider ls = lightSensor.getMode("Red");
  static float[] redData = new float[ls.sampleSize()];

  /**
   * Main, runs lab 5
   * 
   * @param args
   * @throws OdometerExceptions
   */
  public static void main(String[] args) throws OdometerExceptions {
    lcd.drawString("Ready", 0, 0);
    int buttonChoice;
    do {
      // clear the display
      lcd.clear();

      // ask the user whether the motors should do lab 5 or float
      lcd.drawString("< Left | Right >", 0, 0);
      lcd.drawString("       |        ", 0, 1);
      lcd.drawString("  Test | Drive  ", 0, 2);
      lcd.drawString("motors | and do ", 0, 3);
      lcd.drawString("       | lab 5  ", 0, 4);

      buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)
    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

    lcd.clear();
    if (buttonChoice == Button.ID_LEFT) {

      Navigation navigator =
          new Navigation(leftMotor, rightMotor, sensorMotor, WHEEL_RAD, TRACK, bangbangcontroller);

      // test the angles
      // navigator.turnTo(90);
      navigator.moveForward(120, false);
      leftMotor.forward();
      leftMotor.flt();
      rightMotor.forward();
      rightMotor.flt();
      System.exit(0);
    }

    Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);

    Navigation navigator =
        new Navigation(leftMotor, rightMotor, sensorMotor, WHEEL_RAD, TRACK, bangbangcontroller);

    Poller lightPoller =
        new Poller(colorS, rgbData, colorS2, rgbData2, ls, redData, TR, usDistance, usData);

    lightPoller.start();

    UltrasonicLocalizer usLocalizer = new UltrasonicLocalizer(lightPoller, navigator);
    LightLocalizer lsLocalizer = new LightLocalizer(lightPoller, navigator);


    Thread odoThread = new Thread(odometer);
    odoThread.start();

    do {
      buttonChoice = Button.waitForAnyPress();
      try {
        Thread.sleep(20);
      } catch (Exception e) {
      }
    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

    // wait for button and starts ultrasonic localizer
    usLocalizer.fallingEdge();

    // wait for button and moves to (1,1) and does light localization
    lsLocalizer.moveToOrigin();

    switch (SC) {
      case 0:
        odometer.setX(TILE_SIZE);
        odometer.setY(TILE_SIZE);
        break;
      case 1:
        odometer.setX(7 * TILE_SIZE);
        odometer.setY(TILE_SIZE);
        odometer.setTheta(270);
        break;
      case 2:
        odometer.setX(7 * TILE_SIZE);
        odometer.setY(7 * TILE_SIZE);
        odometer.setTheta(180);
        break;
      case 3:
        odometer.setX(TILE_SIZE);
        odometer.setY(7 * TILE_SIZE);
        odometer.setTheta(90);
        break;
      default:
        break;
    }

    switch (SC) {
      case 2:
        navigator.travelToWhileSearching(CORNERS[1][0], CORNERS[1][1]);
        break;
      default:
        break;
    }

    navigator.travelToWhileSearching(LLx, LLy);
    Sound.beep();

    // Zigzags through all the lines
    boolean isLeftLine = true;
    boolean firstPass = true;

    for (int i = LLy; i <= URy; i++) {
      if (RingDetector.targetDetected()) {
    	  break;  
      }
      if (!RingDetector.targetDetected()) {
        if (isLeftLine) {
          if (firstPass) {
            navigator.travelToWhileSearching(URx + 0.5, i);
            isLeftLine = false;
            firstPass = false;
            continue;
          }
          navigator.travelToWhileSearching(LLx - 0.5, i);
          navigator.travelToWhileSearching(URx + 0.5, i);
          isLeftLine = false;
        } else {
          navigator.travelToWhileSearching(URx + 0.5, i);
          navigator.travelToWhileSearching(LLx - 0.5, i);
          isLeftLine = true;
        }
      } else {
        break;
      }
    }

    // go to upper right corner
    navigator.travelToAvoidance(URx, URy);
    Sound.beep();

    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }
}
