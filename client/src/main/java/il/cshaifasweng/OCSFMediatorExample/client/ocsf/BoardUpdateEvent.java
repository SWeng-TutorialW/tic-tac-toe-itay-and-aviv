package il.cshaifasweng.OCSFMediatorExample.client.ocsf;


public class BoardUpdateEvent {
    public final char symbol;
    public final int row;
    public final int col;

    public BoardUpdateEvent(char symbol, int row, int col) {
        this.symbol = symbol;
        this.row = row;
        this.col = col;
    }
}
