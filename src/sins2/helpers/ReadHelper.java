/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.helpers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * This class help us to read the input
 * @author douglas
 */
public class ReadHelper {
    
   
    private StringTokenizer tokenizer;
    private BufferedReader reader;
    
    public ReadHelper(String filePath) throws FileNotFoundException{
      reader=new BufferedReader(new FileReader(filePath));
    }
    
    

    
    
    
    public int nextInt(){
      if(hasNext())return Integer.parseInt(tokenizer.nextToken());
      return 0;
    }
    
    public double nextDouble(){
      if(hasNext())return Double.parseDouble(tokenizer.nextToken());
      return 0;
    }
    
    public String next(){
      if(hasNext())return tokenizer.nextToken();
      return "";
    }
    
    public long nextLong(){
      if(hasNext())return Long.parseLong(tokenizer.nextToken());
      return 0;
    }
    
    /**
     *TODO: control problems if the matrix if empty
     */
    public double[][] readMatrix() throws IOException{
       ArrayList<Double[]> lista=new ArrayList<Double[]>();
       
      
        String line = reader.readLine();
        String delims = "[ ]+";


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

        double[][] res = new double[lista.size()][lista.get(0).length];
        
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[0].length; j++) {
                res[i][j] = lista.get(i)[j];
            }
        }

        return res;
    }
    
    public double[][] readMatrix(int n,int m){
       double[][] matrix=new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j]=nextDouble();
            }
        }
        return matrix;
    }
    
    
    
    
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
    
    
    public void close() throws IOException{
       reader.close();       
    }
    
    
    
    
    
    
    
    
}
