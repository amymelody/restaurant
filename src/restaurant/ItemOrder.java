package restaurant;

/**
 * Class that holds a String representing the type of food and an integer of the amount of that food
 */
public class ItemOrder {
	private String food;
	private int amount;
	
	/**
	 * Constructor
	 *
	 * @param f Name of food
	 * @param a Amount of food
	 */
	ItemOrder(String f, int a) {
		food = f;
		amount = a;
	}
	
	/**
	 * Returns the name of the ItemOrder's food
	 */
	public String getFood() {
		return food;
	}
	
	/**
	 * Returns the amount of food in the ItemOrder
	 */
	public int getAmount() {
		return amount;
	}
}
