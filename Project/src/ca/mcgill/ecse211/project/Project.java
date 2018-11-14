package ca.mcgill.ecse211.project;

import java.util.Map;
import ca.mcgill.ecse211.WiFiClient.WifiConnection;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.poller.Poller;
import ca.mcgill.ecse211.poller.PollerException;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

public class Project {
	// CUSTOM VARIABLES

	// T: tunnel
	private static int TLLx = 6;
	private static int TLLy = 2;
	private static int TURx = 7;
	private static int TURy = 4;

	// Ring tree
	private static int TGx = 5;
	private static int TGy = 6;

	// No prefix: starting zone
	private static int URx = 8;
	private static int URy = 3;
	private static int LLx = 3;
	private static int LLy = 3;

	// I: island
	private static int IURx = 8;
	private static int IURy = 8;
	private static int ILLx = 0;
	private static int ILLy = 5;

	// Starting corner
	private static int SC = 1;

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

	private static final double OFFSET = 0.4;

	private static final String SERVER_IP = "192.168.2.40";
	// Christiana's IP: 192.168.2.34
	private static final int TEAM_NUMBER = 7;

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws OdometerExceptions, PollerException {

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

//			lcd.clear();
//			lcd.drawString("ready", 0, 0);
			
			//buttonChoice = Button.waitForAnyPress();

			WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, true);

			try {
				Map data = conn.getData();
				System.out.println("Map:\n" + data);

				// Team specifics
				int redTeam = ((Long) data.get("GreenTeam")).intValue();
				System.out.println("Red Team: " + redTeam);

				// ringset location
				TGx = ((Long) data.get("TG_x")).intValue();
				System.out.println("X component of the Green ring tree: " + TGx);
				TGy = ((Long) data.get("TG_y")).intValue();

				// tunnel
				TLLx = ((Long) data.get("TNG_LL_x")).intValue();
				TLLy = ((Long) data.get("TNG_LL_y")).intValue();
				TURx = ((Long) data.get("TNG_UR_x")).intValue();
				TURy = ((Long) data.get("TNG_UR_y")).intValue();

				// zone
				URx = ((Long) data.get("Green_UR_x")).intValue();
				URy = ((Long) data.get("Green_UR_y")).intValue();
				LLx = ((Long) data.get("Green_LL_x")).intValue();
				LLy = ((Long) data.get("Green_LL_y")).intValue();

				// island
				IURx = ((Long) data.get("Island_UR_x")).intValue();
				IURy = ((Long) data.get("Island_UR_y")).intValue();
				ILLx = ((Long) data.get("Island_LL_x")).intValue();
				ILLy = ((Long) data.get("Island_LL_y")).intValue();

			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}

			lcd.clear();
			if (buttonChoice == Button.ID_LEFT) {
				// test the track and wheels
				Navigation.turnTo(90);
				Navigation.moveForward(120, false);
				Navigation.floatWheels();
				System.exit(0);
			}

			else if (buttonChoice == Button.ID_RIGHT) {
				usLocalizer = new UltrasonicLocalizer(navigation);
				lightLocalizer = new LightLocalizer(navigation);
				ringSearch = new RingSearch(TGx, TGy, navigation);

				// beta demo algorithm

				poller.disableCorrection();
				usLocalizer.fallingEdge();
				lightLocalizer.moveToOrigin(SC);
				poller.enableCorrection();

				// odometer.setX(7*30.48);
				// odometer.setY(30.48);
				// odometer.setTheta(0);

				// beta demo algorithm

				// navigation.travelToYellowZone(TLLx, TLLy, TURx, TURy);
				// navigation.travelToRingSet(TGx, TGy);

				// navigation.travelTo(7, 3);
				// navigation.travelTo(4, 3);
				// navigation.travelTo(4, 1);
				// navigation.travelTo(1, 1);

				ringSearch.enableTunnel(true);

				//if horizontal
				if (TURx - TLLx > 1) {
					Navigation.turnTo(0);
					Navigation.travelTo(7, TLLy + OFFSET);
					Navigation.travelTo(TURx + OFFSET, TLLy + OFFSET);
					poller.disableCorrection();
					Navigation.travelTo(TLLx - OFFSET, TLLy + OFFSET);
					poller.enableCorrection();

					Navigation.travelTo(TGx, TLLy + OFFSET);

					if (TGy <= TLLy) {
						Navigation.travelTo(TGx, TGy + 1);
						Navigation.turnTo(180);
					} else {
						Navigation.travelTo(TGx, TGy - 1);
						Navigation.turnTo(0);
					}

				} else {
					Navigation.travelTo(TLLx + OFFSET, 1);
					Navigation.travelTo(TLLx + OFFSET, TLLy - 0.5);

					poller.disableCorrection();
					Navigation.travelTo(TLLx + OFFSET, TURy + 0.5);
					poller.enableCorrection();

					if (TGx < TURx) {
						// left side of tunnel
						Navigation.travelTo(TLLx + OFFSET, TGy);
						Navigation.travelTo(TGx + 1, TGy);
						Navigation.turnTo(270);
					} else {
						// right side of tunnel
						Navigation.travelTo(TLLx + OFFSET, TGy);
						Navigation.travelTo(TGx - 1, TGy);
						Navigation.turnTo(90);
					}
				}

				poller.disableCorrection();
				RingSearch.grabRing(0);
			}

			buttonChoice = Button.waitForAnyPress();
		} while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
