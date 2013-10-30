package restaurant.gui;

import restaurant.CustomerAgent;
import restaurant.HostAgent;
import restaurant.CustomerAgent.AgentEvent;

import java.awt.*;
import java.util.*;

public class CustomerGui implements Gui{

	private CustomerAgent agent = null;
	private boolean isPresent = false;
	private boolean isHungry = false;

	RestaurantGui gui;

	private int xPos, yPos;
	private int xDestination, yDestination;
	private enum Command {noCommand, GoToSeat, GoToCashier, LeaveRestaurant};
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

	public CustomerGui(CustomerAgent c, RestaurantGui gui) {
		agent = c;
		xPos = -2*CUSTWIDTH;
		yPos = -2*CUSTWIDTH;
		xDestination = -2*CUSTWIDTH;
		yDestination = -2*CUSTWIDTH;
		this.gui = gui;
		
		tablePositions.put(1, new Point(xTable, yTable));
		tablePositions.put(2, new Point(xTable + 2*tableWidth, yTable));
		tablePositions.put(3, new Point(xTable + 4*tableWidth, yTable));
		
		foodSymbols.put("steak", "St");
		foodSymbols.put("chicken", "C");
		foodSymbols.put("pizza", "P");
		foodSymbols.put("salad", "Sa");
	}

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
			else if (command==Command.GoToCashier) {
				agent.msgAtCashier();
			}
			command=Command.noCommand;
		}
	}

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

	public boolean isPresent() {
		return isPresent;
	}
	
	public void setHungry(int numCustomers) {
		isHungry = true;
		xDestination = (numCustomers%6)*30;
		yDestination = 30*((numCustomers - numCustomers%6)/6);
		agent.gotHungry();
		setPresent(true);
	}
	
	public void moveForwardInWait(int numCustomers) {
		xDestination = (numCustomers%6)*30;
		yDestination = 30*((numCustomers - numCustomers%6)/6);
	}
	
	public boolean isHungry() {
		return isHungry;
	}

	public void setPresent(boolean p) {
		isPresent = p;
	}
	
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

	public void DoGoToSeat(int seatnumber) {
		xDestination = (int)tablePositions.get(seatnumber).getX();
		yDestination = (int)tablePositions.get(seatnumber).getY();
		gui.removeWaitingCustomer(agent);
		command = Command.GoToSeat;
	}
	
	public void DoGoToCashier() {
		xDestination = CASHIERX;
		yDestination = CASHIERY;
		command = Command.GoToCashier;
	}

	public void DoExitRestaurant() {
		xDestination = -2*CUSTWIDTH;
		yDestination = -2*CUSTWIDTH;
		command = Command.LeaveRestaurant;
	}
}
