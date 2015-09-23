import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		
	
	private static final int NUMRABBITS = 10;
	private static final int GRIDSIZEX = 20;
	private static final int GRIDSIZEY = 20;
	private static final int BIRTHTHRESHOLD = 15;
	private static final int GRASSRATE = 30;
	private static final int INITIALENERGY = 10;
	private static final int MOVEENERGY = 1;
	
	private int numRabbits = NUMRABBITS;
	private int gridSizeX = GRIDSIZEX;
	private int gridSizeY = GRIDSIZEY;
	private int birthThreshold = BIRTHTHRESHOLD;
	private int grassRate = GRASSRATE;
	private int initialEnergy = INITIALENERGY;
	private int moveEnergy = MOVEENERGY;
			
	private Schedule schedule;	
	private RabbitsGrassSimulationSpace rSpace;
	private DisplaySurface displaySurf;
	private ArrayList<RabbitsGrassSimulationAgent> agentList;
	
	private OpenSequenceGraph populationGraph;
	private OpenSequenceGraph grassGraph;
	
	class PopulationInSpace implements DataSource, Sequence {

		@Override
		public double getSValue() {
			return rSpace.getNbAgents();
		}

		@Override
		public Object execute() {
			return new Double(getSValue());
		}
	}
	
	class GrassInSpace implements DataSource, Sequence {
		
		@Override
		public double getSValue() {
			return rSpace.getGrassQuantity();
		}

		@Override
		public Object execute() {
			return new Double(getSValue());
		}
	}


	public static void main(String[] args) {
		
		SimInit init = new SimInit();
	    RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
	    init.loadModel(model, "", false);
	}
	
	public void setup() {
		System.out.println("Running Setup");
		rSpace = null;
		agentList = new ArrayList<RabbitsGrassSimulationAgent>();
		schedule = new Schedule(1);
		
		if (displaySurf != null){
			displaySurf.dispose();
		}
		displaySurf = null;
		
	    if (populationGraph != null){
	    	populationGraph.dispose();
	      }
	    populationGraph = null;
	    
	    if(grassGraph != null) {
	    	grassGraph.dispose();
	    }
	    grassGraph = null;
	    
	    
		displaySurf = new DisplaySurface(this, "Rabbit Model Window 1");
		populationGraph = new OpenSequenceGraph("Amount of agents in space", this);
		grassGraph = new OpenSequenceGraph("Amount of Grass in space", this);
		
		registerDisplaySurface("Rabbit Model Window 1", displaySurf);
		this.registerMediaProducer("Population Plot", populationGraph);
		this.registerMediaProducer("Grass Plot", grassGraph);
	}
	
	public void begin() {
	    buildModel();
	    buildSchedule();
	    buildDisplay();
	    
	    displaySurf.display();
	    populationGraph.display();
	    grassGraph.display();
	}

	public void buildModel(){
		System.out.println("Running BuildModel");
		if(gridSizeX == 0 || gridSizeY == 0){
			System.err.println("Gridsize can't be 0 -- Exiting programm");
			System.exit(-1);
		}
		rSpace = new RabbitsGrassSimulationSpace(gridSizeX, gridSizeY);
		rSpace.spreadGrass(grassRate);
		
		for(int i = 0; i < numRabbits; i++){
			addNewAgent();
		}
	}

	public void buildSchedule(){
		System.out.println("Running BuildSchedule");
		
		class RabbitGrassSimulationStep extends BasicAction {
			public void execute() {
				SimUtilities.shuffle(agentList);
				int nbAgents = agentList.size();
				for(int i = 0; i < nbAgents; i++){
					RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent)agentList.get(i);
					rgsa.step();
					if(rgsa.getEnergy() > birthThreshold){
						if(addNewAgent()){
							rgsa.setEnergy(rgsa.getEnergy() - birthThreshold);	
						}
					}
		        }
			
			reapDeadAgents();
			rSpace.spreadGrass(grassRate);
			displaySurf.updateDisplay();
		    }
		}
	    schedule.scheduleActionBeginning(0, new RabbitGrassSimulationStep());

	      class UpdatePopulationInSpace extends BasicAction {
	        public void execute(){
	          populationGraph.step();
	        }
	      }

	      schedule.scheduleActionAtInterval(10, new UpdatePopulationInSpace());
	      
	      class UpdateGrassInSpace extends BasicAction {
		        public void execute(){
		          grassGraph.step();
		        }
		      }

		      schedule.scheduleActionAtInterval(10, new UpdateGrassInSpace());
	}
	
	public void buildDisplay(){
		System.out.println("Running BuildDisplay");
		ColorMap map = new ColorMap();

	    for(int i = 1; i<16; i++){
	    	map.mapColor(i, new Color(0, (int)(i * 8 + 127), 0));
	    }
	    map.mapColor(0, Color.black);
	    Value2DDisplay displayGrass = new Value2DDisplay(rSpace.getCurrentGrassSpace(), map);
	    Object2DDisplay displayAgents = new Object2DDisplay(rSpace.getCurrentAgentSpace());
	    displayAgents.setObjectList(agentList);
	    
	    displaySurf.addDisplayableProbeable(displayGrass, "Grass");
	    displaySurf.addDisplayableProbeable(displayAgents, "Agents");
	    
	    populationGraph.addSequence("Population In Space", new PopulationInSpace());
	    //grassGraph.addSequence("Grass in Space", new GrassInSpace());
	    populationGraph.addSequence("Grass in Space", new GrassInSpace());
	}
	
	private boolean addNewAgent(){
	    RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(initialEnergy, moveEnergy);
	    boolean canAdd = rSpace.addAgent(a);
	    if(canAdd) {
	    	agentList.add(a);
	    }
	    return canAdd;
	}
	
	private int reapDeadAgents(){
		int count = 0;
	    for(int i = (agentList.size() - 1); i >= 0 ; i--){
	    	RabbitsGrassSimulationAgent rabbitAgent = (RabbitsGrassSimulationAgent)agentList.get(i);
	    	if(rabbitAgent.getEnergy() < 1){
	    		rSpace.removeAgentAt(rabbitAgent.getX(), rabbitAgent.getY());
	    		agentList.remove(i);
	    		count++;
	    	}
	    }
	    return count;
    }
	
	public String[] getInitParam(){
		String[] initParams = {"NumRabbits", "gridSizeX", "gridSizeY", "birthThreshold", "grassRate", "initialEnergy", "moveEnergy"};
	    return initParams;
	}
	
	public int getNumRabbits(){
		return numRabbits;
	}
	
	public void setNumRabbits(int na){
	    numRabbits = na;
	}
	
	public int getGridSizeX() {
		return gridSizeX;
	}

	public void setGridSizeX(int gridSizeX) {
		this.gridSizeX = gridSizeX;
	}

	public int getGridSizeY() {
		return gridSizeY;
	}

	public void setGridSizeY(int gridSizeY) {
		this.gridSizeY = gridSizeY;
	}
	public String getName() {
		return "Rabbits agents";
	}
	
	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void setBirthThreshold(int birthThreshold) {
		this.birthThreshold = birthThreshold;
	}

	public int getGrassRate() {
		return grassRate;
	}

	public void setGrassRate(int grassRate) {
		this.grassRate = grassRate;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public int getInitialEnergy() {
		return initialEnergy;
	}

	public void setInitialEnergy(int initialEnergy) {
		this.initialEnergy = initialEnergy;
	}

	public int getMoveEnergy() {
		return moveEnergy;
	}

	public void setMoveEnergy(int moveEnergy) {
		this.moveEnergy = moveEnergy;
	}
	
}
