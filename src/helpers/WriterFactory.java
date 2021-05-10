/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

/**
 *
 * @author douglas
 */
public class WriterFactory {
    
    public static IWriterHelper getWriterHelper(String writerId){
        
            if(writerId.compareTo("noComp")==0)
                    return new WriterHelper();
            else if(writerId.compareTo("fZip")==0)
                    return new Writer7zHelper(Writer7zHelper.CompressionType.FastCompression);
            else if(writerId.compareTo("rZip")==0)
                    return new Writer7zHelper(Writer7zHelper.CompressionType.StandarCompression);
            else if(writerId.compareTo("bZip")==0)
                    return new Writer7zHelper(Writer7zHelper.CompressionType.BestRadioCompression);     
            //the writer by default is the standar writer
            else return new WriterHelper();
        
    }
}
