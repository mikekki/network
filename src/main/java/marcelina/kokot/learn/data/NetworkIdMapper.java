/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marcelina.kokot.learn.data;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Marcelina
 */
public class NetworkIdMapper {
    
    private static Map<String,Integer> edgeToId;
    private static Map<Integer,NetworkDataRecord> idToEdge;
    private static int counter;
    
    public static void restart()
    {
        edgeToId = new TreeMap<String, Integer>();
        idToEdge = new TreeMap<Integer, NetworkDataRecord>();
        counter = 0;
    }
    
    public static void register(NetworkDataRecord record) 
    {
        String sId = getId(record);
        if (edgeToId.containsKey(sId)) {
            return ;
        }
        int cId = counter++;
        edgeToId.put(sId, cId);
        idToEdge.put(cId, record);
    }
    
    public static NetworkDataRecord toRecord(int id) {
        return idToEdge.get(id);
    }
    
    public static int toId(NetworkDataRecord record) {
        return edgeToId.get(getId(record));
    }
    
    private static String getId(NetworkDataRecord record) {
        return String.format("%d-%d", 
                record.attribute1,//record.attribute1 < record.attribute2 ? record.attribute1 : record.attribute2, 
                record.attribute2);//record.attribute1 < record.attribute2 ? record.attribute2 : record.attribute1 );
    }
}
