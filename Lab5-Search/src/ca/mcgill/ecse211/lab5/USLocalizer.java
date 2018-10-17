package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * This class does the ultrasonic localization
 * 
 * @author Sophie Deng
 */
public class USLocalizer {

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private Navigation navigation;

	private static final int ROTATE_SPEED = 50;
	private static final double WALLDISTANCE = 35;
	private static final double NOISE = 3;

	/**
	 * Constructor
	 * @throws OdometerExceptions 
	 */
	public USLocalizer(SampleProvider usSensor, float[] usData, Navigation nav) throws OdometerExceptions{
		this.odo = Odometer.getOdometer();
		this.usSensor = usSensor;
		this.usData = usData;
		this.navigation = nav;
	}

	/**
	 * Perform localization, robot should be facing north
	 * @param true for rising edge, false for falling edge
	 */
	public void doLocalization(boolean risingEdge) {
		double angleA, angleB, angleZero; //angleA is the backwall and angleB is the side wall and angleZero is the offset
		//get the two angles
		if(risingEdge) {
			angleA = getAngleARisingEdge();
			angleB = getAngleBRisingEdge();
		}
		else {
			angleA = getAngleAFallingEdge();
			angleB = getAngleBFallingEdge();
		}
		//get offset angle
		angleZero = getAngleZero(angleA, angleB);

		//get real angle
		double currentAngle = odo.getXYT()[2];
		double realAngle = (currentAngle + angleZero) % 360;
		if(realAngle < 0) {
			realAngle += 360;
		}

		//set real angle and turn to 0
		odo.setTheta(realAngle);
		navigation.turnTo(0);
	}

	/**
	 * get angleA for rising edge
	 * @return
	 */
	private double getAngleARisingEdge() {
		//rotate robot counterclockwise until it sees a wall
		navigation.rotateWheels(-ROTATE_SPEED, ROTATE_SPEED);
		while(true) {
			if(getDistance() <= WALLDISTANCE + NOISE) {
				Sound.buzz();
				break;
			}
		}
		
		while(true) {
			if(getDistance() > WALLDISTANCE) {
				Sound.buzz();
				navigation.stop();
				break;
			}
		}
		//return back wall angle
		return odo.getXYT()[2];
	}
	/**
	 * get angleB for rising edge
	 * @return
	 */
	private double getAngleBRisingEdge() {
		//rotate clockwise until it sees a wall
		navigation.rotateWheels(ROTATE_SPEED, -ROTATE_SPEED);
		while(true) {
			if(getDistance() <= WALLDISTANCE + NOISE) {
				Sound.buzz();
				break;
			}
		}
		
		while(true) {
			if(getDistance() > WALLDISTANCE) {
				Sound.buzz();
				navigation.stop();
				break;
			}
		}
		//returns side wall angle
		return odo.getXYT()[2];
	}
	/**
	 * get angleA for falling edge
	 * @return
	 */
	private double getAngleAFallingEdge() {
		//rotate clockwise until it doesn't sees a wall
		navigation.rotateWheels(ROTATE_SPEED, -ROTATE_SPEED);
		while(true) {
			if(getDistance() > WALLDISTANCE + NOISE) {
				Sound.buzz();
				break;
			}
		}
		
		while(true) {
			if(getDistance() <= WALLDISTANCE) {
				Sound.buzz();
				navigation.stop();
				break;
			}
		}
		//returns back wall angle
		return odo.getXYT()[2];
	}
	/**
	 * get angleB for falling edge
	 * @return
	 */
	private double getAngleBFallingEdge() {
		//rotate counterclockwise until it doesn't sees a wall
		navigation.rotateWheels(-ROTATE_SPEED, ROTATE_SPEED);
		while(true) {
			if(getDistance() > WALLDISTANCE + NOISE) {
				Sound.buzz();
				break;
			}
		}
		
		while(true) {
			if(getDistance() <= WALLDISTANCE) {
				Sound.buzz();
				navigation.stop();
				break;
			}
		}
		//returns side wall angle
		return odo.getXYT()[2];
	}

	/**
	 * get distance from the wall with ultrasonic sensor
	 * @return
	 */
	private float getDistance() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0]*100;
		return distance;
	}

	/**
	 * get offset angle
	 * @param angle of backwall, angle of side wall
	 * @return
	 */
	private double getAngleZero(double angleA, double angleB) {
		double angleZero = 0;
		if(angleA < angleB) {
			angleZero = 255 - (angleA + angleB)/2.0;
		}
		else {
			angleZero = 45 - (angleA + angleB)/2.0;
		}
		return angleZero;
	}

}
