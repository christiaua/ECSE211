package ca.mcgill.ecse211.poller;

/**
 * This class is used to handle errors regarding the singleton pattern used for the ringDetector and
 * odometerData
 *
 */
@SuppressWarnings("serial")
public class PollerException extends Exception {

	public PollerException(String Error) {
		super(Error);
	}
}
