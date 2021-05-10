/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.input_example_builder;

import sins_buildinput.SINS_BuildInput;

/**
 *
 * @author ph0bi4
 */
public class Example_builder {
    
    
    private final String _sinsMainInputDir;
    private final int _numberOfLayers;
//    private final sins_buildinput.SINS_BuildInput _example_builder;
    
    public Example_builder(String sinsMainInputDir, int numberOfLayers){
        
        _sinsMainInputDir = sinsMainInputDir;
        _numberOfLayers = numberOfLayers;
//        _example_builder = new SINS_BuildInput(_numberOfLayers, _sinsMainInputDir);
    
    }
    
    public void create_example(){
        String[] args = new String[]{"-sinsIn="+_sinsMainInputDir,"-nLayers="+String.valueOf(_numberOfLayers)};
        // SINS_BuildInput.main(args);
        SINS_BuildInput.main(args);
    }
    
}
