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
package receiver.controler;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.matsim.api.core.v01.population.HasPlansAndId;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.core.controler.events.ScoringEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.ReplanningListener;
import org.matsim.core.controler.listener.ScoringListener;
import org.matsim.core.replanning.GenericStrategyManager;

import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.Receivers;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;
import receiver.scoring.ReceiverScoringFunctionFactory;
import receiver.tracking.ReceiverTracker;

/**
 * This controller ensures that each receiver receives a cost (score) per order at the end of each iteration and replans its orders based on the cost of the previous iteration and past iterations.
 * 
 * @author wlbean
 *
 */
public class ReceiverControlerListener implements ScoringListener,
ReplanningListener, BeforeMobsimListener {
	
	private Receivers receivers;
	private ReceiverOrderStrategyManagerFactory stratManFac;
	private ReceiverScoringFunctionFactory scorFuncFac;
	private ReceiverTracker tracker;
	@Inject EventsManager eMan;
	
	/**
	 * This creates a new receiver controler listener for receivers with replanning abilities.
	 * @param receivers
	 * @param stratManFac
	 */
	
	@Inject
	ReceiverControlerListener(Receivers receivers, ReceiverOrderStrategyManagerFactory stratManFac, ReceiverScoringFunctionFactory scorFuncFac){
		this.receivers = receivers;
		this.stratManFac = stratManFac;
		this.scorFuncFac = scorFuncFac;
			}


	@Override
	public void notifyReplanning(ReplanningEvent event) {
	
		GenericStrategyManager<ReceiverPlan, Receiver> stratMan = stratManFac.createReceiverStrategyManager();
		
		Collection<HasPlansAndId<ReceiverPlan, Receiver>> receiverCollection = new ArrayList<>();
		
		for(Receiver receiver : receivers.getReceivers().values()){
			receiverCollection.add(receiver);
		}
		
		stratMan.run(receiverCollection, null, event.getIteration(), event.getReplanningContext());		
	}

	
	/*
	 * Determines the order cost at the end of each iteration.
	 */
	
	@Override
	public void notifyScoring(ScoringEvent event) {
	this.tracker.scoreSelectedPlans();
	}

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		tracker = new ReceiverTracker(receivers, scorFuncFac);
		eMan.addHandler(tracker);		
	}

}
