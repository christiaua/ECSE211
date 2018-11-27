package ca.mcgill.ecse211.project;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import ca.mcgill.ecse211.odometer.*;
import ca.mcgill.ecse211.project.Project.Tunnel;

/**
 * This class controls the robot wheel motors. It contains all the methods to make the robot move.
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
  private static final double TRACK = 14.225;
  private static final double TILE_SIZE = 30.48;

  private static final int DROP_SPEED = 1000;
  private static final int FORWARD_SPEED = 200;
  private static final int ROTATE_SPEED = 140;

  private static Odometer odo = null;
  private static double[] currentDest = {0, 0};

  /**
   * Location of the wheel relative to the center of the robot.
   */
  public enum Side {
    LEFT, RIGHT
  }

  /**
   * Constructor
   * 
   * @throws OdometerExceptions
   */
  public Navigation() throws OdometerExceptions {
    odo = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
  }

  /**
   * Floats the wheels
   */
  public static void floatWheels() {
    leftMotor.forward();
    leftMotor.flt();
    rightMotor.forward();
    rightMotor.flt();
  }

  /**
   * Moves the robot forwards a distance.
   * 
   * @param distance The distance to move. A negative distance implies going backwards.
   * @param immediateReturn To give immediate return or not.
   */
  public static void moveForward(double distance, boolean immediateReturn) {
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
   * @param angle The angle to rotate by.
   * @param immediateReturn To give immediate return or not.
   */
  public static void rotate(double angle, boolean immediateReturn) {
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
   * This method makes the robot traverse the tunnel
   * 
   * @param stack The stack where the path is saved to.
   * @param TLLx The lower left X coordinate of the tunnel
   * @param TLLy The lower left Y coordinate of the tunnel
   * @param TURx The upper right X coordinate of the tunnel
   * @param TURy The upper right Y coordinate of the tunnel
   * @param tunnel The direction of the tunnel
   */
  public static void traverseTunnel(Stack<Coordinate> s, int TLLx, int TLLy, int TURx, int TURy,
      Tunnel tunnel) {
    if (tunnel == Tunnel.VERTICALDOWN) {
      travelTo(TLLx + 0.5, TLLy - 0.5);
      s.push(new Coordinate(TLLx + 0.5, TLLy - 0.5));
    } else if (tunnel == Tunnel.HORIZONTALRIGHT) {
      travelTo(TURx + 0.5, TURy - 0.5);
      s.push(new Coordinate(TURx + 0.5, TURy - 0.5));
    } else if (tunnel == Tunnel.HORIZONTALLEFT) {
      travelTo(TLLx - 0.5, TLLy + 0.5);
      s.push(new Coordinate(TLLx - 0.5, TLLy + 0.5));
    } else {
      // vertical tunnel
      travelTo(TLLx + 0.5, TURy + 0.5);
      s.push(new Coordinate(TLLx + 0.5, TURy + 0.5));
    }
  }

  /**
   * This method moves the robot with the input path and adds the path to the stack
   * 
   * @param stack The stack where the path is saved to.
   * @param path The the path the robot will take
   */
  public static void travelByPath(Stack<Coordinate> s, LinkedList<Coordinate> path) {
    while (!path.isEmpty()) {
      Coordinate location = path.remove();
      Navigation.travelTo(location.x, location.y);
      s.push(location);
      try {
        Thread.sleep(100);
      } catch (Exception e) {
      }
    }
  }

  /**
   * This method moves the robot with the input path, but does not save the path
   * 
   * @param path The path which contains coordinates to travel to
   */
  public static void travelByPath(LinkedList<Coordinate> path) {
    while (!path.isEmpty()) {
      Coordinate location = path.remove();
      Navigation.travelTo(location.x, location.y);
      try {
        Thread.sleep(100);
      } catch (Exception e) {
      }
    }
  }

  /**
   * This method makes the robot move back full speed to drop the ring
   */
  public static void dropRing() {
    leftMotor.setSpeed(DROP_SPEED);
    rightMotor.setSpeed(DROP_SPEED);
    leftMotor.rotate(-convertDistance(WHEEL_RAD, 10), true);
    rightMotor.rotate(-convertDistance(WHEEL_RAD, 10), false);
  }

  /**
   * turns the robot to face a coordinate
   * 
   * @param x The x the robot should be facing
   * @param y The y the robot should be facing
   */
  public static void face(double x, double y) {
    double angleToTurnTo = calculateAngle(x, y, odo);
    turnTo(angleToTurnTo);
  }

  /**
   * This method controls the robot to move towards x, y (tile position).
   * 
   * @param x The x position to travel to.
   * @param y The y position to travel to.
   */
  public static void travelTo(double x, double y) {
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

    while (true) {
      currentPosition = odo.getXYT();
      double angleDif = Math.abs(currentPosition[2] - angleToTurnTo);
      if ((angleDif > 1 && angleDif < 20) || (angleDif > 350 && angleDif < 359)) {
        stop();
        Sound.beep();
        turnTo(angleToTurnTo);
        currentDistance =
            calculateDistance(x * TILE_SIZE, y * TILE_SIZE, currentPosition[0], currentPosition[1]);
        moveForward(currentDistance, true);
      }
      if (!leftMotor.isMoving() && !rightMotor.isMoving()) {
        break;
      }
    }
  }

  /**
   * This method turns the robot in place to the absolute angle theta.
   * 
   * @param theta The angle to turn to.
   */
  public static void turnTo(double theta) {
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
   * This method calculates the angle which the robot has to turn to reach tile position x, y using
   * the data from the odometer which is passed into the method.
   * 
   * @param x The x position which is used to calculate the angle.
   * @param y The y position which is used to calculate the angle.
   * @param odo The odometer from which to obtain the robot's current position and heading.
   * @return The angle which the robot has to turn to reach tile position x, y using the data from
   *         the odometer which is passed into the method.
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
   * @param radius The radius of the wheel.
   * @param distance The distance to travel.
   * @return The rotation in degrees needed for the wheel to cover distance.
   */
  private static int convertDistance(double radius, double distance) {
    return (int) ((180.0 * distance) / (Math.PI * radius));
  }

  /**
   * This method computes how much the wheels should turn to get to an angle
   * 
   * @param radius The radius of the wheels.
   * @param width The wheelbase of the robot.
   * @param angle The angle to turn.
   * @return The rotation in degrees needed for the robot to turn the given angle.
   */
  public static int convertAngle(double radius, double width, double angle) {
    return convertDistance(radius, Math.PI * width * angle / 360.0);
  }

  /**
   * This method calculates the Euclidean distance given x, y initial and x, y final.
   * 
   * @param x1 The initial x.
   * @param y1 The initial y.
   * @param x2 The final x.
   * @param y2 The final y.
   * @return The Euclidean distance given x, y initial and x, y final.
   */
  private static double calculateDistance(double x1, double y1, double x2, double y2) {
    double distance;
    distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    return distance;
  }

  /**
   * This method calculates the minimum difference between two angles a1 and a2.
   * 
   * @param a1 The first angle.
   * @param a2 The second angle.
   * @return The minimum difference between two angles a1 and a2.
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
  public static void stop() {
    leftMotor.stop(true);
    rightMotor.stop(false);
  }

  /**
   * Set speed for both motors
   * 
   * @param speed The speed at which to set the motors to.
   */
  public static void setSpeed(int speed) {
    leftMotor.setSpeed(speed);
    rightMotor.setSpeed(speed);
  }

  /**
   * Move left and right motors backwards.
   * 
   */
  public static void backwards() {
    setSpeed(FORWARD_SPEED);
    leftMotor.backward();
    rightMotor.backward();
  }

  /**
   * Checks if the robot is navigating.
   * 
   * @return True if robot is navigating, False if not navigating.
   */
  public static boolean isNavigating() {
    if (leftMotor.isMoving() || rightMotor.isMoving())
      return true;
    else
      return false;
  }

  /**
   * Checks if the robot is turning.
   * 
   * @return True if robot is turning, False if not turning.
   */
  public static boolean isTurning() {
    if (leftMotor.getSpeed() == ROTATE_SPEED || rightMotor.getSpeed() == ROTATE_SPEED)
      return true;
    else
      return false;
  }

  /**
   * Gets the tacho count of the wanted wheel
   * 
   * @param side The side of the wheel to get the tacho count of
   * @return Tacho count of the wheel
   */
  public static int getTacho(Side side) {
    if (side == Side.LEFT) {
      return leftMotor.getTachoCount();
    } else if (side == Side.RIGHT) {
      return rightMotor.getTachoCount();
    } else {
      return 0;
    }
  }
}
