package bots.mctsbot.ai.opponentmodels.weka;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import bots.mctsbot.ai.bots.bot.gametree.action.BetAction;
import bots.mctsbot.ai.bots.bot.gametree.action.CallAction;
import bots.mctsbot.ai.bots.bot.gametree.action.CheckAction;
import bots.mctsbot.ai.bots.bot.gametree.action.FoldAction;
import bots.mctsbot.ai.bots.bot.gametree.action.RaiseAction;
import bots.mctsbot.ai.bots.bot.gametree.action.SearchBotAction;
import bots.mctsbot.ai.bots.bot.gametree.mcts.MCTSBot;
import bots.mctsbot.ai.bots.bot.gametree.mcts.nodes.INode;
import bots.mctsbot.ai.bots.bot.gametree.mcts.nodes.InnerNode;
import bots.mctsbot.ai.opponentmodels.OpponentModel;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.client.common.gamestate.modifiers.AllInState;
import bots.mctsbot.client.common.gamestate.modifiers.BetState;
import bots.mctsbot.client.common.gamestate.modifiers.CallState;
import bots.mctsbot.client.common.gamestate.modifiers.CheckState;
import bots.mctsbot.client.common.gamestate.modifiers.FoldState;
import bots.mctsbot.client.common.gamestate.modifiers.RaiseState;
import bots.mctsbot.common.elements.player.PlayerId;
import bots.mctsbot.common.util.Util;

import com.google.common.collect.ImmutableList;

/**
 * The ActionTrackingVisitor currently is used to observe the game
 * and delegate important states to an {@link ARFFPropositionalizer}<br>
 */
public class ActionTrackingVisitor extends PlayerTrackingVisitor {

	private final static Logger logger = Logger.getLogger(ARFFPropositionalizer.class);

	private class AccuracyData {
		double truePositive = 0.0;
		double trueNegative = 0.0;
		double falsePositive = 0.0;
		double falseNegative = 0.0;
	}

	private HashMap<PlayerId, AccuracyData> accuracyData;

	public ActionTrackingVisitor(OpponentModel opponentModel, PlayerId bot) {
		super(opponentModel);
		try {
			this.propz = new ARFFPropositionalizer(bot);
			accuracyData = new HashMap<PlayerId, AccuracyData>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ARFFPropositionalizer getPropz() {
		return (ARFFPropositionalizer) this.propz;
	}

	private InnerNode getNode(GameState state) {
		try {
			return (InnerNode) parentOpponentModel.getChosenNode();
		} catch (ClassCastException e) {
			return null;
		}
	}

	public void printAccuracy() {
		for (PlayerId id : accuracyData.keySet()) {
			AccuracyData data = accuracyData.get(id);
			System.out.print(id + "\t" + //" Accuracy : " + 
					(data.trueNegative + data.truePositive) / (data.trueNegative + data.truePositive + data.falseNegative + data.falsePositive));
			System.out.print("\t");
		}
		System.out.println("");
	}

	private Prediction getProbability(GameState gameState) {
		return getProbability(gameState, 0);
	}

	/**
	 * To calculate the accuracy of the opponentmodel we need the probabilities
	 * of considered actions of opponents when the {@link MCTSBot} calculates
	 * the best action for him to take. For this we use the {@link INode} that
	 * contains the action made by the bot. If it is an {@link InnerNode} the
	 * children contain the probabilities of actions the opponent could make.
	 * Probalities of raises are grouped to give a probability for the action
	 * Raise. To consider consecutive actions by opponents we adjust the
	 * {@link INode} to become the correct child, which is the actual action
	 * made by the opponent. In case of a raise, the raise amongst the children
	 * nearest to the actual one is chosen.
	 * The probabilities are returned as a {@link Prediction}.
	 * 
	 * @param gameState
	 *            the {@link GameState} for which we want to calculate the
	 *            probability
	 * @param raiseAmount
	 *            the bet/raise amount, otherwise 0
	 * @return a {@link Prediction} of the considered action.
	 * 
	 * <br>
	 * <br>
	 *         TODO: grouping probalities of raises could be improved.
	 */
	private Prediction getProbability(GameState gameState, double raiseAmount) {
		// This method should only be called after MCTSBot has acted
		if (parentOpponentModel.getChosenNode() == null)
			return null;

		HashMap<Class<?>, SearchBotAction> actions = new HashMap<Class<?>, SearchBotAction>();
		HashMap<Class<?>, Double> probs = new HashMap<Class<?>, Double>();
		Class<?> cProb = null;
		RaiseAction raiseAction = null;
		BetAction betAction = null;
		String errorStr = "";
		InnerNode node = getNode(gameState);
		if (node != null) {
			errorStr = (">-----------------------------");
			errorStr += ("\n" + getPlayerName(gameState) + " State " + gameState.getClass());
			ImmutableList<INode> children = node.getChildren();
			if (children != null) {
				for (INode n : children) {
					Class<?> c = n.getLastAction().getAction().getClass();
					// Same actions are grouped to make one probability (bet/raise)
					if (!probs.containsKey(c))
						probs.put(c, n.getLastAction().getProbability());
					else
						probs.put(c, n.getLastAction().getProbability() + probs.get(c));
					actions.put(c, n.getLastAction().getAction());

					if (gameState.getClass().equals(n.getLastAction().getAction().getUnwrappedStateAfterAction().getClass()) ||
					// TODO: you shouldn't get BetAction in RaiseState (but it does happen somehow...)
							(gameState.getClass().equals(RaiseState.class) && n.getLastAction().getAction().getClass().equals(BetAction.class))) {// ||
						//						// TODO: idem with Raise-/BetAction in AllinState (now this situation is ignored)
						//						(gameState.getClass().equals(AllInState.class) && 
						//								n.getLastAction().getAction().getClass().equals(BetAction.class))) {
						if (cProb == null) {
							errorStr += "\n Setting chosen node with action " + n.getLastAction().getAction();
							parentOpponentModel.setChosenNode(n);
						}
						cProb = c;
						if (raiseAction == null && c.equals(RaiseAction.class))
							raiseAction = (RaiseAction) n.getLastAction().getAction();
						else if (betAction == null && c.equals(BetAction.class))
							betAction = (BetAction) n.getLastAction().getAction();
					}

					// Correct child node is chosen for bet/raise
					if (cProb != null) {
						if (raiseAction != null && c.equals(RaiseAction.class)) {
							RaiseAction newRaiseAction = (RaiseAction) n.getLastAction().getAction();
							if (Math.abs(newRaiseAction.amount - raiseAmount) < Math.abs(raiseAction.amount - raiseAmount)) {
								raiseAction = newRaiseAction;
								errorStr += "\n Setting chosen node with action " + n.getLastAction().getAction();
								parentOpponentModel.setChosenNode(n);
							}
						} else if (betAction != null && c.equals(BetAction.class)) {
							BetAction newBetAction = (BetAction) n.getLastAction().getAction();
							if (Math.abs(newBetAction.amount - raiseAmount) < Math.abs(betAction.amount - raiseAmount)) {
								betAction = newBetAction;
								errorStr += "\n Setting chosen node with action " + n.getLastAction().getAction();
								parentOpponentModel.setChosenNode(n);
							}
						}
					}
					errorStr += ("\nState " + n.getLastAction().getAction().getUnwrappedStateAfterAction().getClass() + " with action "
							+ n.getLastAction().getAction() + "\t with probability " + (double) Math.round(n.getLastAction().getProbability() * 10000) / 100
							+ "% and totalProb " + (double) Math.round(probs.get(c) * 10000) / 100 + "%");
				}
				errorStr += ("\n> Chosen child with action " + parentOpponentModel.getChosenNode().getLastAction().getAction());
			} else {
				errorStr += ("\nNo children for node with action " + node.getLastAction().getAction());
			}
			errorStr += ("\n-----------------------------<");
		}

		// chosen node of opponentmodel should have changed
		SearchBotAction action = parentOpponentModel.getChosenNode().getLastAction().getAction();
		if (parentOpponentModel.getChosenNode() == node || cProb == null) {
			//			System.err.println(str);
			return null;
		}

		//		System.out.println(">----------------------------");
		//		for (Class<?> c : probs.keySet()) {
		//			if (c.equals(cProb))
		//				assimilatePrediction(new Prediction(action, 1, probs.get(cProb)));
		//			else
		//				assimilatePrediction(new Prediction(actions.get(c), 0, probs.get(c)));
		//		}
		//		System.out.println("-----------------------------<");

		return new Prediction(action, 1, probs.get(cProb));
	}

	private void assimilatePrediction(PlayerId id, Prediction p) {
		if (p == null || p.getAction() == null)
			return;
		//		System.out.println(p + ", TP: " + p.getTruePositive() + ", TN: " + p.getTrueNegative() 
		//			+ ", FP: " + p.getFalsePositive() + ", FN: " + p.getFalseNegative());
		if (!accuracyData.containsKey(id))
			accuracyData.put(id, new AccuracyData());

		AccuracyData data = accuracyData.get(id);
		data.truePositive += p.getTruePositive();
		data.trueNegative += p.getTrueNegative();
		data.falsePositive += p.getFalsePositive();
		data.falseNegative += p.getFalseNegative();
		//		printAccuracy();
	}

	public double getAccuracy(PlayerId id) {
		AccuracyData data = accuracyData.get(id);
		if (data == null)
			return 0.0;
		else
			return (data.trueNegative + data.truePositive) / (data.trueNegative + data.truePositive + data.falseNegative + data.falsePositive);
	}

	@Override
	public void visitCallState(CallState callState) {
		InnerNode node = getNode(callState);
		if (node != null && !callState.getNextToAct().equals(parentOpponentModel.getBotId())) {
			Prediction p = getProbability(callState);
			assimilatePrediction(callState.getNextToAct(), p);
			getPropz().logCallProb(callState.getNextToAct(), p);
			logger.trace(getPlayerName(callState) + " " + p);
		} else {
			logger.trace(getPlayerName(callState) + " CallState");
		}
		propz.signalCall(false, callState.getEvent().getPlayerId());
	}

	@Override
	public void visitRaiseState(RaiseState raiseState) {
		InnerNode node = getNode(raiseState);
		if (node != null && !raiseState.getNextToAct().equals(parentOpponentModel.getBotId())) {
			Prediction p = getProbability(raiseState, raiseState.getLargestBet());
			assimilatePrediction(raiseState.getNextToAct(), p);
			getPropz().logRaiseProb(raiseState.getNextToAct(), p);
			logger.trace(getPlayerName(raiseState) + " Raise " + Util.parseDollars(raiseState.getLargestBet()) + " - with <" + p + ">");
		} else {
			logger.trace(getPlayerName(raiseState) + " RaiseState: " + Util.parseDollars(raiseState.getLargestBet()));
		}
		propz.signalRaise(false, raiseState.getLastEvent().getPlayerId(), raiseState.getLargestBet());
	}

	@Override
	public void visitFoldState(FoldState foldState) {
		InnerNode node = getNode(foldState);
		if (node != null && !foldState.getNextToAct().equals(parentOpponentModel.getBotId())) {
			Prediction p = getProbability(foldState);
			assimilatePrediction(foldState.getNextToAct(), p);
			getPropz().logFoldProb(foldState.getNextToAct(), p);
			logger.trace(getPlayerName(foldState) + " " + p);
		} else {
			logger.trace(getPlayerName(foldState) + " FoldState");
		}
		propz.signalFold(foldState.getEvent().getPlayerId());
	}

	@Override
	public void visitCheckState(CheckState checkState) {
		InnerNode node = getNode(checkState);
		if (node != null && !checkState.getNextToAct().equals(parentOpponentModel.getBotId())) {
			Prediction p = getProbability(checkState);
			assimilatePrediction(checkState.getNextToAct(), p);
			getPropz().logCheckProb(checkState.getNextToAct(), p);
			logger.trace(getPlayerName(checkState) + " " + p);
		} else {
			logger.trace(getPlayerName(checkState) + " CheckState");
		}
		propz.signalCheck(checkState.getEvent().getPlayerId());
	}

	@Override
	public void visitBetState(BetState betState) {
		InnerNode node = getNode(betState);
		if (node != null && !betState.getNextToAct().equals(parentOpponentModel.getBotId())) {
			Prediction p = getProbability(betState, betState.getEvent().getAmount());
			assimilatePrediction(betState.getNextToAct(), p);
			getPropz().logBetProb(betState.getNextToAct(), p);
			logger.trace(getPlayerName(betState) + " Bet " + Util.parseDollars(betState.getEvent().getAmount()) + " - with <" + p + ">");
		} else {
			logger.trace(getPlayerName(betState) + " BetState: " + Util.parseDollars(betState.getEvent().getAmount()));
		}
		propz.signalBet(false, betState.getEvent().getPlayerId(), betState.getEvent().getAmount());
	}

	@Override
	public void visitAllInState(AllInState allInState) {
		InnerNode node = getNode(allInState);
		if (node != null && !allInState.getNextToAct().equals(parentOpponentModel.getBotId())) {
			Prediction p = getProbability(allInState, allInState.getEvent().getMovedAmount());
			assimilatePrediction(allInState.getNextToAct(), p);

			if (p != null) {
				if (p.getAction() instanceof CallAction)
					getPropz().logCallProb(allInState.getNextToAct(), p);
				if (p.getAction() instanceof FoldAction)
					getPropz().logFoldProb(allInState.getNextToAct(), p);
				if (p.getAction() instanceof RaiseAction)
					getPropz().logRaiseProb(allInState.getNextToAct(), p);
				if (p.getAction() instanceof CheckAction)
					getPropz().logCheckProb(allInState.getNextToAct(), p);
				if (p.getAction() instanceof BetAction)
					getPropz().logBetProb(allInState.getNextToAct(), p);
			}

			logger.trace(getPlayerName(allInState) + " All-in " + Util.parseDollars(allInState.getEvent().getMovedAmount()) + " - with <" + p + ">");
		} else {
			logger.trace(getPlayerName(allInState) + " AllInState");
		}
		propz.signalAllIn(allInState.getEvent().getPlayerId(), allInState.getEvent().getMovedAmount());
	}
}