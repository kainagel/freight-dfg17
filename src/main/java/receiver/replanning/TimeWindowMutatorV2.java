/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
  
/**
 * 
 */
package receiver.replanning;

import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;
import org.matsim.core.utils.misc.Time;

import receiver.ReceiverPlan;

/**
 * This changes the receivers' time window durations, by either extending 
 * the time window end time or retracting the time window end time by the 
 * stepSize provided. This class is used when only the upper bound of the
 * receivers' time windows can be changed.
 * 
 * @author jwjoubert, wlbean
 */
public class TimeWindowMutatorV2 implements GenericPlanStrategyModule<ReceiverPlan> {
	final private double stepSize;
	final private double MINIMUM_TIME_WINDOW = Time.parseTime("02:00:00"); 
	final private double MAXIMUM_TIME_WINDOW = Time.parseTime("12:00:00"); 
	
	
	public TimeWindowMutatorV2(double stepSize) {
		this.stepSize = stepSize;
	}

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {
	}

	@Override
	public void handlePlan(ReceiverPlan plan) {
		TimeWindow oldWindow = plan.getTimeWindows().get(0);
		TimeWindow newWindow = wiggleTimeWindow(oldWindow);
		plan.getTimeWindows().remove(oldWindow);
		plan.getTimeWindows().add(newWindow);
	}
	
	/**
	 * Randomly performs a perturbation to the given {@link TimeWindow}. The
	 * perturbations include increasing or decreasing the {@link TimeWindow}'s
	 * end time.
	 *  
	 * @param tw
	 * @return
	 */
	public TimeWindow wiggleTimeWindow(TimeWindow tw) {
		int move = MatsimRandom.getLocalInstance().nextInt(2);
		switch (move) {
		case 0:
			return extendTimeWindowUpwards(tw);
		case 1:
			return contractTimeWindowTop(tw);
		default:
			throw new IllegalArgumentException("Cannot wiggle TimeWindow with move type '" + move + "'.");
		}
	}

	/**
	 * Increases the {@link TimeWindow} end time by some random step size that
	 * is no more than the threshold specified at this {@link TimeWindowMutatorV2}'s
	 * instantiation.
	 * 
	 * @param tw
	 * @return
	 */
	public TimeWindow extendTimeWindowUpwards(final TimeWindow tw) {
		double newHigh;

		if ((tw.getEnd() + this.stepSize) - tw.getStart() <= MAXIMUM_TIME_WINDOW){
			newHigh = tw.getEnd() + this.stepSize;
		} else newHigh = tw.getStart() + MAXIMUM_TIME_WINDOW ;
		
		if (newHigh < Time.parseTime("18:00:00")){
			return TimeWindow.newInstance(tw.getStart(), newHigh);
		}
		else {
			return TimeWindow.newInstance(tw.getStart(), Time.parseTime("18:00:00"));
		}
		
	}
	
	/**
	 * Decreases the {@link TimeWindow} end time by some random step size that
	 * is no more than the threshold specified at this {@link TimeWindowMutatorV2}'s
	 * instantiation, provided the minimum {@link TimeWindow} width is maintained.
	 * 
	 * @param tw
	 * @return
	 */
	public TimeWindow contractTimeWindowTop(final TimeWindow tw) {
		double gap = Math.max(0, (tw.getEnd() - tw.getStart()) - MINIMUM_TIME_WINDOW);
		double step = Math.min(gap, stepSize);
		double newHigh = tw.getEnd() - step;

		if (newHigh < Time.parseTime("18:00:00")){
			return TimeWindow.newInstance(tw.getStart(), newHigh);
		}
		else {
			return TimeWindow.newInstance(tw.getStart(), Time.parseTime("18:00:00"));
		}
		
	}
	

	@Override
	public void finishReplanning() {
		
	}

}
