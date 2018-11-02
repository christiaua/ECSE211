package test;

import lejos.robotics.SampleProvider;

/**
 * This class is used for sampling sensors and outputting the data onto the console
 * 
 * @author Edward Huang
 */
public class Poller{
	private SampleProvider sample;
	private float[] data;
	
/**
 * This object polls a sensor
 * @param sample poller fetches sample from this
 * @param data poller stores data in this array
 * @param mode Defines which type of sensor to poll. Valid inputs are "us" for ultrasonic sensor, "ls" for light sensor
 */
	public Poller(SampleProvider sample, float[] data) {
		this.sample = sample;
		this.data = data;
	}

	public float[] fetchSample(int numOfSamplesToFetch){
		float[] samples = new float[numOfSamplesToFetch];
		for(int i = 0; i < numOfSamplesToFetch; i++){
			this.sample.fetchSample(this.data, 0);
			samples[i] = this.data[0];
			try {
	            Thread.sleep(25);
	          } catch (Exception e) {
	          }
		}
		return samples;
	}
}