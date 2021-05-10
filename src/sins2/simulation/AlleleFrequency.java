/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.simulation;



/**
 *
 * @author douglas
 */
public class AlleleFrequency {
    
     private double[] frecuency;
     private int[][] secuence;//maybe thi array can be of bite instead
     private int nbAlleles;
    
    //other information
    
    public AlleleFrequency(){
    
    }

    public AlleleFrequency(int[][] secuence, double[] frecuency) {
        this.secuence=secuence;
        this.frecuency=frecuency;
        nbAlleles=secuence.length;
    }
    
    /*
    public void readAlleleFrecuencyFromFile(String path,int alleleLenght) 
            throws  IOException{
      File f=new File(path);
      if(f.exists()){
        ReadHelper reader=new ReadHelper(path);
      reader.next();
      nbAlleles=reader.nextInt();
      secuence=new int[nbAlleles][alleleLenght];
      frecuency=new double[nbAlleles];
      
        for (int i = 0; i < nbAlleles; i++) {
            frecuency[i]=reader.nextDouble();
            secuence[i]=new int[alleleLenght];
            
            for (int j = 0; j < alleleLenght; j++) {
                secuence[i][j]=reader.nextInt();
            }
        }
      
      
      reader.close();
      }
      else{
          //if the file does not exist then ther are not variance in the population
          secuence = new int[1][alleleLenght];
          frecuency = new double[1];
          frecuency[0] = 1;
          for (int j = 0; j < alleleLenght; j++) {
              secuence[0][j] = 0;
          }
      }
      
    }
    */
    
    public double getFrequency(int i){
       return frecuency[i];
    }
    
    public int[] getAllele(int i){
      return secuence[i];
    }
    
    /**
     * @author: Tiago
     * gets number of alleles
     */
    public int getNumberOfAlleles(){
        return nbAlleles;
    }
}
