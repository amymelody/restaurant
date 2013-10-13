package restaurant;

import java.util.*;

public class ItemOrder {
	private String food;
	private int amount;
	
	ItemOrder(String f, int a) {
		food = f;
		amount = a;
	}
	
	public String getFood() {
		return food;
	}
	
	public int getAmount() {
		return amount;
	}
}
