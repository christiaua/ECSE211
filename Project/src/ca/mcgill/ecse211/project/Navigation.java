package ca.mcgill.ecse211.project;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import ca.mcgill.ecse211.odometer.*;

/**
 * This class controls the robot motors
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 * @author Sophie Deng
 */
public class Navigation {

  // Motor Objects, and Robot related parameters
  private static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  private static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

  private static final double WHEEL_RAD = 2.075;
  private static final double TRACK = 13.5;
  private static final double TILE_SIZE = 30.48;

  public static final int FORWARD_SPEED = 200;
  public static final int ROTATE_SPEED = 150;

  private Odometer odo = null;
  private static double[] currentDest = {0, 0};
  private static boolean turning = false;

  /**
   * Constructor
   * 
   * @throws OdometerExceptions
   */
  public Navigation() throws OdometerExceptions {
    odo = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
  }

  /**
   * Floates the wheels
   */
  public void floatWheels() {
    leftMotor.forward();
    leftMotor.flt();
    rightMotor.forward();
    rightMotor.flt();
  }

  /**
   * Moves the robot forwards a distance. A negative distance implies going backwards
   * 
   * @param distance
   * @param immediateReturn
   */
  public void moveForward(double distance, boolean immediateReturn) {
    stop();
    setSpeed(FORWARD_SPEED);
    if (distance < 0) {
      leftMotor.rotate(-convertDistance(WHEEL_RAD, Math.abs(distance)), true);
      rightMotor.rotate(-convertDistance(WHEEL_RAD, Math.abs(distance)), immediateReturn);
      return;
    }
    leftMotor.rotate(convertDistance(WHEEL_RAD, distance), true);
    rightMotor.rotate(convertDistance(WHEEL_RAD, distance), immediateReturn);
  }

  /**
   * Rotates the robot by certain angle. Positive angle is clockwise
   * 
   * @param angle
   * @param immediateReturn
   */
  public void rotate(double angle, boolean immediateReturn) {
    setSpeed(ROTATE_SPEED);
    if (angle < 0) {
      leftMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, Math.abs(angle)), true);
      rightMotor.rotate(convertAngle(WHEEL_RAD, TRACK, Math.abs(angle)), immediateReturn);
      return;
    }
    leftMotor.rotate(convertAngle(WHEEL_RAD, TRACK, Math.abs(angle)), true);
    rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, Math.abs(angle)), immediateReturn);
  }

  /**
   * This method rotates the robot. It does not tell it when to stop
   * 
   * @param left motor speed and right motor speed
   */
  public void rotateWheels(int leftSpeed, int rightSpeed) {
    stop();
    leftMotor.setSpeed(leftSpeed);
    rightMotor.setSpeed(rightSpeed);

    // rotate left motor
    if (leftSpeed < 0) {
      leftMotor.backward();
    } else {
      leftMotor.forward();
    }

    // rotate right motor
    if (rightSpeed < 0) {
      rightMotor.backward();
    } else {
      rightMotor.forward();
    }
  }

  public void travelToYellowZone(int tunnelLL_x, int tunnelLL_y, int tunnelUR_x, int tunnelUR_y) {

    double[] tunnelEntranceXY = new double[2];
    double[] tunnelExitXY = new double[2];

    tunnelEntranceExit(tunnelLL_x, tunnelLL_y, tunnelUR_x, tunnelUR_y, tunnelEntranceXY,
        tunnelExitXY);

    // always move in x axis first (arbitrary)
    travelTo(tunnelEntranceXY[0], odo.getXYT()[1]);
    // move in y axis
    travelTo(tunnelEntranceXY[0], tunnelEntranceXY[1]);
    // move to tunnel exit
    travelTo(tunnelExitXY[0], tunnelExitXY[1]);

  }

  /**
   * This method finds the coordinates of the entrance and the exit of a tunnel relative to the
   * robot's position and stores them in a arrays given as parameters.
   * 
   * 
   * @param tunnelLL_x
   * @param tunnelLL_y
   * @param tunnelUR_x
   * @param tunnelUR_y
   * @param tunnelEntranceXY Array to store the coordinates of the entrance
   * @param tunnelExitXY Array to store the coordinates of the exit
   */
  public void tunnelEntranceExit(int tunnelLL_x, int tunnelLL_y, int tunnelUR_x, int tunnelUR_y,
      double[] tunnelEntranceXY, double[] tunnelExitXY) {

    int tunnelOrientation; // orientation of the tunnel, ranges from 0 to 3, 0 is entrance left exit
                           // right, 1 is entrance down exit up, 2 is entrance right exit left and 3
                           // is entrance up exit down


    // define tunnel orientation
    if (tunnelUR_x - tunnelLL_x == 2) // tunnel is horizontal
    {
      if ((double) (tunnelLL_x + 1) * TILE_SIZE > odo.getXYT()[0]) // if middle of the tunnel is at
                                                                   // the right of the robot
        tunnelOrientation = 0; // entrance left , exit right
      else
        tunnelOrientation = 2;// entrance right, exit left
    } else // tunnel is vertical
    {
      if ((double) (tunnelLL_y + 1) * TILE_SIZE > odo.getXYT()[1]) // if middle of the tunnel is
                                                                   // higher than the robot
        tunnelOrientation = 1; // entrance down , exit up
      else
        tunnelOrientation = 3;// entrance up, exit down
    }

    switch (tunnelOrientation) {
      case 0: // entrance left, exit right
        tunnelEntranceXY[0] = (double) (tunnelLL_x - 0.5) * TILE_SIZE;
        tunnelEntranceXY[1] = (double) (tunnelLL_y + 0.5) * TILE_SIZE;
        tunnelExitXY[0] = (double) (tunnelUR_x + 0.5) * TILE_SIZE;
        tunnelExitXY[1] = (double) (tunnelUR_y - 0.5) * TILE_SIZE;
        break;


      case 1: // entrance down, exit up
        tunnelEntranceXY[0] = (double) (tunnelLL_x + 0.5) * TILE_SIZE;
        tunnelEntranceXY[1] = (double) (tunnelLL_y - 0.5) * TILE_SIZE;
        tunnelExitXY[0] = (double) (tunnelUR_x - 0.5) * TILE_SIZE;
        tunnelExitXY[1] = (double) (tunnelUR_y + 0.5) * TILE_SIZE;
        break;


      case 2: // entrance right, exit left
        tunnelEntranceXY[0] = (double) (tunnelUR_x + 0.5) * TILE_SIZE;
        tunnelEntranceXY[1] = (double) (tunnelUR_y - 0.5) * TILE_SIZE;
        tunnelExitXY[0] = (double) (tunnelLL_x - 0.5) * TILE_SIZE;
        tunnelExitXY[1] = (double) (tunnelLL_y + 0.5) * TILE_SIZE;
        break;


      case 3: // entrance up, exit down
        tunnelEntranceXY[0] = (double) (tunnelUR_x - 0.5) * TILE_SIZE;
        tunnelEntranceXY[1] = (double) (tunnelUR_y + 0.5) * TILE_SIZE;
        tunnelExitXY[0] = (double) (tunnelLL_x + 0.5) * TILE_SIZE;
        tunnelExitXY[1] = (double) (tunnelLL_y - 0.5) * TILE_SIZE;
        break;

    }


    return;

  }
  
  /*
   * /** This method makes the robot travel to the nearest corner of the 2x2 square on which the
   * ring set is centered, relative to the robot, and returns the corner number.
   * 
   * @param RS_x
   * 
   * @param RS_y
   * 
   * @return Corner number: 0 is lower left, 1 is lower right, 2 is upper right and 3 is upper left
   */
  /*
   * public int travelToRingSet(int rs_x, int rs_y) { int cornerNumber = nearestCorner(rs_x , rs_y);
   * double corner_x; double corner_y;
   * 
   * switch(cornerNumber) { case 0: corner_x = (rs_x - 1) * TILE_SIZE; corner_y = (rs_y - 1) *
   * TILE_SIZE; break; case 1: corner_x = (rs_x + 1) * TILE_SIZE; corner_y = (rs_y - 1) * TILE_SIZE;
   * break; case 2: corner_x = (rs_x + 1) * TILE_SIZE; corner_y = (rs_y + 1) * TILE_SIZE; break;
   * case 3: corner_x = (rs_x - 1) * TILE_SIZE; corner_y = (rs_y + 1) * TILE_SIZE; break; default:
   * return -1; //error }
   * 
   */

  /**
   * This method makes the robot travel to the nearest neighbor intersection to the ring set ,
   * relative to the robot, and returns the side number.
   * 
   * @param RS_x
   * @param RS_y
   * @return SideNumber: 0 is down, 1 is right, 2 is up and 3 is left
   */
  public int travelToRingSet(int rs_x, int rs_y) {
    int neighborNumber = nearestNeighbor(rs_x, rs_y);
    double neighbor_x;
    double neighbor_y;

    switch (neighborNumber) {
      // down
      case 0:
        neighbor_x = (rs_x) * TILE_SIZE;
        neighbor_y = (rs_y - 1) * TILE_SIZE;
        break;
      // right
      case 1:
        neighbor_x = (rs_x + 1) * TILE_SIZE;
        neighbor_y = (rs_y) * TILE_SIZE;
        break;
      // up
      case 2:
        neighbor_x = (rs_x) * TILE_SIZE;
        neighbor_y = (rs_y + 1) * TILE_SIZE;
        break;
      // left
      case 3:
        neighbor_x = (rs_x - 1) * TILE_SIZE;
        neighbor_y = (rs_y) * TILE_SIZE;
        break;
      default:
        return -1; // error
    }


    // always move in x axis first (arbitrary)
    travelTo(neighbor_x, odo.getXYT()[1]);
    // move in y axis
    travelTo(neighbor_x, neighbor_y);

    double angleToTurnTo = calculateAngle(rs_x, rs_y, odo);
    turnTo(angleToTurnTo);

    return neighborNumber;
  }

  /**
   * This method returns the number of the nearest neighbor intersection to the the ring set,
   * relative to the robot.
   * 
   * @param rs_x x position of the ring set
   * @param rs_y y position of the ring set
   * @return Side number: 0 is lower left, 1 is lower right, 2 is upper right and 3 is upper left
   */
  public int nearestNeighbor(double rs_x, double rs_y) {

    double odoX = odo.getXYT()[0];
    double odoY = odo.getXYT()[1];

    double[][] neighborIntersection =
        {{rs_x, rs_y - 1}, {rs_x + 1, rs_y}, {rs_x, rs_y + 1}, {rs_x - 1, rs_y}};
    double[] distance = new double[4];
    double dx, dy;
    int i;


    for (i = 0; i < 4; i++) {
      dx = neighborIntersection[i][0] - odoX;
      dy = neighborIntersection[i][1] - odoY;
      distance[i] = Math.sqrt(dx * dx + dy * dy);
    }


    return arrayMin(distance);
  }



  /**
   * This method returns the number of the nearest corner of the 2x2 square on which the ring set is
   * centered, relative to the robot.
   * 
   * @param rs_x x position of the ring set
   * @param rs_y y position of the ring set
   * @return Corner number: 0 is lower left, 1 is lower right, 2 is upper right and 3 is upper left
   */
  public int nearestCorner(double rs_x, double rs_y) {

    double odoX = odo.getXYT()[0];
    double odoY = odo.getXYT()[1];

    double[][] neighborCorner =
        {{rs_x - 1, rs_y - 1}, {rs_x - 1, rs_y + 1}, {rs_x + 1, rs_y + 1}, {rs_x - 1, rs_y - 1}};
    double[] distance = new double[4];
    double dx, dy;
    int i;


    for (i = 0; i < 4; i++) {
      dx = neighborCorner[i][0] - odoX;
      dy = neighborCorner[i][1] - odoY;
      distance[i] = Math.sqrt(dx * dx + dy * dy);
    }


    i = arrayMin(distance);

    double[] minDistXY = {neighborCorner[i][0], neighborCorner[i][1]};

    return arrayMin(minDistXY);
  }

  /**
   * This method returns the index of the smallest element of the array given as a parameter.
   * 
   * @param arr
   * @return
   */
  public int arrayMin(double[] arr) {
    double min = arr[0];
    int i;
    for (i = 1; i < arr.length; i++) {
      if (arr[i] < min)
        min = arr[i];
    }

    for (i = 0; i < arr.length; i++) {
      if (arr[i] == min)
        break;
    }

    return i;
  }



  /**
   * This method controls the robot to move towards x, y (tile position)
   * 
   * @param x
   * @param y
   */
  public void travelTo(double x, double y) {
    double angleToTurnTo;
    double currentDistance;
    double[] currentPosition = odo.getXYT();
    currentDest[0] = x;
    currentDest[1] = y;

    angleToTurnTo = calculateAngle(x, y, odo);
    turnTo(angleToTurnTo);
    currentPosition = odo.getXYT();
    currentDistance =
        calculateDistance(x * TILE_SIZE, y * TILE_SIZE, currentPosition[0], currentPosition[1]);
    moveForward(currentDistance, true);
    while(true) {
    	currentPosition = odo.getXYT();
	    if(Math.abs(currentPosition[2] - angleToTurnTo) > 5 || 
	    		Math.abs(currentPosition[2] - angleToTurnTo) > 355) {
		    stop();
		    turnTo(angleToTurnTo);    
		    currentDistance =
		        calculateDistance(x * TILE_SIZE, y * TILE_SIZE, currentPosition[0], currentPosition[1]);
		    moveForward(currentDistance, true);
	    }
	    if(!leftMotor.isMoving() && !rightMotor.isMoving()) {
	    	break;
	    }
    }
  }
  
  public void continueTraveling() {
	  stop();
	  try {
		  Thread.sleep(30);
	  }catch(Exception e) {
		  
	  }
	  travelTo(currentDest[0], currentDest[1]);
  }

  public void travelToStraight(double x, double y) {
    travelTo(x, odo.getXYT()[1]);
    travelTo(odo.getXYT()[0], y);
  }

  /**
   * This method turns the robot in place to the absolute angle theta.
   * 
   * @param theta
   */
  public void turnTo(double theta) {
    double currentTheta = odo.getXYT()[2];
    double dtheta = theta - currentTheta;
    double minAngle;

    // calculate minimum angle needed to turn to get to desired angle
    if (Math.abs(dtheta) > 180) {
      if (dtheta > 0)
        minAngle = dtheta - 360;
      else
        minAngle = dtheta + 360;
    } else
      minAngle = dtheta;

    // set motor speed
    setSpeed(ROTATE_SPEED);

    // turn by minimum angle
    leftMotor.rotate(convertAngle(WHEEL_RAD, TRACK, minAngle), true);
    rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, minAngle), false);
  }

  /**
   * This method checks if a wheel is turning
   * 
   * @return if a wheel is moving
   */
  public boolean isMoving() {
    return (leftMotor.isMoving() || leftMotor.isMoving());
  }

  /**
   * This method calculates the angle which the robot has to turn to reach tile position x, y using
   * the data from the odometer which is passed into the method.
   * 
   * @param x
   * @param y
   * @param odo
   * @return angle
   */
  public static double calculateAngle(double x, double y, Odometer odo) {
    double angle, dx, dy, h;
    double[] currentPosition;

    currentPosition = odo.getXYT();
    dx = x * TILE_SIZE - currentPosition[0];
    dy = y * TILE_SIZE - currentPosition[1];
    h = Math.sqrt(dx * dx + dy * dy);

    if (dx >= 0)
      angle = Math.acos(dy / h) / Math.PI * 180;
    else
      angle = -Math.acos(dy / h) / Math.PI * 180;

    if (angle < 0) {
      return angle + 360;
    } else {
      return angle;
    }
  }

  /**
   * This method allows the conversion of a distance to the total rotation of each wheel need to
   * cover that distance.
   * 
   * @param radius
   * @param distance
   * @return wheel rotation
   */
  private static int convertDistance(double radius, double distance) {
    return (int) ((180.0 * distance) / (Math.PI * radius));
  }

  /**
   * This method computes how much the wheels should turn to get to an angle
   * 
   * @param radius
   * @param distance
   * @return wheel rotation
   */
  public static int convertAngle(double radius, double width, double angle) {
    return convertDistance(radius, Math.PI * width * angle / 360.0);
  }

  /**
   * This method calculates the Euclidean distance given x, y initial and x, y final.
   * 
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @return euclidean distance
   */
  private static double calculateDistance(double x1, double y1, double x2, double y2) {
    double distance;
    distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    return distance;
  }


  /**
   * This method calculates the minimum difference between two angles a1 and a2.
   * 
   * @param a1
   * @param a2
   * @return minimum angle difference
   */
  public static double angleDiff(double a1, double a2) {
    double diff = a1 - a2;
    if (diff > 180) {
      return Math.abs(diff - 360);
    } else if (diff < -180) {
      return diff + 360;
    } else {
      return Math.abs(diff);
    }
  }

  /**
   * Stops both right and left motors
   * 
   */
  public void stop() {
    leftMotor.stop(true);
    rightMotor.stop(false);
  }

  /**
   * Stops specified motor
   * 
   * @param motor "left" for stopping left motor, "right" for stopping right motor
   */
  public void stop(String motor) {
    if (motor.equals("left"))
      leftMotor.stop(true);
    else if (motor.equals("right"))
      rightMotor.stop(false);
  }

  /**
   * Set speed for both motors
   * 
   */
  public void setSpeed(int speed) {
    leftMotor.setSpeed(speed);
    rightMotor.setSpeed(speed);
  }

  /**
   * Move robot backwards
   * 
   */
  public void backwards() {
    setSpeed(FORWARD_SPEED);
    leftMotor.backward();
    rightMotor.backward();
  }

  public boolean isNavigating() {
    if (leftMotor.isMoving() || rightMotor.isMoving())
      return true;
    else
      return false;
  }

  public int getTacho(String motor) {
    if (motor.equals("left"))
      return leftMotor.getTachoCount();
    else if (motor.equals("right"))
      return rightMotor.getTachoCount();
    else
      return -1;
  }

  public boolean isTurning() {
    if (leftMotor.getSpeed() == ROTATE_SPEED || rightMotor.getSpeed() == ROTATE_SPEED)
      return true;
    else
      return false;
  }
}
