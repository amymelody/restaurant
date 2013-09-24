package restaurant.gui;

import restaurant.CustomerAgent;
import restaurant.HostAgent;
import restaurant.WaiterAgent;
import restaurant.CookAgent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * Panel in frame that contains all the restaurant information,
 * including host, cook, waiters, and customers.
 */
public class RestaurantPanel extends JPanel {

    //Host, cook, waiters and customers
    private HostAgent host = new HostAgent("Sarah");
    private CookAgent cook = new CookAgent("John");
    private Vector<CustomerAgent> customers = new Vector<CustomerAgent>();
    private Vector<WaiterAgent> waiters = new Vector<WaiterAgent>();

    private JPanel restLabel = new JPanel();
    private ListPanel customerPanel = new ListPanel(this, "Customers");
    private ListPanel waiterPanel = new ListPanel(this, "Waiters");
    private JPanel group = new JPanel();

    private RestaurantGui gui; //reference to main gui

    public RestaurantPanel(RestaurantGui gui) {
        this.gui = gui;

        /*WaiterAgent w = new WaiterAgent("Josh");
 		WaiterGui g = new WaiterGui(w);
 		gui.animationPanel.addGui(g);
 		w.setHost(host);
 		w.setCook(cook);
 		w.setGui(g);
 		waiters.add(w);
 		w.startThread();
 		host.addWaiter(w);
     		
 		WaiterAgent w2 = new WaiterAgent("JD");
 		WaiterGui g2 = new WaiterGui(w2);
 		gui.animationPanel.addGui(g2);
 		w2.setHost(host);
 		w2.setCook(cook);
 		w2.setGui(g2);
 		waiters.add(w2);
 		w2.startThread();
 		host.addWaiter(w2);
 		
 		WaiterAgent w3 = new WaiterAgent("AJ");
 		WaiterGui g3 = new WaiterGui(w3);
 		gui.animationPanel.addGui(g3);
 		w3.setHost(host);
 		w3.setCook(cook);
 		w3.setGui(g3);
 		waiters.add(w3);
 		w3.startThread();
 		host.addWaiter(w3);*/
        
        host.startThread();
        cook.startThread();

        setLayout(new GridLayout(1, 2, 20, 20));
        group.setLayout(new GridLayout(1, 3, 10, 10));

        group.add(customerPanel);
        group.add(waiterPanel);

        initRestLabel();
        add(restLabel);
        add(group);
    }
    
    /**
     * Returns the text from RestaurantGui's infoLabel
     */
    public String getInfoText() {
    	return gui.getInfoLabelText();
    }
    
    public void pauseAgents() {
    	host.pause();
    	cook.pause();
    	for (WaiterAgent w : waiters) {
    		w.pause();
    	}
    	for (CustomerAgent c : customers) {
    		c.pause();
    	}
    }
    
    public void resumeAgents() {
    	host.resume();
    	cook.resume();
    	for (WaiterAgent w : waiters) {
    		w.resume();
    	}
    	for (CustomerAgent c : customers) {
    		c.resume();
    	}
    }

    /**
     * Sets up the restaurant label that includes the menu,
     * and host and cook information
     */
    private void initRestLabel() {
        JLabel label = new JLabel();
        //restLabel.setLayout(new BoxLayout((Container)restLabel, BoxLayout.Y_AXIS));
        restLabel.setLayout(new BorderLayout());
        label.setText(
                "<html><h3><u>Tonight's Staff</u></h3><table><tr><td>host:</td><td>" + host.getName() + "</td></tr></table><h3><u> Menu</u></h3><table><tr><td>Steak</td><td>$15.99</td></tr><tr><td>Chicken</td><td>$10.99</td></tr><tr><td>Salad</td><td>$5.99</td></tr><tr><td>Pizza</td><td>$8.99</td></tr></table><br></html>");

        restLabel.setBorder(BorderFactory.createRaisedBevelBorder());
        restLabel.add(label, BorderLayout.CENTER);
        restLabel.add(new JLabel("               "), BorderLayout.EAST);
        restLabel.add(new JLabel("               "), BorderLayout.WEST);
    }

    /**
     * When a customer or waiter is clicked, this function calls
     * updatedInfoPanel() from the main gui so that person's information
     * will be shown
     *
     * @param type indicates whether the person is a customer or waiter
     * @param name name of person
     */
    public void showInfo(String type, String name) {

        if (type.equals("Customers")) {

            for (int i = 0; i < customers.size(); i++) {
                CustomerAgent temp = customers.get(i);
                if (temp.getName() == name)
                    gui.updateInfoPanel(temp);
            }
        }
        if (type.equals("Waiters")) {

            for (int i = 0; i < waiters.size(); i++) {
                WaiterAgent temp = waiters.get(i);
                if (temp.getName() == name)
                    gui.updateInfoPanel(temp);
            }
        }
    }

    /**
     * Adds a customer or waiter to the appropriate list
     *
     * @param type indicates whether the person is a customer or waiter (later)
     * @param name name of person
     */
    public void addPerson(String type, String name) {

    	if (type.equals("Customers")) {
    		CustomerAgent c = new CustomerAgent(name);	
    		CustomerGui g = new CustomerGui(c, gui);

    		gui.animationPanel.addGui(g);// dw
    		c.setHost(host);
    		c.setGui(g);
    		customers.add(c);
    		c.startThread();
    	}
    	if (type.equals("Waiters")) {
    		WaiterAgent w = new WaiterAgent(name);	
    		WaiterGui g = new WaiterGui(w);

    		gui.animationPanel.addGui(g);
     		w.setHost(host);
     		w.setCook(cook);
     		w.setGui(g);
     		waiters.add(w);
     		w.startThread();
     		host.addWaiter(w);
    	}
    }

}
