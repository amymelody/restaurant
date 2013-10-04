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
	private int hungerLevel = 1;        // determines length of meal
	Timer timer = new Timer();
	private CustomerGui customerGui;
	private Menu menu;
	private String choice;
	private Semaphore doneOrdering = new Semaphore(0,true);
	
	public int tableNumber; //Number of the table at which the customer is being seated

	// agent correspondents
	private HostAgent host;
	private WaiterAgent waiter;

	//    private boolean isHungry = false; //hack for gui
	public enum AgentState
	{DoingNothing, WaitingInRestaurant, BeingSeated, Seated, ReadyToOrder, Ordered, Eating, Leaving};
	private AgentState state = AgentState.DoingNothing;//The start state

	public enum AgentEvent 
	{none, gotHungry, followWaiter, seated, madeChoice, order, receivedFood, doneEating, doneLeaving};
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
	}

	/**
	 * hack to establish connection to Host agent.
	 */
	public void setHost(HostAgent host) {
		this.host = host;
	}

	public String getCustomerName() {
		return name;
	}
	
	public String getChoice() {
		return choice;
	}
	
	public boolean isEating() {
		if (state == AgentState.Eating) {
			return true;
		}
		return false;
	}
	
	// Messages

	public void gotHungry() {//from animation
		print("I'm hungry");
		event = AgentEvent.gotHungry;
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
		event = AgentEvent.seated;
		stateChanged();
	}
	
	public void msgWhatWouldYouLike() {
		event = AgentEvent.order;
		stateChanged();
	}
	
	public void msgWantSomethingElse(Menu menu) {
		this.menu = menu;
		state = AgentState.ReadyToOrder;
		event = AgentEvent.order;
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
	
	public void msgDoneOrdering() {//from animation
		doneOrdering.release();// = true;
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		//	CustomerAgent is a finite state machine

		if (state == AgentState.DoingNothing && event == AgentEvent.gotHungry ){
			state = AgentState.WaitingInRestaurant;
			goToRestaurant();
			return true;
		}
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.followWaiter ){
			state = AgentState.BeingSeated;
			SitDown();
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
			state = AgentState.Leaving;
			leaveTable();
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
		host.msgIWantFood(this);//send our instance, so he can respond to us
	}

	private void SitDown() {
		Do("Being seated. Going to table");
		customerGui.DoGoToSeat(tableNumber);//hack; only one table
	}
	
	private void callWaiter() {
		Do("I'm ready to order.");
		waiter.msgReadyToOrder(this);
	}
	
	private void giveOrder() {
		//choice = menu.randomItem();
		if (menu.checkItem(name)) {
			choice = name;
		} else {
			choice = menu.randomItem();
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
		//This next complicated line creates and starts a timer thread.
		//We schedule a deadline of getHungerLevel()*1000 milliseconds.
		//When that time elapses, it will call back to the run routine
		//located in the anonymous class created right there inline:
		//TimerTask is an interface that we implement right there inline.
		//Since Java does not all us to pass functions, only objects.
		//So, we use Java syntactic mechanism to create an
		//anonymous inner class that has the public method run() in it.
		timer.schedule(new TimerTask() {
			//Object cookie = 1;
			public void run() {
				print("Done eating " + choice);
				event = AgentEvent.doneEating;
				//isHungry = false;
				stateChanged();
			}
		},
		getHungerLevel() * 1000);//how long to wait before running task
	}

	private void leaveTable() {
		Do("Leaving.");
		waiter.msgDoneEating(this);
		customerGui.DoExitRestaurant();
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

