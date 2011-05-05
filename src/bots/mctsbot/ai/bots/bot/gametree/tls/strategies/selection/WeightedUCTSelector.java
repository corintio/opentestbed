package bots.mctsbot.ai.bots.bot.gametree.tls.strategies.selection;

import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.AbstractTLSNode;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.client.common.playerstate.PlayerState;
import bots.mctsbot.common.elements.player.PlayerId;

public class WeightedUCTSelector extends SelectionStrategy {

	private final double C;

	public WeightedUCTSelector(double C) {
		this.C = C;
	}

	@Override
	public AbstractTLSNode select(AbstractTLSNode node) {
		AbstractTLSNode leftChild = node.getLeftChild();
		double leftValue = evaluate(leftChild);

		AbstractTLSNode rightChild = node.getRightChild();
		if (rightChild == null)
			return leftChild;

		if (evaluate(rightChild) >= leftValue)
			return rightChild;

		return leftChild;
	}

	protected double evaluate(AbstractTLSNode node) {
		int nbSamples = node.getNbSamples();
		if (nbSamples == 0)
			return 0;
		int nbParentSamples = node.getParent().getNbSamples();
		return node.getEV() + getC2(node) * Math.sqrt(Math.log(nbParentSamples) / nbSamples);
	}

	// C2 = C * (maxProfit - maxLoss)  maxLoss is negative
	private double getC2(AbstractTLSNode node) {
		GameState gameState = node.getGameState();
		PlayerId bot = node.getBot();

		//wrong assumption if bot is already all-in and doesn't match opponents bets
		int maxProfit = gameState.getGamePotSize();

		int botStack = gameState.getPlayer(bot).getStack();

		int maxOpponentStack = 0;

		for (PlayerState playerState : gameState.getAllSeatedPlayers()) {
			if (playerState.getPlayerId() != bot && !playerState.hasFolded()) {
				int stack = playerState.getStack();
				// from each player you can win nothing more than the minimum of his stack and yours
				maxProfit += Math.min(botStack, stack);

				maxOpponentStack = Math.max(maxOpponentStack, stack);
			}
		}

		int maxLoss = Math.min(botStack, maxOpponentStack);

		//we add up because we took the maxLoss positive here
		return C * (maxProfit + maxLoss);
	}

}
