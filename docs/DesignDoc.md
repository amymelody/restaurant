# v2.1 Design Document

### Host Agent
#### Data
	List<Customer> customers;
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
		customers.add(c);
	}
	TableAvailable(int tableNum) {
		if there exists t in tables such that t.tableNumber = tableNum
			then t.setUnoccupied();
	}
	ReceivedOrder(String food) {
		foods.add(food);
	}

#### Scheduler
	if there exists t in tables such that t.isUnoccupied() & if !customers.isEmpty() & !waiters.isEmpty()
		then callWaiter(waiters.selectWaiter() //stub, customers.get(0), table);
	if there exists mw in waiters such that mw.s = WantToGoOnBreak
		if waiters.size > 1 && noWaitersOnBreak() //stub
			then canGoOnBreak(mw);
		else cantGoOnBreak(mw);
	if there exists f in foods
		notifyWaiters(f);
		foods.remove(f);
		
#### Actions
	callWaiter(Waiter w, Customer c, Table t) {
		w.PleaseSeatCustomer(c, t.tableNumber);
		if there exists table in tables such that table = t
			t.setOccupied();
		customers.remove(c);
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
		host.TableAvailable(mc.table);
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
	enum AgentEvent {none, gotHungry, followWaiter, seated, looksAtMenuAndCries, toldWaiter, madeChoice, order, receivedFood, doneEating, receivedCheck, receivedChange, doneLeaving};
	AgentState state = DoingNothing;
	AgentEvent event = none;
	Timer timer;

#### Messages
	GotHungry() {
		event = gotHungry;
	}
	FollowMe(Waiter w, Menu m, int tableNum) {
		waiter = w;
		menu = m;
		tableNumber = tableNum;
		event = followWaiter;
	}
	AnimationFinishedGoToSeat() {
		if (cash < 6)
			event = looksAtMenuAndCries;
		else 
			event = seated;
	}
	WhatWouldYouLike() {
		event = order;
	}
	WantSomethingElse(Menu m) {
		menu = m;
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
	if state = Leaving && event = doneLeaving
		state = DoingNothing;

#### Actions
	goToRestaurant() {
		host.IWantFood(this);
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
		choice = menu.randomItem(); //stub
		waiter.HereIsChoice(this, choice);
	}
	EatFood() {
		timer.schedule(DoneEating(), eatingTime() //stub);
	}
	askForCheck() {
		waiter.DoneEating(this);
	}
	leaveTable() {
		int payment = charge + 10 - charge % 10; //customer pays charge rounded up to next tens place
		if cash < payment
			payment = cash; 
		cashier.Payment(this, payment);
		cash -= payment;
		DoExitRestaurant();
	}

### Cook Agent
#### Data
	List<Market> markets;
	List<Order> orders;
	Timer timer;
	Host host;
	Map<String, Food> foods;
	enum OrderState {Pending, Cooking, Done, Finished};
	enum FoodState {Good, MustBeOrdered, Inquired, Ordered, WaitingForOrder, ReceivedOrderS};
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
		FoodState s = Good;
		Market orderedFrom;
	}

#### Messages
	HereIsOrder(Waiter w, String choice, int table) {
		orders.add(new Order(w, choice, table, Pending));
	}
	OrderDone(Order o) {
		o.s = Done;
	}
	CanFulfillOrder(String food, Market m) {
		if there exists market in markets such that market = m
			if foods.get(food).s = Inquired
				foods.get(food).s = Ordered;
				foods.get(food).orderedFrom = m;
	}
	CantFulfillOrder(String food, Market m) {
		if there exists market in markets such that market = m
			if foods.get(food).s = Inquired & market.lastMarket() //stub; if no one can fulfill the order then take order anyway
				foods.get(food).s = Ordered;
				foods.get(food).orderedFrom = m;
	}
	OrderDelivered(Market m, String food, int amount) {
		if there exists market in markets such that market = m
			if foods.get(food).s = WaitingForOrder
				foods.get(food).amount += amount;
				foods.get(food).s = ReceivedOrder;
	}

#### Scheduler
	if there exists f in foods such that f.s = ReceievedOrder
		addFood(f);
	if there exists f in foods such that f.s = Ordered
		respondToMarkets(f);
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
			if foods.get(o.choice).amount <= foods.get(o.choice).low && foods.get(o.choice).s = Good
				foods.get(o.choice).s = MustBeOrdered;
	}
	plateIt(Order o) {
		o.w.OrderDone(o.choice, o.table);
		o.s = Finished;
	}
	orderFoodFromMarket() {
		for each f in foods
			for each m in markets
				m.HereIsOrder(f.type, f.capacity - f.amount);
			f.s = Inquired;
	}
	respondToMarkets(Food f) {
		f.orderedFrom.IWouldLikeToOrder(f.type);
		f.s = WaitingForOrder;
		for each m in markets such that m != f.orderedFrom
			m.IWouldNotLikeToOrder(f.type);
	}
	addFood(Food f) {
		f.s = Good;
		host.ReceivedOrder(f.type);
	}

### Market Agent
#### Data
	Cook cook;
	Timer timer;
	List<Order> orders;
	Map<String, Food> foods;
	enum OrderState {Received, Waiting, Pending, ProducingOrder, Ready, Finished};
	class Order {
		String food;
		int amount;
		boolean canFulfillOrder;
		OrderState s;
	}
	class Food {
		String type;
		int amount;
		int timeToProduce;
	}

#### Messages
	HereIsOrder(String food, int amount) {
		boolean canFulfillOrder;
		if foods.get(food).amount >= amount
			canFulfillOrder = true;
		else canFulfillOrder = false;
		orders.add(new Order(food, amount, canFulfillOrder, Received));
	}
	IWouldLikeToOrder(String food) {
		if there exists o in orders such that o.s = Waiting & o.food = food
			o.s = Pending;
	}
	IWouldNotLikeToOrder(String food) {
		if there exists o in orders such that o.s = Waiting and o.food = food
			orders.remove(o);
	}
	OrderReady(Order o) {
		o.s = Ready;
	}

#### Scheduler
	if there exists o in orders such that o.s = Ready
		deliverOrder(o);
	if there exists o in orders such that o.s = Received
		respondToCook(o);
	if there exists o in orders such that o.s = Pending
		produceOrder(o);

#### Actions
	respondToCook(Order o) {
		if o.canFulfillOrder = true
			cook.CanFulfillOrder(o.food, this);
		else cook.CantFulfillOrder(o.food, this);
		o.s = Waiting;
	}
	produceOrder(Order o) {
		o.s = ProducingOrder;
		if o.amount > foods.get(o.food).amount
			o.amount = foods.get(o.food).amount;
		timer.schedule(OrderReady(o), foods.get(o.food).timeToProduce*o.amount));
		foods.get(o.food).amount -= o.amount;
	}
	deliverOrder(Order o) {
		cook.OrderDelivered(this, o.food, o.amount);
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
