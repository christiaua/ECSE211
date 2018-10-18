package ca.mcgill.ecse211.lab5;

import lejos.robotics.SampleProvider;

public class ColPoller extends Thread {
  private SampleProvider sample;
  private float[] lightData;

  /**
   * Constructor 
   * @param sample a SampleProvider from which we fetch the samples.
   * @param lightData a float array to store the samples.
   * @param ll a LightLocalizer to which we will pass the data through a synchronized setter.
   */
  public ColPoller(SampleProvider sample, float[] lightData) {
    this.sample = sample;
    this.lightData = lightData;
  }

  public void run() {
    while (true) {
      sample.fetchSample(lightData, 0);
      RingDetector.processRGBData(lightData[0], lightData[1], lightData[2]);
      try {
        Thread.sleep(20);
      } catch (Exception e) {
      } 
    }
  }
  
  public float[] getColorReading(){
	  return this.lightData;
  }
}