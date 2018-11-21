package ca.mcgill.ecse211.project;

/**
 * This is a helper class to contain an X and Y value. It is used by the path
 * finding algorythm to contain X and Y at the same time.
 * 
 * @author Sophie
 *
 */
public class Coordinate {
	double x;
	double y;

	public Coordinate(double xPosition, double yPosition) {
		x = xPosition;
		y = yPosition;
	}
}
