import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.LinkedList;
import java.util.List;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;


public class HomingMissileLauncher extends Floater {
	final static int[] naYUnsplitPoly = new int[] {  0,-10, 10};
	final static int[] naXUnsplitPoly = new int[] { 20,-20,-20};
	final static Polygon pUnsplitShell = new Polygon(naXUnsplitPoly, naYUnsplitPoly, naXUnsplitPoly.length);
	
	final static int[] naYSplitPoly = new int[] {  0,-10,  0};
	final static int[] naXSplitPoly = new int[] { 20,-20,-20};
	final static Polygon pSplitShell = new Polygon(naXSplitPoly, naYSplitPoly, naXSplitPoly.length);
	
	/** Distance that the two pieces of the shell split from each other */
	private float splitDistance;
	private LinkedList<Floater> targetFloaters;
	/** Timer that animates the split of the shell */
	private Animator splitTimer;
	/** For each target, there will be a scope animation that "locks on" as a visual aid to the homing missiles. */
	private Animator scopeRadii;
	/** There are three phases: aiming, launch of the carrier, and the explosion of the carrier when all the missiles are released. 
	 * hasLaunched changes to true between step 1 and 2.  */
	private boolean hasLaunched;
	
	/** References to existing variables */
	List<Floater> GoodGuys;
	List<Floater> BadGuys;
	
	public HomingMissileLauncher(List<Floater> vigilantes, List<Floater> undesirables){
		myHP = 100;
		/* These will all be changed later in the launch function */
		myCenterX = 0;
		myCenterY = 0;
		myPointDirection = 0;
		myDirectionX = 0;
		myDirectionY = 0;
		
		splitDistance = 0;
		targetFloaters = new LinkedList<Floater>();
		
		GoodGuys = vigilantes;
		BadGuys = undesirables;
		
		FindClosestFloaters();
		hasLaunched = false;
		
		splitTimer = new Animator(750, new TimingTarget(){
			public void begin() {
				if(BadGuys.size() > 0){
					
					for(int nNum = 1; nNum < 100; nNum++){
						GoodGuys.add(new HomingMissile(getX() + 10 * Math.random() - 5, getY() + 10 * Math.random() - 5, getPointDirection() - 45 + Math.floor(Math.random() * 2) * 90 + Math.random() * 20, targetFloaters.get((int)(targetFloaters.size() * Math.random())) ));
					}
				}
			}
			public void end() {}
			public void repeat() {}
			public void timingEvent(float fraction) {
				splitDistance = 110 * fraction;
			}
		});
		splitTimer.setStartDelay(500);
		
		scopeRadii = new Animator(500, new TimingTarget(){
			public void begin() {}
			public void end() {}
			public void repeat() {}
			public void timingEvent(float fraction) {
				
			}
		});
		
		scopeRadii.start();
	}
	public void draw(Graphics2D g) {
		/* Draw the shell if launched*/
		if(hasLaunched() && splitDistance == 0){
			g.setColor(Color.blue);
			Polygon translatedPoly = new Polygon(pUnsplitShell.xpoints, pUnsplitShell.ypoints, pUnsplitShell.npoints);
			/* Rotate and translate polygon */
			translatedPoly = Util.rotate(translatedPoly, myPointDirection);
			translatedPoly.translate(getX(), getY());

			g.draw(translatedPoly);
		} else if(splitTimer.isRunning()){
			g.setColor(new Color(0, 0, (int)(255 * (1 - splitTimer.getTimingFraction()))));
			
			/* Rotate and translate first half */
			double nNewMiddleX = myCenterX + splitDistance * Math.cos(Math.toRadians(myPointDirection - 90));
			double nNewMiddleY = myCenterY + splitDistance * Math.sin(Math.toRadians(myPointDirection - 90));
			Polygon translatedPoly = new Polygon(pSplitShell.xpoints, pSplitShell.ypoints, pSplitShell.npoints);
			
			translatedPoly = Util.rotate(translatedPoly, myPointDirection);
			translatedPoly.translate((int)nNewMiddleX, (int)nNewMiddleY);
			g.draw(translatedPoly);
			
			/* Rotate and translate second half */
			nNewMiddleX = myCenterX + splitDistance * Math.cos(Math.toRadians(myPointDirection + 90));
			nNewMiddleY = myCenterY + splitDistance * Math.sin(Math.toRadians(myPointDirection + 90));
			translatedPoly = new Polygon(pSplitShell.xpoints, pSplitShell.ypoints, pSplitShell.npoints);
			
			AffineTransform afTransform =  new AffineTransform();
			afTransform.translate(nNewMiddleX, nNewMiddleY);
			afTransform.rotate(Math.toRadians(myPointDirection));
			afTransform.scale(1, -1);
			g.draw(afTransform.createTransformedShape(translatedPoly));
		}
		
		/* Draw the scopes */
		for(Floater curFloater: targetFloaters){
			float nRadius = (4 - 3 * scopeRadii.getTimingFraction()) * (float)(10 + curFloater.getSize());
			float nOuterRadius = nRadius + 10;
			
			g.setColor(Color.red);
			if(curFloater.shouldBeRemoved()){
				g.setColor(new Color(128,0,0));
			}
			g.drawLine((int)(curFloater.getX() - nRadius * 1.5), curFloater.getY(), (int)(curFloater.getX() + nRadius * 1.5), curFloater.getY());
			g.drawLine(curFloater.getX(), (int)(curFloater.getY() - nRadius * 1.5), curFloater.getX(), (int)(curFloater.getY() + nRadius * 1.5));
			g.draw(new Ellipse2D.Float(curFloater.getX() - nRadius, curFloater.getY() - nRadius, nRadius * 2, nRadius * 2));
			for(int nShift = 0; nShift <= 3; nShift++){
				g.drawArc((int)(curFloater.getX() - nOuterRadius), (int)(curFloater.getY() - nOuterRadius), (int)(nOuterRadius * 2), (int)(nOuterRadius * 2), (int)(Util.modulus(360, 1000) + nShift * 120), 30);
			}
		}
	}
	public void launch(double dStartX, double dStartY, double dDirection){
		splitTimer.start();
		hasLaunched = true;
		myCenterX = dStartX;
		myCenterY = dStartY;
		myPointDirection = dDirection;
		myDirectionX = 10 * Math.cos(Math.toRadians(myPointDirection));
		myDirectionY = 10 * Math.sin(Math.toRadians(myPointDirection));
		
	}
	public void FindClosestFloaters(){
		synchronized(targetFloaters){
			/* Clear the list of targeted Floaters*/
			targetFloaters.removeAll(targetFloaters);
	
			/* Look for the closest floaters */
			Floater origin = new Bullet(Asteroids_v2.getMouseX(), Asteroids_v2.getMouseY(), 0);
			for(int nTargets = 1; nTargets < 5; nTargets++){
				/** The floater closest the the centerFloater. Start off with nothing, and loop through the floater list for any non-targeted floaters. 
				 * If one is found, then compare it to any others that are not being targeted. */
				Floater ClosestFloater = null;
				
				/* Find a floater to start with. This cannot be integrated into the second loop as an if/elseif because if the current Floater does not 
				 * match the criteria for being compared, the if statement will move to the else/if part and compare a null reference. */
				for(Floater curFloater: BadGuys){
					/* If floater is NOT already being targeted, then set floater as the one to compare with and stop looking. */
					if(targetFloaters.contains(curFloater) == false){
						ClosestFloater = curFloater;
						break;
					}
				}
				/* Find a floater closer to the centerFloater than ClosestFloater */
				if(ClosestFloater != null){
					synchronized(BadGuys){
						for(Floater curFloater: BadGuys){
							/* If floater is NOT already being targeted AND floater is closer than ClosestFloater */
							if(targetFloaters.contains(curFloater) == false && origin.distance(curFloater) < origin.distance(ClosestFloater) ){
								ClosestFloater = curFloater;
							}
						}
					}
					targetFloaters.add(ClosestFloater);
				} else {
					break;
				}
			}
		}
	}
	public Shape getShape() {
		return null;
	}
	public boolean shouldBeRemoved() {
		if(splitDistance > 0){
			/* Either all the missiles are gone, 
			 * or all the targets are dead. */
			boolean allMissilesGone = true;
			boolean allFloatersDead = true;
			
			for(Floater curFloater: GoodGuys){
				if(curFloater instanceof HomingMissile){
					allMissilesGone = false;
				}
			}
			
			for(Floater curFloater: targetFloaters){
				if(curFloater.shouldBeRemoved() == false){
					allFloatersDead = false;
				}
			}
			return allMissilesGone || allFloatersDead;
		}
		return false;
	}
	public boolean hasLaunched(){
		return hasLaunched;
	}
}
