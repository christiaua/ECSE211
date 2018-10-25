package ca.mcgill.ecse211.poller;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import ca.mcgill.ecse211.poller.RingDetector.ColourType;

/**
 * This class gets the samples for the light sensors
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 * @author Sophie Deng
 */
public class Poller implements Runnable {

	private static RingDetector ringDetector;
	private static USSensorData sensorData;

	private float lastRedReading;
	private float currentRedReading;

	private double unfilteredDistance;

	// Class control variables
	private volatile static int numberOfIntances = 0; 
	private static final int MAX_INSTANCES = 1; 
	private static Poller poller = null;

	// initialize us sensor
	private static final Port usPort = LocalEV3.get().getPort("S4");
	static SensorModes usSensor = new EV3UltrasonicSensor(usPort);
	static SampleProvider us = usSensor.getMode("Distance"); 
	static float[] usData = new float[us.sampleSize()]; 

	// initialize color sensors
	private static final Port colorSensorPort = LocalEV3.get().getPort("S1");
	static SensorModes colorSensor = new EV3ColorSensor(colorSensorPort);
	static SampleProvider rgbSample = colorSensor.getMode("RGB");
	static float[] rgbData = new float[rgbSample.sampleSize()];

	// initialize line detecting sensor
	private static final Port lsPort = LocalEV3.get().getPort("S2");
	static SensorModes lightSensor = new EV3ColorSensor(lsPort);
	static SampleProvider redSample = lightSensor.getMode("Red");
	static float[] redData = new float[redSample.sampleSize()];


	/**
	 * Constructor
	 * 
	 * @throws PollerException 
	 */
	public Poller() throws PollerException {
		ringDetector = RingDetector.getRingDetector();
		sensorData = USSensorData.getSensorData();
	}

	/**
	 * Gets the one instance of the class
	 * 
	 * @return the Poller
	 * @throws PollerException
	 */
	public static Poller getPoller() throws PollerException {
		if (poller != null) { // Return existing object
			return poller;
		} else if (numberOfIntances < MAX_INSTANCES) { // create object and return it
			poller = new Poller();
			numberOfIntances += 1;
			return poller;
		} else {
			throw new PollerException("Only one intance of the Poller can be created.");
		}
	}

	/**
	 * Get the samples and process them
	 */
	public void run() {
		while (true) {
			redSample.fetchSample(redData, 0);

			lastRedReading = currentRedReading;
			currentRedReading = redData[0];

			rgbSample.fetchSample(rgbData, 0);
			ringDetector.processRGBData(rgbData[0], rgbData[1], rgbData[2]);

			us.fetchSample(usData, 0); // acquire data
			unfilteredDistance = (usData[0] * 100.0); // extract from buffer, cast to int

			sensorData.updateDistance(unfilteredDistance);

			try {
				Thread.sleep(20);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Get R, G and B values
	 * 
	 * @return rgb data
	 */
	public float[] getRGBReading() {
		return rgbData;
	}

	/**
	 * Get the Red value
	 * 
	 * @return red value
	 */
	public float getCurrentRedReading() {
		return currentRedReading;
	}

	/**
	 * Get previous Red value
	 * 
	 * @return get previous red value
	 */
	public float getLastRedReading() {
		return lastRedReading;
	}

	/**
	 * Get the filtered distance to the wall
	 * @return distance
	 */
	public double getDistance() {
		return sensorData.getdistance();
	}

	/**
	 * Get the colour of the detected ring
	 * @return colour
	 */
	public ColourType getColour() {
		return ringDetector.getColourType();
	}
}
