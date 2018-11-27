package ca.mcgill.ecse211.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import ca.mcgill.ecse211.WiFiClient.WifiConnection;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.poller.Poller;
import ca.mcgill.ecse211.poller.PollerException;
import ca.mcgill.ecse211.poller.RingDetector;
import ca.mcgill.ecse211.poller.RingDetector.ColourType;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

/**
 * This class contains the main method which retrieves rings.
 * @author Sophie Deng
 * @author Edward Huang
 *
 */
public class Project {
    /**
     * The orientation of the tunnel.
     */
	public static enum Tunnel {
		HORIZONTALLEFT, VERTICALUP, HORIZONTALRIGHT, VERTICALDOWN
	};

	public static Tunnel tunnel = Tunnel.VERTICALUP;

	// CUSTOM VARIABLES
	// T: tunnel
	public static int TLLx = 3;
	public static int TLLy = 3;
	public static int TURx = 4;
	public static int TURy = 5;

	// Ring tree
	public static int TGx = 1;
	public static int TGy = 1;

	// Other Ring tree
	public static int TRx = 0;
	public static int TRy = 0;

	// No prefix: starting zone
	public static int URx = 8;
	public static int URy = 8;
	public static int LLx = 0;
	public static int LLy = 5;

	// I: island
	public static int IURx = 8;
	public static int IURy = 3;
	public static int ILLx = 0;
	public static int ILLy = 0;

	// Starting corner
	public static int SC = 2;
	private static final double TILE_SIZE = 30.48;
	private static final int FIELDX = 8;
	private static final int FIELDY = 8;
	private static final double FIELD_WIDTH = FIELDX * TILE_SIZE;
	private static final double FIELD_HEIGHT = FIELDY * TILE_SIZE;
	private static int STARTX, STARTY;

	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	private static Display display;
	private static Odometer odometer;
	private static Poller poller;
	private static UltrasonicLocalizer usLocalizer;
	private static LightLocalizer lightLocalizer;
	private static RingSearch ringSearch;
	private static Navigation navigation;

	// Constants
	private static final int TEAM_NUMBER = 7;
	private static final String SERVER_IP = "192.168.2.33";

	/**
	 * The main method. It makes the robot do the localizaion and moves it to the
	 * tunnel entrance. It crosses the tunnel and goes to the ring tree. It grabs
	 * the first ring and comes back with the same path.
	 * 
	 * @param args
	 * @throws OdometerExceptions
	 * @throws PollerException
	 */
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

			poller = Poller.getPoller();
			Thread pollerThread = new Thread(poller);
			pollerThread.start();

			display = new Display(lcd);
			Thread displayThread = new Thread(display);
			displayThread.start();

			lcd.clear();

			if (buttonChoice == Button.ID_LEFT) {
				// Test the track and wheels
				HashMap<ColourType, Coordinate> ringMap = new HashMap<ColourType, Coordinate>();
				ringSearch = new RingSearch(TGx, TGy);
				ringSearch.enableTunnel(false);
				RingSearch.findRing();
				// LinkedList<Coordinate> nextRing = findPath(-1, 1,
				// 0,0, true);
				// Navigation.travelByPath(nextRing);
				// Navigation.face(0, 1);
				// ringSearch.findRing(new Coordinate(0,0), ringMap);

			} else if (buttonChoice == Button.ID_RIGHT) {
				// Receive data over Wifi
				WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, true);
				try {
					Map data = conn.getData();
					System.console().writer().println("Map:\n" + data);

					// Team specifics
					int greenTeam = ((Long) data.get("GreenTeam")).intValue();
					int redTeam = ((Long) data.get("RedTeam")).intValue();
					// Island zone
					ILLx = ((Long) data.get("Island_LL_x")).intValue();
					ILLy = ((Long) data.get("Island_LL_y")).intValue();
					IURx = ((Long) data.get("Island_UR_x")).intValue();
					IURy = ((Long) data.get("Island_UR_y")).intValue();

					if (greenTeam == TEAM_NUMBER) {
						// Target ring tree location
						SC = ((Long) data.get("GreenCorner")).intValue();
						TGx = ((Long) data.get("TG_x")).intValue();
						TGy = ((Long) data.get("TG_y")).intValue();
						// Other team ring
						TRx = ((Long) data.get("TR_x")).intValue();
						TRy = ((Long) data.get("TR_y")).intValue();
						// Tunnel location
						TLLx = ((Long) data.get("TNG_LL_x")).intValue();
						TLLy = ((Long) data.get("TNG_LL_y")).intValue();
						TURx = ((Long) data.get("TNG_UR_x")).intValue();
						TURy = ((Long) data.get("TNG_UR_y")).intValue();
						// Starting zone
						LLx = ((Long) data.get("Green_LL_x")).intValue();
						LLy = ((Long) data.get("Green_LL_y")).intValue();
						URx = ((Long) data.get("Green_UR_x")).intValue();
						URy = ((Long) data.get("Green_UR_y")).intValue();

					} else if (redTeam == TEAM_NUMBER) {
						// Target ring tree location
						SC = ((Long) data.get("RedCorner")).intValue();
						TRx = ((Long) data.get("TG_x")).intValue();
						TRy = ((Long) data.get("TG_y")).intValue();
						// Other team ring
						TGx = ((Long) data.get("TR_x")).intValue();
						TGy = ((Long) data.get("TR_y")).intValue();
						// Tunnel location
						TLLx = ((Long) data.get("TNR_LL_x")).intValue();
						TLLy = ((Long) data.get("TNR_LL_y")).intValue();
						TURx = ((Long) data.get("TNR_UR_x")).intValue();
						TURy = ((Long) data.get("TNR_UR_y")).intValue();
						// Starting zone
						LLx = ((Long) data.get("Red_LL_x")).intValue();
						LLy = ((Long) data.get("Red_LL_y")).intValue();
						URx = ((Long) data.get("Red_UR_x")).intValue();
						URy = ((Long) data.get("Red_UR_y")).intValue();
					} else {
						System.err.println("Error: team not received");
						Sound.buzz();
						System.exit(0);
					}

				} catch (Exception e) {
					System.err.println("Error: " + e.getMessage());
				}

				// Check for the type of tunnel it must traverse
				if (TURx - TLLx > 1 || ILLx >= URx || LLx >= IURx) {
					if (SC == 2 || SC == 1) {
						tunnel = Tunnel.HORIZONTALLEFT;
					} else {
						tunnel = Tunnel.HORIZONTALRIGHT;
					}

				} else {
					if (SC == 2 || SC == 3) {
						tunnel = Tunnel.VERTICALDOWN;
					} else {
						tunnel = Tunnel.VERTICALUP;
					}
				}

				// localization
				usLocalizer = new UltrasonicLocalizer();
				lightLocalizer = new LightLocalizer();
				ringSearch = new RingSearch(TGx, TGy);
				Stack<Coordinate> waypoints = new Stack<Coordinate>();
				poller.enableCorrection(false);
				usLocalizer.fallingEdge();
				lightLocalizer.moveToOrigin();
				
				Sound.twoBeeps();
				Sound.beep();

				// set starting corners
				switch (SC) {
				case 0:
					odometer.setXYT(TILE_SIZE, TILE_SIZE, 0);
					STARTX = 1;
					STARTY = 1;
					break;
				case 1:
					odometer.setXYT(FIELD_WIDTH - TILE_SIZE, TILE_SIZE, 270);
					STARTX = FIELDX - 1;
					STARTY = 1;
					break;
				case 2:
					odometer.setXYT(FIELD_WIDTH - TILE_SIZE, FIELD_HEIGHT - TILE_SIZE, 180);
					STARTX = FIELDX - 1;
					STARTY = FIELDY - 1;
					break;
				case 3:
					odometer.setXYT(TILE_SIZE, FIELD_HEIGHT - TILE_SIZE, 90);
					STARTX = 1;
					STARTY = FIELDY - 1;
					break;
				}
				
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				poller.enableCorrection(true);

				// determine which points of the ring set locations can be accessed
				ArrayList<Coordinate> RingCoordinates = new ArrayList<Coordinate>();
				// visit left, up, right, down
				int[] changeX = { -1, 0, 1, 0 };
				int[] changeY = { 0, 1, 0, -1 };
				for (int i = 0; i < 4; i++) {
					Coordinate change = new Coordinate(TGx + changeX[i], TGy + changeY[i]);
					if (isInBoundaries(change, true)) {
						RingCoordinates.add(change);
					}
				}
				//if is empty, do nothing
				if(RingCoordinates.isEmpty()) {
					Sound.beepSequence();
					System.exit(0);
				}

				ringSearch.enableTunnel(true);

				// nagivate to tunnel
				LinkedList<Coordinate> pathToTunnel = new LinkedList<Coordinate>();
				if (tunnel == Tunnel.HORIZONTALRIGHT) {
					pathToTunnel = findPath(STARTX, STARTY, TLLx - 0.5, TLLy + 0.5, false);
					Navigation.travelByPath(waypoints, pathToTunnel);
				} else if (tunnel == Tunnel.HORIZONTALLEFT) {
					pathToTunnel = findPath(STARTX, STARTY, TURx + 0.5, TURy - 0.5, false);
					Navigation.travelByPath(waypoints, pathToTunnel);
				} else if (tunnel == Tunnel.VERTICALDOWN) {
					pathToTunnel = findPath(STARTX, STARTY, TURx - 0.5, TURy + 0.5, false);
					Navigation.travelByPath(waypoints, pathToTunnel);
				} else {
					// vertical tunnel
					pathToTunnel = findPath(STARTX, STARTY, TLLx + 0.5, TLLy - 0.5, false);
					Navigation.travelByPath(waypoints, pathToTunnel);
				}

				// traverse tunnel
				Navigation.traverseTunnel(waypoints, TLLx, TLLy, TURx, TURy, tunnel);

				// go to ring
				LinkedList<Coordinate> pathToRing = findPath(waypoints.peek().x, waypoints.peek().y,
						RingCoordinates.get(0).x, RingCoordinates.get(0).y, true);
				Navigation.travelByPath(waypoints, pathToRing);
				Sound.twoBeeps();
				Sound.beep();
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				
				
				// grab first ring
				Navigation.face(TGx, TGy);
				Navigation.stop();
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				RingSearch.findRing();
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				
				// grab second ring if no ring on the first face
				if(!RingDetector.foundRing()) {
					LinkedList<Coordinate> pathToSecondRing = findPath(waypoints.peek().x, waypoints.peek().y,
							RingCoordinates.get(1).x, RingCoordinates.get(1).y, true);
					Navigation.travelByPath(waypoints, pathToSecondRing);
					Navigation.face(TGx, TGy);
					Navigation.stop();
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
					RingSearch.findRing();
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
				}
				
				// go back to starting corner
				while (!waypoints.isEmpty()) {
					Coordinate point = waypoints.pop();
					Navigation.travelTo(point.x, point.y);
				}
				Navigation.travelTo(STARTX, STARTY);

				// drop ring
				switch (SC) {
				case 0:
					Navigation.turnTo(180 + 45);
					break;
				case 1:
					Navigation.turnTo(90 + 45);
					break;
				case 2:
					Navigation.turnTo(45);
					break;
				case 3:
					Navigation.turnTo(270 + 45);
					break;
				}
				RingSearch.dropRing();
				Navigation.dropRing();
				Sound.beepSequenceUp();
			}
			buttonChoice = Button.waitForAnyPress();
		} while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}

	/**
	 * This method finds the path to a location, it will always move in x first,
	 * then in y. It checks if the coordinate is within the boundaries.
	 * 
	 * @param startx
	 *            The starting x position
	 * @param starty
	 *            The starting y position
	 * @param endx
	 *            The ending x position
	 * @param endy
	 *            The ending y position
	 * @param island
	 *            True if the robot is navigating on the island
	 * @return the path the robot will take
	 */
	private static LinkedList<Coordinate> findPath(double startx, double starty, double endx, double endy,
			boolean island) {
		LinkedList<Coordinate> path = new LinkedList<Coordinate>();
		double currentx = startx;
		double currenty = starty;
		// Move as long as not at destination
		while (currentx != endx || currenty != endy) {
			double lastx = currentx;
			double lasty = currenty;
			while (endx < currentx) {
				// move left
				currentx -= 0.5;
				if (!isInBoundaries(new Coordinate(currentx, currenty), island)) {
					currentx += 0.5;
					break;
				}
			}
			while (endx > currentx) {
				// move right
				currentx += 0.5;
				if (!isInBoundaries(new Coordinate(currentx, currenty), island)) {
					currentx -= 0.5;
					break;
				}
			}
			// add to path if moved in x
			if (!(lastx == currentx && lasty == currenty)) {
				path.add(new Coordinate(currentx, currenty));
			}
			// break if at destination
			if (currentx == endx && currenty == endy)
				break;
			while (endy < currenty) {
				// move down
				currenty -= 0.5;
				if (!isInBoundaries(new Coordinate(currentx, currenty), island)) {
					currenty += 0.5;
					break;
				}
			}
			while (endy > currenty) {
				// move up
				currenty += 0.5;
				if (!isInBoundaries(new Coordinate(currentx, currenty), island)) {
					currenty -= 0.5;
					break;
				}
			}
			// add to path if moved in y
			if (!(lastx == currentx && lasty == currenty)) {
				path.add(new Coordinate(currentx, currenty));
			}

			// if there is an obstacle in the way
			if (lastx == currentx && lasty == currenty) {
				// same x, y not there
				if (currenty < endy) {
					// go right and up
					if (isInBoundaries(new Coordinate(currentx + 1, currenty), island)
							&& isInBoundaries(new Coordinate(currentx + 1, currenty + 1), island)
							&& isInBoundaries(new Coordinate(currentx + 1, currenty + 2), island)) {
						currentx += 1;
						path.add(new Coordinate(currentx, currenty));
						currenty += 1;
						path.add(new Coordinate(currentx, currenty));
					} else if (isInBoundaries(new Coordinate(currentx - 1, currenty), island)
							&& isInBoundaries(new Coordinate(currentx - 1, currenty + 1), island)
							&& isInBoundaries(new Coordinate(currentx - 1, currenty + 2), island)) {
						// go left and up
						currentx -= 1;
						path.add(new Coordinate(currentx, currenty));
						currenty += 1;
						path.add(new Coordinate(currentx, currenty));
					} else {
						System.err.println("Error: Path not found");
						break;
					}
				} else if (currenty > endy) {
					// go right and down
					if (isInBoundaries(new Coordinate(currentx + 1, currenty), island)
							&& isInBoundaries(new Coordinate(currentx + 1, currenty - 1), island)
							&& isInBoundaries(new Coordinate(currentx + 1, currenty - 2), island)) {
						currentx += 1;
						path.add(new Coordinate(currentx, currenty));
						currenty -= 1;
						path.add(new Coordinate(currentx, currenty));
					} else if (isInBoundaries(new Coordinate(currentx - 1, currenty), island)
							&& isInBoundaries(new Coordinate(currentx - 1, currenty - 1), island)
							&& isInBoundaries(new Coordinate(currentx - 1, currenty - 2), island)) {
						// go left and down
						currentx -= 1;
						path.add(new Coordinate(currentx, currenty));
						currenty -= 1;
						path.add(new Coordinate(currentx, currenty));
					} else {
						System.err.println("Error: Path not found");
						break;
					}
				}
				// same y, x not there
				else if (currentx < endx) {
					// go up and right
					if (isInBoundaries(new Coordinate(currentx, currenty + 1), island)
							&& isInBoundaries(new Coordinate(currentx + 1, currenty + 1), island)
							&& isInBoundaries(new Coordinate(currentx + 2, currenty + 1), island)) {
						currenty += 1;
						path.add(new Coordinate(currentx, currenty));
						currentx += 1;
						path.add(new Coordinate(currentx, currenty));
					}
					// go down and right
					else if (isInBoundaries(new Coordinate(currentx, currenty - 1), island)
							&& isInBoundaries(new Coordinate(currentx + 1, currenty - 1), island)
							&& isInBoundaries(new Coordinate(currentx + 2, currenty - 1), island)) {
						currenty -= 1;
						path.add(new Coordinate(currentx, currenty));
						currentx += 1;
						path.add(new Coordinate(currentx, currenty));
					} else {
						System.err.println("Error: Path not found");
						break;
					}
				} else if (currentx > endx) {
					// go up and left
					if (isInBoundaries(new Coordinate(currentx, currenty + 1), island)
							&& isInBoundaries(new Coordinate(currentx - 1, currenty + 1), island)
							&& isInBoundaries(new Coordinate(currentx - 2, currenty + 1), island)) {
						currenty += 1;
						path.add(new Coordinate(currentx, currenty));
						currentx -= 1;
						path.add(new Coordinate(currentx, currenty));
					}
					// go down and left
					else if (isInBoundaries(new Coordinate(currentx, currenty - 1), island)
							&& isInBoundaries(new Coordinate(currentx - 1, currenty - 1), island)
							&& isInBoundaries(new Coordinate(currentx - 2, currenty - 2), island)) {
						currenty -= 1;
						path.add(new Coordinate(currentx, currenty));
						currentx -= 1;
						path.add(new Coordinate(currentx, currenty));
					} else {
						System.err.println("Error: Path not found");
						break;
					}
				}
			}
		}
		return path;
	}

	/**
	 * Checks if the coordinate is accessible. If the robot is on the island, it
	 * checks for the boundaries of the island.
	 * 
	 * @param coordinate
	 *            The coordinate to check if it is within the boundaries
	 * @param island
	 *            If the robot is on the island or not
	 * @return true if the robot can go to that point
	 */
	private static boolean isInBoundaries(Coordinate coord, boolean island) {
		// robot cannot go on the sides
		if (coord.x >= FIELDX || coord.x <= 0)
			return false;
		if (coord.y >= FIELDY || coord.y <= 0)
			return false;
		// robot cannot go on a tunnel
		if ((coord.x <= TURx && coord.x >= TLLx) && (coord.y <= TURy && coord.y >= TLLy))
			return false;

		// robot cannot be on ring sets
		if (coord.x >= TGx - 0.5 && coord.x <= TGx + 0.5 && coord.y >= TGy - 0.5 && coord.y <= TGy + 0.5)
			return false;
		if (coord.x >= TRx - 0.5 && coord.x <= TRx + 0.5 && coord.y >= TRy - 0.5 && coord.y <= TRy + 0.5)
			return false;

		if (island) {
			if (coord.x > IURx || coord.x < ILLx)
				return false;
			if (coord.y > IURy || coord.y < ILLy)
				return false;
		}
		return true;
	}
}
