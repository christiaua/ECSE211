package ca.mcgill.ecse211.lab5;

import lejos.robotics.SampleProvider;

public class ColPoller extends Thread {
  private SampleProvider sample;
  private LightLocalizer ll;
  private float[] lightData;

  /**
   * Constructor 
   * @param sample a SampleProvider from which we fetch the samples.
   * @param lightData a float array to store the samples.
   * @param ll a LightLocalizer to which we will pass the data through a synchronized setter.
   */
  public ColPoller(SampleProvider sample, float[] lightData, LightLocalizer ll) {
    this.sample = sample;
    this.lightData = lightData;
    this.ll = ll;
  }

  public void run() {
    while (true) {
      sample.fetchSample(lightData, 0);
      if (lightData[0] > 0.f) {
        ll.setLightLevel(lightData[0]);
      }
      try {
        Thread.sleep(20);
      } catch (Exception e) {
      } 
    }
  }
}