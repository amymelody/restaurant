package restaurant;

import agent.Agent;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Cook Agent
 */

public class CookAgent extends Agent {
	public List<Order> orders
	= new ArrayList<Order>();

	private String name;
	private Timer timer;
	
	Map<String, Food> foods;
	
	public enum OrderState
	{Pending, Cooking, Done, Finished};

	public CookAgent(String name) {
		super();

		this.name = name;
	}
	

	public String getName() {
		return name;
	}

	public List getOrders() {
		return orders;
	}
	
	// Messages
	
	public void msgHereIsOrder(WaiterAgent waiter, String choice, int table) {
		orders.add(new Order(waiter, choice, table, OrderState.Pending));
	}
	
	public void msgFoodDone(Order o) {
		o.setState(OrderState.Done);
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		for (Order order : orders) {
			if (order.getState() == OrderState.Done) {
				plateIt(order);
				return true;//return true to the abstract agent to reinvoke the scheduler.
			}
			if (order.getState() == OrderState.Pending) {
				cookIt(order);
				return true;//return true to the abstract agent to reinvoke the scheduler.
			}
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	private void cookIt(Order o) {
		o.setState(OrderState.Cooking);
		//timer.start(run(FoodDone(o)), foods.get(o.choice.getCookingTime()));
	}
	
	private void plateIt(Order o) {
		o.getWaiter().OrderDone(o.getChoice(), o.getTable());
		o.setState(OrderState.Finished);
	}
	

	//utilities

	private class Order {
		WaiterAgent waiter;
		int table;
		private OrderState state;
		String choice;

		Order(WaiterAgent w, String c, int t, OrderState s) {
			waiter = w;
			choice = c;
			table = t;
			state = s;
		}

		WaiterAgent getWaiter() {
			return waiter;
		}
		
		public int getTable() {
			return table;
		}
		
		OrderState getState() {
			return state;
		}
		
		void setState(OrderState s) {
			state = s;
		}
		
		String getChoice() {
			return choice;
		}
		
		void setChoice(String c) {
			choice = c;
		}
	}
	
	private class Food {
		String type;
		int cookingTime;
		
		Food(String t, int c) {
			type = t;
			cookingTime = c;
		}
		
		String getType() {
			return type;
		}
		
		int getCookingTime() {
			return cookingTime;
		}
	}
}

