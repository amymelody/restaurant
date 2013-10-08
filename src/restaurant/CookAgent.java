package restaurant;

import agent.Agent;

import java.util.*;


/**
 * Restaurant Cook Agent
 */

public class CookAgent extends Agent {
	public List<Order> orders
	= new ArrayList<Order>();
	public List<MarketAgent> markets = new ArrayList<MarketAgent>();

	private String name;
	private Timer timer = new Timer();
	
	Food steak = new Food("steak", 15, 3, 1);
	Food chicken = new Food("chicken", 20, 3, 1);
	Food salad = new Food("salad", 5, 3, 1);
	Food pizza = new Food("pizza", 10, 3, 1);
	
	Map<String, Food> foods = new HashMap<String, Food>();
	
	public enum OrderState
	{Pending, Cooking, Done, Finished};
	public enum FoodState
	{Good, MustBeOrdered, Inquired, Ordered, WaitingForOrder};

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
	
	public void addMarket(MarketAgent m) {
		markets.add(m);
	}
	
	public void msgHereIsOrder(WaiterAgent waiter, String choice, int table) {
		orders.add(new Order(waiter, choice, table, OrderState.Pending));
		stateChanged();
	}
	
	public void msgCanFulfillOrder(String food, MarketAgent m) {
		for (MarketAgent market : markets) {
			if (market == m) {
				foods.get(food).setState(FoodState.Ordered);
				foods.get(food).setOrderedFrom(m);
				//print("Can, I will order from " + m.getName());
				//print("Can, " + foods.get(food).getState().toString());
				stateChanged();
			}
		}
	}
	
	public void msgCantFulfillOrder(String food, MarketAgent m) {
		for (MarketAgent market : markets) {
			if (market == m) {
				if (foods.get(food).getState() == FoodState.Inquired && markets.indexOf(market) == markets.size()-1) {
					//print("Can't, " + foods.get(food).getState().toString());
					foods.get(food).setState(FoodState.Ordered);
					foods.get(food).setOrderedFrom(m);
					//print("Can't, I will order from " + m.getName());
					stateChanged();
				}
			}
		}
	}
	
	public void msgOrderDelivered(MarketAgent m, String food, int amount) {
		for (MarketAgent market : markets) {
			if (market == m) {
				if (foods.get(food).getState() == FoodState.WaitingForOrder) {
					foods.get(food).setAmount(foods.get(food).getAmount()+amount);
					foods.get(food).setState(FoodState.Good);
				}
			}
		}
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		for (Food food : foods.values()) {
			if (food.getState() == FoodState.Ordered) {
				respondToMarkets(food);
				return true;
			}
		}
		for (Food food : foods.values()) {
			if (food.getState() == FoodState.MustBeOrdered) {
				orderFoodFromMarket();
				return true;
			}
		}
		for (Order order : orders) {
			if (order.getState() == OrderState.Done) {
				plateIt(order);
				return true;
			}
		}
		for (Order order : orders) {
			if (order.getState() == OrderState.Pending) {
				cookIt(order);
				return true;
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
		if (foods.get(o.choice).getAmount() <= foods.get(o.choice).getLow() && foods.get(o.choice).getState() == FoodState.Good) {
			foods.get(o.choice).setState(FoodState.MustBeOrdered);
		}
	}
	
	private void plateIt(Order o) {
		o.getWaiter().msgOrderDone(o.getChoice(), o.getTable());
		o.setState(OrderState.Finished);
	}
	
	private void orderFoodFromMarket() {
		for (Food food : foods.values()) {
			if (food.getState() == FoodState.MustBeOrdered) {
				food.setState(FoodState.Inquired);
				for (MarketAgent market : markets) {
					int amountToOrder = food.getCapacity()-food.getAmount();
					print(market.getName() + ", I need " + amountToOrder + " " + food.getType() + "s");
					market.msgHereIsOrder(food.getType(), amountToOrder);
				}
			}
		}
	}
	
	private void respondToMarkets(Food food) {
		print(food.getOrderedFrom().getName() + ", I would like to order " + food.getType());
		food.getOrderedFrom().msgIWouldLikeToOrder(food.getType());
		food.setState(FoodState.WaitingForOrder);
		for (MarketAgent market : markets) {
			if (market != food.getOrderedFrom()) {
				print(market.getName() + ", I would not like to order " + food.getType());
				market.msgIWouldNotLikeToOrder(food.getType());
			}
		}
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
		FoodState state;
		MarketAgent orderedFrom;
		
		Food(String t, int c, int a, int l) {
			type = t;
			cookingTime = c;
			amount = a;
			capacity = a;
			low = l;
			state = FoodState.Good;
			orderedFrom = null;
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
		
		int getLow() {
			return low;
		}
		
		int getCapacity() {
			return capacity;
		}
		
		FoodState getState() {
			return state;
		}
		
		void setState(FoodState s) {
			state = s;
		}
		
		MarketAgent getOrderedFrom() {
			return orderedFrom;
		}
		
		void setOrderedFrom(MarketAgent m) {
			orderedFrom = m;
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

