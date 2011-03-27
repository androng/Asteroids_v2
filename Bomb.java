import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Timer;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

import com.jhlabs.image.BlurFilter;
import com.jhlabs.image.BoxBlurFilter;
import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.MaskFilter;
import com.jhlabs.image.RGBAdjustFilter;


public class Bomb extends Floater implements  TimingTarget{
	final static int FUSE_TIME = 250;
	final static int EXPLOSION_TIME = 100;
	final static int EFFECT_DIAMETER = 300;
	
	private boolean bDecaying;
	private Timer tFuse;
	private Timer tExplosionDraw;
	private Animator animRadius;
	private BufferedImage explosionCanvas;
	private ArrayList<Particle> Particles;
	private double dExplosionCenterX;
	private double dExplosionCenterY;
	
	public Bomb(double dStartX, double dStartY, double dSpeedX, double dSpeedY){
		bDecaying = false;
		tFuse = new Timer(FUSE_TIME, new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				ignite();
			}
		});
		tExplosionDraw = new Timer(Asteroids_v2.REFRESH_RATE * 2, new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				drawExplosion();
			}
		});
		animRadius = new Animator(EXPLOSION_TIME, this);
		explosionCanvas = new BufferedImage(EFFECT_DIAMETER, EFFECT_DIAMETER, BufferedImage.TRANSLUCENT);
		Particles = new ArrayList<Particle>();
		
		tFuse.setRepeats(false);
		tFuse.start();
		
		myHP = 100;
		nSize = 20;
		myColor = Color.gray;
		myCenterX = dStartX;
		myCenterY = dStartY;
		myDirectionX = dSpeedX;
		myDirectionY = dSpeedY;
	}
	public void draw(Graphics2D original) {
		Graphics2D g = (Graphics2D)explosionCanvas.getGraphics();
		g.setColor(myColor);
		if(isExploding() == false && isDecaying() == false){
			g.fill(new Ellipse2D.Double(explosionCanvas.getWidth()/2 - nSize / 2.0, explosionCanvas.getWidth()/2 - nSize / 2.0, nSize, nSize));
		} else if (isExploding()){
			g.draw(new Ellipse2D.Double(explosionCanvas.getWidth()/2 - nSize / 2.0, explosionCanvas.getWidth()/2 - nSize / 2.0, nSize, nSize));
		}
		original.drawImage(explosionCanvas, (int)myCenterX - explosionCanvas.getWidth()/2, (int)myCenterY - explosionCanvas.getHeight()/2, null);
	}
	private void drawExplosion(){
		/* Fade the image a bit */
		Graphics2D g = (Graphics2D)explosionCanvas.getGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, .05f));
		g.fillRect(0, 0, explosionCanvas.getWidth(), explosionCanvas.getHeight());
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1f));
		g.setClip(new Ellipse2D.Double(explosionCanvas.getWidth()/2 - nSize / 2.0, explosionCanvas.getWidth()/2 - nSize / 2.0, nSize, nSize));
		
		/* Move all the particles and update the location if the location has moved */
		if(dExplosionCenterX != myCenterX || dExplosionCenterY != myCenterY){
			for(Particle particle:Particles){
				particle.setX(particle.getX() + (int)(myCenterX - dExplosionCenterX));
				particle.setY(particle.getY() + (int)(myCenterY - dExplosionCenterY));
			}
			dExplosionCenterX = myCenterX;
			dExplosionCenterY = myCenterY;
		}
		
		/* Add a few particles */
		for(int nNum = 1; nNum <= 50; nNum++){
			Particle newcomer = new Particle(explosionCanvas.getWidth() / 2, explosionCanvas.getHeight() / 2);
			newcomer.setDirectionX(newcomer.getDirectionX() * .5);
			newcomer.setDirectionY(newcomer.getDirectionY() * .5);
			Particles.add(newcomer);
		}
		
		Iterator<Particle> iterator = Particles.iterator();
		while(iterator.hasNext()){
			Particle particle = iterator.next();
			particle.draw(g);
			particle.move(Asteroids_v2.APPLET_X, Asteroids_v2.APPLET_Y);
			if(particle.shouldBeRemoved())
				iterator.remove();
		}
		
		//BlurFilter op = new BlurFilter();
		//BoxBlurFilter op = new BoxBlurFilter(2, 0, 1);
		//GaussianFilter op = new GaussianFilter();
		//RGBAdjustFilter op = new RGBAdjustFilter(1.0f, 1.0f, 0.0f);
	//	TritoneFilter op;
	//	explosionCanvas = op.filter(explosionCanvas, null);
	}
	private void ignite(){
		myColor = Color.green;
		animRadius.start();
		tExplosionDraw.start();
		myDirectionX = 0;
		myDirectionY = 0;
	}
	public Shape getShape() {
		if(isDecaying() == false){
			return new Ellipse2D.Double(myCenterX - (nSize / 2.0), myCenterY - (nSize / 2.0), nSize, nSize);
		} else {
			return null;
		}
	}
	public boolean shouldBeRemoved() {
		return false;
	}
	public void begin() {System.out.println(animRadius.getRepeatCount());}
	public void end() {
		bDecaying = true;
		dExplosionCenterX = myCenterX;
		dExplosionCenterY = myCenterY;
	}
	public void repeat() {}
	public void timingEvent(float fraction) {
		nSize = (int) (35 + fraction * EFFECT_DIAMETER);
	}
	public boolean isExploding(){return animRadius.isRunning();}
	public boolean isDecaying(){return bDecaying;}
}
