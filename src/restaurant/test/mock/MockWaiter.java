package restaurant.test.mock;


import restaurant.interfaces.Cashier;
import restaurant.interfaces.Waiter;
import restaurant.interfaces.Customer;

public class MockWaiter extends Mock implements Waiter {

	/**
	 * Reference to the Cashier under test that can be set by the unit test.
	 */
	public Cashier cashier;
	private EventLog log;

	public MockWaiter(String name) {
		super(name);

	}
	
	@Override
	public void msgHereIsCheck(Customer c, int charge) {
		
	}
	
}
