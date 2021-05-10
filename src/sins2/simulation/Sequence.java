package sins2.simulation;

import java.io.BufferedWriter;
import sins2.exceptions.InputParametersExceptions;

import sins2.helpers.random.RandGenHelper;
import sins2.simulation.Site.SiteType;

/**
 * TODO: modify and check this file. This file was taken from SINS1
 */
public class Sequence {

    //class which represents an array of site : a sequence. 
    private Site[] _sequence;
    RandGenHelper _randGenHelper;
    private SiteType _seqType;
    
    public Sequence(int length, SiteType typesites, RandGenHelper randGenHelper) throws InputParametersExceptions {
        //build sequence with only 0s
        _randGenHelper = randGenHelper;
        _seqType = typesites;

        _sequence = new Site[length];
        for (int i = 0; i < this._sequence.length; i++) {
            this._sequence[i] = new Site(0, typesites, _randGenHelper);
        }
    }

    public Sequence(int[] seq, SiteType typesites, RandGenHelper randGenHelper) throws InputParametersExceptions {
        _randGenHelper = randGenHelper;
        _seqType = typesites;
        _sequence = new Site[seq.length];
        for (int i = 0; i < this._sequence.length; i++) {
            _sequence[i] = new Site(seq[i], typesites, _randGenHelper);
        }
    }
    
    /**
     * 
     *@author Tiago
     */
    public Sequence(Site[] sequence, RandGenHelper randGenHelper){
    
        _randGenHelper = randGenHelper;
        //Seq = new Site[sequence.length];
        _sequence = sequence;
        
        
    }
    
    public void setSeqType(SiteType typeOfSeq){
        _seqType = typeOfSeq;
    }
    
    public void setSeq(Site[] seq) {
        _sequence = seq;
    }

    public Site[] getSeq() {
        return _sequence;
    }

    public void mutation() {

        _sequence[_randGenHelper.getUniformGenerator().nextIntFromTo(0, this._sequence.length - 1)].mutation();
    }

    public void print(BufferedWriter buff) {
        for (int i = 0; i < this._sequence.length; i++) {
            this._sequence[i].print(buff);
        }
    }

    public int getLength() {
        
        return this._sequence.length;
    }

    public void copySeq(Sequence in) throws InputParametersExceptions {
        
        this._sequence = new Site[in.getLength()];
        for (int i = 0; i < this._sequence.length; i++) {
            if (in.getSeq()[i].getType() == SiteType.MICROSAT) {
                this._sequence[i] = new Site(in.getSeq()[i].getValue(), SiteType.MICROSAT, _randGenHelper);
            } else if (in.getSeq()[i].getType() == SiteType.SEQUENCE) {
                this._sequence[i] = new Site(in.getSeq()[i].getValue(), SiteType.SEQUENCE, _randGenHelper);
            } else {
                this._sequence[i] = new Site(in.getSeq()[i].getValue(), SiteType.SNP, _randGenHelper);
            }
        }
    }
    
    //TODO to finish
    public void copySeqNew(Sequence inSeq) throws InputParametersExceptions{
        
        this._sequence = new Site[inSeq.getLength()];
        
        
        for (int i = 0; i < this._sequence.length; i++) {
            
            this._sequence[i].copySite(inSeq.getSeq()[i]);
            
        }

    } 

    public String getRepr() {
        StringBuilder builder = new StringBuilder(2 * _sequence.length);

        for (Site aSite : _sequence) {
            //builder.append(_sequence[i].getRepr()).append(" ");
            builder.append(aSite.getValue()).append(" ");
        }
        
        return builder.toString();
    }
    
    /**
     * Receives StringBuilder from caller method and puts the sequence representation
     * into it. This improves on the getRepr method that used to create a new string
     * builder to get the sequence representation. There is no need to create a new
     * SB when we already create one on the @GeneticObject with the desired size.
     * 
     * @param theBuilder 
     */
    public void putSeqRepresInSBuilder(StringBuilder theBuilder){
        for (Site aSite : this._sequence) {
            theBuilder.append(aSite.getValue());
        }
    }
    
    public SiteType getSequenceType(){
        return _seqType;
    }
    
    
}
