/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package marcelina.kokot.learn.data;

/**
 *
 * @author Marcelina Kokot
 */
public class NetworkRule {
    
    /**
     * This class is used to represent rule.
     */
    
    // Label attribute 1 (label of first node in network file line)
    String attribute1label;
    // Label attribute 2 (label of second node in network file line)
    String attribute2label;
    // class
    String classValue;
    // Determines which value is conclusion of rule, possible values: NetworkDataSet.attribute1Name, NetworkDataSet.attributeName, NetworkDataSet.className
    String ruleOutput;
    
    // Confidence value
    double confidence;

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        String conclusion = "";
        
        if (attribute1label != null && !ruleOutput.equals(NetworkDataSet.attribute1Name)) {
            buffer.append(String.format("%s = \"%s\" &", NetworkDataSet.attribute1Name, attribute1label));
        }
        if (attribute2label != null && !ruleOutput.equals(NetworkDataSet.attribute2Name)) {
            buffer.append(String.format("%s = \"%s\" &", NetworkDataSet.attribute2Name, attribute2label));
        }
        if (classValue != null && !ruleOutput.equals(NetworkDataSet.className)) {
            buffer.append(String.format("%s = \"%s\" &", NetworkDataSet.className, classValue));
        }
        
        if (ruleOutput.equals(NetworkDataSet.attribute1Name)) {
            conclusion = String.format("%s = \"%s\"", NetworkDataSet.attribute1Name, attribute1label);
        }
        if (ruleOutput.equals(NetworkDataSet.attribute2Name)) {
            conclusion = String.format("%s = \"%s\"", NetworkDataSet.attribute2Name, attribute2label);
        }
        if (ruleOutput.equals(NetworkDataSet.className)) {
            conclusion = String.format("%s = \"%s\"", NetworkDataSet.className, classValue);
        }
        
        String conditions =  buffer.toString();
        
        return String.format("%s => %s : %f", conditions.substring(0, conditions.length() - 2), conclusion, confidence);
    }
    
    
    
}
