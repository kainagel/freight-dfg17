package lspScoringTests;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.events.handler.EventHandler;

import lsp.functions.Info;
import lsp.functions.InfoFunctionValue;
import tracking.SimulationTracker;

public class TrinkgeldSimulationTracker implements SimulationTracker {

	private TrinkgeldEventHandler handler;
	private Info info;
	
	public TrinkgeldSimulationTracker(TrinkgeldEventHandler handler, Info info) {
		this.info = info;
		this.handler = handler;
	}
	
	@Override
	public Collection<EventHandler> getEventHandlers() {
		ArrayList<EventHandler> handlers = new ArrayList<EventHandler>();
		handlers.add(handler);
		return handlers;
	}

	@Override
	public Collection<Info> getInfos() {
		ArrayList<Info> infos = new ArrayList<Info>();
		infos.add(info);
		return infos;
	}

	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		double trinkgeld = handler.getTrinkgeld();
		InfoFunctionValue  value = info.getFunction().getValues().iterator().next();
		value.setValue(String.valueOf(trinkgeld));
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}

	
}
