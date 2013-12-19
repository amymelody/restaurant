package restaurant.test.mock;


import restaurant.interfaces.Cashier;
import restaurant.interfaces.Waiter;
import restaurant.interfaces.Customer;

/**
 * Waiter for purposes of unit testing
 */
public class MockWaiter extends Mock implements Waiter {

	public Cashier cashier;

	public MockWaiter(String name) {
		super(name);

	}
	
	@Override
	public void msgHereIsCheck(Customer c, int charge) {
		log.add(new LoggedEvent("Received msgHereIsCheck from cashier. Customer = " + c.getName() + ". Charge = $" + charge));
	}
	
	public String toString() {
		return getName();
	}
	
}
