package bots.mctsbot.ai.bots.bot.gametree.tls;

import java.io.IOException;
import java.rmi.RemoteException;

import bots.mctsbot.ai.bots.bot.AbstractBot;
import bots.mctsbot.ai.bots.bot.gametree.action.IllegalActionException;
import bots.mctsbot.ai.bots.bot.gametree.action.SearchBotAction;
import bots.mctsbot.ai.bots.bot.gametree.tls.metatree.RootTree;
import bots.mctsbot.ai.bots.bot.gametree.tls.nodes.LeafNode;
import bots.mctsbot.ai.opponentmodels.OpponentModel;
import bots.mctsbot.ai.opponentmodels.weka.WekaOptions;
import bots.mctsbot.ai.opponentmodels.weka.WekaRegressionModelFactory;
import bots.mctsbot.client.common.GameStateContainer;
import bots.mctsbot.common.api.lobby.holdemtable.holdemplayer.context.RemoteHoldemPlayerContext;
import bots.mctsbot.common.elements.player.PlayerId;

public class TLSBot extends AbstractBot {

	private final int decisionTime;

	public TLSBot(PlayerId botId, GameStateContainer gameStateContainer, RemoteHoldemPlayerContext playerContext, int decisionTime) {
		super(botId, gameStateContainer, playerContext);
		this.decisionTime = decisionTime;
	}

	OpponentModel model;

	@Override
	public void doNextAction() {
		long startTime = System.currentTimeMillis();
		WekaOptions config = new WekaOptions();
		config.setUseOnlineLearning(true);
		model = null;
		try {
			model = WekaRegressionModelFactory.createForZip("bots/mctsbot/ai/opponentmodels/weka/models/model1.zip", config).create(botId);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		RootTree root = new RootTree(botId, gameStateContainer.getGameState(), model);

		do {
			for (int i = 0; i < 34; i++)
				iterate(root);
		} while (System.currentTimeMillis() < startTime + decisionTime);

		SearchBotAction action = root.getBestAction();

		try {
			action.perform(playerContext);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void iterate(RootTree root) {
		SimulatedGame game = new SimulatedGame(gameStateContainer.getGameState(), botId, model);
		LeafNode leaf = root.selectRecursively(game);

		leaf.expand();

		game.simulate();
		leaf.backPropagate(game);
	}
}
