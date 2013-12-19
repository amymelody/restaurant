package restaurant.interfaces;

/**
 * Customer Interface
 */
public interface Customer {
	
	public abstract String getName();
	
	public abstract int getCharge();

	public abstract void msgChange(int change);

}