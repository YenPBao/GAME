package model;

import java.util.Arrays;

public class Quadruple extends Hand implements HandType {

	public Quadruple(Player player, CardList cards) {
		super(player, cards);
	}
	public boolean isValid ()
	{
		if (this.size() ==  4 &&this.getCard(0).rank == this.getCard(1).rank && this.getCard(0).rank == this.getCard(2).rank && this.getCard(0).rank == this.getCard(3).rank)
		{
			return true;
		}
		return false;
	}
	public Card getTopCard()
	{
		int[] suits = {this.getCard(0).suit, this.getCard(1).suit, this.getCard(2).suit, this.getCard(3).suit};
		Arrays.sort(suits);
		int index = suits[suits.length-1];
		for (int i = 0 ; i < 4;i++)
		{
			if(this.getCard(i).suit == index)
			{
				index = i;
				break;
			}
		}
		return this.getCard(index);
	}
	public String getType ()
	{
		return new String("Quadruple");
	}

}
