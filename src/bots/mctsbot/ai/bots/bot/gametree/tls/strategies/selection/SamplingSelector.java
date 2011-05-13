package bots.mctsbot.ai.bots.bot.gametree.tls.strategies.selection;

import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.AbstractTLSNode;

public class SamplingSelector extends SelectionStrategy {

	@Override
	public AbstractTLSNode select(AbstractTLSNode node) {
		return node.getRandomChild();
	}

}
