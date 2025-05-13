package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.BoardUpdateEvent;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.GameOverEvent;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.SymbolAssignedEvent;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.ChangeTurnEvent;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class PrimaryController {
	@FXML private Button btn00, btn01, btn02, btn10, btn11, btn12, btn20, btn21, btn22;
	private Button[][] boardButtons;
	private char mySymbol;
	private char turn;

    @FXML
    void sendWarning(ActionEvent event) {
    	try {
			SimpleClient.getClient().sendToServer("#warning");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@FXML
	void sendPlaceMessage(ActionEvent event) {
		Button clickedButton = (Button) event.getSource();
		Integer row = GridPane.getRowIndex(clickedButton);
		Integer col = GridPane.getColumnIndex(clickedButton);

		if(row == null || col == null || row < 0 || col < 0 || row > 2 || col > 2) {
			System.out.println("Could not determine row/column of clicked button");
			return;
		}

		if(this.mySymbol != this.turn) {  // It is probably better to check it in the server's side
			System.out.println("Not your turn!");
			return;
		}

		try {
			String msg = String.format("place %c %d %d", this.mySymbol, row, col);
			SimpleClient.getClient().sendToServer(msg);
			System.out.println("Sent message: " + msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Subscribe
	public void onSymbolAssigned(SymbolAssignedEvent event) {
		this.mySymbol = event.symbol;
		boolean isMyTurn = (this.mySymbol == this.turn);

		System.out.println("Assigned as player " + mySymbol);
	}

	@Subscribe
	public void onChangeTurn(ChangeTurnEvent event) {
		this.turn = event.turn;
		boolean isMyTurn = (this.mySymbol == this.turn);

		for(Button[] row : this.boardButtons)
			for(Button button : row)
				if(button.getText().isEmpty())
					button.setDisable(!isMyTurn);

		System.out.println("Player " + turn + "'s turn.");
	}

	@Subscribe
	public void onBoardUpdate(BoardUpdateEvent event) {
		Platform.runLater(() -> {
			boardButtons[event.row][event.col].setText(String.valueOf(event.symbol));
			boardButtons[event.row][event.col].setDisable(true);
		});
	}

	@Subscribe
	public void onGameOver(GameOverEvent event) {
		Platform.runLater(() -> {
			// Show the winner
			String message = switch(event.winner) {
				case 'X' -> "Player X wins!";
				case 'O' -> "Player O wins!";
				case 'D' -> "It's a draw!";
				default  -> "Game over.";
			};

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Game Over");
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.show();

			// Disable all buttons on the board
			for (Button[] row : boardButtons) {
				for (Button btn : row) {
					btn.setDisable(true);
				}
			}
		});
	}

	@FXML
	void initialize(){
		boardButtons = new Button[][] {
				{ btn00, btn01, btn02 },
				{ btn10, btn11, btn12 },
				{ btn20, btn21, btn22 }
		};

		EventBus.getDefault().register(this);

		try {
			SimpleClient.getClient().sendToServer("add client");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
