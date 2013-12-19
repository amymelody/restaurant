package restaurant.gui;

import restaurant.CustomerAgent;

import java.awt.*;
import java.util.*;

/**
 * Restaurant Customer GUI
 */
public class CustomerGui implements Gui{

	private CustomerAgent agent = null;
	private boolean isPresent = false;
	private boolean isHungry = false;

	RestaurantGui gui;

	private int xPos, yPos;
	private int xDestination, yDestination;
	private enum Command {noCommand, GoToRestaurant, GoToSeat, GoToCashier, LeaveRestaurant};
	private Command command=Command.noCommand;
	private boolean ordering;
	private Timer timer = new Timer();

	//Get rid of the "magic numbers"
	static final int CUSTWIDTH = 20;
	static final int CUSTHEIGHT = 20;
	static final int CASHIERX = -20;
	static final int CASHIERY = 100;
	
	public static final int xTable = 150;
	public static final int yTable = 250;
	public static final int tableWidth = 50;
	public static final int tableHeight = 50;
	
	Map<Integer, Point> tablePositions = new HashMap<Integer, Point>();
	Map<String, String> foodSymbols = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param c Reference to CustomerAgent
	 * @param gui Reference to RestaurantGui
	 */
	public CustomerGui(CustomerAgent c, RestaurantGui gui) {
		agent = c;
		xPos = -CUSTWIDTH;
		yPos = -CUSTWIDTH;
		xDestination = -CUSTWIDTH;
		yDestination = -CUSTWIDTH;
		this.gui = gui;
		
		tablePositions.put(1, new Point(xTable, yTable));
		tablePositions.put(2, new Point(xTable + 2*tableWidth, yTable));
		tablePositions.put(3, new Point(xTable + 4*tableWidth, yTable));
		
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
			if (command==Command.GoToSeat) agent.msgAnimationFinishedGoToSeat();
			else if (command==Command.LeaveRestaurant) {
				agent.msgAnimationFinishedLeaveRestaurant();
				System.out.println("about to call gui.setCustomerEnabled(agent);");
				isHungry = false;
				gui.setCustomerEnabled(agent);
			}
			else if (command==Command.GoToRestaurant) {
				agent.msgAnimationFinishedEnterRestaurant();
			}
			else if (command==Command.GoToCashier) {
				agent.msgAtCashier();
			}
			command=Command.noCommand;
		}
	}

	/**
     * This creates the visuals themselves based on the position set in updatePosition
     */
	public void draw(Graphics2D g) {
		g.setColor(Color.GREEN);
		g.fillRect(xPos, yPos, CUSTWIDTH, CUSTHEIGHT);
		
		if (ordering) {
			g.setColor(Color.WHITE);
			g.fillRect(xPos-CUSTWIDTH, yPos-CUSTHEIGHT, CUSTWIDTH, CUSTHEIGHT);
			g.setColor(Color.BLACK);
			g.drawString(foodSymbols.get(agent.getChoice()) + "?", xPos-CUSTWIDTH, yPos-CUSTHEIGHT/2);
		}
		
		if (agent.isEating()) { 
			g.setColor(Color.WHITE);
			g.fillRect(xPos+CUSTWIDTH, yPos, CUSTWIDTH, CUSTHEIGHT);
			g.setColor(Color.BLACK);
			g.drawString(foodSymbols.get(agent.getChoice()), xPos+CUSTWIDTH+CUSTWIDTH/4, yPos+CUSTHEIGHT/2);
		}
	}

	/**
	 * This returns true if the GUI is currently present in the animation window, false otherwise
	 */
	public boolean isPresent() {
		return isPresent;
	}
	
	/**
	 * Makes the customer hungry and sets his destination as the last spot in the line of waiting customers
	 * 
	 * @param numCustomers Current number of customers waiting
	 */
	public void setHungry(int numCustomers) {
		isHungry = true;
		xDestination = (numCustomers%6)*30;
		yDestination = 30*((numCustomers - numCustomers%6)/6);
		command = Command.GoToRestaurant;
		agent.gotHungry();
		setPresent(true);
	}
	
	/**
	 * Updates the customer's destination so that he will move forward in line
	 * 
	 * @param numCustomers Current number of customers waiting
	 */
	public void moveForwardInWait(int numCustomers) {
		xDestination = (numCustomers%6)*30;
		yDestination = 30*((numCustomers - numCustomers%6)/6);
	}
	
	/**
	 * Returns true if isHungry is true, false otherwise
	 */
	public boolean isHungry() {
		return isHungry;
	}

	/**
	 * Sets whether the customer is present in the animation frame
	 * 
	 * @param p Boolean to set isPresent to
	 */
	public void setPresent(boolean p) {
		isPresent = p;
	}
	
	/**
	 * Starts the customer's ordering animation (a text bubble with the food item he wants)
	 */
	public void order() {
		ordering = true;
		timer.schedule(new TimerTask() {
			public void run() {
				ordering = false;
				agent.msgDoneOrdering();
			}
		},
		1000);
	}

	/**
	 * Sets the destination as the assigned table
	 * 
	 * @param seatnumber Number of the assigned table
	 */
	public void DoGoToSeat(int seatnumber) {
		xDestination = (int)tablePositions.get(seatnumber).getX();
		yDestination = (int)tablePositions.get(seatnumber).getY();
		gui.removeWaitingCustomer(agent);
		command = Command.GoToSeat;
	}
	
	/**
	 * Sets the destination as the cashier's location
	 */
	public void DoGoToCashier() {
		xDestination = CASHIERX;
		yDestination = CASHIERY;
		command = Command.GoToCashier;
	}

	/**
	 * Sets the destination as the restaurant's exit
	 */
	public void DoExitRestaurant() {
		xDestination = -2*CUSTWIDTH;
		yDestination = -2*CUSTWIDTH;
		gui.removeWaitingCustomer(agent);
		command = Command.LeaveRestaurant;
	}
}
