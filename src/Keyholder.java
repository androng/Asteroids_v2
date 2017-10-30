import java.awt.event.KeyEvent;

class KeyHolder{
	
	private int myKeyCode;
	private int myKeyLocation;
	private String myKeyText;
	/** Used for keys that should only act once during each press. For example, if you have a 
	 * key that turns something on or off, when pressed, it should only do one of those things.
	 * It should not do the other one until after the key has been released. (ie not every frame) 
	 * Initially set to false, then when polled (checked) the first time, the polling function
	 * will set this to true. */
	private boolean bPolled;
	
	public KeyHolder(int nKeyCode, int nKeyLocation){
		myKeyCode = nKeyCode;
		myKeyLocation = nKeyLocation;
		myKeyText = KeyEvent.getKeyText(myKeyCode);
		bPolled = false;
		
		/* Special case: if the key is a "left" or "right" key, then append that to myKeyText */
		if(myKeyLocation == KeyEvent.KEY_LOCATION_LEFT){
			myKeyText = "Left " + myKeyText;
		} else if(myKeyLocation == KeyEvent.KEY_LOCATION_RIGHT){
			myKeyText = "Right " + myKeyText;
		}
	}
	public int getKeyCode(){return myKeyCode;}
	public int getKeyLocation(){return myKeyLocation;}
	public String getKeyText(){return myKeyText;}
	public void setPolled(boolean bPolled){this.bPolled = bPolled;}
	public boolean getPolled(){return bPolled;}
	@Override
	public String toString(){
		return myKeyCode+", "+myKeyText+", "+myKeyLocation;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyHolder other = (KeyHolder) obj;
		if (myKeyCode != other.myKeyCode)
			return false;
		if (myKeyLocation != other.myKeyLocation)
			return false;
		if (myKeyText == null) {
			if (other.myKeyText != null)
				return false;
		} else if (!myKeyText.equals(other.myKeyText))
			return false;
		return true;
	}
}