import java.util.*;

/**
 * Emulates a black jack game between two different AI players.
 */
public class Blackjack {

    /**
     * Static factory method.
     * Generates a list containing one of every possible card.
     * This simplified game assumes a single deck of 52 cards, one of every unique suit and rank combination.
     */
    public static final List<Card> ALL_CARDS = new ArrayList<Card>();
    static {
        for (Card.Suit s : Card.Suit.values()) {
            for (Card.Rank r : Card.Rank.values()) {
                ALL_CARDS.add(new Card(r, s));
            }
        }
    }

    /**
     * Represents a single playing card, the combination of a suit (Spades, hearts, clubs, diamonds)
     * and a rank (Ace, two, three... Jack, Queen, King).
     * Any particular card is worth a number of points, according to its rank.
     * In this simplified game, an Ace is always worth 1 points.
     * A two is with 2 points, three worth 3, and so on.
     * The face cards (Jack, Queen, King) are worth 10 points.
     */
    static class Card {

        public enum Suit {
            Spades, Hearts, Clubs, Diamonds
        }

        public enum Rank {
            Ace(1), Two(2), Three(3), Four(4), Five(5), Seven(7), Eight(8), Nine(9), Ten(10),
            Jack(11), Queen(12), King(13);

            private int points;

            Rank(int points) {
                this.points = points;
            }
        }

        final Suit suit;
        final Rank rank;

        public Card(Rank r, Suit s) {
            suit = s;
            rank = r;
        }

        /**
         * Returns a human readable name of the card, for example "Ace of Spades", suitable for inclusion in
         * the game's console output.
         *
         * @return name of the card
         */
        public String getDescription() {
            return this.rank + " of" + this.suit;
        }

        /**
         * Returns the number of points that this card is worth, according to its rank.
         *
         * @return point value of this card.
         */
        public int getPoints() {
            return this.rank.points;
        }

        @Override
        public String toString() {
            return getDescription();
        }

    }

    /**
     * Represents a collection of playing cards that will be dealt in the game.
     *
     * A fresh deck usually begins with the cards in an ordered sequence according to their suit and rank.
     * Before dealing cards to the players, the dealer should shuffle the deck, {@link #shuffle(java.util.Random)}
     * otherwise cards will be dealt in their original sequence.
     *
     * During the game, the dealer deals one card at a time, removing it from the top
     * of the deck, and adding it to the player's hand.
     */
    public static class Deck {

        /**
         * The cards remaining to be dealt
         */
        private List<Card> cards;

        /**
         * Constructs a fresh deck with the specified cards in the given sequence
         *
         * @param cards cards
         */
        public Deck(List<Card> cards) {
            this.cards = cards;
        }

        /**
         * Randomizes the sequence of the cards within the deck.
         */
        public void shuffle(Random random) {
            /* Keep a reference to the existing cards, then build a new list and copy
             * the cards over in a random sequence.  */
            List<Card> originalCards = this.cards;
            List<Card> shuffledCards = new LinkedList<Card>();

            int numberOfCards = originalCards.size();
            for (int i = 0; i < numberOfCards; i++) {
                int nextCardIndex = random.nextInt(originalCards.size());
                Card nextCard = originalCards.get(nextCardIndex);
                shuffledCards.add(nextCard);
            }

            this.cards = shuffledCards;
        }

        /**
         * Draws a card from the top of the deck, so that it can be added to a player's hand.
         *
         * @return dealt card
         */
        public Card deal() {
            return this.cards.get(0);
        }

        /**
         * Returns the list of cards in the deck, in the sequence that they are going to be dealt.
         *
         * @return cards cards in the deck
         */
        public List<Card> getCards() {
            return this.cards;
        }
    }

    /**
     * Represents the collection of cards that have been dealt to a player.
     */
    public static class Hand {

        /**
         * The cards currently held in this hand
         */
        private List<Card> cards;

        /**
         * The player to whom this hand belongs
         */
        private Player player;

        /**
         * Constructs a new hand for the specified player.
         *
         * @param player The player to whom this hand belongs
         */
        public Hand(Player player) {
            this.player = player;
            this.cards = new ArrayList<Card>();
        }

        /**
         * Returns the player that this hand belongs to.
         *
         * @return player
         */
        public Player getPlayer() {
            return player;
        }

        /**
         * Adds a card to the hand
         *
         * @param card the card to be added
         */
        public void addCard(Card card) {
            this.cards.add(card);
        }

        /**
         * Returns the total points for this hand by adding up the points of each card.
         *
         * @return total points
         */
        public int getTotalPoints() {
            int points = 0;
            for (int i = 1; i  < this.cards.size(); i++) {
                points += this.cards.get(i).getPoints();
            }
            return points;
        }

        /**
         * Determines whether this hand is better than the other player's hand.
         * In general, the winning hand is the hand with the greatest number of points.
         * But, if the hand exceeds 21 then it is a "bust" - the other player wins.
         * If both players bust, or if their total points are the same, then it is a draw.
         *
         * @param other the hand to compare against
         * @return true, if this is a better hand than the specified other hand
         */
        public boolean beats(Hand other) {
            int myScore = getTotalPoints();
            if (myScore > 21) {
                return false;
            }
            int otherScore = other.getTotalPoints();
            if (otherScore > myScore) {
                return false;
            }
            return true;
        }

        /**
         * Returns a formatted description of the cards in the hand, suitable for screen output
         *
         * @return description of the hand
         */
        public String getDescription() {
            StringBuilder description = new StringBuilder();
            description.append(this.cards.size());
            description.append(" cards: ");
            boolean first = true;
            for (Card card : this.cards) {
                if (!first) {
                    description.append(", ");
                }
                description.append(card.getDescription());
                first = false;
            }
            description.append(".");
            return description.toString();
        }
    }

    /**
     * Represents a player, and their decision making logic for whether to
     * "hit" or "stand"
     */
    public static class Player {

        /**
         * A friendly name that identifies the player.
         */
        private String name;

        /**
         * Used for calculating probability of bust
         */
        private ProbabilityCalculator probabilityCalculator;

        /**
         * Constructor.
         *
         * @param name player's name
         * @param probabilityCalculator for calculating probability of bust
         */
        public Player(String name, ProbabilityCalculator probabilityCalculator) {
            this.name = name;
            this.probabilityCalculator = probabilityCalculator;
        }

        /**
         * Returns the player's name
         *
         * @return name
         */
        public String getName() {
            return name;
        }

        /**
         * Determines whether the player would like to "hit" (have another card dealt to their hand),
         * or "stand" (end their turn).
         * In this simple implementation, the player's strategy is to hit, so long as they are more likely
         * to increase their points than they are to bust.
         * If their current points are at 21, they should always stay
         * If their current points are at 10 or below, they can never bust, so they should always hit
         * For points in between, they will hit so long as the probability of the next draw causing a bust is less
         * than 50%.
         *
         * @param currentPoints the total of the cards currently held by this player
         * @return true if the players wants to "hit", false if they want to "stand".
         */
        public boolean wantsToHit(int currentPoints) {
            if (currentPoints >= 21) {
                return false;
            }
            else if (currentPoints <= 10) {
                return true;
            }
            else {
                return this.probabilityCalculator.calculateProbability(currentPoints) < 0.5;
            }
        }
    }

    /**
     * Used by the player's hit/stand decision making, to determine the probability of busting if
     * they deal another card.
     */
    public static class ProbabilityCalculator {

        /**
         * All possible cards
         */
        private List<Card> fullDeck;

        /**
         * Initialises the calculator
         *
         * @param fullDeck all possible cards
         */
        public ProbabilityCalculator(List<Card> fullDeck) {
            this.fullDeck = fullDeck;
        }

        /**
         * Determines the probability of a "bust" if the players deals one more card into their hand.
         * A "bust" occurs if the next card takes their total over 21.
         *
         * The probability is simulated as follows:
         * Consider all possible cards that might be dealt next if the player chooses to "hit".
         * (Since "card counting" is a no-no in Blackjack, we imagine that *any* of the cards from a
         * normal full deck could be dealt next. ie. don't exclude cards that have already been dealt).
         * For each possibility, determine the total points that would be achieved if that card
         * were added to the player's hand, and decide whether or not it is a bust.
         * The probability of a bust is the percentage of all the combinations tried that resulted in a bust.
         *
         * For example, if the player's current total is 12, then dealing any card worth 10 points
         * will result in a bust. Out of the 52 cards in a deck, 16 of them are worth 10 points (the Tens, Jacks,
         * Queens and Kings of Spades, Hearts, Clubs and Diamonds). So, the probability of a bust
         * is 0.3077 (30.77%), which is 16 divided by 52.
         * Similarly, if the player's current total is 19, there are 44 possible cards that will result in
         * a bust (everything except the aces and twos), and so the probability is 0.8462.
         *
         * @param currentPoints the total points for the player's current hand
         * @return probability of a bust if one more card is dealt (0.5f = 50%)
         */
        public double calculateProbability(int currentPoints) {
            if (this.fullDeck.isEmpty()) {
                /* Problem: we can't calculate the probability if we don't have any cards to simulate with.
                 * This shouldn't happen, but to prevent potential crashes, we'll return 50% */
                return 0.5d;
            }
            int numberOfBusts = 0;
            int numberOfNonBusts = 0;
            for (Card card : this.fullDeck) {
                int potentialPoints = currentPoints + card.getPoints();
                if (potentialPoints > 21) {
                    numberOfBusts++;
                }
                else {
                    numberOfNonBusts++;
                }
            }
            return numberOfBusts / (double) numberOfNonBusts;
        }
    }

    /**
     * Controller responsible for overall game loop.
     */
    public static class Game {

        private Deck deck;

        private Random random;

        private Player player1;

        private Player player2;

        public Game(Deck deck, Random random, Player player1, Player player2) {
            this.deck = deck;
            this.random = random;
            this.player1 = player1;
            this.player2 = player2;
        }

        public Deck getDeck() {
            return deck;
        }

        public Random getRandom() {
            return random;
        }

        public Player getPlayer1() {
            return player1;
        }

        public Player getPlayer2() {
            return player2;
        }

        /**
         * Main control loop. You an imagine this routine to reflect the actions of the dealer, who
         * coordinates gameplay.
         *
         * This simple game is played by two players.
         * The dealer is given a single fresh deck of cards, which he then shuffles.
         * Each player is dealt two cards.
         * Then each player takes their turn.
         * During their turn, a player can choose to "hit", which means they want to be dealt another card,
         * or to "stand", which means they will end their turn.
         * The player may "hit" as many times as they wish before ending their turn.
         * Once both players have taken their turn, the winner is determined.
         * The winner is the player with the greatest number of points without exceeding 21.
         *
         * @return output from the game
         */
        public String play() {
            StringBuilder output = new StringBuilder();

            /* The game begins... */
            Hand hand1 = new Hand(this.player1);
            hand1.addCard(this.deck.deal());
            hand1.addCard(this.deck.deal());

            Hand hand2 = new Hand(this.player2);
            hand1.addCard(this.deck.deal());
            hand1.addCard(this.deck.deal());

            output.append(hand1.getPlayer().getName()).append(" starts with ").append(hand1.getDescription()).append("\n");
            output.append(hand2.getPlayer().getName()).append(" starts with ").append(hand2.getDescription()).append("\n");

            /* Players take their turns */
            for (Hand hand : Arrays.asList(hand1, hand2)) {
                String name = hand.getPlayer().getName();
                output.append(name).append("'s turn...\n");
                while (hand.getPlayer().wantsToHit(hand.getTotalPoints())) {
                    Card dealt = this.deck.deal();
                    output.append(name).append(" hits: ").append(dealt.getDescription()).append("\n");
                    hand.addCard(dealt);
                }
                if (hand.getTotalPoints() > 21) {
                    output.append(name).append(" bursts.\n");
                }
                else {
                    output.append(name).append(" stands.\n");
                }
            }

            /* Determine the winner */
            if (hand1.beats(hand2)) {
                output.append(hand1.getPlayer().getName()).append(" WINS!\n");
            }
            else if (hand2.beats(hand1)) {
                output.append(hand2.getPlayer().getName()).append(" WINS!\n");
            }
            else {
                output.append("It's a DRAW!\n");
            }

            return output.toString();
        }
    }

    /**
     * Main method. Constructs the object, plays the game, prints the output.
     * Don't change this method!
     *
     * @param args command line args, unused.
     */
    public static void main(String[] args) {
        Deck deck = new Deck(ALL_CARDS);
        Random random = new Random();
        ProbabilityCalculator calculator = new ProbabilityCalculator(ALL_CARDS);
        Player player1 = new Player("Harry", calculator);
        Player player2 = new Player("Joe", calculator);
        Game game = new Game(deck, random, player1, player2);
        System.out.print(game.play());
    }

}



