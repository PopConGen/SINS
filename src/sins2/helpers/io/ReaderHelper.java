    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.helpers.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import sins2.exceptions.InputParametersExceptions;

/**
 * This class help us to read the input
 * @author douglas
 */
public class ReaderHelper implements IReaderHelper {
    
   
    protected StringTokenizer tokenizer;
    protected BufferedReader reader;
    
    public ReaderHelper(String filePath) throws FileNotFoundException{
      reader=new BufferedReader(new FileReader(filePath));
    }
    
    
    @Override
    public int nextInt(){
      if(hasNext())return Integer.parseInt(next());
      return 0;
    }
    
    @Override
    public double nextDouble(){
      if(hasNext())return Double.parseDouble(next());
      return 0;
    }

    @Override
    public boolean nextBoolean() {
        if(hasNext())return Boolean.parseBoolean(next());
        return false;
    }
    
    @Override
    public String next(){
      if(hasNext())return tokenizer.nextToken();
      return "";
    }
    
    @Override
    public long nextLong(){
      if(hasNext())return Long.parseLong(next());
      return 0;
    }
    
    /**
     *TODO: control problems if the matrix if empty
     */
    @Override
    public double[][] readMatrix() throws IOException{
        ArrayList<Double[]> lista=new ArrayList<Double[]>();
       
      
        String line = reader.readLine();
        String delims = "[ \t]+";


        try {
            while (line != null) {
                String[] tokens = line.split(delims);
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
    
    @Override
    public double[][] readMatrix(int n,int m){
       double[][] matrix=new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j]=nextDouble();
            }
        }
        return matrix;
    }
    
    
    @Override
    public boolean hasNext() {
	while (tokenizer == null || !tokenizer.hasMoreTokens()) {
		try {
			String line = reader.readLine();
			if (line == null) {
				return false;
			}
			tokenizer = new StringTokenizer(line);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}	
	return true;
}
    

    @Override
    public void close() throws IOException{
       reader.close();       
    }

    @Override
    public String readLine() {
        try {
            return reader.readLine();
        } catch (IOException ex) {
            Logger.getLogger(ReaderHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    
    
    
    
    
    
}
