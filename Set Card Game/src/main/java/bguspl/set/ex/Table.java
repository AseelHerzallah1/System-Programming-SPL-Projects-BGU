package bguspl.set.ex;

import bguspl.set.Env;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * This class contains the data that is visible to the player.
 *
 * @inv slotToCard[x] == y iff cardToSlot[y] == x
 */
public class Table {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Mapping between a slot and the card placed in it (null if none).
     */
    protected final Integer[] slotToCard; // card per slot (if any)

    /**
     * Mapping between a card and the slot it is in (null if none).
     */
    protected final Integer[] cardToSlot; // slot per card (if any)

    // public ConcurrentHashMap<Integer,BlockingQueue<Integer>> playerToSlot;

    // map between a slot to a queue of the players that placed a token on the slot.
    // the order that the players put their token on the slot will be the order of
    // the queue.
    public ConcurrentHashMap<Integer, Vector<Integer>> slotToPlayer;
    private Semaphore[] locks;
    /**
     * Constructor for testing.
     *
     * @param env        - the game environment objects.
     * @param slotToCard - mapping between a slot and the card placed in it (null if
     *                   none).
     * @param cardToSlot - mapping between a card and the slot it is in (null if
     *                   none).
     */
    public Table(Env env, Integer[] slotToCard, Integer[] cardToSlot) {

        this.env = env;
        this.slotToCard = slotToCard;
        this.cardToSlot = cardToSlot;
        // BlockingQueue<Integer> cards = new LinkedBlockingQueue<>();
        slotToPlayer = new ConcurrentHashMap<Integer, Vector<Integer>>();
        locks = new Semaphore[env.config.tableSize];
        for(int i = 0; i < env.config.tableSize; i++){
            locks[i] = new Semaphore(1);    //lock for each slot in the table, to synchronize each slot and not the entire table
        }
        /*
         * for(int i = 0; i < env.config.tableSize; i ++){
         * BlockingQueue<Integer> players = new LinkedBlockingQueue<>();
         * slotToPlayer.put(i, players);
         * }
         */
        // playerToSlot = new ConcurrentHashMap<Integer,BlockingQueue<Integer>>();
    }

    /**
     * Constructor for actual usage.
     *
     * @param env - the game environment objects.
     */
    public Table(Env env) {

        this(env, new Integer[env.config.tableSize], new Integer[env.config.deckSize]);
    }

    /**
     * This method prints all possible legal sets of cards that are currently on the
     * table.
     */
    public void hints() {
        List<Integer> deck = Arrays.stream(slotToCard).filter(Objects::nonNull).collect(Collectors.toList());
        env.util.findSets(deck, Integer.MAX_VALUE).forEach(set -> {
            StringBuilder sb = new StringBuilder().append("Hint: Set found: ");
            List<Integer> slots = Arrays.stream(set).mapToObj(card -> cardToSlot[card]).sorted()
                    .collect(Collectors.toList());
            int[][] features = env.util.cardsToFeatures(set);
            System.out.println(
                    sb.append("slots: ").append(slots).append(" features: ").append(Arrays.deepToString(features)));
        });
    }

    /**
     * Count the number of cards currently on the table.
     *
     * @return - the number of cards on the table.
     */
    public synchronized int countCards() {
        int cards = 0;
        for (Integer card : slotToCard)
            if (card != null)
                ++cards;
        return cards;

    }

    /**
     * Places a card on the table in a grid slot.
     * 
     * @param card - the card id to place in the slot.
     * @param slot - the slot in which the card should be placed.
     *
     * @post - the card placed is on the table, in the assigned slot.
     */
    public void placeCard(int card, int slot) {
        try {
            //don't allow any other thread to access this token while it's being removed 
            locks[slot].acquire();
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {
        }
        cardToSlot[card] = slot;
        slotToCard[slot] = card;
        // TODO implement - DONE
        env.ui.placeCard(card, slot);
        Vector<Integer> players = new Vector<>();
        slotToPlayer.put(slot, players);
        // allow access to the token
        locks[slot].release();
    }

    /**
     * Removes a card from a grid slot on the table.
     * 
     * @param slot - the slot from which to remove the card.
     */
    public void removeCard(int slot) {
        try {
            locks[slot].acquire();
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {
        }
        if(slotToCard[slot] != null){
            int id = slotToCard[slot];
            slotToCard[slot] = null;
            cardToSlot[id] = null;
            env.ui.removeCard(slot);
            env.ui.removeTokens(slot);
            slotToPlayer.remove(slot);
        }
        locks[slot].release();
    }

    /**
     * Places a player token on a grid slot.
     * 
     * @param player - the player the token belongs to.
     * @param slot   - the slot on which to place the token.
     */
    public void placeToken(int player, int slot) {
        // TODO implement - DONE
        try{
            locks[slot].acquire();
        } catch (InterruptedException ignored){}
        if (slotToCard[slot] != null && slotToPlayer.get(slot) != null) {
            (slotToPlayer.get(slot)).add(player);
            env.ui.placeToken(player, slot);
        }
        locks[slot].release();
    }

    /**
     * Removes a token of a player from a grid slot.
     * 
     * @param player - the player the token belongs to.
     * @param slot   - the slot from which to remove the token.
     * @return - true iff a token was successfully removed.
     */
    public boolean removeToken(int player, int slot) {
        // TODO implement - DONE
        try{
            locks[slot].acquire();
        } catch (InterruptedException ignored){}
        if (slotToCard[slot] != null && slotToPlayer != null && slotToPlayer.get(slot) != null) {
            boolean slotHasPlayer = slotToPlayer.get(slot).contains(player);
            Integer playerInteger = player;
            if (slotHasPlayer) {
                slotToPlayer.get(slot).remove(playerInteger);
                env.ui.removeToken(player, slot);
                locks[slot].release();
                return true;
            }
        }
        locks[slot].release();
        return false;
    }
}