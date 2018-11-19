package ca.mcgill.ecse211.project;

import java.util.HashMap;

import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.poller.Poller;
import ca.mcgill.ecse211.poller.PollerException;
import ca.mcgill.ecse211.poller.RingDetector.ColourType;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

public class RingSearch {
	private static Poller poller;

	private static final int MOTOR_SPEED = 50;
	private static final int DROP_SPEED = 300;
	private static final double D = 10;

	private static final EV3MediumRegulatedMotor upperMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

	private static final EV3MediumRegulatedMotor lowerMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B"));

	public RingSearch(int tGx, int tGy) throws PollerException, OdometerExceptions {
		RingSearch.poller = Poller.getPoller();
		upperMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.setSpeed(MOTOR_SPEED);
	}

	public int findRing() {
	  int position = -1;
	  poller.enableColourDetection(false);
	  upperMotor.setSpeed(MOTOR_SPEED);
      lowerMotor.setSpeed(MOTOR_SPEED);
      lowerMotor.rotateTo(-90, false);
	  upperMotor.rotateTo(130, false);
	  //Navigation.moveForward(D, false);
	  poller.enableColourDetection(true);
	  upperMotor.rotateTo(90, false);
	  try {
        Thread.sleep(1000);
      } catch (Exception e) {
      }
		if (!(poller.getColour() == ColourType.NONE)) {
		  position = 1;
			return position;
		}
		upperMotor.rotateTo(50, false);
		try {
          Thread.sleep(1000);
        } catch (Exception e) {
        }
		if (!(poller.getColour() == ColourType.NONE)) {
		  position = 0;
			return position;
		}
		//Navigation.moveForward(-D, false);
		System.out.println(position);
		return position;
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
		upperMotor.rotate(70,true);
		lowerMotor.rotate(-70, true);
	}
	
	public static void findRing(Coordinate coord, HashMap<ColourType, Coordinate> ringMap, int num) {
		Navigation.moveForward(D / 2, false);
		Navigation.stop();
//		
//		//if is lower ring, just grab it
//		if (!(poller.getColour() == ColourType.NONE)) {
//			grabLowerRing();
//		}
//		else {
//			//else, check if its upper and store the information
//			upperMotor.rotate(20, false);
//			if (!(poller.getColour() == ColourType.NONE)) {
//				ringMap.put(poller.getColour(), coord);
//			}
//			upperMotor.rotate(-20, false);
//			Navigation.moveForward(-(D/2), false);
//		}
		grabLowerRing(num);
	}

	public static void grabUpperRing() {
		Navigation.moveForward(D, false);
		upperMotor.rotate(-20, true);
		Navigation.moveForward(-(D + D / 2), false);
		Navigation.stop();
		upperMotor.rotate(20, true);
	}

	public static void grabLowerRing(int num) {
		if(num > 1) {
			Navigation.moveForward(-(D / 2), false);
			return;
		}
		upperMotor.rotate(-75, false);
		Navigation.moveForward(8, false);
		Navigation.stop();
		lowerMotor.rotate(30, true);
		Navigation.moveForward(-(8 + D / 2), false);
		Navigation.stop();
		upperMotor.rotate(75, true);
		if(num == 0) {
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
