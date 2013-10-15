package bots.mctsbot.ai.opponentmodels.weka;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import bots.mctsbot.ai.bots.bot.gametree.mcts.MCTSBot;
import bots.mctsbot.ai.bots.bot.gametree.mcts.nodes.INode;
import bots.mctsbot.ai.opponentmodels.OpponentModel;
import bots.mctsbot.ai.opponentmodels.listeners.OpponentModelListener;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.client.common.playerstate.PlayerState;
import bots.mctsbot.common.elements.player.PlayerId;
import bots.mctsbot.common.util.Pair;
import bots.mctsbot.common.util.Triple;

/**
 * This OpponentModel delegates to a provided default {@link WekaModel} for its opponent-model.
 * In addition it observes the game and (configured by {@link WekaOptions}) replaces
 * the opponent-model for each villain after enough data has been collected.
 *
 */
public class WekaLearningModel implements OpponentModel {

	protected static final Logger logger = Logger.getLogger(WekaLearningModel.class);

	private PlayerTrackingVisitor permanentVisitor;
	private ActionTrackingVisitor actionTrackingVisitor;
	private final Deque<PlayerTrackingVisitor> visitors = new ArrayDeque<PlayerTrackingVisitor>();

	Map<PlayerId, WekaRegressionModel> opponentModels = new HashMap<PlayerId, WekaRegressionModel>();
	private final WekaRegressionModel defaultModel;
	private final WekaOptions config;

	private final PlayerId bot;

	private final OpponentModelListener[] listeners;
	private INode node;

	public WekaLearningModel(PlayerId botId, WekaRegressionModel defaultModel, WekaOptions config, OpponentModelListener... listeners) {
		this.permanentVisitor = new PlayerTrackingVisitor(this);
		this.visitors.add(permanentVisitor);
		this.defaultModel = defaultModel;
		this.config = config;
		this.bot = botId;
		this.listeners = listeners;
		for (int i = 0; i < listeners.length; i++)
			listeners[i].setOpponentModel(this);
		if (config.useOnlineLearning()) {
			this.actionTrackingVisitor = new ActionTrackingVisitor(this, bot);
		}
	}

	public WekaOptions getConfig() {
		return config;
	}

	// these methods are used by KullbackLeiblerListener
	// TODO: better design (this is messy)
	public Map<PlayerId, WekaRegressionModel> getOpponentModels() {
		return opponentModels;
	}

	public WekaRegressionModel getDefaultModel() {
		return defaultModel;
	}

	public Propositionalizer getCurrentGamePropositionalizer() {
		return visitors.peek().getPropz();
	}

	// *************************************************

	@Override
	public void assumePermanently(GameState gameState) {
		// make sure we have created Models for all players
		Set<PlayerState> seatedPlayers = gameState.getAllSeatedPlayers();
		for (PlayerState playerState : seatedPlayers) {
			getWekaModel(playerState.getPlayerId());
		}
		permanentVisitor.readHistory(gameState);
		if (actionTrackingVisitor != null) {
			actionTrackingVisitor.readHistory(gameState);
		}
	}

	@Override
	public void assumeTemporarily(GameState gameState) {
		PlayerTrackingVisitor root = visitors.peek();
		PlayerTrackingVisitor clonedTopVisitor = root.clone();
		clonedTopVisitor.readHistory(gameState);
		visitors.push(clonedTopVisitor);
	}

	@Override
	public void forgetLastAssumption() {
		visitors.pop();
		// the permanentVisitor should never be popped
		if (visitors.isEmpty()) {
			throw new IllegalStateException("'forgetAssumption' was called more often than 'assumeTemporarily'");
		}
	}

	private WekaRegressionModel getWekaModel(PlayerId actor) {
		WekaRegressionModel model = opponentModels.get(actor);
		if (model == null) {
			model = new WekaRegressionModel(defaultModel);
			if (config.useOnlineLearning() && !actor.equals(bot)) {
				opponentModels.put(actor, model);
				actionTrackingVisitor.getPropz().addPlayer(actor, new ARFFPlayer(actor, model, config, actionTrackingVisitor));
			}
		}
		return model;
	}

	public ARFFPlayer getPlayer(PlayerId actor) {
		if (config.useOnlineLearning() && !actor.equals(bot)) {
			return actionTrackingVisitor.getPropz().getARFF(actor);
		} else
			return null;
	}

	public double getPlayerAccuracy(PlayerId actor) {
		if (config.useOnlineLearning() && !actor.equals(bot)) {
			return actionTrackingVisitor.getAccuracy(actor);
		} else
			return 0.0;
	}

	@Override
	public Pair<Double, Double> getCheckBetProbabilities(GameState gameState, PlayerId actor) {
		for (int i = 0; i < listeners.length; i++)
			listeners[i].onGetCheckProbabilities(gameState, actor);
		return getWekaModel(actor).getCheckBetProbabilities(actor, getCurrentGamePropositionalizer());
	}

	@Override
	public Triple<Double, Double, Double> getFoldCallRaiseProbabilities(GameState gameState, PlayerId actor) {
		for (int i = 0; i < listeners.length; i++)
			listeners[i].onGetFoldCallRaiseProbabilities(gameState, actor);
		return getWekaModel(actor).getFoldCallRaiseProbabilities(actor, getCurrentGamePropositionalizer());
	}

	@Override
	public double[] getShowdownProbabilities(GameState gameState, PlayerId actor) throws UnsupportedOperationException {
		for (int i = 0; i < listeners.length; i++)
			listeners[i].onGetShowdownProbilities(gameState, actor);
		return getWekaModel(actor).getShowdownProbabilities(actor, getCurrentGamePropositionalizer());
	}

	/**
	 * Saves the node with the last move played by {@link MCTSBot}.
	 * Is used to get probabilities of the opponents moves in order
	 * to calculate the accuracy of predictions by the opponentmodel.
	 * @param node INode containing last action by MCTSBot
	 */
	@Override
	public void setChosenNode(INode node) {
		this.node = node;
	}

	@Override
	public INode getChosenNode() {
		return this.node;
	}

	@Override
	public PlayerId getBotId() {
		return bot;
	}
}
