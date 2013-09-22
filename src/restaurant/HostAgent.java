package restaurant;

import agent.Agent;

import java.util.*;
import java.util.concurrent.Semaphore;

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
	public List<WaiterAgent> waiters = new ArrayList<WaiterAgent>();
	public Collection<Table> tables;
	//note that tables is typed with Collection semantics.
	//Later we will see how it is implemented

	private String name;

	public HostAgent(String name) {
		super();

		this.name = name;
		// make some tables
		tables = new ArrayList<Table>(NTABLES);
		for (int ix = 1; ix <= NTABLES; ix++) {
			tables.add(new Table(ix));//how you add to a collections
		}
	}
	
	public void addWaiter(WaiterAgent waiter) {
		waiters.add(waiter);
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
	// Messages

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
				if (!customers.isEmpty()) {
					int index = 0;
					if (waiters.size() > 1) {
						for (int i = waiters.size()-2; i>=0; i--) {
							if (waiters.get(i).getCustomerCount() > waiters.get(i+1).getCustomerCount()) {
								index = i+1;
							}
						}
					}
					callWaiter(waiters.get(index), customers.get(0), table);//the action
					waiters.get(index).addCustomer();
					return true;//return true to the abstract agent to reinvoke the scheduler.
				}
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
}

