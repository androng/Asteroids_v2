import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Line2D;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;


public class Pulsar_wave extends Floater implements TimingTarget{
	final int nTHICKNESS = 50;
	
	/** Half the length of the wave */
	double dLength;
	
	/** The animator used to change the width. Using an animator is much smoother and
	 * the acceleration is nice compared to when I tried to come up with a mathematical
	 * interpolation of my own. */
	Animator lengthAnim;
	
	public Pulsar_wave(double dStartX, double dStartY, double dDirection){
		myHP = 100;
		myCenterX = dStartX;
		myCenterY = dStartY;
		myPointDirection = dDirection;
		myDirectionX = Math.cos(Math.toRadians(myPointDirection))*10;
		myDirectionY = Math.sin(Math.toRadians(myPointDirection))*10;
		dLength = 8;
		/* Create an animator that increases length for 3 seconds with an increasing rate. */
		lengthAnim = new Animator(4500, this);
		lengthAnim.setAcceleration(1.0f);
		lengthAnim.start();
	}
	public void draw(Graphics2D g){
		for(int nLength = (int) (dLength); nLength > dLength - nTHICKNESS; nLength--){
			if(nLength <= 0){
				break;
			}
			
			/* Blue to black, longest being blue */
			g.setColor(new Color(0,0,(int) (255 * (1 - (dLength - nLength) / nTHICKNESS))));
			//g.setColor(Color.white);
			
			double dRAD_ANGLE = Math.toRadians(myPointDirection);
			/* Calculate the middle of this line*/
			double nMiddleX = myCenterX + Math.cos(dRAD_ANGLE + Math.PI) * (dLength - nLength);
			double nMiddleY = myCenterY + Math.sin(dRAD_ANGLE + Math.PI) * (dLength - nLength);
			g.drawLine((int)Math.round(nMiddleX + Math.cos(dRAD_ANGLE + Math.PI/2) * nLength), (int)Math.round(nMiddleY + Math.sin(dRAD_ANGLE + Math.PI/2) * nLength), 
					   (int)Math.round(nMiddleX + Math.cos(dRAD_ANGLE - Math.PI/2) * nLength), (int)Math.round(nMiddleY + Math.sin(dRAD_ANGLE - Math.PI/2) * nLength));
			
		}
	}
	public Shape getShape() {
		/* The actual size of the area in which floaters will be affected will be larger than the visible wave */
		
		final double DIRECTION_RAD = Math.toRadians(myPointDirection);
		/* In the form "X + (r * cos(direction - 90°))"
		 * 			   "Y + (r * sin(direction + 90°))" */
		Polygon collisionArea = new Polygon();
		collisionArea.addPoint((int)(myCenterX + (dLength * Math.cos(DIRECTION_RAD - (Math.PI / 2)))), (int)(myCenterY + (dLength * Math.sin(DIRECTION_RAD - (Math.PI / 2)))));
		collisionArea.addPoint((int)(myCenterX + (dLength * Math.cos(DIRECTION_RAD + (Math.PI / 2)))), (int)(myCenterY + (dLength * Math.sin(DIRECTION_RAD + (Math.PI / 2)))));
		/* Calculate the middle of the smaller, trailing line (other side of the trapezoid */
		double nLength = Math.max(0, dLength - 100);
		double nMiddleX = myCenterX + Math.cos(DIRECTION_RAD + Math.PI) * (dLength - nLength);
		double nMiddleY = myCenterY + Math.sin(DIRECTION_RAD + Math.PI) * (dLength - nLength);
		collisionArea.addPoint((int)Math.round(nMiddleX + Math.cos(DIRECTION_RAD + Math.PI/2) * nLength), (int)Math.round(nMiddleY + Math.sin(DIRECTION_RAD + Math.PI/2) * nLength)); 
		collisionArea.addPoint((int)Math.round(nMiddleX + Math.cos(DIRECTION_RAD - Math.PI/2) * nLength), (int)Math.round(nMiddleY + Math.sin(DIRECTION_RAD - Math.PI/2) * nLength));
		
		return collisionArea;
	}

	public boolean shouldBeRemoved() {
		/* Remove if distance from center is greater than 1000. */
		return (Math.hypot(getX() - (Asteroids_v2.APPLET_X / 2), getY() - (Asteroids_v2.APPLET_Y / 2)) > 1000);
	}
	public void begin() {}
	public void end() {}
	public void repeat() {}
	public void timingEvent(float fraction) {
		dLength = fraction * 3000;
	}
	public Animator getAnimator(){return lengthAnim;}
	public double getSize(){return dLength;}
}
