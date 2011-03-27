/**
 * The option class is just a container for 3 variables:
 * 
 *	1. Name. This shows up next to the checkbox in the dialog, and is also the key in the options Map.
 * 		Redundant, yes, but it increases potential for a small cost.
 * 	2. Description.
 * 	3. Boolean for enabled value.
 */
public class Option {
	private String sName;
	private String sDescription;
	private boolean bEnabled;

	public Option(String name, boolean enabled, String description) {
		sName = name;
		sDescription = description;
		bEnabled = enabled;
	}
	/** Switches the boolean value to the opposite of what it is already. */
	public void setOpposite(){
		bEnabled = !bEnabled;
	}

	public String getName() {return sName;}
	public void setName(String name) {sName = name;}

	public String getDescription() {return sDescription;}
	public void setDescription(String description) {sDescription = description;}
	
	public boolean isEnabled() {return bEnabled;}
	public void setEnabled(boolean enabled) {bEnabled = enabled;}
}
