import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

public class Particle extends Floater{
	private int nBrightness;
	
	public Particle(double d, double e){
		myCenterX = d;
		myCenterY = e;
		myDirectionX = (30 * Math.random()) - 15;
		myDirectionY = (30 * Math.random()) - 15;
		nSize = 2;
		nBrightness = (int)(Math.random() * 100 + 55);
	}
	public void draw(Graphics2D g){
		g.setColor(new Color(nBrightness,nBrightness,nBrightness));
		g.fillOval((int)myCenterX, (int)myCenterY, nSize, nSize);
		
		if(nBrightness > 0){
			nBrightness = nBrightness - 3;
			
			if(nBrightness < 0)
				nBrightness = 0;
		}
	}
	public boolean shouldBeRemoved(){
		/* Remove if not visible or distance is more than 2000 from center. */
		return (nBrightness == 0)
			|| (Math.hypot(getX() - (Asteroids_v2.APPLET_X / 2), getY() - (Asteroids_v2.APPLET_Y / 2)) > 2000);
	}
	@Override
	public Shape getShape() {
		return null;
	}
}