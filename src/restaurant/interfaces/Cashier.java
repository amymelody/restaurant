package restaurant.interfaces;

import restaurant.CashierAgent.Check;
import restaurant.CashierAgent.CheckState;

public interface Cashier {
	
	public abstract void msgProduceCheck(Waiter w, Customer c, String choice);
	
	public abstract void msgPayment(Customer c, int cash);
	
	public abstract void msgHereIsBill(int bill);

}