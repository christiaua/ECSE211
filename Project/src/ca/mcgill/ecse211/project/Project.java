package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.odometer.OdometryCorrection;
import ca.mcgill.ecse211.poller.Poller;
import ca.mcgill.ecse211.poller.PollerException;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

public class Project {
	// CUSTOM VARIABLES
	private static final int LLx = 3;
	private static final int LLy = 3;
	private static final int URx = 7;
	private static final int URy = 7;
	private static final int TR = 4; // 1 BLUE, 2 GREEN, 3 YELLOW, 4 ORANGE
	private static final int SC = 0;
	private static final int[][] CORNERS = {{1, 1}, {1, 7}, {7, 7}, {7, 1}};

	public static final TextLCD lcd = LocalEV3.get().getTextLCD();
	private static Display display;
	private static Odometer odometer;
	private static Poller poller;
	private static Navigation navigation;
	private static UltrasonicLocalizer usLocalizer;
	private static LightLocalizer lightLocalizer;
	private static OdometryCorrection odometryCorrector;

	public static void main(String[] args) throws OdometerExceptions, PollerException{
		do {
			int buttonChoice;
			do {
				// clear the display
				lcd.clear();

				// ask the user whether the motors should do lab 5 or float
				lcd.drawString("< Left | Right >", 0, 0);
				lcd.drawString("       |        ", 0, 1);
				lcd.drawString("  Test | Drive  ", 0, 2);
				lcd.drawString("motors | and do ", 0, 3);
				lcd.drawString("       | Project", 0, 4);

				buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)
			} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
			
			Navigation navigation = new Navigation();

			lcd.clear();
			if (buttonChoice == Button.ID_LEFT) {
				// test the track and wheels
				navigation.turnTo(90);
				navigation.moveForward(120, false);
				navigation.floatWheels();
				System.exit(0);
			}
			
			else if(buttonChoice == Button.ID_RIGHT){
				odometer = Odometer.getOdometer();
				Thread odoThread = new Thread(odometer);
				odoThread.start();

				poller = Poller.getPoller();
				Thread pollerThread = new Thread(poller);
				pollerThread.start();

				display = new Display(lcd);
				Thread displayThread = new Thread(display);
				displayThread.start();
				
				usLocalizer = new UltrasonicLocalizer();
				lightLocalizer = new LightLocalizer();
				
				//usLocalizer.fallingEdge();
				//lightLocalizer.moveToOrigin();
				
				odometryCorrector = new OdometryCorrection();
				Thread correctionThread = new Thread(odometryCorrector);
				
				correctionThread.start();
				
				navigation.turnTo(7);
				odometer.setTheta(0);
				navigation.travelTo(0, 2);
				navigation.turnTo(0);
			}
			
			buttonChoice = Button.waitForAnyPress();
		} while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
