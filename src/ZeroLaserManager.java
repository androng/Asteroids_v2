import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;


public class ZeroLaserManager {
	
	
	private boolean bActive;
	private boolean bLaserActive;
	/** The width of the laser will be proportional to the hypotenuse of the screen. That means that the actual
	 * width will be dynamic.  */
	private float dLaserWidthFraction;
	private Ellipse2D.Double rawEllipse;
	private Shape rotatedEllipse;
	/* Timers and animators */
	private Animator explosionTimer;
	private Animator growthTimer;
	private Animator contractTimer;
	
	/* References to existing variables */
	private Spaceship centerShip;
	private HUD Interface;
	private List<Floater> BadGuys;
	private HashMap<String, Boolean> imageEffects;
	
	public ZeroLaserManager(Spaceship centerShip2){
		centerShip = centerShip2;
		
		bActive = false;
		bLaserActive = false;
		rawEllipse = new Ellipse2D.Double(0, 0, 0, 0);
		
		explosionTimer = new Animator(300, new TimingTarget(){
			public void begin() {
				bLaserActive = true;
				imageEffects.put("Rumble", new Boolean(true));
				
				centerShip.setDirectionX(0);
				centerShip.setDirectionY(0);
			}
			public void end() {
				growthTimer.start();
			}
			public void repeat() {}
			public void timingEvent(float fraction) {
				dLaserWidthFraction = fraction;
				
				/* Calculate a point for the ship to be "pushed back" to. */
				double dDistance = Util.rayDistanceToRectangle(new Point2D.Double(centerShip.getX(), centerShip.getY()), Math.toRadians(centerShip.getPointDirection() - 180), Asteroids_v2.APPLET_X, Asteroids_v2.APPLET_Y);;

				if(dDistance > 50){
					centerShip.setX((int) (centerShip.getX() + .5 * dDistance * Math.cos(Math.toRadians(centerShip.getPointDirection() - 180))));
					centerShip.setY((int) (centerShip.getY() + .5 * dDistance * Math.sin(Math.toRadians(centerShip.getPointDirection() - 180))));
				}
			}
		});
		explosionTimer.setAcceleration(1.0f);
		
		growthTimer = new Animator(5000, new TimingTarget(){
			public void begin() {
				imageEffects.put("Big Rumble", new Boolean(true));
			}
			public void end() {
				contractTimer.start();
				imageEffects.put("Big Rumble", new Boolean(false));
			}
			public void repeat() {}
			public void timingEvent(float fraction) {
				dLaserWidthFraction = 1.0f + .25f * fraction;
				
				/* Damage all the floaters that come in contact with the zero laser.
				 * Uses "cheap" detection, only checks if the center is in the ellipse unless the floater is huge. */
//				for(Floater floater:BadGuys){
//					floater.setColor(Color.black);
//				}
				/* Calculate the center of attraction for floaters that are not in the beam */
				double DISTANCE_TO_INTERSECTION = Util.rayDistanceToRectangle(centerShip.getCannonPoint(), Math.toRadians(centerShip.getPointDirection()), Asteroids_v2.APPLET_X, Asteroids_v2.APPLET_Y);
				
				/* Calculate the center of attraction for floaters that are not in the beam.
				 * Use half the distance from the point of intersection to the ship for the distance from
				 * the ship to the center of attraction. */
				Point2D.Double centerOfAttraction = new Point2D.Double();
				centerOfAttraction.x = centerShip.getCannonPoint().getX() + DISTANCE_TO_INTERSECTION / 2 * Math.cos(Math.toRadians(centerShip.getPointDirection()));
				centerOfAttraction.y = centerShip.getCannonPoint().getY() + DISTANCE_TO_INTERSECTION / 2 * Math.sin(Math.toRadians(centerShip.getPointDirection()));
				
				final double SKINNY_ELLIPSE_HEIGHT = dLaserWidthFraction * rawEllipse.getWidth() / 20;
				Ellipse2D.Double skinnyRawEllipse = new Ellipse2D.Double(0, -SKINNY_ELLIPSE_HEIGHT / 2, rawEllipse.getWidth(), SKINNY_ELLIPSE_HEIGHT);
				AffineTransform afTransform = AffineTransform.getTranslateInstance(centerShip.getCannonPoint().x, centerShip.getCannonPoint().y);
				afTransform.rotate(Math.toRadians(centerShip.getPointDirection()));
				Shape skinnyRotatedEllipse = afTransform.createTransformedShape(skinnyRawEllipse);
				
				/** Actually move the floaters now */
				synchronized(BadGuys){
					for(Floater floater:BadGuys){
						if(skinnyRotatedEllipse.contains(new Point2D.Double(floater.getX(), floater.getY())) || 
						  (floater.getSize() > 70 && Util.shapeIntersect(skinnyRotatedEllipse, floater.getShape()))){
							floater.setHP(floater.getHP() - floater.getSize() / 20.0);
							floater.setDirectionX(floater.getDirectionX() * .9);
							floater.setDirectionY(floater.getDirectionY() * .9);
							
						} else {
							/* Attract the floater toward the beam */
							double moveAngle = (Math.atan2(centerOfAttraction.y - floater.getY(), centerOfAttraction.x - floater.getX()));
							floater.setDirectionX(floater.getDirectionX() + .15 * Math.cos(moveAngle));
							floater.setDirectionY(floater.getDirectionY() + .15 * Math.sin(moveAngle));
							
						}
					}
				}
				
				/** Only shift the ship angle by a certain amount because it needs to turn slowly. 
				 * Compare the ship angle and the new mouse angle to see which direction to shift the ship angle. */
				 
				/* The ship angle measure needs to be modified because it is in the range 0°...360° 
				 * while the mouse point direction is in the range -180°...180° */
				double dMouseAngleDegrees = Math.toDegrees(Math.atan2(Asteroids_v2.getMouseY() - centerShip.getY(), Asteroids_v2.getMouseX() - centerShip.getX()));
				double dShipDirection = centerShip.getPointDirection();
				dShipDirection %= 360;
				if(dShipDirection > 180){
					dShipDirection -= 360;
				} else if (dShipDirection < -180){
					dShipDirection += 360;
				}
				
				final float ROTATION = .2f;
				if(Math.abs(dMouseAngleDegrees - dShipDirection) > 2){
					/* If the absolute value difference between the angles is greater than 180°, 
					 * that means the ship angle should be shifted the other way. (not the sign of the difference)
					 * There is probably a better way to do this, but I couldn't come up with it. */
					if(dMouseAngleDegrees - dShipDirection > 180){
						centerShip.setPointDirection(centerShip.getPointDirection() - ROTATION);
					} else if(dMouseAngleDegrees - dShipDirection < -180){
						centerShip.setPointDirection(centerShip.getPointDirection() + ROTATION);
					} else {
						centerShip.setPointDirection(centerShip.getPointDirection() + ROTATION * Math.signum(dMouseAngleDegrees - dShipDirection));
					}
				}
			}
		});
		
		contractTimer = new Animator(500, new TimingTarget(){
			public void begin() {}
			public void end() {
				bLaserActive = false;
				imageEffects.put("Rumble", new Boolean(false));
				deactivate();
			}
			public void repeat() {}
			public void timingEvent(float fraction) {
				dLaserWidthFraction = 1.5f * (1.0f - fraction);
				
				/* Destroy all floaters in the blast area */
				synchronized(BadGuys){
					for(Floater floater:BadGuys){
						if(rotatedEllipse.contains(new Point2D.Double(floater.getX(), floater.getY())) || 
						  (floater.getSize() > 70 && Util.shapeIntersect(rotatedEllipse, floater.getShape()))){
							floater.setHP(0);
						} 
					}
				}
			}
		});
	}
	
	public boolean isActive(){
		return bActive;
	}
	public boolean isLaserActive(){
		return bLaserActive;
	}
	public void activate(HUD Interface2, List<Floater> BadGuys2, HashMap imageEffects2){
		bActive = true;
		bLaserActive = false;
		imageEffects = imageEffects2;
		
		Interface = Interface2; 
		BadGuys = BadGuys2;
		dLaserWidthFraction = 0;
		
		explosionTimer.start();
	}
	public void deactivate(){
		bActive = false;
	}
	public void draw(Graphics2D g){
		if(bActive){
			final double ELLIPSE_WIDTH = 2 * Math.hypot(Asteroids_v2.APPLET_X, Asteroids_v2.APPLET_Y);
			final double ELLIPSE_HEIGHT = dLaserWidthFraction * ELLIPSE_WIDTH / 6;
			rawEllipse.setFrame(0, -ELLIPSE_HEIGHT / 2, ELLIPSE_WIDTH, ELLIPSE_HEIGHT);
				
			AffineTransform afTransform = AffineTransform.getTranslateInstance(centerShip.getCannonPoint().x, centerShip.getCannonPoint().y);
			afTransform.rotate(Math.toRadians(centerShip.getPointDirection()));
			rotatedEllipse = afTransform.createTransformedShape(rawEllipse);

			for(int nShrink = 3; nShrink <= 60; nShrink += 10){
				final double SKINNY_ELLIPSE_HEIGHT = ELLIPSE_HEIGHT - nShrink;
				Ellipse2D.Double skinnyRawEllipse = new Ellipse2D.Double(0, -SKINNY_ELLIPSE_HEIGHT / 2, rawEllipse.getWidth(), SKINNY_ELLIPSE_HEIGHT);
				AffineTransform afTransform2 = AffineTransform.getTranslateInstance(centerShip.getCannonPoint().x, centerShip.getCannonPoint().y);
				afTransform2.rotate(Math.toRadians(centerShip.getPointDirection()));
				
				g.setColor(new Color((int)(nShrink / 60.0 * 255), (int)(nShrink / 60.0 * 255), 255));
				g.fill(afTransform2.createTransformedShape(skinnyRawEllipse));
			}
				
//			/* Debug graphics */
//			g.setColor(Color.green);
//			/* Calculate the intersection of a ray originating from the ship and the rectangle of the screen. */
//			double shipAngle = Math.toRadians(centerShip.getPointDirection());
//			Point2D.Double shipCannonPoint = centerShip.getCannonPoint();
//			
////			for(int nTimes = 0; nTimes < 72; nTimes++){
////				shipAngle += 5;
//				
//				/* Intersection 1 will reason out Y value */
//				Point2D.Double intersection1 = new Point2D.Double();
//				if(Math.cos(shipAngle) > 0){
//					intersection1.x = Asteroids_v2.APPLET_X;
//				} else if(Math.cos(shipAngle) == 0){
//					intersection1.x = shipCannonPoint.getX();
//					System.out.println("cos == 0");
//				} else {
//					intersection1.x = 0;
//				}
//				intersection1.y = Math.tan(shipAngle) * (intersection1.x - shipCannonPoint.getX()) + shipCannonPoint.getY();
//				
//				/* Intersection 2 will reason out X value */
//				Point2D.Double intersection2 = new Point2D.Double();
//				if(Math.sin(shipAngle) > 0){
//					intersection2.y = Asteroids_v2.APPLET_Y;
//				} else if(Math.sin(shipAngle) == 0){
//					intersection2.y = shipCannonPoint.getY();
//					System.out.println("sin == 0");
//				} else {
//					intersection2.y = 0;
//				}
//				intersection2.x = (intersection2.y - shipCannonPoint.getY())/ Math.tan(shipAngle) + shipCannonPoint.getX();
//				/* The distance to the point of intersection is the smaller of the distances between the ray origin (ship) 
//				 * and the two points. */
//				double DISTANCE_TO_INTERSECTION = Math.min(intersection1.distance(shipCannonPoint), intersection2.distance(shipCannonPoint));
//				
//				/* Calculate the center of attraction for floaters that are not in the beam.
//				 * Use half the distance from the point of intersection to the ship for the distance from
//				 * the ship to the center of attraction. */
//				Point2D.Double centerOfAttraction = new Point2D.Double();
//				
//				centerOfAttraction.x = shipCannonPoint.getX() + DISTANCE_TO_INTERSECTION / 2 * Math.cos(shipAngle);
//				centerOfAttraction.y = shipCannonPoint.getY() + DISTANCE_TO_INTERSECTION / 2 * Math.sin(shipAngle);
//				g.draw(new Rectangle2D.Double(centerOfAttraction.x -10, centerOfAttraction.y -10, 20, 20));
////			}
//				/* Create a skinnier ellipse for the floaters to stop moving in */
//				final double SKINNY_ELLIPSE_HEIGHT = dLaserWidthFraction * rawEllipse.getWidth() / 10;
//				Ellipse2D.Double skinnyRawEllipse = new Ellipse2D.Double(0, -SKINNY_ELLIPSE_HEIGHT / 2, rawEllipse.getWidth(), SKINNY_ELLIPSE_HEIGHT);
//				AffineTransform afTransform2 = AffineTransform.getTranslateInstance(centerShip.getCannonPoint().x, centerShip.getCannonPoint().y);
//				afTransform2.rotate(Math.toRadians(centerShip.getPointDirection()));
//				g.draw(afTransform.createTransformedShape(skinnyRawEllipse));
		}
	}
}
