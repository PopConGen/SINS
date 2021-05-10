/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.helpers.io;

import cern.colt.Arrays;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import sins2.helpers.io.WriterFactory.OutputFormat;
import sins2.simulation.Layer;

/**
 *
 * @author douglas
 */
public class WriterHelper implements IWriterHelper {
    
    private String _projectName="anonymous";
    private String _rootPath;
    private String _outDir;
    private PrintWriter _out;
    
    private final OutputFormat _outputformat;
    private final WriterHelperType _writerType;

    public WriterHelper(OutputFormat outputformat, String outputDir) {
        
        _outputformat = outputformat;
        _outDir = outputDir;
        _writerType = WriterHelperType.NOCOMP;
    }

    @Override
    public WriterHelperType getWriterType(){
        return _writerType;
    }
    
    @Override
    public void printSimulationOptions(LinkedHashMap<String, HashMap> projOptions){
        String optFilePath = _outDir + File.separator + _projectName + File.separator + _projectName + "_options.txt";
        
        try {
            _out=new PrintWriter(new BufferedWriter(new FileWriter(optFilePath,false)));
            for (String option : projOptions.keySet()) {
                _out.write(option + " " + projOptions.get(option));
                _out.write("\n\n");
            }
            _out.close();
        } 
        catch (IOException ex) {
            Logger.getLogger(WriterHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
      
    @Override
    public void beginToWriteSimulation(String projectName, int simulationId, Layer... layers) {
        
        
        if(projectName.compareTo("")!=0)_projectName=projectName;
        _rootPath=_outDir + File.separator + _projectName + 
                File.separator + "simulation_" + simulationId+ File.separator;
        File dirPopGen=new File(_rootPath);

        deleteDir(dirPopGen); /*Delete directories that already exist so that we dont
        append simulation outputs by mistake*/
	dirPopGen.mkdirs();  
    }

    @Override
    public void finishedWriteOutput() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        _out.close();
    }

    @Override
    public void printSizeMatrix(int[][] popSize, int currentGeneration, int simulationId, String _layerName, int layerId) {
        File file = new File(_rootPath + "demography.txt");
        
        try {

            PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            fw.write("Simulation: ");
            fw.write(String.valueOf(simulationId));
            fw.write("\r\n");
            fw.write("Layer name: ");
            fw.write(_layerName+ "("+layerId+")");
            fw.write("\r\n");
            //fw.write("sizeat_");
            fw.write("Generation: ");
            fw.write(String.valueOf(currentGeneration));
            fw.write("\r\n");
            //printig the matrix
            fw.write("\r\n");
            //Tiago: tagging the demography map so that it is easier to parse
            fw.write("[map]");
            fw.write("\r\n");
            for (int row = 0; row < popSize.length; row++) {
                for (int column = 0; column < popSize[0].length-1; column++) {
                    fw.print(popSize[row][column]);
                    fw.write(" ");
                }
                 fw.print(popSize[row][popSize[0].length-1]);
                 fw.write("\r\n");
            }
            fw.write("[/map]");
            fw.write("\r\n");


            fw.write("\r\n");
            fw.write("\r\n");
            fw.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    @Override
    public void printLine(int geneId, String geneReprs) {
       
        switch (_outputformat) {
            case ADEGENET:
                _out.write(FormatHelper.adegenetFormat(geneReprs));
                break;
            case SINS:
                _out.write(geneReprs);
                break;
            case CUSTOM:
                throw new UnsupportedOperationException("Custom output format is not supported yet.");
            default:
                throw new AssertionError();
        }
        
    }
    
    
    @Override
    public void openRecord(String id,int generation){
        try {
            _out=new PrintWriter(new BufferedWriter(new FileWriter(_rootPath+id+".txt",true)));
        } 
        catch (IOException ex) {
            Logger.getLogger(WriterHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        @Override
    public void openSumStatsRecord(String id,int generation){
        try {
            _out=new PrintWriter(new BufferedWriter(new FileWriter(_rootPath+id+".txt",true)));
        } 
        catch (IOException ex) {
            Logger.getLogger(WriterHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void closeCurrentRecord(){
       _out.close();
    }

    @Override
    public void closeSumStatsCurrentRecord(){
       _out.close();
    }
    
    @Override
    public void setOutPutDir(String outputDir) {
        _outDir=outputDir;
    }
    
    @Override
    public void printSummaryStatsHeader(String[] statNames){
        _out.write(FormatHelper.sumStatsHeader(statNames));
    }
    
    @Override
    public void printSummaryStatsLine(String stats){
        _out.write(stats);
    }

    @Override
    public void setProjectName(String _projectName) {
        this._projectName = _projectName;
    }
    
    /*Recursive method to delete all the directories and files for a given path*/
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    return dir.delete();
    }
}
