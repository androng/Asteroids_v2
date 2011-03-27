import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

public abstract class Floater{
	protected double myHP;
	protected int myPointValue;
	protected int nSize;
	protected Color myColor;
	protected double myCenterX, myCenterY; /* holds center coordinates */
	protected double myDirectionX, myDirectionY; /* holds x and y coordinates of the vector for direction of travel */
	protected double myPointDirection; /* holds current direction the ship is pointing in degrees */
	
	public void accelerate (double dAmount){
		/* convert the current direction the floater is pointing to radians */
		double dRadians = myPointDirection*(Math.PI/180);
		/* change coordinates of direction of travel */
		myDirectionX += ((dAmount) * Math.cos(dRadians));
		myDirectionY += ((dAmount) * Math.sin(dRadians));    
	}
	void rotate (int nDegreesOfRotation){
		/* rotates the floater by a given number of degrees */
		myPointDirection += nDegreesOfRotation;
	}
	public void moveBounded (int nScreenWidth, int nScreenHeight){
		if(myCenterX + myDirectionX < 0){
			myCenterX = 0;
			myDirectionX *= -1;
		} else if (myCenterX + myDirectionX > nScreenWidth){
			myCenterX = nScreenWidth;
			myDirectionX *= -1;
		} else {
			myCenterX += myDirectionX;
		}
		
		if(myCenterY + myDirectionY < 0){
			myCenterY = 0;
			myDirectionY *= -1;
		} else if (myCenterY + myDirectionY > nScreenHeight){
			myCenterY = nScreenHeight;
			myDirectionY *= -1;
		} else {
			myCenterY += myDirectionY;
		}
	}
	public void move (int nScreenWidth, int nScreenHeight){
		myCenterX += myDirectionX;
		myCenterY += myDirectionY;
		
		/* wrap around screen */
		if(myCenterX >nScreenWidth)
			myCenterX = 0;
		else if (myCenterX<0)
			myCenterX = nScreenWidth;
		if(myCenterY >nScreenHeight)
			myCenterY = 0;
		else if (myCenterY < 0)
			myCenterY = nScreenHeight;
	}
	public void moveBoundless(){
		myCenterX += myDirectionX;
		myCenterY += myDirectionY;
	}
	abstract public void draw(Graphics2D g);
	/** Moves the floater in the reverse of the specified X and Y components.
	 * 
	 * 	@param dDirectionX The X component.
	 * 	@param dDirectionY The Y component.
	 */
	public void moveFrom(double dDirectionX, double dDirectionY){
		myCenterX += dDirectionX*-1;
		myCenterY += dDirectionY*-1;
	}
	public double distance(Floater floater){
		return Math.hypot(myCenterX - floater.myCenterX, myCenterY - floater.myCenterY);
	}
	abstract public Shape getShape();
	abstract public boolean shouldBeRemoved();
	public double getSize(){return nSize;};
	public void setHP(double nHP){myHP = nHP;}
	public double getHP(){return myHP;}
	public void setX(int d){myCenterX = d;}
	public int getX(){return (int)myCenterX;}
	public void setY(int nY){myCenterY = nY;}
	public int getY(){return (int)myCenterY;}
	public void setColor(Color cColor){myColor = cColor;}
	public Color getColor(){return myColor;}
	public void setDirectionX(double dX){myDirectionX = dX;}
	public double getDirectionX(){return myDirectionX;}
	public void setDirectionY(double dY){myDirectionY = dY;}
	public double getDirectionY(){return myDirectionY;}
	public void setPointDirection(double dDegrees){myPointDirection = dDegrees;}
	public double getPointDirection(){return myPointDirection;}
	public double getSpeed(){return Math.hypot(myDirectionX, myDirectionY);}
}