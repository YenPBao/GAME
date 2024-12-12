package TienLenGame;

import javax.swing.JOptionPane;
import model.BotClient;
import model.Client;

public class Game {
    public static void main(String[] args) {
        String input = JOptionPane.showInputDialog(null, "Nhập số lượng người chơi:");
        int numberOfPlayers = 0;
        try {
            numberOfPlayers = Integer.parseInt(input); 
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập một số nguyên hợp lệ.");
            return; 
        }

        String input2 = JOptionPane.showInputDialog(null, "Nhập số lượng máy chơi:");
        int numberOfBots = 0;
        try {
            numberOfBots = Integer.parseInt(input2); 
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập một số nguyên hợp lệ.");
            return; 
        }

        ServerTienLen server = new ServerTienLen(numberOfPlayers);
        new Thread(() -> server.start(2396)).start();  
        
        Client[] clients = new Client[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers - numberOfBots; i++) {
            clients[i] = new ClientTienLen(numberOfPlayers);
        }

        for (int i = numberOfPlayers - numberOfBots; i < numberOfPlayers; i++) {
           clients[i] = new BotClient(numberOfPlayers);
        }
    }
}
