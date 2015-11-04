package template;

import logist.task.Task;

public class ExtendedTask {

	private Task t;
	
	private boolean pickup;
	
	private int id;
	
	
	public ExtendedTask(Task t, boolean pickup) {
		// TODO Auto-generated constructor stub
		this.t = t;
		this.pickup = pickup;
		
		String sId = t.id + (pickup ? "0" : "1");
		id = Integer.parseInt(sId);
	}


	/**
	 * @return the t
	 */
	public Task getT() {
		return t;
	}


	/**
	 * @return the pickup
	 */
	public boolean isPickup() {
		return pickup;
	}
	
	public int id() {
		
		return id;
	}
	
	
	
}
