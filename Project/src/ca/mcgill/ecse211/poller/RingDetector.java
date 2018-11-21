package ca.mcgill.ecse211.poller;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ca.mcgill.ecse211.odometer.*;
import ca.mcgill.ecse211.poller.RingDetector.ColourType;
import lejos.hardware.Sound;

/**
 * This class processes the colour sensor data
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 * @author Sophie Deng
 */
public class RingDetector {

	// data from testing sample
	private static final double[] Y_RGB_MEAN = { 0.849, 0.503, 0.160 };
	private static final double[] B_RGB_MEAN = { 0.137, 0.494, 0.851 };
	private static final double[] O_RGB_MEAN = { 0.967, 0.237, 0.092 };
	private static final double[] G_RGB_MEAN = { 0.451, 0.870, 0.190 };

	// prevents the robot from beeping continuously
	private static boolean[] foundRings = { false, false, false, false };

	// colour type
	public static enum ColourType {
		BLUE, ORANGE, YELLOW, GREEN, NONE
	};

	private static ColourType ringColour = ColourType.NONE;

	// singleton class control
	private volatile static int numberOfIntances = 0;
	private static final int MAX_INSTANCES = 1;
	private static RingDetector ringDet = null;

	// Thread control tools
	private static Lock lock = new ReentrantLock(true); // Fair lock for concurrent writing
	private volatile static boolean isReseting = false; // Indicates if a thread is trying to reset
														// any
	// position parameters
	private static Condition doneReseting = lock.newCondition(); // Let other threads know that a
																	// reset operation is over.

	/**
	 * Get the only instance of the ringDetector
	 * 
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
	 * This method checks if a ring has been found.
	 * 
	 * @return If a ring has been found.
	 */
	public static boolean foundRing() {
		lock.lock();
		boolean found = false;
		try {
			while (isReseting) { // If a reset operation is being executed, wait
				// until it is over.
				doneReseting.await(); // Using await() is lighter on the CPU
				// than simple busy wait.
			}
			for (int i = 0; i < foundRings.length; i++) {
				if (RingDetector.foundRings[i]) {
					// Sound.buzz();
					found = true;
				}
			}

		} catch (InterruptedException e) {
			// Print exception to screen
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return found;
	}

	public static ColourType getFoundRing() {
		if (foundRings[0]) {
			return ColourType.BLUE;
		} else if (foundRings[1]) {
			return ColourType.GREEN;
		} else if (foundRings[3]) {
			return ColourType.ORANGE;
		} else if (foundRings[2]) {
			return ColourType.YELLOW;
		} else {
			return ColourType.NONE;
		}
	}

	public void clearFoundRings() {
		for (int i = 0; i < foundRings.length; i++) {
			RingDetector.foundRings[i] = false;
		}
	}

	/**
	 * Beeps 1, 2, 3, or 4 times depending on the colour. If this method has already
	 * been called before for a ring colour, it does not beep.
	 * 
	 * @param colour
	 *            Valid values for colour are 0 (blue), 1 (green), 2 (yellow), 3
	 *            (orange) for beeping one, two, three or four times respectively.
	 */
	private void soundAlert(int colour) {
		if (foundRings[colour])
			return;
		switch (colour) {
		case 0:
			Sound.beep();
			break;
		case 1:
			Sound.twoBeeps();
			break;
		case 2:
			Sound.beep();
			Sound.twoBeeps();
			break;
		case 3:
			Sound.twoBeeps();
			Sound.twoBeeps();
			break;
		}
		foundRings[colour] = true;
	}

	/**
	 * Processes RGB data to determine whether a ring has been detected or not. If
	 * the RGB data corresponds to a ring, the robot beeps a number of times
	 * depending on the colour of the ring that has been detected. Beeps one time
	 * for blue, two times for green, three times for yellow and four times for
	 * orange. Does not beep again if the ring has already been detected previously.
	 * 
	 * @param R
	 *            The red of the RGB data.
	 * @param B
	 *            The blue of the RGB data.
	 * @param G
	 *            The green of the RGB data.
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

			// if is yellow
			if (dY < 0.02 && dY < dB && dY < dO && dY < dG) {
				ringColour = ColourType.YELLOW;
				soundAlert(2);
			}
			// if is blue
			else if (dB < 0.1 && dB < dY && dB < dO && dB < dG) {
				ringColour = ColourType.BLUE;
				soundAlert(0);
			}
			// if is orange
			else if (dO < 0.05 && dO < dY && dO < dG && dO < dB) {
				ringColour = ColourType.ORANGE;
				soundAlert(3);
			}
			// if is green
			else if (dG < 0.03 && dG < dB && dG < dO && dG < dY) {
				ringColour = ColourType.GREEN;
				soundAlert(1);
			}
			// none of the above = nothing detected
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
	 * Normalizes the RGB data.
	 * 
	 * @param R
	 *            Red of the RGB data.
	 * @param B
	 *            Blue of the RGB data.
	 * @param G
	 *            Green of the RGB data.
	 * @return The normalized RGB data.
	 */
	private static float[] normalizeRGBData(float R, float B, float G) {
		float[] result = new float[3];
		result[0] = R / (float) Math.sqrt(R * R + B * B + G * G);
		result[1] = B / (float) Math.sqrt(R * R + B * B + G * G);
		result[2] = G / (float) Math.sqrt(R * R + B * B + G * G);
		return result;
	}

	/**
	 * Get the colour of the ring detected.
	 * 
	 * @return The colour.
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
