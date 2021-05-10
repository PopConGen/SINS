/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.sampling_class;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import sins2.simulation.Individual;

/**
 *
 * @author douglas&tiago
 *
 * This class represent
 */
public class FixedSizeSubsetSampler implements ISampler {

    int[][] _sampleSize;

    public List<Individual> _results;
    
    public FixedSizeSubsetSampler(int[][] size) {
        _sampleSize = size;

    }

    public int[][] getSampleSize() {
        return _sampleSize;
    }
    
    @Override
    public Iterator<Individual> getSampled(int i_deme, int j_deme, List<Individual> males,
            List<Individual> females) {
        //count = number of individuals that we want to sample/save information about
        int numberOfSamples = _sampleSize[i_deme][j_deme];

        //number of males we want to save
        int numberOfMaleSamples= numberOfSamples/2;
        //number of females we want to save
        int numberOfFemaleSamples  = numberOfSamples/2;
        
        _results = new ArrayList<>(numberOfSamples);
 
        //if number of samples == 0 then return empty
        if (numberOfSamples == 0) {
            return _results.iterator();
        } else {
            //if number of males we want to save is larger than the total number of males in the deme, add them all
            if (numberOfMaleSamples > males.size()) {
                _results.addAll(males);
            } else {
                shuffleSampleSortIndividualsIndex(_results, males, numberOfMaleSamples);
            }
            if (numberOfFemaleSamples > females.size()) {
                _results.addAll(females);
            } else {
                shuffleSampleSortIndividualsIndex(_results, females, numberOfFemaleSamples);
            }
        }
        return _results.iterator();
    }

    public void shuffleSampleSortIndividualsIndex(List<Individual> results, List<Individual> individuals, int numberOfSamples) {
        
        
        int numberOfIndividuals = individuals.size();
        ArrayList<Integer> sampledIndividuals = new ArrayList<>(numberOfIndividuals);
        //for each individual, assign an ID number and save it to a list
        for (int i = 0; i < numberOfIndividuals; i++) {
            sampledIndividuals.add(i);
        }

        //create a list of indexes and shuffle them for both sexes
        Collections.shuffle(sampledIndividuals);

        //while there are more numbers than desired samples, remove first number
        //TODO: check if i can do this faster
        while (sampledIndividuals.size() > numberOfSamples) {
            sampledIndividuals.remove(sampledIndividuals.size()-1);
        }
        
        //after having the list with the desired number of samples, sort them once again
        Collections.sort(sampledIndividuals);
        
        for (int i = 0; i < sampledIndividuals.size(); i++) {
                results.add(individuals.get(sampledIndividuals.get(i)));
            } 
    }

    @Override
    public List<Individual> getResults() {
        return _results;
    }    
}
