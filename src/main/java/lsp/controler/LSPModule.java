package lsp.controler;

import com.google.inject.Provides;
import lsp.LSPs;
import lsp.events.EventCreator;
import lsp.replanning.LSPReplanningModule;
import lsp.scoring.LSPScoringModule;
import org.matsim.contrib.freight.FreightConfigGroup;
import org.matsim.core.controler.AbstractModule;

import java.util.Collection;


public class LSPModule extends AbstractModule {

	
	private LSPs lsps;
	private LSPReplanningModule replanningModule;
	private LSPScoringModule scoringModule;
	private Collection<EventCreator> creators;
	
	private FreightConfigGroup carrierConfig = new FreightConfigGroup();
	
	public LSPModule(LSPs  lsps, LSPReplanningModule replanningModule, LSPScoringModule scoringModule, Collection<EventCreator> creators) {
	   this.lsps = lsps;
	   this.replanningModule = replanningModule;
	   this.scoringModule = scoringModule;
	   this.creators = creators;
	}    
	   	  
		    
	@Override
	public void install() {
		bind(FreightConfigGroup.class).toInstance(carrierConfig);
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
	}

	@Provides
	Collection<EventCreator> provideEventCreators(){
		return this.creators;
	}
	
	@Provides
    CarrierResourceTracker provideCarrierResourceTracker(LSPControlerListener lSPControlerListener) {
        return lSPControlerListener.getCarrierResourceTracker();
    }

}
