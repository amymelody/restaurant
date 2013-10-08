package restaurant;

import agent.Agent;

import java.util.*;


/**
 * Restaurant Market Agent
 */

public class MarketAgent extends Agent {
	public List<Order> orders
	= new ArrayList<Order>();

	private String name;
	private CookAgent cook;
	private Timer timer = new Timer();
	
	Food steak;
	Food chicken;
	Food salad;
	Food pizza;
	
	Map<String, Food> foods = new HashMap<String, Food>();
	
	public enum OrderState
	{Received, Waiting, Pending, ProducingOrder, Ready, Finished};
	
	public MarketAgent(String name, CookAgent c, int stAmt, int cAmt, int saAmt, int pAmt) {
		super();
		this.name = name;
		cook = c;
		
		steak = new Food("steak", 15, stAmt);
		chicken = new Food("chicken", 20, cAmt);
		salad = new Food("salad", 5, saAmt);
		pizza = new Food("pizza", 10, pAmt);
		
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
	
	public void msgHereIsOrder(String food, int amount) {
		boolean canFulfillOrder;
		if (foods.get(food).amount >= amount) {
			canFulfillOrder = true;
		} else { 
			canFulfillOrder = false;
		}
		orders.add(new Order(food, amount, canFulfillOrder, OrderState.Received));
		stateChanged();
	}
	
	public void msgIWouldLikeToOrder(String food) {
		for (Order o : orders) {
			if (o.getState() == OrderState.Waiting && o.getFood() == food) {
				o.setState(OrderState.Pending);
			}
		}
		stateChanged();
	}
	
	public void msgIWouldNotLikeToOrder(String food) {
		for (Order o : orders) {
			if (o.getState() == OrderState.Waiting && o.getFood() == food) {
				orders.remove(o);
				stateChanged();
				return;
			}
		}
	}
	
	public void msgOrderReady(Order o) {
		o.setState(OrderState.Ready);
	}


	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		for (Order order : orders) {
			if (order.getState() == OrderState.Ready) {
				deliverOrder(order);
				return true;
			}
		}
		for (Order order : orders) {
			if (order.getState() == OrderState.Received) {
				respondToCook(order);
				return true;
			}
		}
		for (Order order : orders) {
			if (order.getState() == OrderState.Pending) {
				produceOrder(order);
				return true;
			}
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	private void produceOrder(Order o) {
		o.setState(OrderState.ProducingOrder);
		if (o.getAmount() > foods.get(o.food).getAmount()) {
			o.setAmount(foods.get(o.food).getAmount());
		}
		timer.schedule(new ProducingTimerTask(o) {
			@Override
			public void run() {
				order.setState(OrderState.Ready);
				stateChanged();
			}
		},
		foods.get(o.food).getTimeToProduce() * o.amount * 500);
		foods.get(o.food).setAmount(foods.get(o.food).getAmount()-o.amount);
	}
	
	private void deliverOrder(Order o) {
		print("Here is your order of " + o.amount + " " + o.food + "s");
		cook.msgOrderDelivered(this, o.food, o.amount);
		o.setState(OrderState.Finished);
	}
	
	private void respondToCook(Order o) {
		if (o.canFulfillOrder) {
			print("I can fulfill the order for " + o.food);
			cook.msgCanFulfillOrder(o.food, this);
		} else {
			print("I can't fulfill the order for " + o.food);
			cook.msgCantFulfillOrder(o.food, this);
		}
		o.setState(OrderState.Waiting);
	}
	

	//utilities

	private class Order {
		int amount;
		String food;
		boolean canFulfillOrder;
		OrderState state;

		Order(String f, int a, boolean c, OrderState s) {
			food = f;
			amount = a;
			state = s;
			canFulfillOrder = c;
		}
		
		public int getAmount() {
			return amount;
		}
		
		void setAmount(int a) {
			amount = a;
		}
		
		public boolean getCanFulfill() {
			return canFulfillOrder;
		}
		
		void setCanFulfill(boolean c) {
			canFulfillOrder = c;
		}
		
		OrderState getState() {
			return state;
		}
		
		void setState(OrderState s) {
			state = s;
		}
		
		String getFood() {
			return food;
		}
		
		void setFood(String f) {
			food = f;
		}
	}
	
	private class Food {
		String type;
		int timeToProduce;
		int amount;
		
		Food(String t, int p, int a) {
			type = t;
			timeToProduce = p;
			amount = a;
		}
		
		String getType() {
			return type;
		}
		
		int getTimeToProduce() {
			return timeToProduce;
		}
		
		void setAmount(int a) {
			amount = a;
		}
		
		int getAmount() {
			return amount;
		}
	}
	
	private class ProducingTimerTask extends TimerTask {
		Order order;
		
		ProducingTimerTask(Order o) {
			order = o;
		}
		
		public void run() {
			order = order;
		}
	}
}

