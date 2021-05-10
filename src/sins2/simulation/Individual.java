/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.simulation;

/**
 *
 * @author douglas
 */
import sins2.exceptions.InputParametersExceptions;
import sins2.helpers.random.RandGenHelper;
import sins2.simulation.GeneticObject.GeneticObjectType;

/**
 * TODO: modify and check this file. This file was taken from SINS1
 */
public class Individual {
    
    public enum IndividualSex {FEMALE, MALE}
    //This class respresents an individual, principally under a "genotype aspect".
    //It contains an array of genetic object.Instances variables are here to assure the same genotype form for all individual
    //and initalisation of different genetic objects, according to input files.
    private GeneticObject[] genObjArray;
    private int generation;
    private IndividualSex _sex;
    private String identity;
    private String mother;
    private String father;
    private int deme_i;
    private int deme_j;
    private RandGenHelper _ranGenHelper;

    //TODO:Check this method and try to undenstarn it in every detail, refactor repetitions in this method
    //Connstructor of an individual. In input you have the sexe and a boolean of initialisation. 
    //The first is just important for demes initialisation, after sexe is determined by reproduction.
    //The second also, because it allows to pick initials sequences in input initialisation variables, or not. 
    public Individual(IndividualSex sex, boolean init, String name,
            int line, int column, int time, Genotype initialGenotype,RandGenHelper ranGenHelper) throws InputParametersExceptions {
        _sex = sex;
        generation = time;
        identity = name;
        father = "Original";
        mother = "Original";
        deme_i = line;
        deme_j = column;
        genObjArray = new GeneticObject[3 + initialGenotype.getNbAutosomes()];
        _ranGenHelper=ranGenHelper;
        
        if (init) {/*Tiago: if(init){ the sequences built are taken from the input values and variables}
            else{the sequences built are started from scratch with only zeros}
            */
            boolean test = true;
            double itemProbability = _ranGenHelper.getUniformGenerator().nextDouble();
            double cumulativeProbability = 0;
            int i = 0;
            while (test) {
                
                cumulativeProbability += initialGenotype.getAlleleXInf().getFrequency(i);
                
                if (itemProbability < cumulativeProbability) {
                    
                    genObjArray[0] = new GeneticObject(
                            GeneticObjectType.X,
                            initialGenotype.getAlleleXInf().getAllele(i),
                            initialGenotype.getTypeX(),_ranGenHelper);
                    test = false;
                }
                i++;
            }
            itemProbability = _ranGenHelper.getUniformGenerator().nextDouble();
            test = true;
            cumulativeProbability = 0;
            i = 0;
            
            if (_sex == IndividualSex.MALE) {
                while (test) {
                    
                    cumulativeProbability += initialGenotype.getAlleleYInf().getFrequency(i);
                    if (itemProbability <= cumulativeProbability) {
                        //this.genotype[1]=new GeneticObject("Y",Individual.allelesY[i],typeY);
                        genObjArray[1] = new GeneticObject(
                                GeneticObjectType.Y,
                                initialGenotype.getAlleleYInf().getAllele(i),
                                initialGenotype.getTypeY(),_ranGenHelper);
                        test = false;
                    }
                    i++;
                }
            } else {
                while (test) {
                    
                    cumulativeProbability += initialGenotype.getAlleleXInf().getFrequency(i);
                    if (itemProbability < cumulativeProbability) {
                        
                        genObjArray[1] = new GeneticObject(GeneticObjectType.X,
                                initialGenotype.getAlleleXInf().getAllele(i), 
                                initialGenotype.getTypeX(),_ranGenHelper);
                        test = false;
                    }
                    i++;
                }
            }
            itemProbability = _ranGenHelper.getUniformGenerator().nextDouble();
            test = true;
            cumulativeProbability = 0;
            i = 0;
            while (test) {
                
                cumulativeProbability += initialGenotype.getAlleleMtInf().getFrequency(i);

                if (itemProbability < cumulativeProbability) {
                    
                    genObjArray[2] = new GeneticObject(GeneticObjectType.MtDNA,
                            initialGenotype.getAlleleMtInf().getAllele(i), 
                            initialGenotype.getTypeMt(),_ranGenHelper);
                    test = false;
                }
                i++;
            }
            for (int j = 0; j < initialGenotype.getNbAutosomes(); j++) {
                itemProbability = _ranGenHelper.getUniformGenerator().nextDouble();
                test = true;
                cumulativeProbability = 0;
                i = 0;
                int autosomeLength;
                while (test) {
                    
                    cumulativeProbability += initialGenotype.getAlleleAInf(j).getFrequency(i);
                    if (itemProbability < cumulativeProbability) {

                        autosomeLength = initialGenotype.getALength(j);
                        int[] temp = new int[autosomeLength];
                        
                        for (int k = 0; k < autosomeLength; k++) {                       
                            temp[k] = initialGenotype.getAlleleAInf(j).getAllele(i)[k];
                        }

                        genObjArray[3 + j] = new GeneticObject(GeneticObjectType.AUTOSOME, temp, 
                                initialGenotype.getTypeA(j),_ranGenHelper);
                        test = false;
                    }
                    i++;
                }
            }
        } else {

            genObjArray[0] = new GeneticObject(GeneticObjectType.X,
                    initialGenotype.getXLength(), 
                    initialGenotype.getTypeX(),_ranGenHelper);
            if (_sex == IndividualSex.MALE) {
             
                genObjArray[1] = new GeneticObject(GeneticObjectType.Y,
                        initialGenotype.getYLength(), 
                        initialGenotype.getTypeY(),_ranGenHelper);
            } else {
         
                genObjArray[1] = new GeneticObject(GeneticObjectType.X,
                        initialGenotype.getXLength(),
                        initialGenotype.getTypeX(),_ranGenHelper);
            }
            
            genObjArray[2] = new GeneticObject(GeneticObjectType.MtDNA, 
                    initialGenotype.getMtDNALength(), 
                    initialGenotype.getTypeMt(),_ranGenHelper);

            
            for (int i = 0; i < initialGenotype.getNbAutosomes(); i++) {
                                
                genObjArray[i + 3] = new GeneticObject(GeneticObjectType.AUTOSOME,
                        initialGenotype.getALength(i), 
                        initialGenotype.getTypeA(i),_ranGenHelper);
            }
        }
    }
        
    //Basic constructor of an individual which just declares its variable.
    public Individual(int numberOfAutosomes,RandGenHelper ranGenHelper) {
        _ranGenHelper=ranGenHelper;
        genObjArray = new GeneticObject[3 + numberOfAutosomes];
        
    }

    public static Individual reproduction(Individual mother, Individual father, String name, int line, int column, int time, int populationId) throws InputParametersExceptions {

        Individual child = new Individual(mother.genObjArray.length - 3, mother._ranGenHelper); // Here the GeneticObjects are not initialized 
        child.father = father.identity;
        child.mother = mother.identity;
        child.deme_i = line;
        child.deme_j = column;
        child.generation = time;
        
        child.genObjArray[0] = GeneticObject.reproduction(mother.getGenotype()[0], mother.getGenotype()[1]);
        child.genObjArray[1] = GeneticObject.reproduction(father.getGenotype()[0], father.getGenotype()[1]);
        
        
        for (int i = 2; i < mother.genObjArray.length; i++) {
            child.genObjArray[i] = GeneticObject.reproduction(mother.getGenotype()[i], father.getGenotype()[i]);
        }
        
        StringBuilder id = new StringBuilder(15);
        //if is male
        if (child.genObjArray[1].getGenObjType() == GeneticObject.GeneticObjectType.Y) {
            id.append("M_").append(name).append("_").append(populationId).append("_").append(line).append("_").append(column);
            child._sex = IndividualSex.MALE;
            //child.identity = "M_" + name + "_" + String.valueOf(populationId) + "_" + String.valueOf(line) + "_" + String.valueOf(column);

        } else {
            //child.identity = "F_" + name + "_" + String.valueOf(populationId) + "_" + String.valueOf(line) + "_" + String.valueOf(column);
            id.append("F_").append(name).append("_").append(populationId).append("_").append(line).append("_").append(column);
            child._sex = IndividualSex.FEMALE;
        }
        
        child.identity = id.toString();

        return child;
    }
    
    //getter and setter. Maybe to be cleaned.
    public void setGenotype(GeneticObject[] genotype) {
        this.genObjArray = genotype;
    }

    public GeneticObject[] getGenotype() {
        return genObjArray;
    }

   
    //method to do mutation over a genotype of an individual,according to different mutation rates and sequence length of each genetic object
    public void mutation(Genotype genotypeInf) {
        double totalLength; // Essentially lengthK * mutRateK + lengthL * mutRateL + lengthM * mutRateM + ...
        if (this.isMale()) {
            
            totalLength = genotypeInf.getGlobalMaleMutationNumber();
            double rndZomeToMutate = _ranGenHelper.getUniformGenerator().nextDoubleFromTo(0, totalLength);
            boolean mutation = false;
            int zomeID = 0;
            double idxZomeToMutate = 0.;
            while (!mutation) {
                // TODO: defining different mutation rates per locus in 
                // the same marker for microsats and SNPs
                // for example if we have 3 linked microsats 500 499 502
                // each of the positions should have its own mutation rate
                
                //if we were to check for the largest zome first and order them from biggest to smallest the program could possibly be faster
                idxZomeToMutate += (double) (genObjArray[zomeID].getNbSitesInZome()) * genotypeInf.getMutationRateMales(zomeID);
                
                if (rndZomeToMutate <= idxZomeToMutate) {
                    mutation = true;
                    this.genObjArray[zomeID].mutation();
                }
                zomeID++;
            }
        } else {
            
            totalLength = genotypeInf.getGlobalFemaleMutationNumber();
            double rndZomeToMutate = _ranGenHelper.getUniformGenerator().nextDoubleFromTo(0, totalLength);
            boolean mutation = false;
            int zomeID = 0;
            double idxZomeToMutate = 0.;
            while (!mutation) {
                
                idxZomeToMutate += (double) (genObjArray[zomeID].getNbSitesInZome()) * genotypeInf.getMutationRateFemales(zomeID);
                
                if (rndZomeToMutate <= idxZomeToMutate) {
                    mutation = true;
                    this.genObjArray[zomeID].mutation();
                }
                zomeID++;
            }
        }
    }
    /**
     * 
     * @author Tiago
    */
    static int mutMethodCount=0;
    public void mutationTest(Genotype genotypeInf) {
        double totalLength;
        if (this.isMale()) {
            
            totalLength = genotypeInf.getGlobalMaleMutationNumber();
            double ind = _ranGenHelper.getUniformGenerator().nextDoubleFromTo(0, totalLength);
            boolean mutation = false;
            int i = 0;
            double nbsnp = 0.;
            while (!mutation) {

                if (i < genObjArray.length) {
                    nbsnp += (double) (genObjArray[i].getNbSitesInZome()) * genotypeInf.getMutationRateMales(i);
                    //System.out.println(genotype.length);
                    //System.out.println(genotype[i].getType());
                    //System.out.println("_____");
                    System.out.println("GlobalMaleMutationNumber=totalLength="+totalLength);
                    System.out.println("0~totalLength=ind="+ind);
                    System.out.println("nbsnp("+i+")="+nbsnp);
                    System.out.println("if( "+ind+" < "+nbsnp+" )="+(ind<nbsnp));
                    System.out.println();
                    

                    if (ind < nbsnp) {
                        mutation = true;
                        mutMethodCount++;
                        this.genObjArray[i].mutation();
                        //prints sequence after each mutation
                        /*for (Site seq : this.genotype[i].getZome()[0].getSeq()) {
                            System.out.println(seq.getValue());
                        }*/
                    }
                } else {
                    break;
                }
                i++;
            }
        } else {
            
            totalLength = genotypeInf.getGlobalFemaleMutationNumber();
            double ind = _ranGenHelper.getUniformGenerator().nextDoubleFromTo(0, totalLength);
            boolean mutation = false;
            int i = 0;
            double nbsnp = 0.;
            while (!mutation) {
                
                nbsnp += (double) (genObjArray[i].getNbSitesInZome()) * genotypeInf.getMutationRateFemales(i);
                if (ind < nbsnp) {
                    mutation = true;
                    this.genObjArray[i].mutation();
                }
                i++;
            }
        }
        System.out.println("INDIVIDUAL mutMethodCount = "+mutMethodCount);
        System.out.println();
    }
   
    //method which test thesexe of an individual
    public boolean isMale() {
        /*boolean test = false;
        if (this.genotype[1].getType() == GeneticObject.GeneticObjectType.Y) {
            test = true;
        }
        return test;*/
        return this.genObjArray[1].getGenObjType() == GeneticObject.GeneticObjectType.Y;
    }

    String getGeneReprs(int geneId) {
        //System.out.println("indivudual in deme (row; col): "+ "("+deme_i+"; "+deme_j+ ")");
        //return genotype[geneId].getReprs(generation, deme_i, deme_j , identity, mother, father);
        return genObjArray[geneId].getReprs_NEW(generation, deme_i, deme_j , identity, mother, father);
    }
    
    String getAllGeneReprs(){
        
        
        
        String allgenerep = 
                generation+" "+
                identity.split("_")[2]+" "+//layerId
                deme_i+" "+
                deme_j+" "+
                this._sex+" "+
                identity+" "+
                mother+" "+
                father+" ";
        
        for (int i = 0; i < genObjArray.length; i++) {
            if( i == 0 ){
                if(this.isMale()){
                    allgenerep += genObjArray[i].getOnlyGeneReprs();
                }else{
                    allgenerep += genObjArray[i].getOnlyGeneReprs().trim()+"/"+
                        genObjArray[i+1].getOnlyGeneReprs();
                }
            }else if( i == 1 ){
                if(this.isMale()){
                    allgenerep += genObjArray[i].getOnlyGeneReprs();
                }else{
                    allgenerep += "NA ";
                }
            }else{
                allgenerep += genObjArray[i].getOnlyGeneReprs();
            }
        }
        
//        for (GeneticObject genObj : genObjArray) {
//            allgenerep += genObj.getOnlyGeneReprs();
//        }
        
        return allgenerep;
    }
    
    public String getIndividualID(){
        return this.identity;
    }

    public String getFather() {
        return this.father;
    }

    public String getMother() {
        return this.mother;
    }
}
