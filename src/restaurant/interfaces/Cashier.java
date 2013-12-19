package restaurant.interfaces;

/**
 * Cashier Interface
 */
public interface Cashier {
	
	public abstract void msgProduceCheck(Waiter w, Customer c, String choice);
	
	public abstract void msgPayment(Customer c, int cash);
	
	public abstract void msgHereIsBill(int bill, Market m);

}