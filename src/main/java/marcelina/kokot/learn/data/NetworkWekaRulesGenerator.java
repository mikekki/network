/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package marcelina.kokot.learn.data;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import weka.associations.Apriori;
import weka.associations.AprioriItemSet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

/**
 *
 * @author Marcelina Kokot
 */
public class NetworkWekaRulesGenerator {

    /**
     * Class is used to generate rules from weka apriori algorithm
     */
    
    // Generated instances
    private Instances data;
    // Atoms - should containt attribute and classname 
    private LinkedList<String> atoms;
    // possible values for each atom
    private LinkedList<LinkedList> values;
    // weka output
    String wekaModelOutput;
    
    public NetworkWekaRulesGenerator(NetworkDataSet networkDataSet) throws Exception {
        // retrieve data set
        this.data = networkDataSet.getWekaDataSource();
        // set class
        data.setClassIndex(-1);
        // empty atoms
        atoms = new LinkedList<String>();
        // empty values
        values = new LinkedList<LinkedList>();
        Enumeration<Attribute> attributes = data.enumerateAttributes();
        // loop retrives atoms and values
        while (attributes.hasMoreElements()) {
            Attribute a = attributes.nextElement(); 
            atoms.add(a.name());
            LinkedList<String> val = new LinkedList<String>();
            for (int i = 0; i < a.numValues(); i++) {
                val.add(a.value(i));
            }
            values.add(val);   
        }
    }
    
    /**
     * Method is used to generate rules using weka apriori algorithm.
     * @param rulesNumber Number of rules.
     * @param minimalConfidence Minimal confidence value.
     * @param minimalSupport Minimal support value.
     * @return List of network rules (processed).
     * @throws Exception 
     */
    public LinkedList<NetworkRule> generateRules(int rulesNumber, double minimalConfidence, double minimalSupport) throws Exception {
        Apriori algorithm = new Apriori();
        // algorithm settings
        String[] options = new String[]{
            "-N", "" + rulesNumber, // rule #
            "-T", "0",
            "-C", "" + minimalConfidence, // confidence minimal value
            "-D", "0.05",
            "-U", "1.0",
            "-M", "" + minimalSupport, // support minimal support
            "-S", "-1.0",
            "-c", "-1"    
        };
        // set options
        algorithm.setOptions(options);
        // build rules
        algorithm.buildAssociations(data);
        FastVector[] rules = algorithm.getAllTheRules();
        
        // Process rules - retrieve rules from sets
        AprioriItemSet[] premises = new AprioriItemSet[rules[0].size()];
        AprioriItemSet[] conclusions = new AprioriItemSet[rules[0].size()];
        Double[] weights = new Double[rules[0].size()];
        
        Enumeration<AprioriItemSet> enumerationP = rules[0].elements();
        Enumeration<AprioriItemSet> enumerationC = rules[1].elements();
        Enumeration<Double> enumerationW = rules[2].elements();
        
        LinkedList<NetworkRule> result = new LinkedList<NetworkRule>();
        
        // weka algorithm description
        this.wekaModelOutput = algorithm.toString();
        
        int i = 0;
        while (enumerationP.hasMoreElements()) {
            /**
             * Here is weka related format
             */
            premises[i] = enumerationP.nextElement();
            conclusions[i] = enumerationC.nextElement();
            weights[i] = enumerationW.nextElement();
            
            int[] conclusionItems = conclusions[i].items();
            
            for(int cn = 0; cn < conclusionItems.length; cn++) {
                int valConclusion = conclusionItems[cn];
                if (valConclusion != -1) {
                    
                    NetworkRule rule = new NetworkRule();
                    
                    String attributeConclusion = atoms.get(cn);
                    String valueConclusion = values.get(cn).get(valConclusion).toString();
                    
                    if (attributeConclusion.equals(NetworkDataSet.attribute1Name)) {
                        rule.attribute1label = valueConclusion;
                        rule.ruleOutput = NetworkDataSet.attribute1Name;
                    }
                    if (attributeConclusion.equals(NetworkDataSet.attribute2Name)) {
                        rule.attribute2label = valueConclusion;
                        rule.ruleOutput = NetworkDataSet.attribute2Name;
                    }
                    if (attributeConclusion.equals(NetworkDataSet.className)) {
                        rule.classValue = valueConclusion;
                        rule.ruleOutput = NetworkDataSet.className;
                    }

                    int[] items = premises[i].items();
                    for (int n = 0; n < items.length; n++) {
                        int val = items[n];
                        if (val != -1) {
                            String attribute = atoms.get(n);
                            String value = values.get(n).get(val).toString();
                            if (attribute.equals(NetworkDataSet.attribute1Name)) {
                                rule.attribute1label = value;
                            }
                            if (attribute.equals(NetworkDataSet.attribute2Name)) {
                                rule.attribute2label = value;
                            }
                            if (attribute.equals(NetworkDataSet.className)) {
                                rule.classValue = value;
                            }
                        }
                    }
                    rule.confidence = weights[i];
                    
                    result.add(rule);
                }
            }
            i++;
        }
        return this.filterRules(result);
       
    }
    
    public LinkedList<NetworkRule> filterRules(LinkedList<NetworkRule> input) {
        NetworkRule[] rules = new NetworkRule[input.size()];
        rules = input.toArray(rules);
        LinkedList<NetworkRule> output = new LinkedList<NetworkRule>();
        for (int z = rules.length - 1; z >=0; z--) {
            boolean unique = true;
            for (int y = 0; y < z && unique; y++) {
                unique = !rules[z].samePredicates(rules[y]);
            }
            if (unique) {
                output.add(rules[z]);
            }
        } 
        Collections.reverse(output);
        return output; 
    }
    
}
