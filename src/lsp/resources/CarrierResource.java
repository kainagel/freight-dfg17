package lsp.resources;

import org.matsim.contrib.freight.carrier.Carrier;

/* Das ist nicht so, wie man es machen sollte, weil hier inheritance vor Composition geht
 * Sp�ter noch verbessern!
 */
public interface CarrierResource extends Resource{

	public Carrier getCarrier();
	
}
