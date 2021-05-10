/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.helpers.io;

import java.util.HashMap;
import java.util.LinkedHashMap;
import sins2.simulation.Layer;

/**
 *
 * @author douglas
 */
public interface IWriterHelper {
    
    static enum WriterHelperType {NOCOMP, SQL, ZIP;}
    
    void printSimulationOptions(LinkedHashMap<String, HashMap> projectOptions);

    void beginToWriteSimulation(String projectName, int simulationId, Layer... layers);

    void closeCurrentRecord();
    
    void closeSumStatsCurrentRecord();

    void finishedWriteOutput();

    void openRecord(String id,int generation);
    
    void openSumStatsRecord(String geneName, int generation);

    void printLine(int geneId, String geneReprs);
    
    void printSizeMatrix(int[][] popSize, int currentGeneration, int simulationId, String _layerName, int layerId);
    
    void setOutPutDir(String outputDir);
    
    void printSummaryStatsHeader(String[] statNames);
    
    void printSummaryStatsLine(String stats);
    
    WriterHelperType getWriterType();
    
    void setProjectName(String projectName);
}
