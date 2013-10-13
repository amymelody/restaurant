package restaurant;

import restaurant.gui.CustomerGui;
import restaurant.gui.RestaurantGui;
import agent.Agent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

/**
 * Restaurant customer agent.
 */
public class CustomerAgent extends Agent {
	private String name;
	private int hungerLevel = 1;
	Timer timer = new Timer();
	private CustomerGui customerGui;
	private Menu menu;
	private String choice;
	private Semaphore doneOrdering = new Semaphore(0,true);
	private Semaphore atCashier = new Semaphore(0,true);
	
	public int tableNumber;
	private int cash;
	private int charge;
	
	// agent correspondents
	private HostAgent host;
	private WaiterAgent waiter;
	private CashierAgent cashier;

	//    private boolean isHungry = false; //hack for gui
	public enum AgentState
	{DoingNothing, WaitingInRestaurant, BeingSeated, Seated, WantToLeave, ReadyToOrder, Ordered, Eating, WaitingForCheck, Paying, Leaving};
	private AgentState state = AgentState.DoingNothing;//The start state

	public enum AgentEvent 
	{none, gotHungry, gotImpatient, followWaiter, seated, looksAtMenuAndCries, toldWaiter, madeChoice, order, receivedFood, doneEating, receivedCheck, receivedChange, doneLeaving};
	AgentEvent event = AgentEvent.none;

	/**
	 * Constructor for CustomerAgent class
	 *
	 * @param name name of the customer
	 * @param gui  reference to the customergui so the customer can send it messages
	 */
	public CustomerAgent(String name){
		super();
		this.name = name;
		
		cash = 30;
		if (name.equals("cheapskate") || name.equals("poor")) {
			cash = 5;
		}
		if (name.equals("enoughforsalad")) {
			cash = 7;
		}
		charge = 0;
	}

	/**
	 * hack to establish connection to Host agent.
	 */
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	public void setCashier(CashierAgent cashier) {
		this.cashier = cashier;
	}

	public String getCustomerName() {
		return name;
	}
	
	public String getChoice() {
		return choice;
	}
	
	public int getCharge() {
		return charge;
	}
	
	public boolean isEating() {
		if (state == AgentState.Eating) {
			return true;
		}
		return false;
	}
	
	public WaiterAgent getWaiter() {
		return waiter;
	}
	
	// Messages

	public void gotHungry() {//from animation
		print("I'm hungry");
		event = AgentEvent.gotHungry;
		stateChanged();
	}
	
	public void msgRestaurantIsFull() {
		if (name.equals("impatient")) {
			event = AgentEvent.gotImpatient;
		} else {
			Do("I'll wait for a table to open");
		}
		stateChanged();
	}

	public void msgFollowMe(WaiterAgent w, Menu m, int tableNumber) { //Menu will be added later
		waiter = w;
		menu = m;
		this.tableNumber = tableNumber;
		print("Received msgSitAtTable");
		event = AgentEvent.followWaiter;
		stateChanged();
	}

	public void msgAnimationFinishedGoToSeat() {
		//from animation
		if (name != "cheapskate" && cash < menu.lowestPrice()) {
			event = AgentEvent.looksAtMenuAndCries;
		}
		else {
			event = AgentEvent.seated;
		}
		stateChanged();
	}
	
	public void msgWhatWouldYouLike() {
		event = AgentEvent.order;
		stateChanged();
	}
	
	public void msgWantSomethingElse(Menu menu) {
		this.menu = menu;
		if (name != "cheapskate" && cash < menu.lowestPrice()) {
			state = AgentState.BeingSeated;
			event = AgentEvent.looksAtMenuAndCries;
		} else {
			state = AgentState.ReadyToOrder;
			event = AgentEvent.order;
		}
		stateChanged();
	}
	
	public void msgHereIsFood(String choice) {
		if (this.choice == choice) {
			event = AgentEvent.receivedFood;
			stateChanged();
		}
	}
	
	public void msgAnimationFinishedLeaveRestaurant() {
		//from animation
		event = AgentEvent.doneLeaving;
		stateChanged();
	}
	
	public void  msgHereIsCheck(int c) {
		charge += c;
		event = AgentEvent.receivedCheck;
		stateChanged();
	}
	
	public void msgChange(int change) {
		if (change < 0) {
			charge = -change;
			cash += 30;
		}
		else {
			cash += change;
			charge = 0;
		}
		event = AgentEvent.receivedChange;
		stateChanged();
	}
	
	public void msgDoneOrdering() {//from animation
		doneOrdering.release();// = true;
		stateChanged();
	}
	
	public void msgAtCashier() {//from animation
		atCashier.release();// = true;
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		//	CustomerAgent is a finite state machine

		if (state == AgentState.DoingNothing && event == AgentEvent.gotHungry){
			state = AgentState.WaitingInRestaurant;
			goToRestaurant();
			return true;
		}
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.followWaiter){
			state = AgentState.BeingSeated;
			SitDown();
			return true;
		}
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.gotImpatient){
			state = AgentState.Leaving;
			leaveAndNotifyHost();
			return true;
		}
		if (state == AgentState.BeingSeated && event == AgentEvent.looksAtMenuAndCries){
			state = AgentState.WantToLeave;
			tellWaiter();
			return true;
		}
		if (state == AgentState.WantToLeave && event == AgentEvent.toldWaiter){
			state = AgentState.Leaving;
			leaveRestaurant();
			return true;
		}
		if (state == AgentState.BeingSeated && event == AgentEvent.seated){
			state = AgentState.Seated;
			timer.schedule(new TimerTask() {
				public void run() {
					event = AgentEvent.madeChoice;
					stateChanged();
				}
			},
			getHungerLevel() * 500);//how long to wait before running task
			return true;
		}
		if (state == AgentState.Seated && event == AgentEvent.madeChoice){
			state = AgentState.ReadyToOrder;
			callWaiter();
			return true;
		}
		if (state == AgentState.ReadyToOrder && event == AgentEvent.order){
			state = AgentState.Ordered;
			giveOrder();
			return true;
		}
		if (state == AgentState.Ordered && event == AgentEvent.receivedFood){
			state = AgentState.Eating;
			EatFood();
			return true;
		}
		if (state == AgentState.Eating && event == AgentEvent.doneEating){
			state = AgentState.WaitingForCheck;
			askForCheck();
			return true;
		}
		if (state == AgentState.WaitingForCheck && event == AgentEvent.receivedCheck){
			state = AgentState.Paying;
			leaveTable();
			return true;
		}
		if (state == AgentState.Paying && event == AgentEvent.receivedChange){
			state = AgentState.Leaving;
			leaveRestaurant();
			return true;
		}
		if (state == AgentState.Leaving && event == AgentEvent.doneLeaving){
			state = AgentState.DoingNothing;
			//no action
			return true;
		}
		return false;
	}

	// Actions

	private void goToRestaurant() {
		Do("Going to restaurant");
		host.msgIWantFood(this);
	}
	
	private void leaveAndNotifyHost() {
		Do("I don't want to wait. Leaving restaurant");
		customerGui.DoExitRestaurant();
		host.msgImLeaving(this);
	}

	private void SitDown() {
		Do("Being seated. Going to table");
		customerGui.DoGoToSeat(tableNumber);
	}
	
	private void tellWaiter() {
		Do("This food is too expensive. I'm leaving.");
		waiter.msgIWantToLeave(this);
		event = AgentEvent.toldWaiter;
	}
	
	private void leaveRestaurant() {
		Do("Leaving restaurant");
		customerGui.DoExitRestaurant();
	}
	
	private void callWaiter() {
		Do("I'm ready to order.");
		waiter.msgReadyToOrder(this);
	}
	
	private void giveOrder() {
		if (menu.checkItem(name)) {
			choice = name;
		} else {
			do {
				choice = menu.randomItem();
			} while (menu.getPrice(choice) > cash);
		}
		print("I would like to order " + choice);
		customerGui.order();
		try {
			doneOrdering.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		waiter.msgHereIsChoice(this, choice);
	}

	private void EatFood() {
		Do("Eating Food");
		timer.schedule(new TimerTask() {
			public void run() {
				print("Done eating " + choice);
				event = AgentEvent.doneEating;
				stateChanged();
			}
		},
		getHungerLevel() * 1000);//how long to wait before running task
	}
	
	private void askForCheck() {
		Do("Check please.");
		waiter.msgDoneEating(this);
	}

	private void leaveTable() {
		Do("Going to cashier");
		customerGui.DoGoToCashier();
		try {
			atCashier.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int payment = charge + 10 - charge % 10;
		if (cash < payment) {
			payment = cash;
		}
		Do("Paying $" + payment);
		cashier.msgPayment(this, payment);
		cash -= payment;
	}

	// Accessors, etc.

	public String getName() {
		return name;
	}
	
	public int getHungerLevel() {
		return hungerLevel;
	}

	public void setHungerLevel(int hungerLevel) {
		this.hungerLevel = hungerLevel;
		//could be a state change. Maybe you don't
		//need to eat until hunger lever is > 5?
	}

	public String toString() {
		return "customer " + getName();
	}

	public void setGui(CustomerGui g) {
		customerGui = g;
	}

	public CustomerGui getGui() {
		return customerGui;
	}
}

