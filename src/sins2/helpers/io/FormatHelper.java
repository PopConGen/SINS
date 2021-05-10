/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.helpers.io;

/**
 *
 * @author tiagomaie <tiagomaie at igc>
 */
public class FormatHelper {

    public FormatHelper() {

    }

    static String adegenetFormat(String sinsFormat) {

        //any number of spaces or tabs or newLines
        String delims = "[ \t\r\n]+";

        String[] adegenetFormatTokens = sinsFormat.split(delims);

        StringBuilder adegenetFormat = new StringBuilder();

        adegenetFormat.append(adegenetFormatTokens[3]).append("\t");

        //genetic markers
        adegenetFormat.append(adegenetFormatTokens[6]).append("\t");

        adegenetFormat.append(adegenetFormatTokens[1]).append("\t");
        adegenetFormat.append(adegenetFormatTokens[2]).append("\t");
        //pop_"layer"."xcoord"_"ycoord"
        adegenetFormat.append("pop").append("_").append(adegenetFormatTokens[3].split("_")[2]).append(".").append(adegenetFormatTokens[1]).append("-").append(adegenetFormatTokens[2]).append("\t");
        adegenetFormat.append(adegenetFormatTokens[0]).append("\r\n");

        return adegenetFormat.toString();
    }
    
    static String sumStatsHeader(String[] statNames){
            StringBuilder statsHeaderFormat = new StringBuilder();    
            statsHeaderFormat.append("Layer").append("\t");
            statsHeaderFormat.append("Generation").append("\t");
            statsHeaderFormat.append("Marker").append("\t");
            for (String statName : statNames) {
                statsHeaderFormat.append(statName).append("\t");
            }
            statsHeaderFormat.append("\r\n");
            return statsHeaderFormat.toString();
        }
    
}
