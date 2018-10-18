package ca.mcgill.ecse211.lab5;

import lejos.robotics.SampleProvider;

/**
 * Control of the wall follower is applied periodically by the UltrasonicPoller thread. The while
 * loop at the bottom executes in a loop. Assuming that the us.fetchSample, and cont.processUSData
 * methods operate in about 20mS, and that the thread sleeps for 50 mS at the end of each loop, then
 * one cycle through the loop is approximately 70 mS. This corresponds to a sampling rate of 1/70mS
 * or about 14 Hz.
 */
public class UltrasonicPoller extends Thread {
  private SampleProvider us;
  private float[] usData;
  public int distance;
  public int[] lastDistance = new int[2];
  public boolean readUS = false;
  public boolean readLS = false;

  public UltrasonicPoller(SampleProvider us, float[] usData) {
    this.us = us;
    this.usData = usData;
  }

  /*
   * Sensors now return floats using a uniform protocol. Need to convert US result to an integer
   * [0,255] (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  public void run() {
	int counter = 0;
    while (true) {
      us.fetchSample(usData, 0); // acquire data
      
      if(counter % 2 == 0){
    	  lastDistance[0] = distance;
      }
      else{
    	  lastDistance[1] = distance;
      }
      distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
      counter++;
      try {
        Thread.sleep(50);
      } catch (Exception e) {
      } // Poor man's timed sampling
    }
  }

}
