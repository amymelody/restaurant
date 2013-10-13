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
	{Received, ProducingOrder, Ready, Finished};
	
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
	
	public boolean canFulfillOrder(List<ItemOrder> items) {
		for (ItemOrder io : items) {
			if (foods.get(io.getFood()).amount < io.getAmount()) {
				return false;
			}
		}
		return true;
	}
	
	public int timeToProduceOrder(Order o) {
		int time = 0;
		for (ItemOrder io : o.items) {
			time += foods.get(io.getFood()).timeToProduce*io.getAmount();
		}
		return time*400;
	}
	
	// Messages
	
	public void msgHereIsOrder(List<ItemOrder> io) {
		List<ItemOrder> temp = new ArrayList<ItemOrder>();
		for (ItemOrder o : io) {
			temp.add(o);
		}
		orders.add(new Order(temp, canFulfillOrder(io), OrderState.Received));
		stateChanged();
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
			if (order.getState() == OrderState.Received && !order.canFulfillOrder) {
				respondToCook(order);
				return true;
			}
		}
		for (Order order : orders) {
			if (order.getState() == OrderState.Received && order.canFulfillOrder) {
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
		timer.schedule(new ProducingTimerTask(o) {
			@Override
			public void run() {
				order.setState(OrderState.Ready);
				stateChanged();
			}
		},
		timeToProduceOrder(o));
		for (ItemOrder io : o.items) {
			foods.get(io.getFood()).amount -= io.getAmount();
		}
	}
	
	private void deliverOrder(Order o) {
		print("Here is your order: ");
		for (ItemOrder io : o.items) {
			print(io.getAmount() + " " + io.getFood() + "s");
		}
		cook.msgOrderDelivered(o.items);
		o.setState(OrderState.Finished);
	}
	
	private void respondToCook(Order o) {
		print("I can't fulfill this order");
		cook.msgCantFulfillOrder(o.items);
		o.setState(OrderState.Finished);
	}
	

	//utilities

	private class Order {
		List<ItemOrder> items;
		boolean canFulfillOrder;
		OrderState state;

		Order(List<ItemOrder> io, boolean c, OrderState s) {
			items = io;
			state = s;
			canFulfillOrder = c;
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

