/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.helpers.io;

import java.io.IOException;

/**
 *
 * @author douglas
 */
public interface IReaderHelper {

    void close() throws IOException;

    boolean hasNext();
    
    String next();

    double nextDouble();
    
    boolean nextBoolean();

    int nextInt();

    long nextLong();

    /**
     *TODO: control problems if the matrix if empty
     */
    double[][] readMatrix() throws IOException;

    double[][] readMatrix(int n, int m);
    
    String readLine();
}
