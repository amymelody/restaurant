package restaurant.gui;


import restaurant.CustomerAgent;
import restaurant.WaiterAgent;

import java.awt.*;
import java.util.*;

public class WaiterGui implements Gui {

    private WaiterAgent agent = null;
    
    RestaurantGui gui;

    private int xPos, yPos;
    private int xDestination, yDestination;
    private boolean atTable = false;

    //Get rid of the "magic numbers"
    static final int WAITERWIDTH = 20;
	static final int WAITERHEIGHT = 20;
	static final int COOKX = 200;
	static final int COOKY = 10;
	static final int CUSTX = 20;
	static final int CUSTY = 20;
	static final int PLATINGX = 250;
	static final int PLATINGY = 60;
	final int HOMEX;
	final int HOMEY;
	
    public static final int xTable = 150;
    public static final int yTable = 250;
    public static final int tableWidth = 50;
    public static final int tableHeight = 50;
    
    Map<Integer, Point> tablePositions = new HashMap<Integer, Point>();
    Map<String, String> foodSymbols = new HashMap<String, String>();
    ArrayList<String> orders = new ArrayList<String>();

    public WaiterGui(WaiterAgent agent, RestaurantGui gui, int waiterNum) {
        this.agent = agent;
        this.gui = gui;
        HOMEX = 30*((waiterNum + 6-((waiterNum-1)%7))/7 - 1);
        HOMEY = ((waiterNum-1)%7)*30 + 160;
        xPos = HOMEX;
        yPos = HOMEY;
        xDestination = HOMEX;
        yDestination = HOMEY;
        
        tablePositions.put(1, new Point(xTable + WAITERWIDTH, yTable - WAITERWIDTH));
		tablePositions.put(2, new Point(xTable + 2*tableWidth + WAITERWIDTH, yTable - WAITERWIDTH));
		tablePositions.put(3, new Point(xTable + 4*tableWidth + WAITERWIDTH, yTable - WAITERWIDTH));
		
		foodSymbols.put("steak", "St");
		foodSymbols.put("chicken", "C");
		foodSymbols.put("pizza", "P");
		foodSymbols.put("salad", "Sa");
    }

    public void updatePosition() {
        if (xPos < xDestination)
            xPos++;
        else if (xPos > xDestination)
            xPos--;

        if (yPos < yDestination)
            yPos++;
        else if (yPos > yDestination)
            yPos--;

        if (xPos == xDestination && yPos == yDestination) {
        	if ((xDestination >= xTable + WAITERWIDTH) && (yDestination == yTable - WAITERWIDTH)) {
        		if (!atTable) {
        			agent.msgAtTable();
        			atTable = true;
	        		if (!orders.isEmpty()) {
	        			orders.remove(0);
	        		}
        		}
        	}
        	if (xDestination == HOMEX && yDestination == HOMEY) {
        		agent.msgAtHome();
        	}
        	if (xDestination == CUSTX && yDestination == CUSTY) {
        		agent.msgAtCustomer();
        	}
        	if ((xDestination == COOKX && yDestination == COOKY) || (xDestination == PLATINGX && yDestination == PLATINGY)) {
        		agent.msgAtCook();
        	}
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.MAGENTA);
        g.fillRect(xPos, yPos, WAITERWIDTH, WAITERHEIGHT);
        
        if (!orders.isEmpty()) { 
			g.setColor(Color.WHITE);
			g.fillRect(xPos+WAITERWIDTH, yPos, WAITERWIDTH, WAITERHEIGHT);
			g.setColor(Color.BLACK);
			g.drawString(orders.get(0), xPos+WAITERWIDTH+WAITERWIDTH/4, yPos+WAITERHEIGHT/2);
		}
    }

    public boolean isPresent() {
        return true;
    }
    
    public void setCBEnabled() {
    	gui.setWaiterEnabled(agent);
    }

    public void DoGoToTable(int tableNumber) {
        xDestination = (int)tablePositions.get(tableNumber).getX();
		yDestination = (int)tablePositions.get(tableNumber).getY();
		atTable = false;
    }
    
    public void DoGoToCustomer() {
    	xDestination = CUSTX;
		yDestination = CUSTY;
    }
    
    public void DoGoToCook() {
        xDestination = COOKX;
        yDestination = COOKY;
    }
    
    public void DoGoToPlatingArea() {
        xDestination = PLATINGX;
        yDestination = PLATINGY;
    }

    public void DoReturnHome() {
        xDestination = HOMEX;
        yDestination = HOMEY;
    }
    
    public void DoDeliverFood(String choice) {
    	orders.add(foodSymbols.get(choice));
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
}
