package bots.mctsbot.ai.bots.bot.gametree.tls.metatree;

import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.AbstractTLSNode;
import bots.mctsbot.ai.bots.bot.gametree.tls.strategies.selection.SelectionStrategy;
import bots.mctsbot.ai.bots.bot.gametree.tls.strategies.selection.WeightedUCTSelector;
import bots.mctsbot.ai.opponentmodels.OpponentModel;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.common.elements.player.PlayerId;

public class DecisionTree extends TLSTree {

	public DecisionTree(PlayerId player, AbstractTLSNode parent, GameState gameState, OpponentModel model) {
		super(player, parent, gameState, player, model);
		// TODO Auto-generated constructor stub
	}

	private final SelectionStrategy selectionStrategy = new WeightedUCTSelector(108.6957);

	@Override
	public SelectionStrategy getSelectionStrategy() {
		return selectionStrategy;
	}

}
