/* *********************************************************************** *
// * project: org.matsim.*
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

package receiver;

import org.apache.log4j.Logger;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;

import receiver.replanning.*;
import receiver.usecases.UsecasesReceiverScoringFunctionFactory;
import receiver.usecases.capetown.CapeTownReceiverOrderStrategyManagerImpl;

public final class ReceiverModule extends AbstractModule {
    final private static Logger LOG = Logger.getLogger(ReceiverModule.class);
    private ReceiverReplanningType replanningType = null;
    private Boolean createPNG = true;

    public ReceiverModule() {
    }

    @Override
    public void install() {
        /* ConfigGroup */
        ReceiverConfigGroup configGroup;
        if (!this.getConfig().getModules().containsKey(ReceiverConfigGroup.NAME)) {
            configGroup = new ReceiverConfigGroup(ReceiverConfigGroup.NAME);
            this.getConfig().addModule(configGroup);
        }
        configGroup = ConfigUtils.addOrGetModule(this.getConfig(), ReceiverConfigGroup.NAME, ReceiverConfigGroup.class);

        /* Carrier */
        this.addControlerListenerBinding().to(ReceiverResponseCarrierReplanning.class);


        /* Receiver FIXME at this point the strategies are mutually exclusive. That is, only one allowed. */
        bind(ReceiverScoringFunctionFactory.class).toInstance(new UsecasesReceiverScoringFunctionFactory());

        /* Check defaults */
        if (this.replanningType != null) {
            ReceiverReplanningType currentType = configGroup.getReplanningType();
            if (currentType != this.replanningType) {
                LOG.warn("   Overwriting the receiver replanning: was '"
                        + currentType + "'; now '" + this.replanningType + "'");
                configGroup.setReplanningType(this.replanningType);
            }
        }
        LOG.warn("Receiver replanning: " + configGroup.getReplanningType());
        switch (configGroup.getReplanningType()) {
            case timeWindow:
                bind(ReceiverOrderStrategyManagerFactory.class).toInstance(new TimeWindowReceiverOrderStrategyManagerImpl());
                break;
            case serviceTime:
                bind(ReceiverOrderStrategyManagerFactory.class).toInstance(new ServiceTimeReceiverOrderStrategyManagerImpl());
                break;
            case orderFrequency:
                bind(ReceiverOrderStrategyManagerFactory.class).toInstance(new NumDelReceiverOrderStrategyManagerImpl());
                break;
            case afterHoursTimeWindow:
            	bind(ReceiverOrderStrategyManagerFactory.class).toInstance(new CapeTownReceiverOrderStrategyManagerImpl());
            	break;
            default:
                throw new RuntimeException("No valid (receiver) order strategy manager selected.");
        }
        addControlerListenerBinding().to(ReceiverControlerListener.class);
        //FIXME override the createPNG

        /* Statistics and output */
//        CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", this.createPNG);
        addControlerListenerBinding().to(ReceiverScoreStats.class);
    }


    public boolean isCreatePNG() {
        return this.createPNG;
    }

    public void setCreatePNG(boolean createPNG) {
        this.createPNG = createPNG;
    }

    public void setReplanningType(ReceiverReplanningType type) {
        this.replanningType = type;
    }
}


