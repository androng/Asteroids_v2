import java.util.Timer;
import java.util.TimerTask;

/**
 *	The ProgressTimer class is what is replacing the Stageholder class from the previous version.
 *	This is my solution for all the little animations that occur in the game. 
 *
 *	I didn't want to have a "master frame number" variable, and I didn't want to have to 
 *	increment anything in any loops. (e.g. nFrameHolder++) Using the time also makes it 
 *	more natural. How are you supposed to know what will result when your animation is 
 *	based on frame numbers? 
 */
public class ProgressTimer extends Timer{
	private long nInitialTime;
	private long nEndTime;
	private long nDuration;
	/**
	 * The constructor for the ProgressTimer class. 
	 * First calls the Timer() constructor. Initializes nInitialTime as the current time,
	 * and nEndTime as the current time + duration. If specified duration is negative, this
	 * object will act as if it is always complete. (percent complete 1.0, percent todo 0.0)
	 * 
	 * @param tTask The TimerTask to be executed at the end of the animation.
	 * @param nDuration The amount of time in milliseconds that the animation lasts.
	 */
	public ProgressTimer(TimerTask tTask, long nDuration){
		super();
		nInitialTime = System.currentTimeMillis();
		nEndTime = nInitialTime + nDuration;
		this.nDuration = nDuration;
		if(nDuration > 0)
			schedule(tTask, nDuration);
	}
	/**
	 * Returns percentage of progression ranging from 0 to 1.0. Will return 1.0 if 
	 * duration is negative.
	 * 
	 * @return Percentage of progression
	 */
	public double PercentProgressed(){
		if(isIndefinite())
			return 1.0;
		
		return ((double)System.currentTimeMillis() - nInitialTime) / getDuration();
	}
	/**
	 * Returns the percent of time left to go until end time. Ranges from 1.0 to 0. 
	 * Returns 0 if duration is indefinite.
	 * 
	 * @return Percent of time to go until end time.
	 */
	public double PercentToGo(){
		if(isIndefinite())
			return 0;
		
		double dPercentToGo = 1.0 - PercentProgressed();
		if(dPercentToGo < 0)
			return 0;
		
		return dPercentToGo;
	}
	/**
	 * Returns the time passed since this timer was instantiated.
	 * 
	 * @return Time progressed in milliseconds. Returns duration if indefinite.
	 */
	public long timeProrgressed(){
		if(isIndefinite())
			return nDuration;
		
		return System.currentTimeMillis() - nInitialTime;
	}
	/**
	 * Returns the time in milliseconds remaining on the timer.
	 * 
	 * @return Time left in milliseconds. Returns 0 if indefinite. 
	 *         Returns a negative value if past duration.
	 */
	public long timeToGo(){
		if(isIndefinite())
			return 0;
		
		return nEndTime - System.currentTimeMillis();
	}
	public void g(){
		
	}
	/**
	 * Quick way to find out whether the animation is complete. (past 1.0 ratio)
	 * 
	 * @return	True if animation is past its specified span of time, false if not.
	 */
	public boolean isDone(){
		return (double)System.currentTimeMillis() - nInitialTime / (nEndTime - nInitialTime) >= 1.0;
	}
	/**
	 * How to find out if the animation lasts forever. Checks whether duration is negative.
	 * 
	 * @return True if duration is negative.
	 */
	public boolean isIndefinite(){
		return nDuration < 0;
	}
	/**
	 * Quick way to find out how far along an animation is going in terms of the number of "phases".
	 * Takes the total number of phases and returns the current one. For example, if PercentProgressed()
	 * returns .5 and the total number of phases is 3, this function will return 2 since .5 is between 
	 * .33 and .66.
	 * 
	 * @param nTotalPhases The total number of phases that the animation should be divided into.
	 * @throws IllegalArgumentException if nTotalPhases is negative.
	 * @return The current phase number. Returns nTotalPhases if duration is indefinite.
	 */
	public int Phase(int nTotalPhases){
		if(nTotalPhases < 0)
			throw new IllegalArgumentException("Non-positive total phase number.");
		if(isIndefinite())
			return nTotalPhases;
		return 1 + (int)(PercentProgressed() / (1.0 / nTotalPhases));
	}
	/**
	 * Same as Phase(), but returns the number of phases left instead of the current one. 
	 * 
	 * @param nTotalPhases The total number of phases that the animation should be divided into.
	 * @throws IllegalArgumentException if nTotalPhases is negative.
	 * @return The current number of phases left. Returns 0 if duration is indefinite.
	 */
	public int PhasesLeft(int nTotalPhases){
		if(nTotalPhases < 0)
			throw new IllegalArgumentException("Non-positive total phase number.");
		if(isIndefinite())
			return 0;
		
		return nTotalPhases - Phase(nTotalPhases) + 1;
	}
	public long getInitialTime(){return nInitialTime;}
	public long getEndTime(){return nEndTime;}
	public long getDuration(){return nDuration;}
}
