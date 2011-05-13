package bots.mctsbot.ai.bots.bot.gametree.tls;

import java.util.Random;

import bots.mctsbot.ai.bots.bot.gametree.action.CallAction;
import bots.mctsbot.ai.bots.bot.gametree.action.FoldAction;
import bots.mctsbot.ai.bots.bot.gametree.action.RaiseAction;
import bots.mctsbot.ai.bots.bot.gametree.action.SearchBotAction;
import bots.mctsbot.ai.bots.bot.gametree.search.expander.sampling.RelativeBetDistribution;
import bots.mctsbot.ai.opponentmodels.OpponentModel;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.common.elements.player.PlayerId;
import bots.mctsbot.common.util.Triple;

public class Sampler {

	private OpponentModel model;
	static Random random = new Random();

	public Sampler(OpponentModel model, PlayerId bot) {
		this.model = model;
	}

	public SearchBotAction sample(GameState state) {
		Triple<Double, Double, Double> probs = model.getFoldCallRaiseProbabilities(state, state.getNextToAct());

		double rand = random.nextDouble();

		if (rand < probs.getLeft())
			return new FoldAction(state, state.getNextToAct());
		if (rand < probs.getLeft() + probs.getMiddle())
			return new CallAction(state, state.getNextToAct());

		RelativeBetDistribution dist = new RelativeBetDistribution();
		double sample = dist.inverseCdf(random.nextDouble());

		double minBet = state.getLowerRaiseBound(state.getNextToAct());
		double maxBet = state.getUpperRaiseBound(state.getNextToAct());
		maxBet -= minBet;

		sample *= maxBet;
		sample += minBet;

		return new RaiseAction(state, state.getNextToAct(), (int) Math.round(sample));
	}

	public SearchBotAction sample(GameState state, SearchBotAction lowBound, SearchBotAction highBound) {
		Triple<Double, Double, Double> probs = model.getFoldCallRaiseProbabilities(state, state.getNextToAct());

		double foldProb = probs.getLeft();
		double callProb = probs.getMiddle();
		double raiseProb = probs.getRight();

		if (lowBound instanceof CallAction)
			foldProb = 0;
		if (lowBound instanceof RaiseAction) {
			foldProb = 0;
			callProb = 0;
		}
		//		if (highBound instanceof FoldAction) {
		//			callProb = 0;
		//			raiseProb = 0;
		//		}
		if (highBound instanceof CallAction) {
			raiseProb = 0;
			callProb = 0;
		}

		RelativeBetDistribution dist = new RelativeBetDistribution();
		double minBet = state.getLowerRaiseBound(state.getNextToAct());
		double maxBet = state.getUpperRaiseBound(state.getNextToAct());

		if (highBound instanceof RaiseAction) {
			raiseProb = raiseProb * (1 - (((RaiseAction) highBound).amount - minBet) / (maxBet - minBet));
		}

		double sum = foldProb + callProb + raiseProb;
		foldProb /= sum;
		callProb /= sum;
		raiseProb /= sum;

		double rand = random.nextDouble();

		if (rand < foldProb)
			return new FoldAction(state, state.getNextToAct());
		if (rand < foldProb + callProb)
			return new CallAction(state, state.getNextToAct());

		double lowRaise = minBet;
		double highRaise = maxBet;

		if (lowBound instanceof RaiseAction)
			lowRaise = ((RaiseAction) lowBound).amount;
		if (highBound != null)
			highRaise = ((RaiseAction) highBound).amount;

		lowRaise -= minBet;
		lowRaise /= (maxBet - minBet);

		highRaise -= minBet;
		highRaise /= (maxBet - minBet);

		double lowSample = dist.cdf(lowRaise);
		double highSample = dist.cdf(highRaise);

		rand = random.nextDouble() * (highSample - lowSample) + lowSample;

		double sample = dist.inverseCdf(rand);

		maxBet -= minBet;

		sample *= maxBet;
		sample += minBet;

		return new RaiseAction(state, state.getNextToAct(), (int) Math.round(sample));
	}
}
