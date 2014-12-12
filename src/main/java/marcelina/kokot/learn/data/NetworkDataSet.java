package marcelina.kokot.learn.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Random;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author Marcelina Kokot
 */
public class NetworkDataSet {
    
    NetworkDataDescription dataDescription;
    LinkedList<NetworkDataRecord> dataSet;
    
    private static final String classZero = "0";
    static final String attribute1Name = "at1";
    static final String attribute2Name = "at2";
    static final String className = "classConnection";

    private NetworkDataSet(NetworkDataDescription dataDescription, LinkedList<NetworkDataRecord> dataSet) {
        this.dataDescription = dataDescription;
        this.dataSet = dataSet;
    }
    
    /**
     * Method retrieves data from file.
     * Note:
     * File must be space delimited, for instance class.txt:
     * int string
     * network.txt:
     * int int string
     * Be aware, some files may have a tabs instead of space!
     * @param labelsFile class.txt file 
     * @param connectionFile network.txt file
     * @param directedNetwork determines if network is directed
     * @return NetworkDataSet Processed and unified data
     */
    public static NetworkDataSet getFromFile(String labelsFile, String connectionFile, boolean directedNetwork) {
        try {
            // Description - special object containig data about set: classes, labels ...
            NetworkDataDescription description = new NetworkDataDescription();
            // List of data records
            LinkedList<NetworkDataRecord> dataSet = new LinkedList<NetworkDataRecord>();
            
            // Store information about direction
            description.directed = directedNetwork;
            
            // Stream to read class file
            BufferedReader labelReader = new BufferedReader(new FileReader(labelsFile));
            String line = "";
            while ((line = labelReader.readLine()) != null) {
                
                // Here is split by space
                String[] vals = line.split(" ");
                
                // label
                String labelValue = vals[1];
                
                // node number (first value in each line)
                Integer labelNode = Integer.parseInt(vals[0]);
                
                // update description
                description.addLabel(labelNode, labelValue);
            }
            labelReader.close();
            
            // Stream to read network
            BufferedReader records = new BufferedReader(new FileReader(connectionFile));
            while ((line = records.readLine()) != null) {
                
                // Again, split by space
                String[] vals = line.split(" ");
                
                // nodes numbers, first and second
                Integer at1 = Integer.parseInt(vals[0]);
                Integer at2 = Integer.parseInt(vals[1]);
                
                // class value
                String classValue = vals[2];
                
                // add data set, update description
                dataSet.add(description.processRecord(at1, at2, classValue));
                if (!directedNetwork) {
                    // if network is not directed - add the same record with changed nodes
                    dataSet.add(description.processRecord(at2, at1, classValue));
                }
            }
            records.close();
            
            return new NetworkDataSet(description, dataSet);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.fillInStackTrace());
        }
    }
    
    /**
     * Method adds class zero elements using class NetworkClassZeroAdder
     * @param adder Object which selects combination for class 0.
     */
    public void addClassZero(NetworkClassZeroAdder adder) {
        // calculate all possible connections (n*(n-1)) - data set
        int fullNetworkSize = dataDescription.nodes.size();
        fullNetworkSize *= fullNetworkSize - 1;
        fullNetworkSize /= 2;
        fullNetworkSize -= dataSet.size() + (dataDescription.directed ? 0 : dataSet.size());
        adder.init(fullNetworkSize);
        
        this.dataDescription.addClass(NetworkDataSet.classZero);
        
        // record for check - if record exists in data set
        NetworkDataRecord search = new NetworkDataRecord();
        
        // get all nodes numbers
        Object[] nodes = this.dataDescription.nodes.toArray();
        
        // cross nodes: nodes x nodes
        for(int i = 0 ; i < nodes.length; i++) {
            for(int j = i+1; j < nodes.length; j++) {
                search.attribute1 = (Integer)nodes[i];
                search.attribute2 = (Integer)nodes[j];
                // check if nodes combination is already in set - otherwise - ask adder if class 0 should be added.
                if (!dataSet.contains(search) && adder.isZeroRecord(search.attribute1, search.attribute2)) {
                    dataSet.add(dataDescription.processRecord(search.attribute1, search.attribute2, classZero));
                }
            }
        }
    }
    
    /**
     * Method removes records from set and create new one.
     * @param cut Value from range (0.0,1.0) indicating how big part (1 - whole) should be deattached.
     * @return NetworkDataSet Deattached records.
     */
    public NetworkDataSet split(double cut) {
        // random number generator
        Random rand = new Random();
        int dataSize = this.dataSet.size();
        
        // calculate size
        int size = (int)(cut * dataSize);
        
        // data set which will be returned
        LinkedList<NetworkDataRecord> cutRecords = new LinkedList<NetworkDataRecord>();
        // process till cutRecords will be filled with #size records
        for (int i = 0 ; i< size; i++) {
            cutRecords.add(this.dataSet.remove(rand.nextInt(dataSize--)));
        }
        
        return new NetworkDataSet(dataDescription, cutRecords);
    }
    
    /**
     * Method transforms data set to Weka's Instances
     * @return Instances Weka instances
     */
    public Instances getWekaDataSource() 
    {
        try {
            // method should be done better
            File fileData = new File("tmp_csv_transform.csv");
            
            // generate csv file
            PrintWriter writer = new PrintWriter(fileData);
            
            // add csv head 
            writer.printf("%s,%s,%s\n", NetworkDataSet.attribute1Name, NetworkDataSet.attribute2Name, NetworkDataSet.className);
        
            // add records to csv
            for(NetworkDataRecord record : dataSet) {
                writer.printf("%s,%s,%s\n", record.attribute1Label, record.attribute2Label, record.className);
            }
            
            writer.close();
            
            // load csv to instances
            Instances instances = (new ConverterUtils.DataSource("tmp_csv_transform.csv")).getDataSet(2);
            
            // remove file
            fileData.delete();
            
            return instances;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.fillInStackTrace());
        }
    }
    
    /**
     * Retrives classes names: maps class number from 0 to string name
     * @return array of classes
     */
    public Object[] classes() {
        return this.dataDescription.classes.toArray();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        NetworkDataSet clone = new NetworkDataSet(dataDescription, (LinkedList<NetworkDataRecord>) dataSet.clone());
        return clone;
    }
    
}
