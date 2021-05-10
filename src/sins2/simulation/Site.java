package sins2.simulation;

import java.io.BufferedWriter;
import sins2.exceptions.InputParametersExceptions;
import sins2.helpers.random.RandGenHelper;

/**
 * TODO: modify and check this file. This file was taken from SINS1
 */
public class Site {

    //class which represents a site, SNP or microsatellite (defined by a boolean) and associated mutation mechanisms. 
    public enum SiteType {MICROSAT, SEQUENCE, SNP}
    private int value;
    private SiteType type;
    private RandGenHelper _randGenHelper;
  
    public Site(int value, SiteType type, RandGenHelper randGenHelper) throws InputParametersExceptions {

        if (value >= 0) {
            this.value = value;
        } else {
            throw new InputParametersExceptions("The value of a marker has to be equal or greater than 0.");
        }

        _randGenHelper = randGenHelper;

        this.type = type;
    }

    public void copySite(Site in) throws InputParametersExceptions {
        setValue(in.getValue());
        setType(in.getType());
    }

    public void setValue(int i) throws InputParametersExceptions {
        if (!(i < 0)) {
            this.value = i;
        } else {
            throw new InputParametersExceptions("The value of a marker has to be equal or greater than 0.");
        }
    }

    public void setType(SiteType t) {
        this.type = t;
    }

    public int getValue() {
        return this.value;
    }

    public SiteType getType() {
        return type;
    }

    public void mutation() {
        if (this.type == SiteType.MICROSAT) { // if type is microsat
            if (_randGenHelper.getUniformGenerator().nextBoolean()) {
                value = value + 1;
            } else {
                value = value - 1;
            }
        } else {// else type is sequence or SNP - interchanges between 0 and 1
            value = Math.abs(value - 1);
        }
    }

    public void print(BufferedWriter buff) {
        try {
            buff.write(getValue() + " ");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    String getRepr() {
        //return value + " ";
        return String.valueOf(value);
    }
}
