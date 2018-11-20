package ca.mcgill.ecse211.project;

import java.util.HashMap;
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
	private static final double DISTANCE_DETECT = 6;
	private static final double DISTANCE_GRAB = 8;

	private static boolean hasBottomRing = false;
	private static boolean hasTopRing = false;

	private static final EV3MediumRegulatedMotor upperMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

	private static final EV3MediumRegulatedMotor lowerMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B"));

	public RingSearch(int tGx, int tGy) throws PollerException, OdometerExceptions {
		RingSearch.poller = Poller.getPoller();
		RingSearch.ringDetector = RingDetector.getRingDetector();
		upperMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.setSpeed(MOTOR_SPEED);
	}

	public static boolean hasBottomRing() {
		return hasBottomRing;
	}

	public static boolean hasTopRing() {
		return hasTopRing;
	}

	public static void findRing(Coordinate coord, HashMap<ColourType, Coordinate> ringMap) {
		upperMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.setSpeed(MOTOR_SPEED);

		Navigation.moveForward(DISTANCE_DETECT, false);

		upperMotor.rotateTo(130, false);

		poller.enableColourDetection(true);
		ringDetector.clearFoundRings();

		upperMotor.rotateTo(90, false);

		if (RingDetector.foundRing()) {
			upperMotor.rotateTo(70);
			grabLowerRing(0);
		} else {
			ringDetector.clearFoundRings();

			upperMotor.rotateTo(50, false);

			if (RingDetector.foundRing()) {
				if(hasBottomRing) {
					grabUpperRing();
				}
				else {
					ringMap.put(RingDetector.getFoundRing(), coord);
				}
			}
			upperMotor.rotateTo(70);
		}

		Navigation.moveForward(-DISTANCE_DETECT, false);
		poller.enableColourDetection(false);
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
		upperMotor.rotate(-20, true);
		Navigation.moveForward(-DISTANCE_GRAB, false);
		Navigation.stop();
		upperMotor.rotate(20, true);
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
