package uk.ac.leeds.ccg.andyt.projects.genesis.core;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.andyt.generic.io.Generic_IO;
import uk.ac.leeds.ccg.andyt.projects.genesis.logging.GENESIS_Log;
import uk.ac.leeds.ccg.andyt.projects.genesis.society.demography.GENESIS_Age;
import uk.ac.leeds.ccg.andyt.projects.genesis.society.organisations.GENESIS_Family;
import uk.ac.leeds.ccg.andyt.projects.genesis.society.organisations.GENESIS_Household;
import uk.ac.leeds.ccg.andyt.vector.geometry.Vector_Point2D;

public class GENESIS_Male extends GENESIS_Person {

    /**
     * The main collection within which this is stored.
     */
    protected transient GENESIS_MaleCollection MaleCollection;

    private GENESIS_Male() {
    }

    protected GENESIS_Male(
            GENESIS_Environment ge,
            GENESIS_AgentCollectionManager acm,
            GENESIS_Age age) {
        this(ge,
                acm,
                null,
                age,
                null,
                null);
    }

    protected GENESIS_Male(
            GENESIS_Environment ge,
            GENESIS_AgentCollectionManager acm,
            GENESIS_Age age,
            GENESIS_Household household) {
        this(ge,
                acm,
                null,
                age,
                household,
                household._Point2D);
    }

    protected GENESIS_Male(
            GENESIS_Environment ge,
            GENESIS_AgentCollectionManager acm,
            GENESIS_Age age,
            GENESIS_Household household,
            Vector_Point2D point2D) {
        this(ge,
                acm,
                null,
                age,
                household,
                point2D);
    }

    protected GENESIS_Male(
            GENESIS_Environment ge,
            GENESIS_AgentCollectionManager acm,
            File directory,
            GENESIS_Age age,
            GENESIS_Household household,
            Vector_Point2D point2D) {
        super(ge);
        init(acm,
                directory,
                age,
                household,
                point2D);
    }

    /**
     *
     * @param acm
     * @param directory
     * @param age
     * @param household
     * @param point2D
     */
    protected final void init(
            GENESIS_AgentCollectionManager acm,
            File directory,
            GENESIS_Age age,
            GENESIS_Household household,
            Vector_Point2D point2D) {
        LogManager.getLogManager().addLogger(Logger.getLogger(GENESIS_Log.DefaultLoggerName));
        AgentCollectionManager = acm;
        ID = acm.get_NextMaleID(ge.HOOME);
        Type = getTypeLivingMale_String();
        CollectionID = acm.getMaleCollection_ID(ID,
                Type,
                ge.HOOME);
        Generic_IO.addToArchive(AgentCollectionManager.getLivingMaleDirectory(),
                AgentCollectionManager.MaximumNumberOfObjectsPerDirectory,
                CollectionID);
        this.MaleCollection = get_MaleCollection();
        this.MaleCollection.getAgentID_Agent_Map().put(ID, this);
        this._Directory = directory;
        this.Age = new GENESIS_Age(ge, age);
        this._Family = new GENESIS_Family(this);
        if (_Household != null) {
            this._Household = household;
            this._Household._Add_Person(this);
        }
        if (Location != null) {
            this.Location = point2D;
            this.PreviousPoint2D = new Vector_Point2D(point2D);
        }
        this.ResidentialSubregionIDs = new ArrayList<>();
    }

    /**
     * @return A copy of this._Agent_ID
     */
    protected Long get_Male_ID() {
        return ID;
    }

    @Override
    public Long getAgentID(boolean hoome) {
        try {
            Long result = get_Male_ID();
            GENESIS_MaleCollection mc = get_MaleCollection();
            ge.tryToEnsureThereIsEnoughMemoryToContinue(mc, hoome);
            return result;
        } catch (OutOfMemoryError e) {
            if (hoome) {
                ge.clearMemoryReserve();
                ge.swapDataAnyExcept(get_MaleCollection());
                ge.initMemoryReserve();
                return getAgentID(hoome);
            } else {
                throw e;
            }
        }
    }

    @Override
    protected GENESIS_AgentCollection get_AgentCollection() {
        return get_MaleCollection();
    }

    public GENESIS_MaleCollection get_MaleCollection(
            boolean hoome) {
        try {
            GENESIS_MaleCollection result = get_MaleCollection();
            ge.tryToEnsureThereIsEnoughMemoryToContinue(result, hoome);
            return result;
        } catch (OutOfMemoryError e) {
            if (hoome) {
                ge.clearMemoryReserve();
                if (ge.AgentEnvironment.AgentCollectionManager.swapAgentCollection_Account() < 1) {
                    ge.swapChunk();
                }
                ge.initMemoryReserve();
                return get_MaleCollection(hoome);
            } else {
                throw e;
            }
        }
    }

    protected GENESIS_MaleCollection get_MaleCollection() {
        if (MaleCollection == null) {
            MaleCollection = getAgentCollectionManager().getMaleCollection(CollectionID,
                    Type,
                    ge.HOOMEF);
        }
        if (MaleCollection.ge == null) {
            MaleCollection.ge = ge;
        }
        return MaleCollection;
    }

    public Long get_MaleCollection_ID(boolean hoome) {
        try {
            Long result = get_MaleCollection_ID();
            ge.tryToEnsureThereIsEnoughMemoryToContinue(get_MaleCollection(), hoome);
            return result;
        } catch (OutOfMemoryError e) {
            if (hoome) {
                ge.clearMemoryReserve();
                ge.swapDataAnyExcept(get_MaleCollection());
                ge.initMemoryReserve();
                return get_MaleCollection_ID(hoome);
            } else {
                throw e;
            }
        }
    }

    protected Long get_MaleCollection_ID() {
        return getAgentCollectionManager().getAgentCollection_ID(get_Male_ID());
    }

    @Override
    public int getGender(boolean hoome) {
        try {
            int result = getGender();
            ge.checkAndMaybeFreeMemory();
            return result;
        } catch (OutOfMemoryError a_OutOfMemoryError) {
            if (hoome) {
                ge.clearMemoryReserve();
                ge.swapDataAnyExcept(                        get_MaleCollection());
                ge.initMemoryReserve();
                return getGender(hoome);
            } else {
                throw a_OutOfMemoryError;
            }
        }
    }

    @Override
    protected int getGender() {
        return 1;
    }

//    public static GENESIS_Male read(
//            long a_Agent_ID,
//            GENESIS_MaleCollection a_GENESIS_MaleCollection) {
//        GENESIS_Male result;
//        AgentCollectionManager a_GENESIS_AgentCollectionManager =
//                a_GENESIS_MaleCollection.getAgentCollectionManager();
//        File a_MaleDirectory_File = Generic_IO.getObjectDirectory(
//                a_GENESIS_AgentCollectionManager._Male_Directory,
//                a_Agent_ID,
//                a_GENESIS_AgentCollectionManager.IndexOfLastBornMale,
//                a_GENESIS_AgentCollectionManager.MaximumNumberOfObjectsPerDirectory);
//        File a_Male_File = new File(
//                a_MaleDirectory_File,
//                GENESIS_Male.class.getCanonicalName() + ".thisFile");
//        return (GENESIS_Male) Generic_IO.readObject(a_Male_File);
//    }
    @Deprecated
    @Override
    public void write(boolean hoome) {
        try {
            write();
        } catch (OutOfMemoryError e) {
            if (hoome) {
                ge.clearMemoryReserve();
                ge.swapDataAnyExcept(
                        get_MaleCollection());
                ge.initMemoryReserve();
                write(hoome);
            } else {
                throw e;
            }
        }
    }

    @Deprecated
    @Override
    protected void write() {
        File thisAgentFile = new File(
                getDirectory(),
                this.getClass().getCanonicalName() + ".thisFile");
        if (!_Directory.exists()) {
            _Directory.mkdirs();
        }
        Generic_IO.writeObject(
                this,
                thisAgentFile);
    }

    @Override
    protected File getDirectory() {
        if (_Directory == null) {
            _Directory = AgentCollectionManager.getLivingMaleDirectory();
        }
        return _Directory;
    }
}
