package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.lab5.Display;
import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;

public class Lab5 {

  // Motor Objects, and Robot related parameters
  public static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  public static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  public static final TextLCD lcd = LocalEV3.get().getTextLCD();
  public static final double WHEEL_RAD = 2.1;//2.2 OG
  public static final double TRACK = 12.1;//17 OG
  public static String mode = " ";

  //initialize us sensor
  private static final Port usPort = LocalEV3.get().getPort("S4");
  static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
  static SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
                                                            // this instance
  static float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data are returned
  
  private static final Port lsPort = LocalEV3.get().getPort("S1");
  static SensorModes lightSensor = new EV3ColorSensor(lsPort); // usSensor is the instance
  static SampleProvider ls = lightSensor.getMode("RGB"); // usDistance provides samples from
  static float[] lsData = new float[ls.sampleSize()];  
  
  public static void main(String[] args) throws OdometerExceptions {
	
    int buttonChoice;
    
    Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
    
    Navigation navigator = new Navigation(leftMotor, rightMotor, WHEEL_RAD, TRACK);
    
    LightLocalizer llocalizer = new LightLocalizer(ls, lsData, navigator);
    
    ColPoller lightPoller = new ColPoller(ls, lsData);
    
    Display display = new Display(lcd);

    lightPoller.start();
    
    Thread displayThread = new Thread(display);
    displayThread.start();
    
    do{
    	lcd.refresh();
    	lcd.drawString("R:" + lightPoller.getColorReading()[0], 0, 0);
    	lcd.drawString("R:" + lsData[0], 0, 0);
    	buttonChoice = Button.waitForAnyPress();
    	try {
            Thread.sleep(20);
          } catch (Exception e) {
          } 
    } while(buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
    
    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    		System.exit(0);
  }
}
