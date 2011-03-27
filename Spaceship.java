import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.TimerTask;

class Spaceship extends Floater /*implements PolyFloater*/{
	//private int myModelNumber; /* it was in the old version. doubt it will be used here. */
	protected int myDesignNumber;
	
	protected double myHPLimit; /* myHP is already declared in the Floater class */

	protected double myHPRegen;
	protected double myBullets;
	protected double myBulletRegen;
	protected double myBulletLimit;
	protected double mySpecialWeaponCharge;
	protected double mySpecialWeaponCooldown; 
	protected double mySpecialWeaponChargeLimit;
	protected double myHyperspaceCharge; 
	protected double myHyperspaceCooldown; 
	protected double myHyperspaceChargeLimit;
	
	protected double mySpeedLimit;
	protected double mySizeMultiplier;
	protected HyperspaceManager Hyperspace;
	protected Laser Laser1;
	protected ShieldManager Shield;
	protected Repellant RepelUnit;
	protected ChainLightningManager LightningManager;
	protected ReinforcementsManager Radio;
	protected ZeroLaserManager ZeroLaser;
	
	/** The epic declaration of all the spaceship's polygons.
	 * 
	 *	For some reason I cannot explain, 
	 *	the polygon X and Y coordinate must be inverted, which gives me a migraine. 
	 * 
	 *  Hey, I tried.                */
	
	final static int[] naYCorners = new int[] { 0, 4, 6, 6, 4, 8, 8, 4, 6,-6,-4,-8,-8,-4,-6,-6,-4};
	final static int[] naXCorners = new int[] {10, 0, 2, 4, 6, 4, 0,-4,-6,-6,-4, 0, 4, 6, 4, 2, 0};
	protected Polygon pShape;
	
	final static int NUM_DESIGNS = 8;
	final static int[] naYDesign0 = new int[] { 0}; /* Blank */
	final static int[] naXDesign0 = new int[] { 0};
	final static int[] naYDesign1 = new int[] { 0, 4, 2, 2, 0,-2,-2,-4};
	final static int[] naXDesign1 = new int[] { 8,-2, 0,-5, 3,-5, 0,-2};
	final static int[] naYDesign2 = new int[] { 0,-1, 2,-1, 0,-3};
	final static int[] naXDesign2 = new int[] { 7, 1, 2,-5, 0,-1};
	final static int[] naYDesign3 = new int[] { 0, 0, 1, 1, 2, 2, 0,-1,-1, 0, 2, 2, 1, 2, 3, 3, 2, 0, 1, 3, 2, 0,-4,-1,-2,-2,-4,-2,-1,-1};
	final static int[] naXDesign3 = new int[] { 7, 3, 3, 5, 4, 2, 2, 1, 0,-1,-1, 0, 1, 1, 0,-1,-2,-2,-3,-3,-4,-4,-5,-3,-2,-1,-2, 2, 3, 5};
	final static int[] naYDesign4 = new int[] { 0, 3, 3, 0,-3,-3, 0, 0,-3,-3, 0, 3, 3, 0};
	final static int[] naXDesign4 = new int[] { 0,-2, 0, 2, 0,-2, 0,-1,-3,-5,-3,-5,-3,-1};
	final static int[] naYDesign5 = new int[] { 0, 2, 4, 3, 0,-3,-4,-3,-2,-1, 0, 0,-1,-2,-3,-2, 0, 2, 1, 0,-1,-2};
	final static int[] naXDesign5 = new int[] { 7, 2,-2,-4,-5,-4,-2, 0, 1, 1, 0,-2, 0, 0,-2,-3,-3,-2, 0, 4, 2, 2};
	final static int[] naYDesign6 = new int[] { 0, 2, 4, 0, 2,-2, 0,-4,-2};
	final static int[] naXDesign6 = new int[] { 5, 1,-3,-3, 1, 1,-3,-3, 1};
	final static int[] naYDesign7 = new int[] { 0, 2, 1, 1, 3, 2, 2, 3, 0,-3,-2,-2,-3,-1,-1,-2};
	final static int[] naXDesign7 = new int[] { 6, 1, 1,-2,-5,-2, 0, 0, 8, 0, 0,-2,-5,-2, 1, 1};
	final static int[] naYDesign8 = new int[] { 0, 1, 1, 3, 2, 1, 1, 2, 2, 3, 4, 3, 3, 4, 3, 3, 2, 2, 0,-2,-2,-3,-3,-4,-3,-3,-4,-3,-2,-2,-1,-1,-2,-3,-1,-1};
	final static int[] naXDesign8 = new int[] { 0, 2, 5, 1, 1, 0,-1,-2,-1, 0,-2,-2,-3,-3,-4,-5,-4,-3,-2,-3,-4,-5,-4,-3,-3,-2,-2, 0,-1,-2,-1, 0, 1, 1, 5, 2};
	/* This didnt work. The "final" didn't apply to the actual polygon objects, and Polygon is not Clonable anyway. */
//	final static Polygon[] naDesigns = new Polygon[] {new Polygon(naXDesign0, naYDesign0, naXDesign0.length),
//													  new Polygon(naXDesign1, naYDesign1, naXDesign1.length),
//													  new Polygon(naXDesign2, naYDesign2, naXDesign2.length),
//													  new Polygon(naXDesign3, naYDesign3, naXDesign3.length),
//													  new Polygon(naXDesign4, naYDesign4, naXDesign4.length),
//													  new Polygon(naXDesign5, naYDesign5, naXDesign5.length),
//													  new Polygon(naXDesign6, naYDesign6, naXDesign6.length)};
//	protected Polygon[] pColorShape = naDesigns.clone();
	protected Polygon[] pColorShape;
	
	final static int[] naYFire1Corners = new int[] { 4,  0,-4};
	final static int[] naXFire1Corners = new int[] {-6,-14,-6};
	final static int[] naYFire2Corners = new int[] {-2,  0, 2};
	final static int[] naXFire2Corners = new int[] {-6,-12,-6};
	final static int[] naYFire3Corners = new int[] {-1,  0, 1};
	final static int[] naXFire3Corners = new int[] {-6,-10,-6};
	protected Polygon[] pFire;
	
	public Spaceship() {
		//myModelNumber = 1;
		myDesignNumber = 0;
		pShape = new Polygon(naXCorners, naYCorners, naXCorners.length);
		pColorShape = new Polygon[] {new Polygon(naXDesign0, naYDesign0, naXDesign0.length),
									 new Polygon(naXDesign1, naYDesign1, naXDesign1.length),
									 new Polygon(naXDesign2, naYDesign2, naXDesign2.length),
									 new Polygon(naXDesign3, naYDesign3, naXDesign3.length),
									 new Polygon(naXDesign4, naYDesign4, naXDesign4.length),
									 new Polygon(naXDesign5, naYDesign5, naXDesign5.length),
									 new Polygon(naXDesign6, naYDesign6, naXDesign6.length),
									 new Polygon(naXDesign7, naYDesign7, naXDesign7.length),
									 new Polygon(naXDesign8, naYDesign8, naXDesign8.length)};
		pFire = new Polygon[] {new Polygon(naXFire1Corners, naYFire1Corners, naXFire1Corners.length), 
							   new Polygon(naXFire2Corners, naYFire2Corners, naXFire2Corners.length), 
							   new Polygon(naXFire3Corners, naYFire3Corners, naXFire3Corners.length)};
		changeSize(2);
		
		
		myColor = new Color(105,105,105);
		//myColor = Color.white;
		myCenterX = Asteroids_v2.APPLET_X / 2.0;
		myCenterY = Asteroids_v2.APPLET_Y / 2.0; 
		myDirectionX = 0;
		myDirectionY = 0;
		myPointDirection = 90;
		mySpeedLimit = 15;
		myHPLimit = 5;
		myHP = myHPLimit;
		myHPRegen = 0.005;
		myBulletLimit = 5;
		myBullets = myBulletLimit;
		myBulletRegen = .050;
		mySpecialWeaponChargeLimit = 5;
		mySpecialWeaponCharge = mySpecialWeaponChargeLimit;
		mySpecialWeaponCooldown = .005;
		myHyperspaceChargeLimit = 1;
		myHyperspaceCharge = myHyperspaceChargeLimit;
		myHyperspaceCooldown = .005;
		mySizeMultiplier = 2;
		Radio = new ReinforcementsManager(this);
		Shield = new ShieldManager(this);
		Hyperspace = new HyperspaceManager(this);
		Laser1 = new Laser(this);
		RepelUnit = new Repellant(this);
		LightningManager = new ChainLightningManager(this);
		ZeroLaser = new ZeroLaserManager(this);
	}
	public void accelerate (double dAmount){  
		double dRadians = Math.toRadians(myPointDirection);
		/* change coordinates of direction of travel */
		double dNewX = myDirectionX + ((dAmount) * Math.cos(dRadians));
		double dNewY = myDirectionY + ((dAmount) * Math.sin(dRadians));
		/* accelerate/decelerate if the new direction is lower than the speed limit OR is less than the previous direciton */
		if(Math.hypot(dNewX, dNewY) < mySpeedLimit || Math.hypot(dNewX, dNewY) < Math.hypot(myDirectionX, myDirectionY)){
			myDirectionX = dNewX;
			myDirectionY = dNewY; 
		}
	}
	/** Certain things need to be drawn underneath all the other floaters as opposed to on top. */
	public void drawUnder(Graphics2D g){
		getZeroLaserManager().draw(g);
	}
	public void draw(Graphics2D g){draw(g, false);}
	public void draw (Graphics2D g, boolean bIsAccelerating){
		/* Draw invincibility satellites and other things with a "manager" */
		getShield().draw(g);
		getHyperspace().draw(g);
		getLaser().draw(g);
		getRepellant().draw(g);
		getLightningManager().draw(g);
		getReinforcementsManager().draw(g);
		
		/** Begin drawing ship */
		/* Draw main polygon with a color that contrasts with the design/outline. 
		 * If overall brightness of myColor is low, set color to high and vice versa. */
		Polygon pRotated = getShape();
		if(myColor.getRed() + myColor.getGreen() + myColor.getBlue() < 382.5){ /*  (255 * 3)/2 = 766  */
			g.setColor(new Color(Math.min(255, myColor.getRed() + 150),
								 Math.min(255, myColor.getGreen() + 150),
								 Math.min(255, myColor.getBlue() + 150),
								 myColor.getAlpha()));
		} else {
			g.setColor(new Color(Math.max(0, myColor.getRed() - 150),
								 Math.max(0, myColor.getGreen() - 150),
								 Math.max(0, myColor.getBlue() - 150),
								 myColor.getAlpha()));
		}
		
		g.fillPolygon(pRotated);
		
		/* Draw decorative color polygon and outline of ship in same color */
		g.setColor(myColor);
		Polygon pColorRotated = Util.rotate(pColorShape[myDesignNumber], myPointDirection);
		pColorRotated.translate(getX(), getY());
		g.fillPolygon(pColorRotated);
		g.drawPolygon(pRotated);
		
		/* Draw fire if accelerating [and speed is not super low] */
		if(ZeroLaser.isLaserActive() || (bIsAccelerating && getSpeed() > 1)){
			Polygon[] pFireRotated = new Polygon[] {Util.rotate(new Polygon(pFire[0].xpoints, pFire[0].ypoints, pFire[0].npoints), myPointDirection), 
													Util.rotate(new Polygon(pFire[1].xpoints, pFire[1].ypoints, pFire[1].npoints), myPointDirection),
													Util.rotate(new Polygon(pFire[2].xpoints, pFire[2].ypoints, pFire[2].npoints), myPointDirection)};
			
			pFireRotated[0].translate((int)myCenterX, (int)myCenterY);
			pFireRotated[1].translate((int)myCenterX, (int)myCenterY);
			pFireRotated[2].translate((int)myCenterX, (int)myCenterY);
			
			g.setColor(new Color(255,0,0));
			g.fillPolygon(pFireRotated[0]);
			g.setColor(new Color(255,128,0));
			g.fillPolygon(pFireRotated[1]);
			g.setColor(new Color(255,255,0));
			g.fillPolygon(pFireRotated[2]);
		}
	}
//	public void setModelNumber(int nModelNumber){
//		if (nModelNumber == 1 && myModelNumber != 1){
//			myModelNumber = 1;
//			pShape.npoints = 17; 
//			pShape.xpoints = new int[] {0,4,6,6,4,8,8,4,6,-6,-4,-8,-8,-4,-6,-6,-4};
//			pShape.ypoints = new int[] {10,0,2,4,6,4,0,-4,-6,-6,-4,0,4,6,4,2,0};
//			changeSize(2);
//		}
//		if (nModelNumber == 2 && myModelNumber != 2){
//			myModelNumber = 2;
//			pShape.npoints = 23;
//			pShape.xpoints = new int[] {0, 4,6,6,4,8,10, 4,12, 8, 4, 6,-6,-4,-8,-12,-4,-10,-8,-4,-6,-6,-4};
//			pShape.ypoints = new int[] {10,0,2,4,6,4, 5,11, 6, 0,-4,-6,-6,-4, 0,  6,11,  5, 4, 6, 4, 2, 0};
//			changeSize(2);
//		}
//	}
	//public int getModelNumber(){return myModelNumber;}
	public void changeSize(int nTimes){
		mySizeMultiplier = nTimes;
		for (int nIndex = 0; nIndex < pShape.npoints; nIndex++){ 
			pShape.xpoints[nIndex] *= mySizeMultiplier;
			pShape.ypoints[nIndex] *= mySizeMultiplier;
		}
		for(Polygon colorPoly:pColorShape){
			for (int nIndex = 0; nIndex < colorPoly.npoints; nIndex++){ 
				colorPoly.xpoints[nIndex] *= mySizeMultiplier;
				colorPoly.ypoints[nIndex] *= mySizeMultiplier;
			}
		}
		for(Polygon fire:pFire){
			for (int nIndex = 0; nIndex < fire.npoints; nIndex++){ 
				fire.xpoints[nIndex] *= mySizeMultiplier;
				fire.ypoints[nIndex] *= mySizeMultiplier;
			}
		}
	}
	public HyperspaceManager getHyperspace() {
		return Hyperspace;
	}
	public void regenerate(){
		setSpecialWeaponCharge(getSpecialWeaponCharge()+getSpecialWeaponCooldown());
		setHyperspaceCharge(getHyperspaceCharge()+getHyperspaceCooldown());
		setBullets(getBullets()+getBulletRegen());
		setHP(getHP()+getHPRegen());
	}
	public Polygon getShape() {
		Polygon translatedPoly = new Polygon(pShape.xpoints, pShape.ypoints, pShape.npoints);
		/* Rotate and translate polygon */
		translatedPoly = Util.rotate(translatedPoly, myPointDirection);
		translatedPoly.translate(getX(), getY());
		return translatedPoly;
	}
	public Polygon getBaseShape() {return new Polygon(pShape.xpoints, pShape.ypoints, pShape.npoints);}
	public boolean shouldBeRemoved() {
		return myHP <= 0;
	}
	/**
	 * Returns the point where the ship "cannons" are, so that weapon methods do not have to calculate this themselves. 
	 * 
	 * @return The point where weapons start off.
	 */
	public Point2D.Double getCannonPoint(){
		Point2D.Double cannonPoint = new Point2D.Double();
		cannonPoint.x = getShape().xpoints[0];
		cannonPoint.y = getShape().ypoints[0];
		return cannonPoint;
	}
	/* 
	 * Begin the regular setters and getters! 
	 */
	public void setDesignNumber(int nDesign){myDesignNumber = nDesign;}
	public int getDesignNumber(){return myDesignNumber;}
	public void setHP(double dHP){
		myHP = dHP;
		myHP = Math.min(myHPLimit, myHP);
		myHP = Math.max(0, myHP);
	}
	public double getHP(){return myHP;}
	public void setHPLimit(double dHP){myHPLimit = dHP;}
	public double getHPLimit(){return myHPLimit;}
	public void setHPRegen(double dHPRegen){myHPRegen = dHPRegen;}
	public double getHPRegen(){return myHPRegen;}
	public void setBullets(double dBullets){
		myBullets = dBullets;
		myBullets = Math.min(myBulletLimit, myBullets);
		myBullets = Math.max(0, myBullets);
	}
	public double getBullets(){return myBullets;}
	public void setBulletRegen(double dBulletRegen){myBulletRegen = dBulletRegen;}
	public double getBulletRegen(){return myBulletRegen;}
	public void setBulletLimit(double dBulletLimit){myBulletLimit = dBulletLimit;}
	public double getBulletLimit(){return myBulletLimit;}
	public void setSpecialWeaponCharge(double dSpecialWeaponCharge){
		mySpecialWeaponCharge = dSpecialWeaponCharge;
		mySpecialWeaponCharge = Math.min(mySpecialWeaponChargeLimit, mySpecialWeaponCharge);
		mySpecialWeaponCharge = Math.max(0, mySpecialWeaponCharge);
	}
	public double getSpecialWeaponCharge(){return mySpecialWeaponCharge;}
	public void setSpecialWeaponCooldown(double dSpecialWeaponCooldown){mySpecialWeaponCooldown = dSpecialWeaponCooldown;}
	public double getSpecialWeaponCooldown(){return mySpecialWeaponCooldown;}
	public void setSpecialWeaponChargeLimit(double dSpecialWeaponChargeLimit){mySpecialWeaponChargeLimit = dSpecialWeaponChargeLimit;}
	public double getSpecialWeaponChargeLimit(){return mySpecialWeaponChargeLimit;}
	public void setHyperspaceCharge(double dHyperspaceCharge){
		myHyperspaceCharge = dHyperspaceCharge;
		myHyperspaceCharge = Math.min(myHyperspaceChargeLimit, myHyperspaceCharge);
		myHyperspaceCharge = Math.max(0, myHyperspaceCharge);
	}
	public double getHyperspaceCharge(){return myHyperspaceCharge;}
	public void setHyperspaceCooldown(double dHyperspaceCooldown){myHyperspaceCooldown = dHyperspaceCooldown;}
	public double getHyperspaceCooldown(){return myHyperspaceCooldown;}
	public void setHyperspaceChargeLimit(double dHyperspaceChargeLimit){myHyperspaceChargeLimit = dHyperspaceChargeLimit;}
	public double getHyperspaceChargeLimit(){return myHyperspaceChargeLimit;}
	public void setSpeedLimit(double nLimit){mySpeedLimit = nLimit;}
	public double getSpeedLimit(){return mySpeedLimit;}
	public Laser getLaser(){return Laser1;}
	public ShieldManager getShield(){return Shield;}
	public Repellant getRepellant(){return RepelUnit;}
	public ChainLightningManager getLightningManager(){return LightningManager;}
	public ReinforcementsManager getReinforcementsManager(){return Radio;}
	public ZeroLaserManager getZeroLaserManager(){return ZeroLaser;}
}