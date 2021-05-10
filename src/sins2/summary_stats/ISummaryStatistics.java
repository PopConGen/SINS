/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.summary_stats;

import java.util.HashMap;

/**
 *
 * @author tiagomaie <tiagomaie at igc>
 */
public interface ISummaryStatistics {
    
    /**
     * @param zomeSummaryInformation Allele frequencies for all alleles of a given zome/marker.
     * The "key" is the allele (e.g. "0 1 1 0 1 0" for a sequence or "498" for a microsat) 
     * and the "value" is its relative frequency in relation to all the
     * other keys.
     * @return Heterozygosity value for the given zome/marker.
     */
    double calcHeterozygosity(HashMap<String, Integer> zomeSummaryInformation);
    /**
     * @param zomeSummaryInformation Allele frequencies for all alleles of a given zome/marker.
     * The "key" is the allele (e.g. "0 1 1 0 1 0" for a sequence or "498" for a microsat) 
     * and the "value" is its relative frequency in relation to all the
     * other keys.
     * @return Mean allele frequency for the given zome/maker
     */
    double calcMeanAlleleFreq(HashMap<String, Integer> zomeSummaryInformation);
    /**
     * @param zomeSummaryInformation Allele frequencies for all alleles of a given zome/marker.
     * The "key" is the allele (e.g. "0 1 1 0 1 0" for a sequence or "498" for a microsat) 
     * and the "value" is its relative frequency in relation to all the
     * other keys.
     * @return Absolute number of alleles for the given zome/marker
     */
    double calcNumAlleles(HashMap<String, Integer> zomeSummaryInformation);
    /**
     * @param layerZomeSummaryInformation All the zomes/markers for a given population,
     * along with the allele frequencies for all alleles of these zomes/markers. 
     * The "key" is the numberId for a given marker, the "value" is a hashmap of
     * allele frequencies for all alleles of the given zome/marker.
     * @return Overall/Mean heterozygosity for a given population.
     */
    double calcOverallHet(HashMap<Integer,HashMap<String, Integer>> layerZomeSummaryInformation);
    
    
    
}
