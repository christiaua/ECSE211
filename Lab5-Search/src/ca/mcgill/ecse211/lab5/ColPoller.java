package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.robotics.SampleProvider;

/**
 * This class gets the samples for the light sensors
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 */
public class ColPoller extends Thread {
	private SampleProvider rgbSample;
	private float[] rgbData;
	private SampleProvider redSample;
	private float[] redData;
	private float lastRedReading;
	private float currentRedReading;
	private int target;
	private SampleProvider us;
	private float[] usData;
	public int distance;
	public int[] lastDistance = new int[2];


	/**
	 * Constructor 
	 * @param sample a SampleProvider from which we fetch the samples.
	 * @param lightData a float array to store the samples.
	 * @param ll a LightLocalizer to which we will pass the data through a synchronized setter.
	 */
	public ColPoller(SampleProvider rgbSample, float[] rgbData, SampleProvider redSample, float[] redData, int TR,
			SampleProvider us, float[] usData) {
		this.rgbSample = rgbSample;
		this.rgbData = rgbData;
		this.redSample = redSample;
		this.redData = redData;
		this.target = TR;
		this.us = us;
		this.usData = usData;
	}

	/**
	 * Get the sample from both light sensors and processes the data
	 */
	public void run() {
		int counter = 0;
		while (true) {
			redSample.fetchSample(redData, 0);

			lastRedReading = currentRedReading;
			currentRedReading = redData[0];

			rgbSample.fetchSample(rgbData, 0);
			
			try {
				RingDetector.processRGBData(rgbData[0], rgbData[1], rgbData[2], target);
			} catch (OdometerExceptions e1) {
				e1.printStackTrace();
			}
			
			us.fetchSample(usData, 0); // acquire data

			if(counter % 2 == 0){
				lastDistance[0] = distance;
			}
			else{
				lastDistance[1] = distance;
			}
			distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
			counter++;
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			} 
		}
	}

	/**
	 * Get R, G and B values
	 * @return
	 */
	public float[] getRGBReading(){
		return this.rgbData;
	}

	/**
	 * Get the Red value
	 * @return
	 */
	public float getCurrentRedReading(){
		return this.currentRedReading;
	}

	/**
	 * Get previous Red value
	 * @return
	 */
	public float getLastRedReading(){
		return this.lastRedReading;
	}
}