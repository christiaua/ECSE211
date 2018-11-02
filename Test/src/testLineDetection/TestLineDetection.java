package testLineDetection;

import lejos.hardware.sensor.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;
import lejos.hardware.Button;

public class TestLineDetection {
  private static final double WHEEL_RAD = 2.1;
  private static final double DISTANCE_TO_TRAVEL = 120;
  
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
    
    lcd.drawString("Press to start", 0, 0);
    
    while (option == 0)
      option = Button.waitForAnyPress();
    
    LightPoller lightPoller = new LightPoller(ls, redData);
    lightPoller.start();
    Controller cont = new Controller(leftMotor, rightMotor, WHEEL_RAD);
 	cont.moveForward(DISTANCE_TO_TRAVEL, true);

    // Wait here forever until button pressed
    Button.waitForAnyPress();
    System.exit(0);

  }
}
