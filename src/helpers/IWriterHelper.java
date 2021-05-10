/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

/**
 *
 * @author douglas
 */
public interface IWriterHelper {

    //TODO maybe I need to change this because _inputFolderPath is not the name of the folder where the input is
    void beginToWriteSimuation(String projectName, int simulationId);

    void closeCurrentRecord();

    void finishedWriteOutput();

    void openRecord(String id,int generation);

    void printLine(String geneReprs);

    void printSizeMatrix(int[][] popSize, int currentGeneration, int simulationId, String _layerName);
    
    void setOutPutDir(String outputDir);
}
