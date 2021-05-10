package sins2.helpers.parallel;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author douglas
 */
public class Parallel {
    
    //getting the number of threads
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    private static  ExecutorService forPool ;
         

    public static void startPool(){
        forPool = Executors.newFixedThreadPool(NUM_CORES);
    }
    
    public static void stopPool(){
        forPool.shutdown();
    }
    
    public static void For(int upperBound, final Iteration iteration) {
        try {
            // invokeAll blocks for us until all submitted tasks in the call complete
            List< Future > futuresList = new ArrayList< Future >();
            for (int i = 0; i < upperBound; i++) {
                final int iterationCounter = i;
                futuresList.add(forPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        iteration.interation(iterationCounter);
                    }
                }));
                
            }
            
            for(Future f:futuresList)
                f.get();//wait for cycle completion
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static <T,K> K ForDiv(int upperBound, final IterationDiv<T,K> iteration) {
        try {
            // invokeAll blocks for us until all submitted tasks in the call complete
            List< Future<T> > futuresList = new ArrayList< Future<T>  >();
            for (int i = 0; i < upperBound; i++) {
                final int iterationCounter = i;
                futuresList.add(forPool.submit(new Callable<T>() {
                    
                    @Override
                    public T call() throws Exception {
                        return iteration.iteration(iterationCounter);
                    }
                }));
                
            }
            ArrayList<T> partialResults=new ArrayList<T>();
            for(Future<T> f:futuresList)
                partialResults.add(f.get());//wait for cycle completion
            return iteration.getResults(partialResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public static void For(int lowerBound,int upperBound, final Iteration iteration){
       try {
            // invokeAll blocks for us until all submitted tasks in the call complete
            List< Future > futuresList = new ArrayList< Future >();
            for (int i = lowerBound; i < upperBound; i++) {
                final int iterationCounter = i;
                futuresList.add(forPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        iteration.interation(iterationCounter);
                    }
                }));
                
            }
            
            for(Future f:futuresList)
                f.get();//wait for cycle completion
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> void ForEach(final Iterable<T> elements, final Operation<T> operation) {
        try {
            // invokeAll blocks for us until all submitted tasks in the call complete
            forPool.invokeAll(createCallables(elements, operation));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static <T> Collection<Callable<Void>> createCallables(final Iterable<T> elements, final Operation<T> operation) {
        List<Callable<Void>> callables = new LinkedList<Callable<Void>>();
        for (final T elem : elements) {
            callables.add(new Callable<Void>() {
                @Override
                public Void call() {
                    operation.perform(elem);
                    return null;
                }
            });
        }

        return callables;
    }

    public static interface Operation<T> {
        public void perform(T pParameter);
    }
    
    public static interface Iteration {
        public void interation(int i);
    }
    
    public static interface IterationDiv<T,K>{
       public T iteration(int i);
       
       public K getResults(ArrayList<T> results);
       
    }
}
