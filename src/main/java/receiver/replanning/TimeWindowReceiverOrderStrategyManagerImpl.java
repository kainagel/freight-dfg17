/**
 * 
 */
package receiver.replanning;

import org.matsim.core.replanning.GenericPlanStrategyImpl;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.replanning.selectors.BestPlanSelector;
import org.matsim.core.replanning.selectors.ExpBetaPlanChanger;
import org.matsim.core.replanning.selectors.KeepSelected;
import org.matsim.core.utils.misc.Time;

import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.replanning.OrderChanger;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;

/**
 * This class implements a receiver reorder strategy that changes the delivery time window of its orders.
 * 
 * @author wlbean
 *
 */
public class TimeWindowReceiverOrderStrategyManagerImpl implements ReceiverOrderStrategyManagerFactory{
	
	public TimeWindowReceiverOrderStrategyManagerImpl(){		
	}

	@Override
	public GenericStrategyManager<ReceiverPlan, Receiver> createReceiverStrategyManager() {
		final GenericStrategyManager<ReceiverPlan, Receiver> stratMan = new GenericStrategyManager<>();
		stratMan.setMaxPlansPerAgent(4);
		
		{
//			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new ExpBetaPlanSelector<ReceiverPlan, Receiver>(1.));
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new BestPlanSelector<ReceiverPlan,Receiver>());
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.85);

		}
		
		/*
		 * Increases or decreases the time window start or time window end times.
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new TimeWindowMutator(Time.parseTime("02:00:00")));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.15);		
		}		
		
		return stratMan;
	}

}
