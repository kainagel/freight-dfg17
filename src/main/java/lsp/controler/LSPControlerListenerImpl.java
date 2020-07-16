package lsp.controler;


import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.controler.LSPCarrierTracker;
import org.matsim.contrib.freight.events.eventsCreator.LSPEventCreator;
import org.matsim.contrib.freight.controler.LSPFreightControlerListener;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.core.controler.events.ScoringEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ReplanningListener;
import org.matsim.core.controler.listener.ScoringListener;
import org.matsim.core.events.handler.EventHandler;

import lsp.functions.LSPInfo;
import lsp.LSP;
import lsp.LSPPlan;
import lsp.LSPs;
import lsp.LogisticsSolution;
import lsp.LogisticsSolutionElement;
import lsp.replanning.LSPReplanningModule;
import lsp.resources.LSPCarrierResource;
import lsp.scoring.LSPScoringModule;
import lsp.shipment.LSPShipment;


public class LSPControlerListenerImpl implements LSPFreightControlerListener, BeforeMobsimListener, AfterMobsimListener, ScoringListener,
ReplanningListener, IterationEndsListener, IterationStartsListener{

	
	private LSPCarrierTracker carrierResourceTracker;
	private Carriers carriers;    
	private LSPs lsps;
	private LSPReplanningModule replanningModule;
	private LSPScoringModule scoringModule;
	private Collection<LSPEventCreator> creators;
	
	private ArrayList <EventHandler> registeredHandlers;
	
	
	@Inject EventsManager eventsManager;
	@Inject Network network;

	
	@Inject LSPControlerListenerImpl( LSPs lsps, LSPReplanningModule replanningModule, LSPScoringModule scoringModule, Collection<LSPEventCreator> creators ) {
	        this.lsps = lsps;
	        this.replanningModule = replanningModule;
	        this.scoringModule = scoringModule;
	        this.creators = creators;
	        this.carriers = getCarriers();
	}
	
	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		
		LSPRescheduler rescheduler = new LSPRescheduler(lsps);
		rescheduler.notifyBeforeMobsim(event);
		
		carrierResourceTracker = new LSPCarrierTracker(carriers, network, this, creators);
		eventsManager.addHandler(carrierResourceTracker);
		registeredHandlers = new ArrayList<EventHandler>();
		
		for(LSP lsp : lsps.getLSPs().values()) {
			for(LSPShipment shipment : lsp.getShipments()) {
				for(EventHandler handler : shipment.getEventHandlers()) {
					eventsManager.addHandler(handler);
				}
			}
			LSPPlan selectedPlan = lsp.getSelectedPlan();
				for(LogisticsSolution solution : selectedPlan.getSolutions()) {
					for(EventHandler handler : solution.getEventHandlers()) {
						eventsManager.addHandler(handler);
					}
					for(LogisticsSolutionElement element : solution.getSolutionElements()) {
						for(EventHandler handler : element.getEventHandlers()) {
							eventsManager.addHandler(handler);
						}	
						ArrayList <EventHandler> resourceHandlers = (ArrayList<EventHandler>)element.getResource().getEventHandlers();
							for(EventHandler handler : resourceHandlers) {
								if(!registeredHandlers.contains(handler)) {
									eventsManager.addHandler(handler);
									registeredHandlers.add(handler);
								}
							}
						}
					}		
			}
	}
	
	
	//Hier muss noch die Moeglichkeit reinkommen, dass nicht alle LSPs nach jeder Iteration neu planen, sondern nur ein Teil von denen
	//Das kann durch ein entsprechendes replanningModule erreicht werden. Hier muss man dann nix aendern
	@Override
	public void notifyReplanning(ReplanningEvent event) {
		replanningModule.replanLSPs(event);	
	}

	@Override
	public void notifyScoring(ScoringEvent event) {
		scoringModule.scoreLSPs(event);	
	}

	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		eventsManager.removeHandler(carrierResourceTracker);
		
		ArrayList<LSPSimulationTracker> alreadyUpdatedTrackers = new ArrayList<LSPSimulationTracker>();
		for(LSP lsp : lsps.getLSPs().values()) {
			for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
				for(LogisticsSolutionElement element : solution.getSolutionElements()) {
					for( LSPSimulationTracker tracker : element.getResource().getSimulationTrackers()) {
						if(!alreadyUpdatedTrackers.contains(tracker)) {
							tracker.notifyAfterMobsim(event);
							alreadyUpdatedTrackers.add(tracker);
						}
					}
					for( LSPSimulationTracker tracker : element.getSimulationTrackers()) {
						tracker.notifyAfterMobsim(event);
					}
				}
				for( LSPSimulationTracker tracker : solution.getSimulationTrackers()) {
					tracker.notifyAfterMobsim(event);
				}
			}
		}
	
		for(LSP lsp : lsps.getLSPs().values()) {
			for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
				for(LogisticsSolutionElement element : solution.getSolutionElements()) {
					for(LSPInfo info : element.getInfos()) {
						info.update();
					}
				}
				for(LSPInfo info : solution.getInfos()) {
					info.update();
				}			
			}
		}
	}

	
	private Carriers getCarriers() {
		Carriers carriers = new Carriers();
		for(LSP lsp : lsps.getLSPs().values()) {
			LSPPlan selectedPlan = lsp.getSelectedPlan();
			for(LogisticsSolution solution : selectedPlan.getSolutions()) {
				for(LogisticsSolutionElement element : solution.getSolutionElements()) {
					if(element.getResource() instanceof LSPCarrierResource) {
						
						LSPCarrierResource carrierResource = (LSPCarrierResource) element.getResource();
						Carrier carrier = carrierResource.getCarrier();
						if(!carriers.getCarriers().containsKey(carrier.getId())) {
							carriers.addCarrier(carrier);
						}
					}					
				}
			}
		}
		return carriers;
	}

	public void processEvent(Event event){
		   eventsManager.processEvent(event);
	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		
		
	}

	public LSPCarrierTracker getCarrierResourceTracker() {
		return carrierResourceTracker;
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		if(event.getIteration() > 0) {
			for(EventHandler handler : registeredHandlers) {
				eventsManager.removeHandler(handler);
			}
		
			for(LSP lsp : lsps.getLSPs().values()) {
				for(LSPShipment shipment : lsp.getShipments()) {
					shipment.getEventHandlers().clear();
				}
			
				for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
					for(EventHandler handler : solution.getEventHandlers()) {
						handler.reset(event.getIteration());
					}
					for( LSPSimulationTracker tracker : solution.getSimulationTrackers()) {
						tracker.reset();
					}			
					for(LogisticsSolutionElement element : solution.getSolutionElements()) {
						for(EventHandler handler : element.getEventHandlers()) {
							handler.reset(event.getIteration());
						}
						for( LSPSimulationTracker tracker : element.getSimulationTrackers()) {
							tracker.reset();
						}
						for(EventHandler handler : element.getResource().getEventHandlers()) {
							handler.reset(event.getIteration());
						}		
						for( LSPSimulationTracker tracker : element.getResource().getSimulationTrackers()) {
							tracker.reset();
						}
					}			
				}		
			}			
		}	
	}	
}
