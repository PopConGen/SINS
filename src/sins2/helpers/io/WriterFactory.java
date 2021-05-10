/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.helpers.io;

/**
 *
 * @author douglas
 */
public class WriterFactory {

    public enum OutputFormat {
        SINS, ADEGENET, CUSTOM
    };

    private static OutputFormat _outputFormat;

    public static IWriterHelper getWriterHelper(String writerId, String outformat, String outputDir) {

        if (outformat.equalsIgnoreCase("sins")) {
            _outputFormat = OutputFormat.SINS;
        } else if (outformat.equalsIgnoreCase("adegenet")) {
            _outputFormat = OutputFormat.ADEGENET;
        } else if (outformat.equalsIgnoreCase("custom")) {
            _outputFormat = OutputFormat.CUSTOM;
        } else {
            _outputFormat = OutputFormat.SINS;
        }

        if (writerId.compareTo("noComp") == 0) {
            return new WriterHelper(_outputFormat, outputDir);
        } else if (writerId.compareTo("fZip") == 0) {
            return new Writer7zHelper(Writer7zHelper.CompressionType.FastCompression, _outputFormat, outputDir);
        } else if (writerId.compareTo("rZip") == 0) {
            return new Writer7zHelper(Writer7zHelper.CompressionType.StandarCompression, _outputFormat, outputDir);
        } else if (writerId.compareTo("bZip") == 0) {
            return new Writer7zHelper(Writer7zHelper.CompressionType.BestRadioCompression, _outputFormat, outputDir);
        } else if (writerId.compareTo("SQLdb") == 0) {
            return new WriterSqlHelper(outputDir);
        }
        //the writer by default is the standar writer
        else {
            return new WriterHelper(_outputFormat, outputDir);
        }

    }
}
