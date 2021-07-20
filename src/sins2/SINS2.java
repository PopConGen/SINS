/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2;

import sins2.simulation.World;
import argparser.ArgParser;
import argparser.BooleanHolder;
import argparser.IntHolder;
import argparser.StringHolder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.SimpleFormatter;
import sins2.exceptions.InputParametersExceptions;
import sins2.helpers.parallel.Parallel;
import sins2.input_example_builder.Example_builder;
import sins2.input_output.IInputManager;
import sins2.input_output.InputManager;
import sins2.input_output.InputReplacerManager;


/**
 *
 * @author douglas&tiago
 */
public class SINS2 {

    
    private String _inputFolderPath;
    private static String[] _commandLineArgs;
     
    
    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     * 
     */    
    public static void main(String[] args) throws IOException, 
            FileNotFoundException, InterruptedException, ExecutionException {
        
        //Logging setup
        File logDir = new File("./logs/");
        logDir.mkdir();
        SimpleDateFormat format = new SimpleDateFormat("y-M-d_HHmmss");
        Handler handler = new FileHandler("./logs/"+format.format(Calendar.getInstance().getTime())+"_log_sins.log");
        Logger.getLogger("").addHandler(handler);
        SimpleFormatter formatter = new SimpleFormatter();
        handler.setFormatter(formatter);
        //****
        _commandLineArgs = args;
        try{
        SINS2 program=new SINS2();
        program.parseArgs(args);
        
        if(program._createAndRunExampleInput.value){
            //TODO: make it so that I can feed these args directly to example builder
            program._projectName.value="Premade_SINS_Project";
            program._inputFolderPath="Premade_SINS_Project";
            // input folder path could be better defined
            String inputFolderPath ="input" + File.separator + program._inputFolderPath;
            program._takeSampledParametersFromFile.value = false;
            program._outPutDirectory.value="output";
            program._numberOfSimulation.value=4;
            program._compressFormat.value="SQLdb";
            program._runInParallel.value=true;
            program._verbose.value=false;
            program._outputFormat.value="sins";
            program._makeDemographicImages.value=false;
            
            Example_builder eb = new Example_builder(
                    inputFolderPath,
                    1/*nLayers*/);
            eb.create_example();
        }
        
        System.out.println("Simulation "+ program._projectName.value + " is starting...");
        program.run();
        } catch (Exception ex){
            Logger.getLogger(SINS2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
        
    //parse input
    ArgParser parser;
    
    //especify the project Name
    private StringHolder _projectName;
    //especify the output directory
    private StringHolder _outPutDirectory;
    //specify the compression of the output format
    private StringHolder _compressFormat;
    //specify the output format
    private StringHolder _outputFormat;
    //especify if the programs runs in parallel or not.
    private BooleanHolder _runInParallel;
    //the number of simulations that the sotfware will run
    private IntHolder _numberOfSimulation;
    //the number of cores to be used in simulation
    /*Only used if the number of cores specified is less than
    the number of logical cores of the system*/
    private IntHolder _numberOfCores;
    
    //especify if the programs runs in parallel or not.
    private BooleanHolder _takeSampledParametersFromFile;
    //especify if the programs runs in parallel or not.
    private StringHolder _sampledParametersFilePath;
    //especify if the programs runs in parallel or not.
    private StringHolder _sampledParametersMapFilePath;
    
    //especifies which input will be used eg. input(int).txt
    private IntHolder _inputNumber;
    
    //especify if we want to produce the demographic images/videos
    private BooleanHolder _makeDemographicImages;
    
    private BooleanHolder _createAndRunExampleInput;
    
    private BooleanHolder _verbose;
    
    private void parseArgs(String[] args) {
        
        _projectName = new StringHolder("UnnamedProject");
        _outPutDirectory = new StringHolder("output");
        _compressFormat = new StringHolder("noComp");
        _runInParallel = new BooleanHolder(false);
        _numberOfSimulation = new IntHolder(1);
        _takeSampledParametersFromFile = new BooleanHolder(false);
        _sampledParametersFilePath= new StringHolder("input.txt");
        _sampledParametersMapFilePath= new StringHolder("mapping.txt");
        _numberOfCores = new IntHolder(999999);
        _inputNumber = new IntHolder(0);
        _verbose = new BooleanHolder(true);
        _outputFormat = new StringHolder("sins");
        _makeDemographicImages = new BooleanHolder(false);
        _createAndRunExampleInput = new BooleanHolder(false);
        
        ArgParser parser = new ArgParser("java -jar SINS2.jar -projectName [PROJECT_NAME]");
        parser.setHelpOptionsEnabled(true);
        parser.addOption("-numberOfSimulation,-numberOfSimulations,-nSim %d # Number of simulations",_numberOfSimulation);
        parser.addOption("-projectName,-pjName %s # project name",_projectName);
        parser.addOption("-of,-outfile,-outputFile,-outDir %s # output file",_outPutDirectory);
        parser.addOption("-compFormat,-outputCompFormat,-compress %s {noComp,fZip,rZip,bCZip,SQLdb} "
                + "#especification of the compression output format",_compressFormat);
        parser.addOption("-parallel %b"
                + "#should the simulation replicates be run in parallel?",_runInParallel);
        parser.addOption("-parallelCores, -parCores %d # number of cpu cores to use in simulation", _numberOfCores);
        parser.addOption("-v,-verbose %b # prints simulation timestep", _verbose);
        parser.addOption("-takeSampledParametersFromFile %b"
                + "#",_takeSampledParametersFromFile);
        parser.addOption("-sampledParametersFilePath %s"
                + "#",_sampledParametersFilePath);
        parser.addOption("-sampledParametersMapFilePath %s "
                + "#",_sampledParametersMapFilePath);
        parser.addOption("-inputNumber %d # input number",_inputNumber);//Tiago
        parser.addOption("-outputFormat %s {sins,adegenet,custom}",_outputFormat);
        parser.addOption("-makeDemographicImages,-mkDemImg %b", _makeDemographicImages);
        parser.addOption("-makeExampleInput,-createExampleInput, -createIn %b", _createAndRunExampleInput);
        /*
        edit unmatchedArgs if needed to process some arguments that should not
        follow the normal options
        String[] unmatchedArgs=null;
        unmatchedArgs=parser.matchAllArgs (args, 0, parser.EXIT_ON_ERROR);
        */
        parser.matchAllArgs(args);
        
        _inputFolderPath=_projectName.value;
        
        // If args is null then create and run example input
        if(args.length == 0){_createAndRunExampleInput.value=true;}
    }
    
    static PrintStream originalStream = System.out;
    private void printStreamVerbose(boolean isVerbose) {

        if (isVerbose) {
            System.setOut(originalStream);
        } else {
            PrintStream dummyStream = new PrintStream(new OutputStream() {
                public void write(int b) {
                    //NO-OP
                }
            });
            
            System.setOut(dummyStream);
        }
    }
    
    
    private void getTimeElapsed(String process, long startTime){
       
        
        long myTime = (System.currentTimeMillis() - startTime);
        String timeSt = String.format("%02dhh : %02dmm : %02dss %02dms",
                TimeUnit.MILLISECONDS.toHours(myTime),
                TimeUnit.MILLISECONDS.toMinutes(myTime)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(myTime)),
                TimeUnit.MILLISECONDS.toSeconds(myTime)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(myTime)),
                TimeUnit.MILLISECONDS.toMillis(myTime)
                - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(myTime)));
        System.out.println(process+" "+this._projectName.value +" has finished, time elapsed: " + timeSt);
    }
    
    private void processDemographicImages(long startTime) throws IOException {
        if (_makeDemographicImages.value) {
            
            /*String pythonProgPath =
                    "SINS_PlotDemographyStats" + 
                    File.separator + 
                    "dist" +
                    File.separator + 
                    "PlotDemographySins";*/

            String pythonProgPath =
                "SINS_PlotDemographyStats" + 
                File.separator + 
                "PlotDemographySins";
            
            File pyProg = new File(pythonProgPath);
            
            List<String> pyArgs = new ArrayList<>();
            
            if(pyProg.exists() && pyProg.isFile()){
                pyArgs.add(pythonProgPath);
            }else{
                pythonProgPath += ".py";
                pyArgs.add("python3");
                pyArgs.add(pythonProgPath);
            }

            pyArgs.add("--projectPath=" +
                    _outPutDirectory.value +
                    File.separator +
                    _projectName.value);
            
            /* im calling the input manager again...
            perhaps there is a more elegant solution*/
            IInputManager input = getInputManager();
            
            ArrayList<String> pyOpts = input.initDemographicPlotOpt();
            
            for(int i = 0; i < pyOpts.size(); i++){
                pyArgs.add(pyOpts.get(i));
            }
             
            ProcessBuilder pb = new ProcessBuilder(pyArgs);
            Process p = pb.start();
            BufferedReader pyIn = new BufferedReader(
                    new InputStreamReader(
                            p.getInputStream()
                    )
            );
            BufferedReader pyErr = new BufferedReader(
                    new InputStreamReader(
                            p.getErrorStream()
                    )
            );

            String line = "";
            while ((line = pyIn.readLine()) != null) {
                // display each output line form python script
                System.out.println(line);
            }
            while ((line = pyErr.readLine()) != null) {
                // display each output line from python err
                System.err.println(line);
            }
            getTimeElapsed("Image processing", startTime);
        }
    }
    
    private void run() throws InputParametersExceptions {
       if(!_runInParallel.value)
           runNonParallel();
       else try {
           parallelSimpleVersion();
       } catch (Exception ex) {
           Logger.getLogger(SINS2.class.getName()).log(Level.SEVERE, null, ex);
       }
    }
    
    private IInputManager getInputManager(){        
       if(_takeSampledParametersFromFile.value){
           
           return new InputReplacerManager(_inputFolderPath, _inputNumber.value);
       
       }
       return new InputManager(_inputFolderPath);            
    }

    private void runNonParallel() throws InputParametersExceptions {
        try {
            printStreamVerbose(_verbose.value);
            long startTime = System.currentTimeMillis();
            World world = new World(_outPutDirectory.value,_compressFormat.value,_outputFormat.value);// see parameters from world construction

            String projectName = _inputFolderPath;

            _inputFolderPath = "input" + File.separator + _inputFolderPath;
            
            IInputManager input = getInputManager();            
            world.setInputManager(input);
            world.getWriter().setProjectName(projectName);
   
            int simCount = _numberOfSimulation.value;

            
            for (int i = 1; i <= simCount; i++) {
                world.beginSimulation(i, projectName);
            }
            //always print the "finishing time" information regardless of option set
            printStreamVerbose(true);
            
            world.getWriter().printSimulationOptions(input.getProjectOptions(_commandLineArgs));
            getTimeElapsed("Simulation",startTime);
            processDemographicImages(startTime);
        } catch (IOException ex) {
            Logger.getLogger(SINS2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void parallelSimpleVersion() throws FileNotFoundException, IOException, InterruptedException, ExecutionException{
        
        printStreamVerbose(_verbose.value);
        long startTime = System.currentTimeMillis();
        /*
        World world = new World(_outPutDirectory.value,_outPutFormat.value);// see parameters from world construction

        String projectName = _inputFolderPath;*/

        _inputFolderPath = "input" + File.separator + _inputFolderPath;
                        
        int simCount = _numberOfSimulation.value;
        
        List<Future> futuresList = new ArrayList<>();
        int nrOfProcessors = Runtime.getRuntime().availableProcessors();
        
        /*if number of cores specified by the user is less than the available cores in the system
        use the nb of cores specified by the user*/
        if(_numberOfCores.value < nrOfProcessors) nrOfProcessors = _numberOfCores.value;
        
        ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
     
        for(int index = 0; index < simCount; index++){
           futuresList.add(eservice.submit(new SimulationTask(this,index+1)));
        }
        
        Object taskResult;
        for(Future future:futuresList) {
            try {
                 taskResult = future.get();
                 //System.out.println("result "+taskResult);
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace(System.err);
            }
        }
         
        //always print the "finishing time" information regardless of option set        
        printStreamVerbose(true);
        getTimeElapsed("Simulation",startTime);
        processDemographicImages(startTime);
        System.exit(0);
       
    }
    
    
    
    
    class SimulationTask implements Callable {

        SINS2 program;
        int _simCount;

        public SimulationTask(SINS2 proSins2, int simCount) {
            program = proSins2;
            _simCount = simCount;
        }

        @Override
        public Object call() throws Exception {
            World world = new World(program._outPutDirectory.value, program._compressFormat.value, _outputFormat.value);// see parameters from world construction

            //InputManager input=new InputManager(_inputFolderPath);
            //world.initializeFromInput(program._inputFolderPath);
            IInputManager input = getInputManager();
            world.setInputManager(input);
            //world.getWriter().setProjectName(program._projectName.value);
            
            world.beginSimulation(_simCount, program._projectName.value);
            world.getWriter().printSimulationOptions(input.getProjectOptions(_commandLineArgs));
            return true;
        }

    }

}
