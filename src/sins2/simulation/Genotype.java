/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.simulation;

import sins2.simulation.Site.SiteType;


/**
 *
 * @author douglas
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *


 /**
 *
 * @author douglas
 * 
 * This class contain the information about the genotype of a population
 */
public final class Genotype {

    //Put all this information in the allele class. in that way it is needed only read alleles information    
    //the variables have the same name mentioned in the user manual
    private final int _Xlength, _Ylength, _mtDNAlength, _nbAutosomes;
    private final SiteType _typeX, _typeMt, _typeY; 
    private final SiteType[] _typeA;
    private final int[] _Alength;
    //the followings fields are not used
    //private double _XmutationRate,_YmutationRate;
    //the first three members of the array represent XMutationRate,YMutationRate,mtDNAmutationRate
    private final double[] _mutationRate;
    private final AlleleFrequency[] _allelesFrequenciesInformation;

    /**
     * default constructor
     */
    /*public Genotype() {
    }*/

    public Genotype(int xLength, SiteType typeX, int yLength, SiteType typeY,
            int mtDNAlength, SiteType typeMt, int nbAutosomes, SiteType[] typeA,
            int[] Alenght, double[] mutationRate,
            AlleleFrequency[] allelesFrequenciesInformation) {

        _Xlength = xLength;
        _typeX = typeX;

        _Ylength = yLength;
        _typeY = typeY;

        _mtDNAlength = mtDNAlength;
        _typeMt = typeMt;

        _nbAutosomes = nbAutosomes;

        _typeA = typeA;
        _Alength = Alenght;

        _mutationRate = mutationRate;

        _allelesFrequenciesInformation = allelesFrequenciesInformation;
    }

    public int getNbAutosomes() {
        return _nbAutosomes;
    }
    
    public AlleleFrequency getAlleleXInf() {
        return _allelesFrequenciesInformation[0];
    }

    public SiteType getTypeX() {
        return _typeX;
    }

    public int getXLength() {
        return _Xlength;
    }

    public AlleleFrequency getAlleleYInf() {
        return _allelesFrequenciesInformation[1];
    }

    public SiteType getTypeY() {//Tiago
        return _typeY;
    }

    public int getYLength() {
        return _Ylength;
    }

    public SiteType getTypeA(int autosomeId) {
        return _typeA[autosomeId];
    }

    public AlleleFrequency getAlleleMtInf() {
        return _allelesFrequenciesInformation[2];
    }

    public SiteType getTypeMt() {
        return _typeMt;
    }

    public int getMtDNALength() {
        return _mtDNAlength;
    }

    public AlleleFrequency getAlleleAInf(int number) {
        return _allelesFrequenciesInformation[number + 3];
    }

    public int getALength(int alleleId) {
        return _Alength[alleleId];
    }

    public double getMutationRateFemales(int i) {
        if (i == 1) {
            return _mutationRate[0];//woman dont have Y cromosome
        }
        return _mutationRate[i];
    }

    public double getMutationRateMales(int i) {
        return _mutationRate[i];
    }

    //	static method which returns mutation rate per male
    public double getGlobalMaleMutationNumber() {
        double out = _Xlength * _mutationRate[0] + _Ylength * _mutationRate[1] + _mtDNAlength * _mutationRate[2];
        for (int k = 0; k < _nbAutosomes; k++) {
            out = out + (_Alength[k] << 1) * _mutationRate[k + 3];//this is the same as (_Alenght[k]*2)*_mutationRate[k+3]
        }
        return out;
    }

    public double getGlobalFemaleMutationNumber() {
        double out = (_Xlength << 1) * _mutationRate[0] + _mtDNAlength * _mutationRate[2];//this is the same as (_Xlenght*2)*_mutationRate[0]+_mtDNAlenght*_mutationRate[2]
        for (int k = 0; k < _nbAutosomes; k++) {
            out = out + (_Alength[k] << 1) * _mutationRate[k + 3];//this is the same as (_Alenght[k]*2)*_mutationRate[k+3]
        }
        return out;
    }

    public int getNbTotalSitesMale() {
        int nb = _Xlength + _Ylength + _mtDNAlength;
        for (int i = 0; i < _nbAutosomes; i++) {
            nb += _Alength[i] << 1; //the same as _Alenght[i]*2
        }
        return nb;
    }

    //Method which returns the total number of sites of a female.
    public int getNbTotalSitesFemale() {
        int nb = _Xlength << 1 + _mtDNAlength;//this is the same that 2*_Xlenght+_mtDNAlenght (<< binary operator)
        for (int i = 0; i < _nbAutosomes; i++) {
            nb += _Alength[i] << 1; //the same as _Alenght[i]*2
        }
        return nb;
    }
/**
 * 
 * @param geneId
 * @return
 * True if (X,Y,MT)zome has length aka (X,Y,MT)Length>0
 * True if zome is Autossome
 */
    boolean isRelevant(int geneId) {
        if (geneId == 0) { return _Xlength > 0; } 
        else if (geneId == 1) { return _Ylength > 0; } 
        else if (geneId == 2) { return _mtDNAlength > 0; }
        else { return true; }
    }


}
