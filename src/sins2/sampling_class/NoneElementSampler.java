/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.sampling_class;

//import EDU.oswego.cs.dl.util.concurrent.FJTask;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sins2.simulation.Individual;
import sins2.collections_utils.EmptyIterator;

/**
 *
 * @author douglas
 */
public class NoneElementSampler implements ISampler{

    List<Individual> _results;
    
    @Override
    public Iterator<Individual> getSampled(int i_deme, int j_deme, List<Individual> males, List<Individual> females) {
        return new EmptyIterator<>();
    }

    @Override
    public List<Individual> getResults() {
        _results = new ArrayList<>();
        return _results;
    }

}
