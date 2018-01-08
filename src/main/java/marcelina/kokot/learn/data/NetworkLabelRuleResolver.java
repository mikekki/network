/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marcelina.kokot.learn.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marcelina
 */
public class NetworkLabelRuleResolver extends NetworkRuleResolver {
    
    
    protected String appendData(LinkedList<NetworkDataRecord> dataSet) {
        PrintWriter pw = null;
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append("def partition = data.getPartition(\"0\");\ndef insert = data.getInserter(" + NetworkDataSet.className + ", partition);\n");
            pw = new PrintWriter(new File("classData"));
            int z = 0;
            for (NetworkDataRecord record : dataSet) {
                pw.println(String.format("%d %s", NetworkIdMapper.toId(record), record.className));
            }
            pw.close();
            buffer.append("BufferedReader labelReader = new BufferedReader(new FileReader(\"classData\"));\n" +
"            String line = \"\";\n" +
"            while ((line = labelReader.readLine()) != null) {\n" +
"                String[] vals = line.split(\" \");\n" +
"                insert.insert(Integer.parseInt(vals[0]), vals[1]);\n" +
"            }\n");
            // buffer.append(String.format("insert.insert(%d, \"%s\");\n", record.hashCode(), record.className));
            return buffer.toString();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetworkLabelRuleResolver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }
        return "";
    }
}
