/*
\  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.dflockers;

import java.rmi.Remote;
import java.util.List;

import ec.util.MersenneTwisterFast;
import sim.engine.DSteppable;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.field.continuous.DContinuous2D;
import sim.field.partitioning.DoublePoint;

public class DFlocker extends DSteppable implements Remote {
	private static final long serialVersionUID = 1;
	public DoublePoint loc;
	public DoublePoint lastd = new DoublePoint(0, 0);
	public boolean dead = false;
	
	public int id;

	public DFlocker(final DoublePoint location, final int id) {
		this.loc = location;
		this.id = id;
	}

	public double getOrientation() {
		return orientation2D();
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(final boolean val) {
		dead = val;
	}

	public void setOrientation2D(final double val) {
		lastd = new DoublePoint(Math.cos(val), Math.sin(val));
	}

	public double orientation2D() {
		if (lastd.c[0] == 0 && lastd.c[1] == 0)
			return 0;
		return Math.atan2(lastd.c[1], lastd.c[0]);
	}

	public DoublePoint momentum() {
		return lastd;
	}

	public DoublePoint consistency(final List<DFlocker> b, final DContinuous2D<DFlocker> flockers) {
		if (b == null || b.size() == 0)
			return new DoublePoint(0, 0);

		double x = 0;
		double y = 0;
		int i = 0;
		int count = 0;
		for (i = 0; i < b.size(); i++) {
			final DFlocker other = (b.get(i));
			if (!other.dead) {
				final DoublePoint m = b.get(i).momentum();
				count++;
				x += m.c[0];
				y += m.c[1];
			}
		}
		if (count > 0) {
			x /= count;
			y /= count;
		}
		return new DoublePoint(x, y);
	}

	public DoublePoint cohesion(final List<DFlocker> b, final DContinuous2D<DFlocker> flockers) {
		if (b == null || b.size() == 0)
			return new DoublePoint(0, 0);

		double x = 0;
		double y = 0;

		int count = 0;
		int i = 0;
		for (i = 0; i < b.size(); i++) {
			final DFlocker other = (b.get(i));
			if (!other.dead) {
				final double dx = flockers.tdx(loc.c[0], other.loc.c[0]);
				final double dy = flockers.tdy(loc.c[1], other.loc.c[1]);
				count++;
				x += dx;
				y += dy;
			}
		}
		if (count > 0) {
			x /= count;
			y /= count;
		}
		return new DoublePoint(-x / 10, -y / 10);
	}

	public DoublePoint avoidance(final List<DFlocker> b, final DContinuous2D<DFlocker> flockers) {
		if (b == null || b.size() == 0)
			return new DoublePoint(0, 0);
		double x = 0;
		double y = 0;

		int i = 0;
		int count = 0;

		for (i = 0; i < b.size(); i++) {
			final DFlocker other = (b.get(i));
			if (other != this) {
				final double dx = flockers.tdx(loc.c[0], other.loc.c[0]);
				final double dy = flockers.tdy(loc.c[1], other.loc.c[1]);
				final double lensquared = dx * dx + dy * dy;
				count++;
				x += dx / (lensquared * lensquared + 1);
				y += dy / (lensquared * lensquared + 1);
			}
		}
		if (count > 0) {
			x /= count;
			y /= count;
		}
		return new DoublePoint(400 * x, 400 * y);
	}

	public DoublePoint randomness(final MersenneTwisterFast r) {
		final double x = r.nextDouble() * 2 - 1.0;
		final double y = r.nextDouble() * 2 - 1.0;
		final double l = Math.sqrt(x * x + y * y);
		return new DoublePoint(0.05 * x / l, 0.05 * y / l);
	}

	public void step(final SimState state) {
		final DFlockers dFlockers = (DFlockers) state;
		
		dFlockers.idLocal.add(this.id);
		
		final DoublePoint oldloc = loc;

		if (dead)
			return;
		 List<DFlocker> b = null;
		
		try {
		 b = dFlockers.flockers.getNeighborsWithin(this, DFlockers.neighborhood);
		}catch (Exception e) {
			System.out.println(dFlockers.getPartitioning().getPid());
		}

		final DoublePoint avoid = avoidance(b, dFlockers.flockers);
		final DoublePoint cohe = cohesion(b, dFlockers.flockers);
		final DoublePoint rand = randomness(dFlockers.random);
		final DoublePoint cons = consistency(b, dFlockers.flockers);
		final DoublePoint mome = momentum();

		double dx = DFlockers.cohesion * cohe.c[0] + DFlockers.avoidance * avoid.c[0]
				+ DFlockers.consistency * cons.c[0]
				+ DFlockers.randomness * rand.c[0] + DFlockers.momentum * mome.c[0];
		double dy = DFlockers.cohesion * cohe.c[1] + DFlockers.avoidance * avoid.c[1]
				+ DFlockers.consistency * cons.c[1]
				+ DFlockers.randomness * rand.c[1] + DFlockers.momentum * mome.c[1];

		// re-normalize to the given step size
		final double dis = Math.sqrt(dx * dx + dy * dy);
		if (dis > 0) {
			dx = dx / dis * DFlockers.jump;
			dy = dy / dis * DFlockers.jump;
		}
		lastd = new DoublePoint(dx,dy);
		loc = new DoublePoint(dFlockers.flockers.stx(loc.c[0] + dx), dFlockers.flockers.sty(loc.c[1] + dy));
		try {
			dFlockers.flockers.moveAgent(oldloc, loc, this);
		}catch (Exception e) {
			System.err.println("error on agent "+this+ " in step "+dFlockers.schedule.getSteps()+ "on pid "+dFlockers.getPartitioning().pid);
			System.exit(1);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DFlocker))
			return false;
		DFlocker other = (DFlocker) obj;
		return (id == other.id);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "{ "+this.getClass()+"@"+Integer.toHexString(hashCode())+" id: "+this.id+"}";
	}
	
}
