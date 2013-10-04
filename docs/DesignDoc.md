# v2.1 Design Document

### Host Agent
#### Data
	List<Customer> customers;

	List<MyWaiter> waiters;

	List<Table> tables;

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

#### Scheduler
	if there exists t in tables such that t.isUnoccupied() & if !customers.isEmpty() & !waiters.isEmpty()

		then callWaiter(waiters.selectWaiter() //stub, customers.get(0), table);

	if there exists mw in waiters such that mw.s = WantToGoOnBreak

		if waiters.size > 1 && noWaitersOnBreak() //stub

			then canGoOnBreak(mw);

		else cantGoOnBreak(mw);
		
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


### Waiter Agent
#### Data
	List<MyCustomer> customers;
	
	Host host;

	Cook cook;

	Menu menu;

	enum CustomerState {DoingNothing, Waiting, Seated, AskedToOrder, Asked, Ordered, MustReorder, WaitingForFood, OrderDone, ReadyToEat, Eating, Leaving};

	enum WaiterState {OnTheJob, WantToGoOnBreak, AboutToGoOnBreak, OnBreak, GoingOffBreak};

	WaiterState state = OnTheJob;

	class MyCustomer {

		Customer c;

		int table;

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

	ReadyToOrder(Customer c) {

		if there exists mc in customers such that mc.c = c

			mc.s = AskedToOrder;

	HereIsChoice(Customer c, String choice) {

		if there exists mc in customers such that mc.c = c

			mc.s = Ordered;

			mc.choice = choice;

	OutOfFood(String choice, int table) {

		menu.removeItem(choice);

		if there exists mc in customers such that mc.table = table

			mc.s = MustReorder;

	OrderDone(String choice, int tableNum) {

		if there exists mc in customers such that mc.table = tableNum & mc.choice = choice

			mc.s = OrderDone;

	DoneEating(Customer c) {

		if there exists mc in customer such that mc.c = c

			mc.s = Leaving;

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

		mc.s = Eating;

		DoReturnHome();

	}

	notifyHost(MyCustomer mc) {

		host.TableAvailable(mc.table);

		DoReturnHome();

		mc.s = DoingNothing;

	}

### Customer Agent
#### Data
#### Messages
#### Scheduler
#### Actions

### Cook Agent
#### Data
#### Messages
#### Scheduler
#### Actions

### Market Agent
#### Data
#### Messages
#### Scheduler
#### Actions

### Cashier Agent
#### Data
#### Messages
#### Scheduler
#### Actions