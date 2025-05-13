package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;


public class SimpleServer extends AbstractServer {
	private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();
	private char[][] board = new char[3][3];

	public SimpleServer(int port) {
		super(port);
		for(int i=0; i<3; i++)
			for(int j=0; j<3; j++)
				board[i][j] = ' ';
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		String msgString = msg.toString();
		if(msgString.startsWith("#warning")) {
			Warning warning = new Warning("Warning from server!");

			try {
				client.sendToClient(warning);
				System.out.format("Sent warning to client %s\n", client.getInetAddress().getHostAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(msgString.startsWith("add client")) {
			SubscribedClient connection = new SubscribedClient(client);
			SubscribersList.add(connection);

			try {
				if(SubscribersList.size() == 1) {
					client.sendToClient("waiting for second player...");
				}
				else if(SubscribersList.size() == 2) {
					// Randomly assign 'X' and 'O'
					List<Character> symbols = Arrays.asList('X', 'O');
					Collections.shuffle(symbols);
					char player1_symbol = symbols.get(0);
					char player2_symbol = symbols.get(1);
					SubscribedClient player1 = SubscribersList.get(0);
					SubscribedClient player2 = SubscribersList.get(1);

					Collections.shuffle(symbols);
					char starting = symbols.get(0);

					//player1.setSymbol(symbols.get(0));
					//player2.setSymbol(symbols.get(1));

					player1.getClient().sendToClient("symbol " + symbols.get(0));
					player2.getClient().sendToClient("symbol " + symbols.get(1));

					sendToAllClients("turn " + starting);
				}
				else
					client.sendToClient("game_full");
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else if(msgString.startsWith("remove client")) {
			if(!SubscribersList.isEmpty()){
				for(SubscribedClient subscribedClient: SubscribersList){
					if(subscribedClient.getClient().equals(client)){
						SubscribersList.remove(subscribedClient);
						break;
					}
				}
			}
		}
		else if(msgString.startsWith("place")) {
			String[] msgParts = msgString.split(" ");
			char symbol = msgParts[1].charAt(0);  // 'X' or 'O'
			int row = Integer.parseInt(msgParts[2]);
			int col = Integer.parseInt(msgParts[3]);

			if(this.board[row][col] == ' ') {
				this.board[row][col] = symbol;

				// Check if game is over
				if(isGameOver() != ' ') {
					String gameOverMsg = String.format("game_over %c", isGameOver());
					sendToAllClients(gameOverMsg);
					System.out.println("Sent game over");
				}

				// Change turn
				sendToAllClients("turn " + (symbol == 'X' ? 'O' : 'X'));

				// Send update to all clients so they can update their GUI
				String updateMsg = String.format("update %c %d %d", symbol, row, col);
				sendToAllClients(updateMsg);
				System.out.format("Placed symbol %s in [%d,%d]\n", symbol, row, col);
			}
			else
				System.out.println("Didn't place symbol " + symbol);
		}
	}

	public void sendToAllClients(String message) {
		try {
			for (SubscribedClient subscribedClient : SubscribersList) {
				subscribedClient.getClient().sendToClient(message);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private char isGameOver() {
		// Return values: 'X' = player X won, 'O' = player O won, 'D' = draw, ' ' = game is in progress

		// Check rows and columns
		for(int i = 0; i < 3; i++) {
			// Rows
			if(this.board[i][0] != ' ' && this.board[i][0] == this.board[i][1] && this.board[i][1] == this.board[i][2])
				return this.board[i][0];

			// Columns
			if(this.board[0][i] != ' ' && this.board[0][i] == this.board[1][i] && this.board[1][i] == this.board[2][i])
				return this.board[0][i];
		}

		// Diagonals
		if(this.board[0][0] != ' ' && this.board[0][0] == this.board[1][1] && this.board[1][1] == this.board[2][2])
			return this.board[0][0];

		if(this.board[0][2] != ' ' && this.board[0][2] == this.board[1][1] && this.board[1][1] == this.board[2][0])
			return this.board[0][2];

		// Check if game is in progress
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				if(this.board[i][j] == ' ')
					return ' ';  // Game is still in progress

		return 'D';  // Draw
	}
}
