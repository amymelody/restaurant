package restaurant.test.mock;


import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;

/**
 * Customer for purposes of unit testing
 */
public class MockCustomer extends Mock implements Customer {

	public Cashier cashier;
	private int charge;

	public MockCustomer(String name) {
		super(name);

	}
	
	public int getCharge() {
		return charge;
	}
	
	public void setCharge(int c) {
		charge = c;
	}
	
	@Override
	public void msgChange(int change) {
		log.add(new LoggedEvent("Received msgChange from cashier. Change = $" + change));
	}
	
	public String toString() {
		return "customer " + getName();
	}
}
