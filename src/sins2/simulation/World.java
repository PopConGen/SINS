/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.simulation;

import sins2.input_output.InputManager;
import sins2.sampling_class.AllSetSamplingConfig;
import sins2.helpers.io.IWriterHelper;
import sins2.helpers.random.RandGenHelper;
import sins2.helpers.io.WriterFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import sins2.exceptions.InputParametersExceptions;
import sins2.input_output.IInputManager;
import sins2.sampling_class.ISampler;
import sins2.sampling_class.ISamplingConfig;
import sins2.sampling_class.NoneElementsSamplingConfig;
import sins2.summary_stats.ComputeSummaryStats;

/**
 * *
 * TODO: Try to replace the change that a lot of methods need to have a
 * reference to the input folder.
 */
/**
 *
 * @author douglas
 */
public final class World {//Outcomment "final" keyword for Unit testing
//TODO: remove outcomment after testing
    
    /*
     * Class variables
     */
    //kept the path to the input folder
    private String _inputFolderPath = "";
    
    
    private IInputManager _input;
    
    
    //The number of generations in which the world will be simulated
    
    private boolean _simulationRunning;
    private int _numberOfGenerations;
    private int _layersCount;
    private String[] _layersName;
    private int[] _expansionTime;
    private double[][] _admixtureSexRatio; //TIAGO
    private double[][] _admixture;
    private double[][] _competition;
    private int _numberOfEnvVarEvents;
    
    
    //indicate when ocurrs an enviroment change
    private int[] _envVarTime;
    private Environment currentEnvironment;
    
    //variables for output preferences for the user  
    private boolean _recordDemographicOutput, _recordSummaryStatsOutput;
    private int _nextOutputPreferenceIndex, 
            _generationsIntervalDem, 
            _generationsIntervalGen, 
            _generationsIntervalSumStats,
            _startRecord;
    private List<OutputRangePreference> _outputPreferences;
    
    
    //sampling preferences
    private ISampler[] _samplingPreferences;
    private Map<String, ISamplingConfig> _samplingConfig;
    List<Integer> _samplingConfiTimelines;
    List<String> _samplingConfigTimelinesName;
    int _nextSamplingTimeLineIndex;
    ISamplingConfig _currentSamplingConfig;
    
    
    //containg the layers in the world
    private Layer[] layers;
    //auxiliar variables
    //this class manage the read process
    //private ReadHelper reader;
    //this class manage the write process
    private IWriterHelper _outputManager;
    private String _compressFormat; //transform this into an enum!
    
    private RandGenHelper _randGenHelper;
    
    private ComputeSummaryStats sumStats;

    public World(String outputDir, String compressFormat, String outputFormat) {
        //selecting the correct writer
        _outputManager = WriterFactory.getWriterHelper(compressFormat, outputFormat, outputDir);
        _compressFormat=compressFormat;
        //set up the output directory
        _outputManager.setOutPutDir(outputDir);
        _randGenHelper = new RandGenHelper();        
    }

    
    private void initializeFromInput() throws InputParametersExceptions, Exception{
               
        //initialize parameters of worl from the input 
        if(_input.inputChange())
            _input.initializeWorld(this);
        else resetWorld();//is noly needed reset worl configuration
        readOutputPreferences();
       
    }
    
   
    public void beginSimulation(int simulationId, String projectName) throws IOException, InputParametersExceptions {
   
        
        long t = System.nanoTime();
        long r = ThreadLocalRandom.current().nextLong(t);
        //System.out.println(t-r);
        _randGenHelper.reInit((int) (t-r));//TODO: test if ok for parallel
        //_randGenHelper.reInit(0);
        
        try {
            //initialize world
            initializeFromInput();
        } catch (InputParametersExceptions ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
        _simulationRunning=true;
        //start to write the results of the simulation
        if("SQLdb".equals(_compressFormat)){
            _outputManager.beginToWriteSimulation(projectName, simulationId, layers);
        }else{
            _outputManager.beginToWriteSimulation(projectName, simulationId);
        }
       
        //Print simulation options to file
        _outputManager.printSimulationOptions(_input.getProjectOptions(new String[0]));
        
        //init summaryStats
        sumStats = new ComputeSummaryStats(_outputManager);

        //this is the current environement (initial environment)       
        
        int currentEnvironmentIndex = 0, nextEnvironmentChange = -1;
        if (_numberOfEnvVarEvents > 0) {
            nextEnvironmentChange = _envVarTime[currentEnvironmentIndex];                    
        }

        //initialize population
        Layer.checkForStartOfExpansions(_input, 0, layers);
        printResults(0, simulationId);

        for (int i = 0; i < _numberOfGenerations; i++) {
            nextEnvironmentChange--;
            //simple simulation step

            //this report maybe is better eliminate it
            System.out.println("Simulation " + simulationId + " Generation " + i);
            

            //all events related with movements (Migration and Admixture)
            movements(i, currentEnvironment);

            //events related with population growth (reproduction, competition)
            logisticGrowth(currentEnvironment, i+1);

            //all process related to write the output in some manner
            printResults(i + 1, simulationId);
            
            //check if start an expansion in the next generation
            Layer.checkForStartOfExpansions(_input, i + 1, layers);

            //analize if a simulation change can suceed 
            if (nextEnvironmentChange == 0) {
                //at this time the envoronment has to change
                currentEnvironmentIndex++;
                if (_numberOfEnvVarEvents > currentEnvironmentIndex) {
                    nextEnvironmentChange = _envVarTime[currentEnvironmentIndex] - i - 1;
                }
                //change the environment 
                currentEnvironment=_input.getEnvironment(currentEnvironmentIndex,
                        _layersName);
               

            }
        }


        _outputManager.finishedWriteOutput();
        _simulationRunning=false;
    }

    /**
     * Read parameters from the output_preferences.txt file TODO: Change locale
     * to English
     */
    private void readOutputPreferences() throws FileNotFoundException {
        
        _input.initializeOutPutParameters(this);
        _nextOutputPreferenceIndex = 1;      

        if (_outputPreferences.isEmpty()) //by default all the data is read
        {
            _outputPreferences.add(new OutputRangePreference(0, 1, 1, 1));
        }
        
        _startRecord = _outputPreferences.get(0).startRecord;
        _generationsIntervalDem = _outputPreferences.get(0).recordIntervalDem;
        _generationsIntervalGen = _outputPreferences.get(0).recordIntervalGen;
        _generationsIntervalSumStats = _outputPreferences.get(0).recordIntervalSumStats;
        
        //start to read sampling preferences        
        _input.initializeSamplingPreferences(this);
        //adding defaults options
        _currentSamplingConfig = new AllSetSamplingConfig();
        _samplingConfig.put("all",_currentSamplingConfig);
        _samplingConfig.put("none", new NoneElementsSamplingConfig());
        
        _nextSamplingTimeLineIndex = 0;
    }


    private void movements(int currentGeneration, Environment currentEnvironment)
            throws IOException {

        Layer.doAllMovements(_inputFolderPath, currentGeneration, currentEnvironment, layers, _admixture, /*TIAGO*/_admixtureSexRatio);

    }

    public int[][][] newGenerationSizeValueToTest;
    public int nRowSizeTest;
    public int mColumnSizeTest;
    public int layersCountTest;
    
    public void logisticGrowth(Environment currentEnvironment, int generation) throws InputParametersExceptions {
        int n = currentEnvironment.getRowSize();
        int m = currentEnvironment.getColumnSize();
        int[][][] newGenerationSize = new int[_layersCount][n][m];
        
        /*TIAGO Testing parameters/values*/
        newGenerationSizeValueToTest = new int[_layersCount][n][m];
        nRowSizeTest = n;
        mColumnSizeTest = m;
        layersCountTest = _layersCount;
        
        //System.out.println("ASD"+nRowSizeTest+"  "+mColumnSizeTest+"   "+layersCountTest);
        /*TIAGO END Testing parameters/values*/
        
        double denominator = 0;

        for (int layerIdA = 0; layerIdA < _layersCount; layerIdA++) {
            for (int row = 0; row < n; row++) {
                for (int column = 0; column < m; column++) {
                    if (layers[layerIdA].demeContainMaleAndFemale(row, column)) {
                        denominator = 1;
                        for (int layerIdB = 0; layerIdB < _layersCount; layerIdB++) {
                            denominator += (2 * layers[layerIdA].getGrowthRate()
                                    * _competition[layerIdA][layerIdB]
                                    * layers[layerIdA].getFemaleCount(row, column)
                                    * layers[layerIdA].getDominantFemalePercent()
                                    / currentEnvironment.getK(layerIdA, row, column));
                        }

                        //TODO: Check this computation later and compare it with the paper
                        newGenerationSize[layerIdA][row][column] =
                                _randGenHelper.getPoissonGenerator()
                                .nextInt(2 * ((1. + layers[layerIdA].getGrowthRate())
                                * layers[layerIdA].getFemaleCount(row, column)
                                * layers[layerIdA].getDominantFemalePercent() / denominator));
                    }
                }
            }

            //creating the next generation
            //Arrays.parallelSetAll(layers[layerId], nextGeneration(newGenerationSize[layerId], generation));
            layers[layerIdA].nextGeneration(newGenerationSize[layerIdA], generation);
            //layers[layerId].nextGenerationParallel(newGenerationSize[layerId], generation);
        }

        //newGenerationSizeValueToTest = newGenerationSize;

    }

    /**
     * Tiago
     * @param growthRate
     * @param competition
     * @param femaleCount
     * @param dominantFemalePercent
     * @param carryingCapacity
     * @return the denominator of the logisticGrowth formula 
     */
    public double logisticGrowthCalculation_Denominator(double growthRate,
            double competition, int femaleCount, double dominantFemalePercent,
            double carryingCapacity){
        
        double logisticGrowthResult_Denominator;
        
        logisticGrowthResult_Denominator = (2 * growthRate
                                    * competition
                                    * femaleCount
                                    * dominantFemalePercent
                                    / carryingCapacity);
        
        
        return logisticGrowthResult_Denominator;
    }
    
    /**
     * Tiago
     * @param growthRate
     * @param femaleCount
     * @param dominantFemalePercent
     * @return the numerator of the logisticGrowth formula 
     */
    public double logisticGrowthCalculation_Numerator(double growthRate, int femaleCount, double dominantFemalePercent){
        
        double logisticGrowthResult_Numerator;
        
        logisticGrowthResult_Numerator = 2*(1. + growthRate) * femaleCount * dominantFemalePercent;
        
        return logisticGrowthResult_Numerator;
    }
    
    /**
     * 
     * @param numerator
     * @param denominator
     * @return logisticGrowth value
     */
    public double logisticGrowthCalculation(double numerator, double denominator){
        
        double logisticGrowthResult;
        
        logisticGrowthResult = (numerator/denominator);
        
        
        return logisticGrowthResult;
    }
    
    
    /**
     * Tiago
     * Refactoring logisctiGrowth
     * @param currentEnvironment
     * @param generation 
     */
    public void logisticGrowthTiagoRefactor(Environment currentEnvironment, int generation) throws InputParametersExceptions {
        int n = currentEnvironment.getRowSize();
        int m = currentEnvironment.getColumnSize();
        int[][][] newGenerationSize = new int[_layersCount][n][m];       
        
        double denominator = 0;
        double numerator;
        double theGrowthRate;
        int theFemaleCount;
        double theDominantFemalePercent;
        double theCarryingCapacity;
        double theCompetition;

        for (int layerId = 0; layerId < _layersCount; layerId++) {
            for (int row = 0; row < n; row++) {
                for (int column = 0; column < m; column++) {
                    if (layers[layerId].demeContainMaleAndFemale(row, column)) {
                        
                        denominator = 1;
                        theGrowthRate = layers[layerId].getGrowthRate();
                        theFemaleCount = layers[layerId].getFemaleCount(row, column);
                        theDominantFemalePercent = layers[layerId].getDominantFemalePercent();
                        theCarryingCapacity = currentEnvironment.getK(layerId, row, column);
                        
                        for (int j = 0; j < _layersCount; j++) {
                            
                            theCompetition = _competition[layerId][j];
                            
                            denominator += logisticGrowthCalculation_Denominator(
                                    theGrowthRate, theCompetition, theFemaleCount,
                                    theDominantFemalePercent, theCarryingCapacity);
                        }

                        numerator = logisticGrowthCalculation_Numerator(theGrowthRate, theFemaleCount, theDominantFemalePercent);
                        //TODO: Check this computation later and compare it with the paper
                        newGenerationSize[layerId][row][column] =
                                _randGenHelper.getPoissonGenerator()
                                .nextInt(logisticGrowthCalculation(numerator, denominator));
                    }
                }
            }

            //creating the next generation
            layers[layerId].nextGeneration(newGenerationSize[layerId], generation);
        }

    }
    
    /**
     * In this methods write the output of the software
     */
    private void printResults(int currentGeneration, int simulationId) throws IOException {
        //TODO: Check and refactor this methods

        if(_nextSamplingTimeLineIndex < _samplingConfiTimelines.size()
                && currentGeneration == _samplingConfiTimelines.get(_nextSamplingTimeLineIndex)){
             _currentSamplingConfig =
                     _samplingConfig.get(_samplingConfigTimelinesName.get(_nextSamplingTimeLineIndex));
             
             _nextSamplingTimeLineIndex++;
        }

        //updating output preference
        if (_nextOutputPreferenceIndex < _outputPreferences.size()
                && currentGeneration >= _outputPreferences.get(_nextOutputPreferenceIndex).startRecord) {
            /*We can gate the "production" of output by setting different
            output_preferences option-sets.*/
            _startRecord = _outputPreferences.get(_nextOutputPreferenceIndex).startRecord;
            _generationsIntervalDem = _outputPreferences.get(_nextOutputPreferenceIndex).recordIntervalDem;
            _generationsIntervalGen = _outputPreferences.get(_nextOutputPreferenceIndex).recordIntervalGen;
            _nextOutputPreferenceIndex++;
        }

        //updating sampling reference


        int currentGenerationRelative = currentGeneration - _startRecord;

        if (currentGenerationRelative < 0 || (_generationsIntervalDem == 0 && _generationsIntervalGen == 0)) {
            return;
        }


        //recording demographic output
        if (_recordDemographicOutput
                && (((currentGenerationRelative) % _generationsIntervalDem) == 0
                || currentGenerationRelative == 0)) {
            for (int k = 0; k < _layersCount; k++) {
                layers[k].printSize(currentGeneration, simulationId, _outputManager);
            }
        }
        
        
        //print summaryStats
        if(_recordSummaryStatsOutput 
                && ((currentGenerationRelative % _generationsIntervalSumStats) == 0 
                || currentGenerationRelative == 0)){
        sumStats.printSummaryStatistics(/*outputManager,*/currentGeneration, simulationId,layers);
        }

        //TODO Tiago add a variable that allows us to define if we want to
        //record the genetic output (similar to the one defined for the demographic output)
        //TODO implement option/tag so that the user can choose to save the very last generation of the simulation
        //TOOO implement method so that the user can choose the specific generations that he wants to save
        if (_generationsIntervalGen > 0) {
            if (((currentGenerationRelative) % _generationsIntervalGen) == 0
                    || currentGenerationRelative == 0) {
                for (int k = 0; k < _layersCount; k++) {
                    layers[k].printInformation(currentGeneration,
                            simulationId, _outputManager,                            
                            _currentSamplingConfig.getSampler(k));
                }
            }
        } else {
            //printing the last generation
            if (currentGeneration == _numberOfGenerations - 1) {
                for (int k = 0; k <= _layersCount; k++) {
                    layers[k].printInformation(currentGeneration,
                            simulationId, _outputManager,
                            _currentSamplingConfig.getSampler(k));
                }
            }
        }
    }

    private void resetWorld() {
        for (int i = 0; i < _layersCount; i++) {
            layers[i].setExpansionTime(_expansionTime[i]);
            layers[i].clearDemes();
        }
        currentEnvironment=_input.getEnvironment(0, _layersName);
    }

    public void setCurrentEnvironment(Environment environment) {
       currentEnvironment=environment;
    }

    public static class OutputRangePreference {

        public int startRecord, recordIntervalGen, recordIntervalDem, recordIntervalSumStats;

        public OutputRangePreference(int startRec, int recIntDem, int recIntGen, int recIntSumStat) {
            startRecord = startRec;
            recordIntervalGen = recIntGen;
            recordIntervalDem = recIntDem;
            recordIntervalSumStats = recIntSumStat;
        }
    }

    
    /*
     * This class is for thread safe purpose     
     */
    public void checkIfExternalChangeIsAllowed() throws Exception{
      if(_simulationRunning)throw new Exception("Forbidden methods");
    }
    
    //properties
    public void setNumberOfGenerations(int numberOfGenerations) throws Exception{
        checkIfExternalChangeIsAllowed();
       _numberOfGenerations=numberOfGenerations;
    }
    
    public void setLayersParameters(String[] layerName,int[] expansionTime, /*TIAGO*/double[][] admixtureSexRatio,
            double[][] admixture,double[][] competition) throws Exception
    {
        checkIfExternalChangeIsAllowed();
        
        _layersCount=layerName.length;
        
        _layersName=new String[_layersCount];
        _expansionTime=new int[_layersCount];
        
        for(int i=0;i<_layersCount;i++){
         _layersName[i]=layerName[i];
         _expansionTime[i]=expansionTime[i];
        }
        
        _admixtureSexRatio =new double[_layersCount][_layersCount];//TIAGO
                
        _admixture=new double[_layersCount][_layersCount];
        _competition=new double[_layersCount][_layersCount];
        
        for (int i = 0; i < _layersCount; i++) {
            for (int j = 0; j < _layersCount; j++) {
                _admixture[i][j]=admixture[i][j];
                _competition[i][j]=competition[i][j];
                _admixtureSexRatio[i][j]=admixtureSexRatio[i][j];
            }
        }
            
    }

    
    public void setEnviromentalChangesTime(int[] enviromentalChanges)
    {
       _numberOfEnvVarEvents=enviromentalChanges.length;
       _envVarTime=new int[_numberOfEnvVarEvents];
       
        System.arraycopy(enviromentalChanges, 0, _envVarTime, 0, _numberOfEnvVarEvents);
    }

    public void setInputManager(IInputManager input){
        try {
            checkIfExternalChangeIsAllowed();
        } catch (Exception ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
       _input=input;    
    }
    
    public void setOutputPreferenceParameters(boolean recordDemographicOutput,
            boolean recordSummaryStatsOutput,
            List<OutputRangePreference> outputPreferencesList){
        _outputPreferences=outputPreferencesList;
        _recordDemographicOutput=recordDemographicOutput;
        _recordSummaryStatsOutput=recordSummaryStatsOutput;
    }
    
    public void setSamplingConfig(Map<String,ISamplingConfig> samplingConfigs,
            ISampler[] samplingPreferences,
            List<Integer> samplingConfigTimelines,
            List<String> samplingConfigTimelinesName){
    
            _samplingConfig=samplingConfigs;
            _samplingPreferences=samplingPreferences;
            _samplingConfiTimelines=samplingConfigTimelines;
            _samplingConfigTimelinesName=samplingConfigTimelinesName;
            
    }
    
    public void setLayersInfo(Layer[] layers) {
        this.layers = layers;
    }
    
    //GETTERS 
    public int getLayersCount() {
        return _layersCount;
    }

    public int getRowSize() {
        return currentEnvironment.getRowSize();
    }

    public int getColumnSize() {
        return currentEnvironment.getColumnSize();
    }
    
    public IWriterHelper getWriter(){
        return _outputManager;
    }

    public RandGenHelper getRandomGenerator() {
        return _randGenHelper;
    }

    public boolean inputDontChanged() {
        return true;
    }

}
