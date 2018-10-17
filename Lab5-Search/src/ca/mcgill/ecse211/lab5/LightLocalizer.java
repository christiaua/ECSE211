package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.*;
import lejos.robotics.SampleProvider;

/**
 * This class does the light localization
 * 
 * @author Sophie Deng
 */
public class LightLocalizer {
	private Odometer odometer;
	private Navigation navigation;
	private double[] angleData;
	
	private SampleProvider colourSensor;
	private float[] colourData;
	private float light_Intensity;
	
	private static int ROTATE_SPEED = 90;
	private static double SENSOR_DISTANCE = 3.2;
	

	
	/**
	 * Constructor
	 * @throws OdometerExceptions 
	 */
	public LightLocalizer(SampleProvider cSensor, float[] cData, Navigation nav) throws OdometerExceptions {
		this.odometer = Odometer.getOdometer();
		this.colourSensor = cSensor;
		this.colourData = cData;
		this.navigation = nav;
		this.angleData = new double[4];
	}
	
	/**
	 * Perform localization, robot should be facing north at (0,0)
	 */
	public void doLocalization() {
		collectAngleData();
		correctPosition();
		double currentX = odometer.getXYT()[0];
		double currentY = odometer.getXYT()[1];
		
		double dx = 0 - currentX;
		double dy = 0 - currentY;
		navigation.travelTo(dx, dy);
		navigation.turnTo(0);
	}
	
	/**
	 * correct x and y position
	 */
	private void correctPosition() {
		double deltaY = (angleData[2] - angleData[0]);
		double deltaX = (angleData[3] - angleData[1]);
		
		double xPosition = SENSOR_DISTANCE * Math.cos(Math.PI*deltaX/(2*180));
		double yPosition = SENSOR_DISTANCE * Math.cos(Math.PI*deltaY/(2*180));
		
		odometer.setX(-1 * xPosition);
		odometer.setY(-1 * yPosition);
	}

	/**
	 * collect angles at which the lines are
	 */
	private void collectAngleData() {
		//rotate counterclockwise
		navigation.rotateWheels(-ROTATE_SPEED, ROTATE_SPEED);
		int count = 0;
		while(count != 4) {
			colourSensor.fetchSample(colourData, 0);
			if(colourData[0] < 0.4) {
				Sound.buzz();
				angleData[count] = odometer.getXYT()[2];
				count++;
			}
		}
		navigation.stop();		
	}
//Getter and setters for light intensity
	public void setLightLevel(float newLIntensity) {
		light_Intensity = newLIntensity;
		
	}
	public synchronized float getLightLevel() {
	    return light_Intensity;
	  }
}
