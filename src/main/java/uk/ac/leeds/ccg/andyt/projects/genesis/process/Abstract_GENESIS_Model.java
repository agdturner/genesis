package uk.ac.leeds.ccg.andyt.projects.genesis.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_StaticIO;
import uk.ac.leeds.ccg.andyt.grids.exchange.Grids_ImageExporter;
import uk.ac.leeds.ccg.andyt.projects.genesis.core.GENESIS_AgentCollectionManager;
import uk.ac.leeds.ccg.andyt.projects.genesis.core.GENESIS_Environment;
import uk.ac.leeds.ccg.andyt.projects.genesis.core.GENESIS_ErrorAndExceptionHandler;
import uk.ac.leeds.ccg.andyt.projects.genesis.core.GENESIS_Object;
import uk.ac.leeds.ccg.andyt.projects.genesis.logging.GENESIS_Log;

public abstract class Abstract_GENESIS_Model extends GENESIS_Object implements Serializable {

    /**
     * Used for Logging
     */
    private static final String sourcePackage = Abstract_GENESIS_Model.class.getPackage().getName();
    //public GENESIS_Log _GENESIS_Log;
    //protected transient HashMap<String,GENESIS_AgentCollectionManager> _GENESIS_AgentCollectionManager;
    protected transient GENESIS_AgentCollectionManager _GENESIS_AgentCollectionManager;
    protected File _Input_Parameter_File;
    protected Long _RandomSeed;
    protected Long _InitialRandomSeed;
    protected Long _NextRandomSeed;
    protected Long _RandomSeedIncrement;
    protected Random[] _RandomArray;
    public File _Directory;
    public File _ResultDataDirectory_File;
    public boolean _HandleOutOfMemoryError;
    public Grids_ImageExporter _ImageExporter;
    protected transient ExecutorService executorService;

    protected ExecutorService getExecutorService() {
        if (executorService == null) {
            /*
             * An advantage of using a Single ThreadExecutor is that it has an 
             * unbounded queue.
             */
            //executorService = Executors.newSingleThreadExecutor();
            /*
             * The CachedThreadPool works when the system file hander limit is 
             * not exceeded. When it is, things get problematic.
             */
            //executorService = Executors.newCachedThreadPool();
            int limitOfThreadPool = 100;
            executorService = Executors.newFixedThreadPool(limitOfThreadPool);
            //ThreadFactory tf = Executors.defaultThreadFactory();
            //executorService = Executors.newFixedThreadPool(limitOfThreadPool, tf);
        }
        return executorService;
    }

    public long get_NextRandomSeed() {
        return _NextRandomSeed;
    }

    public void init_RandomArrayMinLength(
            int length,
            long initialSeed,
            long seedIncrement) {
        long seed = initialSeed;
        _RandomSeedIncrement = seedIncrement;
        _InitialRandomSeed = seed;
        _RandomArray = new Random[length];
        for (int i = 0; i < length; i++) {
            _RandomArray[i] = new Random(seed);
            seed += seedIncrement;
        }
        _NextRandomSeed = seed;
    }

    protected Random[] get_RandomArrayMinLength(
            int length) {
        if (_RandomArray == null) {
            init_RandomArrayMinLength(
                    length,
                    0L,
                    1L);
        }
        if (_RandomArray.length < length) {
            Random[] newRandomArray = new Random[length];
            System.arraycopy(_RandomArray, 0, newRandomArray, 0, _RandomArray.length);
            long seed = _NextRandomSeed;
            for (int i = _RandomArray.length; i < length; i++) {
                newRandomArray[i] = new Random(seed);
                seed += _RandomSeedIncrement;
            }
            _NextRandomSeed = seed;
            _RandomArray = newRandomArray;
        }
        return _RandomArray;
    }

    public Random[] get_RandomArray() {
        return _RandomArray;
    }

    public Random[] get_RandomArray(int i) {
        if (i < 0) {
            throw new IllegalArgumentException(
                    "i < 0 in " + this.getClass().getName()
                    + ".get_RandomArray(int)");
        }
        return get_RandomArrayMinLength(i);
    }

    public Random get_Random(int i) {
        Random[] randomArray = get_RandomArrayMinLength(i);
        return randomArray[i];
    }

    public Random get_NextRandom() {
        return get_Random(_RandomArray.length);
    }

    public void init_Environment(
            GENESIS_Environment a_GENESIS_Environment) {
        ge = a_GENESIS_Environment;
        ge._AbstractModel = this;
//        _GENESIS_Environment._AbstractModel._GENESIS_Log = new GENESIS_Log(
//                _GENESIS_Environment,
//                GENESIS_Log.GENESIS_DefaultLogLevel,
//                _GENESIS_Environment._Directory, 
//                GENESIS_Log.GENESIS_DefaultLoggerName);
        ge._GENESIS_AgentEnvironment._GENESIS_Environment = a_GENESIS_Environment;
        _GENESIS_AgentCollectionManager = ge._GENESIS_AgentEnvironment.get_AgentCollectionManager(ge.HandleOutOfMemoryErrorFalse);
        _GENESIS_AgentCollectionManager._GENESIS_Environment = a_GENESIS_Environment;
    }
    
    /**
     * @param directory
     * @param filename
     * @param method A String representing the method from which this was called.
     * @return The File given by directory and filename if it exists or exits 
     * the program with an appropriate message identifying the calling method.
     */
    public File getFileThatExists(
            File directory,
            String filename,
            String method) {
        File result = null;
        try {
            result = Generic_StaticIO.getFileThatExists(
                    directory, filename);
        } catch (FileNotFoundException e) {
            System.err.println(
                    e.getMessage()
                    + this.getClass().getName() + "." + method + ".");
            System.exit(
                    GENESIS_ErrorAndExceptionHandler.ArgsErrorExitStatus);
        }
        return result;
    }

    protected static void log(Level level, String message) {
        getLogger().log(level, message);
    }

    public static Logger getLogger() {
        return GENESIS_Log.logger;
    }
}
