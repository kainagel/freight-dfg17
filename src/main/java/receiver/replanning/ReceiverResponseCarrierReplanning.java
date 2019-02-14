/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
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
package receiver.replanning;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlWriterV2;
import org.matsim.contrib.freight.carrier.CarrierShipment;
import org.matsim.contrib.freight.jsprit.MatsimJspritFactory;
import org.matsim.contrib.freight.jsprit.NetworkBasedTransportCosts;
import org.matsim.contrib.freight.jsprit.NetworkRouter;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.utils.io.IOUtils;
import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.ReceiverUtils;
import receiver.ReceiversWriter;
import receiver.collaboration.CollaborationUtils;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

import javax.inject.Inject;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

public class ReceiverResponseCarrierReplanning implements IterationStartsListener {
    @Inject private Scenario sc;

    public ReceiverResponseCarrierReplanning(){
    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        // replan only in replanning iteration:
        if(event.getIteration() % ReceiverUtils.getReplanInterval( sc ) != 0) {
            return;
        }

        // Adds the receiver agents that are part of the current (sub)coalition.
        CollaborationUtils.setCoalitionFromReceiverAttributes( sc );

        // clean out plans, services, shipments from carriers:
        Map<Id<Carrier>, Carrier> carriers = ReceiverUtils.getCarriers( sc ).getCarriers();
        for( Carrier carrier : carriers.values() ){
            carrier.clearPlans();
            carrier.getShipments().clear();
            carrier.getServices().clear();
        }

        // re-fill the carriers from the receiver orders:
        Map<Id<Receiver>, Receiver> receivers = ReceiverUtils.getReceivers( sc ).getReceivers();
        int nn = 0 ;
        for( Receiver receiver : receivers.values() ){
            ReceiverPlan receiverPlan = receiver.getSelectedPlan();
            for( ReceiverOrder receiverOrder : receiverPlan.getReceiverOrders() ){
                for( Order order : receiverOrder.getReceiverProductOrders() ){
                    nn++ ;
                    CarrierShipment.Builder builder = CarrierShipment.Builder.newInstance(
                            Id.create("Order" + receiverPlan.getReceiver().getId().toString() + nn, CarrierShipment.class),
                            order.getProduct().getProductType().getOriginLinkId(),
                            order.getReceiver().getLinkId(),
                            (int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity())) );
                    CarrierShipment newShipment = builder
                            .setDeliveryServiceTime( order.getServiceDuration() )
                            .setDeliveryTimeWindow( receiverPlan.getTimeWindows().get( 0 ) )
                            // TODO This only looks at the FIRST time window. This may need revision once we handle multiple
                            // time windows.
                            .build();
                    if (newShipment.getSize() != 0) {
                        receiverOrder.getCarrier().getShipments().add(newShipment );
                    }
                }
            }
        }

        for( Carrier carrier : carriers.values() ){
            // for all carriers, re-run jsprit:

            VehicleRoutingProblem.Builder vrpBuilder = MatsimJspritFactory.createRoutingProblemBuilder(carrier, sc.getNetwork());

            NetworkBasedTransportCosts netBasedCosts = NetworkBasedTransportCosts.Builder.newInstance(sc.getNetwork(), carrier.getCarrierCapabilities().getVehicleTypes()).build();
            VehicleRoutingProblem vrp = vrpBuilder.setRoutingCost(netBasedCosts).build();

            //read and create a pre-configured algorithms to solve the vrp
            URL algoConfigFileName = IOUtils.newUrl( sc.getConfig().getContext(), "initialPlanAlgorithm.xml" );
            VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, algoConfigFileName);

            //solve the problem
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

            //create a new carrierPlan from the best solution
            CarrierPlan newPlan = MatsimJspritFactory.createPlan(carrier, Solutions.bestOf( solutions ) );

            //route plan
            NetworkRouter.routePlan(newPlan, netBasedCosts);

            //assign this plan now to the carrier and make it the selected carrier plan
            carrier.setSelectedPlan(newPlan);

        }

        new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( sc ) ).write(sc.getConfig().controler().getOutputDirectory() + "carriers.xml");
        new ReceiversWriter( ReceiverUtils.getReceivers( sc ) ).write(sc.getConfig().controler().getOutputDirectory() + "receivers.xml");

    }
}
