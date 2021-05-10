/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.sampling_class;

import java.util.Iterator;
import java.util.List;
import sins2.simulation.Individual;

/**
 *
 * @author douglas
 */
public interface ISampler {
    
     public Iterator<Individual>  getSampled(int i_deme,int j_deme,List<Individual> males,
                List<Individual> females);
     
     public List<Individual> getResults();
}
