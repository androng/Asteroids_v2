import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.TimerTask;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.Animator.RepeatBehavior;

public class ShieldManager implements TimingTarget{
	/* These specify parameters for the animation like frequency, color etc. */
	final static int nINITIAL_RADIUS = 50; 
	final static int nFREQUENCY = 500; /* in milliseconds per period. i.e. one revolution. for ONE satellite.  */
	final static int nINITIAL_THICKNESS_OF_RING = 1;
	final static Color SATELLITE_COLOR = new Color(0,0,255);  /* Do NOT adjust alpha here, it be totally ignored. */
	
	/* This is for timing*/
	final static long INDIVIDUAL_SATELLITE_DURATION = 3000;
	
	/** The duration of invincibility */
	private long lDuration;
	/** The time at which the satellite animation was started. getTotalElapsedTime() was buggy.  */
	private long animationStartTime;
	/** The number of active satellites */
	private int nActiveSatellites;
	/** The ProgressTimer used to track how long the shield has until deactivation. */
	//private ProgressTimer ptInvincibilityTimer;
	/** Acts as a timer for the generation of new satellite images. In this object, it does 
	 * not animate anything. I didn't use a timer because they are not nearly as easy to use as 
	 * animators. */
	private Animator satelliteTimer;
	/** The ship variable, used to determine where the Shield is drawn. */
	private Spaceship Ship;
	/** Unrotated image holder for satellites so that they don't have to be rendered every frame.  */
	private BufferedImage imgSatellites;
	
	ShieldManager(Spaceship Ship){
		imgSatellites = new BufferedImage(300, 300, BufferedImage.TRANSLUCENT);
		this.Ship = Ship;
		satelliteTimer = new Animator(0);
	}
	
	public void activate(long lNewDuration){
//		new Exception().printStackTrace();
		
		if(lNewDuration == 0)
			return;
		
		/* If shield is active, deactivate, or else it will keep sending events even though it has no handle */
		if(isActive()){
			deactivate();
		}
		lDuration = lNewDuration;
		if(lNewDuration != Animator.INFINITE){
			final double REPEAT_COUNT = Math.ceil((double)lNewDuration / INDIVIDUAL_SATELLITE_DURATION);
			if(REPEAT_COUNT > 0)
				satelliteTimer = new Animator((int) INDIVIDUAL_SATELLITE_DURATION, REPEAT_COUNT, RepeatBehavior.LOOP, this);
			else
				satelliteTimer = new Animator((int) INDIVIDUAL_SATELLITE_DURATION, this);
			
			satelliteTimer.setStartFraction((1 - ((float)lNewDuration % INDIVIDUAL_SATELLITE_DURATION / INDIVIDUAL_SATELLITE_DURATION)) % 1);
			
		} else {
			satelliteTimer = new Animator(Animator.INFINITE, this);
		}
		
		animationStartTime = System.currentTimeMillis();
		satelliteTimer.start();
	}
	public void append(long lAddedDuration){
		long lOldDuration = 0;
		
		/* If shield is active, save the old duration and merge with new duration */
		if(isActive()){
			lOldDuration = getRemainingDuration();
			deactivate();
		}
		lAddedDuration += lOldDuration;
		
		/* If old duration was infinite, set to infinite. */
		if(lOldDuration == Animator.INFINITE){
			lAddedDuration = Animator.INFINITE;
		}
		activate(lAddedDuration);
	}
	public void deactivate(){
		satelliteTimer.stop();
	}
	public boolean isActive(){return satelliteTimer.isRunning();}
	public long getRemainingDuration(){
		if(satelliteTimer.getDuration() == Animator.INFINITE)
			return Animator.INFINITE;
		
//		long remainder = lDuration - satelliteTimer.getTotalElapsedTime(); // <-- getTotalElapsedTime() didn't work as expected.
		long remainder = lDuration - (System.currentTimeMillis() - animationStartTime);
		if(remainder < 0)
			return 0;
		return remainder;
	}
	public void draw(Graphics2D g){
		/* Makes an animation around the ship if it is in invincible mode */
		if(isActive()){	
			
			/* Makes the rotation speed the same for any number of satellites */
			//final double ROTATION_ANGLE = -Math.toRadians(Asteroids_v2.modulus(360, nRotationSpeed));
			/* Makes full rotation speed only apply when there is ONE satellite, proportionally slower if there is more than one. */
			final double ROTATION_ANGLE = -Math.toRadians(Util.modulus(360, (int)(nFREQUENCY * (nActiveSatellites))));
			
			BufferedImage dstImg = new BufferedImage(imgSatellites.getWidth(), imgSatellites.getHeight(), BufferedImage.TRANSLUCENT);
			AffineTransformOp op = new AffineTransformOp(AffineTransform.getRotateInstance(ROTATION_ANGLE, imgSatellites.getWidth()/2, imgSatellites.getWidth()/2), null);
			op.filter(imgSatellites, dstImg);
			/* Copy rotated image to visible image */
			g.drawImage(dstImg, Ship.getX()-(dstImg.getWidth()/2), Ship.getY()-(dstImg.getHeight()/2), null);
		
			/* DEBUG: Draw non-rotated image instead. */
//			g.drawImage(imgSatellites, Ship.getX()-(dstImg.getWidth()/2), Ship.getY()-(dstImg.getHeight()/2), null);
		
		}
	}
	/**
	 * Generates an image of shield satellites centered at (0, 0). Rotation is left to the calling method.
	 * 
	 * @param nSatellites The number of satellites to incorporate into the image.
	 */
	private BufferedImage generateSatellites(int nSatellites){
		/* Initialize a transparent image and a drawing canvas. */
		BufferedImage image = new BufferedImage(imgSatellites.getWidth(), imgSatellites.getHeight(), BufferedImage.TRANSLUCENT);
		Graphics2D g = (Graphics2D)image.getGraphics();
		
		/* Radius as a negative exponential function of the number of satellites with asymptote at 100 (reaches 99 at 7.5 or so) */
		final int nRADIUS = nINITIAL_RADIUS + (int)(100.0 * (-Math.pow(2.0, (-nSatellites + 1)/2.0 ) + 1)); 
		
		final int nTHICKNESS_OF_RING = nINITIAL_THICKNESS_OF_RING;
		
		for(int nSatellite = 0; nSatellite < nSatellites; nSatellite++){
			for(int nNum = (360/nSatellites)*nSatellite; nNum < (360/nSatellites)*(nSatellite+1); nNum++){
//				g.setColor(new Color((int)(SATELLITE_COLOR.getRed()*((double)(nNum%(360/nSatellites))/(360/nSatellites)))
//									,(int)(SATELLITE_COLOR.getGreen()*((double)(nNum%(360/nSatellites))/(360/nSatellites)))
//									,(int)(SATELLITE_COLOR.getBlue()*((double)(nNum%(360/nSatellites))/(360/nSatellites)))));
				g.setColor(new Color(SATELLITE_COLOR.getRed(), SATELLITE_COLOR.getGreen(), SATELLITE_COLOR.getBlue(),
								(int)(SATELLITE_COLOR.getAlpha()*((double)(nNum%(360/nSatellites))/(360/nSatellites)))));
				for(int nThickness = 0; nThickness <= nTHICKNESS_OF_RING;nThickness++)
					g.drawArc(imgSatellites.getWidth()/2 - ((nRADIUS+(2*nThickness))/2),imgSatellites.getHeight()/2 - ((nRADIUS+(2*nThickness))/2),nRADIUS+(2*nThickness),nRADIUS+(2*nThickness),nNum,2);
			}
		}
		return image;
	}
	public void begin() {
		/* If the duration is infinite, then only draw a certain number of satellites. 
		 * else just generate an image based on how much time is left.  */
		if(lDuration == Animator.INFINITE){
			imgSatellites = generateSatellites(5);
			nActiveSatellites = 5;
		} else {
			nActiveSatellites = (int)Math.ceil((double)lDuration / INDIVIDUAL_SATELLITE_DURATION);
			imgSatellites = generateSatellites(nActiveSatellites);
		}
	}
	public void end() {
//		lDuration = 0;
	}
	public void repeat() {
		nActiveSatellites--;
		imgSatellites = generateSatellites(nActiveSatellites);
	}
	public void timingEvent(float fraction) {}
}
