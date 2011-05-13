package bots.mctsbot.ai.bots.bot.gametree.tls.nodes;

import java.util.ArrayList;
import java.util.List;

import bots.mctsbot.ai.bots.bot.gametree.action.DefaultWinnerException;
import bots.mctsbot.ai.bots.bot.gametree.action.GameEndedException;
import bots.mctsbot.ai.bots.bot.gametree.action.SearchBotAction;
import bots.mctsbot.ai.bots.bot.gametree.tls.Sampler;
import bots.mctsbot.ai.bots.bot.gametree.tls.SimulatedGame;
import bots.mctsbot.ai.bots.bot.gametree.tls.metatree.DecisionTree;
import bots.mctsbot.ai.bots.bot.gametree.tls.metatree.OpponentTree;
import bots.mctsbot.ai.bots.bot.gametree.tls.metatree.TLSTree;
import bots.mctsbot.ai.bots.bot.gametree.tls.tests.Test;
import bots.mctsbot.ai.bots.util.RunningStats;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.common.elements.player.PlayerId;

public class LeafNode extends AbstractTLSNode {

	public TLSTree childTree;
	private List<Test> possibleTests = null;
	private List<SearchBotAction> samples = new ArrayList<SearchBotAction>();
	Sampler sampler = new Sampler(getTree().model, getBot());

	public LeafNode(AbstractTLSNode parent, TLSTree tree, RunningStats stats) {
		super(parent, tree, stats);
	}

	public void expand() {
		PlayerId nextPlayer = getGameState().getNextSeatedPlayerAfter(getTree().getPlayer()).getPlayerId();

		if (nextPlayer == getBot())
			childTree = new DecisionTree(nextPlayer, this, getGameState(), getTree().model);
		else
			childTree = new OpponentTree(nextPlayer, this, getGameState(), getBot(), getTree().model);
	}

	public LeafNode selectRecursively(GameState state, SimulatedGame game) {
		if (childTree == null)
			return this;

		AbstractTLSNode parent = this.getParent();
		SearchBotAction lowBound = null;
		SearchBotAction highBound = null;
		while (!(parent instanceof RootNode) && (highBound != null || lowBound != null)) {
			if (highBound == null && this == parent.getLeftChild())
				highBound = ((InnerNode) parent).test.getTestAction();
			else if (lowBound == null)
				lowBound = ((InnerNode) parent).test.getTestAction();
		}
		if (highBound == null && this == parent.getLeftChild())
			highBound = ((InnerNode) parent).test.getTestAction();
		else if (lowBound == null)
			lowBound = ((InnerNode) parent).test.getTestAction();

		return this.selectRecursively(sampler.sample(state, lowBound, highBound), game);
	}

	@Override
	public void backPropagate(SimulatedGame game) {
		if (possibleTests == null) {
			samples.add(game.peek());
			if (timeToGenerateTests())
				generateTests();
		}
		if (possibleTests != null) {
			for (Test test : possibleTests) {
				test.updateStats(game.peek(), game.getValue());
			}
			if (needsToBeSplit())
				split();
		}
		super.backPropagate(game);
	}

	private void generateTests() {
		// TODO Auto-generated method stub
		possibleTests = new ArrayList<Test>();
	}

	private boolean timeToGenerateTests() {
		return samples.size() > 10;
	}

	private void split() {
		Test test = getBestTest();
		LeafNode leftChild = new LeafNode(this.getParent(), getTree(), test.getLeftStats());
		LeafNode rightChild = new LeafNode(this.getParent(), getTree(), test.getRightStats());

		if (getParent().getRightChild() == null)
			((RootNode) getParent()).introduceSplit(test, leftChild, rightChild);
		else {
			InnerNode newNode = new InnerNode(getParent(), getTree(), test, this.backPropagationStrategy.getRunningStats());
			newNode.leftChild = leftChild;
			newNode.rightChild = rightChild;
			getParent().replaceNode(this, newNode);
		}
	}

	private Test getBestTest() {
		Test bestTest = possibleTests.get(0);
		for (int i = 1; i < possibleTests.size(); i++) {
			if (possibleTests.get(i).getSDR() > bestTest.getSDR())
				bestTest = possibleTests.get(i);
		}
		return bestTest;
	}

	private boolean needsToBeSplit() {
		return getNbSamples() > 30;
	}

	@Override
	public LeafNode selectRecursively(SearchBotAction sample, SimulatedGame game) {
		game.push(sample);
		if (childTree == null)
			return this;
		try {
			game.gameState = sample.getStateAfterAction();
			return childTree.selectRecursively(game);
		} catch (GameEndedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DefaultWinnerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IllegalArgumentException();
	}
}
