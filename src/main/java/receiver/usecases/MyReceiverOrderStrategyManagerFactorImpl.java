/**
 * 
 */
package receiver.usecases;

import org.matsim.core.replanning.GenericPlanStrategy;
import org.matsim.core.replanning.GenericPlanStrategyImpl;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.replanning.selectors.ExpBetaPlanChanger;

import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;

/**
 * @author u04416422
 *
 */
public class MyReceiverOrderStrategyManagerFactorImpl implements ReceiverOrderStrategyManagerFactory {

	@Override
	public GenericStrategyManager<ReceiverPlan, Receiver> createReceiverStrategyManager() {
		final GenericStrategyManager<ReceiverPlan, Receiver> stratMan = new GenericStrategyManager<>();
		stratMan.setMaxPlansPerAgent(5);
		{
			GenericPlanStrategy<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new ExpBetaPlanChanger<ReceiverPlan, Receiver>(1.));
			stratMan.addStrategy(strategy, null, 1.0);

		}
		
		/*TODO:
		 * 1. Consider a "time window wiggle" replanning strategy that searches 
		 *    randomly within the current time window, but also between different
		 *    time windows if there are multiple.
		 * 2. Consider/Add a ConfigModule.
		 */

		return stratMan;
	}
	
}
