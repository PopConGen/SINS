/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author douglas
 */
public class Writer7zHelper implements IWriterHelper{

    private ZipOutputStream _rootOut;
    private ZipEntry _currentEntry;
    
    private String _rootPath,_projectName="anonymous";
    private String _outDir;
    CompressionType _type;
    
    public Writer7zHelper(CompressionType type){
      _type=type;
    }
    
    @Override
    public void beginToWriteSimuation(String projectName, int simulationId) {
        try {
            if (projectName.compareTo("") != 0) {
                _projectName = projectName;
            }
            _rootPath = _outDir + File.separator + _projectName
                    + File.separator + "simulation_" + simulationId + File.separator;
            
            File dirPopGen=new File(_rootPath);
        
	    dirPopGen.mkdirs();  
            
            _rootOut = new ZipOutputStream(new FileOutputStream(_rootPath+"demographicData.zip", true));
            //_rootOut.setLevel(Deflater.BEST_SPEED);//imporve the compresion ratio
            switch (_type) {
                case FastCompression:
                    _rootOut.setLevel(Deflater.BEST_SPEED);//imporve the speed of the algorithm
                    break;
                case BestRadioCompression:
                    _rootOut.setLevel(Deflater.BEST_COMPRESSION);//imporve the compresion ratio
                    break;                
                default:;
                    
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
    public void printLine(String geneReprs) {
        byte[] buff=geneReprs.getBytes();
        try {
            _rootOut.write(buff, 0, buff.length);
        } catch (IOException ex) {
            Logger.getLogger(Writer7zHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void printSizeMatrix(int[][] popSize, int currentGeneration, int simulationId, String _layerName) {
        File file = new File(_rootPath + "demography.txt");
        
        try {

            PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            fw.write("simulation");
            fw.write(String.valueOf(simulationId));
            fw.write("\r\n");
            fw.write(_layerName);
            fw.write("\r\n");
            fw.write("sizeat_");
            fw.write(String.valueOf(currentGeneration));
            fw.write("\r\n");
            //printig the matrix
            fw.write("\r\n");
            for (int row = 0; row < popSize.length; row++) {
                for (int column = 0; column < popSize[0].length-1; column++) {
                    fw.print(popSize[row][column]);
                    fw.write(" ");
                }
                 fw.print(popSize[row][popSize[0].length-1]);
                 fw.write("\r\n");
            }
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
     
     
     public static enum CompressionType{
          FastCompression,StandarCompression,BestRadioCompression;
     }
    
}
