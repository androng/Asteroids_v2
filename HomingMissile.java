import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;


public class HomingMissile extends Bullet{
	private Floater targetFloater;
	private double dAcceleration;
	private Point2D lastPoint;
	
	public HomingMissile(double startX, double startY, double direction, Floater target) {
		super(startX, startY, direction);
		
		lastPoint = new Point2D.Double(startX, startY);
		dAcceleration = 1;
		
		myDirectionX = (25 + 5 * Math.random()) * Math.cos(Math.toRadians(myPointDirection));
		myDirectionY = (25 + 5 * Math.random()) * Math.sin(Math.toRadians(myPointDirection));
		
		myColor = Color.cyan;
		targetFloater = target;
		nLength = 25;
	}
	public void draw(Graphics2D g){
		/* Draw ellipse around target */
//		g.setColor(Color.red);
//		g.draw(new Ellipse2D.Float(targetFloater.getX() - 50, targetFloater.getY() - 50, 100, 100));
		
		/* Draw missile */
		g.setColor(myColor);
		double dRadians = myPointDirection  * (Math.PI/180);
		g.drawLine((int)myCenterX,(int)myCenterY,(int)(nLength * Math.cos(dRadians)*-1+myCenterX),(int)(nLength * Math.sin(dRadians)*-1+myCenterY));
		
	}
	public void moveBoundless(){
		lastPoint.setLocation(myCenterX, myCenterY);
		
		myCenterX += myDirectionX;
		myCenterY += myDirectionY;
		
		if(targetFloater.shouldBeRemoved() == false){
			/* Accelerate toward target */
//			final double DISTANCE = Point.distance(targetFloater.getX(), targetFloater.getY(), myCenterX, myCenterY);
			final double ANGLE_TOWARD_TARGET = Math.atan2(targetFloater.getY() - getY(), targetFloater.getX() - getX());
			final double AXIS_ROTATION = ANGLE_TOWARD_TARGET + Math.PI/2;
			
			
			final double VELOCITY = Math.hypot(myDirectionX, myDirectionY);
			final double VELOCITY_ANGLE = Math.atan2(myDirectionY, myDirectionX);
			
			final double VELOCITY_ANGLE_ROTATED = VELOCITY_ANGLE - AXIS_ROTATION;
			
			final double ACCEL_X_ROTATED = -Math.cos(VELOCITY_ANGLE_ROTATED);
			final double ACCEL_Y_ROTATED = -Math.abs(Math.sin(VELOCITY_ANGLE_ROTATED));
			
			final double ACCEL_ANGLE = Math.atan2(ACCEL_Y_ROTATED, ACCEL_X_ROTATED) + AXIS_ROTATION;
			final double TARGET_VELOCITY_ANGLE = Math.atan2(targetFloater.getDirectionY(), targetFloater.getDirectionX());
			
			myDirectionX += dAcceleration * Math.cos(ACCEL_ANGLE) + .1 * targetFloater.getDirectionX();
			myDirectionY += dAcceleration * Math.sin(ACCEL_ANGLE) + .1 * targetFloater.getDirectionY();
			myPointDirection = Math.toDegrees(Math.atan2(myDirectionY, myDirectionX));
		/* Change color if no longer active (and subtract health) */
		} else {
			myColor = new Color(0, 75, 75);
			myHP -= 1;
		}
	}
	public void setDirectionX(double dX){
		myDirectionX = dX;
		/* Change the point direction to match the new direction */
		myPointDirection = Math.toDegrees(Math.atan2(myDirectionY, myDirectionX));
	}
	public void setDirectionY(double dY){
		myDirectionY = dY;
		/* Change the point direction to match the new direction */
		myPointDirection = Math.toDegrees(Math.atan2(myDirectionY, myDirectionX));
	}
	public Shape getShape(){
		return new Line2D.Double(lastPoint, new Point2D.Double(myCenterX, myCenterY));
	}
}
