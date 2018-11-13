package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.poller.Poller;
import ca.mcgill.ecse211.poller.PollerException;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

public class RingSearch {
	private static Navigation navigation;
	private static Poller poller;
	
	private static int TGx = 5;
	private static int TGy = 7;
	
	private static final double OFFSET = 0.5;
	private static final double TILE_SIZE = 30.48;
	private static final int MOTOR_SPEED = 200;
	private static final double D = 10;
	
	
	private static final EV3MediumRegulatedMotor upperMotor =
			new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));
	
	private static final EV3MediumRegulatedMotor lowerMotor =
			new EV3MediumRegulatedMotor(LocalEV3.get().getPort("D"));
	
	public RingSearch(int tGx, int tGy, Navigation nav) throws PollerException {
		RingSearch.navigation = nav;
		RingSearch.poller = Poller.getPoller();
		RingSearch.TGx = tGx;
		RingSearch.TGy = tGy;
	}

	public int findRing() {
		navigation.travelTo(TGx + OFFSET, TGy + OFFSET);
		if(poller.foundRing()) return 0;
		navigation.travelTo(TGx - OFFSET, TGy + OFFSET);
		if(poller.foundRing()) return 1;
		navigation.travelTo(TGx - OFFSET, TGy - OFFSET);
		if(poller.foundRing()) return 2;
		navigation.travelTo(TGx + OFFSET, TGy - OFFSET);
		if(poller.foundRing()) return 3;
		return 4;
	}

	//0 right, 1 top, 2 left, 3 down
	public void grabRing(int ringPosition) {
		navigation.moveForward(-TILE_SIZE/2, false);
		switch(ringPosition) {
			case 0:
				navigation.turnTo(270);
				break;
			case 1:
				navigation.turnTo(180);
				break;
			case 2:
				navigation.turnTo(90);
				break;
			case 3:
				navigation.turnTo(0);
				break;
			default:
				return;
		}
		upperMotor.setSpeed(MOTOR_SPEED);
		lowerMotor.setSpeed(MOTOR_SPEED);
		upperMotor.rotate(100);
		lowerMotor.rotate(100);
		navigation.moveForward(D, false);
		upperMotor.rotate(-100);
		lowerMotor.rotate(-100);
		navigation.moveForward(-D, false);
		navigation.stop();
	}
	
	public void stop() {
		upperMotor.stop(true);
		lowerMotor.stop(false);
	}


}
