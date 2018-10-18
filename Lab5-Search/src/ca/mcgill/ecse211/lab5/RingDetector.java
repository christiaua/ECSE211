package ca.mcgill.ecse211.lab5;

import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;

public class RingDetector {
	
	private static final double[] Y_RGB_MEAN = {0.849, 0.503, 0.160};
	private static final double[] B_RGB_MEAN = {0.158, 0.706, 0.645};
	private static final double[] O_RGB_MEAN = {0.967, 0.237, 0.092};
	private static final double[] G_RGB_MEAN = {0.443, 0.874, 0.202};
	
	public static void processRGBData(float R, float B, float G){
		float dY, dB, dO, dG;
		float[] data = normalizeRGBData(R, B, G);
		
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

		if(dY < 0.020744+0.010672*2){
			Lab5.lcd.drawString("Object detected", 0, 4);
			Lab5.lcd.drawString("Yellow", 0, 5);
		}
		else if(dB < 0.032986+0.021538*2){
			Lab5.lcd.drawString("Object detected", 0, 4);
			Lab5.lcd.drawString("Blue  ", 0, 5);
		}
		else if(dO < 0.011160+0.006943*2){
			Lab5.lcd.drawString("Object detected", 0, 4);
			Lab5.lcd.drawString("Orange", 0, 5);
		}
		else if(dG < 0.023811+0.013883*2){
			Lab5.lcd.drawString("Object detected", 0, 4);
			Lab5.lcd.drawString("Green ", 0, 5);
		}
		else{
			Lab5.lcd.drawString("No object detected", 0, 4);
			Lab5.lcd.drawString("None  ", 0, 5);
		}
	}
	
	private static float[] normalizeRGBData(float R, float B, float G){
		float[] result = new float[3];
		result[0] = R / (float)Math.sqrt(R*R + B*B + G*G);
		result[1] = B / (float)Math.sqrt(R*R + B*B + G*G);
		result[2] = G / (float)Math.sqrt(R*R + B*B + G*G);
		return result;
	}
}
