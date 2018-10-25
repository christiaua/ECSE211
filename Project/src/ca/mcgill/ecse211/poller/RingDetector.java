package ca.mcgill.ecse211.poller;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.Sound;

/**
 * This class detects the rings
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 * @author Sophie Deng
 */
public class RingDetector {

	//data from testing sample
	private static final double[] Y_RGB_MEAN = {0.849, 0.503, 0.160};
	private static final double[] B_RGB_MEAN = {0.158, 0.706, 0.645};
	private static final double[] O_RGB_MEAN = {0.967, 0.237, 0.092};
	private static final double[] G_RGB_MEAN = {0.443, 0.874, 0.202};

	//prevents the robot from beeping continuously
	private static boolean[] foundRings = {false, false, false, false};

	//colour type
	public static enum ColourType {BLUE, ORANGE, YELLOW, GREEN, NONE};
	private static ColourType ringColour = ColourType.NONE;

	//singleton class control
	private volatile static int numberOfIntances = 0; 
	private static final int MAX_INSTANCES = 1; 
	private static RingDetector ringDet = null;

	// Thread control tools
	private static Lock lock = new ReentrantLock(true); // Fair lock for concurrent writing
	private volatile boolean isReseting = false; // Indicates if a thread is trying to reset any position parameters
	private Condition doneReseting = lock.newCondition(); // Let other threads know that a reset operation is over.

	/**
	 * Get the only instance of the ringDetector
	 * @return the ringdetector
	 * @throws PollerException
	 */
	public synchronized static RingDetector getRingDetector() throws PollerException {
		if (ringDet != null) { // Return existing object
			return ringDet;
		} else if (numberOfIntances < MAX_INSTANCES) { // create object and
			// return it
			ringDet = new RingDetector();
			numberOfIntances += 1;
			return ringDet;
		} else {
			throw new PollerException("Only one intance of the Odometer can be created.");
		}
	}

	/**
	 * Processes the data and checks ring colour
	 * 
	 * @param R
	 * @param B
	 * @param G
	 * @param target
	 * @throws OdometerExceptions
	 */
	public void processRGBData(float R, float B, float G) {
		lock.lock();
		isReseting = true;
		try {
			float dY, dB, dO, dG;
			float[] data = normalizeRGBData(R, B, G);

			dY = (float) Math.sqrt((data[0] - Y_RGB_MEAN[0]) * (data[0] - Y_RGB_MEAN[0])
					+ (data[1] - Y_RGB_MEAN[1]) * (data[1] - Y_RGB_MEAN[1])
					+ (data[2] - Y_RGB_MEAN[2]) * (data[2] - Y_RGB_MEAN[2]));

			dB = (float) Math.sqrt((data[0] - B_RGB_MEAN[0]) * (data[0] - B_RGB_MEAN[0])
					+ (data[1] - B_RGB_MEAN[1]) * (data[1] - B_RGB_MEAN[1])
					+ (data[2] - B_RGB_MEAN[2]) * (data[2] - B_RGB_MEAN[2]));

			dO = (float) Math.sqrt((data[0] - O_RGB_MEAN[0]) * (data[0] - O_RGB_MEAN[0])
					+ (data[1] - O_RGB_MEAN[1]) * (data[1] - O_RGB_MEAN[1])
					+ (data[2] - O_RGB_MEAN[2]) * (data[2] - O_RGB_MEAN[2]));

			dG = (float) Math.sqrt((data[0] - G_RGB_MEAN[0]) * (data[0] - G_RGB_MEAN[0])
					+ (data[1] - G_RGB_MEAN[1]) * (data[1] - G_RGB_MEAN[1])
					+ (data[2] - G_RGB_MEAN[2]) * (data[2] - G_RGB_MEAN[2]));

			//if is yellow
			if (dY < 0.020744 + 0.010672 * 2) {
				ringColour = ColourType.YELLOW;
				if (!foundRings[2]) {
					foundRings[2] = true;
					Sound.beep();
					Sound.beep();
				}
			} 
			//if is blue
			else if (dB < 0.1) {
				ringColour = ColourType.BLUE;
				if (!foundRings[0]) {
					foundRings[0] = true;
					Sound.beep();
					Sound.beep();
				}
			} 
			//if is orange
			else if (dO < 0.075) {
				ringColour = ColourType.ORANGE;
				if (!foundRings[3]) {
					foundRings[3] = true;
					Sound.beep();
					Sound.beep(); 
				}
			} 
			//if is green
			else if (dG < 0.023811 + 0.013883 * 2) {
				ringColour = ColourType.GREEN;
				if (!foundRings[1]) {
					foundRings[1] = true;
					Sound.beep();
					Sound.beep();
				}
			} 
			//none of the above = nothing detected
			else {
				ringColour = ColourType.NONE;
			}

			isReseting = false; // Done reseting
			doneReseting.signalAll(); // Let the other threads know that you are
			// done reseting
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Normalizes the data
	 * 
	 * @param R
	 * @param B
	 * @param G
	 * @return normalized data
	 */
	private static float[] normalizeRGBData(float R, float B, float G) {
		float[] result = new float[3];
		result[0] = R / (float) Math.sqrt(R * R + B * B + G * G);
		result[1] = B / (float) Math.sqrt(R * R + B * B + G * G);
		result[2] = G / (float) Math.sqrt(R * R + B * B + G * G);
		return result;
	}

	/**
	 * get the colour of the ring detected
	 * @return the colour
	 */
	public ColourType getColourType() {
		ColourType type = ColourType.NONE;
		lock.lock();
		try {
			while (isReseting) { // If a reset operation is being executed, wait
				// until it is over.
				doneReseting.await(); // Using await() is lighter on the CPU
				// than simple busy wait.
			}
			type = ringColour;

		} catch (InterruptedException e) {
			// Print exception to screen
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return type;
	}
}
