/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.dtest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

//import mpi.MPI;
import sim.app.flockers.Flocker;
import sim.engine.DSteppable;
import sim.engine.DSimState;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.field.continuous.DContinuous2D;
import sim.field.partitioning.DoublePoint;
import sim.field.partitioning.NdPoint;
//import sim.util.MPIUtil;
import sim.util.Timing;

public class DSimulation extends DSimState {
	private static final long serialVersionUID = 1;

	public final static int width = 600;
	public final static int height = 600;
	public final static int numFlockers = 9;
	public final static int neighborhood = 10; // aoi

	public final DContinuous2D<DAgent> field;

	final SimpleDateFormat format = new SimpleDateFormat("ss-mm-HH-yyyy-MM-dd");
	String dateString = format.format(new Date());
	String dirname = System.getProperty("user.dir") + File.separator + dateString;

	/** Creates a Flockers simulation with the given random number seed. */
	public DSimulation(final long seed) {
		super(seed, DSimulation.width, DSimulation.height, DSimulation.neighborhood);

		final double[] discretizations = new double[] { DSimulation.neighborhood / 1.5, DSimulation.neighborhood / 1.5 };
		field = new DContinuous2D<DAgent>(getPartitioning(), aoi, discretizations, this);
	}

	@Override
	public void preSchedule() {
		super.preSchedule();
		
		if(schedule.getSteps() == 92) {
			System.exit(0);
		}

		//if (schedule.getSteps() % 10 == 0 ) {
			String filename = dirname + File.separator +
					getPartitioning().pid + "." + (schedule.getSteps());

			File testdir = new File(dirname);
			testdir.mkdir();

			File myfileagent = new File(filename);
			System.out.println("Create file " + filename);

			PrintWriter out = null;
			try {
				myfileagent.createNewFile();
				out = new PrintWriter(new FileOutputStream(myfileagent, false));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (DAgent f : field.getAllObjects()) {
				out.println("agent "+f.id+" in position "+f.loc+" num neighbours: "+f.neighbours.size()+" neighbours "+f.neighbours);
			}

			out.close();
		//}

	}
	
	@Override
	protected void startRoot() {
		ArrayList<DAgent> agents = new ArrayList<DAgent>();
		int c=0;
		for(int i=75;i<600;i=i+150) {
			for(int j=75;j<600;j=j+150) {
				DoublePoint loc = new DoublePoint(i, j);
				int id = 100*partition.toPartitionId(loc)+c;
				c++;
				agents.add(new DAgent(loc, id));
			}
		}
		
		sendRootInfoToAll("agents",agents);
	}
	
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		super.start(); //do not forget this line
		
		ArrayList<Object> agents = (ArrayList<Object>) getRootInfo("agents");
		
		for(Object p : agents) {
			DAgent a = (DAgent) p;
			if(partition.getPartition().contains(a.loc)) {
				field.addAgent(a.loc, a );
				System.out.println("pid "+partition.getPid()+" add agent "+a);
			}
		}
			
	}

	public static void main(final String[] args) {
		Timing.setWindow(20);
		doLoopDistributed(DSimulation.class, args);
		System.exit(0);
	}
}
