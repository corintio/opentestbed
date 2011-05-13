package bots.mctsbot.ai.bots.bot.gametree.action;

import java.rmi.RemoteException;

import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.common.api.lobby.holdemtable.holdemplayer.context.RemoteHoldemPlayerContext;
import bots.mctsbot.common.elements.player.PlayerId;

public class DoNothingAction extends SearchBotAction {

	public DoNothingAction(GameState gameState, PlayerId actor) {
		super(gameState, actor);
	}

	@Override
	public GameState getStateAfterAction() throws GameEndedException, DefaultWinnerException {
		return gameState;
	}

	@Override
	public GameState getUnwrappedStateAfterAction() {
		return gameState;
	}

	@Override
	public void perform(RemoteHoldemPlayerContext context) throws RemoteException, IllegalActionException {
	}

}
