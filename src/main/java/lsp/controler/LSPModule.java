package lsp.controler;

import java.util.Collection;

import org.matsim.contrib.freight.CarrierConfig;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.name.Named;

import lsp.LSPs;
import lsp.events.EventCreator;
import lsp.mobsim.CarrierResourceTracker;
import lsp.mobsim.FreightQSimFactory;
import lsp.replanning.LSPReplanningModule;
import lsp.scoring.LSPScoringModule;


public class LSPModule extends AbstractModule {

	
	private LSPs lsps;
	private LSPReplanningModule replanningModule;
	private LSPScoringModule scoringModule;
	private Collection<EventCreator> creators;
	
	private CarrierConfig carrierConfig = new CarrierConfig();
	
	public LSPModule(LSPs  lsps, LSPReplanningModule replanningModule, LSPScoringModule scoringModule, Collection<EventCreator> creators) {
	   this.lsps = lsps;
	   this.replanningModule = replanningModule;
	   this.scoringModule = scoringModule;
	   this.creators = creators;
	}    
	   	  
		    
	@Override
	public void install() {
		bind(CarrierConfig.class).toInstance(carrierConfig);
		bind(LSPs.class).toInstance(lsps);
        if(replanningModule != null) {
        	bind(LSPReplanningModule.class).toInstance(replanningModule);
        }
		if(scoringModule != null) {
			 bind(LSPScoringModule.class).toInstance(scoringModule);
		}
		
		bind(LSPControlerListener.class).asEagerSingleton();
        addControlerListenerBinding().to(LSPControlerListener.class);
        bindMobsim().toProvider(FreightQSimFactory.class);
        addControlerListenerBinding().to(FreightEventsHandlingImpl.class);
	}

	@Provides
	Collection<EventCreator> provideEventCreators(){
		return this.creators;
	}
	
	@Provides
    CarrierResourceTracker provideCarrierResourceTracker(LSPControlerListener lSPControlerListener) {
        return lSPControlerListener.getCarrierResourceTracker();
    }

	@Provides @Named("Freight")
	EventsManager  provideEventsManager(LSPControlerListener lSPControlerListener) {
		 return lSPControlerListener.getFreightEventsManager();
	}
	
    public void setPhysicallyEnforceTimeWindowBeginnings(boolean physicallyEnforceTimeWindowBeginnings) {
        this.carrierConfig.setPhysicallyEnforceTimeWindowBeginnings(physicallyEnforceTimeWindowBeginnings);
    }

}
