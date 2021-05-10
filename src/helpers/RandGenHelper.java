package helpers;

import cern.jet.random.Binomial;
import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomSeedGenerator;


/**
 *   TODO: modify and check this file. This file was taken from SINS1
 *   TODO:Try to eliminate or simplify static class.This is not a good stuff in a parallel version
 *   This is considered a bad programming practice until it 's obligatory
 * */
public class RandGenHelper{
	
	private RandomSeedGenerator seeding;
	private MersenneTwister64 randGen;
	private Uniform unirnd;
	private Poisson poissrnd;
	private Binomial binrnd;
        
        
        public RandGenHelper(){
          seeding=new RandomSeedGenerator();
	  randGen=new MersenneTwister64(seeding.nextSeed());
	  unirnd=new Uniform(randGen);
	  poissrnd=new Poisson(0,randGen);
	  binrnd=new Binomial(1,0.5,randGen);
        }

	public void reInit(int n){
		randGen=new MersenneTwister64(seeding.nextSeed()*(int)System.nanoTime()*(n+1));
		unirnd=new Uniform(randGen);
		poissrnd=new Poisson(0,randGen);
		binrnd=new Binomial(1,0.5,randGen);
	}
        
        public Uniform getUniformGenerator(){
          return unirnd;
        }
        
        public Poisson getPoissonGenerator(){
          return poissrnd;
        }
        
        public Binomial getBinomialGenerator(){
          return binrnd;
        }
        
	

}