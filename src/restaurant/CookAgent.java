package restaurant;

import agent.Agent;

import java.util.*;


/**
 * Restaurant Cook Agent
 */

public class CookAgent extends Agent {
	public List<Order> orders
	= new ArrayList<Order>();

	private String name;
	private Timer timer = new Timer();
	
	Food steak = new Food("steak", 15, 2, 1);
	Food chicken = new Food("chicken", 20, 2, 1);
	Food salad = new Food("salad", 5, 3, 1);
	Food pizza = new Food("pizza", 10, 3, 1);
	
	Map<String, Food> foods = new HashMap<String, Food>();
	
	public enum OrderState
	{Pending, Cooking, Done, Finished};

	public CookAgent(String name) {
		super();
		this.name = name;
		
		foods.put("steak", steak);
		foods.put("chicken", chicken);
		foods.put("salad", salad);
		foods.put("pizza", pizza);
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
		stateChanged();
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
		}
		for (Order order : orders) {
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
		if (foods.get(o.choice).getAmount() == 0) {
			print("We're out of " + o.choice);
			o.waiter.msgOutOfFood(o.choice, o.table);
			o.setState(OrderState.Finished);
			return;
		}
		o.setState(OrderState.Cooking);
		timer.schedule(new CookingTimerTask(o) {
			@Override
			public void run() {
				order.setState(OrderState.Done);
				stateChanged();
			}
		},
		foods.get(o.choice).getCookingTime() * 1000);
		foods.get(o.choice).setAmount(foods.get(o.choice).getAmount()-1);
	}
	
	private void plateIt(Order o) {
		o.getWaiter().msgOrderDone(o.getChoice(), o.getTable());
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
		int amount;
		int low;
		int capacity;
		boolean orderingState;
		
		Food(String t, int c, int a, int l) {
			type = t;
			cookingTime = c;
			amount = a;
			capacity = a;
			low = l;
			orderingState = false;
		}
		
		String getType() {
			return type;
		}
		
		int getCookingTime() {
			return cookingTime;
		}
		
		void setAmount(int a) {
			amount = a;
		}
		
		int getAmount() {
			return amount;
		}
	}
	
	private class CookingTimerTask extends TimerTask {
		Order order;
		
		CookingTimerTask(Order o) {
			order = o;
		}
		
		public void run() {
			order = order;
		}
	}
}

