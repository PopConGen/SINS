/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.helpers.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import sins2.exceptions.InputParametersExceptions;

/**
 *
 * @author douglas
 */
public class ReaderReplaceHelper extends ReaderHelper{
    
    
    private Map<String, String> _argsMapper;
    
    public ReaderReplaceHelper(String filePath
            ,String inputFilePath,String mappingFilePath) 
            throws FileNotFoundException{
         super(filePath);
         //input the file to do mapping
         ReaderHelper mappingFileReader=new ReaderHelper(mappingFilePath);
         ReaderHelper inputFileReader=new  ReaderHelper(inputFilePath);
         //read the input file and load the input
         _argsMapper=new HashMap<String, String>();
         while(inputFileReader.hasNext()){
             _argsMapper.put(mappingFileReader.next()
                     , inputFileReader.next());
         }
    }
    
    public ReaderReplaceHelper(String filePath,String mappingFile) throws FileNotFoundException
    {
       this(filePath,"input",mappingFile);
    }
    
    
    public ReaderReplaceHelper(String filePath
            ,String mappingFile,int inputId) throws FileNotFoundException{
        this(filePath,"input"+inputId,mappingFile) ;       
    }
    
    
    @Override
    public String next(){
      if(hasNext()){
          String response=super.tokenizer.nextToken();
          if(response.startsWith("%"))
              return _argsMapper.get(response.substring(1));
          else return response;
      }
      return "";
    }

    @Override
    public double[][] readMatrix() throws IOException {
        
        ArrayList<Double[]> lista = new ArrayList<Double[]>();
        
        String line = reader.readLine();
        String delims = "[ \t]+";
        //delimiter = any number of spaces or tabs

        try {
            while (line != null) {
                String[] tokens = line.split(delims);

                //replace process
                for (int i = 0; i < tokens.length; i++) {
                    if(tokens[i].startsWith("%"))
                        tokens[i]=_argsMapper.get(tokens[i].substring(1));                    
                }
                
                Double[] res = new Double[tokens.length];

                for (int i = 0; i < tokens.length; i++) {
                    res[i] = Double.parseDouble(tokens[i]);
                }
                lista.add(res);
                line = reader.readLine();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //This block verifies if all the lines inside a map are of the same size
        int tmp_size=0;
        for (Double[] linesInMap : lista) {
            if (tmp_size == 0){
                tmp_size = linesInMap.length;
            }else{
                if (tmp_size != linesInMap.length){
                    try {
                        throw new InputParametersExceptions("Environmental maps have lines with different length.");
                    } catch (InputParametersExceptions ex) {
                        Logger.getLogger(ReaderReplaceHelper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
                
        }
        
        
        double[][] res = new double[lista.size()][lista.get(0).length];

        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[0].length; j++) {
                res[i][j] = lista.get(i)[j];
            }
        }

        return res;

    }

}
