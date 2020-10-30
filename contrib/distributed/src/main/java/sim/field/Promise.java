package sim.field;
import java.io.*;

/** 
	This class eventually provides data in the future (usually one MASON timestep away).  
	It is in many ways like a simplified and easier to use version of java.util.concurrent.Future.
*/
	
public class Promise
	{
	boolean ready = false;
	Serializable object = null;
	
	/** Returns TRUE if the promised data is ready, else FALSE. */ 
	public boolean isReady() { return ready; }
	
	/** Returns the data.  This data is only valid if isReady() is TRUE.  */ 
	public Serializable get() { return object; }
	
	/** Provides the data and makes the promise ready.  */ 
	public void fulfill(Serializable object)
		{
		ready = true;
		this.object = object;
		}
		
	/** Copies the data and readiness from another promise.  */ 
	public void setTo(Promise promise)
		{
		ready = promise.ready;
		object = promise.object;
		}
	}
