package restaurant;

import java.util.*;

public class Menu {
	private List<String> items = new ArrayList<String>();
	private Map<String, Integer> prices = new HashMap<String, Integer>();
	
	Menu() {}
	
	public void addItem(String item, int price) {
		items.add(item);
		prices.put(item, price);
	}
	
	public void removeItem(String item) {
		items.remove(item);
		prices.remove(item);
	}
	
	public int getPrice(String item) {
		return prices.get(item);
	}
	
	public int lowestPrice() {
		int low = 10000;
		for (Integer price : prices.values()) {
			if (price < low) {
				low = price;
			}
		}
		return low;
	}
	
	public boolean checkItem(String item) {
		return items.contains(item);
	}
	
	public String randomItem() {
		int rand = (int)(Math.random() * items.size());
		return items.get(rand);
	}
}
