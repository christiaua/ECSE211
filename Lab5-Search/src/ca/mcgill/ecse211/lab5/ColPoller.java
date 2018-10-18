package ca.mcgill.ecse211.lab5;

import lejos.robotics.SampleProvider;

public class ColPoller extends Thread {
  private SampleProvider rgbSample;
  private float[] rgbData;
  private SampleProvider redSample;
  private float[] redData;
  private float lastRedReading;
  private float currentRedReading;
  
  
  /**
   * Constructor 
   * @param sample a SampleProvider from which we fetch the samples.
   * @param lightData a float array to store the samples.
   * @param ll a LightLocalizer to which we will pass the data through a synchronized setter.
   */
  public ColPoller(SampleProvider rgbSample, float[] rgbData, SampleProvider redSample, float[] redData) {
    this.rgbSample = rgbSample;
    this.rgbData = rgbData;
    this.redSample = redSample;
    this.redData = redData;
  }

  public void run() {
    while (true) {
    	redSample.fetchSample(redData, 0);
    	
    	lastRedReading = currentRedReading;
    	currentRedReading = redData[0];
    	
      rgbSample.fetchSample(rgbData, 0);
      RingDetector.processRGBData(rgbData[0], rgbData[1], rgbData[2]);
      try {
        Thread.sleep(20);
      } catch (Exception e) {
      } 
    }
  }
  
  public float[] getRGBReading(){
	  return this.rgbData;
  }
  
  public float getCurrentRedReading(){
	  return this.currentRedReading;
  }
  
  public float getLastRedReading(){
	  return this.lastRedReading;
  }
}