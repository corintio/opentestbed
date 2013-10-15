package bots.mctsbot.ai.bots.bot.gametree.tls.nodes;

import java.util.Random;

import bots.mctsbot.ai.bots.bot.gametree.action.ProbabilityAction;
import bots.mctsbot.ai.bots.bot.gametree.action.SearchBotAction;
import bots.mctsbot.ai.bots.bot.gametree.mcts.strategies.backpropagation.SampleWeightedBackPropStrategy;
import bots.mctsbot.ai.bots.bot.gametree.tls.SimulatedGame;
import bots.mctsbot.ai.bots.bot.gametree.tls.metatree.TLSTree;
import bots.mctsbot.ai.bots.bot.gametree.tls.strategies.selection.SelectionStrategy;
import bots.mctsbot.ai.bots.util.RunningStats;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.common.elements.player.PlayerId;

public abstract class AbstractTLSNode {

	protected AbstractTLSNode leftChild;
	protected AbstractTLSNode rightChild;

	private final TLSTree tree;

	protected SampleWeightedBackPropStrategy backPropagationStrategy;

	//protected List<Test> possibleTests = new ArrayList<Test>();

	public AbstractTLSNode(AbstractTLSNode parent, TLSTree tree, RunningStats stats) {
		this.parent = parent;
		this.tree = tree;
		this.backPropagationStrategy = new SampleWeightedBackPropStrategy(stats);
	}

	private final AbstractTLSNode parent;

	public AbstractTLSNode getLeftChild() {
		return leftChild;
	}

	public AbstractTLSNode getRightChild() {
		return rightChild;
	}

	public AbstractTLSNode selectChild(SelectionStrategy strategy) {
		return strategy.select(this);
	}

	public AbstractTLSNode getParent() {
		return parent;
	}

	public void backPropagate(SimulatedGame game) {
		SearchBotAction action = game.peek();
		backPropagationStrategy.onBackPropagate(game.getValue());
		this.getParent().backPropagate(game);
	}

	//	protected boolean isSplit() {
	//		return rightChild != null;
	//	}
	//
	//	/**
	//	 * For now when a node is split, all information down the tree is lost.
	//	 */
	//	public void split() {
	//		leftChild = new LeafNode(getParent(), getTree());
	//		rightChild = new LeafNode(getParent(), getTree());
	//	}

	public int getNbSamples() {
		return backPropagationStrategy.getNbSamples();
	}

	public double getEVStdDev() {
		return backPropagationStrategy.getEVStdDev();
	}

	public double getStdDev() {
		return backPropagationStrategy.getStdDev();
	}

	public double getEV() {
		return backPropagationStrategy.getEV();
	}

	public TLSTree getTree() {
		return tree;
	}

	public ProbabilityAction getLastAction() {
		//TODO:fix implementation
		return null;
	}

	public GameState getGameState() {
		return getTree().getGameState();
	}

	public PlayerId getBot() {
		return getTree().bot;
	}

	public LeafNode selectRecursively(SimulatedGame game) {
		return getTree().getSelectionStrategy().select(this).selectRecursively(game);
	}

	public void replaceNode(LeafNode original, InnerNode replacement) {
		if (getLeftChild() == original)
			leftChild = replacement;
		if (getRightChild() == original)
			rightChild = replacement;
		assert false;
	}

	private final static Random random = new Random();

	public AbstractTLSNode getRandomChild() {
		//TODO fix with opponentmodel
		double randomNumber = random.nextDouble();
		if (randomNumber < 0.5)
			return getLeftChild();
		else
			return getRightChild();
	}

	public abstract LeafNode selectRecursively(SearchBotAction sample, SimulatedGame game);

}
