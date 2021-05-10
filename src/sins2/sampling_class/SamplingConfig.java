/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.sampling_class;



import java.io.IOException;
import sins2.input_output.InputManager;

/**
 *
 * @author douglas
 */
 
    
 public class SamplingConfig implements ISamplingConfig{
 
    SamplerType[] _samplerTypes;
    String[] _configFiles;
    ISampler[] _samplers;
    String _rootInputFilesPath;
    int _n, _m;
    InputManager _input;

    public SamplingConfig(SamplerType[] samplerTypes,
            String[] configFiles, String rootInputFilesPath, int n, int m,
            InputManager input) {
        _samplerTypes = samplerTypes;
        _configFiles = configFiles;
        _samplers = new ISampler[_samplerTypes.length];
        _rootInputFilesPath = rootInputFilesPath;
        _n = n;
        _m = m;
        _input=input;
    }

    @Override
    public ISampler getSampler(int layerIndex) throws IOException {
        switch (_samplerTypes[layerIndex]) {
            case all:
                return new AllSetSampler();
            case none:
                return new NoneElementSampler();
            case fixedSizeSubset:
                if (_samplers[layerIndex] == null) {
                    lazySamplerLoad(layerIndex);
                }
                return _samplers[layerIndex];
            default:
                return new AllSetSampler();
        }
    }

    private void lazySamplerLoad(int layerIndex) throws IOException {

        int[][] matrix=
                _input.<int[][]>
                getConfig("subset"
                ,_configFiles[layerIndex]);
        _samplers[layerIndex] = new FixedSizeSubsetSampler(matrix);
    }
        
}
