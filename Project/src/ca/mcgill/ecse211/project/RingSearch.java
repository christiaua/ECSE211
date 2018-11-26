package ca.mcgill.ecse211.project;

import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.poller.Poller;
import ca.mcgill.ecse211.poller.PollerException;
import ca.mcgill.ecse211.poller.RingDetector;
import ca.mcgill.ecse211.poller.RingDetector.ColourType;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

/**
 * This class controls the two motors of the ring grabbing mechanism.
 * 
 * @author Sophie Deng
 * @author Edward Huang
 *
 */
public class RingSearch {
	private static Poller poller;
	private static RingDetector ringDetector;

	private static final int MOTOR_SPEED = 50;
	private static final int DROP_SPEED = 300;
	private static final double DISTANCE_DETECT = 5;
	private static final double DISTANCE_GRAB = 8;

	private static int TGx;
	private static int TGy;

	// singleton class control
	private volatile static int numberOfIntances = 0;
	private static final int MAX_INSTANCES = 1;

	private static final EV3MediumRegulatedMotor upperMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

	private static final EV3MediumRegulatedMotor lowerMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B"));

	/**
	 * Constructor
	 * 
	 * @param tGx
	 *            The x position of the ring tree
	 * @param tGy
	 *            The y position of the ring tree
	 * @throws PollerException
	 * @throws OdometerExceptions
	 */
	public RingSearch(int tGx, int tGy) throws PollerException, OdometerExceptions {
		if (numberOfIntances < MAX_INSTANCES) { // create object and
			// return it
			RingSearch.poller = Poller.getPoller();
			RingSearch.ringDetector = RingDetector.getRingDetector();
			upperMotor.setSpeed(MOTOR_SPEED);
			lowerMotor.setSpeed(MOTOR_SPEED);
			numberOfIntances += 1;
			TGx = tGx;
			TGy = tGy;
		}
	}

	/**
	 * Makes the robot go to grabbing position
	 */
	public static void goToGrabPosition() {
		Navigation.moveForward(DISTANCE_DETECT, false);
	}

	/**
	 * Makes the robot go back from the grabbing position
	 */
	public static void goToPosition() {
		Navigation.moveForward(-DISTANCE_DETECT, false);
	}

	/**
	 * Makes the sensor go up and down to detect the ring
	 * 
	 */
	public static void findRing() {
		upperMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.setSpeed(MOTOR_SPEED);

		Navigation.moveForward(DISTANCE_DETECT, false);

		upperMotor.rotateTo(130, false);

		poller.enableColourDetection(true);
		ringDetector.clearFoundRings();

		upperMotor.rotateTo(50, false);
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		upperMotor.rotateTo(75);
		poller.enableColourDetection(false);
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		grabLowerRing();
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		grabUpperRing();
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		Navigation.moveForward(-DISTANCE_DETECT, false);
	}

	/**
	 * This method floats the two motors
	 */
	public static void floatMotors() {
		upperMotor.forward();
		upperMotor.flt();
		lowerMotor.backward();
		lowerMotor.flt();
	}

	/**
	 * This method makes the robot drop the ring by moving the motors up and down
	 */
	public static void dropRing() {
		upperMotor.rotate(-50, true);
		lowerMotor.rotate(50, false);
		upperMotor.setSpeed(DROP_SPEED);
		lowerMotor.setSpeed(DROP_SPEED);
		upperMotor.rotate(70, true);
		lowerMotor.rotate(-70, true);
	}

	/**
	 * This method moves the upper motor to get the top ring
	 */
	public static void grabUpperRing() {
		Navigation.face(TGx, TGy);
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		Navigation.moveForward(DISTANCE_GRAB, false);
		upperMotor.rotate(-15, true);
		Navigation.moveForward(-DISTANCE_GRAB, false);
		Navigation.stop();
		upperMotor.rotate(15, true);
	}

	/**
	 * This method moves the lower motor to get the bottom ring
	 */
	public static void grabLowerRing() {
		Navigation.face(TGx, TGy);
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		upperMotor.rotate(-75, false);
		Navigation.moveForward(DISTANCE_GRAB, false);
		Navigation.stop();
		lowerMotor.rotate(30, true);
		Navigation.moveForward(-DISTANCE_GRAB, false);
		Navigation.stop();
		lowerMotor.rotate(-30, true);
		upperMotor.rotate(75, false);
	}

	/**
	 * This method lowers the two ring grabbers to allow the robot to fit in the
	 * tunnel
	 * 
	 * @param immediateReturn
	 *            True if an immediate return is wanted
	 */
	public void enableTunnel(boolean immediateReturn) {
		upperMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.rotate(-70, true);
		upperMotor.rotate(70, immediateReturn);
	}

	/**
	 * This method lifts the two ring grabbers to put the robot back to the starting
	 * position
	 * 
	 * @param immediateReturn
	 *            True if an immediate return is wanted
	 */
	public void disableTunnel(boolean immediateReturn) {
		upperMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.rotate(70, true);
		upperMotor.rotate(-70, immediateReturn);
	}

	/**
	 * This method stops both motors
	 */
	public void stop() {
		upperMotor.stop(true);
		lowerMotor.stop(false);
	}

}
