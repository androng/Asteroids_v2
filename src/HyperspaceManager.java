import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TimerTask;


public class HyperspaceManager {

	final static int OUT_AND_IN = 0;
	final static int OUT_ONLY = 1;
	final static int IN_ONLY = 2;
	
	private final int NUMBER_OF_SHIPS = 10; //A high number of ships will cause lag.
	private final int DURATION = NUMBER_OF_SHIPS * 100; //Duration from first ship placement to last ship placement, not including the halfway pause.
	private final int HALFWAY_PAUSE = 2000;
	
	private int nMode;
	private boolean bActive; 
	private boolean bAnimationDone;
	private Spaceship Cruzer;
	private long nInvincibilitySave;
	private Point oldLocation;
	private Point newLocation;
	private double dOldPointDirection;
	private double dNewPointDirection;
	private List<Spaceship> ShipGhosts;
	private ProgressTimer ShipReleaseTimer;
	
	public HyperspaceManager(Spaceship Cruzer){
		bActive = false;
		bAnimationDone = true;
		this.Cruzer = Cruzer;
		ShipGhosts = Collections.synchronizedList(new LinkedList<Spaceship>());
		nInvincibilitySave = 0;
	}
	/** Umbrella method for the actual activate function. Defaults to HyperspaceManager.OUT_AND_IN .  */
	public void activate(){activate(OUT_AND_IN);}
	/**
	 * The method that after construction, enables the hyperspace module on the ship this manager is bound to. 
	 * Generates a random new location and direction. 
	 * 
	 * @param nMode Which parts of the animation to play. Accepts HyperspaceManager.OUT_AND_IN, IN_ONLY, and OUT_ONLY. 
	 * If a part of the animation is skipped, there will be no "halfway pause" and that part of the animation will take the
	 * same amount of time as if it were by itself. (not doubled in duration)
	 */
	public void activate(int nMode){
		/* Generate a new location at least 200 away from the old one */
		oldLocation = new Point(Cruzer.getX(), Cruzer.getY());
		Point newLoc;
		do{
			newLoc = new Point((int) (Asteroids_v2.APPLET_X * (3.0/4) * Math.random() + Asteroids_v2.APPLET_X * (1.0/8)), 
									(int) (Asteroids_v2.APPLET_Y * (3.0/4) * Math.random() + Asteroids_v2.APPLET_Y * (1.0/8)));
		}while(oldLocation.distance(newLoc) < 200);
		
		/* The new point direction will be facing the center with a small randomness factor. */
		double dNewPointDir = 180 + Math.toDegrees(Math.atan2(newLoc.getY() - (Asteroids_v2.APPLET_Y / 2), newLoc.getX() - (Asteroids_v2.APPLET_X / 2))) + (180 * Math.random() - 90);
		
		/* Call the real activate method */
		activate(nMode, dNewPointDir, newLoc, true);
	}
	/**
	 * This is a special activate method for the game to specify exact teleport coordinates and whether the shield is on afterward. 
	 * It is used wherever it is not the player initiating the teleport.
	 * 
	 * @param nMode Which parts of the animation to play. Accepts HyperspaceManager.OUT_AND_IN, IN_ONLY, and OUT_ONLY. 
	 * If a part of the animation is skipped, there will be no "halfway pause" and that part of the animation will take the
	 * same amount of time as if it were by itself. (not doubled in duration)
	 * @param bSaveInvincibility Necessary because of a weird bug in the Reinforcements manager where if running on an OS X Java VM,
	 * the Reinforcements manager will "hyperspace" in two ships at a time, causing two shields to be drawn at once, crashing the VM due to a 
	 * "Invalid memory access of location" error. The reinforcement ships should not have shields anyway. 
	 */
	public void activate(int nMode, double newPointDirection, Point newLocation2, boolean bSaveInvincibility){
		if(nMode != OUT_AND_IN && nMode != OUT_ONLY && nMode != IN_ONLY){
			throw new IllegalArgumentException("Undefined hyperspace mode, defaulting to OUT_AND_IN.");
		} else {
			this.nMode = nMode;
		}
		
		bActive = true;
		bAnimationDone = false;
		
		if(bSaveInvincibility){
			/* Save the invincibility duration to restore later. 
			 * If invincibility wasn't active, set restore time to 1 second. */
			nInvincibilitySave = Cruzer.getShield().getRemainingDuration();
			Cruzer.getShield().deactivate();
			if(nInvincibilitySave == 0)
				nInvincibilitySave = 1000;
		}
			
		/* Generate a new location that is at least 200 away from the original location. */
		oldLocation = new Point(Cruzer.getX(), Cruzer.getY());
		newLocation = newLocation2;
		dOldPointDirection = Cruzer.getPointDirection();
		
		dNewPointDirection = newPointDirection;
		
		/* Schedule the time to release the ship */
		if(nMode == OUT_AND_IN){
			ShipReleaseTimer = new ProgressTimer(new HyperspaceFinishTimerTask(), (long) (DURATION * ((NUMBER_OF_SHIPS - 1.0) / NUMBER_OF_SHIPS) + HALFWAY_PAUSE));
		} else {
			ShipReleaseTimer = new ProgressTimer(new HyperspaceFinishTimerTask(), (long) (DURATION/ 2 * ((NUMBER_OF_SHIPS/ 2 - 1.0) / (NUMBER_OF_SHIPS/ 2) ) ));
		}
		
		/* Schedule the times for the ships to appear*/
		if(nMode == OUT_AND_IN){
			for(int nI = 0; nI < NUMBER_OF_SHIPS / 2; nI++)
				ShipReleaseTimer.schedule(new ShipAddTimerTask(), nI * (DURATION / NUMBER_OF_SHIPS));
			for(int nI = NUMBER_OF_SHIPS / 2; nI < NUMBER_OF_SHIPS; nI++)
				ShipReleaseTimer.schedule(new ShipAddTimerTask(), nI * (DURATION / NUMBER_OF_SHIPS) + HALFWAY_PAUSE);
		} else {
			for(int nI = 0; nI < NUMBER_OF_SHIPS / 2; nI++)
				ShipReleaseTimer.schedule(new ShipAddTimerTask(), nI * ((DURATION/2) / (NUMBER_OF_SHIPS/2) ));
		}
		
		/* Move main ship offscreen for the duration of the hyperspace animation. */
		Cruzer.setX(5000);
		Cruzer.setY(5000);
	}
	private void ReleaseShip(){
		bActive = false;
		Cruzer.setX((int) newLocation.getX());
		Cruzer.setY((int) newLocation.getY());
		Cruzer.setDirectionX(0);
		Cruzer.setDirectionY(0);
		Cruzer.setPointDirection(dNewPointDirection);
		Cruzer.getShield().activate(nInvincibilitySave);
	}
	public boolean isActive(){
		return bActive;
	}
	public boolean animationIsDone(){
		return bAnimationDone;
	}
	public void terminate(){
		if(isActive() == true){
			ReleaseShip();
			Cruzer.setX((int)oldLocation.getX());
			Cruzer.setY((int)oldLocation.getY());
			Cruzer.setPointDirection(dOldPointDirection);
		}
		ShipReleaseTimer.cancel();
		ShipGhosts.removeAll(ShipGhosts);
		bAnimationDone = true;
	}
	class HyperspaceFinishTimerTask extends TimerTask{
		HyperspaceFinishTimerTask(){}
		public void run(){
			ReleaseShip();
		}
	}
	class ShipAddTimerTask extends TimerTask{
		ShipAddTimerTask(){}
		public void run(){
			/* Add the spaceship and add the common attributes. */
			ShipGhosts.add(new Spaceship());
			final int nLast = ShipGhosts.size() - 1;
			ShipGhosts.get(nLast).setColor(Cruzer.getColor());
			ShipGhosts.get(nLast).setDesignNumber(Cruzer.getDesignNumber());
			//ShipGhosts.get(nLast).setModelNumber(Cruzer.getModelNumber());
			
			/* Then add the attributes that are different for each half. */
			if(nMode == OUT_AND_IN){
				if(ShipGhosts.size() <= NUMBER_OF_SHIPS / 2){
					ShipGhosts.get(nLast).setX((int)oldLocation.getX());
					ShipGhosts.get(nLast).setY((int)oldLocation.getY());
					ShipGhosts.get(nLast).setPointDirection(dOldPointDirection);
					ShipGhosts.get(nLast).changeSize(2 * ShipGhosts.size());
				} else if(ShipGhosts.size() <= NUMBER_OF_SHIPS){
					ShipGhosts.get(nLast).setX((int)newLocation.getX());
					ShipGhosts.get(nLast).setY((int)newLocation.getY());
					ShipGhosts.get(nLast).setPointDirection(dNewPointDirection);
					ShipGhosts.get(nLast).changeSize(2 * (1 + NUMBER_OF_SHIPS - ShipGhosts.size()));
				}
			} else if(nMode == OUT_ONLY) {
				ShipGhosts.get(nLast).setX((int)oldLocation.getX());
				ShipGhosts.get(nLast).setY((int)oldLocation.getY());
				ShipGhosts.get(nLast).setPointDirection(dOldPointDirection);
				ShipGhosts.get(nLast).changeSize(2 * ShipGhosts.size());
			} else if(nMode == IN_ONLY) {
				ShipGhosts.get(nLast).setX((int)newLocation.getX());
				ShipGhosts.get(nLast).setY((int)newLocation.getY());
				ShipGhosts.get(nLast).setPointDirection(dNewPointDirection);
				ShipGhosts.get(nLast).changeSize(2 * (NUMBER_OF_SHIPS/2 - ShipGhosts.size()));
			} 
		}
	}
	public void draw(Graphics2D g){
		if(animationIsDone() == false){
			
			synchronized(ShipGhosts){
				ListIterator<Spaceship> itr = ShipGhosts.listIterator();
				while(itr.hasNext()){
					Spaceship shipGhost = itr.next();
					
					shipGhost.draw(g, false);
					/* The amount of time it takes for the ships to fade away depends on the number of total ships. */
					int nNewShipGhostAlpha = Math.max(0, shipGhost.getColor().getAlpha() - (NUMBER_OF_SHIPS / 1));
					if(nNewShipGhostAlpha != shipGhost.getColor().getAlpha()){
						shipGhost.setColor(new Color(shipGhost.getColor().getRed(),
													 shipGhost.getColor().getGreen(),
													 shipGhost.getColor().getBlue(),
													 nNewShipGhostAlpha));
					}
				}
			}
		}
		/* Terminate the animation if the designated number of ships has been reached and
		 * the last one is invisible */
		final int nLast = ShipGhosts.size() - 1;
		if( (ShipGhosts.size() >= NUMBER_OF_SHIPS || (
				nMode != OUT_AND_IN && ShipGhosts.size() >= NUMBER_OF_SHIPS / 2 )) 
			&& ShipGhosts.get(nLast).getColor().getAlpha() <= 0){
			terminate();
		}
	}
	public void moveFromShip(){
		synchronized(ShipGhosts){
			ListIterator<Spaceship> itr = ShipGhosts.listIterator();
			while(itr.hasNext()){
				Spaceship shipGhost = itr.next();
//				shipGhost.moveFrom(Cruzer.getDirectionX(), Cruzer.getDirectionY());
			}
		}
	}
	/**
	 * Sets the new location of the ship currently in hyperspace. This must be called after activate()
	 * and before the ship comes back from hyperspace. 
	 * 
	 * @param newLoc
	 */
//	public void setNewLocation(Point newLoc){newLocation = newLoc;}
//	public void setNewDirection(double dNewDir){dNewPointDirection = dNewDir;}
}
