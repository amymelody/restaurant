package restaurant.gui;


import restaurant.CustomerAgent;
import restaurant.WaiterAgent;

import java.awt.*;
import java.util.*;

public class WaiterGui implements Gui {

    private WaiterAgent agent = null;

    private int xPos = -20, yPos = -20;//default waiter position
    private int xDestination = -20, yDestination = -20;//default start position
    private boolean atTable = false;

    //Get rid of the "magic numbers"
    static final int WAITERWIDTH = 20;
	static final int WAITERHEIGHT = 20;
	static final int COOKX = 300;
	static final int COOKY = 10;
	
    public static final int xTable = 150;
    public static final int yTable = 250;
    public static final int tableWidth = 50;
    public static final int tableHeight = 50;
    
    Map<Integer, Point> tablePositions = new HashMap<Integer, Point>();
    Map<String, String> foodSymbols = new HashMap<String, String>();
    ArrayList<String> orders = new ArrayList<String>();

    public WaiterGui(WaiterAgent agent) {
        this.agent = agent;
        
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
        	if (xDestination == -WAITERWIDTH && yDestination == -WAITERWIDTH) {
        		agent.msgAtHome();
        	}
        	if (xDestination == COOKX && yDestination == COOKY) {
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

    public void DoGoToTable(int tableNumber) {
        xDestination = (int)tablePositions.get(tableNumber).getX();
		yDestination = (int)tablePositions.get(tableNumber).getY();
		atTable = false;
    }
    
    public void DoGoToCook() {
        xDestination = COOKX;
        yDestination = COOKY;
    }

    public void DoReturnHome() {
        xDestination = -WAITERWIDTH;
        yDestination = -WAITERWIDTH;
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
