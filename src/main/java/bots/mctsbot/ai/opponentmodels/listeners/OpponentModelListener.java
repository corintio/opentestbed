package bots.mctsbot.ai.opponentmodels.listeners;

import bots.mctsbot.ai.opponentmodels.OpponentModel;
import bots.mctsbot.client.common.gamestate.GameState;
import bots.mctsbot.common.elements.player.PlayerId;

public interface OpponentModelListener {

	public void onGetCheckProbabilities(GameState state, PlayerId actor);

	public void onGetFoldCallRaiseProbabilities(GameState state, PlayerId actor);

	public void onGetShowdownProbilities(GameState state, PlayerId actor);

	public void setOpponentModel(OpponentModel opponentModel);
}