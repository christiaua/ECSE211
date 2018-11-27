package ca.mcgill.ecse211.project;

import java.text.DecimalFormat;
import ca.mcgill.ecse211.odometer.*;
import ca.mcgill.ecse211.poller.*;
import ca.mcgill.ecse211.poller.RingDetector.ColourType;
import lejos.hardware.lcd.TextLCD;

/**
 * This class is used to display the content of the odometer variables (x, y,
 * Theta) as well as the values read in the poller
 * 
 * @author Sophie Deng
 */
public class Display implements Runnable {

	private Odometer odo;
	private TextLCD lcd;
	private double[] position;
	private final long DISPLAY_PERIOD = 25;
	private long timeout = Long.MAX_VALUE;

	private Poller poller;
	private ColourType colourType = ColourType.NONE;

	private double distance = 0;

	/**
	 * This is the class constructor
	 * 
	 * @param lcd
	 *            The TextLCD made in the main
	 * @throws OdometerExceptions
	 * @throws PollerException
	 */
	public Display(TextLCD lcd) throws OdometerExceptions, PollerException {
		odo = Odometer.getOdometer();
		this.lcd = lcd;
		poller = Poller.getPoller();
		odo = Odometer.getOdometer();
	}

	/**
	 * 
	 * @param lcd
	 *            TextLCD made in the main
	 * @param timeout
	 *            The timeout time for how often the display updates
	 * @throws OdometerExceptions
	 * @throws PollerException
	 */
	public Display(TextLCD lcd, long timeout) throws OdometerExceptions, PollerException {
		this.timeout = timeout;
		this.lcd = lcd;
		poller = Poller.getPoller();
		odo = Odometer.getOdometer();
	}

	/**
	 * Starts the display thread. It displays X, Y, T, distance and colour
	 */
	public void run() {

		lcd.clear();

		long updateStart, updateEnd;

		long tStart = System.currentTimeMillis();
		do {
			updateStart = System.currentTimeMillis();

			// Retrieve x, y and Theta information
			position = odo.getXYT();
			distance = poller.getDistance();
			colourType = poller.getColour();

			// Print x,y, and theta information
			DecimalFormat numberFormat = new DecimalFormat("######0.00");
			lcd.drawString("X: " + numberFormat.format(position[0] / 30.48), 0, 0);
			lcd.drawString("Y: " + numberFormat.format(position[1] / 30.48), 0, 1);
			lcd.drawString("T: " + numberFormat.format(position[2]), 0, 2);
			lcd.drawString("SC: " + numberFormat.format(Project.SC), 0, 3);
			lcd.drawString("RingSet: (" + Project.TGx + ", " + Project.TGy + ")", 0, 4);
			lcd.drawString("Tunnel: (" + Project.TLLx + ", " + Project.TLLy + ") (" + Project.TURx + ", " + Project.TURy + ") ", 0, 5);
			lcd.drawString("Placement: " + Project.tunnel, 0, 6);
			
			// this ensures that the data is updated only once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < DISPLAY_PERIOD) {
				try {
					Thread.sleep(DISPLAY_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while ((updateEnd - tStart) <= timeout);
	}
}
