package ca.mcgill.ecse211.project;

import java.util.Map;
import ca.mcgill.ecse211.WiFiClient.WifiConnection;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.odometer.OdometryCorrection;
import ca.mcgill.ecse211.poller.Poller;
import ca.mcgill.ecse211.poller.PollerException;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

public class Project {
	// CUSTOM VARIABLES
	//T tunnel
	private static int TLLx = 2;
	private static int TLLy = 3;
	private static int TURx = 3;
	private static int TURy = 5;
	private static int TGx = 7;
	private static int TGy = 7;
	//no prefix starting zone
	private static int URx = 8;
	private static int URy = 3;
	private static int LLx = 3;
	private static int LLy = 3;
	//I island
	private static int IURx = 8;
	private static int IURy = 3;
	private static int ILLx = 3;
	private static int ILLy = 3;
	//starting corner
	private static int SC = 0;
	
	private static final double[] displacementX = { 0.5, 0, -0.5 };
	private static final double[] displacementY = { 0.5, 1, 0, 5 };

	// private static final int TR = 4; // 1 BLUE, 2 GREEN, 3 YELLOW, 4 ORANGE
	
	// private static final int[][] CORNERS = {{1, 1}, {1, 7}, {7, 7}, {7, 1}};

	public static final TextLCD lcd = LocalEV3.get().getTextLCD();
	private static Display display;
	private static Odometer odometer;
	private static Poller poller;
	private static Navigation navigation;
	private static UltrasonicLocalizer usLocalizer;
	private static LightLocalizer lightLocalizer;
	private static RingSearch ringSearch;

	private static final String SERVER_IP = "192.168.2.1";
	private static final int TEAM_NUMBER = 7;

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws OdometerExceptions, PollerException {

		WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, true);
		try {
			Map data = conn.getData();
			System.out.println("Map:\n" + data);

			//team specifics
			int redTeam = ((Long) data.get("GreenTeam")).intValue();
			System.out.println("Red Team: " + redTeam);

			//ringset location
			TGx = ((Long) data.get("TG_x")).intValue();
			System.out.println("X component of the Green ring tree: " + TGx);
			TGy = ((Long) data.get("TG_y")).intValue();

			//tunnel
			TLLx = ((Long) data.get("TNG_LL_x")).intValue();
			TLLy = ((Long) data.get("TNG_LL_y")).intValue();
			TURx = ((Long) data.get("TNG_UR_x")).intValue();
			TURy = ((Long) data.get("TNG_UR_y")).intValue();
			
			//zone
			URx = ((Long) data.get("Green_UR_x")).intValue();
			URy = ((Long) data.get("Green_UR_y")).intValue();
			LLx = ((Long) data.get("Green_LL_x")).intValue();
			LLy = ((Long) data.get("Green_LL_y")).intValue();
			
			//island
			IURx = ((Long) data.get("Island_UR_x")).intValue();
			IURy = ((Long) data.get("Island_UR_y")).intValue();
			ILLx = ((Long) data.get("Island_LL_x")).intValue();
			ILLy = ((Long) data.get("Island_LL_y")).intValue();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

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
			
			odometer = Odometer.getOdometer();
			Thread odoThread = new Thread(odometer);
			odoThread.start();
			
			poller = Poller.getPoller(navigation);
			Thread pollerThread = new Thread(poller);
			pollerThread.start();
			
			display = new Display(lcd);
			Thread displayThread = new Thread(display);
			displayThread.start();

			lcd.clear();
			if (buttonChoice == Button.ID_LEFT) {
				// test the track and wheels
				navigation.turnTo(90);
				navigation.moveForward(120, false);
				navigation.floatWheels();
				System.exit(0);
			}

			else if (buttonChoice == Button.ID_RIGHT) {
				usLocalizer = new UltrasonicLocalizer(navigation);
				lightLocalizer = new LightLocalizer(navigation);
				ringSearch = new RingSearch(TGx, TGy, navigation);


				
	
				// TODO: take ring

				// usLocalizer.fallingEdge();
				// lightLocalizer.moveToOrigin(SC);
				
				//navigation.travelToYellowZone(TNG_LL_x, TNG_LL_y, TNG_UR_x, TNG_UR_y); 
				
				//int startingCorner = navigation.travelToRingSet(TG_x, TG_y); // travel to closest corner of the 2x2 square on which the ring set is centered
				
				//int[] ringFound = ringSearch.findRing( TG_x, TG_y , startingCorner); //start to search rings
				
				
				//ringSearch.faceRing(ringFound[1], TG_x, TG_y); 
				
				//ringSearch.grabRing(ringLevel, ringNumber); //to be implemented

				//beta demo algorithm
				usLocalizer.fallingEdge();
				lightLocalizer.moveToOrigin(SC);
				navigation.travelToYellowZone(TLLx, TLLy, TURx, TURy); 
				
				int startingCorner = navigation.travelToRingSet(TG_x, TG_y); // travel to nearest corner of the 2x2 square on which the ring set is centered
				//startingCorner is an int from 0 to 3. 0 is lower left, 1 is lower right, 2 is upper right, 3 is upper left
				
				
				// TODO: ring search and grab algorithm
				

				//beta demo algorithm
				navigation.travelTo(TLLx + 0.5, 1);
				navigation.travelTo(TLLx + 0.5, TLLy - 0.5);
				poller.disable();
				navigation.travelTo(TLLx + 0.5, TURy + 0.5);
				poller.enable();
				
				navigation.travelTo(TGx + 0.5, TURy + 0.5);
				navigation.travelTo(TGx + 0.5, TGy - 0.5);

				int ringLocation = ringSearch.findRing();
				ringSearch.grabRing(ringLocation);
				
			}

			buttonChoice = Button.waitForAnyPress();
		} while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
