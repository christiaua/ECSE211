package ca.mcgill.ecse211.poller;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class processes the ultrasonic sensor data.
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 * @author Sophie Deng
 */
public class USSensorData {
	// sampling distance
	private final int SAMPLE_SIZE = 5;
	private double[] lastDistances = new double[SAMPLE_SIZE];
	private volatile static double distance;
	private volatile static double lastDistance;
	private volatile static int counter = 0;

	// Class control variables
	private volatile static int numberOfIntances = 0;
	private static final int MAX_INSTANCES = 1;
	private static USSensorData senData = null;

	// Thread control tools
	private static Lock lock = new ReentrantLock(true); // Fair lock for concurrent writing
	private volatile boolean isUpdating = false;
	private Condition doneUpdating = lock.newCondition();

	/**
	 * Constructor, sets all data to 0
	 */
	private USSensorData() {
		distance = 0;
		counter = 0;
	}

	/**
	 * Gets the one instance of the class or creates a new instance if one doesn't
	 * exist already.
	 * 
	 * @return USSensorData instance.
	 * @throws PollerException
	 *             If more than one instance of this class exists
	 */
	public synchronized static USSensorData getSensorData() throws PollerException {
		if (senData != null) { // Return existing object
			return senData;
		} else if (numberOfIntances < MAX_INSTANCES) { // create object and
			// return it
			senData = new USSensorData();
			numberOfIntances += 1;
			return senData;
		} else {
			throw new PollerException("Only one intance of the SensorData can be created.");
		}
	}

	/**
	 * Get the distance read by the ultrasonic sensor after it has been filtered.
	 * 
	 * @return distance The filtered distance.
	 */
	public double getDistance() {
		double d = 0;
		lock.lock();
		try {
			while (isUpdating) {
				doneUpdating.await();
			}
			d = distance;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return d;
	}

	/**
	 * Get the previous ultrasonic sensor reading after it has been filtered.
	 * 
	 * @return distance The previous ultrasonic sensor reading after it has been
	 *         filtered.
	 */
	public double getLastDistance() {
		double d = 0;
		lock.lock();
		try {
			while (isUpdating) {
				doneUpdating.await();
			}
			d = lastDistance;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return d;
	}

	/**
	 * Filters ultrasonic sensor data by taking the median of previous readings.
	 * Updates the current and previous filtered ultrasonic sensor readings.
	 * 
	 * @param newDistance
	 *            The unfiltered ultrasonic sensor reading.
	 */
	public void updateDistance(double newDistance) {
		lock.lock();
		isUpdating = true;
		try {
			lastDistances[counter] = newDistance;
			if (counter == SAMPLE_SIZE - 1) {
				Arrays.sort(lastDistances);
				lastDistance = distance;
				distance = lastDistances[2];
			}
			counter++;
			counter = counter % SAMPLE_SIZE;
			isUpdating = false;
			doneUpdating.signalAll();
		} finally {
			lock.unlock();
		}
	}
}
