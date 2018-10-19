package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.lab5.LightLocalizer;
import ca.mcgill.ecse211.lab5.UltrasonicLocalizer;
import ca.mcgill.ecse211.lab5.BangBangController;
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

/**
 * This class runs the lab 5
 * 
 * @author Sophie Deng
 * @author Edward Huang
 */
public class Lab5 {
	
	//CUSTOM VARIABLES
	private static final int LLx = 2;
	private static final int LLy = 2;
	private static final int URx = 5;
	private static final int URy = 5;
	private static final int TR = 1;
	private static final int SC = 0;
	

	// Motor Objects, and Robot related parameters
	public static final EV3LargeRegulatedMotor leftMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	public static final EV3MediumRegulatedMotor sensorMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));
	public static final TextLCD lcd = LocalEV3.get().getTextLCD();
	public static final double WHEEL_RAD = 2.1;//2.2 OG
	public static final double TRACK = 12.1;//17 OG
	public static String mode = " ";
	private static final int wallFollowingHighSpeed = 100;
	private static final int wallFollowingLowSpeed = 33;
	public static final int wallFollowingBandCenter = 20;
	private static final int wallFollowingBandWidth = 1;
	public static BangBangController bangbangcontroller = new BangBangController(wallFollowingBandCenter, wallFollowingBandWidth, wallFollowingLowSpeed
			, wallFollowingHighSpeed, leftMotor, rightMotor);

	//initialize us sensor
	private static final Port usPort = LocalEV3.get().getPort("S4");
	static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
	static SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
	// this instance
	static float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data are returned

	//initialize color sensor
	private static final Port colorSensorPort = LocalEV3.get().getPort("S1");
	static SensorModes colorSensor = new EV3ColorSensor(colorSensorPort); // usSensor is the instance
	static SampleProvider colorS = colorSensor.getMode("RGB"); // usDistance provides samples from
	static float[] rgbData = new float[colorS.sampleSize()];  

	//initialize line detecting sensor
	private static final Port lsPort = LocalEV3.get().getPort("S2");
	static SensorModes lightSensor = new EV3ColorSensor(lsPort); // usSensor is the instance
	static SampleProvider ls = lightSensor.getMode("Red"); // usDistance provides samples from
	static float[] redData = new float[ls.sampleSize()];  

	public static void main(String[] args) throws OdometerExceptions {

		int buttonChoice;
		buttonChoice = Button.waitForAnyPress(); //wait for button before starting

		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);

		Navigation navigator = new Navigation(leftMotor, rightMotor, sensorMotor, WHEEL_RAD, TRACK, bangbangcontroller);

		UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData);

		ColPoller lightPoller = new ColPoller(colorS, rgbData, ls, redData, TR);

		Display display = new Display(lcd);

		lightPoller.start();
		usPoller.start();

		UltrasonicLocalizer usLocalizer = new UltrasonicLocalizer(usPoller, navigator);
		LightLocalizer lsLocalizer = new LightLocalizer(lightPoller, navigator);


		Thread displayThread = new Thread(display);
		displayThread.start();
		Thread odoThread = new Thread(odometer);
		odoThread.start();

		do{
			buttonChoice = Button.waitForAnyPress();
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			} 
		} while(buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
		
		//wait for button and starts ultrasonic localizer
		usLocalizer.fallingEdge();

		do{
			buttonChoice = Button.waitForAnyPress();
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			} 
		} while(buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		//wait for button and moves to (1,1) and does light localization
		lsLocalizer.moveToOrigin(); 

		do{
			buttonChoice = Button.waitForAnyPress();
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			} 
		} while(buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
		//wait for button and goes to (LLX, LLY)
		navigator.travelTo(LLx, LLy);
		
		//zigzags through all the lines
		int diffX = URx - LLx;
		int diffY = URy - LLy;
		boolean isLowerLine = true;
		
		for(int i = LLy+1; i < URy; i++) {
			if(isLowerLine) {
				navigator.travelTo(LLx, i);
				navigator.travelTo(URx, i);
				isLowerLine = false;
			}
			else {
				navigator.travelTo(URx, i);
				navigator.travelTo(LLx, i);
				isLowerLine = true;
			}
		}
			
		//if target found, go to upper right corner
		navigator.travelTo(URx, URy);

		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
