import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

public class Laser {
	final static double FADE_DURATION = 500;
	
	/** The ship variable, used to determine where the laser starts. */
	private Spaceship Cruzer;
	/** The level of the lasers, ranges from 1-3. Determines damage and color. */
	private int nLevel;
	/** Point to draw end of laser to */
	private Point endPoint;
	/** "Active" timer that does the firing */
	private Animator durationTimer;
	
	/** References to existing variables */
	private List<Floater> undesirables;
	
	public Laser(Spaceship Ship){
		nLevel = 1;
		Cruzer = Ship;
		endPoint = new Point();
		
		durationTimer = new Animator(15000, new TimingTarget(){
			public void begin() {}
			public void end() {}
			public void repeat() {}
			public void timingEvent(float fraction) {
				fire();
			}
		});
	}
	public boolean isActive(){return durationTimer.isRunning();}
	public void activate(List<Floater> undesirables){
		activate(undesirables, 10000);
	}
	public void activate(List<Floater> undesirables, int nDuration){
		if(isActive() == false){
			this.undesirables = undesirables;
			durationTimer.setDuration(nDuration);
			durationTimer.start();
		}
	}
	public void fire(){
		/* Initialize the coordinates of the start and end of laser. The end of the laser will be at 1000 away
		 * from the ship unless it is changed in the collision detection. */
		final double DIRECTION = Cruzer.getPointDirection() * (Math.PI/180);
		final int LASER_X_START = (int)Cruzer.getCannonPoint().x;
		final int LASER_Y_START = (int)Cruzer.getCannonPoint().y;
		int nLaserXEnd = (int)(3300*Math.cos(DIRECTION)+LASER_X_START);
		int nLaserYEnd = (int)(3300*Math.sin(DIRECTION)+LASER_Y_START);
		
		/* Start with a line 3300 long and build a list of all the floaters that collide with it
		 * so that only those have to be checked later when calculating the laser end point. */
		final Line2D.Double FULL_LENGTH_LASER = new Line2D.Double(LASER_X_START, LASER_Y_START, nLaserXEnd, nLaserYEnd);
		LinkedList<Floater> intersected = new LinkedList<Floater>();
		synchronized (undesirables) {
			Iterator<Floater> iterator = undesirables.iterator();
			while (iterator.hasNext()) {
				Floater curFloater = iterator.next();
				
				if (Util.shapeIntersect(FULL_LENGTH_LASER, curFloater.getShape())) {
					intersected.add(curFloater);
				}
			}
		}
		/* Calculate whether the laser comes in contact with anything and change end coordinates if necessary. 
		 * Starting from 10, increment the length of laser until it makes contact with something. Then increment 
		 * by 2 for drawing accuracy and set the end coordinates. */
		for(int nR = 10; nR < 3300; nR += 10){
			/** Bool for whether the correct coordinate was found. 
			 * Cannot use a break alone because there are three loops. */
			boolean bHit = false; 
			
			Iterator<Floater> iterator2 = intersected.iterator();
			while(iterator2.hasNext()){
				Floater curFloater = iterator2.next();
				int nLaserXTestEnd = (int)(nR*Math.cos(DIRECTION) + LASER_X_START);
				int nLaserYTestEnd = (int)(nR*Math.sin(DIRECTION) + LASER_Y_START);
				if(Util.shapeIntersect(new Line2D.Double(LASER_X_START,LASER_Y_START, nLaserXTestEnd,nLaserYTestEnd), curFloater.getShape())){
					for(int nRPrecise = nR - 10; nRPrecise <= nR; nRPrecise += 2){
						nLaserXTestEnd = (int)(nRPrecise*Math.cos(DIRECTION) + LASER_X_START);
						nLaserYTestEnd = (int)(nRPrecise*Math.sin(DIRECTION) + LASER_Y_START);
						if(Util.shapeIntersect(new Line2D.Double(LASER_X_START,LASER_Y_START, nLaserXTestEnd,nLaserYTestEnd), curFloater.getShape())){
							nLaserXEnd = nLaserXTestEnd;
							nLaserYEnd = nLaserYTestEnd;
							bHit = true;
							break;
						}
					}
				}
				if(bHit){
					damage(curFloater);
					break;
				}
			}
			if(bHit)
				break;
		}
		
		/* Set the endpoints of the laser for the draw function */
		endPoint.x = nLaserXEnd;
		endPoint.y = nLaserYEnd;
	}
	
	public void draw(Graphics2D g){
		if(isActive()){
			/* Set the color depending on the laser level and draw laser. */
			switch(nLevel){
				case 1:
					g.setColor(Color.red);
					break;
				case 2:
					g.setColor(Color.green);
					break;
				case 3:
					g.setColor(Color.blue);
					break;
			}
			/* If the duration is long enough for the laser to get noticed in the first place, we can add a nice fade effect 
			 * at the end. */
			final long TIME_LEFT = durationTimer.getDuration() - durationTimer.getCycleElapsedTime();
			if(durationTimer.getDuration() > FADE_DURATION && TIME_LEFT < FADE_DURATION && TIME_LEFT >= 0){
				double fraction = TIME_LEFT / FADE_DURATION;
				g.setColor(new Color((int)(fraction * g.getColor().getRed()),
									 (int)(fraction * g.getColor().getGreen()),
									 (int)(fraction * g.getColor().getBlue())));
			}
			
			final double DIRECTION = Cruzer.getPointDirection() * (Math.PI/180);
			final int LASER_X_START = (int)Cruzer.getCannonPoint().x;
			final int LASER_Y_START = (int)Cruzer.getCannonPoint().y;
			
//			BasicStroke stroke;
//			stroke = new BasicStroke(5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);
//		    g.setStroke(stroke);
			g.drawLine(LASER_X_START, LASER_Y_START, endPoint.x, endPoint.y);	
//			g.setStroke(new BasicStroke(1));
		}
	}
	/** Damages the floater specified depending on the level of the laser. */
	private void damage(Floater floater){
		switch(nLevel){
			case 1:
				floater.setHP(floater.getHP() - 10);
				break;
			case 2:
				floater.setHP(floater.getHP() - 20);
				break;
			case 3:
				floater.setHP(floater.getHP() - 40);
				break;
		}
	}
	public int getLevel() {return nLevel;}
	public void setLevel(int level) {nLevel = level;}
}
