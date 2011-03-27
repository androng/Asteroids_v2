import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

public class Asteroid extends Floater /*implements PolyFloater*/{
	
	//final static double MAX_HEALTH = 100;
	
	private Polygon pShape;
	private int myRotationSpeed;
	
	public Asteroid(int nMaxRadius){
		
		nSize = Math.max(5, nMaxRadius);
		pShape = new Polygon();
		int nCorners = (int)(nMaxRadius / 4 + 5 * Math.random() + 2);
		myPointValue = nMaxRadius; 
		for(int nIndex = 0; nIndex < nCorners; nIndex++){
			double dR = nMaxRadius - (10 * Math.random());
			double dTheta = Math.toRadians(360 * ((double)nIndex / nCorners));
			pShape.addPoint((int)(dR * Math.cos(dTheta)), (int)(dR * Math.sin(dTheta)));
		}
		myHP = nSize;
		/* Brightness will depend on HP, max value 255. */
		final int nBrightness = Math.min((int)(myHP * 2), 255);
		myColor = new Color(nBrightness, nBrightness, nBrightness);
		myCenterX = (int)(Asteroids_v2.APPLET_X * Math.random());
		myCenterY = (int)(Asteroids_v2.APPLET_Y * Math.random());
		while(myDirectionX == 0 || myDirectionY == 0){
			myDirectionX = (int)(Math.random()*10-5);
			myDirectionY = (int)(Math.random()*10-5);
//			myDirectionX = .1;
//			myDirectionY = .1;
		}
		while(myRotationSpeed == 0)
			myRotationSpeed = (int)(Math.random()*10-5);
	}
//	public void move (int nScreenWidth, int nScreenHeight){
//		myCenterX += myDirectionX;
//		myCenterY += myDirectionY;
//		
//		/* wrap around a screen twice as big as the visible area */
//		if(myCenterX > nScreenWidth + (nScreenWidth / 2))
//			myCenterX = 0 - (nScreenWidth / 2);
//		else if (myCenterX < 0 - (nScreenWidth / 2))
//			myCenterX = nScreenWidth + (nScreenWidth / 2);
//		if(myCenterY > nScreenHeight + (nScreenHeight / 2))
//			myCenterY = 0 - (nScreenHeight / 2);
//		else if (myCenterY < 0 - (nScreenHeight / 2))
//			myCenterY = nScreenHeight + (nScreenHeight / 2);
//	}
	public void draw (Graphics2D g){
		Polygon pRotated = getShape();
		
		g.setColor(myColor);   
		g.fillPolygon(pRotated);
		
		g.setColor(Color.white);
		if(myHP < 25)
			g.setColor(Color.white);
		g.drawPolygon(pRotated);
	}
	public void move (int nScreenWidth, int nScreenHeight){
		super.move(nScreenWidth, nScreenHeight);
		
		/* Rotate each time it is moved */
		myPointDirection += myRotationSpeed;
	}
	public void setRotationSpeed(int nDegrees){myRotationSpeed = nDegrees;}
	public int getRotationSpeed(){return myRotationSpeed;}
	public int getPointValue(){return myPointValue;}
	public void setHP(double dHP){
		myHP = dHP;
		if(myHP < 0)
			myHP = 0;
		/* Brightness will depend on HP, max value 255. */
		final int nBrightness = Math.min((int)(myHP * 2), 255);
		myColor = new Color(nBrightness, nBrightness, nBrightness);
	}
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Asteroid other = (Asteroid) obj;
		return myRotationSpeed == other.myRotationSpeed
		&& myCenterX == other.myCenterX
		&& myCenterY == other.myCenterY;
	}
	public Polygon getShape() {
		Polygon translatedPoly = new Polygon(pShape.xpoints, pShape.ypoints, pShape.npoints);
		translatedPoly = Util.rotate(translatedPoly, myPointDirection);
		translatedPoly.translate((int)myCenterX, (int)myCenterY);
		return translatedPoly;
	}
	public boolean shouldBeRemoved(){
		return myHP <= 0;
	}
}