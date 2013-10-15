package bots.mctsbot.ai.opponentmodels.weka;

import bots.mctsbot.ai.bots.bot.gametree.action.SearchBotAction;

public class Prediction {

	private SearchBotAction action;
	private double probActual;
	private double probHypothesis;

	public Prediction(SearchBotAction action, double probActual, double probHypothesis) {
		//		if (!checkProbability(probActual))
		//			throw new IllegalArgumentException("Incorrect probability of actual action => " + probActual);
		//		if (!checkProbability(probHypothesis))
		//			throw new IllegalArgumentException("Incorrect probability of hypothesis action => " + probHypothesis);

		// TODO: why are probabilities wrong? then put IllegalArgumentException back
		if (!checkProbability(probActual))
			probActual = correctProb(probActual);
		if (!checkProbability(probHypothesis))
			probHypothesis = correctProb(probHypothesis);

		this.action = action;
		this.probActual = probActual;
		this.probHypothesis = probHypothesis;
	}

	private boolean checkProbability(double prob) {
		return (prob >= 0.0 && prob <= 1.0);
	}

	private double correctProb(double prob) {
		//		System.err.println("Probability " + prob + " corrected to " + (prob < 0.0?"0.0":"1.0"));
		if (prob < 0.0)
			return 0.0;
		if (prob > 1.0)
			return 1.0;
		return prob;
	}

	public SearchBotAction getAction() {
		return action;
	}

	public double getTruePositive() {
		return Math.min(probActual, probHypothesis);
	}

	public double getTrueNegative() {
		return Math.min(1 - probActual, 1 - probHypothesis);
	}

	public double getFalsePositive() {
		return Math.max(0, (1 - probActual) - getTrueNegative());
	}

	public double getFalseNegative() {
		return Math.max(0, probActual - getTruePositive());
	}

	@Override
	public String toString() {
		return action + " with probability " + probHypothesis;
	}
}
