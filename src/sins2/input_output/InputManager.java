/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.input_output;

import cern.colt.Arrays;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import sins2.exceptions.InputParametersExceptions;
import sins2.simulation.Environment;
import sins2.simulation.World;
import sins2.helpers.io.IReaderHelper;
import sins2.helpers.io.ReaderHelper;
import sins2.helpers.random.RandGenHelper;
import sins2.sampling_class.ISamplingConfig;
import sins2.sampling_class.SamplerType;
import sins2.sampling_class.SamplingConfig;
import sins2.simulation.AlleleFrequency;
//import sins2.simulation.Deme;
import sins2.simulation.Genotype;
import sins2.simulation.Layer;
import sins2.simulation.Layer.MatingSystem;
import sins2.simulation.Layer.LddOptions;
import sins2.simulation.Site;
import sins2.simulation.World.OutputRangePreference;

/**
 *
 * @author douglas
 */
public class InputManager implements IInputManager {

    
    //this class manage the read process
    private IReaderHelper reader;
    private String _inputFolderPath;
    boolean _inputChange;
    LinkedHashMap<String, HashMap> _simOptsList = new LinkedHashMap<>();
    LinkedHashMap<String, Object> _simulationSamplingOptions = new LinkedHashMap<>();
    LinkedHashMap<String, Object> _simulationOptions;
    
    
    public InputManager(String inputFolderPath){
        _inputFolderPath = inputFolderPath;
        _inputChange=true;
    }
    
    protected IReaderHelper getReader(String path) throws FileNotFoundException
    {
      return new ReaderHelper(path);
    }
    /*
     * Todo: Control possibles errors in input
     */
    @Override
    public final void initializeWorld(World world){
        _simulationOptions = new LinkedHashMap<>();
        try {
            reader = getReader(_inputFolderPath + File.separator + "world.txt");

            reader.next();// the label of the second configuration parameters is not used
            int numberOfGenerations = reader.nextInt();
            
            _simulationOptions.put("NumberOfGenerations", numberOfGenerations);
            
            //System.out.println("@initializeWorld "+_inputFolderPath + File.separator + "world.txt");
            world.setNumberOfGenerations(numberOfGenerations);
            reader.next();
            int layersCount = reader.nextInt();
            _simulationOptions.put("NumberOfLayers", layersCount);
            String[] layerName = new String[layersCount];
            int[] expansionTime = new int[layersCount];

            for (int i = 0; i < layersCount; i++) {
                reader.next();
                layerName[i] = reader.next();
                _simulationOptions.put("name_layer_"+i, layerName[i]);

                reader.next();
                expansionTime[i] = reader.nextInt();
                _simulationOptions.put("expansionTime_layer_"+i, expansionTime[i]);
            }

            //double admixtureSexRatio = reader.nextDouble(); //TIAGO
            double[][] admixtureSexRatio = new double[layersCount][layersCount];//TIAGO 
            double[][] admixture = new double[layersCount][layersCount];
            double[][] competition = new double[layersCount][layersCount];

            for (int i = 0; i < layersCount; i++) {
                for (int j = 0; j < layersCount; j++) {
                    reader.next();
                    admixtureSexRatio[i][j] = reader.nextDouble();
                    _simulationOptions.put("admixtureSexRatio"+i+""+j, admixtureSexRatio[i][j]);
                }
            }
            
            for (int i = 0; i < layersCount; i++) {
                for (int j = 0; j < layersCount; j++) {
                    reader.next();
                    admixture[i][j] = reader.nextDouble();
                    _simulationOptions.put("admixture"+i+""+j, admixture[i][j]);
                }
            }

            for (int i = 0; i < layersCount; i++) {
                for (int j = 0; j < layersCount; j++) {
                    reader.next();
                    competition[i][j] = reader.nextDouble();
                    if(i == j && competition[i][j]!=1.0){
                        try {
                            throw new InputParametersExceptions();
                        } catch (InputParametersExceptions ex) {
                            Logger.getLogger(InputManager.class.getName()).log(Level.WARNING, "In the world.txt file, competition between the same layer/population needs to be set to 1.\ncompetition{0}{1} will be set to 1 and the simulation will proceed.", new Object[]{i, j});
                        }
                        competition[i][j] = 1.0;
                    }
                    _simulationOptions.put("competition"+i+""+j, competition[i][j]);
                }
            }
            
            world.setLayersParameters(layerName, expansionTime, admixtureSexRatio/*TIAGO*/
                    , admixture, competition);



            reader.next();
            int numberOfEnvVarEvents = reader.nextInt();
            _simulationOptions.put("NumberOfEnvironmentalChanges", numberOfEnvVarEvents);
            int[] envVarTime = new int[numberOfEnvVarEvents];
            for (int i = 0; i < numberOfEnvVarEvents; i++) {
                reader.next();
                envVarTime[i] = reader.nextInt();
                _simulationOptions.put("EnvironmentalChangeTime", envVarTime[i]);
            }
            reader.close();
            _simOptsList.put("WorldOptions", _simulationOptions);
            
            
            world.setEnviromentalChangesTime(envVarTime);
            
            world.setCurrentEnvironment(getEnvironment(0, layerName));
            
            Layer[] layers=getLayersInfo(layerName, 
                    world.getRandomGenerator(), _envN, _envM, expansionTime);
            
            world.setLayersInfo(layers);
            
            _inputChange=false;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex){
           Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (Exception ex){
           Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
     *
     */
    @Override
    public Environment getEnvironment(int enviromentId,String[] layersName) {
        try {
            String prefix = "Init";
            if (enviromentId > 0) {
                prefix = String.valueOf(enviromentId);
            }

            return readEnvironmentFromFile(
                    _inputFolderPath + File.separator + "environment" + File.separator,
                    layersName, prefix);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    
    //variables that preload the number of rows and columns in the enviromental matrix
    int _envN,_envM;
    
    Environment readEnvironmentFromFile(String filePath,String[] populationsName,String suffix) throws FileNotFoundException, IOException{
        _simulationOptions = new LinkedHashMap<>();
        
        double[][] tmpK, tmpF;
        int n=_envN;
        int m=_envM;
        
        //Loads the CC and F maps of the first layer and then compares the size of each
        //against the other
        if(n==0){
            reader=getReader(filePath+File.separator+populationsName[0]+"CC"+suffix+".txt");
            tmpK=reader.readMatrix();
            reader.close();
            
            reader=getReader(filePath+File.separator+populationsName[0]+"F"+suffix+".txt");
            tmpF=reader.readMatrix();
            reader.close();
        }
        else{
            reader=getReader(filePath+File.separator+populationsName[0]+"CC"+suffix+".txt");
            tmpK=reader.readMatrix(n, m);
            reader.close();
            
            reader=getReader(filePath+File.separator+populationsName[0]+"F"+suffix+".txt");
            tmpF=reader.readMatrix(n, m);
            reader.close();
        }
        
        
        if(tmpK.length == tmpF.length && tmpK[0].length == tmpF[0].length){
            n=tmpK.length;
            m=tmpK[0].length;
            _envN=n;
            _envM=m;        
        } else {
            try {
                throw new InputParametersExceptions("Environmental maps CC and F have different sizes.");
            } catch (InputParametersExceptions ex) {
                Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        double[][][] K=new double[populationsName.length][n][m];
        K[0]=tmpK;
        
        double[][][] F=new double[populationsName.length][n][m];
        F[0]=tmpF;
        
        _simulationOptions.put(populationsName[0]+"CC"+suffix, mapMatrixToString(K[0]));
        _simulationOptions.put(populationsName[0]+"F"+suffix, mapMatrixToString(F[0]));
        
        for (int i = 1; i < populationsName.length; i++) {
           reader=getReader(filePath+File.separator+populationsName[i]+"CC"+suffix+".txt");
           K[i]=reader.readMatrix(n,m);
           reader.close();
           _simulationOptions.put(populationsName[i]+"CC"+suffix, mapMatrixToString(K[i]));
            
           reader=getReader(filePath+File.separator+populationsName[i]+"F"+suffix+".txt");
           F[i]=reader.readMatrix(n, m);
           reader.close();
           _simulationOptions.put(populationsName[i]+"F"+suffix, mapMatrixToString(F[i]));
        }
        _simOptsList.put("Environment_"+suffix, _simulationOptions);
        
        return new Environment(F,K);
        
    }

    static String mapMatrixToString(double[][] mapMatrix){
        StringBuilder sb = new StringBuilder(64);
        
        sb.append("\n");
        for (double[] rows : mapMatrix) {
            for (double cols : rows) {
                sb.append(cols);
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
        static String mapMatrixToString(int[][] mapMatrix){
        StringBuilder sb = new StringBuilder(64);
        
        sb.append("\n");
        for (int[] rows : mapMatrix) {
            for (int cols : rows) {
                sb.append(cols);
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    @Override
    public void initializeOutPutParameters(World world) {
        try {
            reader = getReader(_inputFolderPath + File.separator + "output_preferences.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<OutputRangePreference> outputPreferences 
                = new ArrayList<>();
        
        reader.next();
        boolean recordDemographicOutput = reader.nextBoolean();
        reader.next();
        boolean recordSummaryStats = reader.nextBoolean();
        int recStart, genIntDem, genIntGen, genSumStatGen;

        while (reader.hasNext()) {
            reader.next();
            recStart = reader.nextInt();
            reader.next();
            genIntDem = reader.nextInt();
            reader.next();
            genIntGen = reader.nextInt();
            reader.next();
            genSumStatGen = reader.nextInt();
            outputPreferences.add(new OutputRangePreference(recStart, genIntDem, genIntGen,genSumStatGen));
        }
        try {
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        saveOutputPrefOpts(recordDemographicOutput,
                recordSummaryStats,
                outputPreferences);
        
        world.setOutputPreferenceParameters(recordDemographicOutput,
                recordSummaryStats,
                outputPreferences);
        
    }

    public void saveOutputPrefOpts(
            boolean recordDemographicOutput,
            boolean recordSummaryStatsOutput, 
            List<World.OutputRangePreference> outputPreferencesList
    ){
        _simulationOptions = new LinkedHashMap<>();
        
        _simulationOptions.put("RecordDemographicOuput", recordDemographicOutput);
        _simulationOptions.put("RecordSummaryStats", recordSummaryStatsOutput);
        for(int i = 0; i < outputPreferencesList.size(); i++){
            _simulationOptions.put("StartRecordingAt("+i+")",outputPreferencesList.get(i).startRecord);
            _simulationOptions.put("DemographyRecordingInterval("+i+")",outputPreferencesList.get(i).recordIntervalDem);
            _simulationOptions.put("GeneticsRecordingInterval("+i+")",outputPreferencesList.get(i).recordIntervalGen);
            _simulationOptions.put("SummaryStatsRecordingInterval("+i+")",outputPreferencesList.get(i).recordIntervalSumStats);
        }
        
        _simOptsList.put("OutputPreferences", _simulationOptions);
    }
    
    @Override
    public void initializeSamplingPreferences(World world) {

        String path=_inputFolderPath + File.separator
                    + "sampling_preferences" + File.separator
                    + "sampling_conf.txt";
        
        Map<String, ISamplingConfig> samplingConfig = 
                new HashMap<String, ISamplingConfig>();   
        List<Integer> samplingConfigTimelines = new ArrayList<Integer>();
        List<String> samplingConfigTimelinesName = new ArrayList<String>();
        int confNumber = 0;
        
        if (new File(path).exists()) {

            try {
                reader = getReader(path);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
            }

            int n = world.getRowSize(), m = world.getColumnSize();

            reader.next();
            confNumber = reader.nextInt();//numOfConfigs [int]
            _simulationSamplingOptions.put("NumberOfSamplingConfigs", confNumber);
            String configName, samplerTypeName;

            int layersCount = world.getLayersCount();


            for (int i = 0; i < confNumber; i++) {
                configName = reader.next();
                _simulationSamplingOptions.put("ConfigName("+i+")", configName);
                SamplerType[] samplerTypes = new SamplerType[layersCount];
                String[] samplerConfigFiles = new String[layersCount];

                for (int j = 0; j < layersCount; j++) {
                    samplerTypeName = reader.next(); //keyword: all/none/subset

                    if (samplerTypeName.compareTo("subset") == 0) {
                        //saves a subset of the information, user needs to provide a config map file
                        samplerTypes[j] = SamplerType.fixedSizeSubset;
                        samplerConfigFiles[j] = reader.next();
                        _simulationSamplingOptions.put("ConfigType("+i+")_layer("+j+")", SamplerType.fixedSizeSubset);
                        _simulationSamplingOptions.put("ConfigSubsetMap("+i+")_layer("+j+")", samplerConfigFiles[j]);
                        
                    } else if (samplerTypeName.compareTo("all") == 0) {
                        //saves all information as a sim without this option would do
                        samplerTypes[j] = SamplerType.all;
                        _simulationSamplingOptions.put("ConfigType("+i+")_layer("+j+")", SamplerType.all);
                    } else {//in this case we only have the none option
                        samplerTypes[j] = SamplerType.none;
                        _simulationSamplingOptions.put("ConfigType("+i+")_layer("+j+")", SamplerType.none);
                    }
                }

                
                samplingConfig.put(configName,
                        new SamplingConfig(samplerTypes, samplerConfigFiles,
                        _inputFolderPath, n, m, this));
            }

            

            reader.next();//ignore timeline word

            //reading the timeline word and begining to read samplig configurations timeline 
            while (reader.hasNext()) {
                //ignore start word (startAt)
                reader.next();
                //read (startAt) Generation
                samplingConfigTimelines.add(reader.nextInt());
                //read name of the config option (configName)
                samplingConfigTimelinesName.add(reader.next());
            }
        }
             
        saveSamplingConfigOpts(confNumber,
            samplingConfig,
            samplingConfigTimelines, 
            samplingConfigTimelinesName);
        
        world.setSamplingConfig(samplingConfig, 
                null, 
                samplingConfigTimelines, 
                samplingConfigTimelinesName);

    }
    
    public void saveSamplingConfigOpts(int confNumber, 
            Map<String,ISamplingConfig> samplingConfigs,
            List<Integer> samplingConfigTimelines,
            List<String> samplingConfigTimelinesName){
        
        //_simulationOptions = new LinkedHashMap<>();
        
        //_simulationOptions.put("NumberOfSamplingConfigs", confNumber);

        
        for(int i = 0; i < samplingConfigTimelines.size(); i++){
            _simulationSamplingOptions.put(
                    "timelineStartAt("+i+")", samplingConfigTimelines.get(i)
            );
            _simulationSamplingOptions.put(
                    "timelineConfig("+i+")", samplingConfigTimelinesName.get(i)
            );
        }  
    }
    
    @Override
    public Layer[] getLayersInfo(String[] layersName, 
            RandGenHelper randGenHelper,int n,int m,int[] expansionTime)
    {
        int layersCount=layersName.length;
        Layer[] layers = new Layer[layersCount];

        //this structure is used to speed the process of assigning to each layer its layer settle.
        //TODO: Consider to use a SortedMap instead of a HashMap
        HashMap<String, Layer> layersByName = new HashMap<String, Layer>();
        
        

        for (int i = 0; i < layersCount; i++) {
            try {
                Genotype genType = getGenotype(layersName[i]);
                layers[i]= readLayerFromFile(genType, layersName[i]
                        ,i,randGenHelper
                        ,_inputFolderPath + 
                        File.separator + "layer_parameters" + File.separator,
                        n, m);
                
                layers[i].setExpansionTime(expansionTime[i]);
                layersByName.put(layersName[i], layers[i]);
            } catch (Exception ex) {
                Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //finding each layer settle
        for (int i = 0; i < layersCount; i++) {
            layers[i].setLayerSettle(layersByName.get(layers[i].getSettlerName()));
        }
        
        return layers;
    }
    
    @Override
    public boolean inputChange(){
      return _inputChange;
    }

    @Override
    public int[][] readExpansionMatrix(String _layerName) {
        int[][] initMatrix = new int[_envN][_envM];
        _simulationOptions = new LinkedHashMap<>();
        
        try {
            reader = getReader(_inputFolderPath + File.separator + "layer_parameters"
                    + File.separator + _layerName + "_init.txt");
            
            for (int i = 0; i < _envN; i++) {
                for (int j = 0; j < _envM; j++) {
                    initMatrix[i][j] = reader.nextInt();
                }
            }
            reader.close();
            
            _simulationOptions.put(_layerName+"_init", mapMatrixToString(initMatrix));
            _simOptsList.put("LayerInit_"+_layerName, _simulationOptions);
        } catch (IOException ex) {
            Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return initMatrix;
    }
    
    @Override
    public <T> T getConfig(String configName, Object... args) {
        if (configName.compareTo("subset") == 0) {
            try {
                String configFile = (String) args[0];
                
                reader = getReader(_inputFolderPath + File.separator
                        + "sampling_preferences" + File.separator + configFile);
                int[][] matrix = new int[_envN][_envM];
                for (int i = 0; i < _envN; i++) {
                    for (int j = 0; j < _envM; j++) {
                        matrix[i][j] = reader.nextInt();
                    }
                }
                _simulationSamplingOptions.put(configFile, mapMatrixToString(matrix));
                _simOptsList.put("SamplingPreferences", _simulationSamplingOptions);
                return (T) matrix;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            System.out.println(configName+": Unknown or incorrect configuration");
            System.exit(1);
        }
        
        return null;

    }

    @Override
    public Genotype getGenotype(String layerName){
        
        String path = _inputFolderPath
                + File.separator + "genetics" + File.separator
                + layerName + File.separator;
        
        try {
            reader = getReader(path + "genotype.txt");

            reader.next();
            int lenghtX = reader.nextInt();
            
            reader.next();
            String tmpType = reader.next(); //Read type of X chromosome
            Site.SiteType typeX;
            if(tmpType.equalsIgnoreCase("microsat")){typeX = Site.SiteType.MICROSAT;}
            else if(tmpType.equalsIgnoreCase("snp")){typeX = Site.SiteType.SNP;}
            else if(tmpType.equalsIgnoreCase("sequence")){typeX = Site.SiteType.SEQUENCE;}
            else{throw new IllegalArgumentException("Type of X chromosome/marker is neither microsat nor sequence nor SNP.");}
            
            reader.next();
            int lenghtY = reader.nextInt();

            reader.next();
            tmpType = reader.next(); //Read type of Y chromosome
            Site.SiteType typeY;
            if(tmpType.equalsIgnoreCase("microsat")){typeY = Site.SiteType.MICROSAT;}
            else if(tmpType.equalsIgnoreCase("snp")){typeY = Site.SiteType.SNP;}
            else if(tmpType.equalsIgnoreCase("sequence")){typeY = Site.SiteType.SEQUENCE;}
            else{throw new IllegalArgumentException("Type of Y chromosome/marker is neither microsat nor sequence nor SNP.");}
            
            reader.next();
            int lenghtMtDNA = reader.nextInt();

            reader.next();
            tmpType = reader.next(); //Read type of MtDNA chromosome
            Site.SiteType typeMt;
            if(tmpType.equalsIgnoreCase("microsat")){typeMt = Site.SiteType.MICROSAT;}
            else if(tmpType.equalsIgnoreCase("snp")){typeMt = Site.SiteType.SNP;}
            else if(tmpType.equalsIgnoreCase("sequence")){typeMt = Site.SiteType.SEQUENCE;}
            else{throw new IllegalArgumentException("Type of MtDNA marker is neither microsat nor sequence nor SNP.");}
            
            reader.next();
            int nbAutosomes = reader.nextInt();

            Site.SiteType[] typeA = new Site.SiteType[nbAutosomes];
            int[] lenghtA = new int[nbAutosomes];

            double[] mutationRate = new double[3 + nbAutosomes];

            for (int i = 0; i < nbAutosomes; i++) {
                reader.next();
                lenghtA[i] = reader.nextInt();
                reader.next();
                tmpType = reader.next();
                if (tmpType.equalsIgnoreCase("microsat")) {typeA[i] = Site.SiteType.MICROSAT;} 
                else if (tmpType.equalsIgnoreCase("snp")) {typeA[i] = Site.SiteType.SNP;}
                else if (tmpType.equalsIgnoreCase("sequence")) {typeA[i] = Site.SiteType.SEQUENCE;}
                else {throw new IllegalArgumentException("Type of A" + (i + 1) + " marker is neither microsat nor sequence nor SNP.");}
            }

            
            for (int i = 0; i < nbAutosomes + 3; i++) {
                reader.next();
                // TODO: implement different mutation rates per locus in the same marker, see code below
                //if(i == 0 && typeX != Site.SiteType.SEQUENCE){
                //    for (int j = 0; j < lenghtX; j++) {
                //      mutationRate[i] = reader.nextDouble();
                //    }
                //    HashMap<String, double[]> mutationMap.put(/*xOrId*/, mutationRate);
                //}
                mutationRate[i] = reader.nextDouble();
            }
            
            reader.close();
            
            AlleleFrequency[] allelesFrequencyInformation = 
                    getAlleleFrequency(path, nbAutosomes, 
                            lenghtA, typeA, 
                            lenghtX, typeX, 
                            lenghtY, typeY, 
                            lenghtMtDNA, typeMt);

            saveGenotypeOpts(
                    layerName, 
                    lenghtX, typeX,
                    lenghtY, typeY,
                    lenghtMtDNA, typeMt,
                    nbAutosomes,
                    typeA, lenghtA,
                    mutationRate, 
                    allelesFrequencyInformation
            );
            
            return new Genotype(lenghtX, typeX, lenghtY,
                    typeY, lenghtMtDNA, typeMt, nbAutosomes, typeA,
                    lenghtA, mutationRate, allelesFrequencyInformation);
            
        } catch (IOException ex) {
            Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void saveGenotypeOpts(String layerName, int lengthX, Site.SiteType typeX, int lengthY, Site.SiteType typeY,
            int lengthMtDNA, Site.SiteType typeMt, int nbAutosomes, Site.SiteType[] typeA,
            int[] lenghtA, double[] mutationRate,
            AlleleFrequency[] allelesFrequencyInformation){
        
        _simulationOptions = new LinkedHashMap<>();
        
        _simulationOptions.put("Length_ChrX_"+layerName, lengthX);
        _simulationOptions.put("Type_ChrX_"+layerName, typeX);
        _simulationOptions.put("Length_ChrY_"+layerName, lengthY);
        _simulationOptions.put("Type_ChrY_"+layerName, typeY);
        _simulationOptions.put("Length_ChrMT_"+layerName, lengthMtDNA);
        _simulationOptions.put("Type_ChrMT_"+layerName, typeMt);
        _simulationOptions.put("Number_of_autosomes_"+layerName, nbAutosomes);
        for (int i = 0; i < nbAutosomes; i++) {
            _simulationOptions.put("Length_ChrA"+(i+1)+"_"+layerName, lenghtA[i]);
            _simulationOptions.put("Type_ChrA"+(i+1)+"_"+layerName, typeA[i]);
        }
        
        for (int i = 0; i < nbAutosomes + 3; i++) {
            if(i==0){
                _simulationOptions.put("Mutation_rate_ChrX_"+layerName, mutationRate[i]);   
            }else if(i==1){
                _simulationOptions.put("Mutation_rate_ChrY_"+layerName, mutationRate[i]);   
            }else if(i==2){
                _simulationOptions.put("Mutation_rate_ChrMT_"+layerName, mutationRate[i]);   
            }else{
                _simulationOptions.put("Mutation_rate_ChrA"+(i-2)+"_"+layerName, mutationRate[i]);   
            }
        }
        
        for (int i = 0; i < allelesFrequencyInformation.length; i++) {
                AlleleFrequency alleleFrequency = allelesFrequencyInformation[i];
                for (int j = 0; j < alleleFrequency.getNumberOfAlleles(); j++) {
                    alleleFrequency.getAllele(j);   
                    if(i==0){
                        _simulationOptions.put(
                                "Allele_"+j+"_frequency_ChrX_"+layerName,
                                alleleFrequency.getFrequency(j));
                        _simulationOptions.put(
                                "Allele_"+j+"_sequence_ChrX_"+layerName,
                                Arrays.toString(alleleFrequency.getAllele(j)));
                    }
                    else if(i==1){
                        _simulationOptions.put(
                                "Allele_"+j+"_frequency_ChrY_"+layerName,
                                alleleFrequency.getFrequency(j));
                        _simulationOptions.put(
                                "Allele_"+j+"_sequence_ChrY_"+layerName,
                                Arrays.toString(alleleFrequency.getAllele(j)));
                    }
                    else if(i==2){
                        _simulationOptions.put(
                                "Allele_"+j+"_frequency_ChrMT_"+layerName,
                                alleleFrequency.getFrequency(j));
                        _simulationOptions.put(
                                "Allele_"+j+"_sequence_ChrMT_"+layerName,
                                Arrays.toString(alleleFrequency.getAllele(j)));}
                    else {
                        _simulationOptions.put(
                                "Allele_"+j+"_frequency_ChrA"+(i-2)+"_"+layerName,
                                alleleFrequency.getFrequency(j));
                        _simulationOptions.put(
                                "Allele_"+j+"_sequence_ChrA"+(i-2)+"_"+layerName,
                                Arrays.toString(alleleFrequency.getAllele(j)));
                    }
                }
            }
        
        _simOptsList.put("Genotype_"+layerName, _simulationOptions);
        
    }
    
    private AlleleFrequency[] getAlleleFrequency(String path, int nbAutosomes, 
            int[] Alenght, Site.SiteType[] typeA, 
            int Xlenght, Site.SiteType typeX,
            int Ylenght, Site.SiteType typeY, 
            int mtDNAlenght,Site.SiteType typeMt) {
        try {

            AlleleFrequency[] allelesFrequencyInformation
                    = new AlleleFrequency[nbAutosomes + 3];

            for (int i = 3; i < nbAutosomes + 3; i++) {
                allelesFrequencyInformation[i] = readAlleleFrequencyFromFile(path
                        + "allelesA" + (i - 2) + ".txt", Alenght[i - 3], typeA[i - 3]);
            }

            allelesFrequencyInformation[0]
                    = readAlleleFrequencyFromFile(path + "allelesX.txt", Xlenght, typeX);

            allelesFrequencyInformation[1]
                    = readAlleleFrequencyFromFile(path + "allelesY.txt", Ylenght, typeY);

            allelesFrequencyInformation[2]
                    = readAlleleFrequencyFromFile(path + "allelesMT.txt", mtDNAlenght, typeMt);

            return allelesFrequencyInformation;
        } catch (IOException ex) {
            Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private AlleleFrequency readAlleleFrequencyFromFile(String path, int alleleLength, Site.SiteType alleleType)
            throws IOException {

        File f = new File(path);
        int[][] sequence = null;
        double[] frequency = null;
        if (f.exists()) {
            reader = getReader(path);
            reader.next();
            int nbAlleles = reader.nextInt();
            sequence = new int[nbAlleles][alleleLength];
            frequency = new double[nbAlleles];

            for (int i = 0; i < nbAlleles; i++) {
                frequency[i] = reader.nextDouble();
                sequence[i] = new int[alleleLength];

                for (int j = 0; j < alleleLength; j++) {
                    sequence[i][j] = reader.nextInt();
                }
            }

            reader.close();
        } else {
            //if the file does not exist then there is no variance in the population
            sequence = new int[1][alleleLength];
            frequency = new double[1];
            frequency[0] = 1;
            int seqValue;
            if(alleleType == Site.SiteType.MICROSAT) seqValue = 500; //if microsat
            else seqValue = 0; //if anything else
            for (int j = 0; j < alleleLength; j++) {
                sequence[0][j] = seqValue;
            }
        }
        
        return new AlleleFrequency(sequence,frequency);
    }

    private Layer readLayerFromFile(Genotype genType, String layerName, int layerId, RandGenHelper randGenHelper, String path, int n, int m) {
        try {
            _simulationOptions = new LinkedHashMap<>();

            path += layerName;
            reader = getReader(path + ".txt");
            reader.next();
            double growthRate = reader.nextDouble();
            _simulationOptions.put("Growth_rate_"+layerName, growthRate);

            reader.next();
            double shortDistanceRate = reader.nextDouble();
            _simulationOptions.put("Short_distance_migration_rate_"+layerName, shortDistanceRate);

            reader.next();
            double shortDistanceSexRatio = reader.nextDouble();
            _simulationOptions.put("Short_distance_migration_sex_ratio_"+layerName, shortDistanceSexRatio);
            
            reader.next();
            String doLongDistanceDispersal = reader.next();
            
            LddOptions lddOption = null;
            if(doLongDistanceDispersal.equalsIgnoreCase("no")){
                lddOption = LddOptions.NO;
            }else if(doLongDistanceDispersal.equalsIgnoreCase("lddkernel")){
                lddOption = LddOptions.LDDKERNEL;
            }else if(doLongDistanceDispersal.equalsIgnoreCase("method_1")){
                lddOption = LddOptions.METHOD_1;
            }else throw new IllegalArgumentException(
                    "doLongDistanceDispersal in "+path+".txt"+" is not correctly defined."
                    + " \nMust be \"no\", \"lddkernel\", \"method_1\" or \"method_2\"."
            );
            _simulationOptions.put("Perform_LDD_"+layerName, doLongDistanceDispersal);
            
            double lddLambda = 0;
            boolean useMeanAsParameter = false;
            double mean_shape = 0;
            double variance_scale = 0;
            double minAngle = 0;
            double maxAngle = 0;
            double ldMigrationEventRate = 0;
            double ldMigrationRate = 0;
            double lddSexRatio = 0;
            int meanPoissonDistance = 0;
            
            if (lddOption != LddOptions.NO) {
                if (lddOption == LddOptions.LDDKERNEL) {
                    reader.next();
                    lddLambda = reader.nextDouble();
                    _simulationOptions.put("LDD_lambda_"+layerName, lddLambda);
                    reader.next();
                    useMeanAsParameter = reader.nextBoolean();
                    _simulationOptions.put("LDD_mean_as_parameter_"+layerName, useMeanAsParameter);
                    reader.next();
                    mean_shape = reader.nextDouble();
                    _simulationOptions.put("LDD_mean|shape_"+layerName, mean_shape);
                    reader.next();
                    variance_scale = reader.nextDouble();
                    _simulationOptions.put("LDD_variance|scale_"+layerName, variance_scale);
                } else if (lddOption == LddOptions.METHOD_1) {
                    reader.next();
                    ldMigrationEventRate = reader.nextDouble();
                    _simulationOptions.put("LDD_event_rate_"+layerName, ldMigrationEventRate);
                    reader.next();
                    ldMigrationRate = reader.nextDouble();
                    _simulationOptions.put("LDD_migration_rate_"+layerName, ldMigrationRate);
                    reader.next();
                    meanPoissonDistance = reader.nextInt();
                    _simulationOptions.put("LDD_mean_Poisson_distance_"+layerName, meanPoissonDistance);
                }
                reader.next();
                minAngle = reader.nextDouble();
                _simulationOptions.put("LDD_minimum_angle_"+layerName, minAngle);
                reader.next();
                maxAngle = reader.nextDouble();
                _simulationOptions.put("LDD_maximum_angle_"+layerName, maxAngle);
                reader.next();
                lddSexRatio = reader.nextDouble();
                _simulationOptions.put("LDD_sex_ratio_"+layerName, lddSexRatio);
                
                if(minAngle < -360 || minAngle > 360 ||
                        maxAngle < -360 || maxAngle > 360){
                    throw new IllegalArgumentException(
                            "Min and Max angle values must"
                                    + " in the interval [-360, 360]");
                }
                minAngle = Math.toRadians(minAngle);
                maxAngle = Math.toRadians(maxAngle);
            }

            reader.next();
            double dominantMalePercent = reader.nextDouble();
            _simulationOptions.put("Proportion_reproductive_males_"+layerName, dominantMalePercent);

            reader.next();
            double dominantFemalePercent = reader.nextDouble();
            _simulationOptions.put("Proportion_reproductive_females_"+layerName, dominantFemalePercent);

            reader.next();
            String tmpMatingSystem = reader.next();
            _simulationOptions.put("Mating_system_"+layerName, tmpMatingSystem);
            
            MatingSystem matingSystem;
            if(tmpMatingSystem.equalsIgnoreCase("random"))
                matingSystem = MatingSystem.RANDOM;
            else if(tmpMatingSystem.equalsIgnoreCase("monogamy"))
                matingSystem = MatingSystem.MONOGAMY;
            else if(tmpMatingSystem.equalsIgnoreCase("soft_monogamy"))
                matingSystem = MatingSystem.SOFTMONOGAMY;
            else if(tmpMatingSystem.equalsIgnoreCase("polygyny"))
                matingSystem = MatingSystem.POLYGYNY;
            else if(tmpMatingSystem.equalsIgnoreCase("polyandry"))
                matingSystem = MatingSystem.POLYANDRY;
            else throw new IllegalArgumentException("matingSystem in "+path+".txt"+" is not correctly defined."
                    + " \nMust be \"random\", \"monogamy\", \"soft_monogamy\", \"polygyny\" or \"polyandry\".");
            
            reader.next();
            double ratioSettlers = reader.nextDouble();
            _simulationOptions.put("Ratio_settlers_"+layerName, ratioSettlers);

            reader.next();
            String settlers = reader.next();
            _simulationOptions.put("Settlers_of_layer_"+layerName, settlers);
            /*TODO TIAGO: add admixture ratio somewhere around here */
            reader.close();
            _simOptsList.put("LayerOptions_"+layerName, _simulationOptions);
            
            if(lddOption == LddOptions.NO){
            return new Layer(genType, 
                    layerName, layerId,
                    randGenHelper, growthRate, shortDistanceRate,
                    shortDistanceSexRatio, dominantMalePercent,
                    dominantFemalePercent, ratioSettlers,
                    settlers, n, m, lddOption,matingSystem);
            }else if(lddOption==LddOptions.LDDKERNEL){
                return new Layer(genType, 
                    layerName, layerId,
                    randGenHelper, growthRate, shortDistanceRate,
                    shortDistanceSexRatio, dominantMalePercent,
                    dominantFemalePercent, ratioSettlers,
                    settlers, n, m,
                    lddOption, lddLambda, useMeanAsParameter, mean_shape, variance_scale,
                    lddSexRatio, minAngle, maxAngle,matingSystem);
            }else if(lddOption==LddOptions.METHOD_1){
                return new Layer(genType, 
                    layerName, layerId,
                    randGenHelper, growthRate, shortDistanceRate,
                    shortDistanceSexRatio, dominantMalePercent,
                    dominantFemalePercent, ratioSettlers,
                    settlers, n, m,
                    lddOption, ldMigrationEventRate, ldMigrationRate, lddSexRatio,
                    meanPoissonDistance, minAngle, maxAngle,matingSystem);
            }

        } catch (IOException ex) {
            Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;


    }
    
    
    private Layer readLayerFromDemeArray(){

        return null;
    }

    public void saveCLArgs(String[] clargs){
        _simulationOptions = new LinkedHashMap<>();
        _simulationOptions.put("[-option, value[, ...]]", Arrays.toString(clargs));
        _simOptsList.put("CommandLineArguments", _simulationOptions);
    }
    
    @Override
    public LinkedHashMap<String, HashMap> getProjectOptions(String[] clargs) {
        saveCLArgs(clargs);
        return _simOptsList;
    }

    @Override
    public ArrayList<String> initDemographicPlotOpt(){
        try {
            reader = getReader(_inputFolderPath + File.separator + "demographyPlot_preferences.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ArrayList<String> plotOpt = new ArrayList<>();
        
        reader.next();
        boolean isMovieMp4 = reader.nextBoolean();
        if(isMovieMp4) plotOpt.add("-mp4");
        else plotOpt.add("-gif");
        reader.next();
        int fps = reader.nextInt();
        plotOpt.add("-fps="+String.valueOf(fps));
        reader.next();
        int dpi = reader.nextInt();
        plotOpt.add("-dpi="+String.valueOf(dpi));
        reader.next();
        boolean plotSStats = reader.nextBoolean();
        if(plotSStats) plotOpt.add("--plotSumStats");
        reader.next();
        boolean isVerbose = reader.nextBoolean();
        if(isVerbose) plotOpt.add("-v");
        else plotOpt.add("-q");

        String[] tokens = reader.readLine().split(" ");
        for(int i = 1; i < tokens.length; i++){
            plotOpt.add("-simlist="+tokens[i]);
        }
        tokens = reader.readLine().split(" ");
        String[] pngGeneration = new String[tokens.length-1];
        for(int i = 1; i < tokens.length; i++){
            plotOpt.add("-saveAt="+tokens[i]);
        }
        return plotOpt;
    }

}

