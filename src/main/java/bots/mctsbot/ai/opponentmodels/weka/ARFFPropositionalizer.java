package bots.mctsbot.ai.opponentmodels.weka;

import java.io.IOException;
import java.util.HashMap;

import bots.mctsbot.ai.opponentmodels.weka.instances.InstancesBuilder;
import bots.mctsbot.ai.opponentmodels.weka.instances.PostCheckBetInstances;
import bots.mctsbot.ai.opponentmodels.weka.instances.PostFoldCallRaiseInstances;
import bots.mctsbot.ai.opponentmodels.weka.instances.PreCheckBetInstances;
import bots.mctsbot.ai.opponentmodels.weka.instances.PreFoldCallRaiseInstances;
import bots.mctsbot.ai.opponentmodels.weka.instances.ShowdownInstances;
import bots.mctsbot.common.elements.player.PlayerId;

/**
 * This ARFFPropositionalizer will be called on important GameEvents.
 * It manages the {@link ARFFPlayer}-instances for each villain, creates
 * classified Weka-Instances and delegates to the corresponding {@link ARFFPlayer} 
 *
 */
public class ARFFPropositionalizer extends Propositionalizer {

	//	private final static Logger logger = Logger.getLogger(ARFFPropositionalizer.class);

	private static final String nl = InstancesBuilder.nl;

	private HashMap<Object, ARFFPlayer> arffFiles = new HashMap<Object, ARFFPlayer>();

	private final PreCheckBetInstances preCheckBetInstance;
	private final PostCheckBetInstances postCheckBetInstance;
	private final PreFoldCallRaiseInstances preFoldCallRaiseInstance;
	private final PostFoldCallRaiseInstances postFoldCallRaiseInstance;
	private final ShowdownInstances showdownInstance;

	private final PlayerId bot;

	public ARFFPropositionalizer(PlayerId bot) throws IOException {
		this.preCheckBetInstance = getPreCheckBetInstance();
		this.postCheckBetInstance = getPostCheckBetInstance();
		this.preFoldCallRaiseInstance = getPreFoldCallRaiseInstance();
		this.postFoldCallRaiseInstance = getPostFoldCallRaiseInstance();
		this.showdownInstance = getShowdownInstance();
		this.bot = bot;
	}

	public static PreCheckBetInstances getPreCheckBetInstance() {
		return new PreCheckBetInstances("PreCheckBet", "@attribute betProb real" + nl + "@attribute action {check, bet}" + nl);
	}

	public static PostCheckBetInstances getPostCheckBetInstance() {
		return new PostCheckBetInstances("PostCheckBet", "@attribute betProb real" + nl + "@attribute action {check, bet}" + nl);
	}

	public static PreFoldCallRaiseInstances getPreFoldCallRaiseInstance() {
		return new PreFoldCallRaiseInstances("PreFoldCallRaise", "@attribute foldProb real" + nl + "@attribute callProb real" + nl
				+ "@attribute raiseProb real" + nl + "@attribute action {fold,call,raise}" + nl);
	}

	public static PostFoldCallRaiseInstances getPostFoldCallRaiseInstance() {
		return new PostFoldCallRaiseInstances("PostFoldCallRaise", "@attribute foldProb real" + nl + "@attribute callProb real" + nl
				+ "@attribute raiseProb real" + nl + "@attribute action {fold,call,raise}" + nl);
	}

	public static ShowdownInstances getShowdownInstance() {
		return new ShowdownInstances("Showdown", "@attribute part0Prob real" + nl + "@attribute part1Prob real" + nl + "@attribute part2Prob real" + nl
				+ "@attribute part3Prob real" + nl + "@attribute part4Prob real" + nl + "@attribute part5Prob real" + nl
				+ "@attribute avgPartition {0,1,2,3,4,5}" + nl);
	}

	public ARFFPlayer getARFF(Object actorId) {
		if (arffFiles.containsKey(actorId))
			return arffFiles.get(actorId);
		else {
			throw new IllegalStateException("no arff-player for " + actorId + " registered yet");
		}
	}

	public void addPlayer(Object actorID, ARFFPlayer arffPlayer) {
		if (arffFiles.containsKey(actorID)) {
			throw new IllegalStateException("arffPlayer for actor " + actorID + " registered twice");
		} else {
			arffFiles.put(actorID, arffPlayer);
		}
	}

	@Override
	protected void logFold(Object actorId) {
		if (actorId.equals(bot))
			return;
		if (getRound().equals("preflop")) {
			getARFF(actorId).writePreFoldCallRaise(preFoldCallRaiseInstance.getClassifiedInstance(this, actorId, new Object[] { 1, 0, 0, "fold" }));
		} else {
			getARFF(actorId).writePostFoldCallRaise(postFoldCallRaiseInstance.getClassifiedInstance(this, actorId, new Object[] { 1, 0, 0, "fold" }));
		}
	}

	protected void logFoldProb(Object actorId, Prediction p) {
		if (actorId.equals(bot))
			return;
		if (getRound().equals("preflop")) {
			getARFF(actorId).addPreFoldCallRaisePrediction(p);
		} else {
			getARFF(actorId).addPostFoldCallRaisePrediction(p);
		}
	}

	@Override
	protected void logCall(Object actorId) {
		if (actorId.equals(bot))
			return;
		if (getRound().equals("preflop")) {
			getARFF(actorId).writePreFoldCallRaise(preFoldCallRaiseInstance.getClassifiedInstance(this, actorId, new Object[] { 0, 1, 0, "call" }));
		} else {
			getARFF(actorId).writePostFoldCallRaise(postFoldCallRaiseInstance.getClassifiedInstance(this, actorId, new Object[] { 0, 1, 0, "call" }));
		}
	}

	protected void logCallProb(Object actorId, Prediction p) {
		if (actorId.equals(bot))
			return;
		if (getRound().equals("preflop")) {
			getARFF(actorId).addPreFoldCallRaisePrediction(p);
		} else {
			getARFF(actorId).addPostFoldCallRaisePrediction(p);
		}
	}

	@Override
	protected void logRaise(Object actorId, double raiseAmount) {
		if (actorId.equals(bot))
			return;
		if (getRound().equals("preflop")) {
			getARFF(actorId).writePreFoldCallRaise(preFoldCallRaiseInstance.getClassifiedInstance(this, actorId, new Object[] { 0, 0, 1, "raise" }));
		} else {
			getARFF(actorId).writePostFoldCallRaise(postFoldCallRaiseInstance.getClassifiedInstance(this, actorId, new Object[] { 0, 0, 1, "raise" }));
		}
	}

	protected void logRaiseProb(Object actorId, Prediction p) {
		if (actorId.equals(bot))
			return;
		if (getRound().equals("preflop")) {
			getARFF(actorId).addPreFoldCallRaisePrediction(p);
		} else {
			getARFF(actorId).addPostFoldCallRaisePrediction(p);
		}
	}

	@Override
	protected void logCheck(Object actorId) {
		if (actorId.equals(bot))
			return;
		if (getRound().equals("preflop")) {
			getARFF(actorId).writePreCheckBet(preCheckBetInstance.getClassifiedInstance(this, actorId, new Object[] { 0, "check" }));
		} else {
			getARFF(actorId).writePostCheckBet(postCheckBetInstance.getClassifiedInstance(this, actorId, new Object[] { 0, "check" }));
		}
	}

	protected void logCheckProb(Object actorId, Prediction p) {
		if (actorId.equals(bot))
			return;
		if (getRound().equals("preflop")) {
			getARFF(actorId).addPreCheckBetPrediction(p);
		} else {
			getARFF(actorId).addPostCheckBetPrediction(p);
		}
	}

	@Override
	protected void logBet(Object actorId, double raiseAmount) {
		if (actorId.equals(bot))
			return;
		if (getRound().equals("preflop")) {
			getARFF(actorId).writePreCheckBet(preCheckBetInstance.getClassifiedInstance(this, actorId, new Object[] { 1, "bet" }));
		} else {
			getARFF(actorId).writePostCheckBet(postCheckBetInstance.getClassifiedInstance(this, actorId, new Object[] { 1, "bet" }));
		}
	}

	protected void logBetProb(Object actorId, Prediction p) {
		if (actorId.equals(bot))
			return;
		if (getRound().equals("preflop")) {
			getARFF(actorId).addPreCheckBetPrediction(p);
		} else {
			getARFF(actorId).addPostCheckBetPrediction(p);
		}
	}

	@Override
	protected void logShowdown(Object actorId, double[] partitionDistr) {
		if (actorId.equals(bot))
			return;
		Object[] targets = new Object[partitionDistr.length + 1];
		double avgBucket = 0;
		for (int i = 0; i < partitionDistr.length; i++) {
			targets[i] = partitionDistr[i];
			avgBucket += i * partitionDistr[i];
		}
		targets[partitionDistr.length] = (int) Math.round(avgBucket);
		getARFF(actorId).writeShowdown(showdownInstance.getClassifiedInstance(this, actorId, targets));
	}

	protected void logShowdownProb(Object actorId, Prediction p) {
		if (actorId.equals(bot))
			return;
		getARFF(actorId).addShowdownPrediction(p);
	}
}
