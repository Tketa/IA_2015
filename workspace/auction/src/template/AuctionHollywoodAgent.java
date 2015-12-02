//package template;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Random;
//import java.util.Set;
//
//import logist.LogistPlatform;
//import logist.LogistSettings;
//import logist.agent.Agent;
//import logist.behavior.AuctionBehavior;
//import logist.config.Parsers;
//import logist.plan.Plan;
//import logist.simulation.Vehicle;
//import logist.task.Task;
//import logist.task.TaskDistribution;
//import logist.task.TaskSet;
//import logist.topology.Topology;
//
//
//public class AuctionHollywoodAgent implements AuctionBehavior {
//
//	private Topology topology;
//	private TaskDistribution distribution;
//	private Agent agent;
//	private Random random;
//	private List<Vehicle> vehicles;
//	private List<Task> currentTasks;
//	
//	private Solution currentSolution;
//	private Solution futureSolution;
//	
//    private long timeout_bid;
//	
//	@Override
//	public void setup(Topology topology, TaskDistribution distribution,
//			Agent agent) {
//		
//		this.topology = topology;
//		this.distribution = distribution;
//		this.agent = agent;
//		this.vehicles = agent.vehicles();
//		this.currentTasks = new ArrayList<Task>();
//
//		long seed = -9019554669489983951L * vehicles.get(0).getCurrentCity().hashCode() * agent.id();
//		this.random = new Random(seed);
//
//		this.timeout_bid = LogistPlatform.getSettings().get(LogistSettings.TimeoutKey.BID);
//	}
//
//	@Override
//	public Long askPrice(Task task) {
//		
//		System.out.println("Ask price");
//
//		long startTime = System.currentTimeMillis();
//		long endTime = startTime + timeout_bid;
//		
//		Vehicle[] vArray = new Vehicle[agent.vehicles().size()];
//		vArray = agent.vehicles().toArray(vArray);
//		
//		List<Task> futureTasks = new ArrayList<Task>(currentTasks);
//		futureTasks.add(task);
//		
//		double minCost = Double.POSITIVE_INFINITY;
//		
//		while(System.currentTimeMillis() < endTime) {
//			Solution possibleSolution = computeCentralized(vehicles, futureTasks, 0.5);
//			double possibleCost = possibleSolution.computeCost(vArray);
//			if(possibleCost < minCost) {
//				minCost = possibleCost;
//				this.futureSolution = possibleSolution;
//			}
//		}
//		
//		double marginalCost = minCost - currentSolution.computeCost(vArray);
//		
//		return (long) Math.ceil(marginalCost);
//	}
//
//	@Override
//	public void auctionResult(Task lastTask, int lastWinner, Long[] lastOffers) {
//		
//		/**
//		 * Do shit here to predict what the opponents do
//		 */
//		
//		if (lastWinner == this.agent.id()) {
//			System.out.println("You won the auction!");
//			this.currentSolution = this.futureSolution;
//		}
//		
//	}
//
//	@Override
//	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	private Solution computeCentralized(List<Vehicle> vehicles, List<Task> tasks, double probability) {
//		
//		int nbTasks = tasks.size();
//		Random r = new Random();
//		
//        Vehicle[] vArray = new Vehicle[vehicles.size()];
//        vArray = vehicles.toArray(vArray);
//		
//		Solution initialSolution = initialSolution(vArray, tasks);
//		
//		Solution intermediateSolution = initialSolution;
//        
//        intermediateSolution.print();
//        
//        int nbIterations = 0;
//        while(nbIterations < 5000){
//        	System.out.println("Iteration " + (nbIterations + 1));
//	    	int vi = -1;
//	    	do {
//	    		vi = r.nextInt(vehicles.size());
//	    	} while(intermediateSolution.getVehicleFirstTask(vi) == null);    	
//	    	
//	    	Set<Solution> possibleNeighbours = new HashSet<Solution>();
//	    	
//	    	possibleNeighbours.addAll(changeVehicleOperator(intermediateSolution, vArray, vi));
//
//	    	possibleNeighbours.addAll(changeTaskOrderOperation(intermediateSolution, vi));
//	    	
//	    	Solution ultimateSolution = null;
//	    	ArrayList<Solution> bestSolutions = new ArrayList<Solution>();
//	    	
//	    	//double currentCost = intermediateSolution.computeCost(vArray);
//	    	double maxImprovement = Double.MAX_VALUE;
//	    	
//	    	boolean strictMin = false;
//	    	
//	    	
//	    	for(Solution s : possibleNeighbours) {
//	    		if(s.isValid(vArray, nbTasks)) {
//	    			double cost = s.computeCost(vArray);
//	    			
//	    			if(cost < maxImprovement) {
//	    				ultimateSolution = s;
//	    				maxImprovement = cost;
//	    				
//	    				strictMin = true;
//	    				bestSolutions.clear();
//	    			} else if(cost == maxImprovement) {
//	    				
//	    				if(strictMin)
//	    					bestSolutions.add(ultimateSolution);
//	    				
//	    				bestSolutions.add(s);
//	    				strictMin = false;
//	    			}
//	    		}
//	    	}
//	    	
//	    	double randomNumber = r.nextDouble();
//	    
//	    	if(randomNumber < probability) {
//	    		
//		    	if(strictMin) {
//		    		intermediateSolution = ultimateSolution;
//		    	} else {
//		    		int rndIndex = r.nextInt(bestSolutions.size());
//		    		intermediateSolution = bestSolutions.get(rndIndex);
//		    	}
//	    	}
//	    	
//	    	double intermediateCost = intermediateSolution.computeCost(vArray);
//	    	
//	    	System.out.println("Cost [" + intermediateCost + "]");
//	    	nbIterations++;
//        }
//		
//		return intermediateSolution;
//	}
//	
//    private Solution initialSolution(Vehicle[] vehicles, List<Task> tasks) {
//    	
//    	Solution s = null;
//    	for(int i = 0; i < vehicles.length; i++) {
//        	s = new Solution(vehicles.length);
//
//	    	for(Task t : tasks) {
//	    		s.addTask(vehicles[i].id(), t);
//	    	}
//	    	
//	    	if(s.isValid(vehicles, tasks.size())) {
//	    		break;
//	    	}
//    	}
//    	
//    	if(!s.isValid(vehicles, tasks.size())) {
//    		System.err.println("[ERROR] One task is too heavy to be carried by any of our vehicles. Aborting");
//    		// Exit aggressively with error code: -1
//    		System.exit(-1);
//    	}
//    	
//    	
//    	return s;
//    }
//    
//    /**
//     * -----------------------------------
//     * SLS OPERATORS 
//     * -----------------------------------
//     */
//    
//    private Set<Solution> changeVehicleOperator(Solution oldSolution, Vehicle[] vehicles, int vi) {
//    	
//    	Random r = new Random();
//    	Set<Solution> solutions = new HashSet<Solution>();
//    	int nbVehicles = oldSolution.getNbVehicles();
//    	    	        	
//    	ExtendedTask t = oldSolution.getVehicleFirstTask(vi);
//
//    	for(int vj = 0; vj < nbVehicles; vj++) {
//    		if(vi != vj) {
//    			Solution tmpSolution = oldSolution.clone();
//    				
//    			
//    			solutions.add(tmpSolution.swapVehicles(vi, vj));    			
//    		}
//    	}
//
//    	return solutions;
//    }
//    
//    private Set<Solution> changeTaskOrderOperation(Solution oldSolution, int vi) {
//    	
//    	Random r = new Random();
//    	Set<Solution> solutions = new HashSet<Solution>();
//    	
//    	int length = oldSolution.getNbTaskForVehicle(vi);
//    	
//    	
//    	if(length >= 2) {
//    		for(int i = 0; i < length -1; i++) {
//    			for(int j = i + 1; j < length; j++) {
//    				Solution toPrint = oldSolution.swapTasks(vi, i, j);
//    				
//    				solutions.add(toPrint);
//    			}
//    		}
//    	}
//    	
//    	return solutions;
//    }
//
//	
//	
//	
//}
