import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Timer;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.Animator.RepeatBehavior;


public class ReinforcementsManager implements ActionListener{
	final static int NUMBER_OF_SHIPS = 6; //Untested with an odd number.
	final static int ENTRY_THRESHOLD = 60;
	final static int BOUNTY_THRESHOLD = 40;
	final static int SHIELD_CHARGE_DURATION = 45000;
	
	private boolean bActive;
	/** The radius of the initial radio wave animation */
	private double dRadius; 
	/** Whether to draw the "shield charge" animation */
	private boolean bShieldCharging;
	/** Indicates whether threat is high or low. Used to determine what mode the ships will fire in. */
	private boolean bFewFloatersLeft;
	/** The current pair to be teleported in */
	private int nShipPair;
	/** The angle of rotation for the ship orbit */
	private double angShipOrbit;
	/** The list of ships that will surround the ship calling for help. */
	private LinkedList<Spaceship> Reinforcements;
	private Timer[] ShipAccelerationTimers;
	private Floater[] ShipTargets;
	/* Timers and animators */
	Animator RadioWave;
	Timer teleportInTimer;
	Timer mainLoop;
	Animator ringRetractor;
	Timer warningTimer;
	Animator shieldChargeTimer;
	
	/* References to existing variables */
	private Spaceship centerShip;
	private HUD Interface;
	private List<Floater> GoodGuys;
	private List<Floater> BadGuys; //For lasers only. No collision checking goes on here. 
	private Animator cooldownTimer;
	
	public ReinforcementsManager(Spaceship centerShip2){
		this.centerShip = centerShip2;
		
		bActive = false;
		dRadius = 25;
		bShieldCharging = false;
		bFewFloatersLeft = false;
		nShipPair = 0;
		angShipOrbit = 0;
		Reinforcements = new LinkedList<Spaceship>();
		ShipAccelerationTimers = new Timer[NUMBER_OF_SHIPS];
		ShipTargets = new Floater[NUMBER_OF_SHIPS];
		
		RadioWave = new Animator(2500, 2, Animator.RepeatBehavior.LOOP, new TimingTarget(){
			public void begin() {
				/* Decide here whether to bring in the ships */
				if(BadGuys.size() < ENTRY_THRESHOLD){
					Interface.setNotification(1, "Request denied. Too few enemies. (check weapon cooldowns)", Color.red, 8000);
					deactivate();
				}
			}
			public void end() {
				/* Decide here whether to bring in the ships EDIT: moved to the beginning */
//				if(BadGuys.size() < ENTRY_THRESHOLD){
//					Interface.setNotification(1, "Request denied. Reason: not enough danger. (bar is orange)", Color.red, 8000);
//					deactivate();
//				} else {
					Interface.setNotification(1, "Request acknowledged. Sending reinforcements...", Color.green, 6000);
					bFewFloatersLeft = false;
					cooldownTimer.start();
					
					/* Generate ship colors */
					Color shipColor = new Color((int)(Math.random() * 100), (int)(Math.random() * 100), (int)(Math.random() * 100));
					Color secondShipColor;
					if(shipColor.getRed() + shipColor.getGreen() + shipColor.getBlue() < 382.5){ /*  (255 * 3)/2 = 766  */
						secondShipColor = new Color(Math.min(255, shipColor.getRed() + 150),
											 Math.min(255, shipColor.getGreen() + 150),
											 Math.min(255, shipColor.getBlue() + 150),
											 shipColor.getAlpha());
					} else {
						secondShipColor = new Color(Math.max(0, shipColor.getRed() - 150),
											 Math.max(0, shipColor.getGreen() - 150),
											 Math.max(0, shipColor.getBlue() - 150),
											 shipColor.getAlpha());
					}
					
					/* Generate the ships and their acceleration timers. 
					 * This cannot go in the first level of the constructor because that would cause infinite recursion. */
					for(int nShip = 1; nShip <= NUMBER_OF_SHIPS; nShip++){
						Spaceship ship = new Spaceship();
						ship.setDesignNumber(8);
						ship.setX(10000);
						ship.setY(10000);
						
						/* Set the ship color. One color for odds and another for evens. */
						if(nShip % 2 == 1){
							ship.setColor(secondShipColor);
						} else {
							ship.setColor(shipColor);
						}
						
						Reinforcements.add(ship);
						GoodGuys.add(ship);
						ShipAccelerationTimers[nShip - 1] = new Timer(1000, null);
						ShipAccelerationTimers[nShip - 1].setRepeats(false);
					}
					
					teleportInTimer.start();
					warningTimer.start();
//				}
			}
			public void repeat() {}
			public void timingEvent(float progress) {
				/* Increase the radius of the radio wave */
				dRadius = 25 + 50 * progress;
			}
		});
		
		teleportInTimer = new Timer(1000, new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				/* Calculate new position and direction for each ship */
				final double dPointDirection = angShipOrbit + nShipPair * (360 / NUMBER_OF_SHIPS);
				final Point newLocation = new Point(centerShip.getX() + (int)(100 * Math.cos(Math.toRadians(dPointDirection))), centerShip.getY() + (int)(100 * Math.sin(Math.toRadians(dPointDirection))));
				final Point newLocation2 = new Point(centerShip.getX() + (int)(100 * Math.cos(Math.toRadians(dPointDirection + 180))), centerShip.getY() + (int)(100 * Math.sin(Math.toRadians(dPointDirection + 180))));
				
				Reinforcements.get(nShipPair).getHyperspace().activate(HyperspaceManager.IN_ONLY, dPointDirection, newLocation, false);
				Reinforcements.get(nShipPair + (NUMBER_OF_SHIPS / 2)).getHyperspace().activate(HyperspaceManager.IN_ONLY, dPointDirection + 180, newLocation2, false);
				nShipPair++;
				if((nShipPair + 1)* 2 >= NUMBER_OF_SHIPS){
					teleportInTimer.setRepeats(false);
				}
				if(mainLoop.isRunning() == false){
					mainLoop.start();
				}
			}
		});
		
		mainLoop = new Timer(Asteroids_v2.REFRESH_RATE, this);
		
		ringRetractor = new Animator(1500, new TimingTarget(){
			Point[] oldLocations = new Point[NUMBER_OF_SHIPS];
			
			public void begin() {
				/* Store the original locations */
				for(int nShip = 0; nShip < Reinforcements.size(); nShip++){
					oldLocations[nShip] = new Point(Reinforcements.get(nShip).getX(), Reinforcements.get(nShip).getY());
					Reinforcements.get(nShip).setDirectionX(0);
					Reinforcements.get(nShip).setDirectionY(0);
					
				}
			}
			public void end() {
				bFewFloatersLeft = false;
			}
			public void repeat() {}
			public void timingEvent(float progress) {
				/* Linearly interpolate location of all ships from original location to their position in the ring. */
				for(int nShip = 0; nShip < Reinforcements.size(); nShip++){
					Spaceship ship = Reinforcements.get(nShip);
					
					/* If the ship is in the visible area (ie present) */
					if((ship.getX() > -500 && ship.getX() < Asteroids_v2.APPLET_X + 500)
					&& (ship.getY() > -500 && ship.getY() < Asteroids_v2.APPLET_Y + 500)){
						final double shipAngle = angShipOrbit + Math.toRadians(nShip * (360 / Reinforcements.size()));
						Point newLocation = new Point(centerShip.getX() + (int)(100 * Math.cos(shipAngle)), 
													  centerShip.getY() + (int)(100 * Math.sin(shipAngle)));
						int newX = Math.round(oldLocations[nShip].x + progress * (newLocation.x - oldLocations[nShip].x));
						int newY = Math.round(oldLocations[nShip].y + progress * (newLocation.y - oldLocations[nShip].y));
						ship.setX(newX);
						ship.setY(newY);
						
						ship.setPointDirection(Math.toDegrees(Math.atan2(centerShip.getY() - ship.getY(), centerShip.getX() - ship.getX())));
					}
				}
			}
		});
		ringRetractor.setDeceleration(.75f);
		
		warningTimer = new Timer(45000, new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				Interface.setNotification(1, "Reinforcements preparing to depart...", Color.white, 6000);
				Interface.setNotification(2, "Shields charging", new Color(0,162,255), SHIELD_CHARGE_DURATION);
				
				/* Start the departure timer and start the "shield charge" animation. */
				shieldChargeTimer.setResolution(100);
				shieldChargeTimer.start();
			}
		});
		warningTimer.setRepeats(false);
		
		shieldChargeTimer = new Animator(SHIELD_CHARGE_DURATION / 3 / (NUMBER_OF_SHIPS + 1), (NUMBER_OF_SHIPS + 1), RepeatBehavior.LOOP, new TimingTarget(){
			/** For keeping track of which ship to teleport out. */
			int nTeleportOutShipIndex;
			
			public void begin() {
				bShieldCharging = true;
				nTeleportOutShipIndex = 0; 
			}
			public void end() {
				Interface.setNotification(1, "Reinforcements have departed", Color.white, 3000);
				bShieldCharging = false;
				deactivate();
			}
			public void repeat() {
				/* Teleport ship out */
				Reinforcements.get(nTeleportOutShipIndex).getHyperspace().activate(HyperspaceManager.OUT_ONLY, 0, new Point(10000,10000), false);
				nTeleportOutShipIndex++;
				
				/* Remove their target floater from array */
				ShipTargets[nTeleportOutShipIndex - 1] = null;
			}
			public void timingEvent(float arg0) {
				centerShip.getShield().append((long) (SHIELD_CHARGE_DURATION / ((double)shieldChargeTimer.getDuration() * NUMBER_OF_SHIPS / shieldChargeTimer.getResolution())));

				/* Face to point the center ship*/
				if(mainLoop.isRunning() == false){
					for(Spaceship ship: Reinforcements){
						final double angleTowardShip = Math.atan2(centerShip.getY() - ship.getY(), centerShip.getX() - ship.getX());
						ship.setPointDirection(Math.toDegrees(angleTowardShip));
					}
				}
			}
		});
		
	}
	public void activate(HUD Interface2, List<Floater> GoodGuys2, List<Floater> BadGuys2, Animator weaponTimer){
		bActive = true;
		dRadius = 25;
		bShieldCharging = false;
		bFewFloatersLeft = false;
		nShipPair = 0;
		angShipOrbit = 0;

		this.Interface = Interface2;
		this.GoodGuys = GoodGuys2;
		this.BadGuys = BadGuys2;
		
		Interface.setNotification(1, "Requesting assistance...", Color.white, 4000);
		
		RadioWave.setDeceleration(1.0f);
		RadioWave.start();
		cooldownTimer = weaponTimer;
		
		teleportInTimer.setRepeats(true);		
	}
	public void deactivate(){
		bActive = false;
		bShieldCharging = false;
		bFewFloatersLeft = false;
		for(Spaceship ship: Reinforcements){
			ship.setHP(0);
		}
		Reinforcements.clear();
		for(Timer timer: ShipAccelerationTimers){
			if (timer != null){
				timer.stop();
			}
		}
		for(int nI = 0; nI < ShipTargets.length; nI++){
			ShipTargets[nI] = null;
		}
		RadioWave.cancel();
		teleportInTimer.stop();
		mainLoop.stop();
		ringRetractor.stop();
		warningTimer.stop();
		shieldChargeTimer.cancel();
	}
	public boolean isActive(){return bActive;}
	public void draw(Graphics2D g){
		if(bActive){
			/* if radio wave is running, draw the magenta circles */
			if(RadioWave.isRunning()){
				/* Magenta with a fade out opacity */
				g.setColor(new Color(255, 0, 255, (int) (255 - 255 * ((dRadius - 25) / 50)) ));
				g.draw(new Ellipse2D.Double(centerShip.getX() - dRadius, centerShip.getY() - dRadius, dRadius * 2, dRadius * 2));
				g.draw(new Ellipse2D.Double(centerShip.getX() - (dRadius - 3), centerShip.getY() - (dRadius - 3), (dRadius - 3) * 2, (dRadius - 3) * 2));
				g.draw(new Ellipse2D.Double(centerShip.getX() - (dRadius - 5), centerShip.getY() - (dRadius - 5), (dRadius - 5) * 2, (dRadius - 5) * 2));
				g.draw(new Ellipse2D.Double(centerShip.getX() - (dRadius - 8), centerShip.getY() - (dRadius - 8), (dRadius - 8) * 2, (dRadius - 8) * 2));
				
			/* If the shield is charging, draw the transverse waves*/
			} else if (shieldChargeTimer.isRunning()){
				for(Spaceship ship: Reinforcements){
					/* If the ship is in the visible area (ie present) */
					if((ship.getX() > -500 && ship.getX() < Asteroids_v2.APPLET_X + 500)
					&& (ship.getY() > -500 && ship.getY() < Asteroids_v2.APPLET_Y + 500)){
						final double AMPLITUDE = 20;
						final double ARROW_SLOPE1 = .5 * Math.PI;
						final double ARROW_SLOPE2 = 11.0 / NUMBER_OF_SHIPS * Math.PI;
						final Color ARROW_COLOR1 = new Color(215, 19,171); /* Purple/Magenta */
						final Color ARROW_COLOR2 = new Color(  0,128,255); /* Light blue */
						final double WAVELENGTH = 100;
						final int PERIOD = 500;
						
						/** EM Vector drawing. I just took this from the repellant function and changed a little bit of it to make it look 
						 * like an EM wave.  */
						/* Non-customizable, for code readability only. */
						final double DISTANCE = Point.distance(centerShip.getX(), centerShip.getY(), ship.getX(), ship.getY());
						final double ANGLE_BTWN_FLOATERS = Math.atan2(ship.getY() - centerShip.getY(), ship.getX() - centerShip.getX());
						
						/* Since all the parts of the angle are constant, the angle is constant, and the components are constant. */
						final double ANGLE_SIN_LEFT1 = Math.sin(ANGLE_BTWN_FLOATERS - ARROW_SLOPE1);
						final double ANGLE_COS_LEFT1 = Math.cos(ANGLE_BTWN_FLOATERS - ARROW_SLOPE1);
						final double ANGLE_SIN_LEFT2 = Math.sin(ANGLE_BTWN_FLOATERS - ARROW_SLOPE2);
						final double ANGLE_COS_LEFT2 = Math.cos(ANGLE_BTWN_FLOATERS - ARROW_SLOPE2);
						
						for(double dR = 6 /*+ (-Util.modulus(6, 150))*/; dR <= DISTANCE; dR += 6){
							final double CONE_THICKNESS = AMPLITUDE * Math.cos(2 * Math.PI / WAVELENGTH * (dR + Util.modulus(WAVELENGTH, PERIOD)));
							
							/* Change the color depending on how far along the distance we are */
							g.setColor(new Color((int)(ARROW_COLOR1.getRed()*((double)dR/DISTANCE))
												,(int)(ARROW_COLOR1.getGreen()*((double)dR/DISTANCE))
												,(int)(ARROW_COLOR1.getBlue()*((double)dR/DISTANCE))));
							/* Determine the point in the middle of the cone a distance dR away from centerFloater */
							final int MIDDLE_X = (int)(dR*Math.cos(ANGLE_BTWN_FLOATERS) + centerShip.getX());
							final int MIDDLE_Y = (int)(dR*Math.sin(ANGLE_BTWN_FLOATERS) + centerShip.getY());
							g.drawLine(MIDDLE_X,MIDDLE_Y,(int)(CONE_THICKNESS * ANGLE_COS_LEFT1 + MIDDLE_X),(int)(CONE_THICKNESS* ANGLE_SIN_LEFT1 +MIDDLE_Y));
							g.setColor(new Color((int)(ARROW_COLOR2.getRed()*((double)dR/DISTANCE))
												,(int)(ARROW_COLOR2.getGreen()*((double)dR/DISTANCE))
												,(int)(ARROW_COLOR2.getBlue()*((double)dR/DISTANCE))));
							g.drawLine(MIDDLE_X,MIDDLE_Y,(int)(CONE_THICKNESS * ANGLE_COS_LEFT2 + MIDDLE_X),(int)(CONE_THICKNESS* ANGLE_SIN_LEFT2 +MIDDLE_Y));
						}
					}
				}
			}
			
			/* Draw the scopes */
			for(Floater curFloater: ShipTargets){
				if(curFloater != null){
					float nRadius = (float)(10 + curFloater.getSize());
					float nOuterRadius = nRadius + 10;
					
					g.setColor(Color.orange);
					g.drawLine((int)(curFloater.getX() - nRadius * 1.5), curFloater.getY(), (int)(curFloater.getX() + nRadius * 1.5), curFloater.getY());
					g.drawLine(curFloater.getX(), (int)(curFloater.getY() - nRadius * 1.5), curFloater.getX(), (int)(curFloater.getY() + nRadius * 1.5));
					g.draw(new Ellipse2D.Float(curFloater.getX() - nRadius, curFloater.getY() - nRadius, nRadius * 2, nRadius * 2));
					for(int nShift = 0; nShift <= 4; nShift++){
						g.drawArc((int)(curFloater.getX() - nOuterRadius), (int)(curFloater.getY() - nOuterRadius), (int)(nOuterRadius * 2), (int)(nOuterRadius * 2), (int)(Util.modulus(360, 1000) + nShift * 90), 30);
					}
				}
			}
			
			/* Debug: show actual */
		}
	}
	/* Main loop--once the first two ships are in, this starts repeating until all the ships teleport out.  */
	public void actionPerformed(ActionEvent e) {
		
		/* For each ship */
		for(Spaceship ship: Reinforcements){
			int nShipIndex = Reinforcements.indexOf(ship);
			/* If the ship is in the visible area (ie present) */
			if((ship.getX() > -500 && ship.getX() < Asteroids_v2.APPLET_X + 500)
			&& (ship.getY() > -500 && ship.getY() < Asteroids_v2.APPLET_Y + 500)){
				/* If danger is high */
				if(BadGuys.size() > BOUNTY_THRESHOLD){
					/* If variable is consistent with the actual number of floaters */
					if(bFewFloatersLeft == false){
						/* Change the ring rotation angle */
						angShipOrbit += Math.PI / 180;
						
						/* Fire in the ring, bullets for odds and lasers for evens */
						if(nShipIndex % 2 == 1){
							final Point2D.Double startPoint = ship.getCannonPoint();
							GoodGuys.add(new Bullet(startPoint.x, startPoint.y, ship.getPointDirection()));
						} else {
							ship.getLaser().activate(BadGuys, 1000);		
						}
						
						/* Move ship around the ring */
						final double shipAngle = angShipOrbit + Math.toRadians(nShipIndex * (360 / Reinforcements.size()));
						final Point newLocation = new Point(centerShip.getX() + (int)(100 * Math.cos(shipAngle)), 
															centerShip.getY() + (int)(100 * Math.sin(shipAngle)));
						ship.setPointDirection(Math.toDegrees(shipAngle));
						ship.setX(newLocation.x);
						ship.setY(newLocation.y);
					/* Else variable is inconsistent */
					} else {
						/* If retract animation is not already started*/
						if(ringRetractor.isRunning() == false){
							ringRetractor.start();
							for(Timer accelTimer: ShipAccelerationTimers){
								accelTimer.stop();
							}
						}
					}
					
				/* Else there are only a few bad guys */
				} else {
					/* If variable is INconsistent with the actual number of floaters */
					if(bFewFloatersLeft == false){
						/* If this is the last ship in the group, THEN set the consistency variable to true. Otherwise only 1 ship will get a target. */
						if(nShipIndex == NUMBER_OF_SHIPS - 1)
							bFewFloatersLeft = true;
						/* If there are still floaters to kill, assign one of them to this ship. */
						if(BadGuys.size() > 0){
							ShipAccelerationTimers[nShipIndex].start();
							ShipTargets[nShipIndex] = BadGuys.get((int) (BadGuys.size() * Math.random()));
						}
					}
					
					if(ShipTargets[nShipIndex] != null){
						if(ShipTargets[nShipIndex].shouldBeRemoved() == true){
							ShipAccelerationTimers[nShipIndex].stop();
							ShipTargets[nShipIndex] = null;

						/* Else target is still alive */
						} else {
							/* Point toward the target */
							final double angleTowardTarget = Math.atan2(ShipTargets[nShipIndex].getY() - ship.getY(), ShipTargets[nShipIndex].getX() - ship.getX());
							ship.setPointDirection(Math.toDegrees(angleTowardTarget));
							
							if(ShipAccelerationTimers[nShipIndex].isRunning()){
								/* Accelerate toward target */
								ship.accelerate(.5);
							} else {
								/* FIRE at the target */
								
								/* If floater size is small then just use a laser. */
								if(ShipTargets[nShipIndex].getSize() <= 5){
									ship.getLaser().activate(BadGuys, 1000);
								} else if(nShipIndex % 2 == 1){
									final Point2D.Double startPoint = ship.getCannonPoint();
									GoodGuys.add(new Bullet(startPoint.x, startPoint.y, ship.getPointDirection()));
								} else {
									GoodGuys.add(new HomingMissile(ship.getCannonPoint().x, ship.getCannonPoint().y, ship.getPointDirection() + 0, BadGuys.get((int) Math.floor(Math.random()*BadGuys.size())) ));
								}
							}
						}
					/* else the ship has no current target*/
					} else {
						/* If there are still floaters to kill, assign one of them to this ship. */
						if(BadGuys.size() > 0){
							ShipAccelerationTimers[nShipIndex].start();
							ShipTargets[nShipIndex] = BadGuys.get((int) (BadGuys.size() * Math.random()));
						}
						if(ringRetractor.isRunning()){
							ringRetractor.cancel();
						}
					}
					
					/* If there are no bad guys left, start the departure early. */
					if(BadGuys.size() <= 0){
//						if(shieldChargeTimer.isRunning() == false){
//							for (ActionListener al: warningTimer.getActionListeners()){
//								al.actionPerformed(null);
//								warningTimer.stop();
//							}
//						}
						bFewFloatersLeft = false;
					}
				}
			}
		}
	}
}