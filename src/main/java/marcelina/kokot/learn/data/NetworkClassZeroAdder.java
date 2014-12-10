/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package marcelina.kokot.learn.data;

import java.util.Random;

/**
 *
 * @author Marcelina Kokot
 */
public abstract class NetworkClassZeroAdder {
    
    /**
     * Class is used to determine if record should be added as class zero
     */
    
    /**
     * Abstarct method called once on the beginning - it passes number if possible nodes combination which are not having class.
     * @param dataSize Number of nodes combination without class.
     */
    public abstract void init(int dataSize);
    
    /**
     * Abstarct class which shoudl decide if record should be cosidered as class 0.
     * As arguments, there are nodes number passed.
     * @param at1 First node number
     * @param at2 Second node number
     * @return Value which tells if record should be added to data set with class 0
     */
    public abstract boolean isZeroRecord(Integer at1, Integer at2);
    
    /**
     * Returns prepared class zero adder.
     * It randomly selects with proper possibility.
     * @param size Approximate value of added class zero classes records.
     * @return NetwotkClassZeroAdder
     */
    public static NetworkClassZeroAdder getRandomZeroAdder(final int size) {
        return new NetworkClassZeroAdder() {
            private Random random;
            private double addPossibility = 0;
            
            @Override
            public boolean isZeroRecord(Integer at1, Integer at2) {
                return random.nextDouble() < this.addPossibility;
            }

            @Override
            public void init(int dataSize) {
                this.random = new Random();
                this.addPossibility = (double)size / (double)dataSize;
            }
        };
    }
    
    /**
     * Returns prepared class zero adder.
     * It selects in deterministic way: starting from offset-th and then each each.
     * @param offset First record which should start
     * @param each Repeat number after next add.
     * @return NetwotkClassZeroAdder
     */
    public static NetworkClassZeroAdder getDeterministicZeroAdder(final int offset, final int each) {
        return new NetworkClassZeroAdder() {
            private int added;
            
            @Override
            public boolean isZeroRecord(Integer at1, Integer at2) {
                return added++ % each == 0;
            }

            @Override
            public void init(int dataSize) {
                System.out.println(dataSize);
                this.added = offset;
            }
        };
    }
    
    /**
     * Returns prepared class zero adder.
     * It selects all records.
     * @return NetwotkClassZeroAdder
     */
    public static NetworkClassZeroAdder getAllZeroAdder() {
        return new NetworkClassZeroAdder() {

            @Override
            public boolean isZeroRecord(Integer at1, Integer at2) {
                return true;
            }

            @Override
            public void init(int dataSize) {
            }
        };
    }
    
}
