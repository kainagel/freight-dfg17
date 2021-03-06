package example.lsp.lspReplanning;

import java.util.Random;

import lsp.LSP;
import lsp.ShipmentAssigner;
import lsp.shipment.LSPShipment;
import org.matsim.core.gbl.Gbl;

/*package-private*/ class MaybeTodayAssigner implements ShipmentAssigner{

	private LSP lsp;
	private final Random random;
	
	public MaybeTodayAssigner() {
		this.random = new Random(1);
	}
	
	@Override
	public void assignShipment(LSPShipment shipment) {
		boolean assignToday = random.nextBoolean();
		if(assignToday) {
			Gbl.assertIf( lsp.getSelectedPlan().getSolutions().size()==1 );
			lsp.getSelectedPlan().getSolutions().iterator().next().assignShipment(shipment);
		}	
	}

	@Override
	public void setLSP(LSP lsp) {
		this.lsp = lsp;
		
	}

//	@Override
//	public LSP getLSP() {
//		return lsp;
//	}

}
