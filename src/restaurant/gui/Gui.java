package restaurant.gui;

import java.awt.*;

/**
 * Base class for all GUIs
 */
public interface Gui {

	/**
	 * This is what drives the animation; called each time the animation timer fires
	 */
    public void updatePosition();
    
    /**
     * This creates the visuals themselves based on the position set in updatePosition
     */
    public void draw(Graphics2D g);
    
    /**
     * This returns true if the GUI is currently present in the animation window, false otherwise
     */
    public boolean isPresent();

}
