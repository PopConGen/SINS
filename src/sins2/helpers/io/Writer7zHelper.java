/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.helpers.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import sins2.helpers.io.WriterFactory.OutputFormat;
import sins2.simulation.Layer;

/**
 *
 * @author douglas
 */
public class Writer7zHelper implements IWriterHelper{

    public static enum CompressionType {
        FastCompression, StandarCompression, BestRadioCompression;
    }
    
    private ZipOutputStream _rootOut;
    private ZipEntry _currentEntry;
    
    private String _rootPath,_projectName="anonymous";
    private String _outDir;
    CompressionType _type;
    
    private final OutputFormat _outputFormat;
    private final WriterHelperType _writerType;
    
    public Writer7zHelper(CompressionType type, OutputFormat outputFormat, String outputDir){
      _type=type;
      
      _outputFormat = outputFormat;
      _outDir = outputDir;
      _writerType = WriterHelperType.ZIP;
    }
    
    @Override
    public WriterHelperType getWriterType(){
        return _writerType;
    }
    
    @Override
    public void beginToWriteSimulation(String projectName, int simulationId, Layer... layers) {
        try {
            if (projectName.compareTo("") != 0) {
                _projectName = projectName;
            }
            _rootPath = _outDir + File.separator + _projectName
                    + File.separator + "simulation_" + simulationId + File.separator;
            
            File dirPopGen=new File(_rootPath);
            
            deleteDir(dirPopGen);
        
	    dirPopGen.mkdirs();  
            
            _rootOut = new ZipOutputStream(new FileOutputStream(_rootPath+"GeneticData.zip", true));
            //_rootOut.setLevel(Deflater.BEST_SPEED);//imporve the compresion ratio
            switch (_type) {
                case FastCompression:
                    _rootOut.setLevel(Deflater.BEST_SPEED);//imporve the speed of the algorithm
                    break;
                case BestRadioCompression:
                    _rootOut.setLevel(Deflater.BEST_COMPRESSION);//imporve the compresion ratio
                    break;                
                default:
                    _rootOut.setLevel(Deflater.DEFAULT_COMPRESSION);
                    break;
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Writer7zHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void closeCurrentRecord() {
        try {
            _rootOut.closeEntry();
        } catch (IOException ex) {
            Logger.getLogger(Writer7zHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void closeSumStatsCurrentRecord() {
        try {
            _rootOut.closeEntry();
        } catch (IOException ex) {
            Logger.getLogger(Writer7zHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void finishedWriteOutput() {
        try {
            _rootOut.close();
        } catch (IOException ex) {
            Logger.getLogger(Writer7zHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void openRecord(String id,int generation) {
        _currentEntry=new ZipEntry(id+"-gen-"+generation+".txt");
        try {
            _rootOut.putNextEntry(_currentEntry);
        } catch (IOException ex) {
            Logger.getLogger(Writer7zHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        @Override
    public void openSumStatsRecord(String id,int generation) {
        _currentEntry=new ZipEntry(id+"-gen-"+generation+".txt");
        try {
            _rootOut.putNextEntry(_currentEntry);
        } catch (IOException ex) {
            Logger.getLogger(Writer7zHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void printLine(int geneId, String geneReprs) {
        byte[] buff;
        switch (_outputFormat) {
            case ADEGENET:
                buff = FormatHelper.adegenetFormat(geneReprs).getBytes();
                break;
            case SINS:
                buff = geneReprs.getBytes();
                break;
            case CUSTOM:
                throw new UnsupportedOperationException("Custom output format is not supported yet.");
            default:
                throw new AssertionError();
        }
        
        try {
            _rootOut.write(buff, 0, buff.length);
        } catch (IOException ex) {
            Logger.getLogger(Writer7zHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        }
    }

     @Override
    public void setOutPutDir(String outputDir) {
        _outDir=outputDir;
    }
    
    @Override
    public void printSummaryStatsHeader(String[] statNames){
        byte[] buff = FormatHelper.sumStatsHeader(statNames).getBytes();
        try {
            _rootOut.write(buff, 0, buff.length);
        } catch (IOException ex) {
            Logger.getLogger(Writer7zHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void printSummaryStatsLine(String stats){
        byte[] buff = stats.getBytes();
        try {
            _rootOut.write(buff, 0, buff.length);
        } catch (IOException ex) {
            Logger.getLogger(Writer7zHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    @Override
    public void printSimulationOptions(LinkedHashMap projOptions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProjectName(String _projectName) {
        this._projectName = _projectName;
    }
    
}
