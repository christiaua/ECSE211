package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.poller.Poller;
import ca.mcgill.ecse211.poller.PollerException;
import ca.mcgill.ecse211.poller.RingDetector.ColourType;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

public class RingSearch {
	private static Poller poller;
	
	private static int TGx = 5;
	private static int TGy = 7;
	
	private static final double OFFSET = 0.5;
	private static final double TILE_SIZE = 30.48;
	private static final int MOTOR_SPEED = 50;
	private static final double D = 10;
	
	
	private static final EV3MediumRegulatedMotor upperMotor =
			new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));
	
	private static final EV3MediumRegulatedMotor lowerMotor =
			new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B"));
	
	public RingSearch(int tGx, int tGy, Navigation nav) throws PollerException {
		RingSearch.poller = Poller.getPoller();
		RingSearch.TGx = tGx;
		RingSearch.TGy = tGy;
		upperMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.setSpeed(MOTOR_SPEED);
	}

	public int findRing() {
		int position = 3;
		if(!(poller.getColour() == ColourType.NONE)) {
			position = 1;
		}
		upperMotor.rotate(20, false);
		
		if(!(poller.getColour() == ColourType.NONE)) {
			position = 0;
		}
		upperMotor.rotate(-5, false);
		return position;
	}

	//0 low; 1 upper
	public static void grabRing(int ringPosition) {
//		switch(ringPosition) {
//			case 0:
//				upperMotor.rotate(-75, false);
//				navigation.moveForward(5, false);
//				lowerMotor.rotate(30, true);
//				break;
//			case 1:
//				navigation.moveForward(5, false);
//				upperMotor.rotate(-30, true);
//				break;
//			default:
//				break;
//		}	
		Navigation.moveForward(D/2, false);
		upperMotor.rotate(-75, false);
		Navigation.moveForward(8, false);
		Navigation.stop();
		lowerMotor.rotate(30, true);
		Navigation.moveForward(-D, false);
		Navigation.stop();
		upperMotor.rotate(75, false);
		Navigation.moveForward(D+2, false);
		Navigation.stop();
		upperMotor.rotate(-20, true);
		Navigation.moveForward(-D, false);
		Navigation.stop();
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
