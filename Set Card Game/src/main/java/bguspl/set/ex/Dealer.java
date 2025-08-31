package bguspl.set.ex;

import bguspl.set.Env;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    // if a player claim set, save the players id. otherwise -1.
    public volatile BlockingQueue<Player> playersClaimSet;
    // public Player playerClaimSet;

    public Vector<Integer> emptySlots;

    private long time;

    public Vector<Integer> slotsToRemove;

    public volatile boolean restarting;
    private Player checking;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        this.playersClaimSet = new LinkedBlockingQueue<>(players.length);
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        emptySlots = new Vector<Integer>();
        for (int i = 0; i < env.config.tableSize; i++) {
            emptySlots.add(i);
        }
        reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis;
        //time = reshuffleTime;
        slotsToRemove = new Vector<Integer>();
        // playerClaimSet = null;
        restarting = true;

    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        for (int i = 0; i < players.length; i++) {
            Thread playerThread = new Thread(players[i]);
            playerThread.start();
        }
        while (!shouldFinish()) {
            Collections.shuffle(deck);
            placeCardsOnTable();// 100%
            updateTimerDisplay(true);// 100%
            restarting = false;
            timerLoop();
            restarting = true;
            updateTimerDisplay(true);// 100%
            removeAllCardsFromTable();
        }
        terminate();
        announceWinners();
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did
     * not time out.
     */
    private void timerLoop() {
        // chaged- added time >= 0
        while (!terminate && time <= reshuffleTime && time > 0) {
            sleepUntilWokenOrTimeout(); // 100%
            updateTimerDisplay(false); // 100%
            removeCardsFromTable();
            placeCardsOnTable();// 100%
        }
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        // TODO implement
        terminate = true;
        for (int i = players.length-1; i >= 0; i--) {
            players[i].terminate();
        }
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeCardsFromTable() {
        // TODO implement - done
        // check if we need to lock the players thread
        int[] cards = new int[env.config.featureSize];
        int[] slots = new int[env.config.featureSize];
        if (checking != null) { // updated in sleepUntilWokenUp
            boolean playerNotClaimSet = false;
            try {
                for (int j = 0; j < env.config.featureSize; j++) {
                    if (checking.tokens.size() == 0) {
                        playerNotClaimSet = true;
                        break;
                    }
                    slots[j] = checking.tokens.take();
                    if (table.slotToCard[slots[j]] == null) {
                        playerNotClaimSet = true;
                        break;
                    }
                    cards[j] = table.slotToCard[slots[j]];
                }
            } catch (InterruptedException e) {}
            if (!playerNotClaimSet) {
                // check if the player has a legal set
                if (env.util.testSet(cards)) {
                    // give the player a point
                    checking.point = true;
                    // run over the cards of the set
                    for (int i = 0; i < env.config.featureSize; i++) {
                        // search for the players that placed a token on the card
                        Vector<Integer> vector = table.slotToPlayer.get(slots[i]);
                        for (int index = 0; index < vector.size(); index++) {
                            for (Player playerToCheck : players) {
                                // remove the token from the player's actions queue
                                if (playerToCheck.id == vector.elementAt(index)) {
                                    playerToCheck.actionsQueue.remove(slots[i]);
                                    table.removeToken(playerToCheck.id, slots[i]);
                                    playerToCheck.checkingSet = false;
                                    //index--;
                                    break;
                                }
                            }
                        }

                        // remove the card from the table
                        table.removeCard(slots[i]);
                        // add the card to the list of empty slots
                        emptySlots.add(slots[i]);
                    }
                    // reset the timer
                    updateTimerDisplay(true);
                    try{
                        checking.fromDealer.put(0);
                    } catch(InterruptedException ignored){}
                } else {
                    for (int i = 0; i < env.config.featureSize; i++) {
                        table.removeToken(checking.id, slots[i]);
                    }
                    checking.penalty = true;
                    try{
                        checking.fromDealer.put(0);
                    } catch(InterruptedException ignored){}
                }
            } else {
                // player.freeze = false;
                checking.checkingSet = false;
                checking.penalty = false;
                checking.point = false;
                try{
                    checking.fromDealer.put(0);
                } catch(InterruptedException ignored){}
            }

        }

        // player.freeze = false;
        //playersClaimSet.clear();
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        // TODO implement - done
        Collections.shuffle(emptySlots);
        while (!emptySlots.isEmpty()) {
            if (!deck.isEmpty()) {
                table.placeCard(deck.get(0), emptySlots.get(0));
                deck.remove(0);
                emptySlots.remove(0);
            } else {
                break;
            }
        }
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some
     * purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        // TODO implement - done
        if (!terminate && time < reshuffleTime && slotsToRemove.size() == 0) {
            try {
                /*synchronized(this){
                    this.wait(100);
                }*/
                if (time < env.config.turnTimeoutWarningMillis) {
                    checking = playersClaimSet.poll(10, TimeUnit.MILLISECONDS);
                } else
                    checking = playersClaimSet.poll(1000, TimeUnit.MILLISECONDS);
                
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        // TODO implement
        time = reshuffleTime - System.currentTimeMillis();
        boolean warn = false;
        // check if the dealer needs to update the timer every second
        if (time <= env.config.turnTimeoutWarningMillis) {
            warn = true;
        }
        if (reset) {
            time = env.config.turnTimeoutMillis;
            reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis;
            env.ui.setCountdown(time, false);
        } else {
            env.ui.setCountdown(time, warn);
        }
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // TODO implement - done
        for (Player player : playersClaimSet) {
            try {
                player.fromDealer.put(0);// wake up from line 120 in player.java
            } catch (InterruptedException ignored) {
            }
        }
        playersClaimSet.clear();

        // search for the players that placed a token on the slot
        for (Player playerToCheck : players) {
            playerToCheck.actionsQueue.clear();
            for(Integer token : playerToCheck.tokens){
                table.removeToken(playerToCheck.id, token);
            }
            playerToCheck.tokens.clear();
        }
        
        for (int slot = 0; slot < env.config.tableSize; slot++) {
            emptySlots.add(slot);
            if(table.slotToCard[slot] != null){
                deck.add(table.slotToCard[slot]);
                table.removeCard(slot);
            }
        }
        table.slotToPlayer.clear();


    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement - done
        int maxScore = 0;
        LinkedList<Integer> winners = new LinkedList<Integer>();
        // find the max score
        for (int i = 0; i < players.length; i++) {
            if (players[i].score() > maxScore) {
                maxScore = players[i].score();
            }
        }
        // find winners and adds them to a list
        for (int i = 0; i < players.length; i++) {
            if (players[i].score() == maxScore) {
                winners.add(players[i].id);
            }
        }
        // adds the winners to an int array
        int[] intWinners = new int[winners.size()];
        int j = 0;
        for (Integer winner : winners) {
            intWinners[j] = winner;
            j++;
        }
        // announce winners
        env.ui.announceWinner(intWinners);
    }
}