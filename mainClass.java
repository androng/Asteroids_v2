import java.awt.*;

/**
 *	This class is only for compiling into a native application. 
 */
public class mainClass {
	  public static void main(String[] args) {
	    Asteroids_v2 myApplet = new Asteroids_v2(); /* define applet of interest */
	    Frame myFrame = new Frame("Applet Holder"); /* create frame with title */

	    /* Call applet's init method (since Java App does not call it as a browser automatically does) */
	    myApplet.init();	

	    myApplet.start();
	    /* add applet to the frame */
	    myFrame.add(myApplet, BorderLayout.CENTER);
	    myFrame.pack(); /* set window to appropriate size (for its elements) */
	    myFrame.setVisible(true); /* usual step to make frame visible */

	}
}