package bots.mctsbot.ai.bots.bot.gametree.tls.metatree;

import bots.mctsbot.ai.bots.bot.gametree.action.SearchBotAction;
import bots.mctsbot.ai.bots.bot.gametree.tls.SimulatedGame;
import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.LeafNode;
import bots.mctsbot.ai.opponentmodels.OpponentModel;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.common.elements.player.PlayerId;

public class RootTree extends DecisionTree {

	public RootTree(PlayerId player, GameState gameState, OpponentModel model) {
		super(player, null, gameState, model);
		// TODO Auto-generated constructor stub
	}

	public SearchBotAction getBestAction() {
		// TODO Auto-generated method stub
		return null;
	}

	public LeafNode selectRecursively(SimulatedGame game) {
		return super.selectRecursively(game);
	}

}
