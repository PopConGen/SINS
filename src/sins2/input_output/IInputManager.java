/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.input_output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import sins2.helpers.random.RandGenHelper;
import sins2.simulation.Environment;
import sins2.simulation.Genotype;
import sins2.simulation.Layer;
import sins2.simulation.World;

/**
 *
 * @author douglas
 */
public interface IInputManager {

    <T> T getConfig(String configName, Object... args);

    /*
     *
     */
    Environment getEnvironment(int enviromentId, String[] layersName);

    Genotype getGenotype(String layerName);

    Layer[] getLayersInfo(String[] layersName, RandGenHelper randGenHelper, int n, int m, int[] expansionTime);

    void initializeOutPutParameters(World world);

    void initializeSamplingPreferences(World world);
    
    ArrayList<String> initDemographicPlotOpt();

    /*
     * Todo: Control possibles errors in input
     */
    /**
     *
     * @param world
     */
    void initializeWorld(World world);

    boolean inputChange();

    int[][] readExpansionMatrix(String _layerName);
    
    LinkedHashMap<String, HashMap> getProjectOptions(String[] clargs);
    
}
