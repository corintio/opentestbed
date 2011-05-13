package bots.mctsbot.ai.bots.bot.gametree.tls;

import java.util.Stack;

import bots.mctsbot.ai.bots.bot.gametree.action.SearchBotAction;
import bots.mctsbot.ai.bots.bot.gametree.rollout.BucketRollOut;
import bots.mctsbot.ai.opponentmodels.OpponentModel;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.common.elements.player.PlayerId;

/**
 * 
 * @author thijs
 * 
 * Class to keep track of the actions taken during selection and simulation. This information is important to update tests during backpropagation.
 *
 */
public class SimulatedGame extends Stack<SearchBotAction> {

	public GameState gameState;
	public final BucketRollOut rollout;
	double value = 0;

	public SimulatedGame(GameState gameState, PlayerId bot, OpponentModel model) {
		this.gameState = gameState;
		this.rollout = new BucketRollOut(gameState, bot, model);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -628544300776476142L;

	public double getValue() {
		return value;
	}

	public void simulate() {
		double stackSize = rollout.botState.getStack();
		value = stackSize + rollout.doRollOut(4);
	}

}
