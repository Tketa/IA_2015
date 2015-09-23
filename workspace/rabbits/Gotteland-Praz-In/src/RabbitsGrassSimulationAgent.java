import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

import java.awt.Color;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	
	private int x;
	private int y;
	private int vX;
	private int vY;
	private int energy;
	private int moveEnergy;
	private RabbitsGrassSimulationSpace rSpace;
	
	
	public RabbitsGrassSimulationAgent(int energy, int moveEnergy) {
		x = -1;
		y = -1;
		setVxVy();
		this.energy = energy;
		this.moveEnergy = moveEnergy;
	}
	
	public void setCarryDropSpace(RabbitsGrassSimulationSpace rgss){
		rSpace = rgss;
	}
	
	private void setVxVy(){
		vX = 0;
		vY = 0;
		while( Math.abs(vX + vY) != 1){
			vX = (int)Math.floor(Math.random() * 3) - 1;
			vY = (int)Math.floor(Math.random() * 3) - 1;
		}
	}

	public void draw(SimGraphics G) {
		G.drawFastOval(Color.white);
	}
	
	public void step(){
		int newX = x + vX;
	    int newY = y + vY;
	    System.out.println(rSpace);
	    Object2DGrid grid = rSpace.getCurrentAgentSpace();
	    newX = (newX + grid.getSizeX()) % grid.getSizeX();
	    newY = (newY + grid.getSizeY()) % grid.getSizeY();

	    tryMove(newX, newY); // Qu'est-ce qu'on fait quand on peut pas bouger? On retente ailleurs ou alors on attend le prochain step?
	    setVxVy();
	    energy = energy - moveEnergy + rSpace.eatGrassAt(x, y);
	    
	}
	
	private boolean tryMove(int newX, int newY){
		return rSpace.moveAgentAt(x, y, newX, newY);
	}
	
	public void setXY(int newX, int newY){
		this.x = newX;
		this.y = newY;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}
}
