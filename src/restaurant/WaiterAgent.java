package restaurant;

import agent.Agent;
import restaurant.gui.WaiterGui;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Waiter Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class WaiterAgent extends Agent {
	public List<MyCustomer> customers
	= new ArrayList<MyCustomer>();
	HostAgent host;
	CookAgent cook;

	private String name;
	private Semaphore atHome = new Semaphore(0,true);
	private Semaphore atTable = new Semaphore(0,true);
	private Semaphore atCook = new Semaphore(0,true);
	private boolean returningHome = false;
	private Menu menu;
	private int customerCount = 0;
	
	public enum CustomerState
	{DoingNothing, Waiting, Seated, AskedToOrder, Asked, Ordered, WaitingForFood, OrderDone, ReadyToEat, Eating, Leaving};

	public WaiterGui waiterGui = null;

	public WaiterAgent(String name) {
		super();
		this.name = name;
		
		menu = new Menu();
		menu.addItem("steak");
		menu.addItem("chicken");
		menu.addItem("salad");
		menu.addItem("pizza");
	}
	
	/**
	 * hack to establish connection to Host agent.
	 */
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	public void setCook(CookAgent cook) {
		this.cook = cook;
	}

	public String getName() {
		return name;
	}

	public List getCustomers() {
		return customers;
	}
	
	public String toString() {
		return getName();
	}
	
	public int getCustomerCount() {
		return customerCount;
	}
	
	public void addCustomer() {
		customerCount++;
	}
	
	// Messages
	
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
	
	public void msgReadyToOrder(CustomerAgent cust) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == cust) {
				mc.setState(CustomerState.AskedToOrder);
				stateChanged();
			}
		}
	}
	
	public void msgHereIsChoice(CustomerAgent cust, String choice) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == cust) {
				mc.setState(CustomerState.Ordered);
				mc.setChoice(choice);
				stateChanged();
			}
		}
	}
	
	public void msgOrderDone(String choice, int tableNum) {
		for (MyCustomer mc : customers) {
			if (mc.getTable() == tableNum && mc.getChoice() == choice) {
				mc.setState(CustomerState.OrderDone);
				stateChanged();
			}
		}
	}

	public void msgDoneEating(CustomerAgent cust) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == cust) {
				print(mc.getCust() + " leaving table " + mc.getTable());
				mc.setState(CustomerState.Leaving);
				stateChanged();
			}
		}
	}
	
	public void msgAtHome() {//from animation
		if (returningHome) {
			atHome.release();// = true;
			stateChanged();
			returningHome = false;
		}
	}

	public void msgAtTable() {//from animation
		atTable.release();// = true;
		stateChanged();
	}
	
	public void msgAtCook() {//from animation
		atCook.release();// = true;
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
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
			if (mc.getState() == CustomerState.Leaving){
				notifyHost(mc);
				return true;
			}
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	private void seatCustomer(MyCustomer mc) {
		returningHome = true;
		waiterGui.DoReturnHome();
		try {
			atHome.acquire();
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
	
	private void retrieveOrder(MyCustomer mc) {
		print("Retrieving order for table " + mc.getTable());
		waiterGui.DoGoToCook();
		try {
			atCook.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		print("Delivering " + mc.getChoice() + " to table " + mc.getTable());
		waiterGui.DoDeliverFood(mc.getChoice());
		mc.setState(CustomerState.ReadyToEat);
	}
	
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
		mc.setState(CustomerState.Eating);
		waiterGui.DoReturnHome();
	}
	
	private void notifyHost(MyCustomer mc) {
		host.msgTableAvailable(mc.getTable());
		waiterGui.DoReturnHome();
		mc.setState(CustomerState.DoingNothing);
	}

	// The animation DoXYZ() routines
	private void DoSeatCustomer(MyCustomer mc) {
		//Notice how we print "customer" directly. It's toString method will do it.
		//Same with "table"
		print("Seating " + mc.getCust() + " at table " + mc.getTable());
		waiterGui.DoGoToTable(mc.getTable()); 

	}

	//utilities

	public void setGui(WaiterGui gui) {
		waiterGui = gui;
	}

	public WaiterGui getGui() {
		return waiterGui;
	}

	private class MyCustomer {
		CustomerAgent cust;
		int table;
		private CustomerState state = CustomerState.DoingNothing;//The start state
		String choice;

		MyCustomer(CustomerAgent c, int tableNumber, CustomerState s) {
			cust = c;
			table = tableNumber;
			state = s;
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

