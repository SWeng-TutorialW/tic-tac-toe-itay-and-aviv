package il.cshaifasweng.OCSFMediatorExample.client;

import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.BoardUpdateEvent;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.GameOverEvent;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.SymbolAssignedEvent;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.ChangeTurnEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;


public class SimpleClient extends AbstractClient {
	private static SimpleClient client = null;

	private SimpleClient(String host, int port) {
		super(host, port);
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		if(msg.getClass().equals(Warning.class)) {
			EventBus.getDefault().post(new WarningEvent((Warning) msg));
		}
		else if(msg.toString().startsWith("waiting")) {
			System.out.println("Waiting for a second player to connect");
		}
		else if(msg.toString().startsWith("update")) {
			String[] msgParts = msg.toString().split(" ");
			char symbol = msgParts[1].charAt(0);
			int row = Integer.parseInt(msgParts[2]);
			int col = Integer.parseInt(msgParts[3]);

			EventBus.getDefault().post(new BoardUpdateEvent(symbol, row, col));
		}
		else if (msg.toString().startsWith("game_over")) {
			String[] msgParts = msg.toString().split(" ");
			char winner = msgParts[1].charAt(0);  // 'X', 'O', or 'D'

			EventBus.getDefault().post(new GameOverEvent(winner));
		}
		else if (msg.toString().startsWith("symbol")) {
			String[] msgParts = msg.toString().split(" ");
			char assignedSymbol = msgParts[1].charAt(0);

			EventBus.getDefault().post(new SymbolAssignedEvent(assignedSymbol));
		}
		else if (msg.toString().startsWith("turn")) {
			String[] msgParts = msg.toString().split(" ");
			char turn = msgParts[1].charAt(0);

			EventBus.getDefault().post(new ChangeTurnEvent(turn));
		}
		else if (msg.toString().startsWith("game_full")) {
			System.out.println("Game full :(");
		}
	}
	
	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}

}
