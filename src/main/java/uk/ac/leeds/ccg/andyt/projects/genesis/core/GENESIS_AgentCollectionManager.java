/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.ccg.andyt.projects.genesis.core;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_IO;
import uk.ac.leeds.ccg.andyt.projects.genesis.logging.GENESIS_Log;

/**
 * Each AgentCollection is assigned an AgentCollectionManager which manages File
 * IO. Each Agent has a reference to at least one AgentCollection and hence at
 * least one AgentCollectionManager. This class contains methods for creating
 * and deleting file stores/caches.
 */
public class GENESIS_AgentCollectionManager
        extends GENESIS_Object
        implements Serializable {

    //static final long serialVersionUID = 1L;
    /**
     * File where all GENESIS_AgentCollections may be written.
     */
    protected File _Directory;
    /**
     * File where all Living GENESIS_Female data may be written.
     */
    protected File _LivingFemaleDirectory;
    /**
     * File where all Living GENESIS_Male data may be written.
     */
    protected File _LivingMaleDirectory;
    /**
     * File where all Dead GENESIS_Female data may be written.
     */
    protected File _DeadFemaleDirectory;
    /**
     * File where all Dead GENESIS_Male data may be written.
     */
    protected File _DeadMaleDirectory;
    /**
     * A HashMap to be held in memory at all times. The keys are
     * GENESIS_FemaleCollection.AgentCollectionID and the values are either the
     * respective GENESIS_FemaleCollection or they are null. In the case of them
     * being null, the data has been swapped to File and can be retrieved. If
     * the key is not there, but it was known to have been, then this indicates
     * that all members of that collection have died. In that case, the dead
     * individuals data can be retrieved via the _DeadFemaleCollection_HashMap
     * or from _DeadFemaleCollection.
     */
    public HashMap<Long, GENESIS_FemaleCollection> LivingFemales;
    /**
     * A HashMap to be held in memory at all times. The keys are
     * GENESIS_MaleCollection.AgentCollectionID and the values are either the
     * respective GENESIS_MaleCollection or they are null. In the case of them
     * being null, the data has been swapped to File and can be retrieved. If
     * the key is not there, but it was known to have been, then this indicates
     * that all members of that collection have died. In that case, the dead
     * individuals data can be retrieved via the _DeadMaleCollection_HashMap or
     * from _DeadMaleCollection.
     */
    public HashMap<Long, GENESIS_MaleCollection> LivingMales;
    /**
     * This is to be held in memory at all times and is for storing dead
     * GENESIS_Females. Once this has reached capacity it will be written to a
     * File and a new one initialised.
     */
    public GENESIS_FemaleCollection _DeadFemaleCollection;
    /**
     * This is to be held in memory at all times and is for storing dead
     * GENESIS_Males. Once this has reached capacity it will be written to a
     * File and a new one initialised.
     */
    public GENESIS_MaleCollection _DeadMaleCollection;
    /**
     * A mapping for storing the location of dead GENESIS_Females. The keys are
     * GENESIS_Female._Agent_ID and the values are the respective
     * _DeadFemaleCollection where the GENESIS_Female is stored.
     */
    public HashMap<Long, Long> _DeadFemaleCollection_HashMap;
    /**
     * A mapping for storing the location of dead GENESIS_Males. The keys are
     * GENESIS_Male._Agent_ID and the values are the respective
     * _DeadMaleCollection where the GENESIS_Male is stored.
     */
    public HashMap<Long, Long> _DeadMaleCollection_HashMap;
    /**
     * This is to be a sensible number of Agents to hold in a single collection.
     * If this number is too small, IO will be slow. Too big and the RAM memory
     * demand could be too high. Hopefully 1000 is a minimum I should work to.
     * Any factor of 10 increase on this is likely to be good. However, loading
     * a large collection just to get at a few agents is likely to be expensive
     * if that collection then has to be cached again writing/storing changes to
     * to the file system...
     */
    public int _MaximumNumberOfAgentsPerAgentCollection;
    /**
     * This is to be a sensible number of files to store in a single file. If
     * this number is too small, the directory depth will be high with possible
     * repercussions for IO. If too big then the file system might get upset and
     * it might be hard to find and order things. A value of 100 seems a good
     * compromise.
     */
    public int MaximumNumberOfObjectsPerDirectory;
    /**
     * The index of the last born GENESIS_Male
     */
    public long IndexOfLastBornMale;
    /**
     * The index of the last born GENESIS_Female
     */
    public long _IndexOfLastBornFemale;
    //public long _IndexOfLastLivingFemaleCollection;

    public long getIndexOfLastLivingFemaleCollection() {
        return getFemaleCollection_ID(
                _IndexOfLastBornFemale,
                GENESIS_Person.getTypeLivingFemale_String(),
                true);
    }

    public long getIndexOfLastLivingMaleCollection() {
        return getMaleCollection_ID(IndexOfLastBornMale,
                GENESIS_Person.getTypeLivingMale_String(),
                true);
    }
    //public long _IndexOfLastLivingMaleCollection;
    public long _NumberOfDeadFemales;
    public long _NumberOfDeadMales;
    public long _LargestIndexOfDeadFemaleCollection;
    public long _LargestIndexOfDeadMaleCollection;

    public GENESIS_AgentCollectionManager() {
        init();
    }

    /**
     * Creates a new AgentCollectionManager based on
     * a_GENESIS_AgentCollectionManager. Because an instance of
     * GENESIS_GENESIS_AgentCollectionManager holds a reference to ge which
     * holds references to all the data in a simulation this does not deep copy
     * everything. Also a_GENESIS_AgentCollectionManager.ge.AgentEnvironment
     * contains a references to a_GENESIS_AgentCollectionManager. Full
     * instantiation would necessarily be a multi stage process that would use
     * dummies to get all the references set up. This method can be implemented
     * more comprehensively as needed, but the depth of the copy wanted is
     * unlikely to be to the root of everything.
     *
     * @param a_GENESIS_AgentCollectionManager The AgentCollectionManager upon
     * which the new instance is based.
     */
    public GENESIS_AgentCollectionManager(
            GENESIS_AgentCollectionManager a_GENESIS_AgentCollectionManager) {
        this(a_GENESIS_AgentCollectionManager.ge,
                a_GENESIS_AgentCollectionManager);
    }

    public GENESIS_AgentCollectionManager(
            GENESIS_Environment ge,
            GENESIS_AgentCollectionManager a_GENESIS_AgentCollectionManager) {
        super(ge);
//        throw new UnsupportedOperationException();
//        // This is not deep copied or set
//        this._DeadFemaleCollection_HashMap = a_GENESIS_AgentCollectionManager._DeadFemaleCollection_HashMap;
//        this._DeadFemaleDirectory = a_GENESIS_AgentCollectionManager._DeadFemaleDirectory;
//        this._DeadMaleCollection = a_GENESIS_AgentCollectionManager._DeadMaleCollection;
//        this._DeadMaleCollection_HashMap = a_GENESIS_AgentCollectionManager._DeadMaleCollection_HashMap;
//        this._DeadMaleDirectory = a_GENESIS_AgentCollectionManager._DeadMaleDirectory;
//        this.Directory = a_GENESIS_AgentCollectionManager._DeadMaleDirectory;
//        this._IndexOfLastBornFemale = a_GENESIS_AgentCollectionManager._IndexOfLastBornFemale;
//        this.IndexOfLastBornMale = a_GENESIS_AgentCollectionManager.IndexOfLastBornMale;
//        this._IndexOfLastLivingFemaleCollection = a_GENESIS_AgentCollectionManager._IndexOfLastLivingFemaleCollection;
//        this._IndexOfLastLivingMaleCollection = a_GENESIS_AgentCollectionManager._IndexOfLastLivingMaleCollection;
//        this._LargestIndexOfDeadFemaleCollection = a_GENESIS_AgentCollectionManager._LargestIndexOfDeadFemaleCollection;
//        this.LivingFemales = a_GENESIS_AgentCollectionManager.LivingFemales;
//        this._LivingFemaleDirectory = a_GENESIS_AgentCollectionManager._LivingFemaleDirectory;
//        this.LivingMales = a_GENESIS_AgentCollectionManager.LivingMales;
//        this._LivingMaleDirectory = a_GENESIS_AgentCollectionManager._LivingMaleDirectory;
//        this._MaximumNumberOfAgentsPerAgentCollection = a_GENESIS_AgentCollectionManager._MaximumNumberOfAgentsPerAgentCollection;
//        this.MaximumNumberOfObjectsPerDirectory = a_GENESIS_AgentCollectionManager.MaximumNumberOfObjectsPerDirectory;
//        this._NumberOfDeadFemales = a_GENESIS_AgentCollectionManager._NumberOfDeadFemales;
//        this._NumberOfDeadMales = a_GENESIS_AgentCollectionManager._NumberOfDeadMales;
    }

    /**
     * <code>this(AgentEnvironment,AgentEnvironment.Directory);</code>
     *
     * @param ge
     */
    public GENESIS_AgentCollectionManager(
            GENESIS_Environment ge) {
        this(ge,
                ge.Directory);
    }

    public GENESIS_AgentCollectionManager(
            GENESIS_Environment ge,
            File directory) {
        super(ge);
        this._Directory = directory;
        init();
    }

    private void init() {
        initLivingMales();
        initLivingFemales();
        IndexOfLastBornMale = -1L;
        _IndexOfLastBornFemale = -1L;
        _NumberOfDeadFemales = 0L;
        _NumberOfDeadMales = 0L;
        _LargestIndexOfDeadFemaleCollection = 0L;
        _LargestIndexOfDeadMaleCollection = 0L;
        //_IndexOfLastFemaleCollection = -1L;
        //_IndexOfLastMaleCollection = -1L;
    }

    /**
     * <code>
     * return new File(Directory.toString());
     * </code>
     *
     * @return a copy of Directory
     */
    public File getDirectory() {
        return new File(_Directory.toString());
    }

    /**
     * <code>
     * this.Directory = a_File;
     * </code>
     *
     * @param a_File
     */
    public void setDirectory(File a_File) {
        this._Directory = a_File;
        a_File.mkdirs();
    }

    /**
     * <code>
     * return new File(_LivingFemaleDirectory.toString());
     * </code>
     *
     * @return a copy of _LivingFemaleDirectory
     */
    public File getLivingFemaleDirectory() {
        return new File(_LivingFemaleDirectory.toString());
    }

    /**
     * <code>
     * this._LivingFemaleDirectory = a_File;
     * </code>
     *
     * @param a_File
     */
    public void setLivingFemaleDirectory(File a_File) {
        this._LivingFemaleDirectory = a_File;
        a_File.mkdirs();
    }

    /**
     * <code>
     * return new File(_LivingMaleDirectory.toString());
     * </code>
     *
     * @return a copy of _LivingMaleDirectory
     */
    public File getLivingMaleDirectory() {
        return new File(_LivingMaleDirectory.toString());
    }

    /**
     * <code>
     * this._LivingMaleDirectory = a_File;
     * </code>
     *
     * @param a_File
     */
    public void setLivingMaleDirectory(File a_File) {
        this._LivingMaleDirectory = a_File;
        a_File.mkdirs();
    }

    /**
     * <code>
     * return new File(_DeadFemaleDirectory.toString());
     * </code>
     *
     * @return a copy of _DeadFemaleDirectory
     */
    public File getDeadFemaleDirectory() {
        return new File(_DeadFemaleDirectory.toString());
    }

    /**
     * <code>
     * this._DeadFemaleDirectory = a_File;
     * </code>
     *
     * @param a_File
     */
    public void setDeadFemaleDirectory(File a_File) {
        this._DeadFemaleDirectory = a_File;
        a_File.mkdirs();
    }

    /**
     * <code>
     * return new File(_DeadMaleDirectory.toString());
     * </code>
     *
     * @return a copy of _DeadMaleDirectory
     */
    public File getDeadMaleDirectory() {
        return new File(_DeadMaleDirectory.toString());
    }

    /**
     * <code>
     * this._DeadMaleDirectory = a_File;
     * </code>
     *
     * @param a_File
     */
    public void setDeadMaleDirectory(File a_File) {
        this._DeadMaleDirectory = a_File;
        a_File.mkdirs();
    }

    /**
     * @return a number representing the ID of the FemaleCollection with the
     * highest ID
     */
    public Long getMaxLivingFemaleCollectionID() {
        return _IndexOfLastBornFemale / _MaximumNumberOfAgentsPerAgentCollection;
    }

    /**
     * @return a number representing the ID of the MaleCollection with the
     * highest ID
     */
    public Long getMaxLivingMaleCollectionID() {
        return IndexOfLastBornMale / _MaximumNumberOfAgentsPerAgentCollection;
    }

    /**
     * @return a number representing the ID of the FemaleCollection with the
     * highest ID
     */
    public Long getMaxDeadFemaleCollectionID() {
        return Generic_IO.getArchiveHighestLeaf(
                getDeadFemaleDirectory(),
                "_");
    }

    /**
     * @return a number representing the ID of the MaleCollection with the
     * highest ID
     */
    public Long getMaxDeadMaleCollectionID() {
        return Generic_IO.getArchiveHighestLeaf(
                getDeadMaleDirectory(),
                "_");
    }

    public Long addToDeadFemaleCollection(
            Long a_GENESIS_Female_AgentID,
            GENESIS_Female a_GENESIS_Female) {
        if (_DeadFemaleCollection.AgentID_Agent_Map.size()
                == _MaximumNumberOfAgentsPerAgentCollection) {
            _DeadFemaleCollection.write();
            //@TODO Set _DeadFemaleCollection to null?
            String type = GENESIS_Person.getTypeDeadFemale_String();
            _LargestIndexOfDeadFemaleCollection++;
            Generic_IO.addToArchive(getDeadFemaleDirectory(),
                    MaximumNumberOfObjectsPerDirectory,
                    _LargestIndexOfDeadFemaleCollection);
            _DeadFemaleCollection = new GENESIS_FemaleCollection(
                    ge,
                    _LargestIndexOfDeadFemaleCollection,
                    type);
        }
        _DeadFemaleCollection.AgentID_Agent_Map.put(
                a_GENESIS_Female_AgentID,
                a_GENESIS_Female);
        return _DeadFemaleCollection.getAgentCollectionID();
    }

    public Long addToDeadMaleCollection(
            Long a_GENESIS_Male_AgentID,
            GENESIS_Male a_GENESIS_Male) {
        if (_DeadMaleCollection.AgentID_Agent_Map.size()
                == _MaximumNumberOfAgentsPerAgentCollection) {
            _DeadMaleCollection.write();
            //@TODO Set _DeadMaleCollection to null?
            String type = GENESIS_Person.getTypeDeadMale_String();
            _LargestIndexOfDeadMaleCollection++;
            Generic_IO.addToArchive(getDeadMaleDirectory(),
                    MaximumNumberOfObjectsPerDirectory,
                    _LargestIndexOfDeadMaleCollection);
            _DeadMaleCollection = new GENESIS_MaleCollection(
                    ge,
                    _LargestIndexOfDeadMaleCollection,
                    type);
        }
        _DeadMaleCollection.AgentID_Agent_Map.put(
                a_GENESIS_Male_AgentID,
                a_GENESIS_Male);
        return _DeadMaleCollection.getAgentCollectionID();
    }

    protected File getFemaleCollectionDirectory(
            Long a_Agent_ID,
            String type) {
        File result;
        if (type.equalsIgnoreCase(GENESIS_Person.getTypeLivingFemale_String())) {
            //long maxLivingFemaleCollectionID = getMaxLivingFemaleCollectionID();
            long maxLivingFemaleCollectionID = getMaxLivingFemaleCollectionID();
            if (_IndexOfLastBornFemale < 0) {
                maxLivingFemaleCollectionID = Generic_IO.getArchiveHighestLeaf(
                        getLivingFemaleDirectory(), "_");
            }
            result = new File(
                    Generic_IO.getObjectDir(getLivingFemaleDirectory(),
                            a_Agent_ID,
                            maxLivingFemaleCollectionID,//getMaxLivingFemaleCollectionID(),
                            MaximumNumberOfObjectsPerDirectory),
                    "" + a_Agent_ID);
        } else {
            result = new File(
                    Generic_IO.getObjectDir(getDeadFemaleDirectory(),
                            a_Agent_ID,
                            getMaxDeadFemaleCollectionID(),
                            MaximumNumberOfObjectsPerDirectory),
                    "" + a_Agent_ID);
        }
        return result;
    }

    protected File getMaleCollectionDirectory(
            Long a_Agent_ID,
            String type) {
        File result;
        if (type.equalsIgnoreCase(GENESIS_Person.getTypeLivingMale_String())) {
            //long maxLivingMaleCollectionID = getMaxLivingMaleCollectionID();
            long maxLivingMaleCollectionID = getMaxLivingMaleCollectionID();
            if (IndexOfLastBornMale < 0) {
                maxLivingMaleCollectionID = Generic_IO.getArchiveHighestLeaf(
                        getLivingMaleDirectory(), "_");
            }
            result = new File(
                    Generic_IO.getObjectDir(getLivingMaleDirectory(),
                            a_Agent_ID,
                            maxLivingMaleCollectionID,//getMaxLivingMaleCollectionID(),
                            MaximumNumberOfObjectsPerDirectory),
                    "" + a_Agent_ID);
        } else {
            result = new File(
                    Generic_IO.getObjectDir(getDeadMaleDirectory(),
                            a_Agent_ID,
                            getMaxDeadMaleCollectionID(),
                            MaximumNumberOfObjectsPerDirectory),
                    "" + a_Agent_ID);
        }
        return result;
    }

    /**
     * @param hoome
     * @return
     * @see get_NextFemaleID()
     */
    public Long get_NextFemaleID(
            boolean hoome) {
        try {
            Long result = get_NextFemaleID();
            /*
             * To use:
             * ge.AgentEnvironment.checkAndMaybeFreeMemory(hoome);
             * It's success needs to be assessed and appropriate action
             * performed to prevent a loop.
             */
            ge.AgentEnvironment.checkAndMaybeFreeMemory();
            return result;
        } catch (OutOfMemoryError e) {
            if (hoome) {
                ge.clearMemoryReserve();
                if (swapToFile_AgentCollection_Account(ge.HOOMEF) < 1) {
                    throw e;
                }
                ge.initMemoryReserve();
                return get_NextFemaleID(
                        hoome);
            } else {
                throw e;
            }
        }
    }

    /**
     * @return The Agent_ID of the next Female to be born.
     */
    protected Long get_NextFemaleID() {
        long result = _IndexOfLastBornFemale + 1;
        return result;
    }

    /**
     * @param a_GENESIS_FemaleCollection AgentCollection not to be swapped if an
     * OutOfMemoryError is encountered.
     * @param handleOutOfMemoryError
     * @return
     * @see get_NextFemaleID()
     */
    public Long get_NextFemaleID(
            GENESIS_FemaleCollection a_GENESIS_FemaleCollection,
            boolean handleOutOfMemoryError) {
        try {
            Long result = get_NextFemaleID();
            ge.tryToEnsureThereIsEnoughMemoryToContinue(
                    a_GENESIS_FemaleCollection,
                    handleOutOfMemoryError);
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                if (swapToFile_FemaleCollectionExcept_Account(
                        a_GENESIS_FemaleCollection) < 1) {
                    throw a_OutOfMemoryError;
                }
                ge.initMemoryReserve(
                        a_GENESIS_FemaleCollection,
                        handleOutOfMemoryError);
                return get_NextFemaleID(
                        a_GENESIS_FemaleCollection,
                        handleOutOfMemoryError);
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * @param hoome
     * @return
     * @see get_NextMaleID()
     */
    public Long get_NextMaleID(
            boolean hoome) {
        try {
            Long result = get_NextMaleID();
            /*
             * To use:
             * ge.AgentEnvironment.checkAndMaybeFreeMemory(hoome);
             * It's success needs to be assessed and appropriate action
             * performed to prevent a loop.
             */
            ge.AgentEnvironment.checkAndMaybeFreeMemory();
            return result;
        } catch (OutOfMemoryError e) {
            if (hoome) {
                ge.clearMemoryReserve();
                if (swapToFile_AgentCollection_Account(ge.HOOMEF) < 1) {
                    throw e;
                }
                ge.initMemoryReserve();
                return get_NextMaleID(                        hoome);
            } else {
                throw e;
            }
        }
    }

    /**
     * @return The Agent_ID of the next GENESIS_Male to be born.
     */
    protected Long get_NextMaleID() {
        long result = IndexOfLastBornMale + 1;
        return result;
    }

    /**
     * @param a_GENESIS_MaleCollection AgentCollection not to be swapped if an
     * OutOfMemoryError is encountered.
     * @param handleOutOfMemoryError
     * @return
     * @see get_NextMaleID()
     */
    public Long get_NextMaleID(
            GENESIS_MaleCollection a_GENESIS_MaleCollection,
            boolean handleOutOfMemoryError) {
        try {
            Long result = get_NextMaleID();
            ge.tryToEnsureThereIsEnoughMemoryToContinue(
                    a_GENESIS_MaleCollection,
                    handleOutOfMemoryError);
            return result;
        } catch (OutOfMemoryError e) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                if (swapToFile_MaleCollectionExcept_Account(
                        a_GENESIS_MaleCollection) < 1) {
                    throw e;
                }
                ge.initMemoryReserve(
                        a_GENESIS_MaleCollection,
                        handleOutOfMemoryError);
                return get_NextMaleID(
                        a_GENESIS_MaleCollection,
                        handleOutOfMemoryError);
            } else {
                throw e;
            }
        }
    }

//    /**
//     * @param a_GENESIS_AgentCollection AgentCollection not to be swapped if an
//     * OutOfMemoryError is encountered.
//     */
//    public Long getAndIncrement_NextFemaleID(
//            GENESIS_AgentCollection a_GENESIS_AgentCollection,
//            boolean handleOutOfMemoryError) {
//        try {
//            Long result = get_NextFemaleID();
//            ge.checkAndMaybeFreeMemory(
//                    a_GENESIS_AgentCollection,
//                    handleOutOfMemoryError);
//            increment_NextAgentID(handleOutOfMemoryError);
//            return result;
//        } catch (OutOfMemoryError a_OutOfMemoryError) {
//            if (handleOutOfMemoryError) {
//                ge.clearMemoryReserve();
//                if (swapToFile_AgentCollectionExcept_Account(
//                        a_GENESIS_AgentCollection) < 1) {
//                    throw a_OutOfMemoryError;
//                }
//                ge.initMemoryReserve(
//                        a_GENESIS_AgentCollection,
//                        ge.HOOMEF);
//                return getAndIncrement_NextFemaleID(
//                        a_GENESIS_AgentCollection,
//                        handleOutOfMemoryError);
//            } else {
//                throw a_OutOfMemoryError;
//            }
//        }
//    }
//
//    public Long getAndIncrement_NextFemaleID(boolean handleOutOfMemoryError) {
//        try {
//            Long result = get_NextFemaleID();
//            /*
//             * To use:
//             * ge.AgentEnvironment.checkAndMaybeFreeMemory(handleOutOfMemoryError);
//             * It's success needs to be assessed and appropriate action
//             * performed to prevent a loop.
//             */
//            ge.AgentEnvironment.checkAndMaybeFreeMemory();
//            increment_NextAgentID(handleOutOfMemoryError);
//            return result;
//        } catch (OutOfMemoryError a_OutOfMemoryError) {
//            if (handleOutOfMemoryError) {
//                ge.clearMemoryReserve();
//                if (swapAgentCollection_Account(
//                        ge.HOOMEF) < 1) {
//                    throw a_OutOfMemoryError;
//                }
//                ge.initMemoryReserve(
//                        handleOutOfMemoryError);
//                return getAndIncrement_NextFemaleID(
//                        handleOutOfMemoryError);
//            } else {
//                throw a_OutOfMemoryError;
//            }
//        }
//    }
//
//    /**
//     * @param a_GENESIS_AgentCollection AgentCollection not to be swapped if an
//     * OutOfMemoryError is encountered.
//     */
//    public Long getAndIncrement_NextMaleID(
//            GENESIS_AgentCollection a_GENESIS_AgentCollection,
//            boolean handleOutOfMemoryError) {
//        try {
//            Long result = get_NextMaleID();
//            ge.checkAndMaybeFreeMemory(
//                    a_GENESIS_AgentCollection,
//                    handleOutOfMemoryError);
//            increment_NextAgentID(handleOutOfMemoryError);
//            return result;
//        } catch (OutOfMemoryError a_OutOfMemoryError) {
//            if (handleOutOfMemoryError) {
//                ge.clearMemoryReserve();
//                if (swapToFile_AgentCollectionExcept_Account(
//                        a_GENESIS_AgentCollection) < 1) {
//                    throw a_OutOfMemoryError;
//                }
//                ge.initMemoryReserve(
//                        a_GENESIS_AgentCollection,
//                        ge.HOOMEF);
//                return getAndIncrement_NextMaleID(
//                        a_GENESIS_AgentCollection,
//                        handleOutOfMemoryError);
//            } else {
//                throw a_OutOfMemoryError;
//            }
//        }
//    }
//
//    public Long getAndIncrement_NextMaleID(boolean handleOutOfMemoryError) {
//        try {
//            Long result = get_NextMaleID();
//            /*
//             * To use:
//             * ge.AgentEnvironment.checkAndMaybeFreeMemory(handleOutOfMemoryError);
//             * It's success needs to be assessed and appropriate action
//             * performed to prevent a loop.
//             */
//            ge.AgentEnvironment.checkAndMaybeFreeMemory();
//            increment_NextAgentID(handleOutOfMemoryError);
//            return result;
//        } catch (OutOfMemoryError a_OutOfMemoryError) {
//            if (handleOutOfMemoryError) {
//                ge.clearMemoryReserve();
//                if (swapAgentCollection_Account(
//                        ge.HOOMEF) < 1) {
//                    throw a_OutOfMemoryError;
//                }
//                ge.initMemoryReserve(
//                        handleOutOfMemoryError);
//                return getAndIncrement_NextMaleID(
//                        handleOutOfMemoryError);
//            } else {
//                throw a_OutOfMemoryError;
//            }
//        }
//    }
//
//    /**
//     *
//     * @param a_GENESIS_AgentCollection AgentCollection not to be swapped if an
//     * OutOfMemoryError is encountered.
//     * @param handleOutOfMemoryError
//     * @return
//     */
//    public void increment_NextAgentID(
//            GENESIS_AgentCollection a_GENESIS_AgentCollection,
//            boolean handleOutOfMemoryError) {
//        try {
//            Long a_NextAgentID = get_NextAgentID();
//            a_NextAgentID++;
//            ge.checkAndMaybeFreeMemory(
//                    a_GENESIS_AgentCollection,
//                    handleOutOfMemoryError);
//            _NextAgentID = a_NextAgentID;
//        } catch (OutOfMemoryError a_OutOfMemoryError) {
//            if (handleOutOfMemoryError) {
//                ge.clearMemoryReserve();
//                if (swapToFile_AgentCollectionExcept_Account(
//                        a_GENESIS_AgentCollection) < 1) {
//                    throw a_OutOfMemoryError;
//                }
//                ge.initMemoryReserve(
//                        a_GENESIS_AgentCollection,
//                        ge.HOOMEF);
//                increment_NextAgentID(
//                        a_GENESIS_AgentCollection,
//                        handleOutOfMemoryError);
//            } else {
//                throw a_OutOfMemoryError;
//            }
//        }
//    }
//
//    public void increment_NextAgentID(boolean handleOutOfMemoryError) {
//        try {
//            Long a_NextAgentID = get_NextAgentID();
//            a_NextAgentID++;
//            /*
//             * To use:
//             * ge.AgentEnvironment.checkAndMaybeFreeMemory(handleOutOfMemoryError);
//             * It's success needs to be assessed and appropriate action
//             * performed to prevent a loop.
//             */
//            ge.AgentEnvironment.checkAndMaybeFreeMemory();
//            _NextAgentID = a_NextAgentID;
//        } catch (OutOfMemoryError a_OutOfMemoryError) {
//            if (handleOutOfMemoryError) {
//                ge.clearMemoryReserve();
//                if (swapAgentCollection_Account(
//                        ge.HOOMEF) < 1) {
//                    throw a_OutOfMemoryError;
//                }
//                ge.initMemoryReserve(
//                        ge.HOOMEF);
//                increment_NextAgentID(handleOutOfMemoryError);
//            } else {
//                throw a_OutOfMemoryError;
//            }
//        }
//    }
////
////    /**
////     *
////     * @param a_GENESIS_AgentCollection AgentCollection not to be swapped if an
////     * OutOfMemoryError is encountered.
////     * @param handleOutOfMemoryError
////     * @return
////     */
////    public void increment_NextAgentID(
////            GENESIS_AgentCollection a_GENESIS_AgentCollection,
////            boolean handleOutOfMemoryError) {
////        try {
////            Long a_NextAgentID = get_NextAgentID();
////            a_NextAgentID++;
////            ge.checkAndMaybeFreeMemory(
////                    a_GENESIS_AgentCollection,
////                    handleOutOfMemoryError);
////            _NextAgentID = a_NextAgentID;
////        } catch (OutOfMemoryError a_OutOfMemoryError) {
////            if (handleOutOfMemoryError) {
////                ge.clearMemoryReserve();
////                if (swapToFile_AgentCollectionExcept_Account(
////                        a_GENESIS_AgentCollection) < 1) {
////                    throw a_OutOfMemoryError;
////                }
////                ge.initMemoryReserve(
////                        a_GENESIS_AgentCollection,
////                        ge.HOOMEF);
////                increment_NextAgentID(
////                        a_GENESIS_AgentCollection,
////                        handleOutOfMemoryError);
////            } else {
////                throw a_OutOfMemoryError;
////            }
////        }
////    }
////
////    public void increment_NextAgentID(boolean handleOutOfMemoryError) {
////        try {
////            Long a_NextAgentID = get_NextAgentID();
////            a_NextAgentID++;
////            /*
////             * To use:
////             * ge.AgentEnvironment.checkAndMaybeFreeMemory(handleOutOfMemoryError);
////             * It's success needs to be assessed and appropriate action
////             * performed to prevent a loop.
////             */
////            ge.AgentEnvironment.checkAndMaybeFreeMemory();
////            _NextAgentID = a_NextAgentID;
////        } catch (OutOfMemoryError a_OutOfMemoryError) {
////            if (handleOutOfMemoryError) {
////                ge.clearMemoryReserve();
////                if (swapAgentCollection_Account(
////                        ge.HOOMEF) < 1) {
////                    throw a_OutOfMemoryError;
////                }
////                ge.initMemoryReserve(
////                        ge.HOOMEF);
////                increment_NextAgentID(handleOutOfMemoryError);
////            } else {
////                throw a_OutOfMemoryError;
////            }
////        }
////    }
    public GENESIS_Male getMale(
            Long a_Agent_ID,
            String type,
            boolean handleOutOfMemoryError) {
        try {
            GENESIS_Male result = null;
            if (type.equalsIgnoreCase(GENESIS_Person.getTypeLivingMale_String())) {
                Long a_AgentCollection_ID = getMaleCollection_ID(
                        a_Agent_ID,
                        type,
                        handleOutOfMemoryError);
                GENESIS_MaleCollection a_GENESIS_MaleCollection = getMaleCollection(
                        a_AgentCollection_ID,
                        type,
                        handleOutOfMemoryError);
                result = getMale(
                        a_Agent_ID,
                        a_GENESIS_MaleCollection);
                ge.tryToEnsureThereIsEnoughMemoryToContinue(
                        a_GENESIS_MaleCollection,
                        handleOutOfMemoryError);
                return result;
            } else {
                if (_DeadMaleCollection.AgentID_Agent_Map.containsKey(a_Agent_ID)) {
                    result = (GENESIS_Male) _DeadMaleCollection.AgentID_Agent_Map.get(a_Agent_ID);
                } else {
                    // Load from _DeadMaleCollection_HashMap
                    if (_DeadMaleCollection_HashMap.keySet().contains(a_Agent_ID)) {
                        Long a_AgentCollectionID = _DeadMaleCollection_HashMap.get(a_Agent_ID);
                        GENESIS_MaleCollection a_GENESIS_MaleCollection = getMaleCollection(
                                a_AgentCollectionID,
                                type,
                                handleOutOfMemoryError);
                        result = getMale(
                                a_Agent_ID,
                                a_GENESIS_MaleCollection);
                        ge.tryToEnsureThereIsEnoughMemoryToContinue(
                                a_GENESIS_MaleCollection,
                                handleOutOfMemoryError);
                    }
                    if (result == null) {
                        // For some reason result == null
                        // Step back to figure out why... maybe the dead agent store
                        // does not contain the data...
                        int debug = 1;
                    }
                }
                result.ge = this.ge;
                result.AgentCollectionManager = this.ge.AgentEnvironment.getAgentCollectionManager();
                return result;
            }
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Long a_AgentCollection_ID = getAgentCollection_ID(
                        a_Agent_ID);
                GENESIS_MaleCollection a_GENESIS_MaleCollection
                        = getMaleCollection(
                                a_AgentCollection_ID,
                                type);
                if (swapToFile_MaleCollectionExcept_Account(
                        a_GENESIS_MaleCollection) < 1) {
                    throw a_OutOfMemoryError;
                }
                ge.initMemoryReserve();
                return getMale(
                        a_Agent_ID,
                        type,
                        handleOutOfMemoryError);
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    protected GENESIS_Male getMale(
            Long a_Agent_ID,
            GENESIS_MaleCollection a_GENESIS_MaleCollection) {
        GENESIS_Male a_Male = a_GENESIS_MaleCollection.getMale(a_Agent_ID);
        if (a_Male == null) {
            log(
                    "a_Male == null in " + this.getClass().getName()
                    + ".getMale(Long,GENESIS_MaleCollection)");
            log("a_GENESIS_MaleCollection._Type " + a_GENESIS_MaleCollection.getType());
            log("a_Agent_ID " + a_Agent_ID);
            log("a_GENESIS_MaleCollection_ID " + a_GENESIS_MaleCollection.AgentCollectionID);
            log("a_GENESIS_MaleCollection " + a_GENESIS_MaleCollection);
            return null;
        }
        //a_Male.ge = ge;
        a_Male.ge = a_GENESIS_MaleCollection.ge;
        return a_Male;
    }

    public GENESIS_Female getFemale(
            Long a_Agent_ID,
            String type,
            boolean handleOutOfMemoryError) {
        try {
            GENESIS_Female result = null;
            if (type.equalsIgnoreCase(GENESIS_Person.getTypeLivingFemale_String())) {
                Long a_AgentCollection_ID = getFemaleCollection_ID(
                        a_Agent_ID,
                        type,
                        handleOutOfMemoryError);
                GENESIS_FemaleCollection a_GENESIS_FemaleCollection = getFemaleCollection(
                        a_AgentCollection_ID,
                        type,
                        handleOutOfMemoryError);
                result = getFemale(
                        a_Agent_ID,
                        a_GENESIS_FemaleCollection);
                ge.tryToEnsureThereIsEnoughMemoryToContinue(
                        a_GENESIS_FemaleCollection,
                        handleOutOfMemoryError);
                return result;
            } else {
                if (_DeadFemaleCollection.AgentID_Agent_Map.containsKey(a_Agent_ID)) {
                    result = (GENESIS_Female) _DeadFemaleCollection.AgentID_Agent_Map.get(a_Agent_ID);
                } else {
                    // Load from _DeadFemaleCollection_HashMap
                    if (_DeadFemaleCollection_HashMap.keySet().contains(a_Agent_ID)) {
                        Long a_AgentCollectionID = _DeadFemaleCollection_HashMap.get(a_Agent_ID);
                        GENESIS_FemaleCollection a_GENESIS_FemaleCollection = getFemaleCollection(
                                a_AgentCollectionID,
                                type,
                                handleOutOfMemoryError);
                        result = getFemale(
                                a_Agent_ID,
                                a_GENESIS_FemaleCollection);
                        ge.tryToEnsureThereIsEnoughMemoryToContinue(
                                a_GENESIS_FemaleCollection,
                                handleOutOfMemoryError);
                    }
                    if (result == null) {
                        // For some reason result == null
                        // Step back to figure out why... maybe the dead agent store
                        // does not contain the data...
                        int debug = 1;
                    }
                }
                result.ge = this.ge;
                result.AgentCollectionManager = this.ge.AgentEnvironment.getAgentCollectionManager();
                return result;
            }
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Long a_AgentCollection_ID = getAgentCollection_ID(
                        a_Agent_ID);
                GENESIS_FemaleCollection a_GENESIS_FemaleCollection
                        = getFemaleCollection(
                                a_AgentCollection_ID,
                                type);
                if (swapToFile_FemaleCollectionExcept_Account(
                        a_GENESIS_FemaleCollection) < 1) {
                    throw a_OutOfMemoryError;
                }
                ge.initMemoryReserve();
                return getFemale(
                        a_Agent_ID,
                        type,
                        handleOutOfMemoryError);
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    protected GENESIS_Female getFemale(
            Long a_Agent_ID,
            GENESIS_FemaleCollection a_GENESIS_FemaleCollection) {
        GENESIS_Female a_Female = a_GENESIS_FemaleCollection.getFemale(
                a_Agent_ID);
        if (a_Female == null) {
            log(
                    "a_Female == null in " + this.getClass().getName()
                    + ".getFemale(Long,GENESIS_FemaleCollection)");
            log("a_GENESIS_FemaleCollection._Type " + a_GENESIS_FemaleCollection.getType());
            log("a_Agent_ID " + a_Agent_ID);
            log("a_GENESIS_FemaleCollection_ID " + a_GENESIS_FemaleCollection.AgentCollectionID);
            log("a_GENESIS_FemaleCollection " + a_GENESIS_FemaleCollection);
            return null;
        }
        //a_Female.ge = ge;
        a_Female.ge = a_GENESIS_FemaleCollection.ge;
        return a_Female;
    }

//    public GENESIS_Agent getAgent(
//            Long a_Agent_ID,
//            boolean handleOutOfMemoryError) {
//        try {
//            Long a_AgentCollection_ID = getAgentCollection_ID(
//                    a_Agent_ID,
//                    handleOutOfMemoryError);
//            GENESIS_AgentCollection a_GENESIS_AgentCollection = getAgentCollection(
//                    a_AgentCollection_ID,
//                    handleOutOfMemoryError);
//            GENESIS_Agent result = getAgent(
//                    a_Agent_ID,
//                    a_GENESIS_AgentCollection);
//            ge.checkAndMaybeFreeMemory(
//                    a_GENESIS_AgentCollection,
//                    handleOutOfMemoryError);
//            return result;
//        } catch (OutOfMemoryError a_OutOfMemoryError) {
//            if (handleOutOfMemoryError) {
//                ge.clearMemoryReserve();
//                Long a_AgentCollection_ID = getAgentCollection_ID(
//                        a_Agent_ID);
//                GENESIS_AgentCollection a_GENESIS_AgentCollection = getAgentCollection(
//                        a_AgentCollection_ID);
//                if (swapToFile_AgentCollectionExcept_Account(
//                        a_GENESIS_AgentCollection) < 1) {
//                    throw a_OutOfMemoryError;
//                }
//                ge.initMemoryReserve(
//                        ge.HOOMEF);
//                return getAgent(
//                        a_Agent_ID,
//                        handleOutOfMemoryError);
//            } else {
//                throw a_OutOfMemoryError;
//            }
//        }
//    }
//
//    protected GENESIS_Agent getAgent(
//            Long a_Agent_ID,
//            GENESIS_AgentCollection a_GENESIS_AgentCollection) {
//        GENESIS_Agent a_Agent = a_GENESIS_AgentCollection.getAgent(a_Agent_ID);
////        if (a_Agent.ge == null){
////            a_Agent.ge = AgentEnvironment;
////        }
//        a_Agent.ge = ge;
//        return a_Agent;
//    }
    /**
     * Based on _MaximumNumberOfAgentsPerAgentCollection, this gives an
     * AgentCollection_ID for the given Agent_ID.
     *
     * @param a_Agent_ID
     * @param type
     * @param handleOutOfMemoryError
     * @return
     */
    public Long getFemaleCollection_ID(
            Long a_Agent_ID,
            String type,
            boolean handleOutOfMemoryError) {
        try {
            Long result = getAgentCollection_ID(a_Agent_ID);
            GENESIS_FemaleCollection a_GENESIS_FemaleCollection
                    = getFemaleCollection(
                            result,
                            type);
            ge.tryToEnsureThereIsEnoughMemoryToContinue(
                    a_GENESIS_FemaleCollection,
                    handleOutOfMemoryError);
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Long a_GENESIS_AgentCollection_ID
                        = getAgentCollection_ID(a_Agent_ID);
                GENESIS_FemaleCollection a_GENESIS_FemaleCollection
                        = getFemaleCollection(
                                a_GENESIS_AgentCollection_ID,
                                type);
                if (swapToFile_FemaleCollectionExcept_Account(
                        a_GENESIS_FemaleCollection) < 1) {
                    throw a_OutOfMemoryError;
                }
                ge.initMemoryReserve();
//                return getFemaleCollection_ID(
//                        a_Agent_ID,
//                        type,
//                        handleOutOfMemoryError);
                return a_GENESIS_AgentCollection_ID;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * Based on _MaximumNumberOfAgentsPerAgentCollection, this gives an
     * AgentCollection_ID for the given Agent_ID.
     *
     * @param a_Agent_ID
     * @param type
     * @param handleOutOfMemoryError
     * @return
     */
    public Long getMaleCollection_ID(
            Long a_Agent_ID,
            String type,
            boolean handleOutOfMemoryError) {
        try {
            Long result = getAgentCollection_ID(
                    a_Agent_ID);
            GENESIS_MaleCollection a_GENESIS_MaleCollection
                    = getMaleCollection(
                            result,
                            type);
            ge.tryToEnsureThereIsEnoughMemoryToContinue(
                    a_GENESIS_MaleCollection,
                    handleOutOfMemoryError);
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Long a_GENESIS_AgentCollection_ID = getAgentCollection_ID(
                        a_Agent_ID);
                GENESIS_MaleCollection a_GENESIS_MaleCollection
                        = getMaleCollection(
                                a_GENESIS_AgentCollection_ID,
                                type);
                if (swapToFile_MaleCollectionExcept_Account(
                        a_GENESIS_MaleCollection) < 1) {
                    throw a_OutOfMemoryError;
                }
                ge.initMemoryReserve();
//                return getMaleCollection_ID(
//                        a_Agent_ID,
//                        type,
//                        handleOutOfMemoryError);
                return a_GENESIS_AgentCollection_ID;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * Based on _MaximumNumberOfAgentsPerAgentCollection, this gives an
     * AgentCollection _ID for the given Agent _ID.
     *
     * @param a_Agent_ID
     * @return
     */
    //protected Long getAgentCollection_ID(Long a_Agent_ID) {
    public Long getAgentCollection_ID(Long a_Agent_ID) {
        long l = _MaximumNumberOfAgentsPerAgentCollection;
        long result = 0L;
        while (a_Agent_ID >= l) {
            l += _MaximumNumberOfAgentsPerAgentCollection;
            result++;
        }
        return result;
    }

//    /**
//     * Based on _MaximumNumberOfAgentsPerAgentCollection, this gives an
//     * AgentCollection_ID for the given Agent_ID.
//     * @param a_Agent_ID
//     * @return
//     */
//    public Long getAgentCollection_ID(
//            Long a_Agent_ID,
//            boolean handleOutOfMemoryError) {
//        try {
//            Long result = getAgentCollection_ID(
//                    a_Agent_ID);
//            GENESIS_AgentCollection a_GENESIS_AgentCollection = getAgentCollection(result);
//            ge.checkAndMaybeFreeMemory(
//                    a_GENESIS_AgentCollection,
//                    handleOutOfMemoryError);
//            return result;
//        } catch (OutOfMemoryError a_OutOfMemoryError) {
//            if (handleOutOfMemoryError) {
//                ge.clearMemoryReserve();
//                Long a_GENESIS_AgentCollection_ID = getAgentCollection_ID(a_Agent_ID);
//                GENESIS_AgentCollection a_GENESIS_AgentCollection = getAgentCollection(a_GENESIS_AgentCollection_ID);
//                if (swapToFile_AgentCollectionExcept_Account(
//                        a_GENESIS_AgentCollection) < 1) {
//                    throw a_OutOfMemoryError;
//
//                }
//                ge.initMemoryReserve(
//                        ge.HOOMEF);
//                return getAgentCollection_ID(
//                        a_Agent_ID,
//                        handleOutOfMemoryError);
//            } else {
//                throw a_OutOfMemoryError;
//            }
//        }
//    }
//
//    /**
//     * Based on _MaximumNumberOfAgentsPerAgentCollection, this gives an
//     * AgentCollection _ID for the given Agent _ID.
//     * @param a_Agent_ID
//     * @return
//     */
//    protected Long getAgentCollection_ID(Long a_Agent_ID) {
//        long i = _MaximumNumberOfAgentsPerAgentCollection;
//        long result = 0L;
//        while (a_Agent_ID >= i) {
//            i += _MaximumNumberOfAgentsPerAgentCollection;
//            result++;
//        }
//        return result;
//    }
    public GENESIS_FemaleCollection getFemaleCollection(
            Long a_AgentCollection_ID,
            String type,
            boolean handleOutOfMemoryError) {
        if (type.equalsIgnoreCase(GENESIS_Person.getTypeLivingFemale_String())) {
            try {
                GENESIS_FemaleCollection result = getFemaleCollection(
                        a_AgentCollection_ID,
                        type);
                ge.tryToEnsureThereIsEnoughMemoryToContinue(
                        result,
                        handleOutOfMemoryError);
                return result;
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                if (handleOutOfMemoryError) {
                    ge.clearMemoryReserve();
                    GENESIS_FemaleCollection a_GENESIS_FemaleCollection
                            = getFemaleCollection(
                                    a_AgentCollection_ID,
                                    type);
                    if (swapToFile_FemaleCollectionExcept_Account(
                            a_GENESIS_FemaleCollection) < 1) {
                        throw a_OutOfMemoryError;
                    }
                    ge.initMemoryReserve();
                    return getFemaleCollection(
                            a_AgentCollection_ID,
                            type,
                            handleOutOfMemoryError);
                } else {
                    throw a_OutOfMemoryError;
                }
            }
        } else {
            //@TODO implementation to find dead
            return null;
        }
    }

    /**
     * If AgentCollection with a_AgentCollection_ID is in
     * _AgentCollection_HashMap it is returned, else it is created, put into
     * _AgentCollection_HashMap then returned.
     *
     * @param a_AgentCollection_ID
     * @param type
     * @return
     */
    //protected GENESIS_FemaleCollection getFemaleCollection(
    public GENESIS_FemaleCollection getFemaleCollection(
            Long a_AgentCollection_ID,
            String type) {
        GENESIS_FemaleCollection result;
        if (type.equalsIgnoreCase(GENESIS_Person.getTypeLivingFemale_String())) {
            result = LivingFemales.get(a_AgentCollection_ID);
            if (result == null) {
                File femaleCollectionDirectory = getFemaleCollectionDirectory(
                        a_AgentCollection_ID,
                        type);
                File a_AgentCollection_File = new File(
                        femaleCollectionDirectory,
                        GENESIS_FemaleCollection.class.getName()
                        + type + ".thisFile");
                if (a_AgentCollection_File.exists()) {
                    result = (GENESIS_FemaleCollection) Generic_IO.readObject(
                            a_AgentCollection_File);
                    result.Directory = femaleCollectionDirectory;
                    result.init(ge,
                            a_AgentCollection_ID,
                            type);
                } else {
                    result = new GENESIS_FemaleCollection(
                            ge,
                            a_AgentCollection_ID,
                            type);
                    LivingFemales.put(
                            a_AgentCollection_ID,
                            result);
                }
            }
        } else {
            // Load from AgentCollection Directory
            File a_AgentCollection_Directory
                    = getFemaleCollectionDirectory(a_AgentCollection_ID, type);
            File a_AgentCollection_File = new File(
                    a_AgentCollection_Directory,
                    GENESIS_FemaleCollection.class.getName()
                    + type + ".thisFile");
            if (a_AgentCollection_File.exists()) {
                result = (GENESIS_FemaleCollection) Generic_IO.readObject(
                        a_AgentCollection_File);
                result.Directory = a_AgentCollection_File.getParentFile();
                result.init(ge,
                        a_AgentCollection_ID,
                        type);
            } else {
                return null;
//                    result = new GENESIS_FemaleCollection(
//                            ge,
//                            a_AgentCollection_ID,
//                            type);
//                    // There is no in memory reference store implemented for
//                    // dead agents...
//                    _DeadFemaleCollection_HashMap.put(
//                            a_AgentCollection_ID,
//                            result);
            }
            if (result.ge == null) {
                result.ge = ge;
            }
            if (result.AgentCollectionManager == null) {
                result.AgentCollectionManager = ge.AgentEnvironment.AgentCollectionManager;
            }
        }
        result.ge = ge;
        result.AgentCollectionManager = ge.AgentEnvironment.AgentCollectionManager;
        return result;
    }

    public GENESIS_MaleCollection getMaleCollection(
            Long a_GENESIS_AgentCollection_ID,
            String type,
            boolean handleOutOfMemoryError) {
        try {
            GENESIS_MaleCollection result = getMaleCollection(
                    a_GENESIS_AgentCollection_ID,
                    type);
            ge.tryToEnsureThereIsEnoughMemoryToContinue(
                    result,
                    handleOutOfMemoryError);
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                GENESIS_MaleCollection a_GENESIS_MaleCollection
                        = getMaleCollection(
                                a_GENESIS_AgentCollection_ID,
                                type);
                if (swapToFile_MaleCollectionExcept_Account(
                        a_GENESIS_MaleCollection) < 1) {
                    throw a_OutOfMemoryError;
                }
                ge.initMemoryReserve();
                return getMaleCollection(
                        a_GENESIS_AgentCollection_ID,
                        type,
                        handleOutOfMemoryError);
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * If AgentCollection with a_AgentCollection_ID is in
     * _AgentCollection_HashMap it is returned, else it is created, put into
     * _AgentCollection_HashMap then returned.
     *
     * @param a_AgentCollection_ID
     * @param type
     * @return
     */
    //protected GENESIS_MaleCollection getMaleCollection(
    public GENESIS_MaleCollection getMaleCollection(
            Long a_AgentCollection_ID,
            String type) {
        GENESIS_MaleCollection result;
        if (type.equalsIgnoreCase(GENESIS_Person.getTypeLivingMale_String())) {
            result = LivingMales.get(a_AgentCollection_ID);
            if (result == null) {
                File maleCollectionDirectory = getMaleCollectionDirectory(
                        a_AgentCollection_ID,
                        type);
                File a_AgentCollection_File = new File(
                        maleCollectionDirectory,
                        GENESIS_MaleCollection.class.getName()
                        + type + ".thisFile");
                if (a_AgentCollection_File.exists()) {
                    result = (GENESIS_MaleCollection) Generic_IO.readObject(
                            a_AgentCollection_File);
                    result.Directory = maleCollectionDirectory;
                    result.init(
                            a_AgentCollection_ID,
                            type);
                } else {
                    result = new GENESIS_MaleCollection(
                            ge,
                            a_AgentCollection_ID,
                            type);
                    LivingMales.put(
                            a_AgentCollection_ID,
                            result);
                }
            }
        } else {
            // Load from AgentCollection Directory
            File a_AgentCollection_Directory
                    = getMaleCollectionDirectory(a_AgentCollection_ID, type);
            File a_AgentCollection_File = new File(
                    a_AgentCollection_Directory,
                    GENESIS_MaleCollection.class.getName()
                    + type + ".thisFile");
            if (a_AgentCollection_File.exists()) {
                result = (GENESIS_MaleCollection) Generic_IO.readObject(
                        a_AgentCollection_File);
                result.Directory = a_AgentCollection_File.getParentFile();
                result.init(
                        a_AgentCollection_ID,
                        type);
            } else {
                return null;
//                    result = new GENESIS_MaleCollection(
//                            ge,
//                            a_AgentCollection_ID,
//                            type);
//                    // There is no in memory reference store implemented for
//                    // dead agents...
//                    _DeadMaleCollection_HashMap.put(
//                            a_AgentCollection_ID,
//                            result);
            }
            if (result.ge == null) {
                result.ge = ge;
            }
            if (result.AgentCollectionManager == null) {
                result.AgentCollectionManager = ge.AgentEnvironment.AgentCollectionManager;
            }
        }
        result.ge = ge;
        result.AgentCollectionManager = ge.AgentEnvironment.AgentCollectionManager;
        return result;
    }

//public GENESIS_AgentCollection getAgentCollection(
//            Long a_GENESIS_AgentCollection_ID,
//            boolean handleOutOfMemoryError) {
//        try {
//            GENESIS_AgentCollection result = getAgentCollection(
//                    a_GENESIS_AgentCollection_ID);
//            ge.checkAndMaybeFreeMemory(
//                    result,
//                    handleOutOfMemoryError);
//            return result;
//        } catch (OutOfMemoryError a_OutOfMemoryError) {
//            if (handleOutOfMemoryError) {
//                ge.clearMemoryReserve();
//                GENESIS_AgentCollection a_GENESIS_AgentCollection = getAgentCollection(a_GENESIS_AgentCollection_ID);
//                if (swapToFile_AgentCollectionExcept_Account(
//                        a_GENESIS_AgentCollection) < 1) {
//                    throw a_OutOfMemoryError;
//
//                }
//                ge.initMemoryReserve(
//                        ge.HOOMEF);
//                return getAgentCollection(
//                        a_GENESIS_AgentCollection_ID,
//                        handleOutOfMemoryError);
//            } else {
//                throw a_OutOfMemoryError;
//            }
//        }
//    }
//
//    /**
//     * If AgentCollection with a_AgentCollection_ID is in
//     * _AgentCollection_HashMap it is returned, else it is created, put into
//     * _AgentCollection_HashMap then returned.
//     * @param a_AgentCollection_ID
//     * @return
//     */
//    protected GENESIS_AgentCollection getAgentCollection(Long a_AgentCollection_ID) {
//        GENESIS_AgentCollection result = null;
//        Object a_Object = _AgentCollection_HashMap.get(a_AgentCollection_ID);
//        if (a_Object == null) {
//            // Try loading form AgentCollection Directory
//            File a_AgentCollection_Directory = get_AgentCollection_Directory(a_AgentCollection_ID);
//            File a_AgentCollection_File = new File(
//                    a_AgentCollection_Directory,
//                    "" + a_AgentCollection_ID + "_"
//                    + GENESIS_AgentCollection.class.getName() + ".thisFile");
//            if (a_AgentCollection_File.exists()) {
//                result = (GENESIS_AgentCollection) Generic_IO.readObject(a_AgentCollection_File);
//                result.init(
//                        ge,
//                        a_AgentCollection_ID);
//            } else {
//                result = new GENESIS_AgentCollection(
//                        ge,
//                        a_AgentCollection_ID,
//                        null);
//                _AgentCollection_HashMap.put(
//                        a_AgentCollection_ID,
//                        result);
//            }
//        } else {
//            result = (GENESIS_AgentCollection) a_Object;
//            //int size = result.getAgentID_Agent_Map().size();
//            //log("size " + size);
//        }
//        if (result.ge == null) {
//            result.ge = ge;
//        }
//        return result;
//    }
    public void initLivingMales() {
        if (LivingMales == null) {
            LivingMales = new HashMap<>();
        }
    }

    public void initLivingFemales() {
        if (LivingFemales == null) {
            LivingFemales = new HashMap<>();
        }
    }
//    public void init_AgentCollection_HashMap() {
//        if (_AgentCollection_HashMap == null) {
//            _AgentCollection_HashMap = new HashMap<Long, GENESIS_AgentCollection>();
//        }
//    }

//    /**
//     *
//     * @param numberofFiles
//     * @param filesPerDirectory
//     * @param directory
//     */
//    public long initialise_FileCache(
//            long numberofFiles,
//            int filesPerDirectory,
//            File directory) {
//        long i = numberofFiles;
//        long diff0 = filesPerDirectory;
//        int directoryDepth = 1;//1;
//        while (numberofFiles / diff0 > 1) {
//            diff0 *= filesPerDirectory;
//            directoryDepth++;
//        }
//        long counter = 0;
//        String directoryString = new String("" + 0 + "_" + (diff0 - 1L));
//        File directory0 = new File(
//                directory,
//                directoryString);
//        if (directory0.mkdir()) {
//            counter++;
//        }
//        if (directoryDepth > 1) {
//            long diff1 = 1;
//            for (int j = 1; j < directoryDepth; j++) {
//                diff1 *= filesPerDirectory;
//            }
//            long[] mins = new long[filesPerDirectory];
//            mins[0] = 0L;
//            long[] maxs = new long[filesPerDirectory];
//            //maxs[0] = diffs[1] - 1L;
//            maxs[0] = diff1 - 1L;
//            for (int j = 1; j < filesPerDirectory; j++) {
//                mins[j] = mins[j - 1] + diff1;
//                maxs[j] = maxs[j - 1] + diff1;
//            }
//            File[] directories = new File[filesPerDirectory];
//            for (int j = 0; j < filesPerDirectory; j++) {
//                if (mins[j] < numberofFiles) {
//                    directoryString = new String("" + mins[j] + "_" + maxs[j]);
//                    directories[j] = new File(
//                            directory0,
//                            directoryString);
//                    if (directories[j].mkdir()) {
//                        counter++;
//                    }
//                    counter += initialise_FileCache0(
//                            numberofFiles,
//                            diff1,
//                            filesPerDirectory,
//                            mins[j],
//                            directories[j]);
//                }
//            }
////            if (directoryDepth > 2) {
////                initialise_FileCache()
////                for (int j = 0; j < filesPerDirectory; j++) {
////                    mins[1][j] = mins[0][j];
////                    maxs[1][j] = mins[0][j];
////                    directories[1][j] = directories[0][j];
////                }
//////                for (int j = 0; j < filesPerDirectory; j++) {
//////                    mins[0][j] = mins[1][j];
//////                    diffs[0] = diffs[1] / filesPerDirectory;
//////                    maxs[0][l] = diffs[0] - 1L;
//////                    if (mins[0][l] < numberofFiles) {
//////                        directoryString = new String("" + mins[1][l] + "_" + maxs[1][l]);
//////                        directories[1][l] = new File(
//////                                directories[0][l],
//////                                directoryString);
//////                        if (directories[1][l].mkdir()) {
//////                            counter++;
//////                        }
//////                        directories[0][l] = directories[1][l];
//////                        mins[1][l] += diffs[0];
//////                        maxs[1][l] += diffs[0];
//////                    }
//////                    for (int depth = 3; depth < directoryDepth; depth++) {
//////                        for (int n = 0; n < filesPerDirectory; n++) {//
//////                            mins[0][l] = mins[1][l];
//////                            diffs[0] = diffs[1] / filesPerDirectory;
//////                            maxs[0][l] = diffs[0] - 1L;
//////                            if (mins[0][l] < numberofFiles) {
//////                                for (int m = 0; m < filesPerDirectory; m++) {
//////
//////                                    directoryString = new String("" + mins[1][l] + "_" + maxs[1][l]);
//////                                    directories[1][l] = new File(
//////                                            directories[0][l],
//////                                            directoryString);
//////                                    if (directories[1][l].mkdir()) {
//////                                        counter++;
//////                                    }
//////                                    directories[0][l] = directories[1][l];
//////                                    mins[1][l] += diffs[0];
//////                                    maxs[1][l] += diffs[0];
//////                                }
//////                            }
//////                        }
//////                        mins[1][0] += diffs[0];
//////                        maxs[1][0] += diffs[0];
//////                    }
//////                }
////            }
//        }
//        log("created " + counter + " directories");
//        return counter;
//    }
//
//    /**
//     *
//     * @param numberofFiles
//     * @param filesPerDirectory
//     * @param directory
//     */
//    private long initialise_FileCache0(
//            long numberofFiles0,
//            long numberofFiles,
//            int filesPerDirectory,
//            long min,
//            File directory) {
//        long i = numberofFiles;
//        long diff0 = filesPerDirectory;
//        int directoryDepth = 1;//1;
//        while (numberofFiles / diff0 > 1) {
//            diff0 *= filesPerDirectory;
//            directoryDepth++;
//        }
//        long counter = 0;
//        if (directoryDepth > 1) {
////            String directoryString = new String("" + 0 + "_" + (diffk - 1));
////            File directory0 = new File(
////                    directory,
////                    directoryString);
////            if (directory0.mkdir()) {
////                counter++;
////            }
//            long diff1 = 1;
//            for (int j = 1; j < directoryDepth; j++) {
//                diff1 *= filesPerDirectory;
//            }
//            long[] mins = new long[filesPerDirectory];
//            mins[0] = min;
//            long[] maxs = new long[filesPerDirectory];
//            maxs[0] = min + diff1 - 1L;
//            for (int j = 1; j < filesPerDirectory; j++) {
//                mins[j] = mins[j - 1] + diff1;
//                maxs[j] = maxs[j - 1] + diff1;
//            }
//            File[] directories = new File[filesPerDirectory];
//            for (int j = 0; j < filesPerDirectory; j++) {
//                if (mins[j] < numberofFiles0) {
//                    String directoryString = new String("" + mins[j] + "_" + maxs[j]);
//                    directories[j] = new File(
//                            directory,
//                            directoryString);
//                    if (directories[j].mkdir()) {
//                        counter++;
//                    }
//                    counter += initialise_FileCache0(
//                            numberofFiles0,
//                            diff1,
//                            filesPerDirectory,
//                            mins[j],
//                            directories[j]);
//                }
//            }
//        }
//        return counter;
//    }
//    public void delete_FileCache(
//            File directory) {
//        long[] deletedCounts = StaticFile.deleteDirectory(directory);
//        log("Deleted " + deletedCounts[0] + " directories");
//        log("Deleted " + deletedCounts[1] + " files");
//    }
    /**
     * Swaps to file any AgentCollection.
     *
     * @param handleOutOfMemoryError If true then OutOfMemoryErrors are caught,
     * swap operations are initiated, then the method is re-called. If false
     * then OutOfMemoryErrors are caught and thrown.
     * @return
     */
    public boolean swapToFile_AgentCollection(
            boolean handleOutOfMemoryError) {
        try {
            boolean result = swapAgentCollection();
            try {
                ge.AgentEnvironment.checkAndMaybeFreeMemory();
            } catch (OutOfMemoryError e) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw e;
            }
            return result;
        } catch (OutOfMemoryError e) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                long account;
                boolean createdRoom = false;
                while (!createdRoom) {
                    account = swapAgentCollection_Account();
                    if (account < 1) {
                        throw e;
                    }
                    ge.initMemoryReserve();
                    Object[] ensurance = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account();
                    if (ensurance != null) {
                        createdRoom = (Boolean) ensurance[0];
                        if (!createdRoom) {
                            log(
                                    "Struggling to ensure there is enough memory to continue in "
                                    + this.getClass().getName()
                                    + ".swapToFile_AgentCollection(boolean)");
                        }
                    }
                }
                return true;
            } else {
                throw e;
            }
        }
    }

    /**
     * Attempts to swap to file any MaleCollection. If there are none to be
     * swapped this simply returns without swapping anything rather than calling
     * on something else to be swapped or throwing an Error or Exception.
     *
     * @return
     */
    protected boolean swapToFile_MaleCollection() {
        if (LivingMales.isEmpty()) {
            log(
                    "Warning: No MaleCollections to swap in "
                    + this.getClass().getName()
                    + ".swapToFile_MaleCollection()");
            //ge.swapToFile_SomeDataExcept(this);
            return false;
        }
        Iterator a_Iterator = LivingMales.keySet().iterator();
        Long a_AgentCollection_ID = null;
        GENESIS_MaleCollection a_MaleCollection;
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = (Long) a_Iterator.next();
            Object value = LivingMales.get(
                    a_AgentCollection_ID);
            //if (value != null) {
            a_MaleCollection = (GENESIS_MaleCollection) value;
            //a_GENESIS_AgentCollection.swapToFileAgents();
            a_MaleCollection.write();
            a_MaleCollection = null;
            break;
            //}
        }
        LivingMales.remove(a_AgentCollection_ID);
        return true;
    }

    /**
     * Attempts to swap to file any FemaleCollection. If there are none to be
     * swapped this simply returns without swapping anything rather than calling
     * on something else to be swapped or throwing an Error or Exception.
     *
     * @return
     */
    protected boolean swapToFile_FemaleCollection() {
        if (LivingFemales.isEmpty()) {
            log(
                    "Warning: No FemaleCollections to swap in "
                    + this.getClass().getName()
                    + ".swapToFile_FemaleCollection()");
            //ge.swapToFile_SomeDataExcept(this);
            return false;
        }
        Iterator a_Iterator = LivingFemales.keySet().iterator();
        Long a_AgentCollection_ID = null;
        GENESIS_FemaleCollection a_FemaleCollection;
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = (Long) a_Iterator.next();
            Object value = LivingFemales.get(
                    a_AgentCollection_ID);
            //if (value != null) {
            a_FemaleCollection = (GENESIS_FemaleCollection) value;
            //a_GENESIS_AgentCollection.swapToFileAgents();
            a_FemaleCollection.write();
            a_FemaleCollection = null;
            break;
            //}
        }
        LivingFemales.remove(a_AgentCollection_ID);
        return true;
    }

    /**
     * Attempts to swap to file any AgentCollection. If there are none to be
     * swapped this simply returns without swapping anything rather than calling
     * on something else to be swapped or throwing an Error or Exception.
     *
     * @return
     */
    protected boolean swapAgentCollection() {
        if (LivingFemales.isEmpty()
                && LivingMales.isEmpty()) {
            log(
                    "Warning: No AgentCollections to swap in "
                    + this.getClass().getName()
                    + ".swapToFile_AgentCollection()");
            //ge.swapToFile_SomeDataExcept(this);
            return false;
        }
        /*
         * Implementation tries to swap _MaleCollection first. Another
         * implementation might randomly choose Male or Female...
         */
        if (LivingMales.isEmpty()) {
            Iterator a_Iterator = LivingFemales.keySet().iterator();
            Long a_AgentCollection_ID = null;
            GENESIS_FemaleCollection a_FemaleCollection;
            while (a_Iterator.hasNext()) {
                a_AgentCollection_ID = (Long) a_Iterator.next();
                Object value = LivingFemales.get(
                        a_AgentCollection_ID);
                //if (value != null) {
                a_FemaleCollection = (GENESIS_FemaleCollection) value;
                //a_GENESIS_AgentCollection.swapToFileAgents();
                a_FemaleCollection.write();
                a_FemaleCollection = null;
                break;
                //}
            }
            LivingFemales.remove(a_AgentCollection_ID);
            return true;
        } else {
            Iterator a_Iterator = LivingMales.keySet().iterator();
            Long a_AgentCollection_ID = null;
            GENESIS_MaleCollection a_MaleCollection;
            while (a_Iterator.hasNext()) {
                a_AgentCollection_ID = (Long) a_Iterator.next();
                Object value = LivingMales.get(
                        a_AgentCollection_ID);
                //if (value != null) {
                a_MaleCollection = (GENESIS_MaleCollection) value;
                //a_GENESIS_AgentCollection.swapToFileAgents();
                a_MaleCollection.write();
                a_MaleCollection = null;
                break;
                //}
            }
            LivingMales.remove(a_AgentCollection_ID);
            return true;
        }
    }

    /**
     * Attempts to swaps to file any AgentCollection
     *
     * @param handleOutOfMemoryError
     * @return The number of AgentCollections swapped
     */
    public long swapToFile_AgentCollection_Account(
            boolean handleOutOfMemoryError) {
        try {
            long result = swapAgentCollection_Account();
            try {
                if (result < 1) {
                    Object[] account = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account();
                    if ((Boolean) account[0]) {
                        result += (Long) account[1];
                    } else {
                        throw new OutOfMemoryError();
                    }
                } else {
                    result += ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            handleOutOfMemoryError);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                long result = 0;
                long account = 0;
                boolean createdRoom = false;
                while (!createdRoom) {
                    account = swapAgentCollection_Account();
                    if (account < 1) {
                        throw a_OutOfMemoryError;
                    }
                    result += account;
                    result += ge.initMemoryReserve_AccountAgentCollections(ge.HOOMEF);
                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account();
                    if (potentialPartResult != null) {
                        result += (Long) potentialPartResult[1];
                        createdRoom = (Boolean) potentialPartResult[0];
                        if (!createdRoom) {
                            log(
                                    "Struggling to ensure there is enough memory to continue in "
                                    + this.getClass().getName()
                                    + ".swapToFile_AgentCollection_Account(boolean)");
                        }
                    }
                }
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * Attempts to swaps to file any FemaleCollection
     *
     * @param handleOutOfMemoryError
     * @return The number of AgentCollections swapped
     */
    public long swapToFile_FemaleCollection_Account(
            boolean handleOutOfMemoryError) {
        try {
            long result = swapToFile_FemaleCollection_Account();
            try {
                if (result < 1) {
                    Object[] potentialPartResult
                            = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account();
                    if ((Boolean) potentialPartResult[0]) {
                        result += (Long) potentialPartResult[1];
                    } else {
                        throw new OutOfMemoryError();
                    }
                } else {
                    result += ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            handleOutOfMemoryError);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                long result = 0;
                long account;
                boolean createdRoom = false;
                while (!createdRoom) {
                    account = swapAgentCollection_Account();
                    if (account < 1) {
                        throw a_OutOfMemoryError;
                    }
                    result += account;
                    result += ge.initMemoryReserve_AccountAgentCollections(ge.HOOMEF);
                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account();
                    if (potentialPartResult != null) {
                        result += (Long) potentialPartResult[1];
                        createdRoom = (Boolean) potentialPartResult[0];
                        if (!createdRoom) {
                            log(
                                    "Struggling to ensure there is enough memory to continue in "
                                    + this.getClass().getName()
                                    + ".swapToFile_FemaleCollection_Account(boolean)");
                        }
                    }
                }
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    protected long swapToFile_FemaleCollection_Account() {
        long result = 0;
        if (LivingFemales.isEmpty()) {
            log(
                    "Warning: No FemaleCollections to swap in "
                    + this.getClass().getName()
                    + ".swapToFile_FemaleCollection_Account()");
            return result;
//        } else {
//            // Implementation changed, so mappings are removed rather than
//            // values being set to null, so this is not needed...
//            Collection values = LivingFemales.values();
//            if (values.size() == 1 && values.contains(null)) {
//                log(
//                        "Warning: No FemaleCollections to swap in "
//                        + this.getClass().getName()
//                        + ".swapToFile_FemaleCollection_Account()");
//                return result;
//            }
        }
        Iterator<Long> a_Iterator
                = LivingFemales.keySet().iterator();
        Long a_AgentCollection_ID = null;
        GENESIS_FemaleCollection a_GENESIS_FemaleCollection;
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            a_GENESIS_FemaleCollection = LivingFemales.get(
                    a_AgentCollection_ID);
            if (a_GENESIS_FemaleCollection != null) {
                a_GENESIS_FemaleCollection.write();
                a_GENESIS_FemaleCollection = null;
                result = 1;
                break;
            }
        }
        // The value of result should always be 1 here!
        if (result == 1) {
            LivingFemales.remove(a_AgentCollection_ID);
        }
        return result;
    }

    /**
     * Attempts to swaps to file any MaleCollection
     *
     * @param handleOutOfMemoryError
     * @return The number of MaleCollections swapped
     */
    public long swapToFile_MaleCollection_Account(
            boolean handleOutOfMemoryError) {
        try {
            long result = swapToFile_MaleCollection_Account();
            try {
                if (result < 1) {
                    Object[] potentialPartResult
                            = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account();
                    if ((Boolean) potentialPartResult[0]) {
                        result += (Long) potentialPartResult[1];
                    } else {
                        throw new OutOfMemoryError();
                    }
                } else {
                    result += ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            handleOutOfMemoryError);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                long result = 0;
                long account = 0;
                boolean createdRoom = false;
                while (!createdRoom) {
                    account = swapAgentCollection_Account();
                    if (account < 1) {
                        // Can try GENESIS_Enviroment.swap...?
                        throw a_OutOfMemoryError;
                    }
                    result += account;
                    result += ge.initMemoryReserve_AccountAgentCollections(ge.HOOMEF);
                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account();
                    if (potentialPartResult != null) {
                        result += (Long) potentialPartResult[1];
                        createdRoom = (Boolean) potentialPartResult[0];
                        if (!createdRoom) {
                            log(
                                    "Struggling to ensure there is enough memory to continue in "
                                    + this.getClass().getName()
                                    + ".swapToFile_MaleCollection_Account(boolean)");
                        }
                    }
                }
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    protected long swapToFile_MaleCollection_Account() {
        long result = 0;
        if (LivingMales.isEmpty()) {
            log(
                    "Warning: No MaleCollections to swap in "
                    + this.getClass().getName()
                    + ".swapToFile_MaleCollection_Account()");
            return result;
//        } else {
//            // Implementation changed, so mappings are removed rather than
//            // values being set to null, so this is not needed...
//            Collection values = LivingMales.values();
//            if (values.size() == 1 && values.contains(null)) {
//                log(
//                        "Warning: No MaleCollections to swap in "
//                        + this.getClass().getName()
//                        + ".swapToFile_MaleCollection_Account()");
//                 return result;
//           }
        }
        Iterator<Long> a_Iterator
                = LivingMales.keySet().iterator();
        Long a_AgentCollection_ID = null;
        GENESIS_MaleCollection a_GENESIS_MaleCollection;
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            a_GENESIS_MaleCollection = LivingMales.get(
                    a_AgentCollection_ID);
            if (a_GENESIS_MaleCollection != null) {
                a_GENESIS_MaleCollection.write();
                a_GENESIS_MaleCollection = null;
                result = 1;
                break;
            }
        }
        // The value of result should always be 1 here!
        if (result == 1) {
            LivingMales.remove(a_AgentCollection_ID);
        }
        return result;
    }

    /**
     * First try to swap Male then Female. Another implementation might chose
     * more randomly or iteratively whether Male of Female...
     *
     * @return
     */
    protected long swapAgentCollection_Account() {
        long result = swapToFile_MaleCollection_Account();
        if (result == 0) {
            result = swapToFile_FemaleCollection_Account();
        }
        return result;
    }

    /**
     * @TODO Change to return null if nothing to be swapped instead of throwing
     * OutOfMemoryError... Also some accounting may be lost if FemaleCollection
     * is swapped yet an OutOfMemoryError gets thrown, so this is this can be
     * made more robust/reliable... Attempts to swap to file any
     * FemaleCollection and account detail by returning an Object[] identifying
     * which AgentCollections have been swapped in the process.
     * @param handleOutOfMemoryError
     * @return A Object[] with first element indicating the success of having
     * swapped a FemaleCollection. The second element is a HashSet<Long> of all
     * FemaleCollection ID for those FemaleCollection swapped in the process.
     * The third element is a HashSet<Long> of all FemaleCollection ID for those
     * FemaleCollection swapped in the process.
     */
    public Object[] swapToFile_FemaleCollection_AccountDetail(
            boolean handleOutOfMemoryError) {
        try {
            Object[] result = new Object[3];
            HashSet<Long> swappedFemaleCollection_HashSet
                    = swapToFile_FemaleCollection_AccountDetail();
            if (swappedFemaleCollection_HashSet == null) {
                result[0] = false;
            } else {
                result[0] = true;
            }
            try {
                Object[] potentialPartResult
                        = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail();
                if (!(Boolean) potentialPartResult[0]) {
                    throw new OutOfMemoryError();
                }
                combine(result, potentialPartResult);
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing
                // a_OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Object[] result = swapToFile_FemaleCollection_AccountDetail(ge.HOOMEF);
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult
                            = swapToFile_MaleCollection_AccountDetail(ge.HOOMEF);
                    if (!(Boolean) potentialPartResult[0]) {
                        // Could try swapping grids here...
                        throw a_OutOfMemoryError;
                    }
                    combine(result, potentialPartResult);
                }
                Object[] potentialPartResult
                        = ge.initMemoryReserve_AccountDetailAgentCollections(ge.HOOMEF);
                combine(result, potentialPartResult);
                potentialPartResult
                        = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                                handleOutOfMemoryError);
                combine(result, potentialPartResult);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * Attempts to swap to file a _FemaleCollection in LivingFemales
     *
     * @return null if there are no _FemaleCollection to swap in LivingFemales
     * otherwise a collection of the IDs of those that are swapped
     */
    //protected Object[] swapToFile_FemaleCollection_AccountDetail() {
    protected HashSet<Long> swapToFile_FemaleCollection_AccountDetail() {
        //Object[] result = new Object[3];
        HashSet<Long> swappedFemaleCollectionID_HashSet = new HashSet<Long>(0);
        if (LivingFemales.isEmpty()) {
            log(
                    "Warning: No FemaleCollections to swap in "
                    + this.getClass().getName()
                    + ".swapToFile_FemaleCollection_AccountDetail()");
            return null; //return result;
//        } else {
//            Collection values = LivingFemales.values();
//            if (values.size() == 1 && values.contains(null)) {
//                log(
//                        "Warning: No FemaleCollections to swap in "
//                        + this.getClass().getName()
//                        + ".swapToFile_FemaleCollection_AccountDetail()");
//                return null; //return result;
//            }
        }
        //result[0] = false;
        Iterator<Long> a_Iterator
                = LivingFemales.keySet().iterator();
        Long a_AgentCollection_ID = null;
        GENESIS_FemaleCollection a_GENESIS_FemaleCollection;
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            a_GENESIS_FemaleCollection = LivingFemales.get(
                    a_AgentCollection_ID);
            if (a_GENESIS_FemaleCollection != null) {
                a_GENESIS_FemaleCollection.write();
                a_GENESIS_FemaleCollection = null;
                swappedFemaleCollectionID_HashSet.add(a_AgentCollection_ID);
                //result[0] = true;
                break;
            }
        }
        // It should not be the case that swappedMaleCollectionID_HashSet is empty!
        if (!swappedFemaleCollectionID_HashSet.isEmpty()) {
            LivingMales.remove(a_AgentCollection_ID);
        }
        //result[1] = swappedFemaleCollectionID_HashSet;
        //return result;
        return swappedFemaleCollectionID_HashSet;
    }

    /**
     * @TODO Change to return null if nothing to be swapped instead of throwing
     * OutOfMemoryError... Also some accounting may be lost if MaleCollection is
     * swapped yet an OutOfMemoryError gets thrown, so this is this can be made
     * more robust/reliable... Attempts to swap to file any MaleCollection and
     * account detail by returning an Object[] identifying which
     * AgentCollections have been swapped in the process.
     * @param handleOutOfMemoryError
     * @return A Object[] with first element indicating the success of having
     * swapped a MaleCollection. The second element is a HashSet<Long> of all
     * MaleCollection ID for those MaleCollection swapped in the process. The
     * third element is a HashSet<Long> of all MaleCollection ID for those
     * MaleCollection swapped in the process.
     */
    public Object[] swapToFile_MaleCollection_AccountDetail(
            boolean handleOutOfMemoryError) {
        try {
            Object[] result = new Object[3];
            HashSet<Long> swappedMaleCollection_HashSet
                    = swapToFile_MaleCollection_AccountDetail();
            if (swappedMaleCollection_HashSet == null) {
                result[0] = false;
            } else {
                result[0] = true;
            }
            try {
                Object[] potentialPartResult
                        = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail();
                if (!(Boolean) potentialPartResult[0]) {
                    throw new OutOfMemoryError();
                }
                combine(result, potentialPartResult);
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing
                // a_OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Object[] result = swapToFile_MaleCollection_AccountDetail(ge.HOOMEF);
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult
                            = swapToFile_FemaleCollection_AccountDetail(ge.HOOMEF);
                    if (!(Boolean) potentialPartResult[0]) {
                        // Could try swapping grids here...
                        throw a_OutOfMemoryError;
                    }
                    combine(result, potentialPartResult);
                }
                Object[] potentialPartResult
                        = ge.initMemoryReserve_AccountDetailAgentCollections(ge.HOOMEF);
                combine(result, potentialPartResult);
                potentialPartResult
                        = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                                handleOutOfMemoryError);
                combine(result, potentialPartResult);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * Attempts to swap to file a _MaleCollection in LivingMales
     *
     * @return null if there are no _MaleCollection to swap in LivingMales
     * otherwise a collection of the IDs of those that are swapped
     */
    //protected Object[] swapToFile_MaleCollection_AccountDetail() {
    protected HashSet<Long> swapToFile_MaleCollection_AccountDetail() {
        //Object[] result = new Object[3];
        HashSet<Long> swappedMaleCollectionID_HashSet = new HashSet<Long>(0);
        if (LivingMales.isEmpty()) {
            log(
                    "Warning: No MaleCollections to swap in "
                    + this.getClass().getName()
                    + ".swapToFile_MaleCollection_AccountDetail()");
            return null; //return result;
//        } else {
//            Collection values = LivingMales.values();
//            if (values.size() == 1 && values.contains(null)) {
//                log(
//                        "Warning: No MaleCollections to swap in "
//                        + this.getClass().getName()
//                        + ".swapToFile_MaleCollection_AccountDetail()");
//                return null; //return result;
//            }
        }
        //result[0] = false;
        Iterator<Long> a_Iterator
                = LivingMales.keySet().iterator();
        Long a_AgentCollection_ID = null;
        GENESIS_MaleCollection a_GENESIS_MaleCollection;
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            a_GENESIS_MaleCollection = LivingMales.get(
                    a_AgentCollection_ID);
            if (a_GENESIS_MaleCollection != null) {
                a_GENESIS_MaleCollection.write();
                a_GENESIS_MaleCollection = null;
                swappedMaleCollectionID_HashSet.add(a_AgentCollection_ID);
                //result[0] = true;
                break;
            }
        }
        // It should not be the case that swappedMaleCollectionID_HashSet is empty!
        if (!swappedMaleCollectionID_HashSet.isEmpty()) {
            LivingMales.remove(a_AgentCollection_ID);
        }
        //result[2] = swappedMaleCollectionID_HashSet;
        //return result;
        return swappedMaleCollectionID_HashSet;
    }

    public static void combine(
            Object[] result,
            Object[] potentialPartResult) {
        if (potentialPartResult != null) {
            if (result[1] == null) {
                if (potentialPartResult[1] != null) {
                    result[1] = potentialPartResult[1];
                }
            } else {
                ((HashSet<Long>) result[1]).addAll(
                        (HashSet<Long>) potentialPartResult[1]);
            }
            if (result[2] == null) {
                if (potentialPartResult[2] != null) {
                    result[2] = potentialPartResult[2];
                }
            } else {
                ((HashSet<Long>) result[2]).addAll(
                        (HashSet<Long>) potentialPartResult[2]);
            }
        }
    }

    /**
     * Attempts to swaps to file any AgentCollection and account detail by
     * returning an Object[] HashSet<Long> identifying AgentCollections swapped
     * in the process. MaleCollections are swapped before FemaleCollections.
     *
     * @param handleOutOfMemoryError
     * @return A Object[] with first element indicating the success of having
     * swapped an AgentCollection. The second element is a HashSet<Long> of all
     * FemaleCollection ID for those FemaleCollection swapped in the process.
     * The third element is a HashSet<Long> of all MaleCollection ID for those
     * MaleCollection swapped in the process.
     */
    public Object[] swapToFile_AgentCollection_AccountDetail(
            boolean handleOutOfMemoryError) {
        try {
            Object[] result = new Object[3];
            HashSet<Long> swapped_MaleCollectionID_HashSet
                    = swapToFile_MaleCollection_AccountDetail();
            HashSet<Long> swapped_FemaleCollectionID_HashSet = null;
            if (swapped_MaleCollectionID_HashSet == null) {
                swapped_FemaleCollectionID_HashSet
                        = swapToFile_FemaleCollection_AccountDetail();
                if (swapped_FemaleCollectionID_HashSet == null) {
                    //return null;
                    result[0] = false;
                } else {
                    result[0] = true;
                }
            } else {
                result[0] = true;
            }
            result[1] = swapped_FemaleCollectionID_HashSet;
            result[2] = swapped_MaleCollectionID_HashSet;
//            Object[] result = swapToFile_MaleCollection_AccountDetail(
//                    handleOutOfMemoryError);
//            if (!((Boolean) result[0])) {
//                if (result[2] == null) {
//                    Object[] potentialPartResult =
//                            swapToFile_FemaleCollection_AccountDetail(
//                            handleOutOfMemoryError);
//                    if ((Boolean) potentialPartResult[0]) {
//                        result[0] = potentialPartResult[0];
//                        result[1] = potentialPartResult[1];
////                    } else {
////                        // return null;
////                        throw new OutOfMemoryError();
//                    }
//                }
//            }
            try {
                Object[] potentialPartResult
                        = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail();
                if (!(Boolean) potentialPartResult[0]) {
                    throw new OutOfMemoryError();
                }
                combine(result, potentialPartResult);
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing
                // a_OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Object[] result = new Object[3];
                HashSet<Long> swapped_MaleCollectionID_HashSet
                        = swapToFile_MaleCollection_AccountDetail();
                HashSet<Long> swapped_FemaleCollectionID_HashSet = null;
                if (swapped_MaleCollectionID_HashSet == null) {
                    swapped_FemaleCollectionID_HashSet
                            = swapToFile_FemaleCollection_AccountDetail();
                    if (swapped_FemaleCollectionID_HashSet == null) {
                        //return null;
                        result[0] = false;
                    } else {
                        result[0] = true;
                    }
                } else {
                    result[0] = true;
                }
                result[1] = swapped_FemaleCollectionID_HashSet;
                result[2] = swapped_MaleCollectionID_HashSet;
//                HashSet<Long> result = new HashSet<Long>();
                boolean createdRoom = false;
                while (!createdRoom) {
                    Object[] potentialPartResult = swapToFile_AgentCollection_AccountDetail();
                    combine(result, potentialPartResult);
                    potentialPartResult = ge.initMemoryReserve_AccountDetailAgentCollections(ge.HOOMEF);
                    combine(result, potentialPartResult);
                    potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                            handleOutOfMemoryError);
                    combine(result, potentialPartResult);
                    createdRoom = true;
                }
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * Attempts to swap to file AgentCollections first trying Male then Female.
     * Only one type is
     *
     * @return null if nothing to swap otherwise Object[] containing HashSets of
     * IDs for each AgentCollection swapped. The first element of the result are
     * for Female and the second are Male.
     */
    protected Object[] swapToFile_AgentCollection_AccountDetail() {
        Object[] result = new Object[3];
        HashSet<Long> swapped_MaleAgentCollectionID_HashSet
                = swapToFile_MaleCollection_AccountDetail();
        if (swapped_MaleAgentCollectionID_HashSet == null) {
            HashSet<Long> swapped_FemaleAgentCollectionID_HashSet
                    = swapToFile_FemaleCollection_AccountDetail();
            if (swapped_FemaleAgentCollectionID_HashSet == null) {
                result[0] = false;
            } else {
                result[0] = true;
                result[1] = swapped_FemaleAgentCollectionID_HashSet;
            }
        } else {
            result[0] = true;
            result[2] = swapped_MaleAgentCollectionID_HashSet;
        }
        return result;
    }

    /**
     * Attempts to swap to file a_GENESIS_FemaleCollection, but may swap other
     * GENESIS_AgentCollections in the process which is likely if
     * a_GENESIS_FemaleCollection is very small and total available memory is
     * almost low and handleOutOfMemoryError is true
     *
     * @param fc The GENESIS_FemaleCollection to attempt to swap to file
     * @param handleOutOfMemoryError
     */
    public void swapToFile_FemaleCollection(
            GENESIS_FemaleCollection fc,
            boolean handleOutOfMemoryError) {
        try {
            boolean success = swapToFile_FemaleCollection(fc);
            try {
                ge.AgentEnvironment.checkAndMaybeFreeMemory();
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                boolean createdRoom = false;
                while (!createdRoom) {
                    if (!swapToFile_FemaleCollection(fc)) {
                        if (!swapToFile_MaleCollection()) {
                            throw a_OutOfMemoryError;
                        }
                    }
                    ge.initMemoryReserve();
                    createdRoom = ge.AgentEnvironment.checkAndMaybeFreeMemory();
                }
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * Swaps to file the FemaleCollection a_GENESIS_FemaleCollection.
     *
     * @param a_GENESIS_FemaleCollection The FemaleCollection to be swapped to
     * disk
     * @return
     */
    protected boolean swapToFile_FemaleCollection(
            GENESIS_FemaleCollection a_GENESIS_FemaleCollection) {
        if (a_GENESIS_FemaleCollection == null) {
            return false;
        }
        a_GENESIS_FemaleCollection.write();
        LivingFemales.remove(a_GENESIS_FemaleCollection.getAgentCollectionID());
        a_GENESIS_FemaleCollection = null;
        return true;
    }

    /**
     * Attempts to swap to file a_GENESIS_MaleCollection, but may swap other
     * GENESIS_AgentCollections in the process which is likely if
     * a_GENESIS_MaleCollection is very small and total available memory is
     * almost low and handleOutOfMemoryError is true
     *
     * @param mc The GENESIS_MaleCollection to attempt to swap to file
     * @param hoome
     */
    public void swapToFile_MaleCollection(
            GENESIS_MaleCollection mc,
            boolean hoome) {
        try {
            boolean success = swapToFile_MaleCollection(mc);
            try {
                ge.AgentEnvironment.checkAndMaybeFreeMemory();
            } catch (OutOfMemoryError e) {
                // Set hoome = false to exit method by throwing OutOfMemoryError
                hoome = false;
                throw e;
            }
        } catch (OutOfMemoryError e) {
            if (hoome) {
                ge.clearMemoryReserve();
                boolean createdRoom = false;
                while (!createdRoom) {
                    if (!swapToFile_MaleCollection(mc)) {
                        if (!swapToFile_FemaleCollection()) {
                            throw e;
                        }
                    }
                    ge.initMemoryReserve();
                    createdRoom = ge.AgentEnvironment.checkAndMaybeFreeMemory();
                }
            } else {
                throw e;
            }
        }
    }

    /**
     * Swaps to file the MaleCollection a_GENESIS_MaleCollection.
     *
     * @param a_GENESIS_MaleCollection The MaleCollection to be swapped to disk
     * @return
     */
    protected boolean swapToFile_MaleCollection(
            GENESIS_MaleCollection a_GENESIS_MaleCollection) {
        if (a_GENESIS_MaleCollection == null) {
            return false;
        }
        a_GENESIS_MaleCollection.write();
        LivingMales.remove(
                a_GENESIS_MaleCollection.getAgentCollectionID());
        a_GENESIS_MaleCollection = null;
        return true;
    }

    /**
     * Swaps to file the FemaleCollection a_GENESIS_FemaleCollection and returns
     * a HashSet<Long> identifying all FemaleCollections swapped in the process.
     *
     * @param a_GENESIS_FemaleCollection The FemaleCollection to be swapped to
     * disk
     * @param handleOutOfMemoryError
     * @return HashSet<Long> identifying all FemaleCollections swapped in the
     * process.
     */
    public Object[] swapToFile_FemaleCollection_AccountDetail(
            GENESIS_FemaleCollection a_GENESIS_FemaleCollection,
            boolean handleOutOfMemoryError) {
        try {
            Object[] result = swapToFile_FemaleCollection_AccountDetail(
                    a_GENESIS_FemaleCollection);
            try {
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult
                            = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                                    a_GENESIS_FemaleCollection);
                    if (!(Boolean) potentialPartResult[0]) {
                        throw new OutOfMemoryError();
                    }
                    combine(result, potentialPartResult);
                } else {
                    Object[] potentialPartResult
                            = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                                    a_GENESIS_FemaleCollection,
                                    handleOutOfMemoryError);
                    combine(result, potentialPartResult);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing
                // a_OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Object[] result
                        = swapToFile_FemaleCollection_AccountDetail(
                                a_GENESIS_FemaleCollection);
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult
                            = swapToFile_FemaleCollection_AccountDetail(
                                    a_GENESIS_FemaleCollection);
                    if (!(Boolean) potentialPartResult[0]) {
                        throw a_OutOfMemoryError;
                    } else {
                        combine(result, potentialPartResult);
                    }
                }
                Object[] potentialPartResult
                        = ge.AgentEnvironment.initMemoryReserve_AccountDetail(ge.HOOMEF);
                combine(result, potentialPartResult);
                potentialPartResult
                        = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                                a_GENESIS_FemaleCollection,
                                handleOutOfMemoryError);
                combine(result, potentialPartResult);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * Swaps to file the FemaleCollection a_GENESIS_FemaleCollection and returns
     * its ID in a HashSet.
     *
     * @param a_GENESIS_FemaleCollection The FemaleCollection to be swapped to
     * disk
     * @return HashSet<Long> containing the ID of a_GENESIS_FemaleCollection.
     */
    protected Object[] swapToFile_FemaleCollection_AccountDetail(
            GENESIS_FemaleCollection a_GENESIS_FemaleCollection) {
        if (a_GENESIS_FemaleCollection == null) {
            return null;
        } else {
            Object[] result = new Object[3];
            HashSet<Long> result1 = new HashSet<Long>(1);
            a_GENESIS_FemaleCollection.write();
            Long a_AgentCollection_ID
                    = a_GENESIS_FemaleCollection.getAgentCollectionID();
            LivingFemales.remove(a_AgentCollection_ID);
            a_GENESIS_FemaleCollection = null;
            result1.add(a_AgentCollection_ID);
            result[0] = true;
            result[1] = result1;
            return result;
        }
    }

    /**
     * Swaps to file the MaleCollection a_GENESIS_MaleCollection and returns a
     * HashSet<Long> identifying all MaleCollections swapped in the process.
     *
     * @param a_GENESIS_MaleCollection The MaleCollection to be swapped to disk
     * @param handleOutOfMemoryError
     * @return HashSet<Long> identifying all MaleCollections swapped in the
     * process.
     */
    public Object[] swapToFile_MaleCollection_AccountDetail(
            GENESIS_MaleCollection a_GENESIS_MaleCollection,
            boolean handleOutOfMemoryError) {
        try {
            Object[] result = swapToFile_MaleCollection_AccountDetail(
                    a_GENESIS_MaleCollection);
            try {
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult
                            = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                                    a_GENESIS_MaleCollection);
                    if (!(Boolean) potentialPartResult[0]) {
                        throw new OutOfMemoryError();
                    }
                    combine(result, potentialPartResult);
                } else {
                    Object[] potentialPartResult
                            = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                                    a_GENESIS_MaleCollection,
                                    handleOutOfMemoryError);
                    combine(result, potentialPartResult);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing
                // a_OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Object[] result
                        = swapToFile_MaleCollection_AccountDetail(
                                a_GENESIS_MaleCollection);
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult
                            = swapToFile_MaleCollection_AccountDetail(
                                    a_GENESIS_MaleCollection);
                    if (!(Boolean) potentialPartResult[0]) {
                        throw a_OutOfMemoryError;
                    } else {
                        combine(result, potentialPartResult);
                    }
                }
                Object[] potentialPartResult
                        = ge.AgentEnvironment.initMemoryReserve_AccountDetail(ge.HOOMEF);
                combine(result, potentialPartResult);
                potentialPartResult
                        = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                                a_GENESIS_MaleCollection,
                                handleOutOfMemoryError);
                combine(result, potentialPartResult);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * Swaps to file the MaleCollection a_GENESIS_MaleCollection and returns its
     * ID in a HashSet.
     *
     * @param a_GENESIS_MaleCollection The MaleCollection to be swapped to disk
     * @return HashSet<Long> containing the ID of a_GENESIS_MaleCollection.
     */
    protected Object[] swapToFile_MaleCollection_AccountDetail(
            GENESIS_MaleCollection a_GENESIS_MaleCollection) {
        if (a_GENESIS_MaleCollection == null) {
            return null;
        } else {
            Object[] result = new Object[3];
            HashSet<Long> result1 = new HashSet<Long>(1);
            a_GENESIS_MaleCollection.write();
            Long a_AgentCollection_ID
                    = a_GENESIS_MaleCollection.getAgentCollectionID();
            LivingMales.remove(a_AgentCollection_ID);
            a_GENESIS_MaleCollection = null;
            result1.add(a_AgentCollection_ID);
            result[0] = true;
            result[1] = result1;
            return result;
        }
    }

    /**
     * Attempts to swap all FemaleCollections.
     *
     * @param hoome If true then OutOfMemoryErrors are caught in this method
     * then swap operations are initiated prior to retrying. If false then
     * OutOfMemoryErrors are caught and thrown.
     */
    public void swapToFile_FemaleCollections(
            boolean hoome) {
        try {
            boolean success = swapToFile_FemaleCollections();
            try {
                ge.AgentEnvironment.checkAndMaybeFreeMemory();
            } catch (OutOfMemoryError e) {
                // Set hoome = false to exit method by throwing e
                hoome = false;
                throw e;
            }
        } catch (OutOfMemoryError e) {
            if (hoome) {
                ge.clearMemoryReserve();
                if (swapAgentCollection_Account() < 1) {
                    throw e;
                }
                ge.initMemoryReserve();
                swapToFile_FemaleCollections(hoome);
            } else {
                throw e;
            }
        }
    }

    protected boolean swapToFile_FemaleCollections() {
        Iterator<Long> a_Iterator = LivingFemales.keySet().iterator();
        Long a_AgentCollection_ID;
        GENESIS_FemaleCollection a_GENESIS_FemaleCollection;
        HashSet<Long> swapped_AgentCollectionID_HashSet = new HashSet<Long>();
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            a_GENESIS_FemaleCollection = LivingFemales.get(a_AgentCollection_ID);
            a_GENESIS_FemaleCollection.write();
            a_GENESIS_FemaleCollection = null;
            swapped_AgentCollectionID_HashSet.add(a_AgentCollection_ID);
        }
        a_Iterator = swapped_AgentCollectionID_HashSet.iterator();
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            LivingFemales.remove(a_AgentCollection_ID);
        }
        return true;
    }

    /**
     * Attempts to swap all MaleCollections.
     *
     * @param hoome If true then OutOfMemoryErrors are caught in this method
     * then swap operations are initiated prior to retrying. If false then
     * OutOfMemoryErrors are caught and thrown.
     */
    public void swapToFile_MaleCollections(
            boolean hoome) {
        try {
            boolean success = swapToFile_MaleCollections();
            try {
                ge.AgentEnvironment.checkAndMaybeFreeMemory();
            } catch (OutOfMemoryError e) {
                // Set hoome = false to exit method by throwing e
                hoome = false;
                throw e;
            }
        } catch (OutOfMemoryError e) {
            if (hoome) {
                ge.clearMemoryReserve();
                if (swapAgentCollection_Account() < 1) {
                    throw e;
                }
                ge.initMemoryReserve();
                swapToFile_MaleCollections(hoome);
            } else {
                throw e;
            }
        }
    }

    protected boolean swapToFile_MaleCollections() {
        Iterator<Long> a_Iterator = LivingMales.keySet().iterator();
        Long a_AgentCollection_ID;
        GENESIS_MaleCollection a_GENESIS_MaleCollection;
        HashSet<Long> swapped_AgentCollectionID_HashSet = new HashSet<Long>();
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            a_GENESIS_MaleCollection = LivingMales.get(a_AgentCollection_ID);
            a_GENESIS_MaleCollection.write();
            a_GENESIS_MaleCollection = null;
            swapped_AgentCollectionID_HashSet.add(a_AgentCollection_ID);
        }
        a_Iterator = swapped_AgentCollectionID_HashSet.iterator();
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            LivingMales.remove(a_AgentCollection_ID);
        }
        return true;
    }

    /**
     * Attempts to swap all AbstractAgent in this._Grid2DSquareCells.
     *
     * @param hoome If true then OutOfMemoryErrors are caught in this method
     * then swap operations are initiated prior to retrying. If false then
     * OutOfMemoryErrors are caught and thrown.
     */
    public void swapToFile_AgentCollections(
            boolean hoome) {
        try {
            boolean success = swapToFile_AgentCollections();
            try {
                ge.AgentEnvironment.checkAndMaybeFreeMemory();
            } catch (OutOfMemoryError e) {
                // Set hoome = false to exit method by throwing e
                hoome = false;
                throw e;
            }
        } catch (OutOfMemoryError e) {
            if (hoome) {
                ge.clearMemoryReserve();
                if (swapAgentCollection_Account() < 1) {
                    throw e;
                }
                ge.initMemoryReserve();
                swapToFile_AgentCollections(hoome);
            } else {
                throw e;
            }
        }
    }

    protected boolean swapToFile_AgentCollections() {
        swapToFile_FemaleCollections();
        swapToFile_MaleCollections();
        return true;
    }

    public long swapToFile_AgentCollections_Account(
            boolean handleOutOfMemoryError) {
        try {
            long result = swapToFile_AgentCollections_Account();
            try {
                if (result < 1) {
                    Object[] account = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account();
                    if ((Boolean) account[0]) {
                        result += (Long) account[1];
                    } else {
                        throw new OutOfMemoryError();
                    }
                } else {
                    result += ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            handleOutOfMemoryError);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                long result = swapToFile_AgentCollections_Account();
                if (result < 1) {
                    throw a_OutOfMemoryError;
                }
                result += ge.initMemoryReserve_AccountAgentCollections(ge.HOOMEF);
                result += ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                        handleOutOfMemoryError);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    protected long swapToFile_FemaleCollections_Account() {
        long result = 0L;
        Iterator<Long> a_Iterator = LivingFemales.keySet().iterator();
        Long a_AgentCollection_ID;
        GENESIS_FemaleCollection a_GENESIS_FemaleCollection;
        HashSet<Long> swapped_AgentCollectionID_HashSet = new HashSet<Long>();
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            a_GENESIS_FemaleCollection = LivingFemales.get(a_AgentCollection_ID);
            a_GENESIS_FemaleCollection.write();
            a_GENESIS_FemaleCollection = null;
            swapped_AgentCollectionID_HashSet.add(a_AgentCollection_ID);
            result++;
        }
        a_Iterator = swapped_AgentCollectionID_HashSet.iterator();
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            LivingFemales.remove(a_AgentCollection_ID);
        }
        return result;
    }

    protected long swapToFile_MaleCollections_Account() {
        long result = 0L;
        Iterator<Long> a_Iterator = LivingMales.keySet().iterator();
        Long a_AgentCollection_ID;
        GENESIS_MaleCollection a_GENESIS_MaleCollection;
        HashSet<Long> swapped_AgentCollectionID_HashSet = new HashSet<Long>();
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            a_GENESIS_MaleCollection = LivingMales.get(a_AgentCollection_ID);
            a_GENESIS_MaleCollection.write();
            a_GENESIS_MaleCollection = null;
            swapped_AgentCollectionID_HashSet.add(a_AgentCollection_ID);
            result++;
        }
        a_Iterator = swapped_AgentCollectionID_HashSet.iterator();
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            LivingMales.remove(a_AgentCollection_ID);
        }
        return result;
    }

    protected long swapToFile_AgentCollections_Account() {
        long result = swapToFile_FemaleCollections_Account();
        result += swapToFile_MaleCollections_Account();
        return result;
    }

    public Object[] swapToFile_FemaleCollections_AccountDetail(
            boolean handleOutOfMemoryError) {
        try {
            Object[] result = swapToFile_FemaleCollections_AccountDetail();
            try {
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail();
                    if (!(Boolean) potentialPartResult[0]) {
                        throw new OutOfMemoryError();
                    }
                    combine(result, potentialPartResult);
                } else {
                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                            handleOutOfMemoryError);
                    combine(result, potentialPartResult);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Object[] result = swapToFile_FemaleCollections_AccountDetail();
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult = swapToFile_MaleCollections_AccountDetail();
                    if (!(Boolean) potentialPartResult[0]) {
                        throw a_OutOfMemoryError;
                    }
                    combine(result, potentialPartResult);
                }
                Object[] potentialPartResult = ge.AgentEnvironment.initMemoryReserve_AccountDetail(ge.HOOMEF);
                combine(result, potentialPartResult);
                potentialPartResult = swapToFile_FemaleCollections_AccountDetail(
                        handleOutOfMemoryError);
                combine(result, potentialPartResult);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * result[0] is a Boolean with a value true if FemaleCollections are swapped
     * and false otherwise result[1] is a HashSet<Long> of the IDs of the
     * FemaleCollections swapped
     *
     * @return
     */
    protected Object[] swapToFile_FemaleCollections_AccountDetail() {
        Object[] result = new Object[3];
        HashSet<Long> femaleCollectionID_HashSet = new HashSet<Long>();
        Iterator<Long> a_Iterator = LivingFemales.keySet().iterator();
        Long a_AgentCollection_ID;
        GENESIS_FemaleCollection a_GENESIS_FemaleCollection;
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            a_GENESIS_FemaleCollection = LivingFemales.get(a_AgentCollection_ID);
            a_GENESIS_FemaleCollection.write();
            a_GENESIS_FemaleCollection = null;
            femaleCollectionID_HashSet.add(a_AgentCollection_ID);
        }
        if (femaleCollectionID_HashSet.isEmpty()) {
            result[0] = false;
        } else {
            result[0] = true;
            a_Iterator = femaleCollectionID_HashSet.iterator();
            while (a_Iterator.hasNext()) {
                a_AgentCollection_ID = (Long) a_Iterator.next();
                LivingFemales.remove(a_AgentCollection_ID);
            }
            result[1] = femaleCollectionID_HashSet;
        }
        return result;
    }

    public Object[] swapToFile_MaleCollections_AccountDetail(
            boolean handleOutOfMemoryError) {
        try {
            Object[] result = swapToFile_MaleCollections_AccountDetail();
            try {
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail();
                    if (!(Boolean) potentialPartResult[0]) {
                        throw new OutOfMemoryError();
                    }
                    combine(result, potentialPartResult);
                } else {
                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                            handleOutOfMemoryError);
                    combine(result, potentialPartResult);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Object[] result = swapToFile_MaleCollections_AccountDetail();
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult = swapToFile_FemaleCollections_AccountDetail();
                    if (!(Boolean) potentialPartResult[0]) {
                        throw a_OutOfMemoryError;
                    }
                    combine(result, potentialPartResult);
                }
                Object[] potentialPartResult = ge.AgentEnvironment.initMemoryReserve_AccountDetail(ge.HOOMEF);
                combine(result, potentialPartResult);
                potentialPartResult = swapToFile_MaleCollections_AccountDetail(
                        handleOutOfMemoryError);
                combine(result, potentialPartResult);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * result[0] is a Boolean with a value true if MaleCollections are swapped
     * and false otherwise result[2] is a HashSet<Long> of the IDs of the
     * MaleCollections swapped
     *
     * @return
     */
    protected Object[] swapToFile_MaleCollections_AccountDetail() {
        Object[] result = new Object[3];
        HashSet<Long> maleCollectionID_HashSet = new HashSet<Long>();
        Iterator<Long> a_Iterator = LivingMales.keySet().iterator();
        Long a_AgentCollection_ID;
        GENESIS_MaleCollection a_GENESIS_MaleCollection;
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            a_GENESIS_MaleCollection = LivingMales.get(a_AgentCollection_ID);
            a_GENESIS_MaleCollection.write();
            a_GENESIS_MaleCollection = null;
            maleCollectionID_HashSet.add(a_AgentCollection_ID);
        }
        if (maleCollectionID_HashSet.isEmpty()) {
            result[0] = false;
        } else {
            result[0] = true;
            a_Iterator = maleCollectionID_HashSet.iterator();
            while (a_Iterator.hasNext()) {
                a_AgentCollection_ID = (Long) a_Iterator.next();
                LivingMales.remove(a_AgentCollection_ID);
            }
            result[2] = maleCollectionID_HashSet;
        }
        return result;
    }

    public Object[] swapToFile_AgentCollections_AccountDetail(
            boolean handleOutOfMemoryError) {
        try {
            Object[] result = swapToFile_AgentCollections_AccountDetail();
            try {
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail();
                    if (!(Boolean) potentialPartResult[0]) {
                        throw new OutOfMemoryError();
                    }
                    combine(result, potentialPartResult);
                } else {
                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                            handleOutOfMemoryError);
                    combine(result, potentialPartResult);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Object[] result = swapToFile_AgentCollections_AccountDetail();
                if (!(Boolean) result[0]) {
                    throw a_OutOfMemoryError;
                }
                Object[] potentialPartResult = ge.AgentEnvironment.initMemoryReserve_AccountDetail(ge.HOOMEF);
                combine(result, potentialPartResult);
                potentialPartResult = swapToFile_AgentCollections_AccountDetail(
                        handleOutOfMemoryError);
                combine(result, potentialPartResult);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    protected Object[] swapToFile_AgentCollections_AccountDetail() {
        Object[] result = swapToFile_FemaleCollections_AccountDetail();
        Object[] partResult = swapToFile_MaleCollections_AccountDetail();
        combine(result, partResult);
        return result;
    }

//    public HashSet<Long> swapToFile_AgentCollectionExcept_AccountDetail(
//            Long a_AgentCollection_ID,
//            boolean handleOutOfMemoryError) {
//        try {
//            HashSet<Long> result = swapToFile_AgentCollectionExcept_AccountDetail(
//                    a_AgentCollection_ID);
//            try {
//                if (result.isEmpty()) {
//                    Object[] account = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
//                            a_AgentCollection_ID);
//                    if ((Boolean) account[0]) {
//                        result.addAll((HashSet<Long>) account[1]);
//                    } else {
//                        throw new OutOfMemoryError();
//                    }
//                } else {
//                    HashSet<Long> potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
//                    a_AgentCollection_ID,
//                            handleOutOfMemoryError);
//                    result.addAll(potentialPartResult);
//                }
//            } catch (OutOfMemoryError a_OutOfMemoryError) {
//                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
//                handleOutOfMemoryError = false;
//                throw a_OutOfMemoryError;
//            }
//            return result;
//        } catch (OutOfMemoryError a_OutOfMemoryError) {
//            if (handleOutOfMemoryError) {
//                ge.clearMemoryReserve();
//                HashSet<Long> result = swapToFile_AgentCollectionExcept_AccountDetail(
//                        a_AgentCollection_ID);
//                if (result.isEmpty()) {
//                    throw a_OutOfMemoryError;
//                }
//                HashSet<Long> potentialPartResult = ge.AgentEnvironment.initMemoryReserve_AccountDetail(
//                        ge.HOOMEF);
//                if (!potentialPartResult.isEmpty()) {
//                    result.addAll(potentialPartResult);
//                }
//                return result;
//            } else {
//                throw a_OutOfMemoryError;
//            }
//        }
//    }
    protected Object[] swapToFile_FemaleCollectionExcept_AccountDetail(
            Long a_AgentCollection_ID) {
        Object[] result = new Object[3];
        HashSet<Long> femaleCollectionID_HashSet = new HashSet<Long>(1);
        Iterator a_Iterator = LivingFemales.keySet().iterator();
        Long b_AgentCollection_ID = null;
        GENESIS_AgentCollection b_AgentCollection = null;
        while (a_Iterator.hasNext()) {
            b_AgentCollection_ID = (Long) a_Iterator.next();
            if (b_AgentCollection_ID != a_AgentCollection_ID) {
                b_AgentCollection = LivingFemales.get(a_AgentCollection_ID);
                b_AgentCollection.write();
                b_AgentCollection = null;
                femaleCollectionID_HashSet.add(b_AgentCollection_ID);
                break;
            }
        }
        LivingFemales.remove(b_AgentCollection_ID);
//        this._AgentCollection_HashMap.put(
//                b_AgentCollection_ID,
//                b_AgentCollection);
        return result;
    }

    public long swapToFile_FemaleCollectionsExcept_Account(
            GENESIS_FemaleCollection a_GENESIS_FemaleCollection,
            boolean handleOutOfMemoryError) {
        try {
            long result = swapToFile_FemaleCollectionsExcept_Account(
                    a_GENESIS_FemaleCollection);
            try {
                if (result < 1) {
                    Object[] account = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            a_GENESIS_FemaleCollection);
                    if ((Boolean) account[0]) {
                        result += (Long) account[1];
                    } else {
                        throw new OutOfMemoryError();
                    }
                } else {
                    result += ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            a_GENESIS_FemaleCollection,
                            handleOutOfMemoryError);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                long result = swapToFile_FemaleCollectionExcept_Account(
                        a_GENESIS_FemaleCollection);
                if (result < 1) {
                    throw a_OutOfMemoryError;
                }
                result += ge.AgentEnvironment.initMemoryReserve_Account(ge.HOOMEF);
                result += swapToFile_FemaleCollectionsExcept_Account(
                        a_GENESIS_FemaleCollection,
                        handleOutOfMemoryError);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    protected long swapToFile_FemaleCollectionsExcept_Account(
            GENESIS_FemaleCollection a_FemaleCollection) {
        long result = 0L;
        HashSet<Long> swapped_AgentCollections = new HashSet<Long>();
        Iterator<Long> a_Iterator = LivingFemales.keySet().iterator();
        long a_AgentCollection_ID;
        GENESIS_AgentCollection b_FemaleCollection;
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            b_FemaleCollection = LivingFemales.get(
                    a_AgentCollection_ID);
            if (b_FemaleCollection != a_FemaleCollection) {
                b_FemaleCollection.write();
                b_FemaleCollection = null;
                swapped_AgentCollections.add(a_AgentCollection_ID);
                result++;
            }
        }
        a_Iterator = swapped_AgentCollections.iterator();
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = (Long) a_Iterator.next();
            LivingFemales.remove(a_AgentCollection_ID);
        }
        return result;
    }

    public long swapToFile_MaleCollectionsExcept_Account(
            GENESIS_MaleCollection a_GENESIS_MaleCollection,
            boolean handleOutOfMemoryError) {
        try {
            long result = swapToFile_MaleCollectionsExcept_Account(
                    a_GENESIS_MaleCollection);
            try {
                if (result < 1) {
                    Object[] account = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            a_GENESIS_MaleCollection);
                    if ((Boolean) account[0]) {
                        result += (Long) account[1];
                    } else {
                        throw new OutOfMemoryError();
                    }
                } else {
                    result += ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            a_GENESIS_MaleCollection,
                            handleOutOfMemoryError);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                long result = swapToFile_MaleCollectionExcept_Account(
                        a_GENESIS_MaleCollection);
                if (result < 1) {
                    throw a_OutOfMemoryError;
                }
                result += ge.AgentEnvironment.initMemoryReserve_Account(ge.HOOMEF);
                result += swapToFile_MaleCollectionsExcept_Account(
                        a_GENESIS_MaleCollection,
                        handleOutOfMemoryError);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    protected long swapToFile_MaleCollectionsExcept_Account(
            GENESIS_MaleCollection a_MaleCollection) {
        long result = 0L;
        HashSet<Long> swapped_AgentCollections = new HashSet<Long>();
        Iterator<Long> a_Iterator = LivingMales.keySet().iterator();
        long a_AgentCollection_ID;
        GENESIS_AgentCollection b_MaleCollection;
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = a_Iterator.next();
            b_MaleCollection = LivingMales.get(
                    a_AgentCollection_ID);
            if (b_MaleCollection != a_MaleCollection) {
                b_MaleCollection.write();
                b_MaleCollection = null;
                swapped_AgentCollections.add(a_AgentCollection_ID);
                result++;
            }
        }
        a_Iterator = swapped_AgentCollections.iterator();
        while (a_Iterator.hasNext()) {
            a_AgentCollection_ID = (Long) a_Iterator.next();
            LivingMales.remove(a_AgentCollection_ID);
        }
        return result;
    }

    public Object[] swapToFile_FemaleCollectionsExcept_AccountDetail(
            GENESIS_FemaleCollection a_GENESIS_FemaleCollection,
            boolean handleOutOfMemoryError) {
        try {
            Object[] result = swapToFile_FemaleCollectionsExcept_AccountDetail(
                    a_GENESIS_FemaleCollection);
            try {
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                            a_GENESIS_FemaleCollection);
                    if (!(Boolean) potentialPartResult[0]) {
                        throw new OutOfMemoryError();
                    }
                    combine(result, potentialPartResult);
                } else {
                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                            a_GENESIS_FemaleCollection,
                            handleOutOfMemoryError);
                    combine(result, potentialPartResult);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                Object[] result = swapToFile_FemaleCollectionExcept_AccountDetail(
                        a_GENESIS_FemaleCollection);
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult = swapToFile_MaleCollection_AccountDetail(ge.HOOMEF);
                    if (!(Boolean) potentialPartResult[0]) {
                        throw a_OutOfMemoryError;
                    }
                }
                Object[] potentialPartResult
                        = ge.AgentEnvironment.initMemoryReserve_AccountDetail(
                                handleOutOfMemoryError);
                combine(result, potentialPartResult);
                potentialPartResult = swapToFile_FemaleCollectionsExcept_AccountDetail(
                        a_GENESIS_FemaleCollection,
                        handleOutOfMemoryError);
                combine(result, potentialPartResult);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    protected Object[] swapToFile_FemaleCollectionsExcept_AccountDetail(
            GENESIS_FemaleCollection fc) {
        Object[] result = new Object[3];
        HashSet<Long> swappedFemaleCollectionID_HashSet = new HashSet<Long>();
        Iterator a_Iterator = LivingFemales.keySet().iterator();
        long fcID;
        GENESIS_FemaleCollection fc2;
        while (a_Iterator.hasNext()) {
            fcID = (Long) a_Iterator.next();
            fc2 = LivingFemales.get(
                    fcID);
            if (fc2 != fc) {
                fc2.write();
                fc2 = null;
                swappedFemaleCollectionID_HashSet.add(fcID);
            }
        }
        if (swappedFemaleCollectionID_HashSet.size() > 0) {
        }
        a_Iterator = swappedFemaleCollectionID_HashSet.iterator();
        while (a_Iterator.hasNext()) {
            fcID = (Long) a_Iterator.next();
            LivingFemales.remove(fcID);
        }
        return result;
    }

    public Object[] swapToFile_MaleCollectionsExcept_AccountDetail(
            GENESIS_MaleCollection mc,
            boolean hoome) {
        try {
            Object[] result = swapToFile_MaleCollectionsExcept_AccountDetail(mc);
            try {
                if (!(Boolean) result[0]) {
                    Object[] ppr = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                            mc);
                    if (!(Boolean) ppr[0]) {
                        throw new OutOfMemoryError();
                    }
                    combine(result, ppr);
                } else {
                    Object[] ppr = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
                            mc,
                            hoome);
                    combine(result, ppr);
                }
            } catch (OutOfMemoryError e) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                hoome = false;
                throw e;
            }
            return result;
        } catch (OutOfMemoryError e) {
            if (hoome) {
                ge.clearMemoryReserve();
                Object[] result = swapToFile_MaleCollectionExcept_AccountDetail(
                        mc);
                if (!(Boolean) result[0]) {
                    Object[] potentialPartResult = swapToFile_MaleCollection_AccountDetail(ge.HOOMEF);
                    if (!(Boolean) potentialPartResult[0]) {
                        throw e;
                    }
                }
                Object[] potentialPartResult
                        = ge.AgentEnvironment.initMemoryReserve_AccountDetail(
                                hoome);
                combine(result, potentialPartResult);
                potentialPartResult = swapToFile_MaleCollectionsExcept_AccountDetail(
                        mc,
                        hoome);
                combine(result, potentialPartResult);
                return result;
            } else {
                throw e;
            }
        }
    }

    protected Object[] swapToFile_MaleCollectionsExcept_AccountDetail(
            GENESIS_MaleCollection a_MaleCollection) {
        Object[] result = new Object[3];
        HashSet<Long> swappedMaleCollectionID_HashSet = new HashSet<Long>();
        Iterator a_Iterator = LivingMales.keySet().iterator();
        long a_MaleCollection_ID;
        GENESIS_MaleCollection b_MaleCollection;
        while (a_Iterator.hasNext()) {
            a_MaleCollection_ID = (Long) a_Iterator.next();
            b_MaleCollection = LivingMales.get(
                    a_MaleCollection_ID);
            if (b_MaleCollection != a_MaleCollection) {
                b_MaleCollection.write();
                b_MaleCollection = null;
                swappedMaleCollectionID_HashSet.add(a_MaleCollection_ID);
            }
        }
        if (swappedMaleCollectionID_HashSet.size() > 0) {
        }
        a_Iterator = swappedMaleCollectionID_HashSet.iterator();
        while (a_Iterator.hasNext()) {
            a_MaleCollection_ID = (Long) a_Iterator.next();
            LivingMales.remove(a_MaleCollection_ID);
        }
        return result;
    }

//    public Object[] swapToFile_FemaleCollectionsExcept_AccountDetail(
//            GENESIS_FemaleCollection a_GENESIS_FemaleCollection,
//            boolean handleOutOfMemoryError) {
//        try {
//            Object[] result = swapToFile_FemaleCollectionsExcept_AccountDetail(
//                    a_GENESIS_FemaleCollection);
//            try {
//                if ((Boolean) result[0]) {
//                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
//                            a_GENESIS_FemaleCollection,
//                            handleOutOfMemoryError);
//                    combine(result, potentialPartResult);
//                } else {
//                    Object[] potentialPartResult = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_AccountDetail(
//                            a_GENESIS_FemaleCollection);
//                    if (!(Boolean) potentialPartResult[0]) {
//                        throw new OutOfMemoryError();
//                    }
//                    combine(result, potentialPartResult);
//                }
//            } catch (OutOfMemoryError a_OutOfMemoryError) {
//                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
//                handleOutOfMemoryError = false;
//                throw a_OutOfMemoryError;
//            }
//            return result;
//        } catch (OutOfMemoryError a_OutOfMemoryError) {
//            if (handleOutOfMemoryError) {
//                ge.clearMemoryReserve();
//                Object[] result = swapToFile_FemaleCollectionExcept_AccountDetail(
//                        a_GENESIS_FemaleCollection);
//                if ((Boolean) result[0]) {
//                    Object[] potentialPartResult = swapToFile_MaleCollection_AccountDetail();
//                    if (!(Boolean) potentialPartResult[0]) {
//                        throw a_OutOfMemoryError;
//                    }
//                    combine(result, potentialPartResult);
//                }
//                Object[] potentialPartResult = ge.AgentEnvironment.initMemoryReserve_AccountDetail(
//                        handleOutOfMemoryError);
//                combine(result, potentialPartResult);
//                potentialPartResult = swapToFile_FemaleCollectionsExcept_AccountDetail(
//                        a_GENESIS_FemaleCollection,
//                        handleOutOfMemoryError);
//                combine(result, potentialPartResult);
//                return result;
//            } else {
//                throw a_OutOfMemoryError;
//            }
//        }
//    }
    protected Object[] swapToFile_FemaleCollectionExcept_AccountDetail(
            GENESIS_FemaleCollection a_FemaleCollection) {
        Object[] result = new Object[3];
        HashSet<Long> swapedFemaleCollectionID_HashSet = new HashSet<Long>(1);
        Iterator a_Iterator = this.LivingFemales.keySet().iterator();
        long a_FemaleCollection_ID;
        GENESIS_FemaleCollection b_FemaleCollection;
        while (a_Iterator.hasNext()) {
            a_FemaleCollection_ID = (Long) a_Iterator.next();
            b_FemaleCollection
                    = (GENESIS_FemaleCollection) this.LivingFemales.get(a_FemaleCollection_ID);
            if (b_FemaleCollection != a_FemaleCollection) {
                b_FemaleCollection.write();
                b_FemaleCollection = null;
                swapedFemaleCollectionID_HashSet.add(a_FemaleCollection_ID);
                break;
            }
        }
        if (swapedFemaleCollectionID_HashSet.size() > 0) {
            a_Iterator = swapedFemaleCollectionID_HashSet.iterator();
            while (a_Iterator.hasNext()) {
                a_FemaleCollection_ID = (Long) a_Iterator.next();
                this.LivingFemales.remove(
                        a_FemaleCollection_ID);
            }
            result[0] = true;
            result[1] = swapedFemaleCollectionID_HashSet;
        } else {
            result[0] = false;
        }
        return result;
    }

    protected Object[] swapToFile_MaleCollectionExcept_AccountDetail(
            GENESIS_MaleCollection a_MaleCollection) {
        Object[] result = new Object[3];
        HashSet<Long> swapedMaleCollectionID_HashSet = new HashSet<Long>(1);
        Iterator a_Iterator = this.LivingMales.keySet().iterator();
        long a_MaleCollection_ID;
        GENESIS_MaleCollection b_MaleCollection;
        while (a_Iterator.hasNext()) {
            a_MaleCollection_ID = (Long) a_Iterator.next();
            b_MaleCollection
                    = (GENESIS_MaleCollection) this.LivingMales.get(a_MaleCollection_ID);
            if (b_MaleCollection != a_MaleCollection) {
                b_MaleCollection.write();
                b_MaleCollection = null;
                swapedMaleCollectionID_HashSet.add(a_MaleCollection_ID);
                break;
            }
        }
        if (swapedMaleCollectionID_HashSet.size() > 0) {
            a_Iterator = swapedMaleCollectionID_HashSet.iterator();
            while (a_Iterator.hasNext()) {
                a_MaleCollection_ID = (Long) a_Iterator.next();
                this.LivingMales.remove(
                        a_MaleCollection_ID);
            }
            result[0] = true;
            result[1] = swapedMaleCollectionID_HashSet;
        } else {
            result[0] = false;
        }
        return result;
    }

    public long swapToFile_FemaleCollectionExcept_Account(
            GENESIS_FemaleCollection a_GENESIS_FemaleCollection,
            boolean handleOutOfMemoryError) {
        try {
            long result = swapToFile_FemaleCollectionExcept_Account(
                    a_GENESIS_FemaleCollection);
            try {
                if (result < 1) {
                    Object[] account = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            a_GENESIS_FemaleCollection);
                    if ((Boolean) account[0]) {
                        result += (Long) account[1];
                    } else {
                        throw new OutOfMemoryError();
                    }
                } else {
                    result += ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            a_GENESIS_FemaleCollection,
                            handleOutOfMemoryError);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                if (swapToFile_FemaleCollectionExcept_Account(
                        a_GENESIS_FemaleCollection) < 1) {
                    throw a_OutOfMemoryError;
                }
                long result = 1L;
                result += ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                        a_GENESIS_FemaleCollection,
                        handleOutOfMemoryError);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * @param a_FemaleCollection
     * @return 0 or 1
     */
    protected long swapToFile_FemaleCollectionExcept_Account(
            GENESIS_FemaleCollection a_FemaleCollection) {
        long result = 0;
        Iterator<Long> a_Iterator = LivingFemales.keySet().iterator();
        Long a_FemaleCollection_ID = 0L;
        GENESIS_FemaleCollection b_FemaleCollection;
        while (a_Iterator.hasNext()) {
            a_FemaleCollection_ID = a_Iterator.next();
            b_FemaleCollection = LivingFemales.get(
                    a_FemaleCollection_ID);
            if (b_FemaleCollection != a_FemaleCollection) {
                b_FemaleCollection.write();
                b_FemaleCollection = null;
                result++;
                break;
            }
        }
        if (result == 1) {
            LivingFemales.remove(
                    a_FemaleCollection_ID);
        }
        return result;
    }

    public long swapToFile_MaleCollectionExcept_Account(
            GENESIS_MaleCollection a_GENESIS_MaleCollection,
            boolean handleOutOfMemoryError) {
        try {
            long result = swapToFile_MaleCollectionExcept_Account(
                    a_GENESIS_MaleCollection);
            try {
                if (result < 1) {
                    Object[] account = ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            a_GENESIS_MaleCollection);
                    if ((Boolean) account[0]) {
                        result += (Long) account[1];
                    } else {
                        throw new OutOfMemoryError();
                    }
                } else {
                    result += ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                            a_GENESIS_MaleCollection,
                            handleOutOfMemoryError);
                }
            } catch (OutOfMemoryError a_OutOfMemoryError) {
                // Set handleOutOfMemoryError = false to exit method by throwing OutOfMemoryError
                handleOutOfMemoryError = false;
                throw a_OutOfMemoryError;
            }
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (handleOutOfMemoryError) {
                ge.clearMemoryReserve();
                if (swapToFile_MaleCollectionExcept_Account(
                        a_GENESIS_MaleCollection) < 1) {
                    throw a_OutOfMemoryError;
                }
                long result = 1L;
                result += ge.AgentEnvironment.tryToEnsureThereIsEnoughMemoryToContinue_Account(
                        a_GENESIS_MaleCollection,
                        handleOutOfMemoryError);
                return result;
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    /**
     * @param a_MaleCollection
     * @return 0 or 1
     */
    protected long swapToFile_MaleCollectionExcept_Account(
            GENESIS_MaleCollection a_MaleCollection) {
        long result = 0;
        Iterator<Long> a_Iterator = LivingMales.keySet().iterator();
        Long a_MaleCollection_ID = 0L;
        GENESIS_MaleCollection b_MaleCollection;
        while (a_Iterator.hasNext()) {
            a_MaleCollection_ID = a_Iterator.next();
            b_MaleCollection = LivingMales.get(
                    a_MaleCollection_ID);
            if (b_MaleCollection != a_MaleCollection) {
                b_MaleCollection.write();
                b_MaleCollection = null;
                result++;
                break;
            }
        }
        if (result == 1) {
            LivingMales.remove(
                    a_MaleCollection_ID);
        }
        return result;
    }

    private static void log(
            String message) {
        log(GENESIS_Log.GENESIS_DefaultLogLevel, message);
    }

    private static void log(
            Level level,
            String message) {
        Logger.getLogger(GENESIS_Log.DefaultLoggerName).log(level, message);
    }
}
