package sins2.simulation;

import java.io.BufferedWriter;
import sins2.exceptions.InputParametersExceptions;
import sins2.helpers.random.RandGenHelper;
import sins2.simulation.Site.SiteType;

/**
 * TODO: modify and check this file. This file was taken from SINS1 TO CONSIDER
 * USE A UNIFORM GENERATOR HERE INSTEAD OF RanGenHelper
 */
public class GeneticObject {
    
    public enum GeneticObjectType {X, Y, MtDNA, AUTOSOME}
    //This class represents a genetic object, in other terms an 1D or 2D array of sequences.  
    private Sequence[] _zome;
    private GeneticObjectType _type;
    private RandGenHelper _ranGenHelper;

    public GeneticObject(GeneticObjectType type, int length, SiteType typesites, RandGenHelper ranGenHelper) throws InputParametersExceptions {
        _type = type;
        _ranGenHelper = ranGenHelper;
        if (type == GeneticObjectType.AUTOSOME) {
            _zome = new Sequence[2];
            _zome[0] = new Sequence(length, typesites, _ranGenHelper);
            _zome[1] = new Sequence(length, typesites, _ranGenHelper);
        } else {
            _zome = new Sequence[1];
            _zome[0] = new Sequence(length, typesites, _ranGenHelper);
        }
    }

    public GeneticObject(GeneticObjectType type, int[] seq, SiteType typesites, RandGenHelper ranGenHelper) throws InputParametersExceptions {
        _type = type;
        _ranGenHelper = ranGenHelper;
        if (type == GeneticObjectType.AUTOSOME) {
            _zome = new Sequence[2];
            _zome[0] = new Sequence(seq, typesites, _ranGenHelper);
            _zome[1] = new Sequence(seq, typesites, _ranGenHelper);
        } else {
            _zome = new Sequence[1];
            _zome[0] = new Sequence(seq, typesites, _ranGenHelper);
        }
    }

    //Tiago
    public GeneticObject(GeneticObjectType type/*type of chromossome*/,
            Sequence aSequence/*a Sequence, an array of sites*/,
            RandGenHelper ranGenHelper) {
        _type = type;
        _ranGenHelper = ranGenHelper;

        if (type == GeneticObjectType.AUTOSOME) {
            _zome = new Sequence[2];
            _zome[0] = new Sequence(aSequence.getSeq(), _ranGenHelper);
            _zome[1] = new Sequence(aSequence.getSeq(), _ranGenHelper);
        } else {
            _zome = new Sequence[1];
            _zome[0] = new Sequence(aSequence.getSeq(), _ranGenHelper);
        }

    }
//*******

    public void mutation() {
        if (_type == GeneticObjectType.AUTOSOME) {
            if (_ranGenHelper.getUniformGenerator().nextBoolean()) {
                _zome[0].mutation();
            } else {
                _zome[1].mutation();
            }
        } else {
            _zome[0].mutation();
        }
    }

    public Sequence[] getZome() {
        return _zome;
    }

    public GeneticObjectType getGenObjType() {
        return _type;
    }

    public void setType(GeneticObjectType t) {
        _type = t;
    }

    public void copyGeneticObject(GeneticObject original) throws InputParametersExceptions {
        setType(original.getGenObjType());
        
        for (int i = 0; i < _zome.length; i++) {
            _zome[i].copySeq(original.getZome()[i]);
        }
    }

    public static GeneticObject reproduction(GeneticObject female, GeneticObject male) throws InputParametersExceptions {
        
        
        GeneticObject child = new GeneticObject(
                female.getGenObjType(),
                female.getZome()[0].getSeq().length, 
                female.getZome()[0].getSequenceType(),
                female._ranGenHelper);
        
        
        if (female.getGenObjType() == GeneticObjectType.X) {
            
            if (female._ranGenHelper.getUniformGenerator().nextBoolean()) {
                child.copyGeneticObject(female);
            } else {
                child.copyGeneticObject(male);
            }
            
        } else if (female.getGenObjType() == GeneticObjectType.MtDNA) {
        
            child.copyGeneticObject(female);
        
        } else {
            
            if (female._ranGenHelper.getUniformGenerator().nextBoolean()) {
                child.getZome()[0].copySeq(female.getZome()[0]);
            } else {
                child.getZome()[0].copySeq(female.getZome()[1]);
            }
            
            if (female._ranGenHelper.getUniformGenerator().nextBoolean()) {
                child.getZome()[1].copySeq(male.getZome()[0]);

            } else {
                child.getZome()[1].copySeq(male.getZome()[1]);
            }
        }
        return child;
    }

    public void print(BufferedWriter buff, int gen, int deme_i, int deme_j, String identity, String mother, String father) {
        try {
            for (int i = 0; i < _zome.length; i++) {
                if (_zome[0].getLength() > 0) {
                    buff.write(String.valueOf(gen + 1) + " ");
                    buff.write(String.valueOf(deme_i) + " ");
                    buff.write(String.valueOf(deme_j) + " ");
                    buff.write(String.valueOf(identity) + " ");
                    buff.write(String.valueOf(mother) + " ");
                    buff.write(String.valueOf(father) + " ");
                    _zome[i].print(buff);

                }
                buff.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getLength() {
        return _zome[0].getLength();
    }

    //TODO: the way this is coded some unexpected behavior can probably be
    //expected when the X zome is taken into account in the calculations (aka Xlength != 0)
    public int getNbSitesInZome() {
        int nbSites;
        if (getGenObjType() == GeneticObjectType.AUTOSOME) {
            nbSites = getLength() * 2;
        } else {
            nbSites = getLength();
        }
        return nbSites;
    }

    String getReprs(int gen, int deme_i, int deme_j, String identity, String mother, String father) {
        //adjust better the initial sizr of the builder
        StringBuilder builder = new StringBuilder(2 * _zome.length + 20);

        for (int i = 0; i < _zome.length; i++) {
            builder.append(String.valueOf(gen) + " ");
            builder.append(String.valueOf(deme_i) + " ");
            builder.append(String.valueOf(deme_j) + " ");
            builder.append(String.valueOf(identity) + " ");
            builder.append(String.valueOf(mother) + " ");
            builder.append(String.valueOf(father) + " ");
            builder.append(_zome[i].getRepr());
            builder.append("\r\n");
        }
        return builder.toString();
    }
/**
 * Get this genetic objects' data for a single individual.
 * If marker is autosome, then each individual MUST have 2 alleles,
 * therefore print both marker alleles at the end of the line instead
 * of repeating the line twice with only the allele differing
 * for example:
 *  old:
 *  (microsat)
 *      10 3 4 F_137_0_3_4 F_154_0_3_4 M_329_0_4_4 500
 *      10 3 4 F_137_0_3_4 F_154_0_3_4 M_329_0_4_4 498
 *  (SNP)
 *      60 4 1 F_211_0_4_1 F_212_0_4_1 M_167_0_4_1 1
 *      60 4 1 F_211_0_4_1 F_212_0_4_1 M_167_0_4_1 0
 *  new:
 *  (microsat)
 *      10 3 4 F_137_0_3_4 F_154_0_3_4 M_329_0_4_4 500 498
 *  (SNP)
 *      60 4 1 F_211_0_4_1 F_212_0_4_1 M_167_0_4_1 1 0
 * 
 * @param gen
 * @param deme_i
 * @param deme_j
 * @param identity
 * @param mother
 * @param father
 * @return String representation of this genetic object for this individual
 */
    String getReprs_NEW(int gen, int deme_i, int deme_j, String identity, String mother, String father) {
        //adjust better the initial sizr of the builder
        //boolean isZomeDiploid = _zome.length > 1;
        int strSize;
        if (_zome.length > 1) {
            //buffer +
            //length 60 ~ "99999 99 99 M_9999_0_99_99 F_9999_0_99_99 M_9999_0_99_99 "
            //+ (zome.len * 2"spaces+values" * 2"diploid sequence")
            strSize = 5 + 60 + (this.getLength() * 2*2);
        } else {
            strSize = 5 + 60 + (this.getLength()*2);
        }
        
        //StringBuilder builder = new StringBuilder(2 * _zome.length + 20);
        StringBuilder builder = new StringBuilder(strSize);

        builder.append(gen).append(" ");
        builder.append(deme_i).append(" ");
        builder.append(deme_j).append(" ");
        builder.append(identity).append(" ");
        builder.append(mother).append(" ");
        builder.append(father).append(" ");
        //length of zome is at least of 1; if length > 1 then do the for loop
        //builder.append(_zome[0].getRepr());
        _zome[0].putSeqRepresInSBuilder(builder);
        if (_zome.length > 1) {
            //builder.append("/ ").append(_zome[1].getRepr());
            builder.append("/");
            _zome[1].putSeqRepresInSBuilder(builder);
        }        
        builder.append("\r\n");

        return builder.toString();
    }
    
    
    String getOnlyGeneReprs(){
        StringBuilder builder = new StringBuilder(5 + 60 + (this.getLength() * 2*2));
        _zome[0].putSeqRepresInSBuilder(builder);
        if (_zome.length > 1) {
            //builder.append("/ ").append(_zome[1].getRepr());
            builder.append("/");
            _zome[1].putSeqRepresInSBuilder(builder);
        }        
        builder.append(" ");
        return builder.toString();
    }
    
}
