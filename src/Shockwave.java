import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;


public class Shockwave extends Floater {
	final static float FINAL_RADIUS = 1500;
	
	private float nRadius;
	private boolean bHasCollapsed;
	private Animator expandTimer;
	private Animator contractTimer;
	
	private Spaceship centerShip;
	
	public Shockwave(Spaceship centerShip2){
		centerShip = centerShip2;
		myCenterX = centerShip.getX();
		myCenterY = centerShip.getY();
		
		nRadius = 0;
		bHasCollapsed = false;
		
		expandTimer = new Animator(2000, new TimingTarget(){
			public void begin() {}
			public void end() {
				contractTimer.start();
			}
			public void repeat() {}
			public void timingEvent(float fraction) {
				myCenterX = centerShip.getX();
				myCenterY = centerShip.getY();
				
				nRadius = fraction * FINAL_RADIUS;
			}
		});
		expandTimer.setDeceleration(1.0f);
		expandTimer.start();
		
		contractTimer = new Animator(500, new TimingTarget(){
			public void begin() {}
			public void end() {
				bHasCollapsed = true;
			}
			public void repeat() {}
			public void timingEvent(float fraction) {
				myCenterX = centerShip.getX();
				myCenterY = centerShip.getY();
				
				nRadius = (1 - fraction) * FINAL_RADIUS;
			}
		});
	}
	public void draw(Graphics2D g) {
		g.setColor(Color.cyan);
//		g.draw(new Ellipse2D.Float((float)myCenterX - nRadius, (float)myCenterY - nRadius, nRadius * 2, nRadius * 2));
		
		for(int nMultiple = 0; nMultiple < 6; nMultiple++){
			for(float fraction = 1.0f; fraction >= 0; fraction -= .005f){
				double dDirection = 1;
				/* Odds will spin the other way */
				if(nMultiple % 2 != 0)
					dDirection = -1;
				
				g.drawArc((int)(myCenterX - (fraction * nRadius)), (int)(myCenterY - (fraction * nRadius)), (int)(fraction * nRadius) * 2, (int)(fraction * nRadius) * 2, 
						  (int)(nMultiple * 120 +  Util.modulus(dDirection *360, 2000)), (int)(dDirection* fraction /2 * 45));
			}
		}
	}
	public Shape getShape() {
		return (Shape)new Ellipse2D.Float((float)myCenterX - nRadius, (float)myCenterY - nRadius, nRadius * 2, nRadius * 2);
	}
	public boolean shouldBeRemoved() {
		return bHasCollapsed;
	}
}
