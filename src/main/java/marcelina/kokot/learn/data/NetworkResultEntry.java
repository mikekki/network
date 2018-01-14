/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marcelina.kokot.learn.data;

/**
 *
 * @author Marcelina
 */
public class NetworkResultEntry implements Comparable<NetworkResultEntry> {
    int id;
    double value;
    boolean correct;

    public NetworkResultEntry(int id, double value, boolean correct) {
        this.id = id;
        this.value = value;
        this.correct = correct;
    }

    @Override
    public String toString() {
        return "" + id + " " + value + " " + correct;
    }
    
    @Override
    public int compareTo(NetworkResultEntry o) { return this.value == o.value ? 0 : (this.value < o.value ? -1 : 1);}
    
}
