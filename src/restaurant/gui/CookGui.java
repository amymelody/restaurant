package restaurant.gui;

import restaurant.CookAgent;
import restaurant.WaiterAgent;
import restaurant.CashierAgent.Check;
import restaurant.CookAgent.OrderState;

import java.awt.*;
import java.util.*;

public class CookGui implements Gui{
	
	private CookAgent agent = null;
	RestaurantGui gui;
	private Timer timer = new Timer();
	public ArrayList<Order> orders = new ArrayList<Order>();
	
	static final int COOKINGW = 60;
	static final int COOKINGH = 40;
	static final int COOKINGX = 350;
	static final int COOKINGY = 20;
	static final int PLATINGW = 60;
	static final int PLATINGH = 40;
	static final int PLATINGX = 250;
	static final int PLATINGY = 20;
	static final int FOODW = 20;
	static final int FOODH = 20;

	Map<String, String> foodSymbols = new HashMap<String, String>();

	public CookGui(CookAgent c, RestaurantGui gui){
		this.gui = gui;
		
		foodSymbols.put("steak", "St");
		foodSymbols.put("chicken", "C");
		foodSymbols.put("pizza", "P");
		foodSymbols.put("salad", "Sa");
	}

	public void updatePosition() {
		
	}

	public void draw(Graphics2D g) {
		g.setColor(Color.RED);
		g.fillRect(COOKINGX, COOKINGY, COOKINGW, COOKINGH);
		g.setColor(Color.BLACK);
		g.fillRect(PLATINGX, PLATINGY, PLATINGW, PLATINGH);
		try {
			for (Order order : orders) {
				if (order.done) { 
					g.setColor(Color.WHITE);
					g.fillRect(PLATINGX, PLATINGY, FOODW, FOODH);
					g.setColor(Color.BLACK);
					g.drawString(foodSymbols.get(order.food), PLATINGX+FOODW/4, PLATINGY+FOODH/2);
				} else {
					g.setColor(Color.WHITE);
					g.fillRect(COOKINGX, COOKINGY, FOODW, FOODH);
					g.setColor(Color.BLACK);
					g.drawString(foodSymbols.get(order.food), COOKINGX+FOODW/4, COOKINGY+FOODH/2);
				}
			}
		} catch (ConcurrentModificationException e) {
			return;
		}
	}
	
	public void DoCookFood(String food) {
		orders.add(new Order(food));
	}
	
	public void DoPlateFood(String food) {
		try {
			for (Order order : orders) {
				if (order.food.equals(food) && !order.done) {
					order.done = true;
					return;
				}
			}
		} catch (ConcurrentModificationException e) {
			return;
		}
	}
	
	public void DoRemoveFood(String food) {
		try {
			for (Order order : orders) {
				if (order.food.equals(food)) {
					orders.remove(order);
					return;
				}
			}
		} catch (ConcurrentModificationException e) {
			return;
		}
	}

	public boolean isPresent() {
		return true;
	}
	
	private class Order {
		String food;
		boolean done;

		Order(String f) {
			food = f;
			done = false;
		}
	}
}
