package bots.mctsbot.ai.bots.bot.gametree.tls.nodes;

import bots.mctsbot.ai.bots.bot.gametree.action.SearchBotAction;
import bots.mctsbot.ai.bots.bot.gametree.tls.SimulatedGame;
import bots.mctsbot.ai.bots.bot.gametree.tls.metatree.TLSTree;
import bots.mctsbot.ai.bots.bot.gametree.tls.tests.Test;
import bots.mctsbot.ai.bots.util.RunningStats;

public class InnerNode extends AbstractTLSNode {

	public Test test;

	public InnerNode(AbstractTLSNode abstractTLSNode, TLSTree tree, Test test, RunningStats stats) {
		super(abstractTLSNode, tree, stats);
		this.test = test;
	}

	@Override
	public LeafNode selectRecursively(SearchBotAction sample, SimulatedGame game) {
		if (test.succeeds(sample))
			return rightChild.selectRecursively(sample, game);
		else
			return leftChild.selectRecursively(sample, game);
	}

}
