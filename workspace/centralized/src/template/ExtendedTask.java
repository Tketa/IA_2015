package template;

import logist.task.Task;

public class ExtendedTask {

	private Task t;
	
	private boolean pickup;
	
	
	public ExtendedTask(Task t, boolean pickup) {
		// TODO Auto-generated constructor stub
		this.t = t;
		this.pickup = pickup;
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
	
	
}
