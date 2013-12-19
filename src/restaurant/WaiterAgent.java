package restaurant;

import agent.Agent;
import restaurant.gui.WaiterGui;
import restaurant.gui.CookGui;
import restaurant.interfaces.Waiter;
import restaurant.interfaces.Customer;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Waiter Agent
 */
public class WaiterAgent extends Agent implements Waiter {
	public List<MyCustomer> customers
	= new ArrayList<MyCustomer>();
	HostAgent host;
	CookAgent cook;
	CashierAgent cashier;

	private String name;
	private Semaphore atHome = new Semaphore(0,true);
	private Semaphore atCustomer = new Semaphore(0,true);
	private Semaphore atTable = new Semaphore(0,true);
	private Semaphore atCook = new Semaphore(0,true);
	private boolean returningHome = false;
	private Menu menu;
	Timer timer = new Timer();
	
	Map<String, Integer> prices = new HashMap<String, Integer>();
	
	public enum CustomerState
	{DoingNothing, Waiting, Seated, AskedToOrder, Asked, Ordered, MustReorder, WaitingForFood, OrderDone, ReadyToEat, Eating, WaitingForCheck, Leaving};

	public enum WaiterState
	{OnTheJob, WantToGoOnBreak, AboutToGoOnBreak, OnBreak, GoingOffBreak};
	private WaiterState state = WaiterState.OnTheJob;
	
	public WaiterGui waiterGui = null;
	public CookGui cookGui = null;

	/**
	 * Constructor
	 *
	 * @param name Agent name for messages
	 */
	public WaiterAgent(String name) {
		super();
		this.name = name;
		
		prices.put("steak", 16);
		prices.put("chicken", 11);
		prices.put("salad", 6);
		prices.put("pizza", 9);
		
		menu = new Menu();
		menu.addItem("steak", prices.get("steak"));
		menu.addItem("chicken", prices.get("chicken"));
		menu.addItem("salad", prices.get("salad"));
		menu.addItem("pizza", prices.get("pizza"));
	}
	
	/**
	 * Hack to establish connection to HostAgent.
	 * 
	 * @param h Reference to HostAgent
	 */
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	/**
	 * Hack to establish connection to CookAgent.
	 * 
	 * @param cook Reference to CookAgent
	 */
	public void setCook(CookAgent cook) {
		this.cook = cook;
	}
	
	/**
	 * Hack to establish connection to CashierAgent.
	 * 
	 * @param cashier Reference to CashierAgent
	 */
	public void setCashier(CashierAgent cashier) {
		this.cashier = cashier;
	}
	
	/**
	 * Assigns a GUI to Agent
	 * 
	 * @param gui Reference to WaiterGui
	 */
	public void setGui(WaiterGui gui) {
		waiterGui = gui;
	}
	
	/**
	 * Assigns a CookGui to Agent
	 * 
	 * @param gui Reference to CookGui
	 */
	public void setGui(CookGui gui) {
		cookGui = gui;
	}

	/**
	 * Returns WaiterAgent's GUI
	 */
	public WaiterGui getGui() {
		return waiterGui;
	}

	/**
	 * Returns the Agent's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns a String that represents the Agent
	 */
	public String toString() {
		return getName();
	}
	
	/**
	 * Checks if the Waiter is done serving all his Customers
	 * 
	 * @return true if all CustomerAgents in Waiter's list are DoingNothing, false otherwise
	 */
	public boolean doneServingCustomers() {
		for (MyCustomer mc : customers) {
			if (mc.getState() != CustomerState.DoingNothing) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if the WaiterAgent is either on break or about to go on break
	 * 
	 * @return true if WaiterAgent's state is AboutToGoOnBreak or OnBreak, false otherwise
	 */
	public boolean isOnBreak() {
		if (state == WaiterState.AboutToGoOnBreak || state == WaiterState.OnBreak) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the WaiterAgent is about to go on break
	 * 
	 * @return true if WaiterAgent's state is AboutToGoOnBreak, false otherwise
	 */
	public boolean isAboutToGoOnBreak() {
		if (state == WaiterState.AboutToGoOnBreak) {
			return true;
		}
		return false;
	}
	
	// Messages
	
	/**
	 * Sets waiter's state to "WantToGoOnBreak"
	 */
	public void msgWantToGoOnBreak() {
		state = WaiterState.WantToGoOnBreak;
		stateChanged();
	}
	
	/**
	 * Tells the waiter that he can go on break and sets his state to "AboutToGoOnBreak"
	 */
	public void msgCanGoOnBreak() {
		state = WaiterState.AboutToGoOnBreak;
		stateChanged();
	}
	
	/**
	 * Tells the waiter that he can't go on break and sets his state to "OnTheJob"
	 */
	public void msgCantGoOnBreak() {
		state = WaiterState.OnTheJob;
		waiterGui.setCBEnabled();
		stateChanged();
	}
	
	/**
	 * Sets waiter's state to "GoingOffBreak"
	 */
	public void msgGoOffBreak() {
		state = WaiterState.GoingOffBreak;
		stateChanged();
	}
	
	/**
	 * Tells the waiter to seat a customer at a table
	 * 
	 * @param cust Reference to CustomerAgent
	 * @param tableNumber Number of the assigned table
	 */
	public void msgPleaseSeatCustomer(CustomerAgent cust, int tableNumber) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == cust) {
				mc.setState(CustomerState.Waiting);
				mc.setTable(tableNumber);
				stateChanged();
				return;
			}
		}
		customers.add(new MyCustomer(cust, tableNumber, CustomerState.Waiting));
		stateChanged();
	}
	
	/**
	 * Sets a customer's state to "Leaving"
	 * 
	 * @param cust Reference to CustomerAgent
	 */
	public void msgIWantToLeave(CustomerAgent cust) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == cust) {
				print(mc.getCust() + " leaving table " + mc.getTable());
				mc.setState(CustomerState.Leaving);
				stateChanged();
				return;
			}
		}
	}
	
	/**
	 * Sets a customer's state to "AskedToOrder"
	 * 
	 * @param cust Reference to CustomerAgent
	 */
	public void msgReadyToOrder(CustomerAgent cust) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == cust) {
				mc.setState(CustomerState.AskedToOrder);
				stateChanged();
			}
		}
	}
	
	/**
	 * Sets a customer's state to "Ordered" and his choice to the specified choice
	 * 
	 * @param cust Reference to CustomerAgent
	 * @param choice Name of customer's food choice
	 */
	public void msgHereIsChoice(CustomerAgent cust, String choice) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == cust) {
				mc.setState(CustomerState.Ordered);
				mc.setChoice(choice);
				stateChanged();
			}
		}
	}
	
	/**
	 * Tells the waiter that the restaurant is out of the specified food
	 * 
	 * @param choice Name of the food
	 * @param table Number of the waiter's current table
	 */
	public void msgOutOfFood(String choice, int table) {
		menu.removeItem(choice);
		for (MyCustomer mc : customers) {
			if (mc.getTable() == table) {
				mc.setState(CustomerState.MustReorder);
				stateChanged();
			}
		}
	}
	
	/**
	 * Tells the waiter that his customer's order is ready
	 * 
	 * @param choice Name of the customer's food choice
	 * @param tableNum Number of the customer's table
	 */
	public void msgOrderDone(String choice, int tableNum) {
		for (MyCustomer mc : customers) {
			if (mc.getTable() == tableNum && mc.getChoice() == choice) {
				mc.setState(CustomerState.OrderDone);
				stateChanged();
			}
		}
	}

	/**
	 * Tells waiter that the customer has finished eating and sets the customer's state to "WaitingForCheck"
	 * 
	 * @param cust Reference to the CustomerAgent
	 */
	public void msgDoneEating(CustomerAgent cust) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == cust) {
				mc.setState(CustomerState.WaitingForCheck);
				stateChanged();
			}
		}
	}
	
	/**
	 * Tells waiter that the restaurant is no longer out of the specified food and adds that food to the menu
	 * 
	 * @param food Name of the food
	 */
	public void msgFoodArrived(String food) {
		if (!menu.checkItem(food)) {
			menu.addItem(food, prices.get(food));
			stateChanged();
		}
	}
	
	/**
	 * Gives the waiter a check to be delivered to the specified customer
	 * 
	 * @param c Reference to Customer
	 * @param charge Integer amount that the customer owes
	 */
	public void msgHereIsCheck(Customer c, int charge) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == c) {
				mc.setCharge(charge);
				stateChanged();
			}
		}
	}
	
	/**
	 * Lets the waiter know that he has reached his home position
	 */
	public void msgAtHome() {//from animation
		if (returningHome) {
			atHome.release();// = true;
			stateChanged();
			returningHome = false;
		}
	}
	
	/**
	 * Lets the waiter know that he has reached the customer's home position
	 */
	public void msgAtCustomer() {//from animation
		atCustomer.release();// = true;
		stateChanged();
	}

	/**
	 * Lets the waiter know that he has reached the customer's table
	 */
	public void msgAtTable() {//from animation
		atTable.release();// = true;
		stateChanged();
	}
	
	/**
	 * Lets the waiter know that he has reached the cook's home position
	 */
	public void msgAtCook() {//from animation
		atCook.release();// = true;
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {
		try {
			if (state == WaiterState.WantToGoOnBreak) {
				wantToGoOnBreak();
				return true;
			}
			if (state == WaiterState.AboutToGoOnBreak){
				if (doneServingCustomers()) {
					goOnBreak();
					return true;
				}
			}
			if (state == WaiterState.GoingOffBreak){
				goOffBreak();
				return true;
			}
			for (MyCustomer mc : customers) {
				if (mc.getState() == CustomerState.ReadyToEat) {
					deliverFood(mc);
					return true;
				}
			}
			for (MyCustomer mc : customers) {
				if (mc.getState() == CustomerState.OrderDone) {
					retrieveOrder(mc);
					return true;
				}
			}
			for (MyCustomer mc : customers) {
				if (mc.getState() == CustomerState.Waiting) {
					seatCustomer(mc);
					return true;
				}
			}
			for (MyCustomer mc : customers) {
				if (mc.getState() == CustomerState.AskedToOrder) {
					takeOrder(mc);
					return true;
				}
			}
			for (MyCustomer mc : customers) {
				if (mc.getState() == CustomerState.Ordered) {
					giveOrderToCook(mc);
					return true;
				}
			}
			for (MyCustomer mc : customers) {
				if (mc.getState() == CustomerState.MustReorder){
					askToReorder(mc);
					return true;
				}
			}
			for (MyCustomer mc : customers) {
				if (mc.getState() == CustomerState.WaitingForCheck){
					giveCheckToCustomer(mc);
					return true;
				}
			}
			for (MyCustomer mc : customers) {
				if (mc.getState() == CustomerState.Leaving){
					notifyHost(mc);
					return true;
				}
			}
		} catch (ConcurrentModificationException e) {
			return false;
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions
	
	/**
	 * Waiter tells the host that he wants to go on break
	 */
	private void wantToGoOnBreak() {
		print(host + ", I want to go on break.");
		host.msgWantToGoOnBreak(this);
		state = WaiterState.OnTheJob;
	}
	
	/**
	 * Waiter goes on break and tells the host
	 */
	private void goOnBreak() {
		print(host + ", I'm going on break.");
		host.msgGoingOnBreak(this);
		state = WaiterState.OnBreak;
		returningHome = true;
		waiterGui.DoReturnHome();
		try {
			atHome.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		waiterGui.setCBEnabled();
	}
	
	/**
	 * Waiter goes back to work and tells the host
	 */
	private void goOffBreak() {
		print(host + ", I'm going off break.");
		host.msgGoingOffBreak(this);
		state = WaiterState.OnTheJob;
	}

	/**
	 * Waiter goes to the customer and takes him to his assigned table
	 * 
	 * @param mc Reference to MyCustomer
	 */
	private void seatCustomer(MyCustomer mc) {
		returningHome = true;
		waiterGui.DoGoToCustomer();
		try {
			atCustomer.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mc.getCust().msgFollowMe(this, menu, mc.getTable());
		DoSeatCustomer(mc);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mc.setState(CustomerState.Seated);
		waiterGui.DoReturnHome();
	}
	
	/**
	 * Waiter goes to the customer's table and asks what he would like to order
	 * 
	 * @param mc Reference to MyCustomer
	 */
	private void takeOrder(MyCustomer mc) {
		waiterGui.DoGoToTable(mc.getTable());
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Do("What would you like to order?");
		mc.getCust().msgWhatWouldYouLike();
		mc.setState(CustomerState.Asked);
	}
	
	/**
	 * Waiter goes to the customer's table and asks if he would like to reorder
	 * 
	 * @param mc Reference to MyCustomer
	 */
	private void askToReorder(MyCustomer mc) {
		waiterGui.DoGoToTable(mc.getTable());
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Do("I'm sorry, we're out of that menu item. Would you like to order something else?");
		mc.getCust().msgWantSomethingElse(menu);
		mc.setState(CustomerState.Asked);
	}
	
	/**
	 * Waiter goes to the cook and gives him the customer's order
	 * 
	 * @param mc Reference to MyCustomer
	 */
	private void giveOrderToCook(MyCustomer mc) {
		mc.setState(CustomerState.WaitingForFood);
		waiterGui.DoGoToCook();
		try {
			atCook.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		print("Here is the order for table " + mc.getTable() + ": " + mc.getChoice());
		cook.msgHereIsOrder(this, mc.getChoice(), mc.getTable());
		waiterGui.DoReturnHome();
	}
	
	/**
	 * Waiter goes to the plating area and picks up the customer's order
	 * 
	 * @param mc Reference to MyCustomer
	 */
	private void retrieveOrder(MyCustomer mc) {
		print("Retrieving order for table " + mc.getTable());
		waiterGui.DoGoToPlatingArea();
		try {
			atCook.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cookGui.DoRemoveFood(mc.getChoice());
		print("Delivering " + mc.getChoice() + " to table " + mc.getTable());
		waiterGui.DoDeliverFood(mc.getChoice());
		mc.setState(CustomerState.ReadyToEat);
	}
	
	/**
	 * Waiter delivers customer's order to his table and asks the cashier to produce a check
	 * 
	 * @param mc Reference to MyCustomer
	 */
	private void deliverFood(MyCustomer mc) {
		waiterGui.DoGoToTable(mc.getTable());
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Do("Here is your order.");
		mc.getCust().msgHereIsFood(mc.getChoice());
		cashier.msgProduceCheck(this, mc.getCust(), mc.choice);
		mc.setState(CustomerState.Eating);
		waiterGui.DoReturnHome();
	}
	
	/**
	 * Waiter goes back to his home position and tells the host that a customer has left and his table is available
	 * 
	 * @param mc Reference to MyCustomer
	 */
	private void notifyHost(MyCustomer mc) {
		host.msgTableAvailable(mc.getTable());
		waiterGui.DoReturnHome();
		mc.setState(CustomerState.DoingNothing);
	}
	
	/**
	 * Waiter goes to customer's table and gives him his check
	 * 
	 * @param mc Reference to MyCustomer
	 */
	private void giveCheckToCustomer(MyCustomer mc) {
		waiterGui.DoGoToTable(mc.getTable());
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Do("Here is your check. The charge is $" + mc.charge);
		print(mc.getCust() + " leaving table " + mc.getTable());
		mc.getCust().msgHereIsCheck(mc.charge);
		mc.setState(CustomerState.Leaving);
		waiterGui.DoReturnHome();
	}

	/**
	 * Waiter goes to the customer's table
	 * 
	 * @param mc Reference to MyCustomer
	 */
	private void DoSeatCustomer(MyCustomer mc) {
		print("Seating " + mc.getCust() + " at table " + mc.getTable());
		waiterGui.DoGoToTable(mc.getTable()); 

	}

	//Inner classes

	/**
	 * Contains all information about a customer relevant to the WaiterAgent
	 */
	private class MyCustomer {
		CustomerAgent cust;
		int table;
		private CustomerState state = CustomerState.DoingNothing;//The start state
		String choice;
		int charge;

		MyCustomer(CustomerAgent c, int tableNumber, CustomerState s) {
			cust = c;
			table = tableNumber;
			state = s;
			charge = 0;
		}

		CustomerAgent getCust() {
			return cust;
		}
		
		public int getTable() {
			return table;
		}
		
		CustomerState getState() {
			return state;
		}
		
		void setState(CustomerState s) {
			state = s;
		}
		
		void setCharge(int c) {
			charge = c;
		}
		
		void setTable(int t) {
			table = t;
		}
		
		String getChoice() {
			return choice;
		}
		
		void setChoice(String c) {
			choice = c;
		}
	}
}

