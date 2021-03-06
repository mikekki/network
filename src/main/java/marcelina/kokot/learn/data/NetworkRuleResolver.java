/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package marcelina.kokot.learn.data;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marcelina Kokot
 */
public class NetworkRuleResolver {

    /**
     * Class is used to run PSL's groovy script.
     */
    
    // head groovy script (import, intial db and etc)
    private static final String HEAD_SCRIPT = "/groovy/head.groovy";
    // tail groovy script (gathers data)
    private static final String TAIL_SCRIPT = "/groovy/tail.groovy";
    
    // stores data result - including model
    /*
     * data[0] <- PSL model text description
     * data[1] (*) <- LinkedList of results (Object[]).
     * Each result: Object[] contains:
     *  Object[0] - test record number starting from 1,
     *  Object[1] - class value
     *  Object[2] - probability value
     */
    Object[] data;
    // Groovy script
    String groovyScript;
    
    /**
     * Method generates and runs groovy scripts.
     * @param rules Rules from NetworkWekaRulesGenrator
     * @param test Test data set
     * @return Linked list of result, see *
     * @throws Exception 
     */
    public Object resolve(LinkedList<NetworkRule> rules, NetworkDataSet test) throws Exception {
        
        // new script bind
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);
        
        // load head of script
        BufferedReader reader = new BufferedReader(new InputStreamReader(shell.getClass().getResourceAsStream(NetworkRuleResolver.HEAD_SCRIPT)));
        
        // Here is created script
        StringBuilder buffer = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            buffer.append(line).append("\n");
        }
        reader.close();
        
        // Add predicates
        buffer.append(String.format("m.add predicate: \"%s\", types: [ConstantType.UniqueID, ConstantType.String]\n", NetworkDataSet.attribute1Name));
        buffer.append(String.format("m.add predicate: \"%s\", types: [ConstantType.UniqueID, ConstantType.String]\n", NetworkDataSet.attribute2Name));
        buffer.append(String.format("m.add predicate: \"%s\", types: [ConstantType.UniqueID, ConstantType.String]\n", NetworkDataSet.className));
        
        // Add rules
        for(NetworkRule rule : rules) {
            buffer.append(this.createRuleScript(rule));
        }
        
        buffer.append(this.appendData(test.dataSet));
        
        // Weight calculation
        String upperAt1 = "" + Character.toUpperCase(NetworkDataSet.attribute1Name.charAt(0)) + NetworkDataSet.attribute1Name.substring(1);
        String upperAt2 = "" + Character.toUpperCase(NetworkDataSet.attribute2Name.charAt(0)) + NetworkDataSet.attribute2Name.substring(1);
        buffer.append(String.format("Database db = data.getDatabase(partition, [%s, %s] as Set);", upperAt1, upperAt2));
        buffer.append("LazyMPEInference inferenceApp = new LazyMPEInference(m, db, config);\ninferenceApp.mpeInference();\ninferenceApp.close();");
        
        // Load tail script
        reader = new BufferedReader(new InputStreamReader(shell.getClass().getResourceAsStream(NetworkRuleResolver.TAIL_SCRIPT)));
        while ((line = reader.readLine()) != null) {
            buffer.append(line + "\n");
        }
        reader.close();
        
        // Prepare script
        this.groovyScript = buffer.toString();
        
        PrintWriter out = new PrintWriter("groovy-script.groovy");
        out.print(this.groovyScript);
        out.close();
        
        // Run script
        this.data = (Object[])shell.run(new File("groovy-script.groovy"), new LinkedList());
        
        // Return data
        return this.data[1];
    }
    
    /**
     * Method transforms Network rule to PSL groovy script format - it is simplier than it looks.
     * Converts rule to format: premise & premise >> conclusion
     * @param rule NetworkRule Reule from NetworkWekaRuleGenerator
     * @return PSL text rule
     */
    private String createRuleScript(NetworkRule rule) {
        String at1Rule = rule.attribute1label == null ? null : String.format("%s(X, \"%s\")", NetworkDataSet.attribute1Name, rule.attribute1label);
            String at2Rule = rule.attribute2label == null ? null : String.format("%s(X, \"%s\")", NetworkDataSet.attribute2Name, rule.attribute2label);
            String classRule = rule.classValue == null ? null : String.format("%s(X, \"%s\")", NetworkDataSet.className, rule.classValue);
            String pre = "";
            String post = "";
            if (rule.ruleOutput == NetworkDataSet.className) {
                if (rule.attribute1label != null && rule.attribute2label != null) {
                    pre = String.format("%s(X, \"%s\") & %s(X, \"%s\")", NetworkDataSet.attribute1Name, rule.attribute1label, NetworkDataSet.attribute2Name, rule.attribute2label);
                } else {
                    if (rule.attribute1label != null) {
                        pre =  String.format("%s(X, \"%s\")", NetworkDataSet.attribute1Name, rule.attribute1label);
                    } else {
                        pre =  String.format("%s(X, \"%s\")", NetworkDataSet.attribute2Name, rule.attribute2label);
                    }
                }
                post =  String.format("%s(X, \"%s\")", NetworkDataSet.className, rule.classValue);
            }
            if (rule.ruleOutput == NetworkDataSet.attribute1Name) {
                if (rule.classValue != null && rule.attribute2label != null) {
                    pre = String.format("%s(X, \"%s\") & %s(X, \"%s\")", NetworkDataSet.className, rule.classValue, NetworkDataSet.attribute2Name, rule.attribute2label);
                } else {
                    if (rule.classValue != null) {
                        pre =  String.format("%s(X, \"%s\")", NetworkDataSet.className, rule.classValue);
                    } else {
                        pre =  String.format("%s(X, \"%s\")", NetworkDataSet.attribute2Name, rule.attribute2label);
                    }
                }
                post =  String.format("%s(X, \"%s\")", NetworkDataSet.attribute1Name, rule.attribute1label);
            }
            if (rule.ruleOutput == NetworkDataSet.attribute2Name) {
                if (rule.attribute1label != null && rule.classValue != null) {
                    pre = String.format("%s(X, \"%s\") & %s(X, \"%s\")", NetworkDataSet.attribute1Name, rule.attribute1label, NetworkDataSet.className, rule.classValue);
                } else {
                    if (rule.attribute1label != null) {
                        pre =  String.format("%s(X, \"%s\")", NetworkDataSet.attribute1Name, rule.attribute1label);
                    } else {
                        pre =  String.format("%s(X, \"%s\")", NetworkDataSet.className, rule.classValue);
                    }
                }
                post =  String.format("%s(X, \"%s\")", NetworkDataSet.attribute2Name, rule.attribute2label);
            }
            return String.format("m.add rule : (%s) >> (%s), weight: " + rule.confidence + "\n", pre, post);
    }
    
    
    protected String appendData(LinkedList<NetworkDataRecord> dataSet) {
        PrintWriter pw = null;
        int z = 1;
        StringBuilder buffer = new StringBuilder();
        buffer.append("def partition = data.getPartition(\"0\");\n");
        try {
            buffer.append("def insert = data.getInserter(" + NetworkDataSet.attribute1Name + ", partition);\n");
            pw = new PrintWriter(new File("label1Data"));
            for (NetworkDataRecord record : dataSet) {
                pw.println(String.format("%d %s", z++, record.attribute1Label));
            }
            pw.close();
            buffer.append("BufferedReader labelReader1 = new BufferedReader(new FileReader(\"label1Data\"));\n" +
"            String line = \"\";\n" +
"            while ((line = labelReader1.readLine()) != null) {\n" +
"                String[] vals = line.split(\" \");\n" +
"                insert.insert(Integer.parseInt(vals[0]), vals[1]);\n" +
"            }\n");
            // buffer.append(String.format("insert.insert(%d, \"%s\");\n", record.hashCode(), record.className));
            
            z = 1;
            buffer.append("insert = data.getInserter(" + NetworkDataSet.attribute2Name + ", partition);\n");
            pw = new PrintWriter(new File("label2Data"));
            for (NetworkDataRecord record : dataSet) {
                pw.println(String.format("%d %s", z++, record.attribute2Label));
            }
            pw.close();
            buffer.append("BufferedReader labelReader2 = new BufferedReader(new FileReader(\"label2Data\"));\n" +
"            line = \"\";\n" +
"            while ((line = labelReader2.readLine()) != null) {\n" +
"                String[] vals = line.split(\" \");\n" +
"                insert.insert(Integer.parseInt(vals[0]), vals[1]);\n" +
"            }\n");
            
            return buffer.toString();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetworkRuleResolver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }
        return "";
    }
    
    
}
