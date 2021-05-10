/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.sampling_class;

import java.io.IOException;

/**
 *
 * @author douglas
 */
public interface ISamplingConfig {

    ISampler getSampler(int layerIndex) throws IOException;
    
}
