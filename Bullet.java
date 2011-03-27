import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;

public class Bullet extends Floater{
	int nLength;
	
	public Bullet(double dStartX, double dStartY, double dDirection){
		myHP = 100;
		myColor = Color.green;
		myCenterX = dStartX;
		myCenterY = dStartY;
		myPointDirection = dDirection;
		myDirectionX = Math.cos(Math.toRadians(myPointDirection))*25;
		myDirectionY = Math.sin(Math.toRadians(myPointDirection))*25;
		nLength = 25;
	}
	public void draw(Graphics2D g){
		g.setColor(myColor);
		//g.setColor(Color.green);
		double dRadians = myPointDirection  * (Math.PI/180);
		g.drawLine((int)myCenterX,(int)myCenterY,(int)(nLength * Math.cos(dRadians)*-1+myCenterX),(int)(nLength * Math.sin(dRadians)*-1+myCenterY));
	}
	public Shape getShape(){
		double dRadians = myPointDirection  * (Math.PI/180);
		return new Line2D.Double(myCenterX,myCenterY,nLength * Math.cos(dRadians)*-1+myCenterX,nLength * Math.sin(dRadians)*-1+myCenterY);
	}
	public boolean shouldBeRemoved(){
		/* Remove if HP is 0 or distance from center is greater than 1500. */
		return myHP <= 0 
			|| (Math.hypot(getX() - (Asteroids_v2.APPLET_X / 2), getY() - (Asteroids_v2.APPLET_Y / 2)) > 1500);
	}
}