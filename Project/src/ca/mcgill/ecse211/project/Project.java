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

	public static final TextLCD lcd = LocalEV3.get().getTextLCD();
	private static Display display;
	private static Odometer odometer;
	private static Poller poller;
	private static Navigation navigation;
	private static UltrasonicLocalizer usLocalizer;
	private static LightLocalizer lightLocalizer;
	private static RingSearch ringSearch;

	// Constants
	private static final double OFFSET = 0.4; // TODO: more descriptive?
	private static final int TEAM_NUMBER = 7;
	private static final String SERVER_IP = "192.168.2.2";

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws OdometerExceptions, PollerException {

		do {
			int buttonChoice;
			do {
				// Clear the display
				lcd.clear();

				// Float motor or Demo
				lcd.drawString("< Left | Right >", 0, 0);
				lcd.drawString("       |        ", 0, 1);
				lcd.drawString("  Test | Drive  ", 0, 2);
				lcd.drawString("motors | and do ", 0, 3);
				lcd.drawString("       | demo   ", 0, 4);

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

			// Receive data over Wifi
			// WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, true);
			//
			// try {
			// Map data = conn.getData();
			// System.console().writer().println("Map:\n" + data);
			//
			// // Team specifics
			// int team = ((Long) data.get("GreenTeam")).intValue();
			// System.console().writer().println("Team: " + team);
			//
			// // Target ring tree location
			// TGx = ((Long) data.get("TG_x")).intValue();
			// TGy = ((Long) data.get("TG_y")).intValue();
			// System.console().writer().println("Green Tree: " + TGx + ", " + TGy);
			//
			// // Tunnel location
			// TLLx = ((Long) data.get("TNG_LL_x")).intValue();
			// TLLy = ((Long) data.get("TNG_LL_y")).intValue();
			// System.console().writer().println("Tunnel LL: " + TLLx + ", " + TLLy);
			//
			// TURx = ((Long) data.get("TNG_UR_x")).intValue();
			// TURy = ((Long) data.get("TNG_UR_y")).intValue();
			// System.console().writer().println("Tunnel UR: " + TURx + ", " + TURy);
			//
			// // Starting zone
			// LLx = ((Long) data.get("Green_LL_x")).intValue();
			// LLy = ((Long) data.get("Green_LL_y")).intValue();
			// System.console().writer().println("Starting LL: " + LLx + ", " + LLy);
			//
			// URx = ((Long) data.get("Green_UR_x")).intValue();
			// URy = ((Long) data.get("Green_UR_y")).intValue();
			// System.console().writer().println("Starting UR: " + URx + ", " + URy);
			//
			// // Island zone
			// ILLx = ((Long) data.get("Island_LL_x")).intValue();
			// ILLy = ((Long) data.get("Island_LL_y")).intValue();
			// System.console().writer().println("Island LL: " + LLx + ", " + LLy);
			//
			// IURx = ((Long) data.get("Island_UR_x")).intValue();
			// IURy = ((Long) data.get("Island_UR_y")).intValue();
			// System.console().writer().println("Island UR: " + IURx + ", " + IURy);
			//
			// } catch (Exception e) {
			// System.err.println("Error: " + e.getMessage());
			// }

			lcd.clear();

			if (buttonChoice == Button.ID_LEFT) {
				// Test the track and wheels
				Navigation.turnTo(90);
				Navigation.moveForward(120, false);
				Navigation.floatWheels();
				System.exit(0);
			} else if (buttonChoice == Button.ID_RIGHT) {
				usLocalizer = new UltrasonicLocalizer(navigation);
				lightLocalizer = new LightLocalizer(navigation);
				ringSearch = new RingSearch(TGx, TGy, navigation);

				// beta demo algorithm
				poller.enableCorrection(false);
				usLocalizer.fallingEdge();
				lightLocalizer.moveToOrigin(SC);
				poller.enableCorrection(true);
				ringSearch.enableTunnel(true);

				// If tunnel horizontal
				if (TURx - TLLx > 1) {
					Navigation.turnTo(0);
					Navigation.travelTo(7, TLLy + OFFSET);
					Navigation.travelTo(TURx + OFFSET, TLLy + OFFSET);
					poller.enableCorrection(false);
					Navigation.travelTo(TLLx - OFFSET, TLLy + OFFSET);
					poller.enableCorrection(true);

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

					poller.enableCorrection(false);
					Navigation.travelTo(TLLx + OFFSET, TURy + 0.5);
					poller.enableCorrection(true);

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
				poller.enableCorrection(false);
				RingSearch.grabRing(0);
			}
			buttonChoice = Button.waitForAnyPress();
		} while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
