package restaurant.gui;

import java.awt.*;
import java.util.*;

/**
 * Restaurant Cooking Area GUI
 */
public class CookGui implements Gui{
	
	RestaurantGui gui;
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

	/**
	 * Constructor
	 * 
	 * @param gui Reference to RestaurantGui
	 */
	public CookGui(RestaurantGui gui){
		this.gui = gui;
		
		foodSymbols.put("steak", "St");
		foodSymbols.put("chicken", "C");
		foodSymbols.put("pizza", "P");
		foodSymbols.put("salad", "Sa");
	}

	/**
	 * This is what drives the animation; called each time the animation timer fires
	 */
	public void updatePosition() {
		
	}

	/**
     * This creates the visuals themselves based on the position set in updatePosition
     */
	public void draw(Graphics2D g) {
		g.setColor(Color.RED);
		g.fillRect(COOKINGX, COOKINGY, COOKINGW, COOKINGH);
		g.setColor(Color.BLACK);
		g.fillRect(PLATINGX, PLATINGY, PLATINGW, PLATINGH);
		try {
			int cookingTotal = 0;
			int platedTotal = 0;
			for (Order order : orders) {
				if (order.done) { 
					g.setColor(Color.WHITE);
					g.fillRect(PLATINGX + (platedTotal%3)*20, PLATINGY + (platedTotal-platedTotal%3)/3, FOODW, FOODH);
					g.setColor(Color.BLACK);
					g.drawString(foodSymbols.get(order.food), PLATINGX+FOODW/4 + (platedTotal%3)*20, PLATINGY+FOODH/2 + (platedTotal-platedTotal%3)/3);
					platedTotal++;
				} else {
					g.setColor(Color.WHITE);
					g.fillRect(COOKINGX + (cookingTotal%3)*20, COOKINGY + (cookingTotal-cookingTotal%3)/3, FOODW, FOODH);
					g.setColor(Color.BLACK);
					g.drawString(foodSymbols.get(order.food), COOKINGX+FOODW/4 + (cookingTotal%3)*20, COOKINGY+FOODH/2 + (cookingTotal-cookingTotal%3)/3);
					cookingTotal++;
				}
			}
		} catch (ConcurrentModificationException e) {
			return;
		}
	}
	
	/**
	 * Adds a new order to the list of orders
	 * 
	 * @param food Name of food
	 */
	public void DoCookFood(String food) {
		orders.add(new Order(food));
	}
	
	/**
	 * Makes an order done so that it can be put in the plating area
	 * 
	 * @param food Name of food
	 */
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
	
	/**
	 * Removes an order from the list of orders
	 * 
	 * @param food Name of food
	 */
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

	/**
	 * This returns true if the GUI is currently present in the animation window, false otherwise
	 */
	public boolean isPresent() {
		return true;
	}
	
	/**
	 * Contains all information about an order relevant to the CookGui
	 */
	private class Order {
		String food;
		boolean done;

		Order(String f) {
			food = f;
			done = false;
		}
	}
}
