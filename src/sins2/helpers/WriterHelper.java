/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author douglas
 */
public class WriterHelper implements IWriterHelper {
    
    private String _projectName="anonymous";
    private String _rootPath;
    private String _outDir;
    private PrintWriter _out;

    
    @Override
    public void beginToWriteSimuation(String projectName, int simulationId) {
        if(projectName.compareTo("")!=0)_projectName=projectName;
        _rootPath=_outDir + File.separator + _projectName + 
                File.separator + "simulation_" + simulationId+ File.separator;
        File dirPopGen=new File(_rootPath);
        
	dirPopGen.mkdirs();  
    }

    @Override
    public void finishedWriteOutput() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public void printLine(String geneReprs) {
       _out.write(geneReprs);
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
    public void closeCurrentRecord(){
       _out.close();
    }

    @Override
    public void setOutPutDir(String outputDir) {
        _outDir=outputDir;
    }

   
    
}
