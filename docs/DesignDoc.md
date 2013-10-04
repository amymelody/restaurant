# v2.1 Design Document

### Host Agent
#### Data
List(Customer) customers;

List(MyWaiter) waiters;

List(Table) tables;

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
		
    then w.s = WantToGoOnBreak

}


#### Scheduler
#### Actions

### Waiter Agent
#### Data
#### Messages
#### Scheduler
#### Actions

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