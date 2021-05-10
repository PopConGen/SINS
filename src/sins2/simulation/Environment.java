/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.simulation;



/**
 * This class contain all the information about the environment
 * where a population evolve
 * @author douglas
 */
public /*final*/ class Environment {//Outcomment "final" keyword for Unit testing
    
    private double[][][] K;
    private double[][][] F;
    private double[][][] FSum;
    
    
    private static int[] dx=new int[]{1,-1,0,0};
    private static int[] dy=new int[]{0,0,1,-1};
    
    private int n,m;//dimensions of the simulation board
    
    
    
    
    public Environment(){
      K=F=FSum=null;
      n=m=0;
    }
    
    /*
     *
     */
    public Environment(double[][][] F, double[][][] K) {

        n = F[0].length;
        m = F[0][0].length;
        int layersCount = F.length;

        FSum = new double[layersCount][n][m];
        this.F = new double[layersCount][n][m];
        this.K = new double[layersCount][n][m];
        //copy F and K
        for (int layer = 0; layer < layersCount; layer++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    this.F[layer][i][j] = F[layer][i][j];
                    this.K[layer][i][j] = K[layer][i][j];
                }
            }
        }

        //computing FSum that not have any sense because that field is only used to 
        //calculate the directions so maybe should be in the Layer class not here
        for (int k = 0; k < layersCount; k++) {
            //System.out.println("@Env layersCount=" + layersCount);
            //creating matrixes
            //FSum[0] = new double[n][m];
            /*Tiago: HUGE BUG? should be FSum[k]=new double[n][m] OR just removed from here.
            This basically makes it so that, everytime the for loop loops, the FSum[0] is re-initialized making all of its values 0*/
            int newX, newY;

            //TODO: If I dont need F to any other computation that to compute FSum I can eliminate F as a variable of this class
            if (n == 1 && m == 1) {
                FSum[k] = F[k];
            } else {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < m; j++) {
                        FSum[k][i][j] = 0;

                        //analising all neigthbors to compute FSum
                        for (int direction = 0; direction < 4; direction++) {/*Tiago: Here is where we choose (or scroll through)
                             the 4 different directions: north, west, south, east
                             The order by which we go through them is: East -> West -> South -> North*/

                            newX = i + dx[direction];
                            newY = j + dy[direction];
                            if (newX >= 0 && newX < n && newY >= 0 && newY < m) {/*Tiago: Here is where we deal with the boundaries (corners and edges)
                                 That is, the new coordinates (newX,newY) must be greater or equal to 0(zero) and smaller than the length of the table*/

                                //if a want to aavoid migrations in an specific direction I just can push a value of F less than 0 in that deme

                                if (F[k][newX][newY] >= 0 && F[k][newX][newY] < 1) {/*Tiago: Here is where we check our Friction condition
                                     The friction of the deme in the new coordinates must be greater or equal to 0 and strictly smaller than 1*/

                                    //in that way FSum will containg the value n_d-F_t in page 5 of the user manual

                                    FSum[k][i][j] += (1 - F[k][newX][newY]);
                                    /*Tiago - in short: we have 4 directions, if in any of these direction we cant migrate to, we take away that direction.
                                     then for each direction that is possible to migrate to, we check if F is within the permited interval, if not
                                     we take away that direction, in the end we make a sum of (1-Fdir) for all the possible dir(ections). That
                                     will be our final (nd - Ft).
                                     eg: nd - Ft = (1-F(north))+(1-F(south)), where only north and south are possible direction to migrate to and 
                                     within the desired F values.*/
                                    //System.out.println("@Environment.CONSTRUCTOR: FSum["+k+"]["+i+"]["+j+"]="+FSum[k][i][j]+ "\tn="+n+"\tm="+m);
                                }
                            }
                        }
                    }
                }

                //TODO: I dont understant why to do the next cycle
                /*Tiago
                 for (int i = 0; i < n; i++) {
                 for (int j = 0; j < m; j++) {
                 if(FSum[k][i][j]==0)FSum[k][i][j]=1;
                 FSum[k][i][j]+=0.01;
                 }
                 }*/
            }

        }
    }
    

    
    /**
     * Javadoc author: Tiago
     * 
     * @param layerId
     * @param i
     * @param j
     * @return True if the values of F are equal or greater than 0 and strictly smaller than 1, False otherwise
     */
    public boolean isPossibleMigrate(int layerId, int i, int j) {
        /*System.out.println("@Environment.isPossibleMigrate\n"
                + "F[layerId][" + i + "][" + j + "]=" + F[layerId][i][j] + "\n"
                + "F[layerId][i][j]>=0&&F[layerId][i][j]<1=" + (F[layerId][i][j] >= 0 && F[layerId][i][j] < 1)
                + "\nEND Environment.isPossibleMigrate");*/
        return F[layerId][i][j] >= 0 && F[layerId][i][j] < 1;
    }
    
    public double getF(int layerId,int i,int j){
       return F[layerId][i][j];
    }
    
    public double getK(int layerId,int i,int j){
       return K[layerId][i][j];
     }
     
    public double getFSum(int layerId,int i,int j){
       return FSum[layerId][i][j];
     }
    
    public int getRowSize() {
        return n;
    }

    public int getColumnSize() {
        return m;
    }
    
    public Environment cloneEnvironment(){
           
          Environment clone=new Environment();
          
          clone.K=new double[K.length][n][m];
          clone.F=new double[K.length][n][m];
          clone.FSum=new double[K.length][n][m];
          
          clone.n=n;
          clone.m=m;
          
          for (int i = 0; i < K.length; i++) {
              for (int j = 0; j < n; j++) {
                  for (int k = 0; k < m; k++) {
                      clone.K[i][j][k]=K[i][j][k];
                      clone.F[i][j][k]=F[i][j][k];
                      clone.FSum[i][j][k]=FSum[i][j][k];
                  }
              }
          }
          
          return clone;
          
    }
}
