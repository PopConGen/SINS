package sins2.helpers.random;

import cern.jet.random.Binomial;
import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import cern.jet.random.Gamma;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomSeedGenerator;


/**
 *
 **/
public class RandGenHelper{
	
	private RandomSeedGenerator _seeding;
	private MersenneTwister64 _randGen;
	private Uniform _unirnd;
	private Poisson _poissrnd;
	private Binomial _binrnd;
        private Gamma _gammarnd;
        
        public RandGenHelper(){
          _seeding=new RandomSeedGenerator();
	  _randGen=new MersenneTwister64(_seeding.nextSeed());
	  _unirnd=new Uniform(_randGen);
	  _poissrnd=new Poisson(0,_randGen);
	  _binrnd=new Binomial(1,0.5,_randGen);
          _gammarnd = new Gamma(0.0419, 488.5, _randGen);
        }

	public void reInit(int n){
		_randGen=new MersenneTwister64(_seeding.nextSeed()*(int)System.nanoTime()*(n+1));
		_unirnd=new Uniform(_randGen);
		_poissrnd=new Poisson(0,_randGen);
		_binrnd=new Binomial(1,0.5,_randGen);
                _gammarnd = new Gamma(0.0419, 488.5, _randGen);
	}
        
        public Uniform getUniformGenerator(){
          return _unirnd;
        }
        
        public Poisson getPoissonGenerator(){
          return _poissrnd;
        }
        
        public Binomial getBinomialGenerator(){
          return _binrnd;
        }
        
	public Gamma getGammaGenerator(){
          return _gammarnd;
        }

}