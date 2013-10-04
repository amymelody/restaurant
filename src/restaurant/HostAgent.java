package restaurant;

import agent.Agent;

import java.util.*;
import java.util.concurrent.Semaphore;

import restaurant.WaiterAgent.CustomerState;

/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class HostAgent extends Agent {
	static final int NTABLES = 3;//a global for the number of tables.
	//Notice that we implement waitingCustomers using ArrayList, but type it
	//with List semantics.
	public List<CustomerAgent> customers
	= new ArrayList<CustomerAgent>();
	public List<MyWaiter> waiters = new ArrayList<MyWaiter>();
	public Collection<Table> tables;
	//note that tables is typed with Collection semantics.
	//Later we will see how it is implemented

	private String name;
	
	public enum WaiterState
	{OnTheJob, WantToGoOnBreak, AboutToGoOnBreak, OnBreak};

	public HostAgent(String name) {
		super();

		this.name = name;
		// make some tables
		tables = new ArrayList<Table>(NTABLES);
		for (int ix = 1; ix <= NTABLES; ix++) {
			tables.add(new Table(ix));//how you add to a collections
		}
	}

	public String getMaitreDName() {
		return name;
	}

	public String getName() {
		return name;
	}

	public List getCustomers() {
		return customers;
	}

	public Collection getTables() {
		return tables;
	}
	
	public String toString() {
		return getName();
	}
	
	public boolean noWaitersOnBreak() {
		for (MyWaiter waiter : waiters) {
			if (waiter.getState() == WaiterState.OnBreak || waiter.getState() == WaiterState.AboutToGoOnBreak) {
				return false;
			}
		}
		return true;
	}
	
	// Messages
	
	public void addWaiter(WaiterAgent waiter) {
		waiters.add(new MyWaiter(waiter));
		stateChanged();
	}

	public void msgWantToGoOnBreak(WaiterAgent waiter) {
		for (MyWaiter mw : waiters) {
			if (mw.getWaiter() == waiter) {
				mw.setState(WaiterState.WantToGoOnBreak);
				stateChanged();
			}
		}
	}
	
	public void msgGoingOnBreak(WaiterAgent waiter) {
		for (MyWaiter mw : waiters) {
			if (mw.getWaiter() == waiter) {
				mw.setState(WaiterState.OnBreak);
				stateChanged();
			}
		}
	}
	
	public void msgGoingOffBreak(WaiterAgent waiter) {
		for (MyWaiter mw : waiters) {
			if (mw.getWaiter() == waiter) {
				mw.setState(WaiterState.OnTheJob);
				stateChanged();
			}
		}
	}
	
	public void msgIWantFood(CustomerAgent cust) {
		customers.add(cust);
		stateChanged();
	}

	public void msgTableAvailable(int tableNum) {
		for (Table table : tables) {
			if (table.getTableNumber() == tableNum) {
				table.setOccupied(false);
				stateChanged();
			}
		}
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		/* Think of this next rule as:
            Does there exist a table and customer,
            so that table is unoccupied and customer is waiting.
            If so seat him at the table.
		 */
		for (Table table : tables) {
			if (!table.isOccupied()) {
				if (!customers.isEmpty() && !waiters.isEmpty()) {
					int index = 0;
					if (waiters.size() > 1) {
						for (int i = waiters.size()-2; i>=0; i--) {
							if (waiters.get(i+1).getState() == WaiterState.OnTheJob || waiters.get(i+1).getState() == WaiterState.WantToGoOnBreak) {
								if (waiters.get(i).getWaiter().getCustomerCount() > waiters.get(i+1).getWaiter().getCustomerCount()) {
									index = i+1;
								}
							}
						}
					}
					if (index == 0 && (waiters.get(index).getState() == WaiterState.AboutToGoOnBreak || waiters.get(index).getState() == WaiterState.OnBreak)) {
						index = 1;
					}
					callWaiter(waiters.get(index).getWaiter(), customers.get(0), table);//the action
					waiters.get(index).getWaiter().addCustomer();
					return true;//return true to the abstract agent to reinvoke the scheduler.
				}
			}
		}
		
		for (MyWaiter mw : waiters) {
			if (mw.getState() == WaiterState.WantToGoOnBreak) {
				if (waiters.size() > 1 && noWaitersOnBreak()) {
					canGoOnBreak(mw);
					return true;
				}
				cantGoOnBreak(mw);
				return true;
			}
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	private void callWaiter(WaiterAgent waiter, CustomerAgent customer, Table table) {
		print(waiter + ", please bring " + customer + " to " + table);
		waiter.msgPleaseSeatCustomer(customer, table.getTableNumber());
		for (Table t : tables) {
			if (t == table) {
				t.setOccupied(true);
			}
		}
		customers.remove(customer);
	}
	
	private void canGoOnBreak(MyWaiter mw) {
		print(mw.getWaiter() + ", you can go on break.");
		mw.getWaiter().msgCanGoOnBreak();
		mw.setState(WaiterState.AboutToGoOnBreak);
	}
	
	private void cantGoOnBreak(MyWaiter mw) {
		print(mw.getWaiter() + ", you can't go on break.");
		mw.getWaiter().msgCantGoOnBreak();
		mw.setState(WaiterState.OnTheJob);
	}

	//utilities

	private class Table {
		boolean occupied;
		int tableNumber;

		Table(int tableNumber) {
			this.tableNumber = tableNumber;
		}

		void setOccupied(boolean occupied) {
			this.occupied = occupied;
		}

		boolean isOccupied() {
			return occupied;
		}

		public String toString() {
			return "table " + tableNumber;
		}
		
		public int getTableNumber() {
			return tableNumber;
		}
	}
	
	private class MyWaiter {
		WaiterAgent waiter;
		WaiterState state;

		MyWaiter(WaiterAgent w) {
			waiter = w;
			state = WaiterState.OnTheJob;
		}

		WaiterAgent getWaiter() {
			return waiter;
		}
		
		WaiterState getState() {
			return state;
		}
		
		void setState(WaiterState w) {
			state = w;
		}
	}
}

