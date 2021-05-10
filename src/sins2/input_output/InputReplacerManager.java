/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.input_output;

import java.io.File;
import java.io.FileNotFoundException;
import sins2.helpers.io.IReaderHelper;
import sins2.helpers.io.ReaderReplaceHelper;

/**
 *
 * @author douglas
 */
public class InputReplacerManager extends InputManager{
    
    String _mappingFilePath,_argumentForReplacementFile;
    
    public InputReplacerManager(String inputFolderPath, String mappingFilePath, String argumentForReplacementFile) {
        super(inputFolderPath);
        _mappingFilePath = mappingFilePath;
        _argumentForReplacementFile = argumentForReplacementFile;
    }

    public InputReplacerManager(String inputFolderPath, String mappingFileRelativePath) {

        this(inputFolderPath
                ,inputFolderPath + File.separator + mappingFileRelativePath,
                inputFolderPath + File.separator + "input.txt");
    }

    public InputReplacerManager(String inputFolderPath) {

        this(inputFolderPath
                ,inputFolderPath + File.separator + "mapping.txt",
                inputFolderPath + File.separator + "input.txt");
    }

    public InputReplacerManager(String inputFolderPath,
            String mappingFileRelativePath, int k) {

        this(inputFolderPath
                ,inputFolderPath + File.separator + mappingFileRelativePath,
                inputFolderPath + File.separator + "input" + k + ".txt");
    }

    public InputReplacerManager(String inputFolderPath, int k) {

        this(inputFolderPath
                ,inputFolderPath + File.separator + "mapping.txt",
                inputFolderPath + File.separator + "input" + k + ".txt");
    }
    
    
    
    @Override
    protected IReaderHelper getReader(String path) throws FileNotFoundException
    {
      return new ReaderReplaceHelper(path
              ,_argumentForReplacementFile,_mappingFilePath);
    }
    
    @Override
    public boolean inputChange(){
      return true;
    } 
    
    
    
}
