package restaurant.gui;

import restaurant.WaiterAgent;

import java.awt.*;
import java.util.*;

/**
 * Restaurant Waiter GUI
 */
public class WaiterGui implements Gui {

    private WaiterAgent agent = null;
    
    RestaurantGui gui;

    private int xPos, yPos;
    private int xDestination, yDestination;
    private boolean atTable = false;

    //Get rid of the "magic numbers"
    static final int WAITERWIDTH = 20;
	static final int WAITERHEIGHT = 20;
	static final int COOKX = 200;
	static final int COOKY = 10;
	static final int CUSTX = 20;
	static final int CUSTY = 20;
	static final int PLATINGX = 250;
	static final int PLATINGY = 60;
	final int HOMEX;
	final int HOMEY;
	
    public static final int xTable = 150;
    public static final int yTable = 250;
    public static final int tableWidth = 50;
    public static final int tableHeight = 50;
    
    Map<Integer, Point> tablePositions = new HashMap<Integer, Point>();
    Map<String, String> foodSymbols = new HashMap<String, String>();
    ArrayList<String> orders = new ArrayList<String>();

    /**
     * Constructor
     * 
     * @param agent Reference to WaiterAgent
     * @param gui Reference to RestaurantGUI
     * @param waiterNum Number of other waiters in restaurant
     */
    public WaiterGui(WaiterAgent agent, RestaurantGui gui, int waiterNum) {
        this.agent = agent;
        this.gui = gui;
        HOMEX = 30*((waiterNum + 6-((waiterNum-1)%7))/7 - 1);
        HOMEY = ((waiterNum-1)%7)*30 + 160;
        xPos = HOMEX;
        yPos = HOMEY;
        xDestination = HOMEX;
        yDestination = HOMEY;
        
        tablePositions.put(1, new Point(xTable + WAITERWIDTH, yTable - WAITERWIDTH));
		tablePositions.put(2, new Point(xTable + 2*tableWidth + WAITERWIDTH, yTable - WAITERWIDTH));
		tablePositions.put(3, new Point(xTable + 4*tableWidth + WAITERWIDTH, yTable - WAITERWIDTH));
		
		foodSymbols.put("steak", "St");
		foodSymbols.put("chicken", "C");
		foodSymbols.put("pizza", "P");
		foodSymbols.put("salad", "Sa");
    }

    /**
	 * This is what drives the animation; called each time the animation timer fires
	 */
    public void updatePosition() {
        if (xPos < xDestination)
            xPos++;
        else if (xPos > xDestination)
            xPos--;

        if (yPos < yDestination)
            yPos++;
        else if (yPos > yDestination)
            yPos--;

        if (xPos == xDestination && yPos == yDestination) {
        	if ((xDestination >= xTable + WAITERWIDTH) && (yDestination == yTable - WAITERWIDTH)) {
        		if (!atTable) {
        			agent.msgAtTable();
        			atTable = true;
	        		if (!orders.isEmpty()) {
	        			orders.remove(0);
	        		}
        		}
        	}
        	if (xDestination == HOMEX && yDestination == HOMEY) {
        		agent.msgAtHome();
        	}
        	if (xDestination == CUSTX && yDestination == CUSTY) {
        		agent.msgAtCustomer();
        	}
        	if ((xDestination == COOKX && yDestination == COOKY) || (xDestination == PLATINGX && yDestination == PLATINGY)) {
        		agent.msgAtCook();
        	}
        }
    }

    /**
     * This creates the visuals themselves based on the position set in updatePosition
     */
    public void draw(Graphics2D g) {
        g.setColor(Color.MAGENTA);
        g.fillRect(xPos, yPos, WAITERWIDTH, WAITERHEIGHT);
        
        if (!orders.isEmpty()) { 
			g.setColor(Color.WHITE);
			g.fillRect(xPos+WAITERWIDTH, yPos, WAITERWIDTH, WAITERHEIGHT);
			g.setColor(Color.BLACK);
			g.drawString(orders.get(0), xPos+WAITERWIDTH+WAITERWIDTH/4, yPos+WAITERHEIGHT/2);
		}
    }

    /**
	 * This returns true if the GUI is currently present in the animation window, false otherwise
	 */
    public boolean isPresent() {
        return true;
    }
    
    /**
     * Tells the RestaurantGUI to enable the waiter's "Break?" checkbox
     */
    public void setCBEnabled() {
    	gui.setWaiterEnabled(agent);
    }

    /**
	 * Sets the destination as the assigned table
	 * 
	 * @param tablenumber Number of the assigned table
	 */
    public void DoGoToTable(int tableNumber) {
        xDestination = (int)tablePositions.get(tableNumber).getX();
		yDestination = (int)tablePositions.get(tableNumber).getY();
		atTable = false;
    }
    
    /**
     * Sets the destination as the customers' waiting area
     */
    public void DoGoToCustomer() {
    	xDestination = CUSTX;
		yDestination = CUSTY;
    }
    
    /**
     * Sets the destination as the cook's position
     */
    public void DoGoToCook() {
        xDestination = COOKX;
        yDestination = COOKY;
    }
    
    /**
     * Sets the destination as the plating area
     */
    public void DoGoToPlatingArea() {
        xDestination = PLATINGX;
        yDestination = PLATINGY;
    }

    /**
     * Sets the destination as the waiter's home position
     */
    public void DoReturnHome() {
        xDestination = HOMEX;
        yDestination = HOMEY;
    }
    
    /**
     * Adds the appropriate food symbol to the list of orders, so that the waiter can "carry" the order to a table
     * 
     * @param choice Name of the customer's choice
     */
    public void DoDeliverFood(String choice) {
    	orders.add(foodSymbols.get(choice));
    }
}
