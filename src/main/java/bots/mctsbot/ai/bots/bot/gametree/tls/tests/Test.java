package bots.mctsbot.ai.bots.bot.gametree.tls.tests;

import bots.mctsbot.ai.bots.bot.gametree.action.BetAction;
import bots.mctsbot.ai.bots.bot.gametree.action.CallAction;
import bots.mctsbot.ai.bots.bot.gametree.action.CheckAction;
import bots.mctsbot.ai.bots.bot.gametree.action.DoNothingAction;
import bots.mctsbot.ai.bots.bot.gametree.action.FoldAction;
import bots.mctsbot.ai.bots.bot.gametree.action.RaiseAction;
import bots.mctsbot.ai.bots.bot.gametree.action.SearchBotAction;
import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.AbstractTLSNode;
import bots.mctsbot.ai.bots.util.RunningStats;

public class Test {

	private static final Class[] line = { DoNothingAction.class, FoldAction.class, CheckAction.class, CallAction.class, BetAction.class, RaiseAction.class };
	private final SearchBotAction testAction;
	private final int testIndex;

	private final RunningStats failStats = new RunningStats();
	private final RunningStats successStats = new RunningStats();

	private final AbstractTLSNode node;

	public Test(SearchBotAction action, AbstractTLSNode node) {
		this.testAction = action;
		testIndex = findIndex(action);
		this.node = node;
	}

	public SearchBotAction getTestAction() {
		return testAction;
	}

	public void updateStats(SearchBotAction action, double value) {
		if (succeeds(action))
			successStats.add(value);
		else
			failStats.add(value);
	}

	public boolean succeeds(SearchBotAction action) {
		if (findIndex(action) < testIndex)
			return false;
		if (testAction instanceof BetAction && action instanceof BetAction)
			return ((BetAction) action).amount >= ((BetAction) testAction).amount;
		if (testAction instanceof RaiseAction && action instanceof RaiseAction)
			return ((RaiseAction) action).amount >= ((RaiseAction) testAction).amount;
		return true;
	}

	private int findIndex(SearchBotAction action) {
		for (int i = 0; i < line.length; i++) {
			if (line[i] == action.getClass())
				return i;
		}
		throw new IllegalArgumentException("The runtime class of the argument does not match any class in the action line");
	}

	public double getSDR() {
		return node.getStdDev() - failStats.getNbSamples() / node.getNbSamples() * failStats.getStdDev() - successStats.getNbSamples() / node.getNbSamples()
				* successStats.getStdDev();
	}

	public RunningStats getLeftStats() {
		return failStats;
	}

	public RunningStats getRightStats() {
		return successStats;
	}
}
