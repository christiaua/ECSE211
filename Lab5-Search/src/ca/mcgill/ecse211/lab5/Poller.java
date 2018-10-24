package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.robotics.SampleProvider;
import java.util.Arrays;

/**
 * This class gets the samples for the light sensors
 * 
 * @author Edward Huang
 * @author Hugo Parent-Pothier
 */
public class Poller extends Thread {
  private final int SAMPLE_SIZE = 5;

  private SampleProvider rgbSample;
  private float[] rgbData;
  private SampleProvider rgbSample2;
  private float[] rgbData2;
  private SampleProvider redSample;
  private float[] redData;
  private float lastRedReading;
  private float currentRedReading;
  private int target;
  private SampleProvider us;
  private float[] usData;
  public int distance;
  private int unfilteredDistance;
  public int lastDistance;
  private int[] lastDistances = new int[SAMPLE_SIZE];


  /**
   * Constructor
   * 
   * @param sample a SampleProvider from which we fetch the samples.
   * @param lightData a float array to store the samples.
   * @param ll a LightLocalizer to which we will pass the data through a synchronized setter.
   */
  public Poller(SampleProvider rgbSample, float[] rgbData, SampleProvider rgbSample2,
      float[] rgbData2, SampleProvider redSample, float[] redData, int TR, SampleProvider us,
      float[] usData) {
    this.rgbSample = rgbSample;
    this.rgbData = rgbData;
    this.rgbSample2 = rgbSample2;
    this.rgbData2 = rgbData2;
    this.redSample = redSample;
    this.redData = redData;
    this.target = TR;
    this.us = us;
    this.usData = usData;
  }

  /**
   * Get the sample from both light sensors and processes the data
   */
  public void run() {
    int counter = 0;
    while (true) {
      redSample.fetchSample(redData, 0);

      lastRedReading = currentRedReading;
      currentRedReading = redData[0];

      rgbSample.fetchSample(rgbData, 0);
      rgbSample2.fetchSample(rgbData2, 0);

      try {
        RingDetector.processRGBData(rgbData[0], rgbData[1], rgbData[2], rgbData2[0], rgbData2[1],
            rgbData2[2], target);
      } catch (OdometerExceptions e1) {
        e1.printStackTrace();
      }

      us.fetchSample(usData, 0); // acquire data

      unfilteredDistance = (int) (usData[0] * 100.0); // extract from buffer, cast to int

      lastDistances[counter] = unfilteredDistance;
      if (counter == SAMPLE_SIZE - 1) {
        Arrays.sort(lastDistances);
        lastDistance = distance;
        distance = lastDistances[2];
      }
      counter++;
      counter = counter % SAMPLE_SIZE;

      try {
        Thread.sleep(20);
      } catch (Exception e) {
      }
    }
  }

  /**
   * Get R, G and B values
   * 
   * @return rgb data
   */
  public float[] getRGBReading() {
    return this.rgbData;
  }

  /**
   * Get the Red value
   * 
   * @return red value
   */
  public float getCurrentRedReading() {
    return this.currentRedReading;
  }

  /**
   * Get previous Red value
   * 
   * @return get previous red value
   */
  public float getLastRedReading() {
    return this.lastRedReading;
  }
}
