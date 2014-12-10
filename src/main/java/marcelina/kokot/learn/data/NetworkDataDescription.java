package marcelina.kokot.learn.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 *
 * @author Marcelina Kokot
 */
class NetworkDataDescription {
    
    /**
     * This class is used to gather useful information about data set.
     * Including:
     * nodes set,
     * label set, 
     * class set
     */
    
    // nodes set
    HashSet<Integer> nodes;
    // classes set
    HashSet<String> classes;
    // labels set
    HashSet<String> labels;
    // mapping between nodes and labels (node number has label)
    HashMap<Integer,String> nodeLabelMapping;
    // labels connection Label -> list of labels which is connected to
    HashMap<String,LinkedList<String>> labelsConnections;
    // tells if graph is firected
    boolean directed;
    
    // weka apriori demands nominal values - to ensure that - each label and class has prefix
    
    // label prefix
    private static final String LABEL_PREFIX = "label_";
    
    // class prefix
    private static final String CLASS_PREFIX = "class_";

    /**
     * Init empty description
     */
    NetworkDataDescription() {
        nodes = new HashSet<Integer>();
        classes = new HashSet<String>();
        labels = new HashSet<String>();
        nodeLabelMapping = new HashMap<Integer, String>();
        labelsConnections = new HashMap<String, LinkedList<String>>();
    }
    
    /**
     * Creates Network Data Record
     * @param a1 First node number
     * @param a2 Second node number
     * @param className Record class calue
     * @return NetworkDataRecord Data record
     */
    NetworkDataRecord processRecord(int a1, int a2, String className) {
        
        // create record 
        NetworkDataRecord record = new NetworkDataRecord();
        record.attribute1 = addNode(a1);
        record.attribute2 = addNode(a2);
        record.className = addClass(className);
        record.attribute1Label = getLabel(record.attribute1);
        record.attribute2Label = getLabel(record.attribute2);
        
        // add labels connection
        LinkedList<String> connectedLabels;
        if (labelsConnections.containsKey(record.attribute1Label)) {
            connectedLabels = labelsConnections.get(record.attribute1Label);
        } else {
            connectedLabels = new LinkedList<String>();
            labelsConnections.put(record.attribute1Label, connectedLabels);
        }
        connectedLabels.add(record.attribute2Label);
        
        return record;
    }
    
    /**
     * Stores node number
     * @param node node number
     * @return 
     */
    Integer addNode(int node) {
        Integer i = null;
        nodes.add(i = new Integer(node));
        return i;
    }
    
    /**
     * Stores class
     * @param className class name
     * @return 
     */
    String addClass(String className) {
        className = NetworkDataDescription.CLASS_PREFIX + className.replaceAll("\"", "").replaceAll("\'", "");
        classes.add(className);
        return className;
    }
    
    /**
     * Stores label (with node - for mappinf)
     * @param node Node number
     * @param label Label name
     */
    void addLabel(int node, String label) {
        label = NetworkDataDescription.LABEL_PREFIX + label.replaceAll("\"", "").replaceAll("\'", "");
        nodeLabelMapping.put(addNode(node), label);
        labels.add(label);
    }
    
    String getLabel(Integer node) {
        return nodeLabelMapping.get(node);
    }
    
}
