package restaurant;

import agent.Agent;
import java.util.*;

/**
 * Restaurant Host Agent
 */

public class HostAgent extends Agent {
	static final int NTABLES = 3;
	public List<MyCustomer> customers = Collections.synchronizedList(new ArrayList<MyCustomer>());
	public List<MyWaiter> waiters = Collections.synchronizedList(new ArrayList<MyWaiter>());
	public List<String> foods = Collections.synchronizedList(new ArrayList<String>());
	public Collection<Table> tables;

	private String name;
	
	public enum WaiterState
	{OnTheJob, WantToGoOnBreak, AboutToGoOnBreak, OnBreak};

	/**
	 * Constructor
	 *
	 * @param name Agent name for messages
	 */
	public HostAgent(String name) {
		super();

		this.name = name;
		// make some tables
		tables = Collections.synchronizedList(new ArrayList<Table>(NTABLES));
		for (int ix = 1; ix <= NTABLES; ix++) {
			tables.add(new Table(ix));//how you add to a collections
		}
	}

	/**
	 * Returns the Agent's name
	 */
	public String getMaitreDName() {
		return name;
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
	 * Checks if all tables in restaurant are occupied
	 * 
	 * @return true if all tables are occupied, false otherwise
	 */
	public boolean restaurantFull() {
		synchronized(tables) {
			for (Table table : tables) {
				if (!table.isOccupied()) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Checks if there are no WaiterAgents on break
	 * 
	 * @return true if there are no waiters on break, false otherwise
	 */
	public boolean noWaitersOnBreak() {
		synchronized(waiters) {
			for (MyWaiter waiter : waiters) {
				if (waiter.getState() == WaiterState.OnBreak || waiter.getState() == WaiterState.AboutToGoOnBreak) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Hack to establish connection with WaiterAgents
	 * 
	 * @param waiter Reference to WaiterAgent
	 */
	public void addWaiter(WaiterAgent waiter) {
		waiters.add(new MyWaiter(waiter));
		stateChanged();
	}
	
	// Messages

	public void msgWantToGoOnBreak(WaiterAgent waiter) {
		synchronized(waiters) {
			for (MyWaiter mw : waiters) {
				if (mw.getWaiter() == waiter) {
					mw.setState(WaiterState.WantToGoOnBreak);
					stateChanged();
				}
			}
		}
	}
	
	public void msgGoingOnBreak(WaiterAgent waiter) {
		synchronized(waiters) {
			for (MyWaiter mw : waiters) {
				if (mw.getWaiter() == waiter) {
					mw.setState(WaiterState.OnBreak);
					stateChanged();
				}
			}
		}
	}
	
	public void msgGoingOffBreak(WaiterAgent waiter) {
		synchronized(waiters) {
			for (MyWaiter mw : waiters) {
				if (mw.getWaiter() == waiter) {
					mw.setState(WaiterState.OnTheJob);
					stateChanged();
				}
			}
		}
	}
	
	public void msgIWantFood(CustomerAgent cust) {
		customers.add(new MyCustomer(cust));
		stateChanged();
	}

	public void msgTableAvailable(int tableNum) {
		synchronized(tables) {
			for (Table table : tables) {
				if (table.getTableNumber() == tableNum) {
					table.setOccupied(false);
					stateChanged();
				}
			}
		}
	}
	
	public void msgReceivedOrder(String food) {
		foods.add(food);
		stateChanged();
	}
	
	public void msgImLeaving(CustomerAgent c) {
		synchronized(customers) {
			for (MyCustomer mc : customers) {
				if (mc.cust == c) {
					customers.remove(mc);
					stateChanged();
					return;
				}
			}
		}
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {
		synchronized(customers) {
			for (MyCustomer mc : customers) {
				if (mc.waiting && restaurantFull()) {
					tellCustomer(mc);
				}
			}
		}
		
		synchronized(tables) {
			for (Table table : tables) {
				if (!table.isOccupied()) {
					if (!customers.isEmpty() && !waiters.isEmpty()) {
						int index = 0;
						if (waiters.size() > 1) {
							for (int i = waiters.size()-2; i>=0; i--) {
								if (waiters.get(i+1).getState() == WaiterState.OnTheJob || waiters.get(i+1).getState() == WaiterState.WantToGoOnBreak) {
									if (waiters.get(i).customerCount > waiters.get(i+1).customerCount) {
										index = i+1;
									}
								}
							}
						}
						if (index == 0 && (waiters.get(index).getState() == WaiterState.AboutToGoOnBreak || waiters.get(index).getState() == WaiterState.OnBreak)) {
							index = 1;
						}
						callWaiter(waiters.get(index).getWaiter(), customers.get(0), table);//the action
						waiters.get(index).addCustomer();
						return true;//return true to the abstract agent to reinvoke the scheduler.
					}
				}
			}
		}
		
		synchronized(waiters) {
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
		}
		
		synchronized(foods) {
			for (String f : foods) {
				notifyWaiters(f);
				foods.remove(f);
				return true;
			}
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	private void callWaiter(WaiterAgent waiter, MyCustomer mc, Table table) {
		print(waiter + ", please bring " + mc.cust + " to " + table);
		waiter.msgPleaseSeatCustomer(mc.cust, table.getTableNumber());
		synchronized(tables) {
			for (Table t : tables) {
				if (t == table) {
					t.setOccupied(true);
				}
			}
		}
		customers.remove(mc);
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
	
	private void notifyWaiters(String food) {
		synchronized(waiters) {
			for (MyWaiter mw : waiters) {
				mw.getWaiter().msgFoodArrived(food);
			}
		}
	}
	
	private void tellCustomer(MyCustomer mc) {
		print(mc.cust + ", the restaurant is full");
		mc.cust.msgRestaurantIsFull();
		mc.waiting = false;
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
	
	private class MyCustomer {
		CustomerAgent cust;
		boolean waiting;

		MyCustomer(CustomerAgent c) {
			cust = c;
			waiting = true;
		}
	}
	
	private class MyWaiter {
		WaiterAgent waiter;
		WaiterState state;
		int customerCount;

		MyWaiter(WaiterAgent w) {
			waiter = w;
			state = WaiterState.OnTheJob;
			customerCount = 0;
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
		
		void addCustomer() {
			customerCount++;
		}
	}
}

