package restaurant.test.mock;

import restaurant.CashierAgent;
import restaurant.interfaces.Market;

/**
 * Market for purposes of unit testing
 */
public class MockMarket extends Mock implements Market {
	
	public CashierAgent cashier;

	public MockMarket(String name) {
		super(name);
	}
	
	@Override
	public void msgPayment(int cash) {
		log.add(new LoggedEvent("Received msgPayment from cashier. Cash = $" + cash));
	}
	
	public String toString() {
		return getName();
	}
	
}
