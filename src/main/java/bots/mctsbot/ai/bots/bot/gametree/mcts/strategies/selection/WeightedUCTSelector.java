/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package bots.mctsbot.ai.bots.bot.gametree.mcts.strategies.selection;

import bots.mctsbot.ai.bots.bot.gametree.mcts.nodes.INode;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.client.common.playerstate.PlayerState;
import bots.mctsbot.common.elements.player.PlayerId;

public class WeightedUCTSelector extends MaxFunctionSelector {

	private final double C;

	public WeightedUCTSelector(double C) {
		this.C = C;
	}

	@Override
	protected double evaluate(INode node) {
		int nbSamples = node.getNbSamples();
		if (nbSamples == 0)
			return 0;
		int nbParentSamples = node.getParent().getNbSamples();
		return node.getEV() + getC2(node) * Math.sqrt(Math.log(nbParentSamples) / nbSamples);
	}

	// C2 = C * (maxProfit - maxLoss)  maxLoss is negative
	private double getC2(INode node) {
		GameState gameState = node.getGameState();
		PlayerId bot = node.getParent().bot;

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
