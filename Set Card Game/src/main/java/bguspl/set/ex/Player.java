package bguspl.set.ex;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import bguspl.set.Env;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    private Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate
     * key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    private final Dealer dealer;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    public volatile BlockingQueue<Integer> actionsQueue;
    public volatile BlockingQueue<Integer> tokens;
    public volatile BlockingQueue<Integer> fromDealer;

    public volatile boolean freeze;
    public volatile boolean checkingSet;
    public volatile boolean penalty;
    public volatile boolean point;
    public Object lock = new Object();

    /**
     * The current score of the player.
     */
    private int score;

    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided
     *               manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.dealer = dealer;
        this.table = table;
        this.id = id;
        this.human = human;
        this.actionsQueue = new LinkedBlockingQueue<>(env.config.featureSize);
        this.tokens = new LinkedBlockingQueue<>(env.config.featureSize);
        this.fromDealer = new LinkedBlockingQueue<>(env.config.featureSize);
        this.freeze = false;
        this.checkingSet = false;
        this.penalty = false;
        this.point = false;
    }

    /**
     * The main player thread of each player starts here (main loop for the player
     * thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        if (!human)
            createArtificialIntelligence();
        while (!terminate) {
            // TODO implement main player loop

            try {
                if (!dealer.restarting) {
                    Integer slot = actionsQueue.take();
                    System.out.println("Got " + slot);
                    boolean tokenFound = false;
                    checkingSet = true;
                    // if the player has a token on the slot, remove it
                    tokenFound = tokens.contains(slot);
                    // if not, place the token on the slot
                    if (!tokenFound) {
                        tokens.add(slot);
                        table.placeToken(this.id, slot);
                        // after placing the third token, the player claims for a set
                        if (tokens.size() == env.config.featureSize) {
                            checkingSet = true;
                            dealer.playersClaimSet.put(this);
                            /*synchronized (dealer) {
                                dealer.notifyAll();
                            }*/
                            fromDealer.take();
                            if (point) {
                                point();
                                checkingSet = false;
                            }
                            if (penalty) {
                                penalty();
                                checkingSet = false;
                            }
                        } else {
                            checkingSet = false;
                        }
                    } else {
                        // synchronized(table){
                        table.removeToken(this.id, slot);
                        // }
                        tokens.remove(slot);
                        checkingSet = false;
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }
        if (!human)
            try {
                aiThread.join();
            } catch (InterruptedException ignored) {
            }
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of
     * this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it
     * is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very, very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
            // synchronized(aiLock){
            while (!terminate) {
                // TODO implement player key press simulator
                int tableSize = env.config.tableSize;
                int slot = (int) (Math.random() * tableSize);
                try {
                    if (!freeze && !checkingSet && !dealer.restarting) {
                        actionsQueue.offer(slot, 100, TimeUnit.MILLISECONDS);
                    }
                    //Thread.sleep(5);
                } catch (InterruptedException e) {
                }
            }
            // }
            env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        // TODO implement - TO BE CHECKED
        terminate = true;
        playerThread.interrupt();
        try {
            playerThread.join();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        // TODO implement
        // if(!freeze){
        // synchronized(lock){
        System.out.println("pressed " + slot);
        System.out.println(!freeze + " " + !checkingSet + " " + !dealer.restarting);
        if (!freeze && !checkingSet && !dealer.restarting) {
            System.out.println("added " + slot);
            actionsQueue.offer(slot);
        }
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {// chenged - added synchronized
        // TODO implement - TO BE CHECKED
        this.score++; // changed - replaced the lines
        env.ui.setScore(id, score);
        freeze = true;
        long pointTime = env.config.pointFreezeMillis;
        while (pointTime >= 0) {
            env.ui.setFreeze(this.id, pointTime);
            try {
                if (pointTime > 1000)
                    Thread.sleep(1000);
                else
                    Thread.sleep(pointTime);
            } catch (InterruptedException ignored) {
            }
            pointTime -= 1000;
        }
        env.ui.setFreeze(this.id, 0);

        int ignored = table.countCards(); // this part is just for demonstration in the unit tests
        tokens.clear();
        freeze = false;
        point = false;
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() { // changed - added synchronized
        // TODO implement - TO BE CHECKED
        System.err.println("in penalty");
        freeze = true;
        long penaltyTime = env.config.penaltyFreezeMillis;
        while (penaltyTime >= 0) {
            env.ui.setFreeze(this.id, penaltyTime);
            try {
                if (penaltyTime > 1000)
                    Thread.sleep(1000);
                else
                    Thread.sleep(penaltyTime);
            } catch (InterruptedException ex) {
            }
            penaltyTime -= 1000;
        }
        env.ui.setFreeze(this.id, 0);
            
        freeze = false;
        penalty = false;
    }

    public int score() {
        return score;
    }

    public Thread getPlayerThread() {
        return playerThread;
    }
}