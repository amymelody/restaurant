package restaurant.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

/**
 * Panel in frame that displays the restaurant animation
 */
public class AnimationPanel extends JPanel implements ActionListener {

	//Get rid of the "magic numbers"
	static final int TABLEX = 150;
	static final int TABLEY = 250;
	static final int TABLEWIDTH = 50;
	static final int TABLEHEIGHT = 50;
	
    private final int WINDOWX = 450;
    private final int WINDOWY = 400;
    private Image bufferImage;
    private Dimension bufferSize;
    private Timer timer;

    private List<Gui> guis = new ArrayList<Gui>();

    /**
     * Constructor. Starts the animation timer
     */
    public AnimationPanel() {
    	setSize(WINDOWX, WINDOWY);
        setVisible(true);
        
        bufferSize = this.getSize();
 
    	timer = new Timer(10, this );
    	timer.start();
    }

    /**
     * Action listener method that fires after each timer interval to display the animation
     */
	public void actionPerformed(ActionEvent e) {
		repaint();  //Will have paintComponent called
	}
	
	/**
	 * Stops the timer and pauses the animation
	 */
	public void pauseAnimation() {
		timer.stop();
	}
	
	/**
	 * Starts the timer and resumes the animation
	 */
	public void resumeAnimation() {
		timer.start();
	}

	/**
	 * Paints all the static images in the restaurant and updates all individual GUI positions
	 */
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        //Clear the screen by painting a rectangle the size of the frame
        g2.setColor(getBackground());
        g2.fillRect(0, 0, WINDOWX, WINDOWY );

        //Here are the tables
        g2.setColor(Color.ORANGE);
        g2.fillRect(TABLEX, TABLEY, TABLEWIDTH, TABLEHEIGHT);//200 and 250 need to be table params
        g2.fillRect(TABLEX+TABLEWIDTH*2, TABLEY, TABLEWIDTH, TABLEHEIGHT);
        g2.fillRect(TABLEX+TABLEWIDTH*4, TABLEY, TABLEWIDTH, TABLEHEIGHT);


        for(Gui gui : guis) {
            if (gui.isPresent()) {
                gui.updatePosition();
            }
        }

        for(Gui gui : guis) {
            if (gui.isPresent()) {
                gui.draw(g2);
            }
        }
    }

    /**
     * Adds a CustomerGui to the list of GUIs
     * 
     * @param gui Reference to CustomerGui
     */
    public void addGui(CustomerGui gui) {
        guis.add(gui);
    }

    /**
     * Adds a WaiterGui to the list of GUIs
     * 
     * @param gui Reference to WaiterGui
     */
    public void addGui(WaiterGui gui) {
        guis.add(gui);
    }
    
    /**
     * Adds a CookGui to the list of GUIs
     * 
     * @param gui Reference to CookGui
     */
    public void addGui(CookGui gui) {
        guis.add(gui);
    }
}
