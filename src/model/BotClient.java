package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import TienLenGame.TienLenCard;

public class BotClient extends Client implements Runnable {
    private Random random;

    public BotClient(int n) {
        super(n, true); // Gọi constructor của Client với isBot=true
        random = new Random();
        this.table.visible();
    }

    @Override
    public void run() {
        System.out.println("BotClient thread started for bot: " + this.getPlayerName());
    }

    @Override
    public void parseMessage(GameMessage message) {
    	if (message.getType() == Message.YOUR_TURN) {
    	    int turnPlayerID = message.getPlayerID();
    	    System.out.println("Đến lượt " + turnPlayerID);

    	    // Kiểm tra xem có đúng lượt bot không
    	    if (turnPlayerID == this.getPlayerID()) {
    	        makeMoveAutomatically();
    	    } 
    	} else {
            super.parseMessage(message); // Xử lý các thông điệp khác như người thật
        }
    }

    public void makeMoveAutomatically() {
        // Giả lập thời gian suy nghĩ
        try {
            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<Hand> validMoves = getValidMoves();

        // Xác định lastHandOnTable
        Hand lastHandOnTable = null;
        int numOfHandsPlayed = this.getHandsOnTable().size();
        if (numOfHandsPlayed > 0) {
            lastHandOnTable = this.getHandsOnTable().get(numOfHandsPlayed - 1);
        }

        Hand selectedMove = null;

        if (lastHandOnTable != null && !lastHandOnTable.isEmpty()) {
            // Bàn không trống, cố gắng tìm nước đi đánh bại lastHandOnTable
            for (Hand hand : validMoves) {
                if (hand.beats(lastHandOnTable)) {
                    selectedMove = hand;
                    break; // Tìm được nước đi đánh bại lastHand
                }
            }

            // Nếu không tìm được nước đi đánh bại lastHand, chọn ngẫu nhiên nếu có
            if (selectedMove == null && !validMoves.isEmpty()) {
                selectedMove = validMoves.get(random.nextInt(validMoves.size()));
            }

        } else {
            // Bàn trống, cố gắng đánh "3 Bích" (Card(0,0))
            boolean foundThreeBich = false;
            for (Hand hand : validMoves) {
                if (hand.contains(new Card(0,0))) {
                    selectedMove = hand;
                    foundThreeBich = true;
                    break;
                }
            }

            // Nếu không tìm thấy nước đi chứa "3 Bích", chọn ngẫu nhiên nếu có
            // Tuy nhiên, nếu luật bắt buộc phải đánh 3 Bích đầu tiên, không cho pass
            // Nên nếu foundThreeBich == false, ta không nên pass vô tội vạ.
            if (!foundThreeBich && !validMoves.isEmpty()) {
                // Ở đây theo luật: phải có 3 Bích đầu tiên. Nếu không, không move.
                // Bạn có thể cho bot chọn ngẫu nhiên, nhưng sẽ bị "Not a legal move!".
                // Nên tốt hơn là không gửi move (hoặc in ra debug).
                System.out.println("Bot " + this.getPlayerName() + " không tìm thấy 3 Bích, không thể đánh!");
                // Không gửi pass vì bàn trống không cho pass
                return; 
            }
        }

        // Nếu không có nước đi hợp lệ (selectedMove == null)
        // Bot muốn pass, nhưng phải kiểm tra luật:
        if (selectedMove == null) {
            // Muốn bỏ lượt
            // Kiểm tra luật:
            // Nếu bàn trống, không được pass
            if (numOfHandsPlayed == 0) {
                System.out.println("Bot " + this.getPlayerName() + " muốn pass nhưng bàn trống, không được!");
                return; // Không gửi move
            }

            // Nếu lastHandOnTable thuộc về bot, không được pass
            if (lastHandOnTable != null && lastHandOnTable.getPlayer().getName().equals(this.getPlayerList().get(this.getPlayerID()).getName())) {
                System.out.println("Bot " + this.getPlayerName() + " muốn pass nhưng vừa đánh, không được!");
                return; // Không gửi move
            }

            // Nếu qua được 2 check trên, thì pass được
            this.makeMove(this.getPlayerID(), null);
            System.out.println("Bot " + this.getPlayerName() + " đã bỏ lượt.");
        } else {
            // Chuyển Hand thành chỉ số lá bài
            int[] selectedCardIdx = convertHandToCardIndices(selectedMove);
            this.makeMove(this.getPlayerID(), selectedCardIdx);
            System.out.println("Bot " + this.getPlayerName() + " đã chơi nước đi: " + Arrays.toString(selectedCardIdx));
        }
    }

    // Phương thức lấy danh sách nước đi hợp lệ từ game
    private ArrayList<Hand> getValidMoves() {
        ArrayList<Hand> validMoves = new ArrayList<>();

        Player botPlayer = this.getPlayerList().get(this.getPlayerID());
        CardList currentHand = botPlayer.getCardsInHand();
        Hand lastHand = null;
        if (!this.getHandsOnTable().isEmpty()) {
            lastHand = this.getHandsOnTable().get(this.getHandsOnTable().size() - 1);
        }

        // Lấy các nước đi hợp lệ từ game
        validMoves = generateValidMoves(botPlayer, currentHand, lastHand);
        return validMoves;
    }

    // Phương thức chuyển đổi Hand thành danh sách chỉ số lá bài
    private int[] convertHandToCardIndices(Hand hand) {
        Player botPlayer = this.getPlayerList().get(this.getPlayerID());
        CardList currentHand = botPlayer.getCardsInHand();

        int[] indices = new int[hand.size()];
        for (int i = 0; i < hand.size(); i++) {
            for (int j = 0; j < currentHand.size(); j++) {
                if (hand.getCard(i).equals(currentHand.getCard(j))) {
                    indices[i] = j;
                    break;
                }
            }
        }
        return indices;
    }

    // Phương thức tạo danh sách nước đi hợp lệ
    private ArrayList<Hand> generateValidMoves(Player player, CardList currentHand, Hand lastHand) {
        ArrayList<Hand> validMoves = new ArrayList<>();

        // Tìm tất cả các bộ Quadruple
        for (int i = 0; i < currentHand.size() - 3; i++) {
            CardList selectedCards = new CardList();
            for (int j = i; j < i + 4; j++) {
                selectedCards.addCard(currentHand.getCard(j));
            }
            Hand testHand = composeHand(player, selectedCards);
            if (testHand != null && testHand.isValid()) {
                if (lastHand == null || testHand.beats(lastHand)) {
                    validMoves.add(testHand);
                }
            }
        }

        // Tìm tất cả các bộ Triple
        for (int i = 0; i < currentHand.size() - 2; i++) {
            CardList selectedCards = new CardList();
            for (int j = i; j < i + 3; j++) {
                selectedCards.addCard(currentHand.getCard(j));
            }
            Hand testHand = composeHand(player, selectedCards);
            if (testHand != null && testHand.isValid()) {
                if (lastHand == null || testHand.beats(lastHand)) {
                    validMoves.add(testHand);
                }
            }
        }

        // Tìm tất cả các Pair
        for (int i = 0; i < currentHand.size() - 1; i++) {
            CardList selectedCards = new CardList();
            for (int j = i; j < i + 2; j++) {
                selectedCards.addCard(currentHand.getCard(j));
            }
            Hand testHand = composeHand(player, selectedCards);
            if (testHand != null && testHand.isValid()) {
                if (lastHand == null || testHand.beats(lastHand)) {
                    validMoves.add(testHand);
                }
            }
        }

        // Tìm tất cả các Single
        for (int i = 0; i < currentHand.size(); i++) {
            CardList selectedCards = new CardList();
            selectedCards.addCard(currentHand.getCard(i));
            Hand testHand = composeHand(player, selectedCards);
            if (testHand != null && testHand.isValid()) {
                if (lastHand == null || testHand.beats(lastHand)) {
                    validMoves.add(testHand);
                }
            }
        }

        return validMoves;
    }
}