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
	private static final int TLLx = 2;
	private static final int TLLy = 3;
	private static final int TURx = 3;
	private static final int TURy = 5;
	private static final int TGx = 7;
	private static final int TGy = 7;
	private static final int URx = 8;
	private static final int URy = 3;
	
	private static final double[] displacementX = {0.5, 0, -0.5};
	private static final double[] displacementY = {0.5, 1, 0,5};
	
//	private static final int TR = 4; // 1 BLUE, 2 GREEN, 3 YELLOW, 4 ORANGE
//	private static final int SC = 0;
//	private static final int[][] CORNERS = {{1, 1}, {1, 7}, {7, 7}, {7, 1}};

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
			
			navigation = new Navigation();

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
				
				//TODO: beta demo algorithm
				navigation.travelTo(URx-0.5, URy-0.5);
				navigation.travelTo(TLLx+0.5, TLLy-0.5);
				odometryCorrector.disable();
				navigation.travelTo(TURx-0.5, TURy+0.5);
				odometryCorrector.enable();
				navigation.travelTo(TGx, TURy+0.5);
				navigation.travelTo(TGx, TURy-0.5);

				while(!poller.foundRing()) {
					for(int i = 0; i < 3; i++) {
						navigation.travelTo(TGx+displacementX[i], TGy+displacementY[i]);
					}	
				}
				//TODO: take ring
			}
			
			buttonChoice = Button.waitForAnyPress();
		} while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
