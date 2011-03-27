import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

class Star extends Floater{
	private double mySpeed;
	private Color cColor;
	
	public Star(int nScreenX, int nScreenY){
		mySpeed = Math.random()*.75;
		myCenterX = (int)(Math.random()*nScreenX);
		myCenterY = (int)(Math.random()*nScreenY);
		nSize = (int)(3 * (mySpeed / .75));
		int nBrightness = (int)(Math.random() * 200 + 55);
		cColor = new Color(nBrightness,nBrightness,nBrightness,255);
	}
	public void draw(Graphics2D g){
		g.setColor(cColor);
		g.fillOval((int)myCenterX, (int)myCenterY, nSize, nSize);
	}
	public void move (int nScreenWidth, int nScreenHeight)
	{
		myCenterX += myDirectionX*mySpeed;
		myCenterY += myDirectionY*mySpeed;
		
		/* This code makes it so that the star will wrap back, 
		 * and will not bunch up into a line when velocity is high. */
		if(myCenterX >nScreenWidth)
			myCenterX = (int)(Math.random()*(-.1*nScreenWidth));
		else if (myCenterX<0)
			myCenterX = (int)(Math.random()*(.1*nScreenWidth)+(nScreenWidth));
		if(myCenterY >nScreenHeight)
			myCenterY = (int)(Math.random()*(-.1*nScreenHeight));
		else if (myCenterY < 0)
			myCenterY = (int)(Math.random()*(.1*nScreenHeight)+(nScreenHeight));
	}
	@Override
	public Shape getShape() {
		return null;
	}
	public boolean shouldBeRemoved() {
		return false;
	}
}