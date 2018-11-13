package ca.mcgill.ecse211.project;



public class RingSearch {
	
	private static final double TILE_SIZE = 30.48;
	private static final double distance1 = TILE_SIZE/2 ; //to be changed
	private static final double distance2 = TILE_SIZE/2 ; //to be changed
	
	public int sideNb;
	
	private static Navigation navigation;
	
	
	public int[] findRing(int startCorner) {
		int[] ringFound;
		
		int i;
		for(i = 0; i < 4; i++) {
			navigation.moveForward(distance1, false);
			ringFound = detectRings(i);
			if(ringFound != null)
				return ringFound;
			navigation.moveForward(distance2, false);
			//turn 90 degrees left
			navigation.rotate(-90, false);	
		}
		
		return null; //no ring found	
	}
	
	/**
	 * This methods detects rings, given the side of the ring set on which the robot currently is.
	 * When a ring is found, it returns an array containing the ring level number, the ring side number and the ring color number.
	 * If no ring is found the method returns null.
	 * 
	 * @return An array of three elements(ring level number, ring side number and ring color number)
	 */
	public int [] detectRings(int side) {
		int[] ringFound ;
 		
		
		return ringFound;	
	}
	/**
	 * This method makes the robot move to the intersection nearest to the ring set's side specified as a parameter, and makes it face the ring set. .
	 * It is assumed that there is no obstacle between the robot's current position and the target position.
	 * 
	 * @param sideNb The side number of the ring set towards which the robot has to be positioned. 0 is bottom, 1 is right, 2 is top and 3 is left.
	 * @param rs_x   x coordinate of the ring set.
	 * @param rs_y   y coordinate of the ring set.
	 */
	public void faceRing(int sideNb, int rs_x, int rs_y) {
		switch(sideNb) {
		case 0:	//down
				navigation.travelTo( (rs_x) * TILE_SIZE , (rs_y - 1) * TILE_SIZE );
				navigation.turnTo(0);
				break;
			
		case 1:	//right
				navigation.travelTo((rs_x + 1) * TILE_SIZE , rs_y * TILE_SIZE );
				navigation.turnTo(270);
				break;
				
		case 2:	//up
				navigation.travelTo( rs_x * TILE_SIZE , (rs_y + 1) * TILE_SIZE );
				navigation.turnTo(180);
				break;
				
		case 3:	//left
				navigation.travelTo( (rs_x - 1) * TILE_SIZE , rs_y * TILE_SIZE );
				navigation.turnTo(90);
				break;
		}
		
		return;
	}
	
	/**
	 * This method grabs the ring specified by the parameters. It assumes the robot is positioned at the nearest intersection and facing the ring.
	 * @param ringLevel
	 * @param ringNb
	 */
	public void grabRing(int ringLevel, int ringNb) {
		
		
		return;
	}

}
