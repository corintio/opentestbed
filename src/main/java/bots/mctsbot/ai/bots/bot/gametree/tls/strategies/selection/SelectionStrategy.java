package bots.mctsbot.ai.bots.bot.gametree.tls.strategies.selection;

import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.AbstractTLSNode;

public abstract class SelectionStrategy {

	abstract public AbstractTLSNode select(AbstractTLSNode node);

}
