package restaurant;

import agent.Agent;

import java.util.*;


/**
 * Restaurant Cashier Agent
 */

public class CashierAgent extends Agent {
	public List<Check> checks = new ArrayList<Check>();

	private String name;
	
	Map<String, Integer> prices = new HashMap<String, Integer>();
	
	public enum CheckState {Created, GivenToWaiter, Paid, Done};

	public CashierAgent(String name) {
		super();
		this.name = name;
		
		prices.put("steak", 16);
		prices.put("chicken", 11);
		prices.put("salad", 6);
		prices.put("pizza", 9);
	}
	

	public String getName() {
		return name;
	}
	
	// Messages
	
	public void msgProduceCheck(CustomerAgent c, String choice) {
		checks.add(new Check(c, choice, prices.get(choice)+c.getCharge(), CheckState.Created));
		stateChanged();
	}
	
	public void msgPayment(CustomerAgent c, int cash) {
		for (Check check : checks) {
			if (check.cust == c & check.state == CheckState.GivenToWaiter) {
				check.setPayment(cash);
				check.setState(CheckState.Paid);
				stateChanged();
			}
		}
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		for (Check check : checks) {
			if (check.state == CheckState.Created) {
				giveToWaiter(check);
				return true;
			}
		}
		for (Check check : checks) {
			if (check.state == CheckState.Paid) {
				giveCustomerChange(check);
				return true;
			}
		}

		return false;
	}

	// Actions

	private void giveToWaiter(Check c) {
		print(c.cust.getWaiter() + ", here is the check for " + c.cust);
		c.setState(CheckState.GivenToWaiter);
		c.cust.getWaiter().msgHereIsCheck(c.cust, c.charge);
	}
	
	private void giveCustomerChange(Check c) {
		int change = c.payment - c.charge;
		if (change >= 0) {
			print(c.cust + ", here is your change of $" + change);
		} else {
			print(c.cust + ", thank you for eating at our restaurant. Please pay $" + -change + " next time you rotten cheapskate.");
		}
		c.setState(CheckState.Done);
		c.cust.msgChange(change);
	}

	//utilities

	public class Check {
		CustomerAgent cust;
		String choice;
		int charge;
		int payment;
		CheckState state;
		
		Check(CustomerAgent c, String choice, int charge, CheckState s) {
			cust = c;
			this.choice = choice;
			this.charge = charge;
			payment = 0;
			state = s;
		}
		
		public void setPayment(int payment) {
			this.payment = payment;
		}
		
		public void setState(CheckState state) {
			this.state = state;
		}
	}
}

