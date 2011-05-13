package bots.mctsbot.ai.bots.bot.gametree.tls.metatree;

import bots.mctsbot.ai.bots.bot.gametree.action.SearchBotAction;
import bots.mctsbot.ai.bots.bot.gametree.tls.Sampler;
import bots.mctsbot.ai.bots.bot.gametree.tls.SimulatedGame;
import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.AbstractTLSNode;
import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.LeafNode;
import bots.mctsbot.ai.bots.bot.gametree.tls.strategies.selection.SamplingSelector;
import bots.mctsbot.ai.bots.bot.gametree.tls.strategies.selection.SelectionStrategy;
import bots.mctsbot.ai.opponentmodels.OpponentModel;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.common.elements.player.PlayerId;

public class OpponentTree extends TLSTree {

	Sampler sampler = new Sampler(model, bot);

	public OpponentTree(PlayerId player, AbstractTLSNode parent, GameState gameState, PlayerId bot, OpponentModel model) {
		super(player, parent, gameState, bot, model);
		// TODO Auto-generated constructor stub
	}

	private final SelectionStrategy selectionStrategy = new SamplingSelector();

	@Override
	public SelectionStrategy getSelectionStrategy() {
		return selectionStrategy;
	}

	@Override
	public LeafNode selectRecursively(SimulatedGame game) {
		SearchBotAction sample = sampler.sample(game.gameState);
		return root.selectRecursively(sample, game);
	}
}
