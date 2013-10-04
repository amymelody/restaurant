package restaurant;

import java.util.*;

public class Menu {
	private List<String> items = new ArrayList<String>();
	
	Menu() {}
	
	public void addItem(String item) {
		items.add(item);
	}
	
	public void removeItem(String item) {
		items.remove(item);
	}
	
	public boolean checkItem(String item) {
		return items.contains(item);
	}
	
	public String randomItem() {
		int rand = (int)(Math.random() * items.size());
		return items.get(rand);
	}
}
