##Restaurant Project Repository

###Student Information
  + Name: Josh DiGiovanni
  + USC Email: jmdigiov@usc.edu
  + USC ID: 4109364701
  + Lecture: MW 4 pm
  + Lab: Tues 9 am

###Using the GUI
  + Run restaurant.gui.RestaurantGui.java
  + Enter the name of either a customer or a waiter.
  + Press the Add button under "Cusomters" or "Waiters" to add that name to respective list.
  + Select a customer's name in the list to display their name at the top along with a "Hungry" checkbox.
  + Select a waiter's name in the list to display their name at the top along with a "Break" checkbox.
  + Once a waiter is added, it will appear in its home position. A customer will appear in the waiting area once its "Hungry" checkbox is checked.

###Running the Different Scenarios
  + The first market has 10 of each item except for steak (0), the second has 1 of each item, and the last has 2 of each item except for steak (10)
  + The "one order from two markets" scenario will happen as soon as the program is started. To test the normative market bill scenario, add a customer with the name "salad" and the cook will order salad once the customer orders it and the inventory gets low.
  + The cook starts out with 1 steak, 1 chicken, 2 salads, and 3 pizzas (capacity 3 and low 1 for each)
  + The cook orders steak and chicken from the first market right away, so to test the backup market scenario, create a customer named "salad" (the second market will be unable to fulfill the order of 2 salads).
  + To test the out of food scenario, create 2 waiters and 2 customers named "steak". By the time the second customer orders steak, the steak inventory will be 0 (as long as the market's order hasn't arrived, but the order takes a while).
  + To test the customer leaving if he can only afford the least expensive item but it runs out, create 3 waiters, 2 customers named "salad", and 2 customer named "enoughforsalad". Make the 2 "salad" customers hungry first, then the last customer. After the first 2 customers order, the salad inventory will be 0 and the last customer will leave since he can't afford anything else.
  + To test the customer leaving if all items on the menu are too expensive, name the customer "poor" and he will only have $5.
  + To test the customer ordering an item he can't afford, name the customer "cheapskate". Make him hungry twice, and on the second time he will be given enough cash to pay for his debt and his current order.
  + To test the waiter being unable to go on break, simply create 1 waiter and check "Break" while he is serving a customer.
  + The waiter will be able to go on break if there is more than 1 waiter and no other waiter is on break or asked to go on break.
  + To test the customer leaving if the restaurant is full, name the customer "impatient". Otherwise, he will wait.

###Known Problems
  + In some cases the waiter may have to deliver food to a customer twice before the customer gets the food (this scenario is hard to reproduce)

###Resources
  + [Restaurant v1](http://www-scf.usc.edu/~csci201/readings/restaurant-v1.html)
  + [Agent Roadmap](http://www-scf.usc.edu/~csci201/readings/agent-roadmap.html)
