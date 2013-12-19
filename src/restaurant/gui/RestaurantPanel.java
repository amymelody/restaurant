package restaurant.gui;

import restaurant.CustomerAgent;
import restaurant.HostAgent;
import restaurant.MarketAgent;
import restaurant.WaiterAgent;
import restaurant.CookAgent;
import restaurant.CashierAgent;

import javax.swing.*;

import java.awt.*;
import java.util.Vector;

/**
 * Panel in frame that contains all the restaurant information,
 * including host, cook, waiters, and customers.
 */
public class RestaurantPanel extends JPanel {

    //Host, cook, waiters and customers
    private HostAgent host = new HostAgent("Sarah");
    private CookAgent cook = new CookAgent("John");
    private CashierAgent cashier = new CashierAgent("Jake");
    private Vector<MyCustomer> customers = new Vector<MyCustomer>();
    private Vector<WaiterAgent> waiters = new Vector<WaiterAgent>();
    private Vector<MarketAgent> markets = new Vector<MarketAgent>();

    private JPanel restLabel = new JPanel();
    private ListPanel customerPanel = new ListPanel(this, "Customers");
    private ListPanel waiterPanel = new ListPanel(this, "Waiters");
    private JPanel group = new JPanel();

    private RestaurantGui gui; //reference to main gui
    private CookGui cookGui;

    public RestaurantPanel(RestaurantGui gui) {
        this.gui = gui;
        
        markets.add(new MarketAgent("Market 1", cook, cashier, 0, 10, 10, 10));
		markets.add(new MarketAgent("Market 2", cook, cashier, 1, 1, 1, 1));
		markets.add(new MarketAgent("Market 3", cook, cashier, 10, 2, 2, 2));
		
		cook.addMarket(markets.get(0));
		cook.addMarket(markets.get(1));
		cook.addMarket(markets.get(2));
		cook.setHost(host);
        
		cookGui = new CookGui(gui);
		gui.animationPanel.addGui(cookGui);
		cook.setGui(cookGui);
		
        host.startThread();
        cook.startThread();
        cashier.startThread();
        for (MarketAgent market : markets) {
        	market.startThread();
        }

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
    
    /**
     * If a customer exists in the RestaurantPanel's list of customers, this sets that customer's "waiting" to true
     * 
     * @param c Reference to CustomerAgent
     */
    public void addCustomer(CustomerAgent c) {
    	for (MyCustomer mc : customers) {
    		if (mc.cust == c) {
    			mc.waiting = true;
    		}
    	}
    }
    
    /**
     * If a customer exists in the RestaurantPanel's list of customers, this sets that customer's "waiting" to false
     * and moves all other customers forward in the line of waiting customers
     * 
     * @param c Reference to CustomerAgent
     */
    public void removeCustomer(CustomerAgent c) {
    	for (MyCustomer mc : customers) {
    		if (mc.cust == c) {
    			mc.waiting = false;
    		}
    	}
    	int total = 0;
    	for (MyCustomer mc : customers) {
    		if (mc.waiting) {
    			mc.cust.getGui().moveForwardInWait(total);
    			total++;
    		}
    	}
    }
    
    /**
     * Returns the number of waiting customers
     */
    public int getNumCustomers() {
    	int total = 0;
    	for (MyCustomer mc : customers) {
    		if (mc.waiting) {
    			total++;
    		}
    	}
    	return total;
    }
    
    /**
     * Pauses all agents in the restaurant
     */
    public void pauseAgents() {
    	host.pause();
    	cook.pause();
    	cashier.pause();
    	for (WaiterAgent w : waiters) {
    		w.pause();
    	}
    	for (MyCustomer mc : customers) {
    		mc.cust.pause();
    	}
    	for (MarketAgent m : markets) {
    		m.pause();
    	}
    }
    
    /**
     * Resumes all agents in the restaurant
     */
    public void resumeAgents() {
    	host.resume();
    	cook.resume();
    	for (WaiterAgent w : waiters) {
    		w.resume();
    	}
    	for (MyCustomer mc : customers) {
    		mc.cust.resume();
    	}
    	for (MarketAgent m : markets) {
    		m.resume();
    	}
    }

    /**
     * Sets up the restaurant label that includes the menu, and host and cook information
     */
    private void initRestLabel() {
        JLabel label = new JLabel();
        //restLabel.setLayout(new BoxLayout((Container)restLabel, BoxLayout.Y_AXIS));
        restLabel.setLayout(new BorderLayout());
        label.setText(
                "<html><h3><u>Tonight's Staff</u></h3><table><tr><td>host:</td><td>" + host.getName() + "</td></tr></table><table><tr><td>cook:</td><td>" + cook.getName() + "</td></tr></table><table><tr><td>cashier:</td><td>" + cashier.getName() + "</td></tr></table><h3><u> Menu</u></h3><table><tr><td>Steak</td><td>$16.00</td></tr><tr><td>Chicken</td><td>$11.00</td></tr><tr><td>Salad</td><td>$6.00</td></tr><tr><td>Pizza</td><td>$9.00</td></tr></table><br></html>");

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
     * @param type Indicates whether the person is a customer or waiter
     * @param name Name of person
     */
    public void showInfo(String type, String name) {

        if (type.equals("Customers")) {

            for (int i = 0; i < customers.size(); i++) {
                CustomerAgent temp = customers.get(i).cust;
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
     * @param type Indicates whether the person is a customer or waiter
     * @param name Name of person
     */
    public void addPerson(String type, String name) {

    	if (type.equals("Customers")) {
    		CustomerAgent c = new CustomerAgent(name);	
    		CustomerGui g = new CustomerGui(c, gui);

    		gui.animationPanel.addGui(g);
    		c.setHost(host);
    		c.setCashier(cashier);
    		c.setGui(g);
    		customers.add(new MyCustomer(c));
    		c.startThread();
    	}
    	if (type.equals("Waiters")) {
    		WaiterAgent w = new WaiterAgent(name);	
    		WaiterGui g = new WaiterGui(w, gui, waiters.size()+1);

    		gui.animationPanel.addGui(g);
     		w.setHost(host);
     		w.setCashier(cashier);
     		w.setCook(cook);
     		w.setGui(g);
     		w.setGui(cookGui);
     		waiters.add(w);
     		w.startThread();
     		host.addWaiter(w);
    	}
    }

    /**
     * Contains all information about a customer relevant to the RestaurantPanel
     */
    private class MyCustomer {
		CustomerAgent cust;
		boolean waiting;

		MyCustomer(CustomerAgent c) {
			cust = c;
			waiting = false;
		}
    }
}
