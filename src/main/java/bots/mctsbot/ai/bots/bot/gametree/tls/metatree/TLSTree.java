package bots.mctsbot.ai.bots.bot.gametree.tls.metatree;

import java.util.ArrayList;
import java.util.List;

import bots.mctsbot.ai.bots.bot.gametree.action.DoNothingAction;
import bots.mctsbot.ai.bots.bot.gametree.tls.SimulatedGame;
import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.AbstractTLSNode;
import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.LeafNode;
import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.RootNode;
import bots.mctsbot.ai.bots.bot.gametree.tls.strategies.selection.SelectionStrategy;
import bots.mctsbot.ai.opponentmodels.OpponentModel;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.common.elements.player.PlayerId;

public abstract class TLSTree {

	public final PlayerId player;
	public final PlayerId bot;
	public final OpponentModel model;

	public PlayerId getPlayer() {
		return player;
	}

	public AbstractTLSNode getRoot() {
		return root;
	}

	public AbstractTLSNode getParent() {
		return parent;
	}

	public final AbstractTLSNode root;
	public final AbstractTLSNode parent;
	private final List<LeafNode> children = new ArrayList<LeafNode>();
	protected GameState gameState;

	public TLSTree(PlayerId player, AbstractTLSNode parent, GameState gameState, PlayerId bot, OpponentModel model) {
		this.player = player;
		this.parent = parent;
		root = new RootNode(this);
		this.gameState = gameState;
		this.bot = bot;
		this.model = model;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public AbstractTLSNode selectChild(SelectionStrategy moveSelectionStrategy) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNbSamples() {
		// TODO Auto-generated method stub
		return null;
	}

	public LeafNode selectRecursively(SimulatedGame game) {
		if (game.gameState.getNextToAct() == player)
			return root.selectRecursively(game);
		else
			return root.selectRecursively(new DoNothingAction(game.gameState, player), game);
	}

	/*
	public LeafNode selectChild() {
		return selectionStrategy.select(this);
	}
	*/

	public List<LeafNode> getChildren() {
		return children;
	}

	public abstract SelectionStrategy getSelectionStrategy();

	public GameState getGameState() {
		return gameState;
	}

}
