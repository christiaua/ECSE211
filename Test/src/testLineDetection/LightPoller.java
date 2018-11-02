package testLineDetection;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

/**
 * Control of the wall follower is applied periodically by the UltrasonicPoller thread. The while
 * loop at the bottom executes in a loop. Assuming that the us.fetchSample, and cont.processUSData
 * methods operate in about 20mS, and that the thread sleeps for 50 mS at the end of each loop, then
 * one cycle through the loop is approximately 70 mS. This corresponds to a sampling rate of 1/70mS
 * or about 14 Hz.
 */
public class LightPoller extends Thread {
  private SampleProvider ls;
  public float[] lsData;
  public double lightReading;
  public double lastLightReading;

  public LightPoller(SampleProvider ls, float[] lsData) {
    this.ls = ls;
    this.lsData = lsData;
  }

  /*
   * Sensors now return floats using a uniform protocol. Need to convert US result to an integer
   * [0,255] (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  public void run() {
	while (true){
        while (true) {
          ls.fetchSample(lsData, 0); // acquire data
          
          lastLightReading = lightReading;

          lightReading = lsData[0];
          
          if(lastLightReading - lightReading > 0.1) {
				Sound.beep();
			}
          try {
            Thread.sleep(50);
          } catch (Exception e) {
          } // Poor man's timed sampling
        }
    }
  }

}
