import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Timer;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;


public class ChainLightningManager implements ActionListener {
	private boolean bActive;
	/** Timer that controls the rate of jumping */
	private Timer jumpTimer;
	/** How long each floater is shocked */
	private final int nShockDuration;
	/** Max number of nodes the lightning can have at once. */
	private final int nSimultaneousJumps;
	/** Number of floaters to destroy before the lightning completely leaves the ship. */
	private int nDetachJumpCount;
	/** List of floaters currently being affected. */
	private List<Floater> electrocutedFloaters;
	
	/** Reference to list of existing floaters */
	private List<Floater> floatersToDestroy;
	/** Reference to initial floater */
	private Floater origin;
	
	ChainLightningManager(Floater origin){
		bActive = false;
		nShockDuration = 250;
		nSimultaneousJumps = 5;
		jumpTimer = new Timer(nShockDuration / nSimultaneousJumps, this);
		jumpTimer.setCoalesce(false); /* This line is necessary so that after thousands of asteroids, the chain lightning does not lag. */
		nDetachJumpCount = nSimultaneousJumps;
		electrocutedFloaters = Collections.synchronizedList(new LinkedList<Floater>());
		
		this.origin = origin;
	}
	public void activate(List<Floater> canCollideWithShip){
		if(canCollideWithShip.size() > 0){
			bActive = true;
			jumpTimer.start(); 
			this.floatersToDestroy = canCollideWithShip;
				
			/* Add the first floater to electrocute */
			actionPerformed(null);
		}
		
	}
	public void deactivate(){
		if(bActive){
			bActive = false;
			jumpTimer.stop();
			nDetachJumpCount = nSimultaneousJumps;
			electrocutedFloaters.removeAll(electrocutedFloaters);
		}
	}
	public void draw(Graphics2D g){
		if(bActive){
			/* Remove dead floaters */
			removeDeadFloaters();
			
			if(electrocutedFloaters.size() > 0){
				/* If detach count is positive, draw lightning to first floater */
				if(nDetachJumpCount > 0){
					drawLightning(g, origin, electrocutedFloaters.get(0));
				}
					
				synchronized(electrocutedFloaters){
					/* Draw lightning between the current electrocutedFloaters */
					for(int nFloater = 0; nFloater < electrocutedFloaters.size(); nFloater++){
						/* Break if this is the last floater */
						if(nFloater + 1 == electrocutedFloaters.size()){
							break;
						}
						
						drawLightning(g, electrocutedFloaters.get(nFloater), electrocutedFloaters.get(nFloater + 1));
					}
				}
			}
		}
	}
	private void drawLightning(Graphics2D g, Floater floater1, Floater floater2){
//		g.setColor(Color.orange);
//		g.drawLine(floater1.getX(), floater1.getY(), floater2.getX(), floater2.getY());
		
		final int nLightningSegmentLength = 15;
		final int nLightningDeviateAngleDegrees = 90; 
		final double nLightningDeviateAngleRadians = nLightningDeviateAngleDegrees * (Math.PI/180);
		final int nBolts = nLightningDeviateAngleDegrees/20;
		
		int nStartX = floater1.getX(); 
		int nStartY = floater1.getY(); 
		int nMiddleX; 
		int nMiddleY; 
		int nEndX = floater2.getX(); 
		int nEndY = floater2.getY();
		
		if(floater1 instanceof Spaceship){
			nStartX = (int)((Spaceship) floater1).getCannonPoint().x;
			nStartY = (int)((Spaceship) floater1).getCannonPoint().y;
		}
		
		for(int nNum = 1; nNum <= nBolts; nNum++){
			nMiddleX = nStartX;
			nMiddleY = nStartY;
			g.setColor(new Color(0,(int)(Math.random()*155+100), 255));
			while(Math.hypot(nMiddleX-nEndX, nMiddleY-nEndY) > nLightningSegmentLength){
				double dTheta = Math.atan2(nEndY-nMiddleY, nEndX-nMiddleX) + (Math.random()*nLightningDeviateAngleRadians-(nLightningDeviateAngleRadians/2.0));
				int nNewMiddleX = (int)(nLightningSegmentLength*Math.cos(dTheta)+nMiddleX);
				int nNewMiddleY = (int)(nLightningSegmentLength*Math.sin(dTheta)+nMiddleY);
				g.drawLine(nMiddleX, nMiddleY, nNewMiddleX, nNewMiddleY);
				nMiddleX = nNewMiddleX;
				nMiddleY = nNewMiddleY;
			}
			//The last line
			g.drawLine(nMiddleX, nMiddleY, nEndX, nEndY);
		}
	}
	private void removeDeadFloaters(){
		synchronized(electrocutedFloaters){
			ListIterator<Floater> iterator = electrocutedFloaters.listIterator();
			while(iterator.hasNext()){
				Floater curFloater = iterator.next();
				
				if(curFloater.shouldBeRemoved()){
					iterator.remove();
				}
			}
		}
	}
	public void actionPerformed(ActionEvent arg0) {
		final Floater newFloater = FindClosestFloater();
		
		if(newFloater != null && electrocutedFloaters.size() <= nSimultaneousJumps){
			/* Add a floater to electrocute */
			electrocutedFloaters.add(newFloater);
			nDetachJumpCount--;
			
			/* Set the timing event and timer for this floater to die. 
			 * (and deactivate the weapon if it is the last one)*/
			Animator floaterDeathTimer = new Animator(nShockDuration, new TimingTarget(){
				public void begin() {}
				public void end() {
					newFloater.setHP(0);
					if(floatersToDestroy.size() <= 1 && newFloater.getSize() <= 30)
						deactivate();
				}
				public void repeat() {}
				public void timingEvent(float fraction) {
					newFloater.setColor(new Color((int)(fraction * 50), (int)(fraction * 155) + 100, (int)(fraction * 155) + 100));
				}
			});
//			Timer floaterDeathTimer = new Timer(nShockDuration, new ActionListener(){
//				public void actionPerformed(ActionEvent e) {
//					newFloater.setHP(0);
//				}
//			});
			floaterDeathTimer.start();
		}
	}
	public boolean isActive(){return bActive;}
	
	private Floater FindClosestFloater(){
		/** The floater that we try to find the closest floater to */
		Floater centerFloater = origin;
		
		/* If there is currently a number of electrocutedFloaters, use the last one as the center. */
		if(electrocutedFloaters.size() > 0){
			centerFloater = electrocutedFloaters.get(electrocutedFloaters.size() - 1);
		} 
		
		/** The floater closest the the centerFloater. Start off with nothing, and loop through the floater list for any non-electrocuted floaters. 
		 * If one is found, then compare it to any others that are not being electrocuted. */
		Floater ClosestFloater = null;
		
		/* Find a floater to start with. This cannot be integrated into the second loop as an if/elseif because if the current Floater does not 
		 * match the criteria for being compared, the if statement will move to the else/if part and compare a null reference. */
		synchronized(floatersToDestroy){
			for(Floater curFloater: floatersToDestroy){
				/* If floater is NOT already being electrocuted, then set floater as the one to compare with and stop looking. */
				if(electrocutedFloaters.contains(curFloater) == false){
					ClosestFloater = curFloater;
					break;
				}
			}
		}
		/* Find a floater closer to the centerFloater than ClosestFloater */
		if(ClosestFloater != null){
			synchronized(floatersToDestroy){
				for(Floater curFloater: floatersToDestroy){
					/* If floater is NOT already being electrocuted AND floater is closer than ClosestFloater */
					if(electrocutedFloaters.contains(curFloater) == false && centerFloater.distance(curFloater) < centerFloater.distance(ClosestFloater) ){
						ClosestFloater = curFloater;
					}
				}
			}
			
		}
		return ClosestFloater;
	}
}
