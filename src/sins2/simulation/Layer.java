/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.simulation;

import cern.jet.random.Binomial;
import cern.jet.random.Gamma;
import java.awt.Point;
import java.util.AbstractMap;
import sins2.helpers.io.IWriterHelper;
import sins2.helpers.random.RandGenHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import sins2.exceptions.InputParametersExceptions;
import sins2.input_output.IInputManager;
import sins2.sampling_class.ISampler;

/**
 *
 * @author douglas
 */
public final class Layer {//Outcomment "final" keyword for Unit testing
    
    private double _growthRate;
    private double _shortDistanceRate;
    private double _shortDistanceSexRatio;//add by Rita
    private double _dominantMalePercent;
    private double _dominantFemalePercent;
    private double _ratioSettlers;
    private String _settlers;
    private Layer _layerSettle;
    
    //Tiago implementing LongDistanceDispersal
    public enum LddOptions{NO, LDDKERNEL, METHOD_1}
    
    private double _ldMigrationEventRate;
    private double _ldMigrationRate;
    private double _lddSexRatio;
    private LddOptions _lddOption; /*choose between different 
    ldd implementations [none, lddkernel, method_1, method_2]*/
    
    private double _lddLambda; /*lambda is the proportion of migrants that will 
    migrate over a long distance instead*/
    private boolean _useMeanAsParameter;
    private double _meanOrShape;
    private double _varianceOrScale;
    private double _minAngle;
    private double _maxAngle;
    
    private int _meanPoissonDistance;
    
    //minimum distance of an LDD event
    private final int _minLongDistForLDD = 1;
    //---
    
    public enum MatingSystem{RANDOM, MONOGAMY, SOFTMONOGAMY, POLYGYNY, POLYANDRY}
    //the mating system that will be used by the layer
    private MatingSystem _matingSystem;
    
    private Deme[][] _demes;
    private int _n,_m;

    //migrant distribution per direction
    private int[] migrantDistribution;
    
    //time to start the expansion of the population
    private int _expansionTime;
    
    private String _layerName;
    private int _layerId;
    
    //testing variables
    
    
    //genotype information from the layer
    private final Genotype _initialGen;

    
    private static int[] dirX=new int[]{1,-1,0,0};
    private static int[] dirY=new int[]{0,0,1,-1};
    
    private RandGenHelper _randGenHelper;
    
    public Layer(Genotype genType,String layerName,int layerId
            ,RandGenHelper randGenHelper) {        
        _layerName=layerName;
        _layerId=layerId;
        _initialGen=genType;
        _randGenHelper=randGenHelper;
    }
    
    /*no ldd constructor*/
    public Layer(Genotype genType,String layerName,
            int layerId,RandGenHelper randGenHelper
            ,double growthRate,double shortDistanceRate
            ,double shortDistanceSexRatio,double dominantMalePercent
            ,double dominantFemalePercent,double ratioSettlers
            ,String settlers,int n,int m, LddOptions lddOption, MatingSystem matingSystem
            )
    {
       _layerName=layerName;
       _layerId=layerId;
       _initialGen=genType;
       _randGenHelper=randGenHelper;   
       
       _growthRate=growthRate;
       
       _shortDistanceRate=shortDistanceRate;
       
       _shortDistanceSexRatio=shortDistanceSexRatio;
       
        _dominantMalePercent=dominantMalePercent;        
        
        _dominantFemalePercent=dominantFemalePercent;
                
        _ratioSettlers=ratioSettlers;
        
        
        _settlers=settlers;
        
        _n=n;
        _m=m;
        _demes=new Deme[n][m];
        
        _lddOption = lddOption;
        
        _matingSystem = matingSystem;
        
        //todo: consider if this is neccesary
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                _demes[i][j]=new Deme(_randGenHelper,i,j);
            }
        }
        
        
    }
    
    /*ldd with lddkernel*/
    public Layer(Genotype genType,String layerName,
            int layerId,RandGenHelper randGenHelper
            ,double growthRate,double shortDistanceRate
            ,double shortDistanceSexRatio,double dominantMalePercent
            ,double dominantFemalePercent,double ratioSettlers
            ,String settlers,int n,int m,
            LddOptions lddOption, double lddLambda, boolean useMeanAsParameter,
            double mean_shape, double variance_scale, double lddSexRatio,
            double minAngle, double maxAngle, MatingSystem matingSystem
            )
    {
       _layerName=layerName;
       _layerId=layerId;
       _initialGen=genType;
       _randGenHelper=randGenHelper;   
       
       _growthRate=growthRate;
       
       _shortDistanceRate=shortDistanceRate;
       
       _shortDistanceSexRatio=shortDistanceSexRatio;
       
        _dominantMalePercent=dominantMalePercent;        
        
        _dominantFemalePercent=dominantFemalePercent;
                
        _ratioSettlers=ratioSettlers;
        
        
        _settlers=settlers;
        
        _n=n;
        _m=m;
        _demes=new Deme[n][m];
        
        _lddOption = lddOption;
        _lddLambda = lddLambda;
        _useMeanAsParameter = useMeanAsParameter;
        _meanOrShape = mean_shape;
        _varianceOrScale = variance_scale;
        _minAngle = minAngle;
        _maxAngle = maxAngle;
        _lddSexRatio = lddSexRatio;
        
        _matingSystem = matingSystem;
        
        //todo: consider if this is neccesary
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                _demes[i][j]=new Deme(_randGenHelper,i,j);
            }
        }
        
        
    }   

    /*ldd with method 1 or 2*/
    public Layer(Genotype genType,String layerName,
            int layerId,RandGenHelper randGenHelper
            ,double growthRate,double shortDistanceRate
            ,double shortDistanceSexRatio,double dominantMalePercent
            ,double dominantFemalePercent,double ratioSettlers
            ,String settlers,int n,int m,
            LddOptions lddOption, double ldMigrationEventRate, double ldMigrationRate,
            double lddSexRatio, int meanPoissonDistance,
            double minAngle, double maxAngle, MatingSystem matingSystem
            )
    {
       _layerName=layerName;
       _layerId=layerId;
       _initialGen=genType;
       _randGenHelper=randGenHelper;   
       
       _growthRate=growthRate;
       
       _shortDistanceRate=shortDistanceRate;
       
       _shortDistanceSexRatio=shortDistanceSexRatio;
       
        _dominantMalePercent=dominantMalePercent;        
        
        _dominantFemalePercent=dominantFemalePercent;
                
        _ratioSettlers=ratioSettlers;
        
        
        _settlers=settlers;
        
        _n=n;
        _m=m;
        _demes=new Deme[n][m];
        
        _lddOption = lddOption;
        _ldMigrationEventRate = ldMigrationEventRate;
        _ldMigrationRate = ldMigrationRate;
        _meanPoissonDistance = meanPoissonDistance;
        _minAngle = minAngle;
        _maxAngle = maxAngle;
        _lddSexRatio = lddSexRatio;
        
        //random, monogamy, polygyny, polyandry
        _matingSystem = matingSystem;
        
        //todo: consider if this is neccesary
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                _demes[i][j]=new Deme(_randGenHelper,i,j);
            }
        }
        
        
    } 
 

    public void setExpansionTime(int expansionTime) {
      _expansionTime=expansionTime;
    }
    
    
    /**
     * 
     *  In this method the population is initialized
     */
    void startExpansion(int currentGeneration,IInputManager input) throws InputParametersExceptions{

        int[][] initMatrix=input.readExpansionMatrix(_layerName);
        
        //auxiliar variable used in the next cycle
        int newSize;
        if(_ratioSettlers!=0){//initialize the population from another one
        
            for (int i = 0; i < _n; i++) {
                for (int j = 0; j < _m; j++) {             
                    if (initMatrix[i][j] > 0) {//TODO: check if this condition is necesary
                        int size = _layerSettle._demes[i][j].getNumberOfIndividuals();
                        newSize = (int) (size * _ratioSettlers);
                        ArrayList<Individual> settlers = _layerSettle._demes[i][j].extractGroup(newSize);
                        _demes[i][j] = new Deme(_randGenHelper,i,j);
                        _demes[i][j].addGroup(settlers);
                        _demes[i][j]._generation = currentGeneration;
                        _demes[i][j]._deme_line = i;
                        _demes[i][j]._deme_column = j;
                        
                    }
                }
            }

        }
        else{
             for (int i = 0; i < _n; i++) {
                 for (int j = 0; j < _m; j++) {
                     if (initMatrix[i][j] > 0) {
                         //log("@Layer.startExpansion");
                         _demes[i][j]=new Deme(initMatrix[i][j]/2,(int)initMatrix[i][j]/2,
                                 i,j,currentGeneration, _layerId,_initialGen,
                                 _randGenHelper);
                         
                         //TODO refactor to test easily, starting below
                         //_demes[i][j] = createDeme(initMatrix[i][j]/2, (int)initMatrix[i][j]/2, i, j, currentGeneration);
                     }
                 }
             }
        }
    }
    
    static void checkForStartOfExpansions(IInputManager input, int currentGeneration,Layer[] layers) throws InputParametersExceptions {
        int layerCount=layers.length;
        
        while(layerCount>0){
            layerCount--;
            if (layers[layerCount]._expansionTime == 0) {
               layers[layerCount].startExpansion(currentGeneration, input);
            }
            
            
        }
   
        
    
    }
    
    
    static int migrantDistributionDirection_0=0;
    static int migrantDistributionDirection_1=0;
    static int migrantDistributionDirection_2=0;
    static int migrantDistributionDirection_3=0;
    static double totalNumberOfMigrants=0;
    static int[] validDirectionsArraySum = new int[4];
    static int totalDirectionCount=0;
    static int countUP=0;
    static int countDOWN=0;
    static int countLEFT=0;
    static int countRIGHT=0;
    
    
    Deme[][] doMovements(String pathToInput, int currentGeneration, Environment currentEnvironment) {

        _expansionTime--;

        Deme[][] temp = new Deme[_n][_m];
        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                temp[i][j] = new Deme(_randGenHelper, i, j);
            }
        }

        //try to refactor the next code because is too long, I think that maybe it can be shorter
        //determining the migration board
        //int testdist; //tiago TODO: not being used
        //double[] directionProb = new double[4];
        //double[] migDist = new double[4];
        //int[] migDist = new int[4];
        int numberOfMigrants = 0;
        int possibleDirectionCount;
        //int[] validDirections = new int[4];
        int newX, newY;
        int newX_LDD, newY_LDD;
        
        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
        //        for (int i = _n-1; i >= 0; i--) {
          //  for (int j = _m-1; j >= 0; j--) {
                

                //if not the population die                
                if (_demes[i][j].getNumberOfFemales() > 1 && _demes[i][j].getNumberOfMales() > 1) {

                    double[] directionProb = new double[4];//Tiago: initialize variable at every valid deme
                    //int[] validDirections = new int[4];//Tiago: initialize variable at every valid deme
                    int[] validDirections = new int[]{-99,-99,-99,-99}; //Valid directions will be 0;1;2;3. Initializing them like this allows me to check if some weird stuff is happening there
                    //move in all directions
                    possibleDirectionCount = 0;
                    
                    for (int currDir = 0; currDir < 4; currDir++) {
                        newX = i + dirX[currDir];
                        newY = j + dirY[currDir];
                        
                        //check if the direction is valid
                        if (newX >= 0 && newY >= 0 && newX < _n && newY < _m
                                && currentEnvironment.isPossibleMigrate(_layerId, newX, newY)) {
                            
                            validDirections[possibleDirectionCount] = currDir;
                            // todo: chaekc that the value of currentEnvironment.getFSum(_layerId, newX, newY) should be different than 0
                            
                            directionProb[possibleDirectionCount] = (1 - currentEnvironment.getF(_layerId, newX, newY))
                                    / currentEnvironment.getFSum(_layerId, i, j);//Tiago: Pdir = (1 - Fdir) / (nd - Ft)
                            
                            //log("possibleDirectionCount="+possibleDirectionCount+"\tdirectionProb="+directionProb[possibleDirectionCount]);
                            //log("\n@Layer.doMovements PROBABILITIES");
                            //log("X=" + newX + "\tY=" + newY);
                            //log("Pdir=" + "(1 - " + currentEnvironment.getF(_layerId, newX, newY) + ") / " + currentEnvironment.getFSum(_layerId, i, j) + " = " + (1 - currentEnvironment.getF(_layerId, newX, newY)) / currentEnvironment.getFSum(_layerId, i, j));
                            //log("dirprob[" + possibleDirectionCount + "] = Pdir = " + directionProb[possibleDirectionCount] + "\n");

                            possibleDirectionCount++;/*Tiago: in the arrays above possibleDirectionCount only reaches 3 but actually its
                            final value will be 4 since its incremented in the end of the loop. Pretty confusing...*/
                        }
                    }
                    
                    
                    //log(possibleDirectionCount);
                    //log(validDirections[0]+"\t"+validDirections[1]+"\t"+validDirections[2]+"\t"+validDirections[3]);
                    numberOfMigrants = (int) (_demes[i][j].getNumberOfIndividuals() * _shortDistanceRate * (possibleDirectionCount / 4.0));
                    //log("Nm("+numberOfMigrants+") = "+_demes[i][j].getNumberOfIndividuals()+ " * "+ _shortDistanceRate+" * ("+possibleDirectionCount+" / 4.0)");
                    //compute number of migrants in each direction
                    if(!(_lddOption==LddOptions.LDDKERNEL)){
                        setMigrationDistribution(directionProb, possibleDirectionCount, numberOfMigrants, _randGenHelper.getBinomialGenerator()/*, migDist*/);
                    }else if(_lddOption==LddOptions.LDDKERNEL){
                        int numberOfLDDMigrants = computeLDDMigrants(numberOfMigrants, _lddLambda);
                        numberOfMigrants = numberOfMigrants - numberOfLDDMigrants;

                        setMigrationDistribution(directionProb, possibleDirectionCount, numberOfMigrants, _randGenHelper.getBinomialGenerator()/*, migDist*/);


                        ArrayList<Point> newXY = computeLDDKernel(
                                i/*origin_i*/,
                                j/*origin_j*/,
                                _useMeanAsParameter/*useMeanAsParameter*/,
                                _meanOrShape/*meanOrShape*/,
                                _varianceOrScale/*varianceOrScale*/,
                                _minAngle/*0*//*min angle*/,
                                _maxAngle/*2*Math.PI*//*max angle*/,
                                currentEnvironment);

                        if(!newXY.isEmpty()){
                            newX_LDD = newXY.get(0).x;
                            newY_LDD = newXY.get(0).y;
                            
                            temp[newX_LDD][newY_LDD].addGroup(_demes[i][j].extractGroupWithRatio_FullyDeterministic(numberOfLDDMigrants, _lddSexRatio));

                        /*log(numberOfMigrants+ " "+numberOfLDDMigrants+ " "+_lddLambda+"\n"+
                                "origin:"+i+","+j+"\n"+
                                "destination:"+newX_LDD+","+newY_LDD);*/
                        }/*else{
                            log("DESTINATION NOT FOUND");
                        }

                        for (int ii = 0; ii < _n; ii++) {
                            for (int jj = 0; jj < _m; jj++) {
                                System.out.print(temp[ii][jj].getNumberOfMales() + "/" + temp[ii][jj].getNumberOfFemales()+ "\t");
                            }
                            log("");
                        }*/
                    }
                    
                    //dirX = int[]{1,-1,0,0};
                    //dirY = int[]{0,0,1,-1};
                    //TIAGO TODO - MAKE IT SO THAT THE USER IS ABLE TO CHANGE THE SWITCH                   
                    int userInput = 2;
                    switch (userInput) {
                        case 0:
                            for (int currentDirection = 0; currentDirection < possibleDirectionCount; currentDirection++) {
                                newX = i + dirX[validDirections[currentDirection]];
                                newY = j + dirY[validDirections[currentDirection]];
                                temp[newX][newY].addGroup(_demes[i][j].extractGroupWithRatio(migrantDistribution[currentDirection], _shortDistanceSexRatio));
                            }
                            break;
                        case 1:
                            for (int currentDirection = 0; currentDirection < possibleDirectionCount; currentDirection++) {
                                newX = i + dirX[validDirections[currentDirection]];
                                newY = j + dirY[validDirections[currentDirection]];
                                temp[newX][newY].addGroup(_demes[i][j].extractGroupWithRatio_WeightedMigRate(migrantDistribution[currentDirection], _shortDistanceSexRatio));
                            }
                            break;
                        case 2:
                            for (int currentDirection = 0; currentDirection < possibleDirectionCount; currentDirection++) {
                                newX = i + dirX[validDirections[currentDirection]];
                                newY = j + dirY[validDirections[currentDirection]];//TIAGO TESTING 
                                temp[newX][newY].addGroup(_demes[i][j].extractGroupWithRatio_FullyDeterministic(migrantDistribution[currentDirection], _shortDistanceSexRatio));
                            }
                            break;
                        case 3:
                            for (int currentDirection = 0; currentDirection < possibleDirectionCount; currentDirection++) {
                                newX = i + dirX[validDirections[currentDirection]];
                                newY = j + dirY[validDirections[currentDirection]];
                                temp[newX][newY].addGroup(_demes[i][j].extractGroupWithRatio_VariableSexRatio(migrantDistribution[currentDirection], _shortDistanceSexRatio));
                            }
                            break;
                    }
                }
            }
        }
        return temp;
    }

    
    int computeLDDMigrants(int numberOfMigrants, double probabilityToLDD){
        if(numberOfMigrants == 0){
            return 0;
        }else{
            return _randGenHelper.
                    getBinomialGenerator().
                    nextInt(numberOfMigrants, probabilityToLDD);
        }
    }
    
    /**
     * 
     * @param distParams If _lddOption is METHOD_1 then distParams should only
     * have one parameter, the mean of the poisson distribution,
     * if it is LDDKERNEL then it should have 2 parameter, 
     * alpha and lambda to be fed to the gamma distribution.
     * @return The distance that an LDD event will effectively have given the
     * chosen distribution.
     */
    double getLddDistance(ArrayList<Double> distParams){
        
        double longDistanceEffective = 0;
        if(_lddOption == LddOptions.METHOD_1){
            double poissonDist = distParams.get(0);
            longDistanceEffective = _randGenHelper
                            .getPoissonGenerator()
                            .nextInt(poissonDist);
            while(longDistanceEffective<=_minLongDistForLDD)
            {
                longDistanceEffective = _randGenHelper
                            .getPoissonGenerator()
                            .nextInt(poissonDist);
            }
        }else if(_lddOption == LddOptions.LDDKERNEL){
            double alpha = distParams.get(0);
            double lambda = distParams.get(1);
            longDistanceEffective = _randGenHelper
                    .getGammaGenerator()
                    .nextDouble(alpha, lambda);
            while(longDistanceEffective<=_minLongDistForLDD)
            {
                longDistanceEffective = _randGenHelper
                        .getGammaGenerator()
                        .nextDouble(alpha, lambda);
            }
        }
        
        return longDistanceEffective;
    }
    
    /**
     * Note that whichever pair of angle values are selected and independently 
     * of the order in which they are provided, the colt Uniform distribution generator
     * will always output a number from the smaller value to the larger one.
     * This means that the method itself reorders the parameters in the cases where
     * min>max.
     * 
     * @param sideAngle1 value of angle in radians (user inputs it in degrees, conversion is done a priori)
     * @param sideAngle2 value of angle in radians (user inputs it in degrees, conversion is done a priori)
     * @param origin_i original i position
     * @param origin_j original j position
     * @param distParams distance parameters for distributions has to have 1 or 2
     * parameters depending on the ldd option chosen by the user
     * @param currentEnvironment current layer environment to check to where migrants can happen
     * @return Map(String, ArrayList) with new i and new j positions, effective distance and angle selected
     */
    Map<String, ArrayList> calcNewXYandAngleAndDist(
            double sideAngle1, // Note that this value is already received in radians
            double sideAngle2, // Note that this value is already received in radians
            int origin_i,
            int origin_j,
            ArrayList<Double> distParams,
            Environment currentEnvironment)
    {
        double angleTheta
                = _randGenHelper
                        .getUniformGenerator()
                        .nextDoubleFromTo(sideAngle1, sideAngle2);
        
        double longDistanceEffective = getLddDistance(distParams);
        
        int newX = origin_i - (int) Math.round(
                longDistanceEffective * Math.sin(angleTheta)
        );
        int newY = origin_j + (int) Math.round(
                longDistanceEffective * Math.cos(angleTheta)
        );
        
        int numberOfTries = 0;
        while (!(newX >= 0 && newY >= 0 && newX < _n && newY < _m
                && currentEnvironment.isPossibleMigrate(_layerId, newX, newY))
                && numberOfTries < 100) {

            angleTheta = _randGenHelper
                    .getUniformGenerator()
                    .nextDoubleFromTo(sideAngle1, sideAngle2);

            longDistanceEffective = getLddDistance(distParams);
        
            newX = origin_i - (int) Math.round(
                    longDistanceEffective * Math.sin(angleTheta)
            );
            newY = origin_j + (int) Math.round(
                    longDistanceEffective * Math.cos(angleTheta)
            );
            //log(newX+" "+newY);
            numberOfTries++;
        }
        
        
        /*
        Mapping more than the necessary variables so that it is easier to 
        test later on
        */
        Map<String, ArrayList> lddParams = new ConcurrentHashMap<>(4);
        lddParams.put(
                "newX",
                new ArrayList(Collections.singleton(newX))
        );
        lddParams.put(
                "newY",
                new ArrayList(Collections.singleton(newY))
        );
        lddParams.put(
                "longDistanceEffective",
                new ArrayList(Collections.singleton(longDistanceEffective))
        );
        lddParams.put(
                "angleTheta",
                new ArrayList(Collections.singleton(angleTheta))
        );
        
        return lddParams;
    }
    
    
    
    Deme[][] computeLongDistanceMigration(
            double longDistanceMigrationEventRate,
            double longDistanceMigrationRate,
            double longDistanceMigrationSexRatio,
            int longDistanceInput,
            double angleMin,
            double angleMax,
            Environment currentEnvironment
    ) {

        int newX;
        int newY;
        int numberOfLongDistanceMigrants;

        ArrayList<Double> distParams = new ArrayList<>(1);
        distParams.add((double) longDistanceInput);
        Deme[][] temp = new Deme[_n][_m];

        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                temp[i][j] = new Deme(_randGenHelper, i, j);
            }
        }

        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                //if theres more than 1 female and male (else the population would die)   
                if (_demes[i][j].getNumberOfFemales() > 1
                        && _demes[i][j].getNumberOfMales() > 1
                        && _randGenHelper.getUniformGenerator().nextDouble()
                        < longDistanceMigrationEventRate) {

                    Map<String, ArrayList> lddParams
                            = calcNewXYandAngleAndDist(
                                    angleMin,
                                    angleMax,
                                    i,
                                    j,
                                    distParams,
                                    currentEnvironment
                            );
                    newX = (int) lddParams.get("newX").get(0);
                    newY = (int) lddParams.get("newY").get(0);

                    if (newX >= 0 && newY >= 0 && newX < _n && newY < _m
                            && currentEnvironment.isPossibleMigrate(_layerId, newX, newY)
                            //&& _demes[newX][newY].getNumberOfIndividuals() > 0
                            ) {
                        numberOfLongDistanceMigrants
                                = (int) (_demes[i][j].getNumberOfIndividuals()
                                * longDistanceMigrationRate);
                        temp[newX][newY].
                                addGroup(
                                        _demes[i][j].
                                                extractGroupWithRatio_FullyDeterministic(
                                                        numberOfLongDistanceMigrants,
                                                        longDistanceMigrationSexRatio
                                                )
                                );
                        break;
                    }
                }
            }
        }
        //printGridOnScreen_MaleFemale();
        return temp;
    }
    
    /**
     * Computes dispersal kernel for LDD as in N.Ray and L.Excoffier 2010
     * (Molecular Ecology Resources (2010) 10, 902-914)
     * 
     * @param origin_i
     * @param origin_j
     * @param useMeanAsParameter Note that if this parameter is true then,
     * meanOrShape and varianceOrScale will stand for mean and variance respectively.
     * if false then meanOrShape and varianceOrScale will stand for shape and scale respectively
     * @param meanOrShape will stand for mean or shape depending on the useMeanAsParameter value for a gamma distribution
     * @param varianceOrScale will stand for variance or scale depending on the useMeanAsParameter value for a gamma distribution
     * @param angleMin
     * @param angleMax
     * @param currentEnvironment
     * @return 
     */
    ArrayList<Point> computeLDDKernel(
            int origin_i,
            int origin_j,
            boolean useMeanAsParameter,
            double meanOrShape,
            double varianceOrScale,
            double angleMin,
            double angleMax,
            Environment currentEnvironment
    ) {
        int newX;
        int newY;

        /*
        for a general gamma distribution X 
        with shape parameter k and scale parameter b
        we have:
        mean(X) = bk
        var(X) = b²k
        
        alpha = k
        lambda = 1 / b (also known as the rate parameter)
         */
        double alpha;
        double lambda;
        if (useMeanAsParameter) {
            double mean = meanOrShape;
            double variance = varianceOrScale;
            //see random number generator documentation
            //or https://dst.lbl.gov/ACSSoftware/colt/api/cern/jet/random/Gamma.html
            alpha = mean * mean / variance;
            lambda = 1 / (variance / mean);
        } else {
            double shape = meanOrShape;
            double scale = varianceOrScale;
            alpha = shape;
            lambda = 1 / scale;
        }
        ArrayList<Double> distParams = new ArrayList<>(2);
        distParams.add(alpha);
        distParams.add(lambda);

        ArrayList<Point> newXY = new ArrayList<>();

        //if theres more than 1 female and male (else the population would die)   
        if (_demes[origin_i][origin_j].getNumberOfFemales() > 1
                && _demes[origin_i][origin_j].getNumberOfMales() > 1) {

            Map<String, ArrayList> lddParams
                    = calcNewXYandAngleAndDist(
                            angleMin,
                            angleMax,
                            origin_i,
                            origin_j,
                            distParams,
                            currentEnvironment
                    );
            newX = (int) lddParams.get("newX").get(0);
            newY = (int) lddParams.get("newY").get(0);
            //log("origin:"+origin_i+","+origin_j+" new:"+newX + ","+newY);

            if (newX >= 0 && newY >= 0 && newX < _n && newY < _m
                    && currentEnvironment.isPossibleMigrate(_layerId, newX, newY)
                    //&& longDistanceEffective > _minLongDistForLDD
                    //&& _demes[newX][newY].getNumberOfIndividuals() > 0
                    ) {
                newXY.add(new Point(newX, newY));
            }
        }
        //printGridOnScreen_MaleFemale();
        return newXY;
    }
    
    
    
    static void printInformation(String callingFromMethod) {

        log("@Layer.printInformation()");
        log("Calling from method: " + callingFromMethod);
        log("");
        log("Migrant Distribution");
        log("\t" + ((double) migrantDistributionDirection_1 / totalNumberOfMigrants * 100));
        log(((double) migrantDistributionDirection_3 / totalNumberOfMigrants * 100) + "\t" + "\t" + ((double) migrantDistributionDirection_2 / totalNumberOfMigrants * 100));
        log("\t" + ((double) migrantDistributionDirection_0 / totalNumberOfMigrants * 100));
        log("");

        /*log("Directions");
        log("\t" + (double) validDirectionsArraySum[1] / totalDirectionCount * 100);
        log((double) validDirectionsArraySum[3] / totalDirectionCount * 100 + "\t" + "\t" + (double) validDirectionsArraySum[2] / totalDirectionCount * 100);
        log("\t" + (double) validDirectionsArraySum[0] / totalDirectionCount * 100);
        log("");*/
        
        log("Directions");
        log("\t" + countUP);
        log(countLEFT + "\t" + "\t" + countRIGHT);
        log("\t" + countDOWN);
        log("");

        log("");
        log("@Layer.doAllMovments - variables from Layer.setMigrantDistribution");
        log("0\t1\t2\t3");
        log((double)countDirection_0/countALL + "\t" + (double)countDirection_1/countALL + "\t" + (double)countDirection_2/countALL + "\t" + (double)countDirection_3/countALL);
        log("");
    }
    
    
    /**
     * 
     * Tiago: receives the probability for each direction from deme(ij),
     * the number of valid directions from deme(ij) and the total number of migrants from deme(ij).
     * First chooses a random direction. It does this by choosing a random double from 0 to 3.99(9).
     * If this double is less than 1 then the initial direction will be 0, else if this double is less than
     * 2 then the initial direction will be 1 (and so on and so forth)
     * We then assign the probability to the respective direction and then for every valid direction MINUS ONE.
     * If the probability for the current direction and the number of migrants aren't 0 then
     * we pick from a Binomial((n=number of migrants),(p=probability of migrating in current direction)) the number of
     * migrants that will indeed migrate to the current direction. We then update the number of migrants, direction and probability
     * and repeat the process. Finally the last possible direction will receive the remaining migrants.
     * 
     * 
     * @param directionProb
     * @param validDirectionsCount
     * @param numberOfMigrants
     * @param binornd 
     */
    static int countDirection_0=0;
    static int countDirection_1=0;
    static int countDirection_2=0;
    static int countDirection_3=0;
    static int countALL = 0;
    
    void setMigrationDistribution(double[] directionProb,
            int validDirectionsCount, int numberOfMigrants, Binomial binornd/*, int[] migDist*/) {

        migrantDistribution = new int[4];
        
        //choose the initial directions
        int currentDirection = -1;
        /*double randomInitialDirection = Math.random() * validDirectionsCount;
        
        if (randomInitialDirection < 1) {
            currentDirection = 0;
        } else if (randomInitialDirection < 2) {
            currentDirection = 1;
        } else if (randomInitialDirection < 3) {
            currentDirection = 2;
        } else if (randomInitialDirection < 4) {
            currentDirection = 3;
        }*/
        int randomInitialDirectionInt = _randGenHelper.getUniformGenerator().nextIntFromTo(0, validDirectionsCount-1);	
        currentDirection = randomInitialDirectionInt;
        /*
        if(validDirectionsCount==4) {
            if(currentDirection == 0) {
                countDirection_0++;
                countALL++;
            }else if (currentDirection == 1){
                countDirection_1++;
                countALL++;
            }else if (currentDirection == 2){
                countDirection_2++;
                countALL++;
            }else if (currentDirection == 3){
                countDirection_3++;
                countALL++;
            }
        }
        */
        /*log("Direction 0: "+countDirection_0+"\tDirection 1: "+countDirection_1+"\tDirection 2: "+countDirection_2+"\tDirection 3: "+countDirection_3)        */
        
        double p = 0, searchSpaceProb = 0;//used in the next cycle to represent two opposives events
        //int currentDirection = randomInitialDirection;
        p = directionProb[currentDirection/*randomInitialDirection*/];
        searchSpaceProb = 1 - p;

        for (int i = 0; i < validDirectionsCount - 1/**/; i++) {/*Tiago: from what i understood, here we dont run through all the directions
             because the last direction is always accounted for right after the for-loop*/

            if (p >= 1) {
                p = 0.9999999;
            }
            if (p < 0) {
                p = 0;
            }
            if (searchSpaceProb < 0) {
                searchSpaceProb = 0;
            }

            if (p == 0 || numberOfMigrants == 0) {
                migrantDistribution[currentDirection] = 0;
            } else {
                binornd.setNandP(numberOfMigrants, p);
                migrantDistribution[currentDirection] = binornd.nextInt();
                //log("migrantDistribution["+currentDirection+"]="+migrantDistribution[currentDirection]);
                numberOfMigrants -= migrantDistribution[currentDirection];//update number of migrants - mig                
            }

            //updating p and currDir
            currentDirection = (currentDirection + 1) % validDirectionsCount;
            p = directionProb[currentDirection] / searchSpaceProb;
            searchSpaceProb -= directionProb[currentDirection];

        }
        migrantDistribution[currentDirection] = numberOfMigrants;//the rest of individual should take the last direction in the migration
    }
    
    
    /**
     * Tiago
     * 
     * Used for unit testing
     * 
     * @return migrantDistribution
     */
    public int[] getMigrantDistribution(){
        
        return migrantDistribution;
    }
    
    public boolean hasEmptyDemes(){        
        for (Deme[] _deme : this._demes) {
            for (Deme deme : _deme) {
                if(deme.getNumberOfIndividuals()==0){
                    return true;
                }
            }
        }
        return false;
    }
    
    public Deme[][] doMigration(String pathToInput,int currentGeneration,Environment environment){
         _expansionTime--;
         
         return doMovements(pathToInput, currentGeneration, environment);
    }
    
    /**
     * 
     * 
     * @param env
     * @param allLayers
     * @param migrationInf
     * @param admixture 
     * @param admixtureSexRatio
     * 
     * Sets migrationInf
     * 
     */
    public static void doAdmixture(Environment env, Layer[] allLayers,
            Deme[][][] migrationInf, double[][] admixture, /*TIAGO*/double[][] admixtureSexRatio) {//Admixture=The number of individuals that can migrate between layers
        double numberOfIndividualsInLayer_ii;//N(i,t) numberOfIndividualsInLayer_ii oldsize
        double numberOfIndividualsInLayer_jj;//N(j,t) numberOfIndividualsInLayer_jj tempsize
        for (int i = 0; i < env.getRowSize(); i++) {
            for (int j = 0; j < env.getColumnSize(); j++) {
                for (int k = 0; k < allLayers.length; k++) {
                    if (allLayers[k]._demes[i][j].getNumberOfIndividuals() > 0) {
                        numberOfIndividualsInLayer_ii = allLayers[k]._demes[i][j].getNumberOfIndividuals();//N(i,t)*Y(i,j)*((2*N(i,t)*N(j,t))/((N(i,t)+N(j,t))^2))

                        for (int l = 0; l < allLayers.length; l++) {
                            if (l != k) {
                                numberOfIndividualsInLayer_jj = allLayers[l]._demes[i][j].getNumberOfIndividuals();

                                migrationInf[l][i][j].
                                        addGroup(
                                                allLayers[k].
                                                        _demes[i][j].
                                                        extractGroupWithRatio_FullyDeterministic(
                                                                Math.abs((int)
                                        /*Admixture formula starts HERE*/((numberOfIndividualsInLayer_ii * admixture[k][l]) * ((2. * numberOfIndividualsInLayer_jj * numberOfIndividualsInLayer_ii) / Math.pow((numberOfIndividualsInLayer_jj + numberOfIndividualsInLayer_ii), 2.)))), admixtureSexRatio[k][l]));
                                
                                /*TODO TIAGO can possibly substitute the above line with this one. Does the admixture calc in an independent method. Might need further testing*/
                                //migrationInf[l][i][j].addGroup(allLayers[k]._demes[i][j].extractGroupWithRatio_FullyDeterministic(Math.abs((int)(doAdmixtureCalculation(numberOfIndividualsInLayer_ii,numberOfIndividualsInLayer_jj,admixture[k][l]))), admixtureSexRatio[k][l]));
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Tiago
     * 
     * @param numOfIndInLayer_i
     * @param numOfIndInLayer_j
     * @param admixture
     * @return 
     */
    public static double doAdmixtureCalculation(double numOfIndInLayer_i, double numOfIndInLayer_j, double admixture){
        
        double admixtureMigrants;
        
        admixtureMigrants =
                ((numOfIndInLayer_i * admixture) 
                * ((2. * numOfIndInLayer_j * numOfIndInLayer_i)
                / Math.pow((numOfIndInLayer_j + numOfIndInLayer_i), 2.)));

        return admixtureMigrants;
    }

    
        public static void doAdmixtureWithText(Environment env, Layer[] allLayers,
            Deme[][][] migrationInf, double[][] admixture) {//Admixture=The number of individuals that can migrate between layers
        double numberOfIndividualsInLayer_ii;//N(i,t) numberOfIndividualsInLayer_ii oldsize
        double numberOfIndividualsInLayer_jj;//N(j,t) numberOfIndividualsInLayer_jj tempsize
        for (int i = 0; i < env.getRowSize(); i++) {
            for (int j = 0; j < env.getColumnSize(); j++) {
                for (int k = 0; k < allLayers.length; k++) {
                    if (allLayers[k]._demes[i][j].getNumberOfIndividuals() > 0) {
                        numberOfIndividualsInLayer_ii = allLayers[k]._demes[i][j].getNumberOfIndividuals();//N(i,t)*Y(i,j)*((2*N(i,t)*N(j,t))/((N(i,t)+N(j,t))^2))
                        log("");
                        log("preADMIXTURE numberOfIndividualsInLayer_ii["+k+"]["+i+"]["+j+"]:"+numberOfIndividualsInLayer_ii);
                        for (int l = 0; l < allLayers.length; l++) {
                            if (l != k) {
                                numberOfIndividualsInLayer_jj = allLayers[l]._demes[i][j].getNumberOfIndividuals();
                                log("preADMIXTURE numberOfIndividualsInLayer_jj["+l+"]["+i+"]["+j+"]:"+numberOfIndividualsInLayer_jj);
                                migrationInf[l][i][j].addGroup(allLayers[k]._demes[i][j].extractGroupWithRatio_FullyDeterministic(Math.abs((int)//TIAGO: changed from .extractGroup to .extractGroupWithRatio_FullyDeterministic
                                        /*Admixture formula starts HERE*/((numberOfIndividualsInLayer_ii * admixture[k][l]) * ((2. * numberOfIndividualsInLayer_jj * numberOfIndividualsInLayer_ii) / Math.pow((numberOfIndividualsInLayer_jj + numberOfIndividualsInLayer_ii), 2.)))), 0.5));
                                //migrationInf[l][i][j].addGroup(allLayers[k]._demes[i][j].extractGroupWithRatio_WeightedMigRate(Math.abs((int)//TIAGO: changed from .extractGroup to .extractGroupWithRatio_FullyDeterministic
                                  //      /*Admixture formula starts HERE*/((numberOfIndividualsInLayer_ii * admixture[k][l]) * ((2. * numberOfIndividualsInLayer_jj * numberOfIndividualsInLayer_ii) / Math.pow((numberOfIndividualsInLayer_jj + numberOfIndividualsInLayer_ii), 2.)))), 0.4));
                           
                                log("");
                                log("Deme("+i+","+j+") - Admixture[Layer"+k+"->"+l+"]="+(numberOfIndividualsInLayer_ii * admixture[k][l]) * ((2. * numberOfIndividualsInLayer_jj * numberOfIndividualsInLayer_ii) / Math.pow((numberOfIndividualsInLayer_jj + numberOfIndividualsInLayer_ii), 2.)));
                                log("");
                                log("postADMIXTURE numberOfIndividualsInLayer_ii["+k+"]["+i+"]["+j+"]:"+allLayers[k]._demes[i][j].getNumberOfIndividuals());
                                log("postADMIXTURE numberOfIndividualsInLayer_jj["+l+"]["+i+"]["+j+"]:"+allLayers[l]._demes[i][j].getNumberOfIndividuals());
                                log("");
                            }
                        }
                    }
                }
            }
        }
        
        for (int k = 0; k < migrationInf.length; k++) {
            log("doAdmixture Layer:" + k);
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    System.out.print(migrationInf[k][i][j].getNumberOfIndividuals() + "\t");
                }
                log("");
            }
            log("");
        }


    }
    
    
    
        public static void doAdmixtureTest(Environment env, Layer[] allLayers,
            Deme[][][] migrationInf, double[][] admixture, double admixtureSexRatio) {//Admixture=The number of individuals that can migrate between layers
        double numberOfIndividualsInLayer_ii;//N(i,t) numberOfIndividualsInLayer_ii oldsize
        double numberOfIndividualsInLayer_jj;//N(j,t) numberOfIndividualsInLayer_jj tempsize
        
        //ArrayList<Individual> out = new ArrayList<Individual>(10);
        
        int admixtureMigrantCalculation[][][]= new int[allLayers.length][env.getRowSize()][env.getColumnSize()];
        
        for (int i = 0; i < env.getRowSize(); i++) {
            for (int j = 0; j < env.getColumnSize(); j++) {
                for (int k = 0; k < allLayers.length; k++) {
                    if (allLayers[k]._demes[i][j].getNumberOfIndividuals() > 0) {
                        numberOfIndividualsInLayer_ii = allLayers[k]._demes[i][j].getNumberOfIndividuals();//N(i,t)*Y(i,j)*((2*N(i,t)*N(j,t))/((N(i,t)+N(j,t))^2))
                        log("");
                        log("preADMIXTURE numberOfIndividualsInLayer_ii["+k+"]["+i+"]["+j+"]:"+numberOfIndividualsInLayer_ii);
                        for (int l = 0; l < allLayers.length; l++) {
                            if (l != k) {
                                numberOfIndividualsInLayer_jj = allLayers[l]._demes[i][j].getNumberOfIndividuals();
                                log("preADMIXTURE numberOfIndividualsInLayer_jj["+l+"]["+i+"]["+j+"]:"+numberOfIndividualsInLayer_jj);
                                admixtureMigrantCalculation[l][i][j] = Math.abs((int)((numberOfIndividualsInLayer_ii * admixture[k][l]) * ((2. * numberOfIndividualsInLayer_jj * numberOfIndividualsInLayer_ii) / Math.pow((numberOfIndividualsInLayer_jj + numberOfIndividualsInLayer_ii), 2.))));
                                
                                //migrationInf[l][i][j].addGroup(allLayers[k]._demes[i][j].extractGroupWithRatio_FullyDeterministic(admixtureMigrantCalculation[l][i][j], 0.5));
                                
                                log("");
                                log("Deme("+i+","+j+") - Admixture[Layer"+k+"->"+l+"]="+(numberOfIndividualsInLayer_ii * admixture[k][l]) * ((2. * numberOfIndividualsInLayer_jj * numberOfIndividualsInLayer_ii) / Math.pow((numberOfIndividualsInLayer_jj + numberOfIndividualsInLayer_ii), 2.)));
                                log("Deme("+i+","+j+") - Admixture[Layer"+k+"->"+l+"]="+admixtureMigrantCalculation[l][i][j]);
                                log("");
                                log("postADMIXTURE numberOfIndividualsInLayer_ii["+k+"]["+i+"]["+j+"]:"+allLayers[k]._demes[i][j].getNumberOfIndividuals());
                                log("postADMIXTURE numberOfIndividualsInLayer_jj["+l+"]["+i+"]["+j+"]:"+allLayers[l]._demes[i][j].getNumberOfIndividuals());
                                log("");
                            }
                        }
                    }
                }
            }
        }
        
        for (int i = 0; i < env.getRowSize(); i++) {
            for (int j = 0; j < env.getColumnSize(); j++) {
                for (int k = 0; k < allLayers.length; k++) {
                    if (allLayers[k]._demes[i][j].getNumberOfIndividuals() > 0) {
                        for (int l = 0; l < allLayers.length; l++) {
                            if (l != k) {      
                                migrationInf[l][i][j].addGroup(allLayers[k]._demes[i][j].extractGroupWithRatio_FullyDeterministic(admixtureMigrantCalculation[l][i][j], admixtureSexRatio));
                                
                            }
                        }
                    }
                }
            }
        }
        
        
        for (int k = 0; k < migrationInf.length; k++) {
            log("doAdmixture Layer:" + k);
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    System.out.print("m"+migrationInf[k][i][j].getNumberOfMales()+"/f"+migrationInf[k][i][j].getNumberOfFemales()+ "\t");
                }
                log("");
            }
            log("");
        }


    }

    public LddOptions getLddOption() {
        return _lddOption;
    }
    
       
    static void doAllMovements(String _inputFolderPath, int currentGeneration,
            Environment currentEnvironment, Layer[] layers,double[][] admixture, /*TIAGO*/double[][] admixtureSexRatio)  {
        
        Deme[][][] migrationInf=
                new Deme[layers.length]
                        [currentEnvironment.getRowSize()]
                        [currentEnvironment.getColumnSize()];
        
        Deme[][][] migrationLDInf=
                new Deme[layers.length]
                        [currentEnvironment.getRowSize()]
                        [currentEnvironment.getColumnSize()];
        
        
        for (int i = 0; i < layers.length; i++) {
           migrationInf[i]=layers[i].doMovements(
                   _inputFolderPath,
                   currentGeneration,
                   currentEnvironment
           );
           
           if(layers[i].hasEmptyDemes()){
                if(layers[i]._lddOption == LddOptions.METHOD_1){
                    migrationLDInf[i] = layers[i].computeLongDistanceMigration(
                            layers[i]._ldMigrationEventRate,
                            layers[i]._ldMigrationRate,
                            layers[i]._lddSexRatio,
                            layers[i]._meanPoissonDistance,
                            layers[i]._minAngle,
                            layers[i]._maxAngle,
                            currentEnvironment
                    );
                }/*else if(layers[i]._lddOption == LddOptions.METHOD_2){
                    migrationLDInf[i] = layers[i].doLongDistanceMovements(
                            layers[i]._ldMigrationEventRate,
                            layers[i]._ldMigrationRate,
                            layers[i]._lddSexRatio,
                            layers[i]._meanPoissonDistance,
                            layers[i]._minAngle,
                            layers[i]._maxAngle,
                            currentEnvironment
                    );
                }*/
           }
        }
        

        doAdmixture(currentEnvironment, layers,migrationInf,admixture, /*TIAGO*/admixtureSexRatio);
        //doAdmixtureTest(currentEnvironment, layers,migrationInf,admixture);
                       
        //layers[0].printGridOnScreen_MaleFemale();
        //execute movements
        for (int i = 0; i < layers.length; i++) {
            layers[i].executeMovements(migrationInf[i]);
            
            if (layers[i]._lddOption == LddOptions.METHOD_1)
                layers[i].executeMovements(migrationLDInf[i]);
        }
        //layers[0].printGridOnScreen_MaleFemale();
        //layers[0].printGridOnScreen();
        //printInformation("doAllMovements");
    }
    
    /**
     * Tiago-no Admixture movements
     * 
     * @param _inputFolderPath
     * @param currentGeneration
     * @param currentEnvironment
     * @param layers
     * @param admixture 
     */
    static void doAllMovementsTest(String _inputFolderPath, int currentGeneration,
            Environment currentEnvironment, Layer[] layers/*,double[][] admixture*/)  {
        Deme[][][] migrationInf=
                new Deme[layers.length][currentEnvironment.getRowSize()][currentEnvironment.getColumnSize()];
        
        for (int i = 0; i < layers.length; i++) {
            log("@doALLMovementsTest\ti="+i);
            layers[i].printGridOnScreen();
           migrationInf[i]=layers[i].doMovements(_inputFolderPath,currentGeneration,currentEnvironment);
           
        }
        
        //doAdmixture(currentEnvironment, layers,migrationInf,admixture);
        
        //execute movements
        for (int i = 0; i < layers.length; i++) {
            layers[i].executeMovements(migrationInf[i]);
        }
    }
    
    
    public String getSettlerName(){
      return _settlers;
    }
    
    public double getRatioSettler(){
      return _ratioSettlers;
    }

    public void setLayerSettle(Layer layer) {
        _layerSettle=layer;
    }

    /**
     * Javadoc author: Tiago
     * @param migrants 
     * 
     * Adds the migrants determined in doMovements which were placed in temporary demes to the "original" demes
     */
    private void executeMovements(Deme[][] migrants) {
        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                if (migrants[i][j].getNumberOfIndividuals() > 0) {
                    _demes[i][j].addGroup(migrants[i][j].getMales());
                    _demes[i][j].addGroup(migrants[i][j].getFemales());
                }
            }
        }
    }

    boolean demeContainMaleAndFemale(int row,int column) {
        /*Tiago: checks if a deme contains at least 1 male and 1 female.
        Before was checking for some reason if a deme had at least 2 males and 1 female.
        This was probably a typo and has now been corrected.
        */
        return _demes[row][column].getNumberOfFemales() > 0 && _demes[row][column].getNumberOfMales() > 0;
    }

    int getFemaleCount(int row, int column) {
        return _demes[row][column]._females.size();
    }
    
    int getMaleCount(int row, int column) {
        return _demes[row][column]._males.size();
    }

    /**
     * Tiago
     * @param indexOfRow
     * @param indexOfColumn
     * @return number of individuals on specified deme
     */
    int getIndividualsCount(int indexOfRow, int indexOfColumn){
        return _demes[indexOfRow][indexOfColumn].getNumberOfIndividuals();
    }
    
    
    double getGrowthRate() {
        return _growthRate;
    }

    double getDominantFemalePercent() {
        return _dominantFemalePercent;
    }

    public String getMatingSystem() {
        return _matingSystem.toString();
    }

    public Deme[][] getDemes() {
        return _demes;
    }
    
    public Deme getDeme(int row, int col){
        return _demes[row][col];
    }
    
    
    void nextGeneration(int[][] nextGeneration, int generation) throws InputParametersExceptions {
        double ratio = _dominantFemalePercent / _dominantMalePercent;

        int dominantMales, dominantFemales;

        for (int row = 0; row < _n; row++) {
            for (int column = 0; column < _m; column++) {
                /*if the number of dominant individuals is 0, make it at least 1. 
                @Deme.nextGeneration will check if there is anyone in the demes.
                this way we can make sure that a population with males and females 
                doesnt just die because the percentage of dominant individuals is too small.
                
                Also remember that type casting "(int)" always rounds down to 0 (eg (int)10.8 returns 10)
                so we add 0.5, we get the rounded-to-closest-integer result (eg (int)10.8+0.5(=11.3) returns 11)*/
                dominantMales = (int) (_demes[row][column].getNumberOfMales() * _dominantMalePercent + 0.5);
                if (dominantMales == 0) {dominantMales = 1;}

                dominantFemales = (int) (_demes[row][column].getNumberOfFemales() * _dominantFemalePercent + 0.5);
                if (dominantFemales == 0) {dominantFemales = 1;}

                _demes[row][column].nextGeneration(nextGeneration[row][column],
                        dominantMales, dominantFemales, generation, ratio, _layerId, _initialGen, _matingSystem);
                _demes[row][column].mutation(_initialGen);
            }
        }
    }
    
    
    void nextGenerationParallel(int[][] nextGenerationSize, int generation) throws InputParametersExceptions{
        
        double ratio = _dominantFemalePercent / _dominantMalePercent;

        //final int dominantMales, dominantFemales;
        /*
        Arrays.stream(_demes).parallel();
        
        Stream<Deme[]> asd = Arrays.stream(_demes);
        
        
        //Arrays.parallelSetAll(_demes, nextGeneration(nextGenerationSize, generation));
        
        
        IntStream.range(0, _n)
                .mapToObj(x -> IntStream.range(0, _m)
                        .mapToObj(y -> nextGenerationSize[x][y])
                );
        
        Arrays.stream(_demes).parallel().forEach(pd -> Arrays.stream(pd).forEach(
                
                d -> d.nextGeneration(nextGenerationSize[0][0], dominantMales, dominantFemales, generation, ratio, _layerId, _initialGen, _matingSystem)
                
        ));
        */
        
        
        /*
        Arrays.parallelSetAll(_demes,nextGeneration(IntStream.range(0, _n)
                .mapToDouble(x -> IntStream.range(0, _m)
                        .mapToDouble(y -> nextGenerationSize[x][y])
                )),dominantMales, dominantFemales, generation, ratio, _layerId, _initialGen, _matingSystem);
        
        Arrays.parallelSetAll(nextGenerationSize, _demes.nextGeneration);
        */
        
        //System.out.println("nextGenerationSize[n][m] = " + nextGenerationSize[_n-1][_m-1]);
        
        int myMethod = 2;
        
        if(myMethod == 0){
        //ATTEMPT 1
        AtomicInteger rows = new AtomicInteger();
        
        Arrays.stream(_demes)
                //.parallel()
                .forEach(subArray -> {
            
            int row = rows.getAndIncrement();
            AtomicInteger cols = new AtomicInteger();
            
            Arrays.stream(subArray)
                    //.parallel()
                    //.peek(a -> System.out.println(row+" " +cols.get()+ " "+nextGenerationSize[row][cols.get()]))
                    .forEachOrdered(e -> {
                
                int col = cols.getAndIncrement();                
                //System.out.println("Current Thread: "+Thread.currentThread());
                final int dominantMales = ((int) e.getNumberOfMales() * _dominantMalePercent + 0.5) != 0 ? 
                        (int) (e.getNumberOfMales() * _dominantMalePercent + 0.5) : 1;
                
                final int dominantFemales = ((int) e.getNumberOfFemales() * _dominantFemalePercent + 0.5) != 0 ?
                        (int) (e.getNumberOfFemales() * _dominantFemalePercent + 0.5) : 1;
                //System.out.println(row+"   "+cols);
                try {
                    e.nextGeneration(nextGenerationSize[row][col], dominantMales, dominantFemales, generation, ratio, _layerId, _initialGen, _matingSystem);
                    e.mutation(_initialGen);
                } catch (InputParametersExceptions ex) {
                    Logger.getLogger(Layer.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        
        });
        } else if (myMethod == 1) {

            //ATTEMPT 2
            IntStream.range(0, _n)
                    //.parallel()
                    .forEach(row -> {
                        IntStream.range(0, _m)
                                .parallel()
                                .forEach(column -> {
                                    final int dominantMales = ((int) _demes[row][column].getNumberOfMales() * _dominantMalePercent + 0.5) != 0
                                            ? (int) (_demes[row][column].getNumberOfMales() * _dominantMalePercent + 0.5) : 1;

                                    final int dominantFemales = ((int) _demes[row][column].getNumberOfFemales() * _dominantFemalePercent + 0.5) != 0
                                            ? (int) (_demes[row][column].getNumberOfFemales() * _dominantFemalePercent + 0.5) : 1;
                                    try {
                                        _demes[row][column].nextGeneration(nextGenerationSize[row][column], dominantMales, dominantFemales, generation, ratio, _layerId, _initialGen, _matingSystem);
                                        _demes[row][column].mutation(_initialGen);
                                    } catch (InputParametersExceptions ex) {
                                        Logger.getLogger(Layer.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                });
                    });

        }else if (myMethod == 2) {
            
            //ATTEMPT 3
            
            ExecutorService exec = Executors.newCachedThreadPool();
            
            Future<?> f1 = exec.submit(() -> 
            IntStream.range(0, _n)
                    //.parallel()
                    .forEach(row -> {
                        IntStream.range(0, _m)
                                .parallel()
                                .forEach(column -> {
                                    final int dominantMales = ((int) _demes[row][column].getNumberOfMales() * _dominantMalePercent + 0.5) != 0
                                            ? (int) (_demes[row][column].getNumberOfMales() * _dominantMalePercent + 0.5) : 1;

                                    final int dominantFemales = ((int) _demes[row][column].getNumberOfFemales() * _dominantFemalePercent + 0.5) != 0
                                            ? (int) (_demes[row][column].getNumberOfFemales() * _dominantFemalePercent + 0.5) : 1;
                                    try {
                                        _demes[row][column].nextGeneration(nextGenerationSize[row][column], dominantMales, dominantFemales, generation, ratio, _layerId, _initialGen, _matingSystem);
                                        _demes[row][column].mutation(_initialGen);
                                    } catch (InputParametersExceptions ex) {
                                        Logger.getLogger(Layer.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                });
                    })
            );
            
            try {
                f1.get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(Layer.class.getName()).log(Level.SEVERE, null, ex);
            }
            exec.shutdown();
            
            
        }
        
        

       
        
        
    }
    

    /**
     * @author Tiago
     * 
     * @return Prints to the screen the current layer
     */
    void printGridOnScreen(){
        log("Layer "+this._layerName+" on screen:");
        String printToScreen = "";

        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                printToScreen = printToScreen + _demes[i][j].getNumberOfIndividuals() + "\t";
            }
            printToScreen = printToScreen + "\n";
        }
        log(printToScreen);
    }
    
        /**
     * @author Tiago
     * 
     * @return Prints to the screen the current layer (only male individuals)
     */
    void printGridOnScreen_Male(){
        log("Layer "+this._layerName+" on screen(MALES):");
        String printToScreen = "";

        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                printToScreen = printToScreen + _demes[i][j].getNumberOfMales() + "\t";
            }
            printToScreen = printToScreen + "\n";
        }
        log(printToScreen);
    }
    
    /**
     * @author Tiago
     * 
     * @return Prints to the screen the current layer (only male individuals)
     */
    void printGridOnScreen_MaleFemale(){
        log("Layer "+this._layerName+" on screen(MALES/FEMALES):");
        String printToScreen = "";

        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                printToScreen = printToScreen + _demes[i][j].getNumberOfMales()+"/"+_demes[i][j].getNumberOfFemales() + "\t";
            }
            printToScreen = printToScreen + "\n";
        }
        log(printToScreen);
    }
    
    /**
     * @author Tiago
     * 
     * @return Prints to the screen the current layer (only female individuals)
     */
    void printGridOnScreen_Female(){
        log("Layer "+this._layerName+" on screen(FEMALES):");
        String printToScreen = "";

        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                printToScreen = printToScreen + _demes[i][j].getNumberOfFemales() + "\t";
            }
            printToScreen = printToScreen + "\n";
        }
        log(printToScreen);
    }
    
    
    String printGridToFile(){
        //log("Layer "+this._layerName+" on screen:");
        String printToFile = "";

        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                printToFile = printToFile + _demes[i][j].getNumberOfIndividuals() + "\t";
            }
            printToFile = printToFile + "\n";
        }
        printToFile = printToFile + "\n";
        //log(printToScreen);
        return printToFile;
    }
    
    String printGridToFile_Male(){
        //log("Layer "+this._layerName+" on screen:");
        String printToFile = "";

        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                printToFile = printToFile + _demes[i][j].getNumberOfMales() + "\t";
            }
            printToFile = printToFile + "\n";
        }
        printToFile = printToFile + "\n";
        //log(printToScreen);
        return printToFile;
    }
    
    String printGridToFile_Female(){
        //log("Layer "+this._layerName+" on screen:");
        String printToFile = "";

        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                printToFile = printToFile + _demes[i][j].getNumberOfFemales() + "\t";
            }
            printToFile = printToFile + "\n";
        }
        printToFile = printToFile + "\n";
        //log(printToScreen);
        return printToFile;
    }
    
    
    
    /**
     * @author Tiago
     * 
     * @return _n = row size
     */
    public int getRowSize(){
    
        return _n;
        
    }
    
    /**
     * @author Tiago
     * 
     * @return _m = column size
     */
    public int getColumnSize(){
    
        return _m;
    
    }
        
    void printSize(int currentGeneration,int simulationId, IWriterHelper out) {
        
        int[][] popSize=new int[_n][_m];
        
        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                popSize[i][j]=_demes[i][j].getNumberOfIndividuals();
            }
        }
        out.printSizeMatrix(popSize,currentGeneration,simulationId,_layerName,_layerId);
    }
 
    /**For each (relevant) chromosome, for every position/deme in the layer, if the deme has individuals,
     * print the information for the respective chromosome in the given deme.
     * 
     * With this method, the sampling of individuals takes place at the deme level and is different for
     * all chromosomes. This means that there is the possibility that all the individuals sampled 
     * in all chromosome files are different from each other.
     * 
     * @param currentGeneration
     * @param simulationId
     * @param outputManager
     * @param sampler 
     * @deprecated Old print method, use printInformation instead
     */
    void print(int currentGeneration, int simulationId, IWriterHelper outputManager,
            ISampler sampler) {
        
        String geneName;
        //print data from a simple gene
        for (int geneId = 0; geneId < _initialGen.getNbAutosomes()+3; geneId++) {
            //check if it is necessary print data from that kind of gene
            
            if (_initialGen.isRelevant(geneId)) {
                if(geneId==0)geneName="_X";
                else if(geneId==1)geneName="_Y";
                else if(geneId==2)geneName="_MT";
                else geneName="_A"+(geneId-2)+"";
                geneName=_layerName+geneName;
                outputManager.openRecord(geneName,currentGeneration);
                for (int row = 0; row < _n; row++) {
                    for (int column = 0; column < _m; column++) {
                        if (_demes[row][column].getNumberOfIndividuals() > 0) {
                            _demes[row][column].printGeneInf(outputManager, _initialGen,geneId,sampler);
                        }
                    }
                }
                outputManager.closeCurrentRecord();

            }
        }
        
    }
    
    /**
     * First, for all the positions/demes in the layer, if the deme contains
     * individuals, sample the deme and add the results to an array as well as
     * the "xy" positions where this took place; second, for each (relevant)
     * chromosome and for each saved sample, print the deme information for the
     * respective chromosome in the respective deme "xy".
     *
     * With this method, the sampling of individuals takes place at the layer
     * level and is the same for all chromosomes. This means that in all
     * chromosome files the sampled individuals will be the same (if we choose
     * to just sample and save some individuals instead of all of them)
     *
     *
     * @param currentGeneration
     * @param simulationId
     * @param outputManager
     * @param sampler
     */
    void printInformation(int currentGeneration, int simulationId, IWriterHelper outputManager,
            ISampler sampler) {

        String geneName;

        ArrayList<List<Individual>> allSamples = new ArrayList<>(_m * _n);

        ArrayList<Integer> colSampleIdx = new ArrayList<>(_m);
        ArrayList<Integer> rowSampleIdx = new ArrayList<>(_n);

        /*Pass over layer and check which demes should be sampled, sample them and 
        save the results (list of individuals) to an array along with their 
        respective position*/
        for (int row = 0; row < _n; row++) {
            for (int column = 0; column < _m; column++) {
                if (_demes[row][column].getNumberOfIndividuals() > 0) {
                    sampler.getSampled(
                            row, column,
                            _demes[row][column].getMales(),
                            _demes[row][column].getFemales()
                    );
                    //if we add the Iterator directly the output is empty
                    // there is probably a more elegant way to do this...
                    allSamples.add(sampler.getResults());
                    colSampleIdx.add(column);
                    rowSampleIdx.add(row);
                }
            }
        }
        
        if(outputManager.getWriterType() == IWriterHelper.WriterHelperType.SQL){
            geneName="";
            for (int geneId = 0; geneId < _initialGen.getNbAutosomes() + 3; geneId++) {
                if (geneId == 0) {geneName += "X ";}
                else if (geneId == 1) {geneName += "Y ";}
                else if (geneId == 2) {geneName += "MT ";}
                else {geneName = "A" + (geneId - 2) + " ";}
            }
                        
            outputManager.openRecord(geneName, currentGeneration);
            for (int idx = 0; idx < allSamples.size(); idx++) {
                        _demes[rowSampleIdx.get(idx)][colSampleIdx.get(idx)].
                                printAllGeneInfo(
                                        outputManager, 0,
                                        allSamples.get(idx).iterator()
                                );
            }
            outputManager.closeCurrentRecord();
        }else{
            /*For each relevant gene, for each of the samples saved, print the information
            for their respective demes*/
            for (int geneId = 0; geneId < _initialGen.getNbAutosomes() + 3; geneId++) {
                if (_initialGen.isRelevant(geneId)) {
                    if (geneId == 0) geneName = "_X";
                    else if (geneId == 1)geneName = "_Y";
                    else if (geneId == 2) geneName = "_MT";
                    else geneName = "_A" + (geneId - 2) + "";
                    geneName = _layerName + geneName;
                    outputManager.openRecord(geneName, currentGeneration);
                    for (int idx = 0; idx < allSamples.size(); idx++) {
                        _demes[rowSampleIdx.get(idx)][colSampleIdx.get(idx)].
                                printGeneInformation(
                                        outputManager, _initialGen, geneId,
                                        allSamples.get(idx).iterator()
                                );
                    }
                    outputManager.closeCurrentRecord();
                }
            }
        }
    }
    
    void printOutputToScreen(ISampler sampler) {
        
        String geneName;
        //print data from a simple gene
        for (int geneId = 0; geneId < _initialGen.getNbAutosomes()+3; geneId++) {
            //check if it is necessary print data from that kind of gene
            
            if (_initialGen.isRelevant(geneId)) {
                if(geneId==0)geneName="_X";
                else if(geneId==1)geneName="_Y";
                else if(geneId==2)geneName="_MT";
                else geneName="_A"+(geneId-2)+"";
                geneName=_layerName+geneName;
                //outputManager.openRecord(geneName,currentGeneration);
                for (int row = 0; row < _n; row++) {
                    for (int column = 0; column < _m; column++) {
                        if (_demes[row][column].getNumberOfIndividuals() > 0) {
                            log("row "+row+" col "+column);
                            _demes[row][column].printGeneInfTest(_initialGen,geneId,sampler);
                            //_demes[row][column].;
                        }
                    }
                }
                //outputManager.closeCurrentRecord();

            }
        }
        
    }

    void clearDemes() {
        for (int i = 0; i < _n; i++) {
            for (int j = 0; j < _m; j++) {
                _demes[i][j].clearDeme();
            }
        }
    }

    //PRIVATE
    private static void log(Object aThing){
        System.out.println(aThing);
    }

    public Deme createEmptyDeme(int row, int col){
        return new Deme(_randGenHelper, row, col);
    }

    public Deme createDeme(int numFemales, int numMales,int row, int col, int currentGeneration) throws InputParametersExceptions{
        return new Deme(numFemales,numMales,row,col,currentGeneration, _layerId, _initialGen, _randGenHelper);
    }
   
    
    public final Genotype getGenotypeInformation(){
        return _initialGen;
    }
}
