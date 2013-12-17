package restaurant;

import java.util.*;

/**
 * Class that contains a list of Strings representing foods as well as prices for these foods
 *
 */
public class Menu {
	private List<String> items = new ArrayList<String>();
	private Map<String, Integer> prices = new HashMap<String, Integer>();
	
	/**
	 * Constructor
	 */
	Menu() {}
	
	/**
	 * Adds a food with specified name and price to the menu
	 * 
	 * @param item Name of the food
	 * @param price Integer price of the food
	 */
	public void addItem(String item, int price) {
		items.add(item);
		prices.put(item, price);
	}
	
	/**
	 * Removes a food with the specified name from the menu
	 * 
	 * @param item Name of the food to remove
	 */
	public void removeItem(String item) {
		items.remove(item);
		prices.remove(item);
	}
	
	/**
	 * Returns the integer price of a food with the specified name
	 * 
	 * @param item Name of the food
	 */
	public int getPrice(String item) {
		return prices.get(item);
	}
	
	/**
	 * Returns the lowest price on the menu
	 */
	public int lowestPrice() {
		int low = 10000;
		for (Integer price : prices.values()) {
			if (price < low) {
				low = price;
			}
		}
		return low;
	}
	
	/**
	 * Checks whether the menu contains a food item of the specified name
	 * 
	 * @param item Name of the food
	 * @return true if the item exists on the menu, false otherwise
	 */
	public boolean checkItem(String item) {
		return items.contains(item);
	}
	
	/**
	 * Returns the name of a random food item from the menu
	 */
	public String randomItem() {
		int rand = (int)(Math.random() * items.size());
		return items.get(rand);
	}
}
