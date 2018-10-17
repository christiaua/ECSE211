/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import lejos.hardware.sensor.*;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

import lejos.robotics.SampleProvider;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.Sound;

/**
 * This class corrects the robot position based on the lines
 * 
 * @author Sophie and Edris
 */


public class OdometryCorrection implements Runnable {
	private static final long CORRECTION_PERIOD = 10;
	private static final double TILE_SIZE = 30.48;
	private Odometer odometer;

	private static final Port portColor = LocalEV3.get().getPort("S1");
	static EV3ColorSensor myColor  = new EV3ColorSensor(portColor); 
	static SampleProvider usIntensity = myColor.getRedMode();
	float[]sampleColor = new float[usIntensity.sampleSize()]; 

	/**
	 * This is the default class constructor. An existing instance of the odometer is used. This is to
	 * ensure thread safety.
	 * 
	 * @throws OdometerExceptions
	 */
	public OdometryCorrection() throws OdometerExceptions {

		this.odometer = Odometer.getOdometer();

	}

	/**
	 * Here is where the odometer correction code should be run.
	 * 
	 * @throws OdometerExceptions
	 */
	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;
		int lineCountYUP = 0;
		int lineCountYDOWN = 0;
		int lineCountXRIGHT = 0;
		int lineCountXLEFT = 0;

		while (true) {
			//line count must be smaller than number of lines we want to pass

			double position[] = odometer.getXYT();

			correctionStart = System.currentTimeMillis();
			usIntensity.fetchSample(sampleColor, 0);
			float intensity = sampleColor[0];

			//System.out.println(intensity);

			//if pass black line
			if(intensity < 0.40) {
				//if going up
				if (position[2] > 350 || position[2] < 10) {
					odometer.setTheta(0);


					//compare the car current position to the line count, if position is incorrect, probably missed a line
					//add 10 as a padding value
					lineCountYUP = ((int) position[1] + 10)/ (int) TILE_SIZE;

					//estimate the new "corrected" value
					double estimatedY = lineCountYUP * TILE_SIZE;
					odometer.setY(estimatedY);
					Sound.beep();
				}
				//if going right
				else if(Math.abs(position[2] - 90) < 15) {
					odometer.setTheta(90);

					//compare the car current position to the line count, if position is incorrect, probably missed a line
					lineCountXRIGHT = ((int) position[0] + 10) / (int) TILE_SIZE;

					double estimatedX = lineCountXRIGHT * TILE_SIZE;
					odometer.setX(estimatedX);
					Sound.beep();
				}
				//if going down
				else if(Math.abs(position[2] - 180) < 15) {
					odometer.setTheta(180);

					lineCountYDOWN = ((int) position[1] + 10) / (int) TILE_SIZE;

					double estimatedY = lineCountYDOWN * TILE_SIZE;
					odometer.setY(estimatedY);
					Sound.buzz();
				}
				//if going left
				else if (Math.abs(position[2] - 270) < 15){
					odometer.setTheta(270);

					//compare position value line to line count, if it's not the same, it probably missed a line
					lineCountXLEFT = ((int) position[0] + 10) / (int) TILE_SIZE;

					double estimatedX = lineCountXLEFT * TILE_SIZE;
					odometer.setX(estimatedX);
					Sound.buzz();
				}

			}

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here
				}
			}
		}
	}
}