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
import ca.mcgill.ecse211.poller.RingDetector.ColourType;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

public class Project {
	public static enum Tunnel {
		HORIZONTAL, VERTICAL
	};

	private static Tunnel tunnel = Tunnel.VERTICAL;

	// CUSTOM VARIABLES
	// T: tunnel
	private static int TLLx = 5;
	private static int TLLy = 2;
	private static int TURx = 6;
	private static int TURy = 4;

	// Ring tree
	private static int TGx = 7;
	private static int TGy = 6;

	// Other Ring tree
	private static int TRx = 2;
	private static int TRy = 2;

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
	private static final double TILE_SIZE = 30.48;
	private static final int FIELDX = 8;
	private static final int FIELDY = 8;
	private static final double FIELD_WIDTH = FIELDX * TILE_SIZE;
	private static final double FIELD_HEIGHT = FIELDY * TILE_SIZE;
	private static int STARTX, STARTY;

	public static final TextLCD lcd = LocalEV3.get().getTextLCD();
	private static Display display;
	private static Odometer odometer;
	private static Poller poller;
	private static UltrasonicLocalizer usLocalizer;
	private static LightLocalizer lightLocalizer;
	private static RingSearch ringSearch;

	// Constants
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

				Navigation.turnTo(10);
				odometer.setTheta(0);
				Navigation.moveForward(40, false);
				// Navigation.travelTo(0,4);
				Navigation.turnTo(0);

				System.exit(0);

			} else if (buttonChoice == Button.ID_RIGHT) {
				// Receive data over Wifi
				// WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, true);
				// try {
				// Map data = conn.getData();
				// System.console().writer().println("Map:\n" + data);
				//
				// // Team specifics
				// int greenTeam = ((Long) data.get("GreenTeam")).intValue();
				// int redTeam = ((Long) data.get("RedTeam")).intValue();
				// // Island zone
				// ILLx = ((Long) data.get("Island_LL_x")).intValue();
				// ILLy = ((Long) data.get("Island_LL_y")).intValue();
				// IURx = ((Long) data.get("Island_UR_x")).intValue();
				// IURy = ((Long) data.get("Island_UR_y")).intValue();
				// if (greenTeam == TEAM_NUMBER) {
				// // Target ring tree location
				// TGx = ((Long) data.get("TG_x")).intValue();
				// TGy = ((Long) data.get("TG_y")).intValue();
				// // Other team ring
				// TRx = ((Long) data.get("TR_x")).intValue();
				// TRy = ((Long) data.get("TR_y")).intValue();
				// // Tunnel location
				// TLLx = ((Long) data.get("TNG_LL_x")).intValue();
				// TLLy = ((Long) data.get("TNG_LL_y")).intValue();
				// TURx = ((Long) data.get("TNG_UR_x")).intValue();
				// TURy = ((Long) data.get("TNG_UR_y")).intValue();
				// // Starting zone
				// LLx = ((Long) data.get("Green_LL_x")).intValue();
				// LLy = ((Long) data.get("Green_LL_y")).intValue();
				// URx = ((Long) data.get("Green_UR_x")).intValue();
				// URy = ((Long) data.get("Green_UR_y")).intValue();
				// } else if (redTeam == TEAM_NUMBER) {
				// // Target ring tree location
				// TRx = ((Long) data.get("TG_x")).intValue();
				// TRy = ((Long) data.get("TG_y")).intValue();
				// // Other team ring
				// TGx = ((Long) data.get("TR_x")).intValue();
				// TGy = ((Long) data.get("TR_y")).intValue();
				// // Tunnel location
				// TLLx = ((Long) data.get("TNR_LL_x")).intValue();
				// TLLy = ((Long) data.get("TNR_LL_y")).intValue();
				// TURx = ((Long) data.get("TNR_UR_x")).intValue();
				// TURy = ((Long) data.get("TNR_UR_y")).intValue();
				// // Starting zone
				// LLx = ((Long) data.get("Red_LL_x")).intValue();
				// LLy = ((Long) data.get("Red_LL_y")).intValue();
				// URx = ((Long) data.get("Red_UR_x")).intValue();
				// URy = ((Long) data.get("Red_UR_y")).intValue();
				// } else {
				// System.err.println("Error: team not received");
				// System.exit(0);
				// }
				//
				// } catch (Exception e) {
				// System.err.println("Error: " + e.getMessage());
				// }
				if (TURx - TLLx > 1) {
					tunnel = Tunnel.HORIZONTAL;
				}
				// checkfor valid inputs
				if (tunnel == Tunnel.VERTICAL) {
					if (TURx <= ILLx || TURx > IURx || TURy < ILLy || TURy >= IURy) {
						System.err.println("Error: Tunnel not connecting to island");
						System.exit(0);
					}
					if (TLLx >= URx || TLLx < LLx || TLLy > URy || TLLy <= LLy) {
						System.err.println("Error: Tunnel not connecting to starting zone");
						System.exit(0);
					}
				} else {
					if (TURx < ILLx || TURx >= IURx || TURy > IURy || TURy <= ILLy) {
						System.err.println("Error: Tunnel not connecting to island");
						System.exit(0);
					}
					if (TLLx > URx || TLLx <= LLx || TLLy >= URy || TLLy < LLy) {
						System.err.println("Error: Tunnel not connecting to starting zone");
						System.exit(0);
					}
				}

				// usLocalizer = new UltrasonicLocalizer();
				// lightLocalizer = new LightLocalizer();
				ringSearch = new RingSearch(TGx, TGy);
				Stack<Coordinate> waypoints = new Stack<Coordinate>();

				// beta demo algorithm
				poller.enableCorrection(false);
				// usLocalizer.fallingEdge();
				// lightLocalizer.moveToOrigin();
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
				poller.enableCorrection(true);

				Sound.twoBeeps();
				Sound.beep();

				// determine which points of the ring set locations can be accessed
				ArrayList<Coordinate> RingCoordinates = new ArrayList<Coordinate>();
				// visit left, down, right, up
				int[] changeX = { -1, 0, 1, 0 };
				int[] changeY = { 0, -1, 0, 1 };
				for (int i = 0; i < 4; i++) {
					Coordinate change = new Coordinate(TGx + changeX[i], TGy + changeY[i]);
					if (isInBoundaries(change, true)) {
						RingCoordinates.add(change);
					}
				}

				ringSearch.enableTunnel(true);

				LinkedList<Coordinate> pathToTunnel = new LinkedList<Coordinate>();
				if (tunnel == Tunnel.HORIZONTAL) {
					pathToTunnel = findPath(STARTX, STARTY, TLLx - 0.5, TLLy + 0.5, false);
					Navigation.travelByPath(waypoints, pathToTunnel);
				} else {
					// vertical tunnel
					pathToTunnel = findPath(STARTX, STARTY, TLLx + 0.5, TLLy - 0.5, false);
					Navigation.travelByPath(waypoints, pathToTunnel);
				}

				Navigation.traverseTunnel(waypoints, TLLx, TLLy, TURx, TURy, tunnel);

				LinkedList<Coordinate> pathToRing = findPath(waypoints.peek().x, waypoints.peek().y,
						RingCoordinates.get(0).x, RingCoordinates.get(0).y, true);

				Navigation.travelByPath(pathToRing);

				Navigation.face(TGx, TGy);
				
				HashMap<ColourType, Coordinate> ringMap = new HashMap<ColourType, Coordinate>();
						
				for(int i = 0; i < RingCoordinates.size(); i++) {
					double currentX = odometer.getXYT()[0] / TILE_SIZE;
					double currentY = odometer.getXYT()[1] / TILE_SIZE;
					LinkedList<Coordinate> nextRing = findPath(currentX, currentY,
							RingCoordinates.get(i).x, RingCoordinates.get(i).y, true);
					Navigation.travelByPath(nextRing);
					RingSearch.findRing(RingCoordinates.get(i), ringMap);
				}
				double currentX = odometer.getXYT()[0] / TILE_SIZE;
				double currentY = odometer.getXYT()[1] / TILE_SIZE;
				LinkedList<Coordinate> tunnelBack = findPath(currentX, currentY,
						waypoints.peek().x, waypoints.peek().y, true);
				Navigation.travelByPath(tunnelBack);

				while (!waypoints.isEmpty()) {
					Coordinate point = waypoints.pop();
					Navigation.travelTo(point.x, point.y);
				}
				Navigation.travelTo(STARTX, STARTY);

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
	 * finds the path to a location, assumes path exists
	 * 
	 * @param startx
	 * @param starty
	 * @param endx
	 * @param endy
	 * @return
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
	 * Checks if the coordinate is accessible
	 * 
	 * @param coordinate
	 * @return true if the robot can go to that point
	 */
	private static boolean isInBoundaries(Coordinate coord, boolean island) {
		// robot cannot go on the sides
		if (coord.x >= FIELDX || coord.x <= 0)
			return false;
		if (coord.y >= FIELDY || coord.y <= 0)
			return false;
		// robot cannot go on a tunnel
		if (coord.x == TURx && coord.y == TURy)
			return false;
		if (coord.x == TLLx && coord.y == TLLy)
			return false;
		if (tunnel == Tunnel.HORIZONTAL) {
			if (coord.x == TURx && coord.y == TURy - 1)
				return false;
			if (coord.x == TLLx && coord.y == TLLy + 1)
				return false;
		} else {
			if (coord.x == TURx - 1 && coord.y == TURy)
				return false;
			if (coord.x == TLLx + 1 && coord.y == TLLy)
				return false;
		}
		// robot cannot be on ring sets
		if (coord.x == TGx && coord.y == TGy)
			return false;
		if (coord.x >= TRx - 0.5 && coord.x <= TRx + 0.5 && coord.y >= TRy - 0.5 && coord.y <= TRy + 0.5)
			return false;

		if (island) {
			if (coord.x > IURx || coord.x < ILLx)
				return false;
			if (coord.y > IURx || coord.y < ILLy)
				return false;
		}
		return true;
	}
}
