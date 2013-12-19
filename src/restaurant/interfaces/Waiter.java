package restaurant.interfaces;

import restaurant.interfaces.Customer;

/**
 * Waiter Interface
 */
public interface Waiter {
	
	public abstract void msgHereIsCheck(Customer c, int charge);

}