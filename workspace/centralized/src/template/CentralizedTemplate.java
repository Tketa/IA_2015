package template;

//the list of imports
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javafx.util.Duration;
import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.CentralizedBehavior;
import logist.config.Parsers;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */
@SuppressWarnings("unused")
public class CentralizedTemplate implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        
        
        final double PROBABILITY = 0.3;
        
        List<ExtendedTask> allTasks = new LinkedList<ExtendedTask>();
        
        for (Task t : tasks) {
			allTasks.add(new ExtendedTask(t, true));
			allTasks.add(new ExtendedTask(t, false));
		}
        
        
        Vehicle[] vArray = new Vehicle[vehicles.size()];
        vArray = vehicles.toArray(vArray);
        ExtendedTask[] tArray = new ExtendedTask[tasks.size()];
        tArray = allTasks.toArray(tArray);
        
        Random r = new Random();
        Solution initialSolution = selectInitialSolution(vArray, tArray);
        
        
        Solution intermediateSolution = initialSolution;
        
        intermediateSolution.print();
        
        int nbIterations = 0;
        while(nbIterations < 50000) {
        	System.out.println("Iteration " + (nbIterations + 1));
	    	int vi = -1;
	    	do {
	    		vi = r.nextInt(vehicles.size());
	    	} while(intermediateSolution.getVehicleFirstTask(vi) == null);	//En gros on cherche un vehicule qui contient au moins un tache?!    	
	    	
	    	Set<Solution> possibleNeighbours = new HashSet<Solution>();
	    	
	    	possibleNeighbours.addAll(changeVehicleOperator(intermediateSolution, vArray, vi));
	    	//intermediateSolution.print();
	    	possibleNeighbours.addAll(changeTaskOrderOperation(intermediateSolution, vi));
	    	
	    	Solution ultimateSolution = null;
	    	ArrayList<Solution> bestSolutions = new ArrayList<Solution>();
	    	//bestSolutions.add(ultimateSolution);
	    	double minCost = initialSolution.computeCost(vArray);
	    	
	    	boolean strictMin = false;
	    
	    	int validSolutions = 0;
	    	//System.out.println(possibleNeighbours.size() + " possibles neighbours.");
	    	for(Solution s : possibleNeighbours) {
	    		if(s.isValid(vArray)) {
	    			validSolutions++;
	    			double cost = s.computeCost(vArray);
	    			if(cost < minCost) {
	    				ultimateSolution = s;
	    				minCost = cost;
	    				
	    				strictMin = true;
	    				bestSolutions.clear();
	    			} else if(cost == minCost) {
	    				
	    				if(strictMin)
	    					bestSolutions.add(ultimateSolution);
	    				
	    				bestSolutions.add(s);
	    				strictMin = false;
	    			}
	    		}
	    	}
	    	
	    	//System.out.println(validSolutions + " valid solutions.");
	    	
	    	double randomNumber = r.nextDouble();
	    	
	    	// Il faut utiliser ce randomNumber pour faire le truc avec p et 1-p.
	    	
	    	if(randomNumber < PROBABILITY) {
		    	if(strictMin) {
		    		intermediateSolution = ultimateSolution;
		    	} else {
		    		int rndIndex = r.nextInt(bestSolutions.size());
		    		intermediateSolution = bestSolutions.get(rndIndex);
		    	}
	    	}
	    	
	    	double intermediateCost = intermediateSolution.computeCost(vArray);
	    	
	    	System.out.println("Cost [" + intermediateCost + "]");
	    	nbIterations++;
        }
    	
        intermediateSolution.print();
        
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");
        
        List<Plan> plans = new LinkedList<Plan>();
        
        for (Vehicle v : vehicles) {
			plans.add(intermediateSolution.generatePlan(v));
		}
        
        return plans;
    }
    
    private Solution selectInitialSolution(Vehicle[] vehicles, ExtendedTask[] tasks) {
    	
    	int maxCapacity = 0;
    	int maxCapacityIdx = -1;
    	
    	for(int i = 0; i < vehicles.length; i++) {
    		if(vehicles[i].capacity() > maxCapacity) {
    			maxCapacity = vehicles[i].capacity();
    			maxCapacityIdx = i;
    		}
    	}
    	
    	Solution s = new Solution(vehicles.length);
    	
    	int i = 0;
    	for(int j = 0; j < tasks.length; j+=2, i++) {
    		
    		s.addTask(i % vehicles.length, tasks[j]);
    		//s.addTask(i % vehicles.length, tasks[j+1]);
    	}
    	
    	return s;
    	
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
    
    /**
     * -----------------------------------
     * SLS OPERATORS 
     * -----------------------------------
     */
    
    private Set<Solution> changeVehicleOperator(Solution oldSolution, Vehicle[] vehicles, int vi) {
    	
    	Random r = new Random();
    	Set<Solution> solutions = new HashSet<Solution>();
    	int nbVehicles = oldSolution.getNbVehicles();
    	    	        	
    	ExtendedTask t = oldSolution.getVehicleFirstTask(vi);
    	//oldSolution.print();
    	for(int vj = 0; vj < nbVehicles; vj++) {
    		if(vi != vj) {
    			Solution tmpSolution = oldSolution.clone();
    				
    			Solution toPrint = tmpSolution.swapVehicles(vi, vj);
    			solutions.add(toPrint);
    			//System.out.println(toPrint.isValid(vehicles));
    			
    		}
    	}

    	//Je comprend pas comment c'est possible que oldSolution change alors que je fais un clone
    	return solutions;
    }
    
    private Set<Solution> changeTaskOrderOperation(Solution oldSolution, int vi) {
    	
    	Random r = new Random();
    	Set<Solution> solutions = new HashSet<Solution>();
    	
    	int length = oldSolution.getNbTaskForVehicle(vi);
    	
    	
    	if(length >= 2) {
    		for(int i = 0; i < length -1; i++) {
    			for(int j = i + 1; j < length; j++) {
    				Solution toPrint = oldSolution.swapTasks(vi, i, j);
    				
    				solutions.add(toPrint);
    			}
    		}
    	}
    	
    	return solutions;
    }
}
