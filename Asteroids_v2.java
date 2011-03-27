/**
 *	Asteroids v2 by Andrew Ong
 *	Started: March 28, 2009
 *
 *	Credits to Corey for his collision methods.
 *
 *	There were so many things that I wanted to fix with the first one that I decided to ditch it entirely 
 *	and rewrite it from the ground up. I have recycled weapon algorithms (with some serious revisions), 
 *	but the core functions have been changed completely. The last thing I did to improve the first version 
 *	was adding chain lightning. 
 *
 *	Code improvements from the old version:
 *	-Input system MUCH better. Uses PressedKeys ArrayList and a [multi-]Map instead of endless 
 *		booleans. Now has the potential for dynamic in-game controls. There is now a checkKeys 
 *		function rather than before where the key checking was done in ShipMovement(). 
 *		The new KeyHolder class allows you to specify an EXACT key on the keyboard rather than a 
 *		keycode. For example, you can specify that you want hyperspace to map to the LEFT control
 *		rather than both of them.
 *	-All the Applet variables (not constants) are declared in the init() function now.	
 *	-Stopped using "nAnimationRegulator" in favor of System time functions. 
 *		New function: modulus(maximum, period).
 *	-Changed main ship name from "Talon" to "Cruzer".
 *	-Changed some of the variables to static (but still private!) with only "get" accessor methods.
 *		(it's a BIT better than public statics, right?)
 *	-DrawInterface() now has its own class and has been stripped of weird things like the threat 
 *		level monitor. 
 *	-New classes ProgressTimer and various TimerTasks. They enable animations to be specified in number
 *		of milliseconds rather than number of frames. (but a little more work is required to set it up)
 *		They also remove the need for "checker" functions for the sake of executing something at the end.
 *	-Smooth white gradients on HUD are now generated at runtime and dynamically scaled and clipped. 
 *		This reduces CPU load by ~50%. 
 *	-The ship accelerate(...) function now lets you decelerate even when velocity is above the limit.
 *		Before, it didn't let you decelerate if you were going too quickly, so if the asteroids started 
 *		pushing the ship around, you became a ragdoll until the "friction" stopped you. (deceleration as in 
 *		accelerating the opposite way, not decelerating with the down key.
 *	-The rotation formulas have been fixed...they WERE y cos - x sin and y sin + x cos instead of 
 *		x cos - y sin and x sin + y cos. This caused the ship to be drawn 90 degrees the wrong way.
 *	-Removed the "abstract" designator from most of the methods in the Floater class.
 *	-The laser, now with its own class, checks for an asteroid at every 10th space instead of every space. 
 *		In addition, it only checks the asteroids who would be in contact with the laser line anyway. In 
 *		the old version, it checked every radius from 1 to 1000 and EVERY asteroid each time.
 *	-All methods are now [have to be] synchronized where necessary, eliminating the ConcurrentModificationExceptions
 *		that like to accompany Threads.
 *	-Comment style has been improved; all human comments are in block comments and single lines are regular slash 
 *		comments.
 *	-The code for the repellant function has been completely modified, with all the "readability variables" (that never
 *		variate) changed to final variables, and a lot of the "weird" code has been taken out. (see "visible" improvements)
 *
 *	Visible improvements from the old version:
 *	-There is now an intro screen.
 *	-Made white spectrum animation on the dashboard longer. The center of the spectrum also travels beyond 
 *		the borders of the bar width, unlike before. It means that the spectrum will now completely disappear 
 *		before coming back, unlike before where it disappeared halfway.
 *	-There is now a dashboard animation for when a meter is replenishing. The colors can be changed easily 
 *		and the width depends on how much of the meter is filled already.
 *	-The speed meter on the dashboard is smoother, more colorful, and the caption now reads "Velocity" 
 *		instead of "Momentum".
 *	-The dashboard is no longer a restricted area of the screen. Ships, stars and asteroids can travel 
 *		under the dashboard.
 *	-New notification system in the dashboard and the top of the screen. They make use of the new ProgressTimer
 *		class.
 *	-"Friction" is now implemented as a multiplier constant rather than a subtraction constant, 
 *		just like the real universe. It can also be turned on and off.
 *	-Hyperspace now uses more ships, is adjustable in terms of time, and gives a 1-second invulnerability
 *		when the ship comes out of it.
 *	-Bullets now move 50% faster and twice as lengthy. They also come out in a shower instead of a line.
 *	-Asteroids are now less spikey and look less like lint.
 *	-The laser now has multiple levels of effectiveness.
 *	-The down button will now turn you 180 instead of decelerating. That means no more of that "or decelerating 
 *		or accelerating and decelerating at the same time" crap in the ShipMovement() function.
 *	-There is a new notification system in multiple places on the HUD.
 *	-Various buttons now only execute their effect once per button press. This was to make their effects like switches
 *		rather than strobe lights. (like the one to turn 180 and the Debug mode on and off switch) 
 *	-Added basic mouse controls to change ship direction and fire. 
 *	-Repellant now works much more smoothly. The old repellant worked by changing the velocity vectors of the floater
 *		which is how it was supposed to work, but it was doing it through some weird, complicated algorithm. The old
 *		repellant ALSO unrealistically teleported the floater a short distance away from the center. The new repellant
 *		changes only the velocity vectors, so the [realistic] parabolic path is much easier to observe.
 *	-The repellant also has a nice new animation and is timed so that keeping a finger on the button to use it is not necessary. 
 *	-Particle debris now appears on every bullet-asteroid collision. That debris as well as the explosion debris
 *		is somewhat directed depending on the original directions of the bullet or asteroid.
 *	-There is now an options panel where you can switch many boolean options and the game controls!
 *	-It is now possible to change the ship color AND design! Designs are implemented as simple polygons and can have
 *		their color changed via the options panel. In addition, the ship now has an outline that can change color too!
 */

import java.applet.Applet;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.ShortLookupTable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.event.MouseInputListener;

import org.jdesktop.animation.timing.Animator;

import com.jhlabs.image.InvertFilter;

/**
 * 	TODO Fix weapon timer for homing missile
 */

public class Asteroids_v2 extends Applet implements KeyListener, Runnable, FocusListener, ActionListener, MouseInputListener{
	
	/* Constants */
	public final static int REFRESH_RATE = 33;
	public final static int APPLET_X = 1200;
	public final static int APPLET_Y = 700;
	public final int NUMBER_OF_STARS = 300;
	public final static int MAX_PARTICLES = 2000;
	
	/* Do not touch these */
	private Thread animation;
	private BufferedImage image;	//Double buffering
	private Graphics2D offscreen;	//Double buffering
	private BufferedImage imagePost; //Post-processing
	/** Two image buffers are required for the trippy buffer. By trippy, I mean
	 *  it doesn't get erased fully every frame, leaving a trailing effect. Two 
	 *  buffers are required because it takes much less less CPU to copy one 
	 *  buffer to another at only 90% opacity using SRC_OVER than it does to do
	 *  a 10% erase on one using DST_IN. */
	private BufferedImage imageTrippy1; 
	private Graphics2D imageTrippyGraphics1;
	/** Refer to imageTrippy1 for explanation. */
	private BufferedImage imageTrippy2; 
	private Graphics2D imageTrippyGraphics2;
	/** Marks the current active buffer. Flips every frame. */
	private boolean currentActiveTrippyBufffer = false;
	//TODO Engine: Create variables to save CPU by clearing the trippy buffer's erased pixel data
	
	/* Regular variables. I HAD to make some of them static, but they notice they are still private. 
	 * I used the next best thing to public-statics: private statics with only "get" accessor methods! */
	private List<KeyHolder> PressedKeys;
	private List<Floater> GoodGuys; 
	private List<Floater> BadGuys; //Asteroids, enemy ships etc.
	private List<Star> Stars;
	private List<Particle> Particles;
	private LinkedHashMap<String, ArrayList<KeyHolder>> P1Controls; //My cheap multimap. Enables multiple controls for each function.
	private HashMap<String, Boolean> imageEffects;
	private LinkedHashMap<String, Boolean> Unlockables;
	private LinkedHashMap<String, Animator> WeaponTimers;
	private Spaceship Cruzer; 
	private HUD Interface;
	private static int nDeaths;
	private int nRestarts;
	private static int nScore;
	private boolean bAtIntro;
	private boolean bPaused;
	private static int nMouseX;
	private static int nMouseY;
	/* Options */
	private LinkedHashMap<String, Option> options;
	private OptionsDialog optionsSelector;
	/* For game start. Initialized in initGame(). */
	private double dStarDirectionX;
	private double dStarDirectionY;
	/* Debug variables */
	private static boolean bDebugModeEnabled;
	private long lLastFPSTime;
	private int nFramesPassed;
	private int nFPS;
	/* Since there are so few built-in mouse button constants, it is not necessary to create a fancy mouse-controls
	 * system. Simple booleans will suffice. For each button, one variable for when the button is pressed, and 
	 * one to indicate whether it has already been polled. (for effects that occur only once per press) */
	private boolean bMouse1Pressed;
	private boolean bMouse2Pressed;
	private boolean bMouse3Pressed;
	private boolean bMouse1Polled;
	private boolean bMouse2Polled;
	private boolean bMouse3Polled;
	
	@SuppressWarnings("unchecked")
	public void init(){
		setBackground(Color.black);
		
		addKeyListener(this);
		addFocusListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		/* Double buffering */
		image = new BufferedImage(APPLET_X, APPLET_Y, BufferedImage.TRANSLUCENT );
		offscreen = image.createGraphics();
		imageTrippy1 = new BufferedImage(APPLET_X, APPLET_Y, BufferedImage.TRANSLUCENT );
		imageTrippyGraphics1 = imageTrippy1.createGraphics();
		imageTrippy2 = new BufferedImage(APPLET_X, APPLET_Y, BufferedImage.TRANSLUCENT );
		imageTrippyGraphics2 = imageTrippy2.createGraphics();
		
		/* Post-processing*/
		imagePost = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TRANSLUCENT);
		
		/* Variable declarations */
		Stars = new LinkedList<Star>();
		Particles = Collections.synchronizedList(new LinkedList<Particle>());
		GoodGuys = Collections.synchronizedList(new LinkedList<Floater>());
		BadGuys = Collections.synchronizedList(new LinkedList<Floater>());
		nDeaths = 0;
		nRestarts = 0;
		Interface = new HUD();
		Cruzer = null; /* initialized later */
		bPaused = false;
		nMouseX = 0;
		nMouseY = 0;
		bMouse1Pressed = false;
		bMouse2Pressed = false;
		bMouse3Pressed = false;
		bMouse1Polled = true;
		bMouse2Polled = true;
		bMouse3Polled = true;
		WeaponTimers = new LinkedHashMap<String, Animator>();
		WeaponTimers.put("Laser", new Animator(30000));
		WeaponTimers.put("Pulse", new Animator(30000));
		WeaponTimers.put("Repellant", new Animator(30000));
		WeaponTimers.put("Homing missile", new Animator(20000));
		WeaponTimers.put("Fan", new Animator(40000));
		WeaponTimers.put("Chain lightning", new Animator(60000));
		WeaponTimers.put("Reinforcements", new Animator(120000));
		WeaponTimers.put("Zero laser", new Animator(120000));
		
		lLastFPSTime = System.nanoTime();
		nFramesPassed = 0;
		nFPS = 0;
		
		/* Accept Tab KeyEvents */
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,Collections.EMPTY_SET);
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,Collections.EMPTY_SET);
		
		/* Default user input. This can be changed at runtime. */
		PressedKeys = Collections.synchronizedList(new ArrayList<KeyHolder>());
		P1Controls = getDefaultControls();
		
		/* List the post-processing effects */
		imageEffects = new HashMap<String, Boolean>();
		imageEffects.put("Invert", new Boolean(false));
		imageEffects.put("Rumble", new Boolean(false));
		imageEffects.put("Big Rumble", new Boolean(false));
		
		/* Options map and dialog box. The listener for the dialog box is in this class. */
		options = new LinkedHashMap<String, Option>();
		options.put("Friction", new Option("Enable friction breaks", true, "Patented frictional break technologyª makes you slow down over time even though there is nothing around you! It makes it easier to steer the ship."));
		options.put("Precision firing", new Option("Precision firing", false, "Specifies whether bullets fired from the keyboard come out in the same direction each time instead of a 10° shower."));
		options.put("Anti-aliasing", new Option("Enable anti-aliasing", false, "Blends pixels into each other to make for less jagged picture. Large performance penalty, but the game will look prettier!"));
		options.put("Pause on focus loss", new Option("Pause on focus loss", true, "Disable this if you do not want the game to pause every time you click on a different window or out of the applet."));
		optionsSelector = null; /* initialized in initIntro() */
		
		/* Add stars */
		for(int nIndex = 0; nIndex < NUMBER_OF_STARS; nIndex++)
			Stars.add(new Star(APPLET_X, APPLET_Y));
		
		/* Start the game */
		initIntro();
		
	}
	public void paint(Graphics g){
		/* Clear offscreen buffer be drawing large black rectangle. */
		offscreen.setColor(new Color(0,0,0));
		offscreen.fillRect(0, 0, APPLET_X, APPLET_Y);
		/* Partial erasing with multiple buffers: Clear the non-active buffer,
		 * copy the current active trippy buffer on to the other at less than
		 * full opacity, then make the second buffer active by switching 
		 * boolean. */
		if(currentActiveTrippyBufffer){
			/* Clear the non-active buffer using mode SRC and drawing a big 
			 * rectangle of alpha 0 to achieve total transparency. For some 
			 * reason this is faster than just using SRC on the proceeding image
			 * copy. I think it is because this alpha is 1 and the proceeding 
			 * uses .9f, a slow floating number. */
			imageTrippyGraphics2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1));
			imageTrippyGraphics2.setColor(new Color(255, 0, 0, 0));
			imageTrippyGraphics2.fillRect(0, 0, APPLET_X, APPLET_Y);
			/* Copy buffer 1 on to buffer 2 */
			imageTrippyGraphics2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .9f));
			imageTrippyGraphics2.drawImage(imageTrippy1, 0, 0, this);
			imageTrippyGraphics2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		} else { 
			/* Refer to above comment. */
			imageTrippyGraphics1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1));
			imageTrippyGraphics1.setColor(new Color(255, 0, 0, 0));
			imageTrippyGraphics1.fillRect(0, 0, APPLET_X, APPLET_Y);
			/* Copy buffer 1 on to buffer 2 */
			imageTrippyGraphics1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .9f));
			imageTrippyGraphics1.drawImage(imageTrippy2, 0, 0, this);
			imageTrippyGraphics1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		}
		currentActiveTrippyBufffer = !currentActiveTrippyBufffer;
		
		/* Drawing */
		/* Always draw stars */
		for(int nIndex = 0; nIndex < Stars.size();nIndex++)
			Stars.get(nIndex).draw(offscreen);
		if(bAtIntro){
			/* If at intro, paint centered text. */
			Font fDialog = new Font("Dialog", Font.PLAIN, 16);
			offscreen.setFont(fDialog);
			offscreen.setColor(Color.white);
			
			String s1 = new String("  Welcome to Asteroids");
			String s2 = new String("Press \"Fire\" when you are ready");
			String s3 = new String("or just watch the pretty stars");
			
			int nStringX1 = (int)(APPLET_X / 2 - ((double)g.getFontMetrics().stringWidth(s1) / 2));
			int nStringX2 = (int)(APPLET_X / 2 - ((double)g.getFontMetrics().stringWidth(s2) / 2));
			int nStringX3 = (int)(APPLET_X / 2 - ((double)g.getFontMetrics().stringWidth(s3) / 2));
			int nStringY = g.getFontMetrics().getHeight();
			
			offscreen.drawString(s1, nStringX1, APPLET_Y / 2 + (int)(nStringY * -2.0) );
			offscreen.drawString(s2, nStringX2, APPLET_Y / 2 + (int)(nStringY * -0.0) );
			offscreen.drawString(s3, nStringX3, APPLET_Y / 2 + (int)(nStringY * 2.0) );
						
		} else if(bAtIntro == false && Cruzer.getHP() > 0){
			/* If not at intro and still alive */
			Cruzer.drawUnder(offscreen);
			
			
			/* Paint floaters and copy offscreen and trippy buffer to screen 
			 * before HUD. */
			if(currentActiveTrippyBufffer){
				FloaterPaint(offscreen, imageTrippyGraphics1);
				offscreen.drawImage(imageTrippy1, 0, 0, this);
			} else {
				FloaterPaint(offscreen, imageTrippyGraphics2);
				offscreen.drawImage(imageTrippy2, 0, 0, this);
			}
			
			Cruzer.draw(offscreen, isPressed("Accelerate") || bMouse1Pressed);
			
			Interface.DrawDashboard(offscreen, Cruzer, Unlockables, WeaponTimers);
			Interface.DrawUpperNotice(offscreen);
			if(isPressed("Weapons monitor")){
				Interface.drawWeaponMonitor(offscreen, Unlockables, WeaponTimers, BadGuys.size());	
			}
			//Interface.DrawMap(10, APPLET_Y - 200, offscreen, Cruzer, canCollideWithShip);
			
		} else { /* else dead */
			if(currentActiveTrippyBufffer){
				FloaterPaint(offscreen, imageTrippyGraphics1);
				offscreen.drawImage(imageTrippy1, 0, 0, this);
			} else {
				FloaterPaint(offscreen, imageTrippyGraphics2);
				offscreen.drawImage(imageTrippy2, 0, 0, this);
			}
			Interface.DrawDashboard(offscreen, Cruzer, Unlockables, WeaponTimers);
			Interface.DrawUpperNotice(offscreen);
			if(isPressed("Weapons monitor")){
				Interface.drawWeaponMonitor(offscreen, Unlockables, WeaponTimers, BadGuys.size());	
			}
		}
		/* Component drawing */
		paintComponents(offscreen);
		
		/* This is here for debugging. Normally this would be in the run() loop. */
		CheckNonShipCollisions();
		
		/* FPS Meter */
		offscreen.setColor(Color.white);
		offscreen.drawString("FPS: " + nFPS, 20, 20);
		nFramesPassed++;
		if(System.nanoTime() - lLastFPSTime > 1000000000){
			nFPS = nFramesPassed;
			nFramesPassed = 0;
			lLastFPSTime = System.nanoTime();
		}
		
		/* Debug 
		 * Testing whether clearRect() makes things transparent */
		
		
		g.drawImage(postProcess(image), 0, 0, this);
	}
	private BufferedImage postProcess(BufferedImage origImage ){
		/* Clear imagePost */
		Graphics2D g = imagePost.createGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, APPLET_X, APPLET_Y);
		
		/* Clone the image so there is something to show just in case no effects are activated. */
		Graphics2D gPost = imagePost.createGraphics();
		gPost.drawImage(origImage, 0, 0, this);
		
		if(imageEffects.get("Invert").booleanValue()){
			/* Create a data lookup table that inverts by applying formula 255 - i to each pixel. */
			short[] invert = new short[256];
			short[] straight = new short[256];
		    for (int i = 0; i < 256; i++) {
		        invert[i] = (short) (255 - i);
		        straight[i] = (short)i;
		    }
	        short[][] invert3 = new short[][] { invert, invert, invert, straight };

	        LookupTable lookupTable = new ShortLookupTable(0, invert3);
	        LookupOp op = new LookupOp(lookupTable, null);
			
			//InvertFilter op = new InvertFilter();
	        imagePost = op.filter(imagePost, null);
			
		}
		if(imageEffects.get("Rumble").booleanValue() || imageEffects.get("Big Rumble").booleanValue()){
			double RANDOMNESS;
			if(imageEffects.get("Big Rumble").booleanValue()){
				RANDOMNESS = 50;
			} else {
				RANDOMNESS = 10;
			}
			 
//			AffineTransform transform = new AffineTransform();
//			transform.rotate((Math.random() * (Math.PI/15)) - (Math.PI/30),APPLET_X/2,APPLET_Y/2);
//			transform.translate(Math.random()*10 -5,Math.random()*10 -5);
//			AffineTransformOp op = new AffineTransformOp(transform, null);
//			op.filter(origImage, imagePost);
			gPost.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
			for(int nTimes = 1; nTimes <= 2; nTimes++)
				gPost.drawImage(origImage, (int)(Math.random()*RANDOMNESS -(RANDOMNESS/2)), (int)(Math.random()*RANDOMNESS -(RANDOMNESS/2)), this);
			gPost.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		}
		
		return imagePost;
	}
	/**	Initializes values for when the applet starts or when the game is over. */
	private void initIntro(){
		bAtIntro = true;
		bPaused = false;
		bDebugModeEnabled = false;
		
		/* Save the ship color and initialize a new ship */
		Color shipColor;
		final int shipDesign;
		if(Cruzer != null){
			shipColor = Cruzer.getColor();
			shipDesign = Cruzer.getDesignNumber();
			optionsSelector.setVisible(false);
		} else {
			shipColor = new Color(105,105,105);
			shipDesign = 0;
		}
		Cruzer = new Spaceship();
		Cruzer.setColor(shipColor);
		Cruzer.setDesignNumber(shipDesign);
		/* Create new options windows and disable the ship color tab */
		optionsSelector = new OptionsDialog(this, options, P1Controls, Cruzer); /* only necessary because of new Spaceship, so new handle. */
//		JTabbedPane tabs = (JTabbedPane) optionsSelector.getContentPane();
//		tabs.setEnabledAt(tabs.indexOfTab("Ship color"),false);
		
		/* If there has been a game over or a restart, don't change the direction of the stars. */
		if((nDeaths > 0 || nRestarts > 0) && Cruzer.getSpeed() > 0){
			dStarDirectionX = Cruzer.getDirectionX();
			dStarDirectionY = Cruzer.getDirectionY();
		} else { /* the game has just started */
			dStarDirectionX = 3 + (Math.random() * 5);
			dStarDirectionY = dStarDirectionX;
		}
	}
	/** Initializes all the values to start the actual gameplay. */
	private void initGame(){
		bAtIntro = false;
		Cruzer.getShield().activate(3000);
		nScore = 0;
		Interface.setNotification(1, "Level 1", Color.white, 10000);
		Interface.setNotification(1, "Press Escape to pause", Color.white, 7500);
		Unlockables = getFreshUnlockables();
		/* Cancel all weapon recharge timers */
		for(Animator timer : WeaponTimers.values()){
			timer.cancel();
		}
		
		/* Clear arrays of colliders */
		GoodGuys.removeAll(GoodGuys);
		BadGuys.removeAll(BadGuys);
		
		/* Add asteroids */
		for(int nIndex = 0; nIndex < 90; nIndex++){
			BadGuys.add(new Asteroid(5));
		}
	}
	/** Terminates threads, restarts the game at sets it to the intro screen. */
	private void restart(){
		nRestarts++;
		if(Cruzer.getHyperspace().isActive())
			Cruzer.getHyperspace().terminate();
		
		initIntro();
	}
	/** Moves all stars in ArrayList Stars to specified X and Y components.
	 * 
	 * @param dStarDirectionX The X component.
	 * @param dStarDirectionY The Y component.
	 */
	private void StarMovement(double dStarDirectionX, double dStarDirectionY){
		for(int nIndex = 0; nIndex < Stars.size();nIndex++){
			Stars.get(nIndex).setDirectionX(dStarDirectionX);
			Stars.get(nIndex).setDirectionY(dStarDirectionY);
			Stars.get(nIndex).move(APPLET_X, APPLET_Y);
		}
	}
	private void FloaterMovement(boolean bRelativeToShip){
		/* Declare new list to keep track of what has been moved already 
		 * EDIT: Became unnecessary when I changed canCollideWithEachOther to GoodGuys/BadGuys */
//		LinkedList<Floater> beenMoved = new LinkedList<Floater>();
		
		synchronized (GoodGuys) {
			ListIterator<Floater> iterator1 = GoodGuys.listIterator();
			while (iterator1.hasNext()) {
				Floater floater = (Floater) iterator1.next();
//				if (beenMoved.contains(floater) == false) {
//					beenMoved.add(floater);
					
					
					if (floater instanceof Bullet || floater instanceof Pulsar_wave || floater instanceof HomingMissileLauncher) {
						floater.moveBoundless();
						if (bRelativeToShip) {
							floater.moveFrom(Cruzer.getDirectionX(), Cruzer.getDirectionY());							
						}
					} else if(floater instanceof Spaceship){
						ShipMovement((Spaceship) floater);
					}
//				}
			}
		}
		
		synchronized (BadGuys) {
			ListIterator<Floater> iterator2 = BadGuys.listIterator();
			
			while(iterator2.hasNext()){
				Floater floater = (Floater)iterator2.next();
				
//				if (beenMoved.contains(floater) == false) {
//					beenMoved.add(floater);
				
				if (floater instanceof Asteroid) {
					floater.moveBounded(APPLET_X, APPLET_Y);
					floater.rotate(((Asteroid) floater).getRotationSpeed());
					if (bRelativeToShip) {
						floater.moveFrom(Cruzer.getDirectionX(), Cruzer.getDirectionY());
					}
				} 
//				}
			}
		}
		
		synchronized (Particles) {
			ListIterator<Particle> iterator3 = Particles.listIterator();
			while (iterator3.hasNext()) {
				Particle particle = iterator3.next();
				particle.moveBoundless();
				if (bRelativeToShip) {
					particle.moveFrom(Cruzer.getDirectionX(), Cruzer.getDirectionY());
				}
			}
		}
	}
	private void FloaterPaint(Graphics2D g, Graphics2D trippyG){
		/* Declare new list to keep track of what has been drawn already 
		 * EDIT: Became unnecessary when I changed canCollideWithEachOther to GoodGuys/BadGuys */
//		LinkedList<Floater> beenDrawn = new LinkedList<Floater>();
		
		synchronized (Particles) {
			ListIterator<Particle> iterator3 = Particles.listIterator();
			while (iterator3.hasNext()) {
				Particle particle = iterator3.next();
				particle.draw(g);
			}
		}
		//TODO Only make it so that certain floaters get drawn on the trippy buffer 
		synchronized (GoodGuys) {
			ListIterator<Floater> iterator1 = GoodGuys.listIterator();
			while (iterator1.hasNext()) {
				Floater floater = iterator1.next();
//				if (beenDrawn.contains(floater) == false) {
//					beenDrawn.add(floater);
					floater.draw(trippyG);
//				}
			}
		}
		synchronized (BadGuys) {
			ListIterator<Floater> iterator2 = BadGuys.listIterator();
			while (iterator2.hasNext()) {
				Floater floater = iterator2.next();
//				if (beenDrawn.contains(floater) == false) {
//					beenDrawn.add(floater);
					floater.draw(g);
//				}
			}
		}
	}
	/**
	 * Iterates through the list of objects that can collide with each other, not including
	 * the ship, and chooses an action depending on which two objects they are.
	 */
	private void CheckNonShipCollisions(){
		synchronized(GoodGuys){
			synchronized(BadGuys){
					
				//int nLines = 0;
				//int nCombinations = 0;
				for(int nFloater1 = 0; nFloater1 < GoodGuys.size(); nFloater1++){
					for(int nFloater2 = 0; nFloater2 < BadGuys.size(); nFloater2++){
							Floater floater1 = GoodGuys.get(nFloater1);
							Floater floater2 = BadGuys.get(nFloater2);
	
						/* Here be all the collision cases. I tried to reduce the ugliness as much as possible. */
						if(floater1 instanceof Bullet && floater2 instanceof Asteroid){
							Bullet bullet = (Bullet) floater1;
							Asteroid asteroid = (Asteroid) floater2;
							//nCombinations++;
							//offscreen.setColor(new Color(0,0,255));
							//offscreen.drawLine(asteroid.getX(), asteroid.getY(), floater2.getX(), floater2.getY());
	//						offscreen.setColor(Color.blue);
	//						offscreen.draw(bullet.getShape());
							
							if(asteroid.distance(bullet) < 120){
	//							offscreen.setColor(Color.red);
	//							offscreen.drawLine((int)asteroid.getX(), (int)asteroid.getY(), (int)floater2.getX(), (int)floater2.getY());
								
								if(Util.shapeIntersect(bullet.getShape(), asteroid.getShape())){
									/* Generate particles on collision. [not necessarily explosion] */
									if(Particles.size() < MAX_PARTICLES){
										for(int nNum = 1; nNum <= ((Asteroid) asteroid).getSize() / 2; nNum++){
											/* Direct the debris particles in the opposite direction of the bullet */
											Particle debris = new Particle(bullet.getX(), bullet.getY());
											debris.setDirectionX(debris.getDirectionX() - (bullet.getDirectionX() * .1));
											debris.setDirectionY(debris.getDirectionY() - (bullet.getDirectionY() * .1));
											Particles.add(debris);
										}
									}
									/* Destroy bullet */
									bullet.setHP(0);
									/* Damage asteroid */
									asteroid.setHP(asteroid.getHP() - 10);
									/* If the asteroid was small and the player destroyed it, congratulate the player. */
									if(asteroid.getSize() < 10 && asteroid.getHP() <= 0 && !(bullet instanceof HomingMissile) ){
										Interface.setNotification(1, "Sharpshooter!", Color.green);
									}
								}
							}
						} else if(floater1 instanceof Pulsar_wave && floater2 instanceof Asteroid){
							/* Assign to correct handles */
							Pulsar_wave wave = (Pulsar_wave) floater1;
							Asteroid asteroid = (Asteroid) floater2;
							if(wave.getSize() > 25){
								if(Util.shapeIntersect(wave.getShape(), asteroid.getShape())){
									
									/* Damage asteroid */
									asteroid.setHP(asteroid.getHP() - 10);
								}
							}
//							/* Debug graphics */
//							offscreen.setColor(Color.white);
//							offscreen.draw(wave.getShape());
//						} else if(floater1 instanceof Bomb && floater2 instanceof Asteroid){
//							/* Assign to correct handles */
//							Bomb bomb = (Bomb) floater1;
//							Asteroid asteroid = (Asteroid) floater2;
//							
//							if(asteroid.distance(bomb) < asteroid.getSize() + bomb.getSize()){
//								if(Util.shapeIntersect(bomb.getShape(), asteroid.getShape())){
//									if(bomb.isExploding() == false){
//										/* Generate particles on collision. [not necessarily explosion] */
//										for(int nNum = 1; nNum <= asteroid.getSize() / 2; nNum++){
//											/* Direct the debris particles in the opposite direction of the bomb */
//											Particle debris = new Particle(bomb.getX(), bomb.getY());
//											debris.setDirectionX(debris.getDirectionX() - (bomb.getDirectionX() * .1));
//											debris.setDirectionY(debris.getDirectionY() - (bomb.getDirectionY() * .1));
//											Particles.add(debris);
//										}
//										Util.elasticCollision(asteroid, bomb);
//									} else {
//										/* Damage asteroid */
//										asteroid.setHP(asteroid.getHP() - 50);
//									}
//								}
//							}
//						}
						} else if(floater1 instanceof Shockwave){
							Shockwave shockwave = (Shockwave) floater1;
							if(/*floater2 instanceof PolyFloater &&*/ Util.shapeIntersect(shockwave.getShape(), floater2.getShape())){
								floater2.setColor(Color.red);
								floater2.setHP(0);
							}
						}
						/* for demonstration only */
						/* else if((floater1 instanceof Asteroid && floater2 instanceof Asteroid)){
							//Radius test
							if(floater1.distance(floater2) < 100){
								//offscreen.setColor(Color.green);
								//offscreen.drawLine(floater1.getX(), floater1.getY(), floater2.getX(), floater2.getY());
								//nLines++;
								//Polygon test
								if(polyIntersectEdge(((Asteroid) floater1).getShape(), ((Asteroid) floater2).getShape())){
									((Asteroid) floater1).setColor(new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
									((Asteroid) floater2).setColor(new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
								}
							}
						}*/
					}
					
				}
				//System.out.println(canCollideWithEachOther.size() + " Floaters");
				//System.out.println(nCombinations + " bullet-asteroid combinations made, " + canCollideWithEachOther.size() + " Floaters");
				//System.out.println(nLines + " polygon collision tests occuring");
				//System.out.println("-----");
			}
		}
	}
	private void CleanUp(){
		synchronized(GoodGuys){
			synchronized(Particles){
				ListIterator<Floater> iterator1 = GoodGuys.listIterator();
				/* iterator2 needs to be created later because it depends on what is removed from iterator1. */
				//ListIterator<Particle> particleIter = Particles.listIterator();
				
				while(iterator1.hasNext()){
					Floater floater = iterator1.next();
					if(floater.shouldBeRemoved()){
						iterator1.remove();
						
					}
				}
			}
		}
		
		synchronized (BadGuys) {
			ListIterator<Floater> iterator2 = BadGuys.listIterator();
			while (iterator2.hasNext()) {
				Floater floater = iterator2.next();
				if (floater.shouldBeRemoved()) {
					iterator2.remove();
					if(floater instanceof Asteroid){
						final double SIZE = ((Asteroid) floater).getSize();
						
						/* Create blast particles */
						if(Particles.size() < MAX_PARTICLES){
							for(int nNum = 1; nNum <= SIZE ; nNum++){
								final double CENTER_OFFSET_1 = Math.random() * SIZE - (SIZE / 2);
								final double CENTER_OFFSET_2 = Math.random() * SIZE - (SIZE / 2);
								
								Particle debris = new Particle((int) (floater.getX() + CENTER_OFFSET_1), (int) (floater.getY()+ CENTER_OFFSET_2));
								/* Direct the debris particles in the direction the original floater was traveling in */
								debris.setDirectionX(debris.getDirectionX() + (floater.getDirectionX() * 2.5));
								debris.setDirectionY(debris.getDirectionY() + (floater.getDirectionY() * 2.5));
								Particles.add(debris);
							}
						}
						
						/* Add new asteroids, size and number of which depend on original. */
						if(floater.getSize() > 30){
							/* Pick a number of new asteroids */
							final long lNEW_ASTEROIDS = Math.round(2 + Math.random());
							
							for(int nNum = 1; nNum <= lNEW_ASTEROIDS; nNum++){
								Asteroid aRemains = new Asteroid((int) (floater.getSize() / lNEW_ASTEROIDS));
								aRemains.setX(floater.getX());
								aRemains.setY(floater.getY());
								
								iterator2.add(aRemains);
							}
						}
					}
				}
			}
		}
		
		synchronized (Particles) {
			ListIterator<Particle> particleIter = Particles.listIterator();
			while (particleIter.hasNext()) {
				Particle particle = particleIter.next();
				if (particle.shouldBeRemoved()) {
					particleIter.remove();
				}
			}
		}
	}
	private void ShipMovement(Spaceship ship){
		if(ship.getX() < 9000 && ship.getY() < 9000 && ship.getHyperspace().isActive() == false)
			ship.moveBounded(APPLET_X, APPLET_Y);
		
		if(options.get("Friction").isEnabled() && isPressed("Accelerate") == false && bMouse1Pressed == false){
			/* Friction if not accelerating */
			if(ship.getSpeed() > 0){
				ship.setDirectionX(ship.getDirectionX() * .985);
				ship.setDirectionY(ship.getDirectionY() * .985);
			}
			
			/* Remove any remnant ship speed that friction did not cover */
			if(Math.abs(ship.getDirectionX()) < .25)
				ship.setDirectionX(0);
			if(Math.abs(ship.getDirectionY()) < .25)
				ship.setDirectionY(0);
		}
	}
	/**
	 *	Checks which keys are pressed and depending on which state the game is in, responds accordingly.
	 *	This function is basically the center of all user keyboard input in the game. It maps the EFFECTS
	 *	to the KEY NAMES. It DOES NOT map any of the actual keyboard KEYS. That is done in the init function.
	 *
	 *	Iterates over the pressedKeys array for any keys that match up to the defined controls. 
	 *
	 *	This function will also set all the Pressedkeys KeyHolders "polled" value to true.
	 */
	private void checkKeysAndAct(){
		synchronized(PressedKeys){
			Iterator<KeyHolder> itr = PressedKeys.iterator();
			while(itr.hasNext()){
				KeyHolder checkFor = itr.next();
				//String controlName = controlName(checkFor);
				boolean bPolledAlready = checkFor.getPolled();
				
				/* Handle undefined behavior */
				//if(controlName == null){
				if(controlName(checkFor) == null && checkFor.getKeyText().equals("Left Alt") == false){
//					System.out.println("Undefined key pressed: " + checkFor.getKeyText());
				//	controlName = "";
				}
				
				/* Always listen for these events */
				if(keyIsControlFor(checkFor, "Options") && bPolledAlready == false){
					/* If the options window is not visible, show it. 
					 * Else bring it to the front. */
					if(optionsSelector.isVisible() == false){
						optionsSelector.setVisible(true);
					} else {
						optionsSelector.requestFocus();
					}
					
					/* Special case: Keyholder needs to be removed since another window will be stealing the 
					 * KeyReleased event. */
					itr.remove();
				}
				
				/* All other key effects */
				/* If at intro only listen for "Fire" event */
				if(bAtIntro){
					if(keyIsControlFor(checkFor, "Fire")){
						initGame();
					}
				} else if(Cruzer.getHP() > 0){
					/* Only listen for these events when the ship is not busy with an animation 
					 * like hyperspace etc. */
					if(Cruzer.getHyperspace().isActive() == false){
						if(Cruzer.getZeroLaserManager().isLaserActive() == false){
							if(keyIsControlFor(checkFor, "Accelerate"))
								Cruzer.accelerate(.75);
							else if(keyIsControlFor(checkFor, "Turn around") && bPolledAlready == false)
								Cruzer.setPointDirection(Cruzer.getPointDirection() + 180);
							else if(keyIsControlFor(checkFor, "Roatate Left"))
								Cruzer.setPointDirection((int)Cruzer.getPointDirection()-10);
							else if(keyIsControlFor(checkFor, "Roatate Right"))
								Cruzer.setPointDirection((int)Cruzer.getPointDirection()+10);
							else if(keyIsControlFor(checkFor, "Strafe Left")){
								Cruzer.setPointDirection(Cruzer.getPointDirection() - 90);
								Cruzer.accelerate(1.0);
								Cruzer.setPointDirection(Cruzer.getPointDirection() + 90);
							}
							else if(keyIsControlFor(checkFor, "Strafe Right")){
								Cruzer.setPointDirection(Cruzer.getPointDirection() + 90);
								Cruzer.accelerate(1.0);
								Cruzer.setPointDirection(Cruzer.getPointDirection() - 90);
							}
						}
						if(keyIsControlFor(checkFor, "Fire")){
							if(Cruzer.getBullets() >= 1){
								Cruzer.setBullets(Cruzer.getBullets() - 1);
								final Point2D.Double startPoint = Cruzer.getCannonPoint();
								if(options.get("Precision firing").isEnabled())
									GoodGuys.add(new Bullet(startPoint.x, startPoint.y, Cruzer.getPointDirection()));
								else
									GoodGuys.add(new Bullet(startPoint.x, startPoint.y, Cruzer.getPointDirection() + (10 * Math.random() - 5)));
							}
						} else if(keyIsControlFor(checkFor, "Laser") && bPolledAlready == false && Unlockables.get("Laser").booleanValue() && WeaponTimers.get("Laser").isRunning() == false){
							if(Cruzer.getLaser().isActive() == false){
								Cruzer.getLaser().activate(BadGuys);
								WeaponTimers.get("Laser").start();
							}
						} else if(keyIsControlFor(checkFor, "Pulsar wave") && bPolledAlready == false && Unlockables.get("Pulse").booleanValue() && WeaponTimers.get("Pulse").isRunning() == false){
							final Point2D.Double startPoint = Cruzer.getCannonPoint();
							GoodGuys.add(new Pulsar_wave(startPoint.x, startPoint.y, Cruzer.getPointDirection()));
							WeaponTimers.get("Pulse").start();
						} else if(keyIsControlFor(checkFor, "Repellant") && bPolledAlready == false && Unlockables.get("Repellant").booleanValue() && WeaponTimers.get("Repellant").isRunning() == false){
							if(Cruzer.getRepellant().isActive() == false){
								Cruzer.getRepellant().activate(BadGuys);
								WeaponTimers.get("Repellant").start();
							}
						} else if(keyIsControlFor(checkFor, "Homing Missile") && Unlockables.get("Homing missile").booleanValue()){
							//canCollideWithEachOther.add(new HomingMissile(startPoint.x, startPoint.y, Cruzer.getPointDirection() + 0, new Bullet(nMouseX, nMouseY, 2)));
							if(BadGuys.size() > 0){
								if(bPolledAlready == false){
									GoodGuys.add(new HomingMissileLauncher(GoodGuys, BadGuys));
								}
								
								/* Search for HomingMissileLaunchers that have not launched. Update the positions of the scopes as the user 
								 * holds down the HomingMissile key. Once one is found, draw the scopes for it only. If more are drawn then 
								 * they will just overlap since they are all near the mouse anyway. */
								synchronized(GoodGuys){
									for(Floater MissileLauncher: GoodGuys){
										if(MissileLauncher instanceof HomingMissileLauncher && ((HomingMissileLauncher)MissileLauncher).hasLaunched() == false){
											((HomingMissileLauncher)MissileLauncher).FindClosestFloaters();
											break;
										}
									}
								}
							}
						} else if(keyIsControlFor(checkFor, "Shockwave") && bPolledAlready == false && Unlockables.get("Fan").booleanValue() && WeaponTimers.get("Fan").isRunning() == false){
							GoodGuys.add(new Shockwave(Cruzer));
							WeaponTimers.get("Fan").start();
//						} else if(keyIsControlFor(checkFor, "Bomb") && bPolledAlready == false && Unlockables.get("Bomb").booleanValue() && WeaponTimers.get("Bomb").isRunning() == false){
//							final Point2D.Double startPoint = Cruzer.getCannonPoint();
//							final double VELOCITY = 30 + Math.hypot(Cruzer.getDirectionX(), Cruzer.getDirectionY());
//							final double ANGLE = Math.toRadians(Cruzer.getPointDirection());
//							GoodGuys.add(new Bomb(startPoin t.x, startPoint.y, VELOCITY * Math.cos(ANGLE), VELOCITY * Math.sin(ANGLE)));
						} else if(keyIsControlFor(checkFor, "Chain Lightning") && bPolledAlready == false && Unlockables.get("Chain lightning").booleanValue() && WeaponTimers.get("Chain lightning").isRunning() == false){
							if(Cruzer.getLightningManager().isActive() == false && BadGuys.size() > 0){
								Cruzer.getLightningManager().activate(BadGuys);
								WeaponTimers.get("Chain lightning").start();
							}
						} else if(keyIsControlFor(checkFor, "Reinforcements") && bPolledAlready == false && Unlockables.get("Reinforcements").booleanValue() && WeaponTimers.get("Reinforcements").isRunning() == false){
							if(Cruzer.getReinforcementsManager().isActive() == false){
								Cruzer.getReinforcementsManager().activate(Interface, GoodGuys, BadGuys, WeaponTimers.get("Reinforcements"));
								/* Weapon timer is not started as soon as player presses button, but rather when the reinforcements teleport in. */
							}
						} else if(keyIsControlFor(checkFor, "Zero Laser") && bPolledAlready == false && Unlockables.get("Zero laser").booleanValue() && WeaponTimers.get("Zero laser").isRunning() == false){
							if(Cruzer.getZeroLaserManager().isActive() == false){
								Cruzer.getZeroLaserManager().activate(Interface, BadGuys, imageEffects);
								WeaponTimers.get("Zero laser").start();
							}
						} else if(keyIsControlFor(checkFor, "Hyperspace")){
							if(Cruzer.getHyperspaceCharge() == Cruzer.getHyperspaceChargeLimit() 
							&& Cruzer.getReinforcementsManager().isActive() == false
							&& Cruzer.getZeroLaserManager().isActive() == false){
								Cruzer.getHyperspace().activate(HyperspaceManager.OUT_AND_IN);
								Cruzer.setHyperspaceCharge(0);
							}
						}
					}
					/* Listen for these events during game even if busy with a ship animation. */
					if(keyIsControlFor(checkFor, "Restart")){
						restart();
					}
					
					
					/* Debug functions */
					else if(keyIsControlFor(checkFor, "Suicide")){
						Interface.setNotification(1, "Partially lowered health", Color.red);
						Cruzer.setHP(Cruzer.getHP()-.5);
					}
					else if(keyIsControlFor(checkFor, "DebugMode") && bPolledAlready == false){
						DebugMode(!bDebugModeEnabled);
					}
					else if(keyIsControlFor(checkFor, "Speed multiply")){
						Interface.setNotification(1, "Multiplying speed by 1.1", Color.red);
						Cruzer.setDirectionX(Cruzer.getDirectionX() * 1.1);
						Cruzer.setDirectionY(Cruzer.getDirectionY() * 1.1);
					}
					else if(keyIsControlFor(checkFor, "Reset Colors") && bPolledAlready == false){
						Interface.setNotification(1, "Asteroid colors reset to grey", Color.red);
						Iterator<Floater> iterator = BadGuys.iterator();
						while(iterator.hasNext()){
							iterator.next().setColor(Color.gray);
						}
					}
					else if(keyIsControlFor(checkFor, "Destroy all") && bPolledAlready == false){
						Interface.setNotification(1, "All asteroids destroyed", Color.red);
						Iterator<Floater> iterator = BadGuys.iterator();
						while(iterator.hasNext()){
							Floater floater = iterator.next();
							floater.setHP(0);
						}
					}
					else if(keyIsControlFor(checkFor, "Add asteroids") ){
						Interface.setNotification(1, "Asteroids added", Color.red);
						synchronized (BadGuys) {
							for (int nIndex = 0; nIndex < 1; nIndex++) {
								BadGuys.add(new Asteroid((int) (Math.random() * 30 + 30)));
							}
						}
					}
					else if(keyIsControlFor(checkFor, "Add small asteroids") ){
						Interface.setNotification(1, "Small asteroids added", Color.red);
						synchronized (BadGuys) {
							for (int nIndex = 0; nIndex < 1; nIndex++) {
								BadGuys.add(new Asteroid(5));
							}
						}
					}
//					else if(keyIsControlFor(checkFor, "Post-process") && bPolledAlready == false){
//						Interface.setNotification(1, "Inverting", Color.red);
//					}
				} else { /* dead */
					if(keyIsControlFor(checkFor, "Restart")){
						nDeaths++;
						restart();
					}
				}
				
				/* Set the polled state to true to signify that the KeyHolder has been polled at least once. */
				checkFor.setPolled(true);
			}
		}
	}
	private void checkMouseAndAct(){
		/* If at intro only listen for "Fire" event */
		if(bAtIntro){
			
		} else if(Cruzer.getHP() > 0){
			/* Only listen for these events when the ship is not busy with an animation 
			 * like hyperspace etc. */
			if(Cruzer.getHyperspace().isActive() == false){
				if(bMouse1Pressed){
//					if(Cruzer.getBullets() >= 1){
//						Cruzer.setBullets(Cruzer.getBullets() - 1);
//						final Point2D.Double startPoint = Cruzer.getCannonPoint();
//						GoodGuys.add(new Bullet(startPoint.x, startPoint.y, Cruzer.getPointDirection() + (2 * Math.random() - 1)));
//					}
					if(Cruzer.getZeroLaserManager().isLaserActive() == false){
						Cruzer.accelerate(.75);
					}
				}
				if(bMouse2Pressed && bMouse2Polled == false && bDebugModeEnabled){
					Cruzer.setX(nMouseX);
					Cruzer.setY(nMouseY);
				}
			}
			/* Listen for these events during game even if busy with a ship animation. */
			
			
		} else { /* dead */
			
		}
		
		/* Set polled states to true. Doesn't matter if the effect actually happened. */
		bMouse1Polled = true;
		bMouse2Polled = true;
		bMouse3Polled = true;	
	}
	/**
	 * God mode. 
	 * 
	 * Checks bOn and switches on if off or vice versa.
	 * Effects of debug mode: invincibility on, almost infinite amounts of weapons, infinite life. 
	 */
	private void DebugMode(boolean bOn){
		if(bOn){
			bDebugModeEnabled = true;
			Cruzer.getShield().activate(Animator.INFINITE); /* indefinitely. WILL turn off if triggered somewhere else. */
			Cruzer.setSpecialWeaponCooldown(Cruzer.getSpecialWeaponCooldown()*100.0);
			Cruzer.setBulletRegen(Cruzer.getBulletRegen()*100.0);
			Cruzer.setHPLimit(Cruzer.getHPLimit()*10.0);
			Cruzer.setHP(Cruzer.getHP()*10.0);
			Cruzer.setHPRegen(5);
			Interface.setNotification(1, "Debug mode enabled", Color.red);
			Interface.setNotification(2, "Debug mode enabled", Color.red);
		} else {
			bDebugModeEnabled = false;
			Cruzer.getShield().deactivate();
			Cruzer.setSpecialWeaponCooldown(Cruzer.getSpecialWeaponCooldown()/100.0);
			Cruzer.setBulletRegen(Cruzer.getBulletRegen()/100.0);
			Cruzer.setHPLimit(Cruzer.getHPLimit()/10.0);
			Cruzer.setHP(Cruzer.getHP()/10.0);
			Cruzer.setHPRegen(.2);
			Interface.setNotification(1, "Debug mode disabled", Color.red);
			Interface.setNotification(2, "Debug mode disabled", Color.red);
		}	
	}
	/**
	 * Checks whether or not the KeyHolder is a defined control for a function.
	 * 
	 * @param pressedKey The KeyHolder to check membership for. 
	 * @param sFunction The function.
	 */
	private boolean keyIsControlFor(KeyHolder pressedKey, String sFunction){
		return P1Controls.get(sFunction).contains(pressedKey);
	}
	/**
	 *	This function checks the PressedKeys ArrayList to see if it contains any of the keys 
	 *	defined in ArrayLists for player controls. 
	 *	EDIT: expired when the program needed to know whether a key was polled more than once during one press
	 * 	EDIT2: re-added for other if statements that were not in the checkKeysAndAct() function	(e.g. the paint
	 * 	funciton for the ship) because the other checker function requires much more setup. ALSO, the other function
	 * 	is less useful because you cannot do a switch statement on Strings without an enum. (annoying)
	 * 
	 * @param sFunction The function of ArrayList P1Controls to check for in PressedKeys. (e.g. "Fire", "Accelerate")
	 * @return Whether the key is pressed or not.
	 */
	private boolean isPressed(String sFunction){
		synchronized (PressedKeys) {
			Iterator<KeyHolder> itr = P1Controls.get(sFunction).iterator();
			while (itr.hasNext()) {
				KeyHolder checkFor = itr.next();
				if (PressedKeys.contains(checkFor)) {
					return true;
				}
			}
			return false;
		}
	}
	/**
	 * Does a reverse map lookup on the P1Controls map so as to input a pressed key and 
	 * return the first effect it is responsible for.
	 * 
	 * EDIT: Archived when I wanted the ability for 1 key to have multiple functions. This would only
	 * return the first one. 
	 * EDIT2: Unarchived when I needed this to check for whether the key was undefined.
	 * 
	 * @param pressedKey A pressed key to perform the lookup for.
	 * @return The name of the function the key is responsible for. (e.g. "Fire", "Accelerate")
	 */
	private String controlName(KeyHolder pressedKey){
		/* Create an set of keys for iterating the map */
		Set<Map.Entry<String, ArrayList<KeyHolder>>> mapKeys = P1Controls.entrySet();
		
		/* Iterate through the map and return the function name (key) if pressedKey is found in the 
		 * functions key list (value) */
		for(Map.Entry<String, ArrayList<KeyHolder>> entry: mapKeys){
			if(entry.getValue().contains(pressedKey)){
				return entry.getKey();
			}
		}
		return null;
	}
	public static LinkedHashMap<String, ArrayList<KeyHolder>> getDefaultControls(){
		LinkedHashMap<String, ArrayList<KeyHolder>> defaultControls = new LinkedHashMap<String, ArrayList<KeyHolder>>();
		
		/* Declare all ArrayLists to hold all keys for each effect. These can be changed at runtime. */
		defaultControls.put("Accelerate", new ArrayList<KeyHolder>());
		defaultControls.put("Turn around", new ArrayList<KeyHolder>());
		defaultControls.put("Roatate Left", new ArrayList<KeyHolder>());
		defaultControls.put("Roatate Right", new ArrayList<KeyHolder>());
		defaultControls.put("Strafe Left", new ArrayList<KeyHolder>());
		defaultControls.put("Strafe Right", new ArrayList<KeyHolder>());
		defaultControls.put("Fire", new ArrayList<KeyHolder>());
		defaultControls.put("Hyperspace", new ArrayList<KeyHolder>());
		defaultControls.put("Laser", new ArrayList<KeyHolder>());
		defaultControls.put("Pulsar wave", new ArrayList<KeyHolder>());
		defaultControls.put("Repellant", new ArrayList<KeyHolder>());
		defaultControls.put("Homing Missile", new ArrayList<KeyHolder>());
//		defaultControls.put("Bomb", new ArrayList<KeyHolder>());
		defaultControls.put("Shockwave", new ArrayList<KeyHolder>());
		defaultControls.put("Chain Lightning", new ArrayList<KeyHolder>());
		defaultControls.put("Reinforcements", new ArrayList<KeyHolder>());
		defaultControls.put("Zero Laser", new ArrayList<KeyHolder>());
		defaultControls.put("Restart", new ArrayList<KeyHolder>());
		defaultControls.put("Options", new ArrayList<KeyHolder>());
		defaultControls.put("Weapons monitor", new ArrayList<KeyHolder>());
		/* Debug buttons */
		defaultControls.put("Suicide", new ArrayList<KeyHolder>());
		defaultControls.put("DebugMode", new ArrayList<KeyHolder>());
		defaultControls.put("Speed multiply", new ArrayList<KeyHolder>());
		defaultControls.put("Reset Colors", new ArrayList<KeyHolder>());
		defaultControls.put("Destroy all", new ArrayList<KeyHolder>());
		defaultControls.put("Add asteroids", new ArrayList<KeyHolder>());
		defaultControls.put("Add small asteroids", new ArrayList<KeyHolder>());
//		defaultControls.put("Post-process", new ArrayList<KeyHolder>());
		
		/* Populate ArrayLists with KeyHolders specifying the default controls. There can be more 
		 * than one key for each effect. */
		defaultControls.get("Accelerate").add(new KeyHolder(KeyEvent.VK_UP, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Accelerate").add(new KeyHolder(KeyEvent.VK_W, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Turn around").add(new KeyHolder(KeyEvent.VK_DOWN, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Turn around").add(new KeyHolder(KeyEvent.VK_S, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Roatate Left").add(new KeyHolder(KeyEvent.VK_LEFT, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Roatate Left").add(new KeyHolder(KeyEvent.VK_A, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Roatate Right").add(new KeyHolder(KeyEvent.VK_RIGHT, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Roatate Right").add(new KeyHolder(KeyEvent.VK_D, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Strafe Left").add(new KeyHolder(KeyEvent.VK_Q, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Strafe Right").add(new KeyHolder(KeyEvent.VK_E, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Fire").add(new KeyHolder(KeyEvent.VK_SPACE, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Hyperspace").add(new KeyHolder(KeyEvent.VK_CONTROL, KeyEvent.KEY_LOCATION_LEFT));
		defaultControls.get("Laser").add(new KeyHolder(KeyEvent.VK_1, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Pulsar wave").add(new KeyHolder(KeyEvent.VK_2, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Repellant").add(new KeyHolder(KeyEvent.VK_3, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Homing Missile").add(new KeyHolder(KeyEvent.VK_4, KeyEvent.KEY_LOCATION_STANDARD));
//		defaultControls.get("Bomb").add(new KeyHolder(KeyEvent.VK_5, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Shockwave").add(new KeyHolder(KeyEvent.VK_5, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Chain Lightning").add(new KeyHolder(KeyEvent.VK_6, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Reinforcements").add(new KeyHolder(KeyEvent.VK_7, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Zero Laser").add(new KeyHolder(KeyEvent.VK_9, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Restart").add(new KeyHolder(KeyEvent.VK_F12, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Options").add(new KeyHolder(KeyEvent.VK_F1, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Weapons monitor").add(new KeyHolder(KeyEvent.VK_TAB, KeyEvent.KEY_LOCATION_STANDARD));
		/* Debug buttons */
		defaultControls.get("Suicide").add(new KeyHolder(KeyEvent.VK_F5, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("DebugMode").add(new KeyHolder(KeyEvent.VK_0, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Speed multiply").add(new KeyHolder(KeyEvent.VK_F6, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Reset Colors").add(new KeyHolder(KeyEvent.VK_F7, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Destroy all").add(new KeyHolder(KeyEvent.VK_F8, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Add asteroids").add(new KeyHolder(KeyEvent.VK_F9, KeyEvent.KEY_LOCATION_STANDARD));
		defaultControls.get("Add small asteroids").add(new KeyHolder(KeyEvent.VK_F11, KeyEvent.KEY_LOCATION_STANDARD));
//		defaultControls.get("Post-process").add(new KeyHolder(KeyEvent.VK_F11, KeyEvent.KEY_LOCATION_STANDARD));
	
		return defaultControls;
	}
	public static LinkedHashMap<String, Boolean> getFreshUnlockables(){
		LinkedHashMap<String, Boolean> freshUnlockables = new LinkedHashMap<String, Boolean>();
		
		freshUnlockables.put("Laser", new Boolean("true"));
		freshUnlockables.put("Pulse", new Boolean("true"));
		freshUnlockables.put("Repellant", new Boolean("true"));
		freshUnlockables.put("Homing missile", new Boolean("true"));
		freshUnlockables.put("Fan", new Boolean("true"));
		freshUnlockables.put("Chain lightning", new Boolean("true"));
		freshUnlockables.put("Reinforcements", new Boolean("true"));
		freshUnlockables.put("Zero laser", new Boolean("true"));
		
		/* Debug: unlocks all weapons */
//		for(Map.Entry<String, Boolean> entry: freshUnlockables.entrySet()){
//			entry.setValue(new Boolean("true"));
//		}
		
		return freshUnlockables;
	}
	public void run(){
		while(Thread.currentThread() == animation){
			/* Loop code goes here */
			
			if(bAtIntro){
				/* Really trippy rotation of stars for the intro screen.  */
				StarMovement(dStarDirectionX * Math.cos(System.currentTimeMillis()/20000.0), dStarDirectionY * Math.sin(System.currentTimeMillis()/20000.0));
				
				/* Gradually change star x,y directions to be the same if they are not. 
				 * This makes it so that the stars rotate in a circle, not a flat ellipse. */
				if(Math.abs(dStarDirectionY - dStarDirectionX) > 1){
					dStarDirectionX += .005 * (dStarDirectionY - dStarDirectionX);
				}
				if(Math.abs(dStarDirectionX - dStarDirectionY) > 1){
					dStarDirectionY += .005 * (dStarDirectionX - dStarDirectionY);
				}
				/* Gradually lower the magnitude of star x component and/or y component if too high */
				if(Math.abs(dStarDirectionX) > 8 || Math.abs(dStarDirectionY) > 8){
					dStarDirectionX = .95 * dStarDirectionX;
					dStarDirectionY = .95 * dStarDirectionY;
				}
			} 
			/* else if dead */
			else if(Cruzer.getHP() <= 0){ 
				/* Move stars in opposite direction of ship [debris?] */
				StarMovement(-1 * Cruzer.getDirectionX(), -1 * Cruzer.getDirectionY());
				FloaterMovement(true);
				
				/* Lower speed if too high after death */
				if(Cruzer.getSpeed() > 6){
					Cruzer.setDirectionX(Cruzer.getDirectionX() * .95);
					Cruzer.setDirectionY(Cruzer.getDirectionY() * .95);
				}
			} 
			/* else...in play */
			else { 
				/* Move stars in opposite direction of ship */
				StarMovement(-.01 * Cruzer.getDirectionX(), -.01 * Cruzer.getDirectionY());
				Cruzer.getHyperspace().moveFromShip();
				Cruzer.regenerate();
				FloaterMovement(false);
				
				if(Cruzer.getHyperspace().isActive() == false){
					ShipMovement(Cruzer);
				}
			}
			/* Always do these */
			CleanUp();
			checkKeysAndAct();
			checkMouseAndAct();
			repaint();
			
			
			/* Delay */
			try{
				Thread.sleep(REFRESH_RATE);
			} catch (InterruptedException e){
				break;
			}
		}
		System.out.println("Animation stopped");
	}
	public void update(Graphics g) {
		paint(g);
	}
	public void start() {
		animation = new Thread(this);
		if(animation != null) {
			animation.start();
		}
	}
	public void stop() {
		animation = null;
	}
	public void actionPerformed(ActionEvent aEvent){
		/* If the event source is an checkbox, then change the appropriate option and send a notification. 
		 * This had to be here instead of the OptionsDialog class because it interfaces with several different main objects. */
		if(aEvent.getSource() instanceof JCheckBoxWithOption){
			options.get(aEvent.getActionCommand()).setEnabled(((JCheckBoxWithOption) aEvent.getSource()).isSelected());
			
			/* AHHH!! TERNARY OPERATOR!! See how hard this is to understand? And this is a simple one! */
			Interface.setNotification(1, aEvent.getActionCommand() + " " + (options.get(aEvent.getActionCommand()).isEnabled() ? "en":"dis") + "abled", Color.white);
			
			/** Special cases where additional action is necessary along with the simple boolean reverse */
			if(aEvent.getActionCommand().equals("Anti-aliasing")){
				if(options.get("Anti-aliasing").isEnabled()){
					System.out.println("Anti-alias enabled");
					offscreen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					offscreen.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
					offscreen.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//					offscreen.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//					offscreen.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
				} else {
					offscreen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
					offscreen.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
				}
			}
		}
	}
	public void keyPressed(KeyEvent ke){
		KeyHolder keyH = new KeyHolder(ke.getKeyCode(), ke.getKeyLocation());
		synchronized(PressedKeys){
			if(!PressedKeys.contains(keyH)){
				PressedKeys.add(keyH);
			}
		}
	}
	public void keyReleased(KeyEvent ke){
		KeyHolder keyH = new KeyHolder(ke.getKeyCode(), ke.getKeyLocation());
		synchronized (PressedKeys) {
			if (PressedKeys.contains(keyH)) {
				PressedKeys.remove(keyH);
			}
		}
		/* The homing missile launcher is actually ejected when the key is released */
		if(keyIsControlFor(keyH, "Homing Missile")){
			final Point2D.Double startPoint = Cruzer.getCannonPoint();
			synchronized(GoodGuys){
				for(Floater MissileLauncher: GoodGuys){
					if(MissileLauncher instanceof HomingMissileLauncher && ((HomingMissileLauncher)MissileLauncher).hasLaunched() == false){
						((HomingMissileLauncher)MissileLauncher).launch(startPoint.x, startPoint.y, Cruzer.getPointDirection());
					}
				}
			}
			WeaponTimers.get("Homing missile").start();
		}
	}
	public void keyTyped(KeyEvent ke){}
	public void focusGained(FocusEvent f){}
	public void focusLost(FocusEvent f){
		
	}
	public void mouseClicked(MouseEvent mevent) {}
	public void mouseEntered(MouseEvent mevent) {}
	public void mouseExited(MouseEvent mevent) {}
	public void mousePressed(MouseEvent mevent) {
		switch(mevent.getButton()){
			case MouseEvent.BUTTON1:
				bMouse1Pressed = true;
				bMouse1Polled = false;
				break;
			case MouseEvent.BUTTON2:
				bMouse2Pressed = true;
				bMouse2Polled = false;
				break;
			case MouseEvent.BUTTON3:
				bMouse3Pressed = true;
				bMouse3Polled = false;
				break;
		}
	}
	public void mouseReleased(MouseEvent mevent) {
		switch(mevent.getButton()){
			case MouseEvent.BUTTON1:
				bMouse1Pressed = false;
				bMouse1Polled = true;
				break;
			case MouseEvent.BUTTON2:
				bMouse2Pressed = false;
				bMouse2Polled = true;
				break;
			case MouseEvent.BUTTON3:
				bMouse3Pressed = false;
				bMouse3Polled = true;
				break;
		}
	}
	public void mouseDragged(MouseEvent mevent) {
		mouseMoved(mevent);
	}
	public void mouseMoved(MouseEvent mevent) {
		nMouseX = mevent.getX();
		nMouseY = mevent.getY();
		
		if(bPaused == false){
			double dMouseAngleDegrees = Math.toDegrees(Math.atan2(mevent.getY() - Cruzer.getY(), mevent.getX() - Cruzer.getX()));
			if(Cruzer.getZeroLaserManager().isLaserActive() == false){
				Cruzer.setPointDirection(dMouseAngleDegrees);
			}
		}
	}
	public static int getDeaths(){return nDeaths;}
	public static int getScore(){return nScore;}
	public static int getMouseX(){return nMouseX;}
	public static int getMouseY(){return nMouseY;}
	public static boolean DebugModeEnabled(){return bDebugModeEnabled;}
}