package ca.mcgill.ecse211.lab5;

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
	
	private static final double[] Y_RGB_MEAN = {0.849, 0.503, 0.160};
	private static final double[] B_RGB_MEAN = {0.158, 0.706, 0.645};
	private static final double[] O_RGB_MEAN = {0.967, 0.237, 0.092};
	private static final double[] G_RGB_MEAN = {0.443, 0.874, 0.202};
	
	private static boolean ringDetected = false;
	private static boolean targetFound = false;
	
	private static boolean[] foundRings = {false, false, false, false};
	
	/**
	 * Checks if a ring is detected
	 * @return
	 */
	public static boolean ringDetected() {
		return ringDetected;
	}
	
	/**
	 * Checks if the target ring was found
	 * @return
	 */
	public static boolean targetDetected() {
		return targetFound;
	}
	
	/**
	 * Processes the data and checks ring colour
	 * @param R
	 * @param B
	 * @param G
	 * @param target
	 * @throws OdometerExceptions 
	 */
	public static void processRGBData(float R, float B, float G, int target) throws OdometerExceptions{
		float dY, dB, dO, dG;
		float[] data = normalizeRGBData(R, B, G);
		target--;
		
		dY = (float) Math.sqrt( (data[0] - Y_RGB_MEAN[0]) * (data[0] - Y_RGB_MEAN[0])
				+ (data[1] - Y_RGB_MEAN[1]) * (data[1] - Y_RGB_MEAN[1])
				+ (data[2] - Y_RGB_MEAN[2]) * (data[2] - Y_RGB_MEAN[2]) );
		
		dB = (float) Math.sqrt( (data[0] - B_RGB_MEAN[0]) * (data[0] - B_RGB_MEAN[0])
				+ (data[1] - B_RGB_MEAN[1]) * (data[1] - B_RGB_MEAN[1])
				+ (data[2] - B_RGB_MEAN[2]) * (data[2] - B_RGB_MEAN[2]) );
		
		dO = (float) Math.sqrt( (data[0] - O_RGB_MEAN[0]) * (data[0] - O_RGB_MEAN[0])
				+ (data[1] - O_RGB_MEAN[1]) * (data[1] - O_RGB_MEAN[1])
				+ (data[2] - O_RGB_MEAN[2]) * (data[2] - O_RGB_MEAN[2]) );
		
		dG = (float) Math.sqrt( (data[0] - G_RGB_MEAN[0]) * (data[0] - G_RGB_MEAN[0])
				+ (data[1] - G_RGB_MEAN[1]) * (data[1] - G_RGB_MEAN[1])
				+ (data[2] - G_RGB_MEAN[2]) * (data[2] - G_RGB_MEAN[2]) );
		
		double[] position;
		try {
			position = Odometer.getOdometer().getXYT();
			//Lab5.lcd.drawString("L: " + Lab5.redData[0], 0, 0);
			Lab5.lcd.drawString("X: " + position[0], 0, 0);
			Lab5.lcd.drawString("Y: " + position[1], 0, 1);
			Lab5.lcd.drawString("T: " + position[2], 0, 2);
		} catch (OdometerExceptions e) {
			Lab5.lcd.drawString("Couldn't Access Odometer", 0, 0);
			e.printStackTrace();
		}

		if(dY < 0.020744+0.010672*2){
			ringDetected = true;
			Lab5.lcd.drawString("Object detected", 0, 4);
			Lab5.lcd.drawString("Yellow", 0, 5);
			if(!foundRings[2]) {
				foundRings[2] = true;
				if(target == 2) {
					Sound.beep();
					targetFound = true;
				}
				else {
					Sound.beep();
					Sound.beep();
				}
				
			}
		}
		else if(dB < 0.1){
			ringDetected = true;
			Lab5.lcd.drawString("Object detected", 0, 4);
			Lab5.lcd.drawString("Blue  ", 0, 5);
			if(!foundRings[0]) {
				foundRings[0] = true;
				if(target == 0) {
					Sound.beep();
					targetFound = true;
				}
				else {
					Sound.beep();
					Sound.beep();
				}
				
			}
		}
		else if(dO < 0.075){
			ringDetected = true;
			Lab5.lcd.drawString("Object detected", 0, 4);
			Lab5.lcd.drawString("Orange", 0, 5);
			if(!foundRings[3]) {
				foundRings[3] = true;
				if(target == 3) {
					Sound.beep();
					targetFound = true;
				}
				else {
					Sound.beep();
					Sound.beep();
				}
				
			}
		}
		else if(dG < 0.023811+0.013883*2){
			ringDetected = true;
			Lab5.lcd.drawString("Object detected", 0, 4);
			Lab5.lcd.drawString("Green ", 0, 5);
			if(!foundRings[1]) {
				foundRings[1] = true;
				if(target == 1) {
					Sound.beep();
					targetFound = true;
				}
				else {
					Sound.beep();
					Sound.beep();
				}
				
			}
		}
		else{
			ringDetected = false;
			Lab5.lcd.drawString("No object detected", 0, 5);
			Lab5.lcd.drawString("None  ", 0, 6);
		}
	}
	
	public static void resetStatus(){
		targetFound = false;
	}
	
	/**
	 * Normalizes the data
	 * @param R
	 * @param B
	 * @param G
	 * @return
	 */
	private static float[] normalizeRGBData(float R, float B, float G){
		float[] result = new float[3];
		result[0] = R / (float)Math.sqrt(R*R + B*B + G*G);
		result[1] = B / (float)Math.sqrt(R*R + B*B + G*G);
		result[2] = G / (float)Math.sqrt(R*R + B*B + G*G);
		return result;
	}
}
