package bots.mctsbot.ai.bots.bot.gametree.tls.nodes;

import bots.mctsbot.ai.bots.bot.gametree.tls.SimulatedGame;
import bots.mctsbot.ai.bots.bot.gametree.tls.metatree.TLSTree;
import bots.mctsbot.ai.bots.bot.gametree.tls.tests.Test;
import bots.mctsbot.ai.bots.util.RunningStats;

public class RootNode extends InnerNode {

	public RootNode(TLSTree tree) {
		super(null, tree, null, new RunningStats());
		this.leftChild = new LeafNode(this, this.getTree(), new RunningStats());
	}

	@Override
	public void backPropagate(SimulatedGame game) {
		if (!this.getTree().isRoot()) {
			super.backPropagate(game);
			game.pop();
		}
	}

	@Override
	public AbstractTLSNode getParent() {
		return this.getTree().getParent();
	}

	public void introduceSplit(Test test, LeafNode leftChild, LeafNode rightChild) {
		this.test = test;
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}

}
