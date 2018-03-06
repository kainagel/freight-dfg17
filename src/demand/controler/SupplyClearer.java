package demand.controler;

import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

import demand.decoratedLSP.LSPDecorators;
import lsp.LSP;
import lsp.LogisticsSolution;
import lsp.LogisticsSolutionElement;

public class SupplyClearer implements IterationEndsListener{
	
	private LSPDecorators lsps;
	
	public SupplyClearer(LSPDecorators lsps) {
		this.lsps = lsps;
	}
					
	@Override
	public void notifyIterationEnds(IterationEndsEvent arg0) {
			for(LSP lsp : lsps.getLSPs().values()){
				lsp.getShipments().clear();
				for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
					solution.getShipments().clear();
					for(LogisticsSolutionElement element : solution.getSolutionElements()) {
						element.getIncomingShipments().clear();
						element.getOutgoingShipments().clear();
					}
				}	
			}		
	}
						
}
