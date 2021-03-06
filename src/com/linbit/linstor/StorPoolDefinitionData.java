package com.linbit.linstor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.UUID;

import com.linbit.ImplementationError;
import com.linbit.SatelliteTransactionMgr;
import com.linbit.TransactionMap;
import com.linbit.TransactionMgr;
import com.linbit.TransactionObject;
import com.linbit.TransactionSimpleObject;
import com.linbit.linstor.api.pojo.StorPoolDfnPojo;
import com.linbit.linstor.core.LinStor;
import com.linbit.linstor.dbdrivers.interfaces.StorPoolDefinitionDataDatabaseDriver;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.propscon.PropsAccess;
import com.linbit.linstor.propscon.PropsContainer;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.AccessType;
import com.linbit.linstor.security.ObjectProtection;

public class StorPoolDefinitionData extends BaseTransactionObject implements StorPoolDefinition
{
    private final UUID uuid;

    // Runtime instance identifier for debug purposes
    private final transient UUID dbgInstanceId;

    private final StorPoolName name;
    private final ObjectProtection objProt;
    private final StorPoolDefinitionDataDatabaseDriver dbDriver;
    private final TransactionMap<NodeName, StorPool> storPools;
    private final Props props;
    private final TransactionSimpleObject<StorPoolDefinitionData, Boolean> deleted;

    /**
     * Constructor used by {@link StorPoolDefinition#getInstance(AccessContext, StorPoolName, TransactionMgr, boolean)}
     *
     * @param accCtx
     * @param nameRef
     * @param transMgr
     * @throws AccessDeniedException
     * @throws SQLException
     */
    StorPoolDefinitionData(AccessContext accCtx, StorPoolName nameRef, TransactionMgr transMgr)
        throws AccessDeniedException, SQLException
    {
        this(
            UUID.randomUUID(),
            ObjectProtection.getInstance(
                accCtx,
                ObjectProtection.buildPathSPD(nameRef),
                true,
                transMgr
            ),
            nameRef
        );
    }

    /**
     * Constructor used by other Constructor as well as from the DerbyDriver for
     * restoring the UUID and the ObjectProtection
     *
     * @param accCtx
     * @param nameRef
     * @param transMgr
     * @param id
     * @throws AccessDeniedException
     * @throws SQLException
     */
    StorPoolDefinitionData(UUID id, ObjectProtection objProtRef, StorPoolName nameRef)
        throws SQLException
    {
        uuid = id;
        dbgInstanceId = UUID.randomUUID();
        objProt = objProtRef;
        name = nameRef;
        storPools = new TransactionMap<>(new TreeMap<NodeName, StorPool>(), null);

        props = PropsContainer.getInstance(
            PropsContainer.buildPath(nameRef),
            transMgr
        );
        deleted = new TransactionSimpleObject<>(this, false, null);

        dbDriver = LinStor.getStorPoolDefinitionDataDatabaseDriver();

        transObjs = Arrays.<TransactionObject>asList(
            objProt,
            storPools,
            props,
            deleted
        );
    }

    @Override
    public UUID debugGetVolatileUuid()
    {
        return dbgInstanceId;
    }

    public static StorPoolDefinitionData getInstance(
        AccessContext accCtx,
        StorPoolName nameRef,
        TransactionMgr transMgr,
        boolean createIfNotExists,
        boolean failIfExists
    )
        throws AccessDeniedException, SQLException, LinStorDataAlreadyExistsException
    {
        StorPoolDefinitionData storPoolDfn = null;

        StorPoolDefinitionDataDatabaseDriver dbDriver = LinStor.getStorPoolDefinitionDataDatabaseDriver();
        storPoolDfn = dbDriver.load(nameRef, false, transMgr);

        if (failIfExists && storPoolDfn != null)
        {
            throw new LinStorDataAlreadyExistsException("The StorPoolDefinition already exists");
        }

        if (storPoolDfn == null && createIfNotExists)
        {
            storPoolDfn = new StorPoolDefinitionData(accCtx, nameRef, transMgr);

            dbDriver.create(storPoolDfn, transMgr);
        }

        if (storPoolDfn != null)
        {
            storPoolDfn.initialized();
            storPoolDfn.setConnection(transMgr);
        }

        return storPoolDfn;
    }

    public static StorPoolDefinitionData getInstanceSatellite(
        AccessContext accCtx,
        UUID uuid,
        StorPoolName nameRef,
        SatelliteTransactionMgr transMgr
    )
        throws ImplementationError
    {
        StorPoolDefinitionData storPoolDfn = null;

        StorPoolDefinitionDataDatabaseDriver dbDriver = LinStor.getStorPoolDefinitionDataDatabaseDriver();
        try
        {
            storPoolDfn = dbDriver.load(nameRef, false, transMgr);
            if (storPoolDfn == null)
            {
                storPoolDfn = new StorPoolDefinitionData(
                    uuid,
                    ObjectProtection.getInstance(
                        accCtx,
                        "",
                        true,
                        transMgr
                    ),
                    nameRef
                );
            }
            storPoolDfn.initialized();
            storPoolDfn.setConnection(transMgr);
        }
        catch (Exception exc)
        {
            throw new ImplementationError(
                "This method should only be called with a satellite db in background!",
                exc
            );
        }
        return storPoolDfn;
    }

    @Override
    public UUID getUuid()
    {
        checkDeleted();
        return uuid;
    }

    @Override
    public ObjectProtection getObjProt()
    {
        checkDeleted();
        return objProt;
    }

    @Override
    public StorPoolName getName()
    {
        checkDeleted();
        return name;
    }

    @Override
    public Iterator<StorPool> iterateStorPools(AccessContext accCtx)
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);
        return storPools.values().iterator();
    }

    void addStorPool(AccessContext accCtx, StorPoolData storPoolData) throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.USE);
        storPools.put(storPoolData.getNode().getName(), storPoolData);
    }

    void removeStorPool(AccessContext accCtx, StorPoolData storPoolData) throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.USE);
        storPools.remove(storPoolData.getNode().getName());
    }

    @Override
    public StorPool getStorPool(AccessContext accCtx, NodeName nodeName) throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);
        return storPools.get(nodeName);
    }

    @Override
    public Props getProps(AccessContext accCtx) throws AccessDeniedException
    {
        checkDeleted();
        return PropsAccess.secureGetProps(accCtx, objProt, props);
    }

    @Override
    public void delete(AccessContext accCtx)
        throws AccessDeniedException, SQLException
    {
        if (!deleted.get())
        {
            objProt.requireAccess(accCtx, AccessType.CONTROL);

            Collection<StorPool> values = new ArrayList<>(storPools.values());
            for(StorPool storPool : values)
            {
                storPool.delete(accCtx);;
            }

            objProt.delete(accCtx);
            dbDriver.delete(this, transMgr);

            deleted.set(true);
        }
    }

    @Override
    public StorPoolDfnApi getApiData(AccessContext accCtx) throws AccessDeniedException {
        return new StorPoolDfnPojo(getUuid(), getName().getDisplayName(), getProps(accCtx).map());
    }

    private void checkDeleted()
    {
        if (deleted.get())
        {
            throw new ImplementationError("Access to deleted storage pool definition", null);
        }
    }

    @Override
    public String toString()
    {
        return "StorPool: '" + name + "'";
    }
}
