package TienLenGame;

import model.Deck;
import model.Server;
public class ServerTienLen extends Server {
	public ServerTienLen(int n) {
		super("Tien Len Server", n);
	}

	public Deck createDeck() {
		return new TienLenDeck(); 
	}
}
