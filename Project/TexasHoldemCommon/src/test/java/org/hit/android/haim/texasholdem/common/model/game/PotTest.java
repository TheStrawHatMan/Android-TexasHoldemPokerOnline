package org.hit.android.haim.texasholdem.common.model.game;

import org.hit.android.haim.texasholdem.common.model.bean.game.Board;
import org.hit.android.haim.texasholdem.common.model.bean.game.Card;
import org.hit.android.haim.texasholdem.common.model.bean.game.Hand;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.game.rank.HandRank;
import org.hit.android.haim.texasholdem.common.model.game.rank.HandRankCalculatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Haim Adrian
 * @since 22-Jun-21
 */
public class PotTest {
    private static final int COUNT = 5;
    private static final int CHIPS = 5000;
    private Pot pot;
    private Player[] players;
    private Board board;

    @BeforeEach
    void setUp() {
        pot = new Pot();
        players = new Player[COUNT];

        // Arrange a board
        board = new Board();
        board.addCard(Card.valueOf(HandRankCalculatorTest.THREE_HEART));
        board.addCard(Card.valueOf(HandRankCalculatorTest.SEVEN_HEART));
        board.addCard(Card.valueOf(HandRankCalculatorTest.JACK_SPADE));
        board.addCard(Card.valueOf(HandRankCalculatorTest.QUEEN_HEART));
        board.addCard(Card.valueOf(HandRankCalculatorTest.TEN_DIAMOND));

        // Create hands where the first player wins
        Hand[] hands = new Hand[] { new Hand(), new Hand(), new Hand(), new Hand(), new Hand() };
        hands[0].addCard(Card.valueOf(HandRankCalculatorTest.QUEEN_CLUB)); // Winner with 3 QUEENS
        hands[0].addCard(Card.valueOf(HandRankCalculatorTest.QUEEN_DIAMOND));
        hands[1].addCard(Card.valueOf(HandRankCalculatorTest.JACK_CLUB));
        hands[1].addCard(Card.valueOf(HandRankCalculatorTest.FIVE_DIAMOND));
        hands[2].addCard(Card.valueOf(HandRankCalculatorTest.SEVEN_CLUB));
        hands[2].addCard(Card.valueOf(HandRankCalculatorTest.SEVEN_DIAMOND));
        hands[3].addCard(Card.valueOf(HandRankCalculatorTest.THREE_DIAMOND));
        hands[3].addCard(Card.valueOf(HandRankCalculatorTest.FOUR_DIAMOND));
        hands[4].addCard(Card.valueOf(HandRankCalculatorTest.ACE_DIAMOND));
        hands[4].addCard(Card.valueOf(HandRankCalculatorTest.TWO_CLUB));

        for (int i = 0; i < COUNT; i++) {
            Player player = Player.builder()
                .id("" + i)
                .chips(new Chips(CHIPS))
                .isPlaying(true)
                .hand(hands[i])
                .position(i)
                .build();

            players[i] = player;
        }
    }

    @Test
    public void testBet_threePlayersAllIn_playersLeftWithNoChips() {
        // Act
        for (int i = 0; i < COUNT; i++) {
            if ((i % 2) == 0) {
                pot.bet(players[i], players[i].getChips().get());
            }
        }

        // Assert
        for (int i = 0; i < COUNT; i++) {
            if ((i % 2) == 0) {
                Assertions.assertEquals(0, players[i].getChips().get(), "Player at index " + i + " supposed to have no chips");
            } else {
                Assertions.assertEquals(CHIPS, players[i].getChips().get(), "Player at index " + i + " supposed to have chips");
            }
        }
    }

    @Test
    public void testPot_threePlayersAllIn_firstPlayerEarnsAll() {
        // Arrange
        long sum = 0;
        for (int i = 0; i < COUNT; i++) {
            if ((i % 2) == 0) {
                sum += players[i].getChips().get();
                pot.bet(players[i], players[i].getChips().get());
            }
        }

        // Act
        Map<String, Pot.PlayerWinning> playerToEarnings = pot.applyWinning(new HashSet<>(Arrays.asList(players)), board);

        // Assert
        Assertions.assertEquals(1, playerToEarnings.size(), "One winner is expected");
        Assertions.assertTrue(playerToEarnings.containsKey(players[0].getId()), "First player supposed to be the winner");
        Assertions.assertEquals(sum, playerToEarnings.get(players[0].getId()).getSum(), "Sum supposed to be total bets");
        Assertions.assertEquals(sum, players[0].getChips().get(), "First player supposed to have updated amount of chips");
        Assertions.assertNotNull(playerToEarnings.get(players[0].getId()).getHandRank(), "Hand rank is expected to be available for winner");
        Assertions.assertNotNull(playerToEarnings.get(players[0].getId()).getHandRank().getRank(), "Hand rank is expected to be available for winner");
        Assertions.assertEquals(HandRank.TRIPS, playerToEarnings.get(players[0].getId()).getHandRank().getRank().getHandRank(), "Expected hand rank is three of a kind");
    }

    @Test
    public void testPot_threePlayersAllIn_splitPotBetweenTwo() {
        // Arrange
        long sum = 0;
        for (int i = 0; i < COUNT; i++) {
            if ((i % 2) == 0) {
                sum += players[i].getChips().get();
                pot.bet(players[i], players[i].getChips().get());
            }
        }

        // Fictive demonstrating a draw
        players[2].getHand().clear();
        players[2].getHand().addCard(players[0].getHand().getCardAt(0).get());
        players[2].getHand().addCard(players[0].getHand().getCardAt(1).get());

        // Act
        Map<String, Pot.PlayerWinning> playerToEarnings = pot.applyWinning(new HashSet<>(Arrays.asList(players)), board);

        // Assert
        Assertions.assertEquals(2, playerToEarnings.size(), "Two winners are expected");
        Assertions.assertTrue(playerToEarnings.containsKey(players[0].getId()), "First player supposed to be a winner");
        Assertions.assertTrue(playerToEarnings.containsKey(players[2].getId()), "Third player supposed to be a winner");
        Assertions.assertEquals(sum / 2, playerToEarnings.get(players[0].getId()).getSum(), "Sum supposed to be half of total bets");
        Assertions.assertEquals(sum / 2, playerToEarnings.get(players[2].getId()).getSum(), "Sum supposed to be half of total bets");
        Assertions.assertEquals(sum / 2, players[0].getChips().get(), "First player supposed to have updated amount of chips");
        Assertions.assertEquals(sum / 2, players[2].getChips().get(), "Third player supposed to have updated amount of chips");
        Assertions.assertNotNull(playerToEarnings.get(players[0].getId()).getHandRank(), "Hand rank is expected to be available for winner");
        Assertions.assertNotNull(playerToEarnings.get(players[0].getId()).getHandRank().getRank(), "Hand rank is expected to be available for winner");
        Assertions.assertEquals(HandRank.TRIPS, playerToEarnings.get(players[0].getId()).getHandRank().getRank().getHandRank(), "Expected hand rank is three of a kind");
        Assertions.assertNotNull(playerToEarnings.get(players[2].getId()).getHandRank(), "Hand rank is expected to be available for winner");
        Assertions.assertNotNull(playerToEarnings.get(players[2].getId()).getHandRank().getRank(), "Hand rank is expected to be available for winner");
        Assertions.assertEquals(HandRank.TRIPS, playerToEarnings.get(players[2].getId()).getHandRank().getRank().getHandRank(), "Expected hand rank is three of a kind");
    }

    @Test
    public void testPot_twoPlayersAllInMoreThanThird_splitDeadMoneyBetweenTwoAndPotToWinner() {
        // Arrange
        long sum = 0, expectedEarning;
        players[0].getChips().set(3000);
        expectedEarning = players[0].getChips().get() * 3;
        for (int i = 0; i < COUNT; i++) {
            if ((i % 2) == 0) {
                sum += players[i].getChips().get();
                pot.bet(players[i], players[i].getChips().get());
            }
        }

        // Fictive demonstrating a draw
        players[4].getHand().clear();
        players[4].getHand().addCard(players[2].getHand().getCardAt(0).get());
        players[4].getHand().addCard(players[2].getHand().getCardAt(1).get());

        // Act
        Map<String, Pot.PlayerWinning> playerToEarnings = pot.applyWinning(new HashSet<>(Arrays.asList(players)), board);

        // Assert
        Assertions.assertEquals(3, playerToEarnings.size(), "Two winners are expected");
        Assertions.assertTrue(playerToEarnings.containsKey(players[0].getId()), "First player supposed to be a winner");
        Assertions.assertTrue(playerToEarnings.containsKey(players[2].getId()), "Third player supposed to be a winner");
        Assertions.assertTrue(playerToEarnings.containsKey(players[4].getId()), "Fifth player supposed to be a winner");
        Assertions.assertEquals(expectedEarning, playerToEarnings.get(players[0].getId()).getSum(), "The real winner supposed to have this amount");
        Assertions.assertEquals((sum - expectedEarning) / 2, playerToEarnings.get(players[2].getId()).getSum(), "Sum supposed to be half of dead money");
        Assertions.assertEquals((sum - expectedEarning) / 2, playerToEarnings.get(players[4].getId()).getSum(), "Sum supposed to be half of dead money");
        Assertions.assertEquals(expectedEarning, players[0].getChips().get(), "First player supposed to have updated amount of chips");
        Assertions.assertEquals((sum - expectedEarning) / 2, players[2].getChips().get(), "Third player supposed to have updated amount of chips");
        Assertions.assertEquals((sum - expectedEarning) / 2, players[4].getChips().get(), "Fifth player supposed to have updated amount of chips");
        Assertions.assertNotNull(playerToEarnings.get(players[0].getId()).getHandRank(), "Hand rank is expected to be available for winner");
        Assertions.assertNotNull(playerToEarnings.get(players[0].getId()).getHandRank().getRank(), "Hand rank is expected to be available for winner");
        Assertions.assertEquals(HandRank.TRIPS, playerToEarnings.get(players[0].getId()).getHandRank().getRank().getHandRank(), "Expected hand rank is three of a kind");
        Assertions.assertNotNull(playerToEarnings.get(players[2].getId()).getHandRank(), "Hand rank is expected to be available for winner");
        Assertions.assertNotNull(playerToEarnings.get(players[2].getId()).getHandRank().getRank(), "Hand rank is expected to be available for winner");
        Assertions.assertEquals(HandRank.TRIPS, playerToEarnings.get(players[2].getId()).getHandRank().getRank().getHandRank(), "Expected hand rank is three of a kind");
        Assertions.assertNotNull(playerToEarnings.get(players[4].getId()).getHandRank(), "Hand rank is expected to be available for winner");
        Assertions.assertNotNull(playerToEarnings.get(players[4].getId()).getHandRank().getRank(), "Hand rank is expected to be available for winner");
        Assertions.assertEquals(HandRank.TRIPS, playerToEarnings.get(players[4].getId()).getHandRank().getRank().getHandRank(), "Expected hand rank is three of a kind");
    }
}

