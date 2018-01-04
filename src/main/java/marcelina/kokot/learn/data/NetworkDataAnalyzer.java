package marcelina.kokot.learn.data;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author Marcelina Kokot
 */
public class NetworkDataAnalyzer {

    /**
     * Method allows to perform tests. It gathers all data and print them in html document.
     * @param classFile File which contains label. Be aware that network file will be searched in the same location with different suffix (_class. -> _network.)
     * @param numberOfRules Number of rules which should be used
     * @param confidence Minimal value of confidence, range: (0,1)
     * @param support Minimal value of support, range: (0,1)
     * @param split Value determines how big part will be used in learning, range (0,1) - 0.1 means that 10% will be used in learning process
     * @param adder Object which will decided how many class zero objects should be added
     * @param output Path to html output file.
     * @throws Exception 
     */
    public static void runAnalisys(String classFile, int numberOfRules, double confidence, double support, double split, NetworkClassZeroAdder adder, String output) throws Exception {
        // Data values
        int initialSize;
        int extendedSize;
        int learnSize;
        
        // load data set
        NetworkDataSet set = NetworkDataSet.getFromFile(classFile, classFile.replaceAll("_class.", "_network."), false);
        initialSize = set.dataSet.size();
        
        // add zero class
        set.addClassZero(adder);
        extendedSize = set.dataSet.size();
        
        // split data into two sets
        NetworkDataSet test = set.split(1 - split);
        learnSize = test.dataSet.size();
        
        // create rule generator
        NetworkWekaRulesGenerator generator = new NetworkWekaRulesGenerator(set);
        
        // generate rules
        LinkedList<NetworkRule> rules = generator.generateRules(numberOfRules, confidence, support);
        
        // generate PSL resolver
        NetworkRuleResolver resolver = new NetworkRuleResolver();
        
        // resolve
        LinkedList<Object[]> data = (LinkedList<Object[]>) (resolver.resolve(rules, test));
        
        // analysis - classification table
        int[][] analysis = NetworkDataAnalyzer.analyze(data, test);
        
        /**
         * Procces data - generating HTML
         */
        PrintWriter htmlDocument = new PrintWriter(output);
        htmlDocument.println("<html><body>");
        
        // Data set info
        htmlDocument.println("<h2>Data set:");
        htmlDocument.println(classFile);
        htmlDocument.println("</h2>");
        
        // Parameters
        htmlDocument.println("<h3>Parameters:</h3>");
        htmlDocument.println("<table border=1><tr><td>Param</td><td>Value</td></tr>");
        htmlDocument.println(String.format("<tr><td>%s</td><td>%s</td></tr>", "# rules", "" + numberOfRules));
        htmlDocument.println(String.format("<tr><td>%s</td><td>%s</td></tr>", "Min. conf.", "" + confidence));
        htmlDocument.println(String.format("<tr><td>%s</td><td>%s</td></tr>", "Min. support", "" + support));
        htmlDocument.println(String.format("<tr><td>%s</td><td>%s</td></tr>", "Test set", "" + 100*split + "%"));
        htmlDocument.println("</table>");
        
        // Statistics
        htmlDocument.println("<h3>Statistics:</h3>");
        htmlDocument.println("<table border=1><tr><td>Property</td><td>Value</td></tr>");
        htmlDocument.println(String.format("<tr><td>%s</td><td>%s</td></tr>", "Initial data size", "" + initialSize));
        htmlDocument.println(String.format("<tr><td>%s</td><td>%s</td></tr>", "Extended data size", "" + extendedSize));
        htmlDocument.println(String.format("<tr><td>%s</td><td>%s</td></tr>", "Learn data size", "" + learnSize));
        htmlDocument.println(String.format("<tr><td>%s</td><td>%s</td></tr>", "Classes", "" + set.dataDescription.classes.size()));
        htmlDocument.println(String.format("<tr><td>%s</td><td>%s</td></tr>", "Labels", "" + set.dataDescription.labels.size()));
        htmlDocument.println("</table>");
        
        // Results (table)
        htmlDocument.println("<h3>Results (row - actual/ column - classfied):</h3>");
        htmlDocument.println("<table border=1><tr>");
        Object[] classes = test.dataDescription.classes.toArray();
        int correct = 0;
        int sum = 0;
        for(int i = 0; i <= classes.length; i++){
            htmlDocument.println(String.format("<td>%s</td>", i == 0 ? "" : classes[i-1]));
        }
        htmlDocument.println("</tr>");
        for(int i = 1; i <= classes.length; i++){
            htmlDocument.println("<tr>");
            for(int j = 0; j <= classes.length; j++){
                if (j == 0) {
                    htmlDocument.println(String.format("<td>%s</td>", classes[i-1]));
                } else {
                    htmlDocument.println(String.format("<td>%d</td>", analysis[i-1][j-1]));
                    if (i == j) {
                        correct += analysis[i-1][j-1];
                    }
                    sum += analysis[i-1][j-1];
                }
            }
            htmlDocument.println("</tr>");
        }
        htmlDocument.println("</table>");
        
        // Overall accuracy
        htmlDocument.println("<h3>Accuracy: " + ((double)correct/(double)sum) + "</h3>");
        
        // PSL result
        htmlDocument.println("<h3>Results (PSL)</h3>");
        htmlDocument.println("<table border=1><tr><td># test data</td><td>Labels</td><td>Class</td><td>Weight</td><td>Actual class</td></tr>");
        for (int i = 0 ; i < data.size(); i++)
        {
            Object[] record = data.get(i);
            Integer testRecordNo = Integer.parseInt(record[0].toString());
            NetworkDataRecord dataRecord = test.dataSet.get(testRecordNo.intValue() - 1);
            htmlDocument.println(String.format("<tr><td>%s</td><td>%s, %s</td><td>%s</td><td>%s</td><td>%s</td></tr>", testRecordNo.toString(), dataRecord.attribute1Label, dataRecord.attribute2Label, record[1].toString(), record[2].toString(), dataRecord.className));
        }
        htmlDocument.println("</table>");
        
        // Weka rules
        htmlDocument.println("<h3>Weka genreted rules</h3>");
        htmlDocument.println("<div style='font-family: \"Lucida Console\", Monaco, monospace'>");
        htmlDocument.println(generator.wekaModelOutput.replaceAll("\\r?\\n", "<br />").replaceAll("  ", " &emsp;"));
        htmlDocument.println("</div>");
        
        // PSL model
        htmlDocument.println("<h3>PSL model</h3>");
        htmlDocument.println("<div style='font-family: \"Lucida Console\", Monaco, monospace'>");
        htmlDocument.println(((String)(resolver.data[0])).replaceAll("\\r?\\n", "<br />").replaceAll("  ", " &emsp;"));
        htmlDocument.println("</div>");
        
        // Groovy script
        htmlDocument.println("<h3>Groovy script</h3>");
        htmlDocument.println("<div style='font-family: \"Lucida Console\", Monaco, monospace'>");
        htmlDocument.println(((String)(resolver.groovyScript)).replaceAll("\\r?\\n", "<br />").replaceAll("  ", " &emsp;"));
        htmlDocument.println("</div>");
        htmlDocument.println("</body></html>");
        htmlDocument.close();
        
    }
    
    /**
     * Method analyze data from Network Rule Resolver.
     * Data format:
     * - first index describes actual class,
     * - second index describes retrived (reconised) class.
     * For instance:
     * result[0][1] = 45 means that 45 records with class 0 were classified as 1.
     * 0 and 1 class names can be retrieved from Data set by function classes()
     * @param result Data oobject from Network Rule Resolver
     * @param test Test data
     * @return table of classification
     */
    public static int[][] analyze(Object result, NetworkDataSet test) {
        // get test records number
        int size = test.dataSet.size();
        
        // get classes number
        int classesNum = test.dataDescription.classes.size();
        
        // mapping class name to int
        HashMap<String, Integer> classToIntMap = new HashMap<String, Integer>();
        
        // generate mapping
        int it = 0;
        for(String classString : test.dataDescription.classes){
            classToIntMap.put(classString, it++);
        }
        
        // Cast data to list
        LinkedList<Object[]> resultData = (LinkedList<Object[]>) result;
        // classification array - index is test record number - 1, value is retrived class
        String[] classification = new String[size];
        // each record highest probablity value
        double[] values  = new double[size];
        
        for (Object[] o : resultData) {
            int testId = ((Integer)o[0]).intValue() - 1;
            double value = ((Double)o[2]).doubleValue();
            // strip from ' - PSL adds it
            String classify = o[1].toString().replaceAll("'", "");
            // if probability is higher - change record classification
            if (values[testId] < value) {
                values[testId] = value;
                classification[testId] = classify;
            }
        }
        
        // result data
        int[][] results = new int[classesNum][classesNum];
        
        for(int i = 0; i < classification.length; i++) {
            if (classification[i] != null) {
                // actual record class
                int real = classToIntMap.get(test.dataSet.get(i).className);
                // retrived class
                int classfied = classToIntMap.get(classification[i]);
                // add single classification result
                results[real][classfied]++;
            }
        }
        
        return results;
    }
    
    public static void main(String[] args) throws Exception {
        //NetworkDataAnalyzer.runAnalisys("C:\\mgr_data\\d_CSphd_class.txt", 20, 0.001, 0.001, 0.5, NetworkClassZeroAdder.getRandomZeroAdder(800), "C:\\mgr_data\\AAAA2_report.html");
    }
    
}
