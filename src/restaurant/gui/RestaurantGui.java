package restaurant.gui;

import restaurant.CustomerAgent;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
/**
 * Main GUI class.
 * Contains the main frame and subsequent panels
 */
public class RestaurantGui extends JFrame implements ActionListener {
	
	AnimationPanel animationPanel = new AnimationPanel();
	private JPanel controlPanel = new JPanel();
	
    /* restPanel holds 2 panels
     * 1) the staff listing, menu, and lists of current customers all constructed
     *    in RestaurantPanel()
     * 2) the infoPanel about the clicked Customer (created just below)
     */    
    private RestaurantPanel restPanel = new RestaurantPanel(this);
    
    /* infoPanel holds information about the clicked customer, if there is one*/
    private JPanel infoPanel;
    private JButton pause;
    private JTextField infoLabel; //part of infoPanel
    private JCheckBox stateCB;//part of infoLabel
    
    private JPanel idPanel;
    private JLabel idLabel;
    private ImageIcon icon;
    private JLabel iconLabel;

    private Object currentPerson;/* Holds the agent that the info is about.
    								Seems like a hack */

    /**
     * Constructor for RestaurantGui class.
     * Sets up all the gui components.
     */
    public RestaurantGui() {
        int WINDOWX = 450;
        int WINDOWY = 400;
    	
    	setBounds(50, 50, WINDOWX*2, WINDOWY);

    	setLayout(new GridLayout(1, 2, 10, 0));
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setSize(WINDOWX,WINDOWY);

        Dimension restDim = new Dimension(WINDOWX, (int) (WINDOWY * .6));
        restPanel.setPreferredSize(restDim);
        restPanel.setMinimumSize(restDim);
        restPanel.setMaximumSize(restDim);
        controlPanel.add(restPanel, BorderLayout.CENTER);
        
        // Now, setup the info panel
        Dimension infoDim = new Dimension(WINDOWX, (int) (WINDOWY * .15));
        infoPanel = new JPanel();
        infoPanel.setPreferredSize(infoDim);
        infoPanel.setMinimumSize(infoDim);
        infoPanel.setMaximumSize(infoDim);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));

        stateCB = new JCheckBox();
        stateCB.setText("Hungry?");
        stateCB.addActionListener(this);
        stateCB.setEnabled(false);

        infoPanel.setLayout(new GridLayout(1, 3, 30, 0));
        
        infoLabel = new JTextField(); 
        pause = new JButton("Pause");
        pause.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		if (pause.getText().equals("Pause")) {
        			pause.setText("Resume");
        			restPanel.pauseAgents();
        			animationPanel.pauseAnimation();
        		} else {
        			pause.setText("Pause");
        			restPanel.resumeAgents();
        			animationPanel.resumeAnimation();
        		}
        	}
        });

        infoPanel.add(pause);
        infoPanel.add(infoLabel);
        infoPanel.add(stateCB);
        controlPanel.add(infoPanel, BorderLayout.NORTH);
        
        //Set up the ID panel
        idPanel = new JPanel();
        idPanel.setBorder(BorderFactory.createTitledBorder("ID"));
        idPanel.setLayout(new FlowLayout());
        
        idLabel = new JLabel();
        idLabel.setText("Josh DiGiovanni");
        idPanel.add(idLabel);
        
        icon = new ImageIcon("C:/Users/Josh/CS201/restaurant_jmdigiov/src/restaurant/gui/icon.png");
        iconLabel = new JLabel(icon);
        idPanel.add(iconLabel);
        
        controlPanel.add(idPanel, BorderLayout.SOUTH);
        
        add(controlPanel);
        add(animationPanel);
    }
    
    /**
     * Returns the text from infoLabel
     */
    public String getInfoLabelText() {
    	return infoLabel.getText();
    }
    
    /**
     * updateInfoPanel() takes the given customer (or, for v3, Host) object and
     * changes the information panel to hold that person's info.
     *
     * @param person customer (or waiter) object
     */
    public void updateInfoPanel(Object person) {
        currentPerson = person;

        if (person instanceof CustomerAgent) {
            CustomerAgent customer = (CustomerAgent) person;
          //Should checkmark be there? 
            stateCB.setSelected(customer.getGui().isHungry());
          //Is customer hungry? Hack. Should ask customerGui
            stateCB.setEnabled(!customer.getGui().isHungry());
          // Hack. Should ask customerGui
            infoLabel.setText(customer.getName());
        }
        infoPanel.validate();
    }
    /**
     * Action listener method that reacts to the checkbox being clicked;
     * If it's the customer's checkbox, it will make him hungry
     * For v3, it will propose a break for the waiter.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == stateCB) {
            if (currentPerson instanceof CustomerAgent) {
                CustomerAgent c = (CustomerAgent) currentPerson;
                c.getGui().setHungry();
                stateCB.setEnabled(false);
            }
        }
    }
    /**
     * Message sent from a customer gui to enable that customer's
     * "I'm hungry" checkbox.
     *
     * @param c reference to the customer
     */
    public void setCustomerEnabled(CustomerAgent c) {
        if (currentPerson instanceof CustomerAgent) {
            CustomerAgent cust = (CustomerAgent) currentPerson;
            if (c.equals(cust)) {
                stateCB.setEnabled(true);
                stateCB.setSelected(false);
            }
        }
    }
    /**
     * Main routine to get gui started
     */
    public static void main(String[] args) {
        RestaurantGui gui = new RestaurantGui();
        gui.setTitle("csci201 Restaurant");
        gui.setVisible(true);
        gui.setResizable(false);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
