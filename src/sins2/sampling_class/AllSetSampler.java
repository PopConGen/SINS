/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.sampling_class;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sins2.simulation.Individual;
import sins2.collections_utils.IteratorWrapper;

/**
 *
 * @author douglas
 */
public class AllSetSampler implements ISampler{

    public List<Individual> _results;
    
    @Override
    public Iterator<Individual> getSampled(int i_deme, int j_deme, List<Individual> males, List<Individual> females) {
        _results = new ArrayList<>(males.size()+females.size());
        _results.addAll(males);
        _results.addAll(females);
        return new IteratorWrapper<>(_results.iterator());
        //return new IteratorWrapper<Individual>(males.iterator(),females.iterator());
    }
    
    @Override
    public List<Individual> getResults() {
        return _results;
    }
}
