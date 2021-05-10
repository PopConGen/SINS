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
public class RandGen {
	
	private static RandomSeedGenerator Seeding=new RandomSeedGenerator();
	public  static MersenneTwister64 RandGen=new MersenneTwister64(Seeding.nextSeed());
	public static Uniform unirnd=new Uniform(RandGen);
	public static Poisson poissrnd=new Poisson(0,RandGen);
	public static Binomial binrnd=new Binomial(1,0.5,RandGen);

	
	/*
	 * 
	 * @p
	 * */
	public static void reInit(int n){
//		for (int i=0;i<RandGen.nextInt()%System.nanoTime()*(n+1);i++){
//			Seeding.nextSeed();
//		}
		RandGen=new MersenneTwister64(Seeding.nextSeed()*(int)System.nanoTime()*(n+1));
		unirnd=new Uniform(RandGen);
		poissrnd=new Poisson(0,RandGen);
		binrnd=new Binomial(1,0.5,RandGen);

	}
	
	

}