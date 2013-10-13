# v2.1 Design Document

### Host Agent
#### Data
	List<MyCustomer> customers;
	List<MyWaiter> waiters;
	List<Table> tables;
	List<String> foods;
	enum WaiterState {OnTheJob, WantToGoOnBreak, AboutToGoOnBreak, OnBreak};
	class Table {
		boolean occupied;
		int tableNumber;
	}
	class MyWaiter {
		Waiter w;
		WaiterState s;
	}
	class MyCustomer {
		Customer c;
		boolean waiting;
	}

#### Messages
	WantToGoOnBreak(Waiter w) {
		if there exists mw in waiters such that mw.w = w
			then mw.s = WantToGoOnBreak;
	}
	GoingOnBreak(Waiter w) {
		if there exists mw in waiters such that mw.w = w
			then mw.s = OnBreak;
	}
	GoingOffBreak(Waiter w) {
		if there exists mw in waiters such that mw.w = w
			then mw.s = OnTheJob;
	}
	IWantFood(Customer c) {
		customers.add(new MyCustomer(c));
	}
	TableAvailable(int tableNum) {
		if there exists t in tables such that t.tableNumber = tableNum
			then t.setUnoccupied();
	}
	ReceivedOrder(String food) {
		foods.add(food);
	}
	ImLeaving(Customer c) {
		if there exists mc in customers such that mc.c = c
			customers.remove(mc);
	}

#### Scheduler
	if there exists mc in customers such that mc.waiting = true && restaurantFull() //stub
		tellCustomer(mc);	
	if there exists t in tables such that t.isUnoccupied() & if !customers.isEmpty() & !waiters.isEmpty()
		then callWaiter(waiters.selectWaiter() //load-balancing stub, customers.get(0), table);
	if there exists mw in waiters such that mw.s = WantToGoOnBreak
		if waiters.size > 1 && noWaitersOnBreak() //stub
			then canGoOnBreak(mw);
		else cantGoOnBreak(mw);
	if there exists f in foods
		notifyWaiters(f);
		foods.remove(f);
		
#### Actions
	callWaiter(Waiter w, MyCustomer mc, Table t) {
		w.PleaseSeatCustomer(mc.c, t.tableNumber);
		if there exists table in tables such that table = t
			t.setOccupied();
		customers.remove(mc);
	canGoOnBreak(MyWaiter mw) {
		mw.w.CanGoOnBreak();
		mw.s = AboutToGoOnBreak;
	cantGoOnBreak(MyWaiter mw) {
		mw.w.CantGoOnBreak();
		mw.s = OnTheJob;
	notifyWaiters(String food) {
		for each w in waiters
			w.FoodArrived(food);
	}
	tellCustomer(MyCustomer mc) {
		mc.c.RestaurantIsFull();
		mc.waiting = false;
	}

### Waiter Agent
#### Data
	List<MyCustomer> customers;
	Host host;
	Cook cook;
	Cashier cashier;
	Menu menu;
	enum CustomerState {DoingNothing, Waiting, Seated, AskedToOrder, Asked, Ordered, MustReorder, WaitingForFood, OrderDone, ReadyToEat, Eating, WaitingForCheck, Leaving};
	enum WaiterState {OnTheJob, WantToGoOnBreak, AboutToGoOnBreak, OnBreak, GoingOffBreak};
	WaiterState state = OnTheJob;
	class MyCustomer {
		Customer c;
		int table;
		int charge;
		CustomerState s;
		String choice;
	}

#### Messages
	WantToGoOnBreak() {
		state = WantToGoOnBreak;
	}
	CanGoOnBreak() {
		state = AboutToGoOnBreak;
	}
	CantGoOnBreak() {
		state = OnTheJob;
	}
	GoOffBreak() {
		state = GoingOffBreak;
	}
	PleaseSeatCustomer(Customer c, int tableNumber) {
		if there exists mc in customers such that mc.c = c
			mc.s = Waiting;
			mc.table = tableNumber;
	}
	IWantToLeave(Customer c) {
		if there exists mc in customers such that mc.c = c
			mc.s = Leaving;
	}
	ReadyToOrder(Customer c) {
		if there exists mc in customers such that mc.c = c
			mc.s = AskedToOrder;
	}
	HereIsChoice(Customer c, String choice) {
		if there exists mc in customers such that mc.c = c
			mc.s = Ordered;
			mc.choice = choice;
	}
	OutOfFood(String choice, int table) {
		menu.removeItem(choice);
		if there exists mc in customers such that mc.table = table
			mc.s = MustReorder;
	}
	OrderDone(String choice, int tableNum) {
		if there exists mc in customers such that mc.table = tableNum & mc.choice = choice
			mc.s = OrderDone;
	}
	DoneEating(Customer c) {
		if there exists mc in customers such that mc.c = c
			mc.s = WaitingForCheck;
	}
	FoodArrived(String food) {
		if !menu.checkItem(food)
			menu.addItem(food);
	}
	HereIsCheck(Customer cust, int charge) {
		if there exists mc in customers such that mc.c = cust
			mc.charge = charge;
	}

#### Scheduler
	if state = WantToGoOnBreak
		wantToGoOnBreak();
	if state = AboutToGoOnBreak & doneServingCustomers() //stub
		goOnBreak();
	if state = GoingOffBreak
		goOffBreak();
	if there exists mc in customers such that mc.s = ReadyToEat
		deliverFood(mc);
	if there exists mc in customers such that mc.s = OrderDone
		retrieveOrder(mc);
	if there exists mc in customers such that mc.s = Waiting
		seatCustomer(mc);
	if there exists mc in customers such that mc.s = AskedToOrder
		takeOrder(mc);
	if there exists mc in customers such that mc.s = Ordered
		giveOrderToCook(mc);
	if there exists mc in customers such that mc.s = MustReorder
		askToReorder(mc);
	if there exists mc in customers such that mc.s = WaitingForCheck
		giveCheckToCustomer(mc);
	if there exists mc in customers such that mc.s = Leaving
		notifyHost(mc);

#### Actions
	wantToGoOnBreak() {
		host.WantToGoOnBreak(this);
		state = OnTheJob;
	}
	goOnBreak() {
		host.GoingOnBreak(this);
		state = OnBreak;
		DoReturnHome();
	}
	goOffBreak() {
		host.GoingOffBreak(this);
		state = OnTheJob;
	}
	seatCustomer(MyCustomer mc) {
		DoReturnHome();
		mc.c.FollowMe(this, menu, mc.table);
		DoSeatCustomer();
		mc.s = Seated;
		DoReturnHome();
	}
	takeOrder(MyCustomer mc) {
		DoGoToTable(mc.table);
		mc.c.WhatWouldYouLike();
		mc.s = Asked;
	}
	askToReorder(MyCustomer mc) {
		DoGoToTable(mc.table);
		mc.c.WantSomethingElse(menu);
		mc.s = Asked;
	}
	giveOrderToCook(MyCustomer mc) {
		mc.s = WaitingForFood;
		DoGoToCook();
		cook.HereIsOrder(this, mc.choice, mc.table);
		DoReturnHome();
	}
	retrieveOrder(MyCustomer mc) {
		DoGoToCook();
		DoDeliverFood(mc.choice);
		mc.state = ReadyToEat;
	}
	deliverFood(MyCustomer mc) {
		DoGoToTable(mc.table);
		mc.c.HereIsFood(mc.choice);
		cashier.ProduceCheck(mc.c, mc.choice);
		mc.s = Eating;
		DoReturnHome();
	}
	notifyHost(MyCustomer mc) {
		host.TableAvailable(mc.c, mc.table);
		DoReturnHome();
		mc.s = DoingNothing;
	}
	giveCheckToCustomer(MyCustomer mc) {
		DoGoToTable(mc.table);
		mc.c.HereIsCheck(mc.charge);
		mc.s = Leaving;
		DoReturnHome();
	}

### Customer Agent
#### Data
	Menu menu;
	String choice;
	int tableNumber;
	int cash;
	int charge;
	Cashier cashier;
	Host host;
	Waiter waiter;
	enum AgentState {DoingNothing, WaitingInRestaurant, BeingSeated, Seated, WantToLeave, ReadyToOrder, Ordered, Eating, WaitingForCheck, Paying, Leaving};
	enum AgentEvent {none, gotHungry, gotImpatient, followWaiter, seated, looksAtMenuAndCries, toldWaiter, madeChoice, order, receivedFood, doneEating, receivedCheck, receivedChange, doneLeaving};
	AgentState state = DoingNothing;
	AgentEvent event = none;
	Timer timer;

#### Messages
	GotHungry() {
		event = gotHungry;
	}
	RestaurantIsFull() {
		if name = "impatient"
			event = gotImpatient;
	}
	FollowMe(Waiter w, Menu m, int tableNum) {
		waiter = w;
		menu = m;
		tableNumber = tableNum;
		event = followWaiter;
	}
	AnimationFinishedGoToSeat() {
		if (cash < menu.lowestPrice() //stub)
			event = looksAtMenuAndCries;
		else 
			event = seated;
	}
	WhatWouldYouLike() {
		event = order;
	}
	WantSomethingElse(Menu m) {
		menu = m;
		if (cash < menu.lowestPrice() //stub)
			state = BeingSeated;
			event = looksAtMenuAndCries;
		else
			state = ReadyToOrder;
			event = order;
	}
	HereIsFood(String c) {
		if choice = c
			event = receivedFood;
	}
	AnimationFinishedLeaveRestaurant() {
		event = doneLeaving;
	}
	MadeChoice() {
		event = madeChoice;
	}
	DoneEating() {
		event = doneEating;
	}
	HereIsCheck(int c) {
		charge += c;
		event = receivedCheck;
	}
	Change(int change) {
		if change < 0
			charge = -change;
		else
			cash += change;
			charge = 0;
		event = receivedChange;
	}

#### Scheduler
	if state = DoingNothing & event = gotHungry
		state = WaitingInRestaurant;
		goToRestaurant();
	if state = WaitingInRestaurant & event = followWaiter
		state = BeingSeated;
		SitDown();
	if state = WaitingInRestaurant & event = gotImpatient
		state = Leaving;
		leaveAndNotifyHost();
	if state = BeingSeated & event = looksAtMenuAndCries
		state = WantToLeave;
		tellWaiter();
	if state = WantToLeave & event = toldWaiter
		state = Leaving;
		leaveRestaurant();
	if state = BeingSeated & event = seated
		state = Seated;
		timer.schedule(MadeChoice(), choiceTime() //stub);
	if state = Seated & event = madeChoice
		state = ReadyToOrder;
		callWaiter();
	if state = ReadyToOrder & event = order
		state = Ordered;
		giveOrder();
	if state = Ordered & event = receivedFood
		state = Eating;
		EatFood();
	if state = Eating & event = doneEating
		state = WaitingForCheck;
		askForCheck();
	if state = WaitingForCheck & event = receivedCheck
		state = Paying;
		leaveTable();
	if state = Paying & event = receivedChange
		state = Leaving;
		leaveRestaurant();
	if state = Leaving && event = doneLeaving
		state = DoingNothing;

#### Actions
	goToRestaurant() {
		host.IWantFood(this);
	}
	leaveAndNotifyHost() {
		DoExitRestaurant();
		host.ImLeaving(this);
	}
	SitDown() {
		DoGoToSeat(tableNumber);
	}
	tellWaiter() {
		waiter.IWantToLeave(this);
		event = toldWaiter;
	}
	leaveRestaurant() {
		DoExitRestaurant();
	}
	callWaiter() {
		waiter.ReadyToOrder(this);
	}
	giveOrder() {
		do
			choice = menu.randomItem(); //stub
		while (menu.getPrice(choice) > cash)
		waiter.HereIsChoice(this, choice);
	}
	EatFood() {
		timer.schedule(DoneEating(), eatingTime() //stub);
	}
	askForCheck() {
		waiter.DoneEating(this);
	}
	leaveTable() {
		DoGoToCashier();
		int payment = charge + 10 - charge % 10; //customer pays charge rounded up to next tens place
		if cash < payment
			payment = cash; 
		cashier.Payment(this, payment);
		cash -= payment;
	}

### Cook Agent
#### Data
	List<MyMarket> markets;
	List<Order> orders;
	List<ItemOrder> itemOrders;
	Timer timer;
	Host host;
	boolean orderedItems = false;
	Map<String, Food> foods;
	enum OrderState {Pending, Cooking, Done, Finished};
	enum FoodState {Enough, MustBeOrdered, Ordered, ReceivedOrder};
	class Order {
		Waiter w;
		int table;
		OrderState s;
		String choice;
	}
	class Food {
		String type;
		int cookingTime;
		int amount;
		int low;
		int capacity;
		FoodState s;
	}
	class MyMarket {
		Market m;
		int orderedFrom;
	}

#### Messages
	HereIsOrder(Waiter w, String choice, int table) {
		orders.add(new Order(w, choice, table, Pending));
	}
	OrderDone(Order o) {
		o.s = Done;
	}
	CantFulfillOrder(List<ItemOrder> orders) {
		for every o in orders
			foods.get(o.food).s = MustBeOrdered;
	}
	OrderDelivered(List<ItemOrder> orders) {
		for every o in orders
			foods.get(o.food).amount += io.amount;
			foods.get(o.food).s = ReceivedOrder;
	}

#### Scheduler
	if !orderedItems
		orderFoodFromMarket();
		orderedItems = true;
	if there exists f in foods such that f.s = ReceievedOrder
		addFood(f);
	if there exists f in foods such that f.s = MustBeOrdered
		orderFoodFromMarket();
	if there exists o in orders such that o.s = Done
		plateIt(o);
	if there exists o in orders such that o.s = Pending
		cookIt(o);

#### Actions
	cookIt(Order o) {
		if foods.get(o.choice).amount = 0
			o.w.OutOfFood(o.choice, o.table);
			o.s = Finished;
		else
			o.s = Cooking;
			timer.schedule(OrderDone(o), foods.get(o.choice).cookingTime));
			foods.get(o.choice).amount--;
			if foods.get(o.choice).amount <= foods.get(o.choice).low && foods.get(o.choice).s = Enough
				foods.get(o.choice).s = MustBeOrdered;
	}
	plateIt(Order o) {
		o.w.OrderDone(o.choice, o.table);
		o.s = Finished;
	}
	orderFoodFromMarket() {
		for every f in foods such that (f.s = Enough || f.s = MustBeOrdered) & f.amount <= f.low
			itemOrders.add(new ItemOrder(f.type, f.capacity - f.amount));
			f.s = Ordered;
		markets.selectMarket().m.HereIsOrder(itemOrders); //selectMarket() is a load-balancing stub using MyMarket's orderedFrom
		markets.selectMarket().orderedFrom++;
		itemOrders.clear();
	}
	addFood(Food f) {
		f.s = Enough;
		host.ReceivedOrder(f.type);
	}

### Market Agent
#### Data
	Cook cook;
	Timer timer;
	List<Order> orders;
	Map<String, Food> foods;
	enum OrderState {Received, ProducingOrder, Ready, Finished};
	class Order {
		List<ItemOrder> items;
		boolean canFulfillOrder;
		OrderState s;
	}
	class Food {
		String type;
		int amount;
		int timeToProduce;
	}

#### Messages
	HereIsOrder(List<ItemOrder> io) {
		orders.add(new Order(io, canFulfillOrder(io) //stub, Received));
	}
	OrderReady(Order o) {
		o.s = Ready;
	}

#### Scheduler
	if there exists o in orders such that o.s = Ready
		deliverOrder(o);
	if there exists o in orders such that o.s = Received and !o.canFulfillOrder
		respondToCook(o);
	if there exists o in orders such that o.s = Received and o.canFulfillOrder
		produceOrder(o);

#### Actions
	respondToCook(Order o) {
		cook.CantFulfillOrder(o.items);
		o.s = Finished;
	}
	produceOrder(Order o) {
		o.s = ProducingOrder;
		timer.schedule(OrderReady(o), timeToProduceOrder(o) //stub);
		for every io in o.items	
			foods.get(io.food).amount -= io.amount;
	}
	deliverOrder(Order o) {
		cook.OrderDelivered(o.items);
		o.s = Finished;
	}

### Cashier Agent
#### Data
	Map<String, int> prices;
	List<Check> checks;
	enum CheckState {Created, GivenToWaiter, Paid, Done};
	class Check {
		Customer cust;
		String choice;
		int charge;
		int payment;
		CheckState s;
	}

#### Messages
	ProduceCheck(Customer c, String choice) {
		checks.add(new Check(c, choice, prices.get(choice), Created);
	}
	Payment(Customer c, int cash) {
		if there exists c in checks such that c.cust = c and c.s = GivenToWaiter
			c.payment = cash;
			c.s = Paid;
	}

#### Scheduler
	if there exists c in checks such that c.s = Created
		giveToWaiter(c);
	if there exists c in checks such that c.s = Paid
		giveCustomerChange(c);

#### Actions
	giveToWaiter(Check c) {
		c.s = GivenToWaiter;
		c.cust.waiter.HereIsCheck(c.cust, c.charge);
	}
	giveCustomerChange(Check c) {
		c.s = Done;
		c.cust.Change(c.payment - c.charge);
	}
