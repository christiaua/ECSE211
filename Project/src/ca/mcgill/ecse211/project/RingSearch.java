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

public class RingSearch {
	private static Poller poller;
	private static RingDetector ringDetector;

	private static final int MOTOR_SPEED = 50;
	private static final int DROP_SPEED = 300;
	private static final double DISTANCE_DETECT = 5;
	private static final double DISTANCE_GRAB = 8;

	private static boolean hasBottomRing = false;
	private static boolean hasTopRing = false;
	
	  // singleton class control
	  private volatile static int numberOfIntances = 0;
	  private static final int MAX_INSTANCES = 1;
	  private static RingSearch ringSearch = null;

	  // Thread control tools
	  private static Lock lock = new ReentrantLock(true); // Fair lock for concurrent writing
	  private volatile static boolean isReseting = false; // Indicates if a thread is trying to reset
	                                                      // any
	  // position parameters
	  private static Condition doneReseting = lock.newCondition(); // Let other threads know that a
	                                                               // reset operation is over.

	private static final EV3MediumRegulatedMotor upperMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

	private static final EV3MediumRegulatedMotor lowerMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B"));

	public RingSearch(int tGx, int tGy) throws PollerException, OdometerExceptions {
	    if (numberOfIntances < MAX_INSTANCES) { // create object and
	        // return it
			RingSearch.poller = Poller.getPoller();
			RingSearch.ringDetector = RingDetector.getRingDetector();
			upperMotor.setSpeed(MOTOR_SPEED);
			lowerMotor.setSpeed(MOTOR_SPEED);
	        numberOfIntances += 1;
	        ringSearch = this;
	    }
	}

	public static void goToGrabPosition() {
		Navigation.moveForward(DISTANCE_DETECT, false);
	}
	public static void goToPosition() {
		Navigation.moveForward(-DISTANCE_DETECT, false);
	}
	public static int findRing(Coordinate coord, HashMap<ColourType, Coordinate> ringMap) {
		int hasRing = 0;
		upperMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.setSpeed(MOTOR_SPEED);

		Navigation.moveForward(DISTANCE_DETECT, false);

		upperMotor.rotateTo(130, false);

		poller.enableColourDetection(true);
		ringDetector.clearFoundRings();

		upperMotor.rotateTo(80, false);

		if (RingDetector.foundRing()) {
			if(!hasBottomRing) {
				upperMotor.rotateTo(70);
				grabLowerRing(0);
				hasRing = 1;
			}
		} else {
			ringDetector.clearFoundRings();

			upperMotor.rotateTo(50, false);

			if (RingDetector.foundRing()) {
				if(hasBottomRing) {
					upperMotor.rotateTo(70, false);
					grabUpperRing();
					hasRing = 2;
				}
				else {
					ringMap.put(RingDetector.getFoundRing(), coord);
				}
			}
			upperMotor.rotateTo(70);
		}
		Navigation.moveForward(-DISTANCE_DETECT, false);
		poller.enableColourDetection(false);
		return hasRing;
	}

	public static void floatMotors() {
		upperMotor.forward();
		upperMotor.flt();
		lowerMotor.backward();
		lowerMotor.flt();
	}

	public static void dropRing() {
		upperMotor.rotate(-50, true);
		lowerMotor.rotate(50, false);
		upperMotor.setSpeed(DROP_SPEED);
		lowerMotor.setSpeed(DROP_SPEED);
		upperMotor.rotate(70, true);
		lowerMotor.rotate(-70, true);
	}

	public static void grabUpperRing() {
		hasTopRing = true;
		Navigation.moveForward(DISTANCE_GRAB, false);
		upperMotor.rotate(-10, true);
		Navigation.moveForward(-DISTANCE_GRAB, false);
		Navigation.stop();
		upperMotor.rotate(10, true);
	}

	public static void grabLowerRing(int num) {
		hasBottomRing = true;
		if (num > 1) {
			return;
		}
		upperMotor.rotate(-75, false);
		Navigation.moveForward(DISTANCE_GRAB, false);
		Navigation.stop();
		lowerMotor.rotate(30, true);
		Navigation.moveForward(-DISTANCE_GRAB, false);
		Navigation.stop();
		upperMotor.rotate(75, true);
		if (num == 0) {
			lowerMotor.rotate(-30, true);
		}
	}

	public void enableTunnel(boolean immediateReturn) {
		upperMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.rotate(-70, true);
		upperMotor.rotate(70, immediateReturn);
	}

	public void disableTunnel(boolean immediateReturn) {
		upperMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.rotate(70, true);
		upperMotor.rotate(-70, immediateReturn);
	}

	public void stop() {
		upperMotor.stop(true);
		lowerMotor.stop(false);
	}

}
