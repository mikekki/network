package marcelina.kokot.learn.data;

/**
 *
 * @author Marcelina Kokot
 */
class NetworkDataRecord {
    
    /**
     * Class represent one data record which consists on two nodes number and class
     */
    
    // This value is used to calculate hash - which allows faster data search
    private static final int MAX_ATTRIBUTE_NUMBER = 1000000;

    // First node number
    Integer attribute1;
    // Second node number
    Integer attribute2;
    // Class
    String className;
    
    // First node label
    String attribute1Label;
    // Second node label
    String attribute2Label;

    NetworkDataRecord() {
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NetworkDataRecord && 
                attribute1 == ((NetworkDataRecord)obj).attribute1 &&
                attribute2 == ((NetworkDataRecord)obj).attribute2;
    }

    @Override
    public int hashCode() {
        return NetworkDataRecord.MAX_ATTRIBUTE_NUMBER * attribute1.intValue()
                + attribute2.intValue();
    }       
}
