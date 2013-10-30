package restaurant;

import agent.Agent;
import restaurant.CustomerAgent.AgentEvent;
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
	 * hack to establish connection to Host agent.
	 */
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	public void setCook(CookAgent cook) {
		this.cook = cook;
	}
	
	public void setCashier(CashierAgent cashier) {
		this.cashier = cashier;
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
	
	public boolean doneServingCustomers() {
		for (MyCustomer mc : customers) {
			if (mc.getState() != CustomerState.DoingNothing) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isOnBreak() {
		if (state == WaiterState.AboutToGoOnBreak || state == WaiterState.OnBreak) {
			return true;
		}
		return false;
	}
	
	public boolean isAboutToGoOnBreak() {
		if (state == WaiterState.AboutToGoOnBreak) {
			return true;
		}
		return false;
	}
	
	// Messages
	
	public void msgWantToGoOnBreak() {
		state = WaiterState.WantToGoOnBreak;
		stateChanged();
	}
	
	public void msgCanGoOnBreak() {
		state = WaiterState.AboutToGoOnBreak;
		stateChanged();
	}
	
	public void msgCantGoOnBreak() {
		state = WaiterState.OnTheJob;
		waiterGui.setCBEnabled();
		stateChanged();
	}
	
	public void msgGoOffBreak() {
		state = WaiterState.GoingOffBreak;
		stateChanged();
	}
	
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
	
	public void msgOutOfFood(String choice, int table) {
		menu.removeItem(choice);
		for (MyCustomer mc : customers) {
			if (mc.getTable() == table) {
				mc.setState(CustomerState.MustReorder);
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
				mc.setState(CustomerState.WaitingForCheck);
				stateChanged();
			}
		}
	}
	
	public void msgFoodArrived(String food) {
		if (!menu.checkItem(food)) {
			menu.addItem(food, prices.get(food));
			stateChanged();
		}
	}
	
	public void msgHereIsCheck(Customer c, int charge) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == c) {
				mc.setCharge(charge);
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
	
	private void wantToGoOnBreak() {
		print(host + ", I want to go on break.");
		host.msgWantToGoOnBreak(this);
		state = WaiterState.OnTheJob;
	}
	
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
	
	private void goOffBreak() {
		print(host + ", I'm going off break.");
		host.msgGoingOffBreak(this);
		state = WaiterState.OnTheJob;
	}

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
	
	private void notifyHost(MyCustomer mc) {
		host.msgTableAvailable(mc.getTable());
		waiterGui.DoReturnHome();
		mc.setState(CustomerState.DoingNothing);
	}
	
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

	private void DoSeatCustomer(MyCustomer mc) {
		print("Seating " + mc.getCust() + " at table " + mc.getTable());
		waiterGui.DoGoToTable(mc.getTable()); 

	}

	//utilities

	public void setGui(WaiterGui gui) {
		waiterGui = gui;
	}
	
	public void setGui(CookGui gui) {
		cookGui = gui;
	}

	public WaiterGui getGui() {
		return waiterGui;
	}

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

