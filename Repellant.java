

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;


public class Repellant {
	/** Whether or not this weapon is active this frame. */
//	private boolean bActive;
	/** The floater of whose center to repel from. */
	private Floater centerFloater;
	/** The points to draw to. This was used instead of a reference to the floater list because 
	 * the draw function should not have to calculate things like which floaters to draw to based
	 * on the distance to them. That is already calculated in the effect function. [repel(...)] 
	 * EDIT: I don't know what i was thinking when i made a whole new class for this. When I said "floater list", 
	 * I meant list of ALL bad guys, but it changed and it's here now anyway. */
//	private List<SizePoint> points;
	/** List of floaters to draw animation to, refreshed each time in the repel function. */
	private List<Floater> closeFloaters;
	/** When the repellant is active, this timer will be running. */
	private Animator durationTimer;
	
	/** References to existing variables */
	private List<Floater> undesirables;
	
	public Repellant(Floater centerFloater){
//		bActive = false;
		this.centerFloater = centerFloater;
		closeFloaters = Collections.synchronizedList(new LinkedList<Floater>());
		
		durationTimer = new Animator(15000, new TimingTarget(){
			public void begin() {}
			public void end() {}
			public void repeat() {}
			public void timingEvent(float fraction) {
				repel();
			}
		});
	}
	public boolean isActive(){
		return durationTimer.isRunning();
	}
	public void activate(List<Floater> undesirables){
		this.undesirables = undesirables;
		durationTimer.start();
	}
	/**
	 * Loops through the pre-built list of SizePoints (class member) and draws "repellant arrows", 
	 * cone thickness of which is based on size. 
	 * 
	 * @param g Graphics canvas to draw on.
	 */
	public void draw(Graphics2D g){
		if(isActive()){
			/* Static parameters for arrow drawing */
			final double ARROW_SLOPE = .75 * Math.PI;
			final Color ARROW_COLOR = new Color(255,255,0);
			
			/* Draw the animation that shows even when no floaters are being repelled */
			final int NUM_IDLE_ARROWS = 6;
			/** The angle that the cone points towards. Will only change after duration specified by divisor. */
			for(int nArrow = 0; nArrow < NUM_IDLE_ARROWS; nArrow++){
				double dAngleOfCone = System.currentTimeMillis() / (250 + nArrow * 450);
				final double FRACTION_OF_PERIOD = Util.modulus(1, 250 + nArrow * 450);
				final double IDLE_CONE_THICKNESS = 30 * FRACTION_OF_PERIOD;
				final int IDLE_MIDDLE_X = (int)((10 + 60 * FRACTION_OF_PERIOD) * Math.cos(dAngleOfCone) + centerFloater.getX());
				final int IDLE_MIDDLE_Y = (int)((10 + 60 * FRACTION_OF_PERIOD) * Math.sin(dAngleOfCone) + centerFloater.getY());
				g.setColor(new Color((int)(ARROW_COLOR.getRed()* FRACTION_OF_PERIOD)
									,(int)(ARROW_COLOR.getGreen()* FRACTION_OF_PERIOD)
									,(int)(ARROW_COLOR.getBlue()* FRACTION_OF_PERIOD)));
				g.drawLine(IDLE_MIDDLE_X,IDLE_MIDDLE_Y,(int)(IDLE_CONE_THICKNESS * Math.cos(dAngleOfCone - ARROW_SLOPE) + IDLE_MIDDLE_X),(int)(IDLE_CONE_THICKNESS * Math.sin(dAngleOfCone - ARROW_SLOPE) +IDLE_MIDDLE_Y));
				g.drawLine(IDLE_MIDDLE_X,IDLE_MIDDLE_Y,(int)(IDLE_CONE_THICKNESS * Math.cos(dAngleOfCone + ARROW_SLOPE) + IDLE_MIDDLE_X),(int)(IDLE_CONE_THICKNESS * Math.sin(dAngleOfCone + ARROW_SLOPE) +IDLE_MIDDLE_Y));
				
				dAngleOfCone += 2 * Math.PI / NUM_IDLE_ARROWS;
			}
			
			synchronized(closeFloaters){
				for(Floater curFloater: closeFloaters){
					
					/** Arrow drawing */
					/* Non-customizable, for code readability only. */
					final double DISTANCE = Point.distance(centerFloater.getX(), centerFloater.getY(), curFloater.getX(), curFloater.getY());
					final double ANGLE_BTWN_FLOATERS = Math.atan2(curFloater.getY() - centerFloater.getY(), curFloater.getX() - centerFloater.getX());
					
					/* Dynamic parameters for arrow drawing */
					final double ARROW_INTERVAL = DISTANCE / 10.0;
					final double CONE_THICKNESS = .10 * (curFloater.getSize() / 10.0);
					/* Since all the parts of the angle are constant, the angle is constant, and the components are constant. */
					final double ANGLE_SIN_LEFT = Math.sin(ANGLE_BTWN_FLOATERS - ARROW_SLOPE);
					final double ANGLE_COS_LEFT = Math.cos(ANGLE_BTWN_FLOATERS - ARROW_SLOPE);
					final double ANGLE_SIN_RIGHT = Math.sin(ANGLE_BTWN_FLOATERS + ARROW_SLOPE);
					final double ANGLE_COS_RIGHT = Math.cos(ANGLE_BTWN_FLOATERS + ARROW_SLOPE);
					
					for(double dR = 1; dR <= DISTANCE; dR += ARROW_INTERVAL){
						/* Change the color depending on how far along the distance we are */
						g.setColor(new Color((int)(ARROW_COLOR.getRed()*((double)dR/DISTANCE))
											,(int)(ARROW_COLOR.getGreen()*((double)dR/DISTANCE))
											,(int)(ARROW_COLOR.getBlue()*((double)dR/DISTANCE))));
						/* Determine the point in the middle of the cone a distance dR away from centerFloater */
						final int MIDDLE_X = (int)(dR*Math.cos(ANGLE_BTWN_FLOATERS) + centerFloater.getX());
						final int MIDDLE_Y = (int)(dR*Math.sin(ANGLE_BTWN_FLOATERS) + centerFloater.getY());
						g.drawLine(MIDDLE_X,MIDDLE_Y,(int)(dR*CONE_THICKNESS* ANGLE_COS_LEFT + MIDDLE_X),(int)(dR*CONE_THICKNESS* ANGLE_SIN_LEFT +MIDDLE_Y));
						g.drawLine(MIDDLE_X,MIDDLE_Y,(int)(dR*CONE_THICKNESS* ANGLE_COS_RIGHT + MIDDLE_X),(int)(dR*CONE_THICKNESS* ANGLE_SIN_RIGHT +MIDDLE_Y));
					}
					
				}
				/* Remove all the floaters from the drawing list */
				closeFloaters.removeAll(closeFloaters);
			}
//			bActive = false;
		}
	}
	
	public void repel(){
//		bActive = true;
		synchronized(undesirables){
			ListIterator <Floater> it = undesirables.listIterator();
			while(it.hasNext()){
				Floater curFloater = it.next();
				final int DISTANCE = (int)(Point.distance(centerFloater.getX(), centerFloater.getY(), curFloater.getX(), curFloater.getY()));
				
				if(DISTANCE < 200){
					/* Add floater to list for drawing*/
					closeFloaters.add(curFloater);
					
					/** The direction change varies inversely with the size of the floater. */
					final double DIRECTION_CHANGE = 1.5 * Math.pow(2, -(curFloater.getSize()/10 - 1));
					final double ANGLE_BTWN_FLOATERS = Math.atan2(curFloater.getY() - centerFloater.getY(), curFloater.getX() - centerFloater.getX());
					
					curFloater.setDirectionX(curFloater.getDirectionX() + DIRECTION_CHANGE * Math.cos(ANGLE_BTWN_FLOATERS));
					curFloater.setDirectionY(curFloater.getDirectionY() + DIRECTION_CHANGE * Math.sin(ANGLE_BTWN_FLOATERS));
					
				}
			}
		}
	}
}
