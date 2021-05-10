/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.summary_stats;

import java.util.ArrayList;
import java.util.HashMap;
import sins2.helpers.io.IWriterHelper;
import sins2.simulation.Individual;
import sins2.simulation.Layer;
import sins2.simulation.Sequence;

/**
 *
 * @author tiagomaie <tiagomaie at igc>
 */
public class ComputeSummaryStats implements ISummaryStatistics {

    private final IWriterHelper _outputManager;
    
    public ComputeSummaryStats(IWriterHelper outputManager) {
        _outputManager = outputManager;
    }
    

    public void printSummaryStatistics(/*IWriterHelper outputManager,*/int generation, int simulationId, Layer[] layers){

        ArrayList<HashMap<Integer,HashMap<String, Integer>>> layerZomeArray = new ArrayList<>();
        /**
         * for each layer
         *   add all its individuals to a single arraylist
         *   
         *   for each of the autosomes in a layer
         *     save in a hashmap the allele frequency for each zome and person
         */
        for(Layer layer : layers){
            ArrayList<Individual> allIndividuals = new ArrayList<>();
            for(int i = 0; i < layer.getRowSize(); i++){
                for (int j = 0; j < layer.getColumnSize(); j++) {
                    allIndividuals.addAll(layer.getDeme(i, j).getMales());
                    allIndividuals.addAll(layer.getDeme(i, j).getFemales());
                }
            }

            HashMap<Integer,HashMap<String, Integer>> zomeMapIdFreq = new HashMap<>();
        
            for (int zomeId = 0; zomeId < layer.getGenotypeInformation().getNbAutosomes() + 3; zomeId++) {
                //HashMap<Integer, Integer> zomesFreqMSats = new HashMap<>();
                HashMap<String, Integer> zomesFreq = new HashMap<>();
                for (Individual person : allIndividuals) {
                    for (Sequence allele : person.getGenotype()[zomeId].getZome()) {
                        String alleleToAdd = allele.getRepr().trim();
                        if(!(zomeId == 1 && !person.isMale())){
                            if (!zomesFreq.containsKey(alleleToAdd)) {
                                    zomesFreq.put(alleleToAdd, 1);
                            } else {
                                    zomesFreq.put(alleleToAdd, zomesFreq.get(alleleToAdd) + 1);
                            }
                        }
                    }
                }
                zomeMapIdFreq.put(zomeId, zomesFreq);
            }

        layerZomeArray.add(zomeMapIdFreq);
        
        }
        printSummaryStats(/*_outputManager,*/generation,layerZomeArray);

    }
    
    private String buildSummaryInformation(
            ArrayList<HashMap<Integer,HashMap<String, Integer>>> layerZomeArray,
            int generation){
        
        StringBuilder statRepresentation = new StringBuilder();
        
        for(int layerId = 0; layerId < layerZomeArray.size(); layerId++){
            for (Integer zomeId : layerZomeArray.get(layerId).keySet()) {
                //layerID generation marker Het overallHet(if(!X/Y/MT)) meanAlleleFreq numAlleles
                statRepresentation.append(layerId).append("\t");
                statRepresentation.append(generation).append("\t");
                if(zomeId == 0 ) statRepresentation.append("X").append("\t");
                else if(zomeId == 1) statRepresentation.append("Y").append("\t");
                else if(zomeId == 2) statRepresentation.append("MT").append("\t");
                else statRepresentation.append("A").append(zomeId-2).append("\t");
                //Heterozygosity
                statRepresentation.append(calcHeterozygosity(layerZomeArray.get(layerId).get(zomeId))).append("\t");
                //Overall heterozygosity
                if(zomeId>2) statRepresentation.append(calcOverallHet(layerZomeArray.get(layerId))).append("\t");
                else statRepresentation.append("NA").append("\t");
                //Mean allele frequency
                statRepresentation.append(calcMeanAlleleFreq(layerZomeArray.get(layerId).get(zomeId))).append("\t");
                //Number of alleles
                statRepresentation.append(calcNumAlleles(layerZomeArray.get(layerId).get(zomeId))).append("\t");
                statRepresentation.append("\r\n");
                
            }
        }
        
        return statRepresentation.toString();
    }
    
    private void printSummaryStats(/*IWriterHelper outputManager,*/
            int generation,
            ArrayList<HashMap<Integer,HashMap<String, Integer>>> layerZomeArray
            ){
        
        //TODO: find a better way to add/maintain statistics
        //HashMap<String, ISummaryStatistics> summaryStatsInUse = new HashMap<>();
        //LayerId Generation MarkerId Stat1 Stat2 ... StatN
        String[] availableStats = {
            "Heterozygosity",
            "OverallHetAutozomes",
            "MeanAlleleFrequency",
            "NumAlleles"            
        };
        
        _outputManager.openSumStatsRecord("SummaryStatistics", generation);
        //Print the header on the first generation
        if(generation == 0) _outputManager.printSummaryStatsHeader(availableStats);
        _outputManager.printSummaryStatsLine(buildSummaryInformation(layerZomeArray,generation));
        _outputManager.closeSumStatsCurrentRecord();
        
    }
    
    @Override
    public double calcHeterozygosity(HashMap<String, Integer> zomeSummaryInformation) {
        /*
                for(Integer values : zome.values())
                    tot += values;
                the line below does the same as the for-loop above
        int tot = zome.values().stream().reduce(0, (a, b) -> a+b);
         */
        
        /*
        expected heterozygosity single locus = 1 - SUM(_i=1)(^k)(p(_i)²)
        where p(_i) is the freq of the i^th of k alleles.
        */
        int tot = zomeSummaryInformation.values().stream().reduce(0, (a, b) -> a + b);
        double powAlleleFreq = 0;

        for (String key : zomeSummaryInformation.keySet()) {
            double alleleFrequency = (1.0 * zomeSummaryInformation.get(key) / tot);
            powAlleleFreq += Math.pow(alleleFrequency, 2);
        }
        double expHet = 1.0 - powAlleleFreq;
        return expHet;

    }
    
    @Override
    public double calcOverallHet(HashMap<Integer,HashMap<String, Integer>> layerZomeSummaryInformation){
        /**
         * Let "tot" be the number of alleles at locus i, 
         * with a total of N=layerZomeSummaryInformation.size()-3 loci, alleleFrequency
         * is the allele frequency for all the individuals in this layer, the overall
         * heterozygosity is:
         * (1/N) * SUM[N](1 - SUM[i](alleleFrequency²))
         * 
         */
        
        double expHet = 0;
        //start from 3 because at this point we are not counting with zomes X/Y/MT
        for(int i = 3; i < layerZomeSummaryInformation.size();i++){
            HashMap<String, Integer> zomeSummaryInformation = layerZomeSummaryInformation.get(i);
            
            int tot = zomeSummaryInformation.values().stream().reduce(0, (a, b) -> a + b);
            double powAlleleFreq = 0;
            for(String key : zomeSummaryInformation.keySet()) {
                double alleleFrequency = (1.0 * zomeSummaryInformation.get(key) / tot);
                powAlleleFreq += Math.pow(alleleFrequency, 2);
            }
            expHet += (1.0 - powAlleleFreq);
        }
        expHet = (expHet/(layerZomeSummaryInformation.size()-3));
        return expHet;
    }
    
    @Override
    public double calcMeanAlleleFreq(HashMap<String, Integer> zomeSummaryInformation){
        
        int tot = zomeSummaryInformation.values().stream().reduce(0, (a, b) -> a+b);
        double freqTotal = 0;
        
         for(String key : zomeSummaryInformation.keySet()){
            freqTotal += (1.0*zomeSummaryInformation.get(key)/tot);
            
        }
        double meanAlleleFreq = freqTotal/zomeSummaryInformation.keySet().size();
        
        return meanAlleleFreq;
    }
    
    @Override
    public double calcNumAlleles(HashMap<String, Integer> zomeSummaryInformation){
      
        return zomeSummaryInformation.keySet().size();
    }
    
}
