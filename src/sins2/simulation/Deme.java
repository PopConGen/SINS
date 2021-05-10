/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.simulation;

//import java.math.BigDecimal;
//import java.math.RoundingMode;
import sins2.helpers.io.IWriterHelper;
import java.util.*;
import sins2.exceptions.InputParametersExceptions;
import sins2.helpers.random.RandGenHelper;
import sins2.sampling_class.ISampler;
import sins2.simulation.Individual.IndividualSex;
import sins2.simulation.Layer.MatingSystem;


/**
 * TODO: Check this file because it have a lot of code from the other version of
 * SINS
 *
 * @author douglas
 */
public class Deme {

    //class which represents a deme, in other terms a group of individual being in a population, on a precise cell.
    public ArrayList<Individual> _females;
    public ArrayList<Individual> _males;
    public int _deme_line;
    public int _deme_column;
    public int _generation;
    
    private RandGenHelper _ranGenHelper;

    //initialise a deme with a defined number of males and females.
    public Deme(int f, int m, int line, int column, int time, int population, 
            Genotype initialGenotype,RandGenHelper ranGenHelper) throws InputParametersExceptions {
        _generation = time;
        _deme_line = line;
        _deme_column = column;
        _females = new ArrayList<>(f);
        _males = new ArrayList<>(m);

        _ranGenHelper=ranGenHelper;

        for (int i = 0; i < f; i++) {
            _females.add(new Individual(IndividualSex.FEMALE, true, "F_" + String.valueOf(i)
                    + "_" + String.valueOf(population) + "_" + String.valueOf(this._deme_line) + "_"
                    + String.valueOf(_deme_column) ,
                    this._deme_line, this._deme_column, _generation, initialGenotype,ranGenHelper));
        }
        for (int i = 0; i < m; i++) {
            _males.add(new Individual(IndividualSex.MALE, true, "M_" + String.valueOf(i) + "_"
                    + String.valueOf(population) + "_" + String.valueOf(_deme_line)
                    + "_" + String.valueOf(this._deme_column),
                    _deme_line, this._deme_column, _generation, initialGenotype,ranGenHelper));

        }
    }
        
    //initialise an empty deme
    public Deme(RandGenHelper ranGenHelper,int i,int j) {
        _ranGenHelper=ranGenHelper;
        _females = new ArrayList<>();
        _males = new ArrayList<>();
        _deme_line=i;
        _deme_column=j;
    }
   
    public ArrayList<Individual> getFemales() {
        return this._females;
    }

    public ArrayList<Individual> getMales() {
        return this._males;
    }
    
    /*	
     * Build a new generation on the deme with new number of males and females
     */
    public void nextGeneration(int newsize, int numberOfEffectiveMales, int numberOfEffectiveFemales, int time, double ratio, int layerID, Genotype genotypeInf, MatingSystem matingSystem) throws InputParametersExceptions {

        if ((_females.isEmpty()) || (_males.isEmpty())) {
            //if there are no Males or Females in the current deme, next generation is dead.
            _females.clear();
            _males.clear();
        } else {
            ArrayList<Individual> effectiveFemales = getDominantSexIndividuals(numberOfEffectiveFemales, Individual.IndividualSex.FEMALE);
            ArrayList<Individual> effectiveMales = getDominantSexIndividuals(numberOfEffectiveMales, Individual.IndividualSex.MALE);
            //Creates new generation under the given mating system
            ArrayList<ArrayList<Individual>>  newGeneration = relationshipMating(matingSystem, effectiveFemales, effectiveMales, newsize, time, layerID);
            
            _females.clear();
            _males.clear();
            _females.addAll(newGeneration.get(0));
            _males.addAll(newGeneration.get(1));

        }
        _generation = time;
    }
    
    /**Method that receives males, females and the mating system and produces the new generation under the desired mating system
     * 
     * @param matingSystem
     * @param effectiveFemales
     * @param effectiveMales
     * @param newsize
     * @param time
     * @param layerID
     * @return Returns ArrayList of females and males of the new generation
     * @throws InputParametersExceptions 
     */
    private ArrayList<ArrayList<Individual>> relationshipMating(
            MatingSystem matingSystem, ArrayList<Individual> effectiveFemales, ArrayList<Individual> effectiveMales, 
            int newsize, int time, int layerID) 
            throws InputParametersExceptions {

        int indf;
        int indm;
        int indc;
        int k = 0;

        ArrayList<Individual> newFemales = new ArrayList<>(newsize / 2);
        ArrayList<Individual> newMales = new ArrayList<>(newsize / 2);
        
        ArrayList<ArrayList<Individual>> newGeneration = new ArrayList<>(2);
        //Create an array list of "pairs" which will be our couples
        //key = female; value = male
        ArrayList<AbstractMap.SimpleImmutableEntry<Individual, Individual>> couples = new ArrayList<>();

        switch (matingSystem) {
            case RANDOM:

                while (newsize > 0) {
                    indf = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, effectiveFemales.size() - 1);
                    indm = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, effectiveMales.size() - 1);
                    Individual child = Individual.reproduction(effectiveFemales.get(indf), effectiveMales.get(indm), String.valueOf(k), _deme_line, _deme_column, time, layerID);
                    k++;
                    if (child.isMale()) {
                        newMales.add(child);
                    } else {
                        newFemales.add(child);
                    }
                    newsize--;
                }
                break;

            case MONOGAMY://obligate monogamy

                /*while we have females and males, randomly select 
                a couple and assign it to our array 
                and remove it from their array of origin 
                so that we avoid repetitions (because monogamy).
                key = female; value = male*/
                while(effectiveFemales.size() > 0 && effectiveMales.size() > 0){
                    indf = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, effectiveFemales.size() - 1);
                    indm = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, effectiveMales.size() - 1);
                    
                    couples.add(new AbstractMap.SimpleImmutableEntry<>(effectiveFemales.remove(indf),effectiveMales.remove(indm)));
                }
                //randomly select a couple to have a child
                while (newsize > 0) {
                    
                    indc = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, couples.size() - 1);
                    
                    Individual child = Individual.reproduction(couples.get(indc).getKey(), couples.get(indc).getValue(), String.valueOf(k), _deme_line, _deme_column, time, layerID);
                    
                    k++;
                    if (child.isMale()) {
                        newMales.add(child);
                    } else {
                        newFemales.add(child);
                    }
                    newsize--;
                }
                break;
                
            case SOFTMONOGAMY: //monogamy but the extra individuals in the population that didnt find a mate will find one that is already coupled
                
                //first part same as strict monogamy
                while(effectiveFemales.size() > 0 && effectiveMales.size() > 0){
                    indf = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, effectiveFemales.size() - 1);
                    indm = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, effectiveMales.size() - 1);
                    
                    couples.add(new AbstractMap.SimpleImmutableEntry<>(effectiveFemales.remove(indf),effectiveMales.remove(indm)));
                }
                
                /*
                if we still have individuals after assigning the monogamic couples
                get the rest of the individuals from that sex, grab each one of those individuals
                and randomly assign it to another individuals of the opposing sex already in the couples array
                */
                while (effectiveFemales.size() > 0) {

                    indm = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, couples.size() - 1);
                    indf = effectiveFemales.size() - 1;
                    couples.add(new AbstractMap.SimpleImmutableEntry<>(effectiveFemales.remove(indf), couples.get(indm).getValue()));
                }
                while (effectiveMales.size() > 0) {

                    indm = effectiveMales.size() - 1;
                    indf = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, couples.size() - 1);
                    couples.add(new AbstractMap.SimpleImmutableEntry<>(couples.get(indf).getKey(), effectiveMales.remove(indm)));
                }
                
                for (int i = 0; i < couples.size(); i++) {
                    System.out.println(couples.get(i).getKey().getIndividualID()+ " "+ couples.get(i).getValue().getIndividualID());
                }
                
                //randomly select a couple to have a child
                while (newsize > 0) {
                    
                    indc = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, couples.size() - 1);
                    
                    Individual child = Individual.reproduction(couples.get(indc).getKey(), couples.get(indc).getValue(), String.valueOf(k), _deme_line, _deme_column, time, layerID);
                    
                    k++;
                    if (child.isMale()) {
                        newMales.add(child);
                    } else {
                        newFemales.add(child);
                    }
                    newsize--;
                }
                break;

            case POLYGYNY:
                
                /*for every single female assign it a male at random.
                This will make it so that, by chance, a single male will be assigned several different females,
                thus creating polygyny*/
                for(int indexFemale = 0; indexFemale < effectiveFemales.size(); indexFemale++){
                    
                    indm = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, effectiveMales.size() - 1);
                    couples.add(new AbstractMap.SimpleImmutableEntry<>(effectiveFemales.get(indexFemale),effectiveMales.get(indm)));
                }
                //randomly select a couple to have a child
                while (newsize > 0) {
                    
                    indc = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, couples.size() - 1);
                    
                    Individual child = Individual.reproduction(couples.get(indc).getKey(), couples.get(indc).getValue(), String.valueOf(k), _deme_line, _deme_column, time, layerID);
                    
                    k++;
                    if (child.isMale()) {
                        newMales.add(child);
                    } else {
                        newFemales.add(child);
                    }
                    newsize--;
                }
                break;

            case POLYANDRY:
                
                /*for every single male assign it a female at random.
                This will make it so that, by chance, a single female will be assigned several different males,
                thus creating polyandry*/
                for(int indexMale = 0; indexMale < effectiveMales.size(); indexMale++){
                    
                    indf = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, effectiveFemales.size() - 1);
                    couples.add(new AbstractMap.SimpleImmutableEntry<>(effectiveFemales.get(indf),effectiveMales.get(indexMale)));
                }
                //randomly select a couple to have a child
                while (newsize > 0) {
                    
                    indc = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, couples.size() - 1);
                    
                    Individual child = Individual.reproduction(couples.get(indc).getKey(), couples.get(indc).getValue(), String.valueOf(k), _deme_line, _deme_column, time, layerID);
                    
                    k++;
                    if (child.isMale()) {
                        newMales.add(child);
                    } else {
                        newFemales.add(child);
                    }
                    newsize--;
                }                
                break;
            default:
                throw new AssertionError();
        }

        newGeneration.add(0, newFemales);
        newGeneration.add(1, newMales);
        
        couples.clear();
        
        return newGeneration;
    }

    /**Method that received the number of individuals and their sex and returns the individuals that are able to reproduce (dominant/effective)
     * 
     * @param numberOfEffectiveIndividuals
     * @param isMale
     * @return The individuals that are able to reproduce individuals 
     */
    private ArrayList<Individual> getDominantSexIndividuals(int numberOfEffectiveIndividuals,  Individual.IndividualSex sex) {
        
        ArrayList<Individual> effectiveSexInd = new ArrayList<>(numberOfEffectiveIndividuals);
        int compt = 0;
        if (sex == Individual.IndividualSex.MALE) {
            if (numberOfEffectiveIndividuals >= _males.size()) {
                effectiveSexInd.addAll(_males);
            } else {
                while (compt < numberOfEffectiveIndividuals) {
                    effectiveSexInd.add(
                            _males.remove(
                                    _ranGenHelper.getUniformGenerator().
                                            nextIntFromTo(0, _males.size() - 1)));
                    compt++;
                }
            }
        } else {
            if (numberOfEffectiveIndividuals >= _females.size()) {
                effectiveSexInd.addAll(_females);
            } else {
                while (compt < numberOfEffectiveIndividuals) {
                    effectiveSexInd.add(
                            _females.remove(
                                    _ranGenHelper.getUniformGenerator().
                                            nextIntFromTo(0, _females.size() - 1)));
                    compt++;
                }
            }
        }
        return effectiveSexInd;

    }
    
    
    
    public int getNumberOfMales() {
        return this._males.size();
    }

    public int getNumberOfFemales() {
        return this._females.size();
    }

    public int getNumberOfIndividuals() {
        return this.getNumberOfFemales() + this.getNumberOfMales();
    }

    public void mutation(Genotype genotypeInf) {
        
        _ranGenHelper.getPoissonGenerator().setMean((double) this.getNumberOfFemales() * (double) genotypeInf.getGlobalFemaleMutationNumber());
        int nbMutFemale = _ranGenHelper.getPoissonGenerator().nextInt();
        
        _ranGenHelper.getPoissonGenerator().setMean((double) this.getNumberOfMales() * (double) genotypeInf.getGlobalMaleMutationNumber());
        int nbMutMale = _ranGenHelper.getPoissonGenerator().nextInt();
        while (nbMutFemale > 0) {
            int countf = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, this._females.size() - 1);
            _females.get(countf).mutation(genotypeInf);
            nbMutFemale--;
        }
        while (nbMutMale > 0) {
            int countm = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, this._males.size() - 1);
            _males.get(countm).mutation(genotypeInf);
            nbMutMale--;
        }
        
        /* a more elegant way to code the while loop above, maybe change it in the future after testing
        for(int nbMutMale = _ranGenHelper.getPoissonGenerator().nextInt(); nbMutMale > 0; nbMutMale--){
            int countm = _ranGenHelper.getUniformGenerator().nextIntFromTo(0, this._males.size() - 1);
            _males.get(countm).mutation(genotypeInf);
        }*/
        
    }
    
//**********************    
    /*original method
    public ArrayList<Individual> extractGroup(int size) {//admixture
        ArrayList<Individual> out = new ArrayList<Individual>(size);
        while ((size > 0) && (this._females.size() > 1) && (this._males.size() > 1)) {
            if (_ranGenHelper.getUniformGenerator().nextBoolean()) {
                out.add(this._females.remove(this._females.size() - 1));
            } else {
                out.add(this._males.remove(this._males.size() - 1));
            }
            size--;
            //System.out.println("@Deme.extractGroup size="+size+"\tmales="+this._males.size()+"\tfemale="+this._females.size());
        }
        return out;
    }*/
    
    public ArrayList<Individual> extractGroup(int size) {//admixture
        ArrayList<Individual> out = new ArrayList<Individual>(size);

        int femaleMigrants;
        int maleMigrants;
        
        //use BigDecimal?
        if (randomBoolean()) {
            femaleMigrants = (int) Math.round((double) size / 2);
            maleMigrants = size - femaleMigrants;
        } else {
            maleMigrants = (int) Math.round((double) size / 2);
            femaleMigrants = size - maleMigrants;
        }

        for (int i = 0; i < femaleMigrants; i++) {
            if (this._females.size() > 1) {
                out.add(this._females.remove(this._females.size() - 1));
            }
        }

        for (int i = 0; i < maleMigrants; i++) {
            if (this._males.size() > 1) {
                out.add(this._males.remove(this._males.size() - 1));
            }
        }
        return out;    
    }
    

    public void addGroup(ArrayList<Individual> groupToAdd) {
        while (!groupToAdd.isEmpty()) {
            if (groupToAdd.get(groupToAdd.size() - 1).isMale()) {
                this._males.add(groupToAdd.remove(groupToAdd.size() - 1));
            } else {
                this._females.add(groupToAdd.remove(groupToAdd.size() - 1));
            }
        }
    }

    
    /**
     * Tiago:
     * Method that returns a random boolean from a uniform distribution
     * 
     * @return A randomized boolean value.
     */
    private boolean randomBoolean(){
  
        return _ranGenHelper.getUniformGenerator().nextBoolean();
                
    }
    

        
    /**
     * Original method to extract/distribute migrants from current deme, to future deme.
     * Since it uses a random number generator is significantly slower than the deterministic methods
     * It also has the problem that with the conditions that it has to obey ((size > 0) && (this._females.size() > 1) && (this._males.size() > 1))
     * in extreme cases might create weird pattern like a grid lock where every deme has either only 1 male or 1 female and many individuals
     * of the opposing respectively.
     * 
     * @param size
     * @param sexRatio
     * @return Array with migrants
     * 
     * @deprecated slow - Original Method
     */
    public ArrayList<Individual> extractGroupWithRatio(int size, double sexRatio) {
        
        ArrayList<Individual> out = new ArrayList<Individual>(size);      
        
        while ((size > 0) && (this._females.size() > 1) && (this._males.size() > 1)) {            
            if (_ranGenHelper.getUniformGenerator().nextDouble() < sexRatio) {                
                out.add(this._females.remove(this._females.size() - 1));            
            } else {                
                out.add(this._males.remove(this._males.size() - 1));
            }
            size--;
        }        
        return out;
    }
    
    
    //seems to be just a little bit slower due to the calcs - WeightedMigRate
    /**
     * Tiago:
     * Another way to compute how many migrants of each sex we are going to have
     * With this method we take into account the sex disproportion that might exist in the deme with the migration rate
     * This method was suggested by Lounès
     * 
     * @param size
     * @param sexRatio
     * @return Array with migrants
     */
    public ArrayList<Individual> extractGroupWithRatio_WeightedMigRate(int size, double sexRatio) {

        ArrayList<Individual> out = new ArrayList<Individual>(size);

        int newFemaleMigrants;
        int newMaleMigrants;
        
        //lounes
        double femaleMigrationRate = sexRatio;
        double maleMigrationRate = 1 - sexRatio;

        int numberOfInd = this.getNumberOfIndividuals();
        int numberOfFem = this.getNumberOfFemales();
        int numberOfMale = this.getNumberOfMales();
        double femaleFrequency = (double) numberOfFem / numberOfInd;
        double maleFrequency = (double) numberOfMale / numberOfInd;

        double newFemaleMigrationRate = (femaleMigrationRate * femaleFrequency) / (femaleMigrationRate * femaleFrequency + maleMigrationRate * maleFrequency);
        double newMaleMigrationRate = (maleMigrationRate * maleFrequency) / (femaleMigrationRate * femaleFrequency + maleMigrationRate * maleFrequency);

        
        //newFemaleMigrants = (int) Math.round((double)newFemaleMigrationRate * size);
        //newMaleMigrants = (int) Math.round((double)newMaleMigrationRate * size);
        
        if(randomBoolean()){
        newFemaleMigrants = (int) Math.round((double)newFemaleMigrationRate * size);
        newMaleMigrants = size - newFemaleMigrants;
        //System.out.println("sexRatio");
        }else{
        newMaleMigrants = (int) Math.round((double)newMaleMigrationRate * size);
        newFemaleMigrants = size - newMaleMigrants;
        //System.out.println("1 - sexRatio");
        }
        
        /*System.out.println();
        System.out.println("SIZE: "+size);
        System.out.println("newFemaleMigrationRate: "+newFemaleMigrationRate);
        System.out.println("newMaleMigrationRate: "+newMaleMigrationRate);
        System.out.println("newFemaleMigrants: "+newFemaleMigrants);
        System.out.println("newMaleMigrants: "+newMaleMigrants);
        //if(newFemaleMigrants != newMaleMigrants){System.out.println("@TAG");}
        System.out.println();*/
        
        for (int i = 0; i < newFemaleMigrants; i++) {
            out.add(this._females.remove(this._females.size() - 1));
        }

        for (int i = 0; i < newMaleMigrants; i++) {
            out.add(this._males.remove(this._males.size() - 1));
        }
        return out;
    }
    
    
    
    /**
     * Tiago:
     * Fastest way to compute how many migrants of each sex we should have for the desired direction.
     * Method is COMPLETELY deterministic
     * 
     * @param size
     * @param sexRatio
     * @return Array with migrants
     */
    public ArrayList<Individual> extractGroupWithRatio_FullyDeterministic(int size, double sexRatio) {

        ArrayList<Individual> out = new ArrayList<>(size);
        
        int femaleMigrants;
        int maleMigrants;

        
        if(randomBoolean()){
        femaleMigrants = (int) Math.round((double)size * sexRatio);
        maleMigrants = size - femaleMigrants;
        //System.out.println("sexRatio");
        }else{
        maleMigrants = (int) Math.round((double)size * (1 - sexRatio));
        femaleMigrants = size - maleMigrants;
        //System.out.println("1 - sexRatio");
        }

        /*System.out.println();
        System.out.println("TEST extractGroupWithRatio_FullyDeterministic");
        System.out.println("SIZE: "+size);
        System.out.println();*/

        for (int i = 0; i < femaleMigrants; i++) {
            if (this._females.size() > 1) {
                out.add(this._females.remove(this._females.size() - 1));
            }
        }

        for (int i = 0; i < maleMigrants; i++) {
            if (this._males.size() > 1) {
                out.add(this._males.remove(this._males.size() - 1));
            }
        }
        return out;
    }
    
    /**
     * Tiago:
     * Distributes (previously defined number of) migrants to the intended direction.
     * With this method the sexRatio related to migration is variable and taken from a Binomial(n=numberOfMigrants, p=sexRatio)
     * 
     * @param size
     * @param sexRatio
     * @return Array with migrants
     */
    public ArrayList<Individual> extractGroupWithRatio_VariableSexRatio(int size, double sexRatio) {

        ArrayList<Individual> out = new ArrayList<Individual>(size);

        //sexratio threshold - pick number from binomial(Migrants, sexRatio)
        //this should be deme by deme
        
        int femaleMigrants;
        
        int maleMigrants;

        if (randomBoolean()) {
            if (size == 0) {
                femaleMigrants = 0;
            } else {
                femaleMigrants = _ranGenHelper.getBinomialGenerator().nextInt(size, sexRatio);
            }
            maleMigrants = size - femaleMigrants;
        } else {
            if (size == 0) {
                maleMigrants = 0;
            } else {
                maleMigrants = _ranGenHelper.getBinomialGenerator().nextInt(size, (1-sexRatio));
            }
            femaleMigrants = size - maleMigrants;
        }
        
        
        for (int i = 0; i < femaleMigrants; i++) {
            if (this._females.size() > 1) {
                out.add(this._females.remove(this._females.size() - 1));
            }
        }

        for (int i = 0; i < maleMigrants; i++) {
            if (this._males.size() > 1) {
                out.add(this._males.remove(this._males.size() - 1));
            }
        }
        return out;
    }
    

    /**
     * Old method that receives a sampler, produces a sample and then uses this sample of individuals to print the data to file.
     * Since the sample is actually produced here, the individuals sampled will be different
     * for different chromosomes, even if they are from the same generation.
     * @param outputManager
     * @param _initialGen
     * @param geneId
     * @param sampler 
     */
    void printGeneInf(IWriterHelper outputManager, Genotype _initialGen, int geneId
            , ISampler sampler) {
        
        Iterator<Individual> sample=sampler.getSampled(_deme_line, _deme_column, _males, _females);
        
        while (sample.hasNext()) {   
            Individual sampleIndividual=sample.next();
            if(geneId!=1)//
                outputManager.printLine(geneId,sampleIndividual.getGeneReprs(geneId));
            else{
                //all males have Y 
                if(sampleIndividual.isMale())
                    outputManager.printLine(geneId,sampleIndividual.getGeneReprs(geneId));
                }
            //a female have two X 
            if(geneId==0&&!sampleIndividual.isMale())
                outputManager.printLine(geneId,sampleIndividual.getGeneReprs(1));
            }
  
    }
    
    /**
     * New method that receives a sample of individuals and prints the data to file.
     * Since the sample received is always the same for the same deme (in the same generation),
     * the information of the individuals that is printed to file is the same for all chromosomes.
     * In other words, in a given deme, for a given generation, the same individuals are 
     * sampled and printed to for all chromosomes.
     * 
     * @param outputManager
     * @param _initialGen
     * @param geneId
     * @param sample 
     */
    void printGeneInformation(IWriterHelper outputManager, Genotype _initialGen, int geneId
            , Iterator<Individual> sample) {
        
        //Iterator<Individual> sample=sampler.getSampled(_deme_line, _deme_column, _males, _females);
        
        while (sample.hasNext()) {   
            Individual sampleIndividual=sample.next();
            
            if(geneId != 1)//if anything other than Y
                outputManager.printLine(geneId,sampleIndividual.getGeneReprs(geneId));
            else{
                //all males have Y 
                if(sampleIndividual.isMale())
                    outputManager.printLine(geneId,sampleIndividual.getGeneReprs(geneId));
            }
            //a female have two X 
            if(geneId == 0 && !sampleIndividual.isMale())
                outputManager.printLine(geneId,sampleIndividual.getGeneReprs(1));
        }
  
    }
    
    void printAllGeneInfo(IWriterHelper outputManager, int geneId,
            Iterator<Individual> sample){
        while (sample.hasNext()) {   
            Individual sampleIndividual=sample.next();
            outputManager.printLine(geneId,sampleIndividual.getAllGeneReprs());
        }
    }
    
    void printGeneInfTest(/*IWriterHelper outputManager, */Genotype _initialGen, int geneId, ISampler sampler) {
        
        Iterator<Individual> sample = sampler.getSampled(_deme_line, _deme_column, _males, _females);
        System.out.println("*.*.*.*.*.*.*");
        System.out.println("printGeneInfTest");
        System.out.println("*.*.*.*.*.*.*");
        System.out.println("**row "+ this._deme_line +" **col "+this._deme_column+"\n");
        while (sample.hasNext()) {
            Individual sampleIndividual = sample.next();
            if (geneId != 1)//
            {
                System.out.println(sampleIndividual.getGenotype()[geneId].getGenObjType()+" " + sampleIndividual.getGeneReprs(geneId));
            } else {
                //all males have Y 
                if (sampleIndividual.isMale()) {
                    System.out.println(sampleIndividual.getGenotype()[geneId].getGenObjType()+" " + sampleIndividual.getGeneReprs(geneId));
                }

            }
            //a female have two X 
            if (geneId == 0 && !sampleIndividual.isMale()) {
                System.out.println(sampleIndividual.getGenotype()[geneId].getGenObjType()+" " + sampleIndividual.getGeneReprs(1));
            }
        }

    }
    
    

    void clearDeme() {
        _females.clear();
        _males.clear();
        _generation=0;
    }    
}
