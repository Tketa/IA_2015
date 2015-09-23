import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
	private Object2DGrid grassSpace;
	private Object2DGrid agentSpace;
	
	private int nbAgents = 0;
	private int grassQuantity = 0;
		
	public RabbitsGrassSimulationSpace (int xSize, int ySize){
		grassSpace = new Object2DGrid(xSize, ySize);
		agentSpace = new Object2DGrid(xSize, ySize);
		
		for(int i = 0; i < xSize; i++){
			for(int j = 0; j < ySize; j++){
				grassSpace.putObjectAt(i,j,new Integer(0));
			}
		}   
	}
	public void spreadGrass(int grass){
		
		grassQuantity += grass;
		
		for(int i = 0; i < grass; i++){
			
			int x = (int)(Math.random()*(grassSpace.getSizeX()));
			int y = (int)(Math.random()*(grassSpace.getSizeY()));
			
			int currentValue = getGrassAt(x, y);
			grassSpace.putObjectAt(x,y,new Integer(currentValue+1));
	    }
	}
	public int getGrassAt(int x, int y){
		int value;
		if(grassSpace.getObjectAt(x,y)!= null){
			value = ((Integer)grassSpace.getObjectAt(x,y)).intValue();
		}
		else{
			value = 0;
		}
		return value;
	}

	
	public boolean isCellOccupied(int x, int y){
		boolean occupied = false;
		if(agentSpace.getObjectAt(x, y)!=null) occupied = true;
		return occupied;
	}
	
	public boolean addAgent(RabbitsGrassSimulationAgent agent){
		boolean occupied = false;
		int count = 0;
		int maxGuess = 10* agentSpace.getSizeX() * agentSpace.getSizeY(); // Je sais pas si c'est vraiment bien ca!
		
		while((occupied==false) && (count < maxGuess)){
			int x = (int)(Math.random()*(agentSpace.getSizeX()));
			int y = (int)(Math.random()*(agentSpace.getSizeY()));
			if(isCellOccupied(x,y) == false){
				agentSpace.putObjectAt(x,y,agent);
				nbAgents++;
				agent.setXY(x,y);
				agent.setCarryDropSpace(this);
				occupied = true;
			}
			count++;
		}
		
		return occupied;
		
	}
	
	public void removeAgentAt(int x, int y){
		agentSpace.putObjectAt(x, y, null);
		nbAgents--;
	}
	
	public int eatGrassAt(int x, int y){
		int grass = getGrassAt(x, y);
		
		// Remove that grass quantity
		grassQuantity -= grass;
		
		grassSpace.putObjectAt(x, y, new Integer(0));
		return grass;
	}
	
	public boolean moveAgentAt(int x, int y, int newX, int newY){
		boolean retVal = false;
	    if(!isCellOccupied(newX, newY)){
	    	RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)agentSpace.getObjectAt(x, y);
	    	removeAgentAt(x,y);
	    	rgsa.setXY(newX, newY);
	    	agentSpace.putObjectAt(newX, newY, rgsa);
	    	nbAgents++;
	    	retVal = true;
	    }
	    return retVal;
	}
	
	public Object2DGrid getCurrentGrassSpace(){
		return grassSpace;
	}
	
	public Object2DGrid getCurrentAgentSpace(){
		return agentSpace;
	}
	
	public int getNbAgents() {
		return nbAgents;
	}
	
	public int getGrassQuantity() {
		return grassQuantity;
	}
}
