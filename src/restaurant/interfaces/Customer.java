package restaurant.interfaces;

import restaurant.test.mock.EventLog;

public interface Customer {
	
	public abstract String getName();
	
	public abstract int getCharge();

	public abstract void msgChange(int change);

}