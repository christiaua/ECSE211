package test;

import lejos.hardware.sensor.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;
import lejos.hardware.Button;

public class Test {
	private static final int ACTUAL_DISTANCE = 35; //actual distance between us sensor and object
	
	public static final double WHEEL_RAD = 2.1;
	
  private static final Port usPort = LocalEV3.get().getPort("S1");
  static SensorModes usSensor = new EV3UltrasonicSensor(usPort);
  static SampleProvider usDistance = usSensor.getMode("Distance");
  static float[] usData = new float[usDistance.sampleSize()]; 
  
  private static final Port lsPort = LocalEV3.get().getPort("S2");
	static SensorModes lightSensor = new EV3ColorSensor(lsPort); // usSensor is the instance
	static SampleProvider ls = lightSensor.getMode("Red"); // usDistance provides samples from
	static float[] redData = new float[ls.sampleSize()];  
  
  public static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  public static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  
  public static final TextLCD lcd = LocalEV3.get().getTextLCD();

  public static void main(String[] args) {

    int option = 0;
    
    lcd.drawString("Press left to start US testing", 0, 0);
    lcd.drawString("Press right to start LS testing", 0, 1);
    
    while (option == 0)
      option = Button.waitForAnyPress();
    
    switch (option) {
      case Button.ID_LEFT:
    	  float[] samples;
    	  Poller usPoller = new Poller(usDistance, usData);
    	  samples = usPoller.fetchSample(1000);
    	  System.out.println("Sample, Measured distance (cm), Actual distance (cm)");
    	  for(int i = 0; i < samples.length; i++){
    		  System.out.println(i + ", " + (samples[i] * 100) + ", " + ACTUAL_DISTANCE);
    	  }
        break;
      case Button.ID_RIGHT:
    	  float[] samples2;
    	  Poller lsPoller = new Poller(ls, redData);
    	  samples2 = lsPoller.fetchSample(100);
    	  System.out.println("Sample, Reading");
    	  for(int i = 0; i < samples2.length; i++){
    		  System.out.println(i + ", " + samples2[i]);
    	  }
    	  /*LightPoller lightPoller = new LightPoller(ls, redData);
    	  lightPoller.start();
    	  Controller cont = new Controller(leftMotor, rightMotor, WHEEL_RAD);
    	  cont.moveForward(90, false);*/
        break;
      default:
        System.out.println("Error - invalid button"); // None of the above - abort
        System.exit(-1);
        break;
    }
    // Wait here forever until button pressed
    Button.waitForAnyPress();
    System.exit(0);

  }
}
