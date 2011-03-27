import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class OptionsDialog extends JDialog {
	/** The actionListener to forward checkbox events to. */
	private ActionListener listener;
	/** The ship to modify when the ColorChooser is changed */
	private Spaceship ship;
	/** List of keys that cannot be assigned to a function. 
	 * In addition, any keys already bound to the keys on the banlist will not be configurable via the panel. */
	private ArrayList<String> bannedKeys;
	/** The button used to reset all controls to the default. */
	private JButton btRevert;
	
	/* References need to be kept to these because they can be modified/read later. */
	private Map<String, ArrayList<KeyHolder>> controls;
	
	public OptionsDialog(ActionListener al, Map options, Map controls, Spaceship ship){
		/* Not sure if this is necessary or not */
		super();
		
		listener = al;
		this.ship = ship;
		bannedKeys = new ArrayList<String>();
		bannedKeys.add(new String("Escape"));
		/* Add all the F keys */
		for (int nNum = 1; nNum <= 16; nNum++){
			bannedKeys.add(new String("F" + nNum));
		}
		btRevert = new JButton("Revert to defaults");
		this.controls = controls;
		
		populate(options, controls);
		setTitle("Asteroids options");
		
		pack();
		//setVisible(true);
	}
	private void populate(Map options, Map controls){
		/* "Proper" schematic for JDialogs: dialog.getContentPane().add(whatever) */
		
		/* Master content pane */
		JTabbedPane tabbedPane = new JTabbedPane();
		setContentPane(tabbedPane);
		tabbedPane.setPreferredSize(new Dimension(547,400));
		//tabbedPane.addChangeListener(this);
		
		/* Initialize content pane and tabs. Yes, GridBagLayout. BoxLayout was giving me a headache with its
		 * stupid alignment issues with components that don't have a restricted maximum size, and GridLayout 
		 * was expanding the spaces between checkboxes when the window was expanded. */
		JComponent panelOptions = new JPanel();
		panelOptions.setLayout(new GridBagLayout());
		tabbedPane.addTab("Options", panelOptions);
		JComponent panelControls = new JPanel(new GridBagLayout());
		tabbedPane.addTab("Controls", panelControls);
		JComponent panelColor = new JPanel(new GridBagLayout());
		tabbedPane.addTab("Ship color", panelColor);
		JComponent panelSpeedLimit = new JPanel(new GridBagLayout());
		tabbedPane.addTab("Speed limit", panelSpeedLimit);
		
		/* Add padding to each panel */
		Border padding = BorderFactory.createEmptyBorder(15,15,15,15);
        panelOptions.setBorder(padding);
        panelControls.setBorder(padding);
        panelColor.setBorder(padding);
        panelSpeedLimit.setBorder(padding);
		
		/** Individual components that populate panels starting with options panel */
		/* GridBagContstraint object that governs component placement in options panel */
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0; 
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        /* The text box that explains each option. */
        final JTextAreaMultiLine explanationLabel;
		explanationLabel = new JTextAreaMultiLine(" ");
		
        /* The MouseListener that will change the explanation text. */
        MouseListener mlChangeExplanation = new MouseAdapter(){
        	public void mouseEntered(MouseEvent mEvent) {
        		/* Change the explanation label to whatever option the mouse is over */
        		explanationLabel.setText(((JCheckBoxWithOption) mEvent.getSource()).getOption().getDescription());
        	}
        	public void mouseExited(MouseEvent mEvent) {
        		/* Clear the explanation label text */
        		explanationLabel.setText(" ");
        	}
        };
        
        /* Iterate through options map to add checkboxes on the first panel */
		Set<Map.Entry<String, Option>> mapKeys = options.entrySet();
		
		for(Map.Entry<String, Option> mapEntry: mapKeys){
			JCheckBox optionBox = new JCheckBoxWithOption(mapEntry.getValue().getName(), mapEntry.getValue());
			optionBox.setSelected(mapEntry.getValue().isEnabled());
			optionBox.setActionCommand(mapEntry.getKey());
			optionBox.addActionListener(listener);
			optionBox.addMouseListener(mlChangeExplanation);
			panelOptions.add(optionBox, constraints);
		}
		
		/* let the explanation box get all the extra space from a windows resize, 
		 * while keeping at the top left of its cell. Also give the explanation box a top margin.*/
		constraints.weightx = 1.0f;
		constraints.weighty = 1.0f;
		constraints.anchor = GridBagConstraints.PAGE_START;
		constraints.insets = new Insets(10,0,0,0);
        panelOptions.add(explanationLabel, constraints);
        
        /** Controls panel */
        /* Create a new GridBagConstraints */
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 15, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
		
        /* Instructions */
        JTextAreaMultiLine instructions = new JTextAreaMultiLine("To assign new keys to a function, click the text box that contains the original keys, then press the keys that you wish to assign to the function. You can assign multiple keys for one function. (e.g. \"Up\" and \"W\" for acceleration) Press Escape when done. If more than one function shares a single key, that key will trigger all of its bound functions.");
        //instructions.setBorder(BorderFactory.createTitledBorder("Choose a design for your ship!"));
        panelControls.add(instructions, constraints);
        
        /* Reset button */
        panelControls.add(btRevert, constraints);
        
        /* Iterate through controls map to add text fields on the second tab 
         * But first get reference to defaultControls map so that it doesn't have to be generated repeatedly. */
        Map<String, ArrayList<KeyHolder>> defaultControls = Asteroids_v2.getDefaultControls();
        Set<Map.Entry<String, ArrayList<KeyHolder>>> controlSet = controls.entrySet();
		
		for(Map.Entry<String, ArrayList<KeyHolder>> mapEntry: controlSet){
			/* If the first key defined for this function is on the banlist, skip this function. */
			if(bannedKeys.contains(mapEntry.getValue().get(0).getKeyText())){
				continue;
			}
			
			enableControlChange(mapEntry.getKey(), defaultControls.get(mapEntry.getKey()));
		} 

        /** Ship color panel */
        /* Create a new  GridBagConstraints to avoid confusion */
        constraints = new GridBagConstraints();
        constraints.gridx = 0; 
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0f;
		
        JPanel subPanelButtons = new JPanel();
        subPanelButtons.setBorder(BorderFactory.createTitledBorder("Choose a design for your ship!"));
        panelColor.add(subPanelButtons, constraints);
        
        /* ActionListener and Button group for design selection */
        ActionListener alChangeShipColor = new ActionListener(){
        	public void actionPerformed(ActionEvent aEvent) {
				ship.setDesignNumber(Integer.parseInt(aEvent.getActionCommand()));
			}
        };
        ButtonGroup buttonGroup = new ButtonGroup();
        for(int nDesign = 0; nDesign < Spaceship.NUM_DESIGNS; nDesign++){
        	JRadioButton designSelector = new JRadioButton(new Integer(nDesign).toString());
        	designSelector.setActionCommand(new Integer(nDesign).toString());
        	designSelector.addActionListener(alChangeShipColor);
        	buttonGroup.add(designSelector);
        	subPanelButtons.add(designSelector);
        	
        	if(nDesign == ship.getDesignNumber()){
        		designSelector.setSelected(true);
        	}
        }
        
        /* Add a small top margin to the next component */
        constraints.insets = new Insets(10,0,0,0);
        
        /* ChangeListener for ColorChooser and ColorChooser initialization. */
        final JColorChooser colorChooser = new JColorChooser();
        ChangeListener colorChangeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				ship.setColor(colorChooser.getColor());
			}
		};
		colorChooser.setBorder(BorderFactory.createTitledBorder("Choose a color for the design of your ship!"));
        colorChooser.getSelectionModel().addChangeListener(colorChangeListener);
        colorChooser.setPreviewPanel(new JPanel()); /* Removes the preview panel. */
        panelColor.add(colorChooser, constraints);
        
        /** Ship speed limit panel */
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.insets = new Insets(0, 0, 15, 0);
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        final JLabel sliderLabel = new JLabel("Ship speed limit: " + ship.getSpeedLimit(), JLabel.CENTER);
        JSlider speedLimitSelector = new JSlider(JSlider.HORIZONTAL, 0, 30, (int) ship.getSpeedLimit());
        speedLimitSelector.setMajorTickSpacing(5);
        speedLimitSelector.setMinorTickSpacing(1);
        speedLimitSelector.setPaintTicks(true);
        speedLimitSelector.setPaintLabels(true);
        
        /* ChangeListener for slider */
        ChangeListener speedLimitChangeListener = new ChangeListener() {
        	public void stateChanged(ChangeEvent changeEvent) {
        		int nValue = ((JSlider) changeEvent.getSource()).getValue();
        		sliderLabel.setText("Ship speed limit: " + nValue);
        		ship.setSpeedLimit(nValue);
        	}
        };
        speedLimitSelector.addChangeListener(speedLimitChangeListener);
        panelSpeedLimit.add(sliderLabel, constraints);
        panelSpeedLimit.add(speedLimitSelector, constraints);
	}
	/**
	 * Adds a place in the Controls panel to change the key assignments for the specified function.
	 * This is used at initialization and for later in the game when other functions are enabled.
	 * 
	 * @param sFunction The function to add components for.
	 * @param defaultControls The default controls array for this function.
	 */
	public void enableControlChange(final String sFunction, final ArrayList<KeyHolder> defaultKeys){
		/* Get the panel to add the components to */
		JComponent panelControls = (JComponent) getContentPane().getComponent(((JTabbedPane) getContentPane()).indexOfTab("Controls"));
		
		/* Create a new GridBagConstraints */
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2,2,2,2);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
		JLabel keyNameLabel = new JLabel(sFunction, JLabel.RIGHT);
		final JTextField keysField = new JTextField( 10);
		keyNameLabel.setLabelFor(keysField);
		keysField.setEditable(false);
		
		/* Get and set the text for the keyField from the controls map */
		String sKeyFieldText = "";
		for(KeyHolder keyHolder: controls.get(sFunction)){
			if(sKeyFieldText.length() != 0){
				sKeyFieldText += ", ";
			}
			sKeyFieldText += keyHolder.getKeyText();
		}
		keysField.setText(sKeyFieldText);
 
		/* Layout properties for labels */
		constraints.weightx = .25f;
		constraints.gridwidth = 1;
        panelControls.add(keyNameLabel, constraints);
		
		/* Start a new line after the addition of this component along with other properties */
        constraints.weightx = 1.0f;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		panelControls.add(keysField, constraints);
		
		/* The list of new keys that will be populated by the KeyListener */
		final ArrayList<KeyHolder> newKeys = new ArrayList<KeyHolder>();
		
		/* Now declare the KeyListener for the textField */
		final KeyListener kListenerAddKeys = new KeyAdapter(){
			
			public void keyReleased(KeyEvent kEvent) {
				KeyHolder keyH = new KeyHolder(kEvent.getKeyCode(), kEvent.getKeyLocation());
				/* If the key released was Escape, replace the keys ArrayList for that function and stop listenting. */
				if(keyH.getKeyText().equals("Escape")){
					controls.put(sFunction, newKeys);
					
					JTextField textField = (JTextField) kEvent.getSource();
					textField.removeKeyListener(this);
					textField.setBackground(Color.white);
					if(textField.getText().equals("Press the keys you want to assign, then Escape.")){
						textField.setText("");
					}
				/* If the key released is not a banned key AND it is not already in the ArrayList
				 * add it to the ArrayList and change the text field. */
				} else if(bannedKeys.contains(keyH.getKeyText()) == false && newKeys.contains(keyH) == false) {
					newKeys.add(keyH);
					
					JTextField textField = (JTextField) kEvent.getSource();
					//String sTextFieldText = ((JTextField) kEvent.getSource()).getText();
					if(textField.getText().equals("Press the keys you want to assign, then Escape.")){
						textField.setText("");
					}
					if(textField.getText().length() != 0){
						textField.setText(textField.getText() + ", ");
					}
					textField.setText(textField.getText() + keyH.getKeyText());
				}
			}
        };
        
        /* And now the MouseListener that activates the KeyListener */
        //MouseListener mListenerActivateKeyListener = new MouseListener(){
        keysField.addMouseListener(new MouseAdapter(){
        	public void mouseClicked(MouseEvent e) {
        		keysField.addKeyListener(kListenerAddKeys);
        		keysField.setBackground(Color.green);
        		keysField.setText("Press the keys you want to assign, then Escape.");
        		newKeys.clear();
			}
        });
        
        /* And lastly, the listener for the reset button that reverts the textfield back to defaults. */
        btRevert.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent aEvent) {
        		controls.put(sFunction, defaultKeys);
				
        		keysField.removeKeyListener(kListenerAddKeys);
        		keysField.setBackground(Color.white);
        		/* Get and set the text for the keyField from the default controls array */
        		String sKeyFieldText = "";
        		for(KeyHolder keyHolder: defaultKeys){
        			if(sKeyFieldText.length() == 0){
        				sKeyFieldText += ", ";
        			}
        			sKeyFieldText += keyHolder.getKeyText();
        		}
        		keysField.setText(sKeyFieldText);
			}
        });
	}
//	public void stateChanged(ChangeEvent cEvent) {
//		JTabbedPane sourceTabbedPane = (JTabbedPane) cEvent.getSource();
//		final int INDEX = sourceTabbedPane.getSelectedIndex();
//		final Component component = sourceTabbedPane.getComponentAt(INDEX);
//		setBounds(new Rectangle(getX(), getY(), component.getPreferredSize().width, component.getPreferredSize().height));
//	}
}

/**
 * It's a JCheckBox with +1 member variable, the Option. (another minor class)
 * This is used so that extra information can be attached to the checkbox.
 */
class JCheckBoxWithOption extends JCheckBox {
	Option option;
	
	public JCheckBoxWithOption(String text, Option option){
		super(text);
		this.option = option;
	}
	
	public void setOption(Option newOption){this.option = newOption;}
	public Option getOption(){return option;}
}

/**
 *	It's a mystical line breaking text area. I can't believe that this isn't the default functionality.
 *
 */
class JTextAreaMultiLine extends JTextArea {
	JTextAreaMultiLine(String text){
		super(text);
		
		/* All this is for a mystical line-wrapping text label/area. */
		setEditable(false);
		setFocusable(false);
		setLineWrap(true);
		setWrapStyleWord(true);
	}
}