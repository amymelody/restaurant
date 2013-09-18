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
	private Semaphore atTable = new Semaphore(0,true);
	private boolean readyToSeat = true;
	
	public enum CustomerState
	{DoingNothing, Waiting, Seated, AskedToOrder, Asked, Ordered, WaitingForFood, ReadyToEat, Eating, Leaving};

	public WaiterGui waiterGui = null;

	public WaiterAgent(String name) {
		super();

		this.name = name;
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
	
	// Messages
	
	public void msgPleaseSeatCustomer(CustomerAgent cust, int tableNumber) {
		customers.add(new MyCustomer(cust, tableNumber, CustomerState.Waiting));
	}
	
	/*public void msgReadyToOrder(CustomerAgent cust) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == cust) {
				mc.setState(CustomerState.AskedToOrder);
			}
		}
	}
	
	public void msgHereIsChoice(CustomerAgent cust, String choice) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == cust) {
				mc.setState(CustomerState.Ordered);
				mc.setChoice(choice);
			}
		}
	}
	
	public void msgOrderDone(String choice, int tableNum) {
		for (MyCustomer mc : customers) {
			if (mc.getTable() == tableNum && mc.getChoice() == choice) {
				mc.setState(CustomerState.ReadyToEat);
			}
		}
	}*/

	public void msgDoneEating(CustomerAgent cust) {
		for (MyCustomer mc : customers) {
			if (mc.getCust() == cust) {
				print(mc.getCust() + " leaving table " + mc.getTable());
				mc.setState(CustomerState.Leaving);
				stateChanged();
			}
		}
	}
	
	public void msgReadyToSeat() {
		readyToSeat = true;
		stateChanged();
	}

	public void msgAtTable() {//from animation
		//print("msgAtTable() called");
		atTable.release();// = true;
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		if (readyToSeat) {
			for (MyCustomer mc : customers) {
				if (mc.getState() == CustomerState.Waiting) {
					seatCustomer(mc);//the action
					readyToSeat = false;
					return true;//return true to the abstract agent to reinvoke the scheduler.
				}
				if (mc.getState() == CustomerState.Leaving){
					notifyHost(mc);
					return true;
				}
			}
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	private void seatCustomer(MyCustomer mc) {
		mc.getCust().msgFollowMe(this, mc.getTable());
		DoSeatCustomer(mc);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mc.setState(CustomerState.Seated);
		waiterGui.DoLeaveCustomer();
	}
	
	/*private void takeOrder(MyCustomer mc) {
		waiterGui.DoGoToTable(mc.getTable());
		mc.getCust().WhatWouldYouLike();
		mc.setState(CustomerState.Asked);
	}
	
	private void giveOrderToCook(MyCustomer mc) {
		mc.setState(CustomerState.WaitingForFood);
		waiterGui.DoGoToCook();
		cook.HereIsOrder(this, mc.getChoice(), mc.getTable());
	}
	
	private void deliverFood(MyCustomer mc) {
		waiterGui.DoGoToTable(mc.getTable());
		mc.getCust().HereIsFood(mc.getChoice());
		mc.setState(CustomerState.Eating);
	}*/
	
	private void notifyHost(MyCustomer mc) {
		host.msgTableAvailable(mc.getTable());
		mc.setState(CustomerState.DoingNothing);
	}

	// The animation DoXYZ() routines
	private void DoSeatCustomer(MyCustomer mc) {
		//Notice how we print "customer" directly. It's toString method will do it.
		//Same with "table"
		print("Seating " + mc.getCust() + " at table " + mc.getTable());
		waiterGui.DoBringToTable(mc.getCust(), mc.getTable()); 

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
		
		String getChoice() {
			return choice;
		}
		
		void setChoice(String c) {
			choice = c;
		}
	}
}

