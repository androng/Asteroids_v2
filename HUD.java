import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.jdesktop.animation.timing.Animator;

public class HUD {
	private List<Notification> naNotice1;
	private Notification Notice2;
	private BufferedImage dualWhiteSpectrum;
	private BufferedImage WhiteSpectrum;
	private BufferedImage shieldSatellites;
	private BufferedImage[] sprWeapons;
	private final int ICON_PADDING = 5;
	/** This a member variable so that it doesn't have to be initialized each frame. */
	private BufferedImage lowerHUDCanvas;
	final boolean NEW_NOTIFICATIONS_ON_TOP = false;
	
	HUD(){
		naNotice1 = Collections.synchronizedList(new LinkedList<Notification>());
		Notice2 = null; /* will be modified later */
		dualWhiteSpectrum = DrawSmoothWhiteGradient(300, 10, true);
		WhiteSpectrum = DrawSmoothWhiteGradient(255, 10, false);
		lowerHUDCanvas = new BufferedImage(Asteroids_v2.APPLET_X, 100, BufferedImage.TRANSLUCENT);
		
		/* Attempt to load sprite */
		BufferedImage weaponStrip = null;
		try {
			weaponStrip = ImageIO.read(new File("icons.png"));
			//imgMol = getImage(getCodeBase(), hSelected.getImgName());
		} catch (IOException e) {
			System.out.println("Weapon icons not loaded.");
			e.printStackTrace();
		}
		/** Scale down and split into separate images*/
		sprWeapons = null;
		if(weaponStrip != null){
			/** Scale down so that all 8 icons fit on the screen. (gaps + padding) Height is scaled by the same factor as the width.  */
			final int NEW_WIDTH = Asteroids_v2.APPLET_X - (7 * ICON_PADDING) - 100;
			final int NEW_HEIGHT = (int)Math.round((double)NEW_WIDTH / weaponStrip.getWidth() * weaponStrip.getHeight());
			
			/* If the window is small enough to warrant scaling in the first place... */
			if(NEW_WIDTH < weaponStrip.getWidth()){
				Image weaponStrip2 = weaponStrip.getScaledInstance(NEW_WIDTH, NEW_HEIGHT, Image.SCALE_SMOOTH);
				/* It is necessary to clear weaponStrip and create a new one because you cannot convert BufferedImage to Image by casting.
				 * It has to be done with graphics. */
				weaponStrip = new BufferedImage(NEW_WIDTH, NEW_HEIGHT, BufferedImage.TYPE_INT_RGB);
				weaponStrip.getGraphics().drawImage(weaponStrip2, 0, 0, null);
			}
			
			/* Split into separate images */
			sprWeapons = new BufferedImage[8];
			final int ICON_LENGTH = (int)Math.round((double)weaponStrip.getWidth() / sprWeapons.length);
			int nIndex = 0;
			for(BufferedImage weaponImage: sprWeapons){
				try{
					sprWeapons[nIndex] = weaponStrip.getSubimage(nIndex * ICON_LENGTH, 0, ICON_LENGTH, weaponStrip.getHeight());
				} catch(RasterFormatException e){
					sprWeapons[nIndex] = weaponStrip.getSubimage(nIndex * ICON_LENGTH - 1, 0, ICON_LENGTH, weaponStrip.getHeight());
				}
				nIndex++;
			}
		}
	}
	/**
	 * Umbrella method for the 4-parameter setNotification(...). Sets default values for the duration 
	 * of the notifications.
	 */
	public void setNotification(int area, String sNotification, Color cNoticeColor){
		if(area == 1){
			setNotification(area, sNotification, cNoticeColor, 2000);
		} else if(area == 2){
			setNotification(area, sNotification, cNoticeColor, 10000);
		} else {
			throw new IllegalArgumentException("Invalid area number");
		}
	}
	public void setNotification(int area, String sNotification, Color cNoticeColor, long lDuration){
		if(area == 1){
			naNotice1.add(new Notification(area, sNotification, cNoticeColor, lDuration));
		} else if(area == 2){
			if(Notice2 != null){
				clearNotification(area, null);
			}
			Notice2 = new Notification(area, sNotification, cNoticeColor, lDuration);
		} else {
			throw new IllegalArgumentException("Invalid area number");
		}
	}
	public void clearNotification(int area, Notification note){
		if(area == 1 && naNotice1.isEmpty() == false){
			final int nIndex = naNotice1.indexOf(note);
			if(nIndex != -1){
				naNotice1.get(nIndex).clear();
				naNotice1.remove(nIndex);
			}
		} else if(area == 2 && Notice2 != null){
			Notice2.clear();
			Notice2 = null;
		} else if (area != 1 && area != 2){
			throw new IllegalArgumentException("Invalid area number");
		}
	}
	/**
	 * The Notification class is used to store the notifications that appear in the interface. 
	 * The only reason it is not in its own file is because it is dependent on NotificationClearTimerTask.
	 */
	class Notification{
		private int area;
		private String sNotice;
		private Color cNoticeColor;
		private long lDuration;
		private ProgressTimer NoticeTimer;
		
		public Notification(int area, String sNotice, Color cNoticeColor, long lDuration){
			this.area = area;
			this.sNotice = sNotice;
			this.cNoticeColor = cNoticeColor;
			this.lDuration = lDuration;
			this.NoticeTimer = new ProgressTimer(new NotificationClearTimerTask(area, this), lDuration);
		}
		public void clear(){
			this.NoticeTimer.cancel();
			this.NoticeTimer.purge();
		}
		public int getArea() {return area;}
		public String getNotice() {return sNotice;}
		public Color getColor() {return cNoticeColor;}
		public long getDuration() {return lDuration;}
		public ProgressTimer getTimer() {return NoticeTimer;}
	}
	class NotificationClearTimerTask extends TimerTask{
		int nArea;
		Notification note;
		
		public NotificationClearTimerTask(int Area, Notification notification){
			nArea = Area;
			note = notification;
		}
		@Override
		public void run() {
			clearNotification(nArea, note);
		}
	}
	/**
	 * Draws the notifications on the top of the window. Also doubles as a checker for timers that are past
	 * their duration, because sometimes the TimerTask does not do its job of removing its Timer from the 
	 * notice list.
	 * 
	 * @param g A Graphics2D object to draw on.
	 */
	public void DrawUpperNotice(Graphics2D g){
		if(naNotice1.isEmpty() == false){
			Font font = new Font("Dialog", Font.PLAIN, 12);
			g.setFont(font);
			
			synchronized(naNotice1){
				ListIterator<Notification> noticeIterator = naNotice1.listIterator();
				
				/* If new notifications should be drawn on top, forward the LinkedList iterator for rewinding later. */
				if(NEW_NOTIFICATIONS_ON_TOP){
					while(noticeIterator.hasNext()){
						noticeIterator.next();	
					}
				}
				int nStringY = 0;
				while((noticeIterator.hasNext() && NEW_NOTIFICATIONS_ON_TOP == false)
					||(noticeIterator.hasPrevious() && NEW_NOTIFICATIONS_ON_TOP == true)){
					Notification nNotice1;
					
					/* If new notifications should be drawn on top, then process them in reverse order.
						else draw them in forward order. */
					if(NEW_NOTIFICATIONS_ON_TOP){
						nNotice1 = noticeIterator.previous();
					} else {
						nNotice1 = noticeIterator.next();
					}
					
					/* Set the color and lower the alpha if notification timer has less than a second to go. */
					int nAlpha = 255;
					if(nNotice1.getTimer().timeToGo() < 1000 && nNotice1.getTimer().timeToGo() > 0){
						nAlpha = (int)(255 * (nNotice1.getTimer().timeToGo() / 1000.0));
					} else if (nNotice1.getTimer().timeToGo() < 0){ 
						/* Timer is past its duration and didn't get removed by the TimerTask, so remove it here,
							where it would cause a problem anyway. */
//						noticeIterator.remove();
//						nAlpha = 0;
					}
					
					/* Draw the notification if it is not too low */
					if(nStringY < Asteroids_v2.APPLET_Y - 150){
						g.setColor(new Color(nNotice1.getColor().getRed(), nNotice1.getColor().getGreen(), nNotice1.getColor().getBlue(), nAlpha));
						String s1 = new String(nNotice1.getNotice());
						int nStringX = (int)(Asteroids_v2.APPLET_X / 2 - ((double)g.getFontMetrics().stringWidth(s1) / 2));
						
						g.drawString(s1, nStringX, 50 + nStringY );
						
						nStringY += 5 + g.getFontMetrics().getAscent();
					}
				}
			}
		}
	}
	/**
	 * Draws a minimap showing the location of all objects that can collide with the ship.
	 * 
	 * @param nX X coordinate of upper-left corner
	 * @param nY Y coordinate of upper-left corner
	 * @param original Graphics object to draw on
	 * @param canCollideWithShip List of objects that can collide with ship
	 */
//	public void DrawMap(int nX, int nY, Graphics2D original, Spaceship Cruzer, List<Floater> canCollideWithShip){
//		/* Transparency value. Ranges from 1.0 to 0.0, will be less when covers ship. */
//		float fTransparency;
//		if(Cruzer.getX() > nX && Cruzer.getX() < nX + 100 
//		&& Cruzer.getY() > nY && Cruzer.getY() < nY + 100){
//			fTransparency = (float)0.2;
//		} else {
//			fTransparency = (float)1.0;
//		}
//		/* We draw the interface to another image so that we can easily change the transparency of the entire map. */
//		BufferedImage image2 = new BufferedImage(101, 101, BufferedImage.TRANSLUCENT);
//		image2.createGraphics();
//		Graphics2D g = (Graphics2D)image2.getGraphics();
//		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fTransparency));
//		
//		g.setColor(Color.white);
//		g.drawRect(0, 0, 100, 100);
//		
//		/* Copy image2 to visible image */
//		original.drawImage(image2, nX, nY, null);
//	}
	public void DrawDashboard(Graphics2D original, Spaceship Cruzer, LinkedHashMap<String, Boolean> Unlockables, LinkedHashMap<String, Animator> weaponTimers){
		/* Clear the image so that all pixels have near zero alpha */
		Graphics2D g = (Graphics2D)lowerHUDCanvas.getGraphics();
//		g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, .25f));
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, 1.0f));
		g.fillRect(0, 0, lowerHUDCanvas.getWidth(), lowerHUDCanvas.getHeight());
		
		/* Transparency value. Ranges from 1.0 to 0.0, will be less when dashboard covers ship. */
		float fTransparency;
		if(Cruzer.getY() > Asteroids_v2.APPLET_Y - 100 && Cruzer.getY() < Asteroids_v2.APPLET_Y){
			fTransparency = 0.2f;
		} else {
			fTransparency = 1.0f;
		}
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fTransparency));
		
		
		int nHeight = 10;	/* Height of each bar */
		int nWidth = 300;	/* Width of each bar */
		/* dRatio will be used as THE ratio variable to make code reading easier. */
		double dRatio;
		int nDualSpectrumX = (int) Util.modulus(2 * nWidth, 5000) - nWidth;
		int nRegenerationX;
		
		/* Speed monitor */
		/* Here there is a 5% increase in it to prevent the constant fluctuating when it reaches its maximum. 
		 * EDIT: The increase has been removed. */
		g.setColor(Color.white);
		g.drawRect(10, nHeight + 0, nWidth, nHeight);
		//dRatio = Cruzer.getSpeed()/Cruzer.getSpeedLimit() * 1.05;
		dRatio = Cruzer.getSpeed() / 30.0;
		Color cMeterColor;
		if(dRatio <= .9){
			/* Variates from yellow to orange */
			cMeterColor = new Color(255, 255 - (int)(255 * dRatio), 0);
		} else {
			/* Variates from red to black */
			cMeterColor = new Color((int)(Math.abs(Util.modulus(255, 2000) - 255)), 0, 0);
			/* Sends a notification every 6 seconds */
			if(Util.modulus(10, 3000) > 9.5)
				setNotification(2, "WARNING: Velocity at dangerous level!!", Color.red);
		}
		g.setClip(getSegmentedClippingArea(10, nHeight + 0, nWidth , nHeight, dRatio, 1 ));
		DrawMeter(g, cMeterColor, /*dRatio, */10, 10, nWidth, nHeight);
		g.setClip(null);
		
		/* HP bar */
		dRatio = Cruzer.getHP()/Cruzer.getHPLimit();
		/* if HP is below 25%, then draw the outlining rectangle in red. else draw in white. */
		if(dRatio <= .25){ 
			g.setColor(new Color(255,(int)(Util.modulus(255, 2000)),(int)(Util.modulus(255, 2000))));
		} else {
			g.setColor(Color.white);
		}
		g.drawRect(10, nHeight + 15, nWidth, nHeight);
		g.setClip(getSegmentedClippingArea(10, nHeight + 15, nWidth , nHeight, Cruzer.getHP(), Cruzer.getHPLimit()));
		DrawMeter(g, new Color(255, 0, 0), /*dRatio, */10, 25, nWidth, nHeight);
		g.clipRect(10, 25, (int)(nWidth * dRatio), nHeight);
		/* if HP is full, draw smooth white gradient. else if it is regenerating, draw regen animation, else just draw a bar. */
		if(dRatio == 1){
			g.drawImage(dualWhiteSpectrum, 10 + nDualSpectrumX, 25, null);
		} else if (Cruzer.getHPRegen() != 0){
			nRegenerationX = (int)(Util.modulus(2 * (nWidth * dRatio), (int) (250 / (Cruzer.getHPRegen() * 50 / Cruzer.getHPLimit()))) - (nWidth * dRatio));
			AffineTransform transformation = new AffineTransform();
			transformation.translate(nRegenerationX, 25);
			transformation.scale(dRatio, 1);
			g.drawImage(WhiteSpectrum, transformation, null);
		}
		g.setClip(null);
		
		/* Bullet monitor */
		g.setColor(Color.white);
		g.drawRect(10, nHeight + 30, nWidth, nHeight);
		dRatio = Cruzer.getBullets()/Cruzer.getBulletLimit();
		g.setClip(getSegmentedClippingArea(10, nHeight + 30, nWidth , nHeight, Cruzer.getBullets(), Cruzer.getBulletLimit()));
		DrawMeter(g, new Color(0, 255, 0), /*dRatio, */10, nHeight + 30, nWidth, nHeight);
		//g.clipRect(10, nHeight + 30, (int)(nWidth * dRatio), nHeight);
		/* if Bullet meter is full, draw smooth white gradient. else if it is regenerating, draw regen animation, else just draw a bar. */
		if(dRatio == 1){
			g.drawImage(dualWhiteSpectrum, 10 + nDualSpectrumX, nHeight + 30, null);
		} else if (Cruzer.getHPRegen() != 0){
			nRegenerationX = (int)(Util.modulus(2 * (nWidth * dRatio), (int) (250 / (Cruzer.getBulletRegen() * 50 / Cruzer.getBulletLimit()))) - (nWidth * dRatio));
			AffineTransform transformation = new AffineTransform();
			transformation.translate(nRegenerationX, nHeight + 30);
			transformation.scale(dRatio, 1);
			g.drawImage(WhiteSpectrum, transformation, null);
		}
		g.setClip(null);
		
		/* SpecialWeapon monitor */
		g.setColor(Color.white);
		g.drawRect(10, nHeight + 45, nWidth, nHeight);
		dRatio = Cruzer.getSpecialWeaponCharge()/Cruzer.getSpecialWeaponChargeLimit();
		g.setClip(getSegmentedClippingArea(10, nHeight + 45, nWidth , nHeight, Cruzer.getSpecialWeaponCharge(), Cruzer.getSpecialWeaponChargeLimit()));
		DrawMeter(g, new Color(0, 128, 0), /*dRatio, */10, (2*nHeight) + 35, nWidth, nHeight);
		//g.clipRect(10, (2*nHeight) + 35, (int)(nWidth * dRatio), nHeight);
		/* if Special Weapon meter is full, draw smooth white gradient. else if it is regenerating, draw regen animation, else just draw a bar. */
		if(dRatio == 1){
			g.drawImage(dualWhiteSpectrum, 10 + nDualSpectrumX, 2 * nHeight + 35, null);
		} else if (Cruzer.getHPRegen() != 0){
			nRegenerationX = (int)(Util.modulus(2 * (nWidth * dRatio), (int) (250 / (Cruzer.getSpecialWeaponCooldown() * 50 / Cruzer.getSpecialWeaponChargeLimit()))) - (nWidth * dRatio));
			AffineTransform transformation = new AffineTransform();
			transformation.translate(nRegenerationX, 2 * nHeight + 35);
			transformation.scale(dRatio, 1);
			g.drawImage(WhiteSpectrum, transformation, null);
		}
		g.setClip(null);
		
		/* Hyperspace monitor */
		g.setColor(Color.white);
		g.drawRect(10, nHeight + 60, nWidth, nHeight);
		dRatio = Cruzer.getHyperspaceCharge()/Cruzer.getHyperspaceChargeLimit();
		g.setClip(getSegmentedClippingArea(10, nHeight + 60, nWidth , nHeight, Cruzer.getHyperspaceCharge(), Cruzer.getHyperspaceChargeLimit()));
		DrawMeter(g, new Color(0, 128, 255), /*dRatio, */10, (3*nHeight) + 40, nWidth, nHeight);
		//g.clipRect(10, (3*nHeight) + 40, (int)(nWidth * dRatio), nHeight);
		/* if Hyperspace meter is full, draw smooth white gradient. else if it is regenerating, draw regen animation, else just draw a bar. */
		if(dRatio == 1){
			g.drawImage(dualWhiteSpectrum, 10 + nDualSpectrumX, 3*nHeight + 40, null);
		} else if (Cruzer.getHPRegen() != 0){
			nRegenerationX = (int)(Util.modulus(2 * (nWidth * dRatio), (int) (250 / (Cruzer.getHyperspaceCooldown() * 50 / Cruzer.getHyperspaceChargeLimit()))) - (nWidth * dRatio));
			AffineTransform transformation = new AffineTransform();
			transformation.translate(nRegenerationX, 3 * nHeight + 40);
			transformation.scale(dRatio, 1);
			g.drawImage(WhiteSpectrum, transformation, null);
		}
		g.setClip(null);
		
		/* Strings */
		g.setColor(Color.white);
		g.drawString("Velocity",315,20);
		g.drawString("Shields",315,35);
		g.drawString("Primary artillery",315,50);
		g.drawString("Special weapons",315,65);
		g.drawString("Hyperdrive",315,80);
		g.drawString("Score: "+Asteroids_v2.getScore(),450,35);
		if(Asteroids_v2.DebugModeEnabled()){
			//g.setColor(Color.red);
			g.drawString("Debug mode on",550,35);
		}
		if(Asteroids_v2.getDeaths() == 1){
			g.setColor(Color.white);
			g.drawString("You have died "+Asteroids_v2.getDeaths()+" time",650,35);
		}
		else if(Asteroids_v2.getDeaths() > 1){
			g.setColor(Color.white);
			g.drawString("You have died "+Asteroids_v2.getDeaths()+" times",650,35);
		}
		if(Notice2 != null){
			try {
				Font font = new Font("Dialog", Font.PLAIN, 16);
				g.setFont(font);
				
				/* Set the color and lower the alpha if notification timer has less than a second to go. */
				int nAlpha = 255;
				if(Notice2.getTimer().timeToGo() < 1000 && Notice2.getTimer().timeToGo() > 0){
					nAlpha = (int)(255 * (Notice2.getTimer().timeToGo() / 1000.0));
				}
				g.setColor(new Color(Notice2.getColor().getRed(), Notice2.getColor().getGreen(), Notice2.getColor().getBlue(), nAlpha));
				
				g.drawString(Notice2.getNotice(), 450, 50 + (font.getSize() / 3));
			} catch (NullPointerException e) {
				System.out.println("External thread removed Notice2 after null check.");
			}
			
			/* This shows all fonts the system has available. */
			/* Font[] faSystemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
			for(Font fFont : faSystemFonts){
				System.out.println(fFont.getFontName());
			}*/
		}
		
		/* Weapon monitor. Iterate through each of the weapon timers and display the cooldown monitor. */
		final int WIDTH = 300;
		int nYPosition = 10;
		
		for(Map.Entry<String, Animator> entry: weaponTimers.entrySet()){
			/* Only draw the meter if the weapon is unlocked in the first place. */
			if(Unlockables.get(entry.getKey()).equals(new Boolean("true"))){
				Animator cooldownTimer = entry.getValue();
				
				/* If the timer is not running [then it is not cooling down], then draw a full meter. */
				if(cooldownTimer.isRunning() == false){
					g.setColor(new Color(172, 196, 215));
					g.fillRect(Asteroids_v2.APPLET_X - 10 - WIDTH, nYPosition, WIDTH, 5);
				} else {
					/** The amount that we want to "cut off" the bar and shift it by is the amount of time that has passed on the timer.  */
					final double nCutoffRatio = (double)cooldownTimer.getCycleElapsedTime() / cooldownTimer.getDuration();
					
					g.setColor(new Color(100, 145, 181));
					g.fillRect(Asteroids_v2.APPLET_X - 10 - (int)Math.round(nCutoffRatio * WIDTH), nYPosition, (int)Math.round(nCutoffRatio * WIDTH), 5);
				}
				nYPosition += 10;
			}
		}
		
		/* Copy image2 to visible image */
		original.drawImage(lowerHUDCanvas, 0, Asteroids_v2.APPLET_Y - 100, null);
	}
	public void drawWeaponMonitor(Graphics2D g, LinkedHashMap<String, Boolean> Unlockables, LinkedHashMap<String, Animator> weaponTimers, int nFloaters){
		if(sprWeapons != null){
			final int ICON_LENGTH = sprWeapons[0].getWidth();
			final int ICON_HEIGHT = sprWeapons[0].getHeight();
			int nXPosition = Asteroids_v2.APPLET_X / 2;
			/* For each unlocked weapon, there will be one icon, and the start X position of the entire 
			 * set of icons is moved to the left by one half of an icon length + spacing. */
			for(Boolean weaponUnlocked: Unlockables.values()){
				if(weaponUnlocked){
					nXPosition -= (ICON_LENGTH + ICON_PADDING) / 2;
				}
			}
			int nIndex = 0;
			for(String weapon: Unlockables.keySet()){
				if(Unlockables.get(weapon)){
					/** Draw the icon and the cooldown timer below it */
					g.drawImage(sprWeapons[nIndex], nXPosition, Asteroids_v2.APPLET_Y / 2 - ICON_HEIGHT - ICON_PADDING, null);
					
					int nMeterHeight = 10;
					/* If the weapon is the reinforcements meter, draw one blue meter and one half-size regular meter */
					if(weapon.equals("Reinforcements")){
						nMeterHeight /= 2;
						final double ENEMY_RATIO = Math.min(1.0, (double)nFloaters / ReinforcementsManager.ENTRY_THRESHOLD);
						if(ENEMY_RATIO == 1.0){
							g.setColor(Color.green);
						} else {
							g.setColor(new Color(255,128, 0));
						}
						g.fillRect(nXPosition, Asteroids_v2.APPLET_Y / 2 + nMeterHeight, (int)Math.round(ICON_LENGTH * ENEMY_RATIO), nMeterHeight);
					}
					/* If the timer is not running [then it is not cooling down], then draw a full meter. */
					if(weaponTimers.get(weapon).isRunning() == false){
						g.setColor(new Color(  0,128, 0));
						g.fillRect(nXPosition, Asteroids_v2.APPLET_Y / 2, ICON_LENGTH, nMeterHeight);
					} else {
						final double COOLDOWN_RATIO = (double)weaponTimers.get(weapon).getCycleElapsedTime() / weaponTimers.get(weapon).getDuration();
						
						g.setColor(new Color(128, 0, 0));
						g.fillRect(nXPosition, Asteroids_v2.APPLET_Y / 2, (int)Math.round(ICON_LENGTH * COOLDOWN_RATIO), nMeterHeight);
					}
					
					nXPosition += ICON_LENGTH + ICON_PADDING;
					nIndex++;
				}
			}
		}
		
	}
	/**
	 * Draws a long colored bar, length depending on ratio given.
	 * 
	 * @param g	The image to draw on.
	 * @param meterColor	The color of the meter.
	 * @param outlineColor	The color of the outline.
	 * @param dRatio	How much of the bar is filled with color.
	 * @param nX	X location.
	 * @param nY	Y location.
	 * @param nWidth	The width of the bar.
	 * @param nHeight	The height of the bar. 
	 */
	private static void DrawMeter(Graphics g, Color meterColor, /*Color outlineColor, double dRatio,*/ int nX, int nY , int nWidth, int nHeight){
		g.setColor(meterColor); 
		
		/* The ratio cannot be more than 1.0 for the purpose of drawing this meter. */
//		if(dRatio > 1.0){
//			dRatio = 1.0;
//		}
//		g.fillRect(nX,nY,(int)(nWidth*dRatio),nHeight);
		g.fillRect(nX,nY,(int)(nWidth),nHeight);
		
		/* Draw the outlining rectangle. */
//		g.setColor(outlineColor);
//		g.drawRect(nX, nY, nWidth, nHeight);
	}
	private static Polygon getSegmentedClippingArea(int nX, int nY , int nWidth, int nHeight, double dQuantity, double dMaxQuantity){
		/** The border thickness. */
		final int nBORDER = 1;
		/** The amount of space between each segment. The space comes AFTER the segment. */
		final int nGAP = 1;
		/** The X coordinate to cut off the clipping shape */
		final int nCLIP_AT = (int) (nX + 1 + nBORDER + (nWidth - nBORDER * 2) * (dQuantity/dMaxQuantity) + nGAP);
		/** Segment length, including gaps. */
		final double dSEG_LENGTH = (nWidth - nBORDER * 2) / dMaxQuantity;
		
		//int nCurrentX;
		Polygon pClippingArea = new Polygon();
		
		/* Add the segments, one by one. Each segment is a rectangle. */
		for(int nSegment = 0; nSegment < dQuantity; nSegment++){
			/* Add the first two points of this rectangle */
			pClippingArea.addPoint(nX + 1 + nBORDER + (int)(nSegment * dSEG_LENGTH), nY + 1 + nBORDER);
			pClippingArea.addPoint(nX + 1 + nBORDER + (int)(nSegment * dSEG_LENGTH), nY + nHeight - nBORDER);
			
			double dEndSegmentX = nX + 1 + nBORDER + (nSegment * dSEG_LENGTH) + dSEG_LENGTH - nGAP;
			//double dEndSegmentX = nX + 1 + nBORDER + (nSegment * dSEG_LENGTH) + dSEG_LENGTH;
			/* If this segment is the not the last, subtract some of it for a gap. Yes I know this doesn't produce exact results, but they are close and
			 * the alternative would be at least another half hour of debugging. */
//			if(nSegment + 1 < dQuantity){
//				dEndSegmentX -= nGAP;
//			}
			/* Change the legnth of the segment if it extends past CLIP_AT or would make the shape longer than the nWidth. */
			dEndSegmentX = Math.min(dEndSegmentX, nCLIP_AT);
			dEndSegmentX = Math.min(dEndSegmentX, nX + nWidth);
			
			/* Add the remaining two points */
			pClippingArea.addPoint((int) dEndSegmentX, nY + nHeight - nBORDER);
			pClippingArea.addPoint((int) dEndSegmentX, nY + 1 + nBORDER);
		}
		
		/* Close off the polygon with the first point, then return. */
		pClippingArea.addPoint(nX + 1 + nBORDER, nY + 1 + nBORDER);
		
		return pClippingArea;
	}
	/**
	 * Generates an image of a horizontal, white, transparent gradient by drawing individual lines.
	 * 
	 * @param nWidth The width of the image. 
	 * @param nHeight The height of the image.
	 * @param bDual Specifies whether gradient varies from transparent-opaque or transparent-opaque-transparent.
	 * @return The generated image.
	 */
	private BufferedImage DrawSmoothWhiteGradient(int nWidth, int nHeight, boolean bDual){
		BufferedImage image = new BufferedImage(nWidth, nHeight, BufferedImage.TRANSLUCENT);
		Graphics g = image.getGraphics();
		for(int nX = 0; nX <= nWidth; nX++){
			if(bDual)
				g.setColor(new Color(255,255,255, 255 - (int)((Math.abs(nWidth / 2.0 - nX) / (nWidth / 2) * 255)) ));
			else
				g.setColor(new Color(255,255,255, (int)(255 * ((double)nX / nWidth))));
			g.drawLine(nX, 0, nX, nHeight);
		}
		return image;
	}
	/**
	 * Draws a bar/meter with an animation indicating that it's going up.
	 * Width of spectrum is proportional to the ratio of the bar filled.
	 * 
	 * ARCHIVED. Reason: Realized that I could just draw this once and scale it instead of drawing hundreds
	 * of lines, multiple times during each draw of the interface. 
	 * 
	 * @param g	The image to draw on.
	 * @param barColor	The color of the bar.
	 * @param regenColor	The color of the regeneration animation start.
	 * @param dRatio	How much of the bar is filled with color. Should be a value from 0.0 to 1.0.
	 * @param dRegenAmount	Regen factor. Determines how fast animation goes
	 * @param nX	X location.
	 * @param nY	Y location.
	 * @param nWidth	The width of the bar.
	 * @param nHeight	The height of the bar. 
	 */
	/*private static void DrawRegeneration(Graphics g, Color barColor, Color regenColor, double dRatio, double dRegenAmount, int nX, int nY , int nWidth, int nHeight){
		//Determines where along the bars to draw spectrum 
		//width * ratio * (timeInMilliSec / rate of change / regen factor % percent of width * percentage modifer - offset)
		int nStartSpectrum = (int)(nWidth*(System.currentTimeMillis()/ 15.0  * (dRegenAmount * 15) % 120.0 * .01 - .0));
		int nWidthOfSpectrum = (int)(nWidth * dRatio/ 1);
		
		for(int nNum = 0; nNum <= nWidthOfSpectrum; nNum++){
			if(regenColor.getAlpha() == 255){
				g.setColor(new Color(regenColor.getRed()+(int)(((double)nNum/nWidthOfSpectrum)*((double)barColor.getRed()-regenColor.getRed())),
									regenColor.getGreen()+(int)(((double)nNum/nWidthOfSpectrum)*((double)barColor.getGreen()-regenColor.getGreen())),
									regenColor.getBlue()+(int)(((double)nNum/nWidthOfSpectrum)*((double)barColor.getBlue()-regenColor.getBlue()))));
			} else {
				g.setColor(new Color(regenColor.getRed()+(int)(((double)nNum/nWidthOfSpectrum)*((double)barColor.getRed()-regenColor.getRed())),
						regenColor.getGreen()+(int)(((double)nNum/nWidthOfSpectrum)*((double)barColor.getGreen()-regenColor.getGreen())),
						regenColor.getBlue()+(int)(((double)nNum/nWidthOfSpectrum)*((double)barColor.getBlue()-regenColor.getBlue())),
						regenColor.getAlpha()+(int)(((double)nNum/nWidthOfSpectrum)*((double)barColor.getBlue()-regenColor.getAlpha()))));
			}
			//(constant bar offset from border + where the spectrum is brightest - position of next line to be
			//drawn
			if((nX+nStartSpectrum-nNum > nX) && (nX+nStartSpectrum-nNum < (int)(nWidth*dRatio) + nX)){
				g.drawLine(nX+nStartSpectrum-nNum,nY + 1,nX+nStartSpectrum-nNum, nY-1 + nHeight);
			}
		}
	}*/
	/**
	 * Draws a bar/meter...with a smooth white gradient. Also draws a long transparent white spectrum 
	 * that traverses the colored part of the meter. 
	 * 
	 * ARCHIVED. Reason: Realized that I could just draw this once and clip it instead of drawing hundreds
	 * of lines, FOUR times during each draw of the interface. 
	 * 
	 * @param g	The image to draw on.
	 * @param nWidthOfSpectrum	The width of the white spectrum from end-to-end, both ways. 
	 * @param dRatio	How much of the bar is filled with color.
	 * @param nX	X location.
	 * @param nY	Y location.
	 * @param nWidth	The width of the bar.
	 * @param nHeight	The height of the bar. 
	 */
	/*private static void DrawSmoothWhiteDualGradient(Graphics g, int nWidthOfSpectrum, double dRatio, int nX, int nY , int nWidth, int nHeight){
		//Determines where along the bars to draw spectrum 
		//width * (timeInMilliSec / rate of change % percent of width * percent - offset)
		int nCenterSpectrum = (int)(nWidth*(System.currentTimeMillis()/25.0 % 200.0 * .01 - .5));
		
		for(int nNum = 0; nNum <= nWidthOfSpectrum; nNum++){
			g.setColor(new Color(255,255,255,255-(int)(255.0/nWidthOfSpectrum*nNum)));
			//old if statement that went from one end of the bar to another. didnt go from one end to the end 
			//of the COLORED region.
			//(constant bar offset from border + where the spectrum is brightest - position of next line to be
			//drawn
			//if((nX+nCenterSpectrum-nNum >= nX) && (nX+nCenterSpectrum-nNum < nWidth+nX)){
			if((nX+nCenterSpectrum-nNum > nX) && (nX+nCenterSpectrum-nNum < (int)(nWidth*dRatio) + nX)){
				g.drawLine(nX+nCenterSpectrum-nNum,nY,nX+nCenterSpectrum-nNum, nY + nHeight);
			}
			//old if statement like above
			//if((nX+nCenterSpectrum+nNum >= nX) && nX+nCenterSpectrum+nNum < nWidth+nX){
			//the nX+1 is to make it so that the middle of the spectrum is drawn over two X locations rather
			//than once over one X location. this matters when using transparency for the spectrum.
			if((nX+1+nCenterSpectrum+nNum > nX) && nX+1+nCenterSpectrum+nNum < (int)(nWidth*dRatio) + nX){
				g.drawLine(nX+1+nCenterSpectrum+nNum,nY,nX+1+nCenterSpectrum+nNum, nY + nHeight);
			}
		}
	}*/
}