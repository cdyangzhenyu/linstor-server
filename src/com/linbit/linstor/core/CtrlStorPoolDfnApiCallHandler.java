package com.linbit.linstor.core;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import com.linbit.ImplementationError;
import com.linbit.TransactionMgr;
import com.linbit.linstor.LinStorDataAlreadyExistsException;
import com.linbit.linstor.StorPool;
import com.linbit.linstor.StorPoolDefinition;
import com.linbit.linstor.StorPoolDefinitionData;
import com.linbit.linstor.StorPoolName;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.interfaces.serializer.CtrlClientSerializer;
import com.linbit.linstor.api.interfaces.serializer.CtrlStltSerializer;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.AccessType;

class CtrlStorPoolDfnApiCallHandler extends AbsApiCallHandler
{
    private final ThreadLocal<String> currentStorPoolNameStr = new ThreadLocal<>();
    private final CtrlClientSerializer clientComSerializer;

    CtrlStorPoolDfnApiCallHandler(
        ApiCtrlAccessors apiCtrlAccessorsRef,
        CtrlStltSerializer interComSerializer,
        CtrlClientSerializer clientComSerializerRef,
        AccessContext apiCtxRef
    )
    {
        super(
            apiCtrlAccessorsRef,
            apiCtxRef,
            ApiConsts.MASK_STOR_POOL_DFN,
            interComSerializer
        );
        super.setNullOnAutoClose(currentStorPoolNameStr);
        clientComSerializer = clientComSerializerRef;
    }

    public ApiCallRc createStorPoolDfn(
        AccessContext accCtx,
        Peer client,
        String storPoolNameStr,
        Map<String, String> storPoolDfnProps
    )
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();

        try (
            AbsApiCallHandler basicallyThis = setContext(
                accCtx,
                client,
                ApiCallType.CREATE,
                apiCallRc,
                null, // create new transMgr
                storPoolNameStr
            );
        )
        {
            requireStorPoolDfnChangeAccess();

            StorPoolDefinitionData storPoolDfn = createStorPool(storPoolNameStr);
            getProps(storPoolDfn).map().putAll(storPoolDfnProps);
            commit();

            apiCtrlAccessors.getStorPoolDfnMap().put(storPoolDfn.getName(), storPoolDfn);
            reportSuccess(storPoolDfn.getUuid());
        }
        catch (ApiCallHandlerFailedException ignore)
        {
            // a report and a corresponding api-response already created. nothing to do here
        }
        catch (Exception | ImplementationError exc)
        {
            reportStatic(
                exc,
                ApiCallType.CREATE,
                getObjectDescriptionInline(storPoolNameStr),
                getObjRefs(storPoolNameStr),
                getVariables(storPoolNameStr),
                apiCallRc,
                accCtx,
                client
            );
        }

        return apiCallRc;
    }

    public ApiCallRc modifyStorPoolDfn(
        AccessContext accCtx,
        Peer client,
        UUID storPoolDfnUuid,
        String storPoolNameStr,
        Map<String, String> overrideProps,
        Set<String> deletePropKeys
    )
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();

        try (
            AbsApiCallHandler basicallyThis = setContext(
                accCtx,
                client,
                ApiCallType.MODIFY,
                apiCallRc,
                null,
                storPoolNameStr
            );
        )
        {
            requireStorPoolDfnChangeAccess();
            StorPoolDefinitionData storPoolDfn = loadStorPoolDfn(storPoolNameStr, true);

            if (storPoolDfnUuid != null && !storPoolDfnUuid.equals(storPoolDfn.getUuid()))
            {
                addAnswer(
                    "UUID-check failed",
                    ApiConsts.FAIL_UUID_STOR_POOL_DFN
                );
                throw new ApiCallHandlerFailedException();
            }

            Props props = getProps(storPoolDfn);
            Map<String, String> propsMap = props.map();

            propsMap.putAll(overrideProps);

            for (String delKey : deletePropKeys)
            {
                propsMap.remove(delKey);
            }

            commit();

            updateSatellites(storPoolDfn);
            reportSuccess(storPoolDfn.getUuid());;
        }
        catch (ApiCallHandlerFailedException ignore)
        {
            // a report and a corresponding api-response already created. nothing to do here
        }
        catch (Exception | ImplementationError exc)
        {
            reportStatic(
                exc,
                ApiCallType.MODIFY,
                getObjectDescriptionInline(storPoolNameStr),
                getObjRefs(storPoolNameStr),
                getVariables(storPoolNameStr),
                apiCallRc,
                accCtx,
                client
            );
        }

        return apiCallRc;
    }

    public ApiCallRc deleteStorPoolDfn(AccessContext accCtx, Peer client, String storPoolNameStr)
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();

        try (
            AbsApiCallHandler basicallyThis = setContext(
                accCtx,
                client,
                ApiCallType.DELETE,
                apiCallRc,
                null, // create new transMgr
                storPoolNameStr
            );
        )
        {
            requireStorPoolDfnChangeAccess();

            StorPoolDefinitionData storPoolDfn = loadStorPoolDfn(storPoolNameStr, true);

            Iterator<StorPool> storPoolIterator = getPrivilegedStorPoolIterator(storPoolDfn);

            if (storPoolIterator.hasNext())
            {
                StringBuilder nodeNames = new StringBuilder();
                nodeNames.append("'");
                while (storPoolIterator.hasNext())
                {
                    nodeNames.append(storPoolIterator.next().getNode().getName().displayValue)
                             .append("', '");
                }
                nodeNames.setLength(nodeNames.length() - 3); // cut the last ", '"

                addAnswer(
                    getObjectDescription() + " has still storage pools on node(s): " + nodeNames + ".",
                    null, // cause
                    null, // details
                    "Remove the storage pools first.", // correction
                    ApiConsts.FAIL_IN_USE
                );
            }
            else
            {
                UUID storPoolDfnUuid = storPoolDfn.getUuid();
                StorPoolName storPoolName = storPoolDfn.getName();
                delete(storPoolDfn);
                commit();

                apiCtrlAccessors.getStorPoolDfnMap().remove(storPoolName);

                reportSuccess(storPoolDfnUuid);
            }
        }
        catch (ApiCallHandlerFailedException ignore)
        {
            // a report and a corresponding api-response already created. nothing to do here
        }
        catch (Exception | ImplementationError exc)
        {
            reportStatic(
                exc,
                ApiCallType.DELETE,
                getObjectDescriptionInline(storPoolNameStr),
                getObjRefs(storPoolNameStr),
                getVariables(storPoolNameStr),
                apiCallRc,
                accCtx,
                client
            );
        }

        return apiCallRc;
    }

    private void delete(StorPoolDefinitionData storPoolDfn)
    {
        try
        {
            storPoolDfn.delete(currentAccCtx.get());
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asAccDeniedExc(
                accDeniedExc,
                "delete " + getObjectDescriptionInline(),
                ApiConsts.FAIL_ACC_DENIED_STOR_POOL_DFN
            );
        }
        catch (SQLException sqlExc)
        {
            throw asSqlExc(
                sqlExc,
                "deleting " + getObjectDescriptionInline()
            );
        }
    }

    private Iterator<StorPool> getPrivilegedStorPoolIterator(StorPoolDefinitionData storPoolDfn)
    {
        try
        {
            return storPoolDfn.iterateStorPools(apiCtx);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asImplError(accDeniedExc);
        }
    }

    byte[] listStorPoolDefinitions(int msgId, AccessContext accCtx, Peer client)
    {
        ArrayList<StorPoolDefinitionData.StorPoolDfnApi> storPoolDfns = new ArrayList<>();
        try
        {
            apiCtrlAccessors.getStorPoolDfnMapProtection().requireAccess(accCtx, AccessType.VIEW);// accDeniedExc1
            for (StorPoolDefinition storPoolDfn : apiCtrlAccessors.getStorPoolDfnMap().values())
            {
                try
                {
                    storPoolDfns.add(storPoolDfn.getApiData(accCtx));
                }
                catch (AccessDeniedException accDeniedExc)
                {
                    // don't add storpooldfn without access
                }
            }
        }
        catch (AccessDeniedException accDeniedExc)
        {
            // for now return an empty list.
        }

        return clientComSerializer
            .builder(ApiConsts.API_LST_STOR_POOL_DFN, msgId)
            .storPoolDfnList(storPoolDfns)
            .build();
    }

    protected AbsApiCallHandler setContext(
        AccessContext accCtx,
        Peer peer,
        ApiCallType type,
        ApiCallRcImpl apiCallRc,
        TransactionMgr transMgr,
        String storPoolNameStr
    )
    {
        super.setContext(
            accCtx,
            peer,
            type,
            apiCallRc,
            transMgr,
            getObjRefs(storPoolNameStr),
            getVariables(storPoolNameStr)
        );

        currentStorPoolNameStr.set(storPoolNameStr);

        return this;
    }

    private StorPoolDefinitionData createStorPool(String storPoolNameStr)
    {
        try
        {
            return StorPoolDefinitionData.getInstance(
                currentAccCtx.get(),
                asStorPoolName(storPoolNameStr),
                currentTransMgr.get(),
                true, // persist this entry
                true // fail if already exists
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asAccDeniedExc(
                accDeniedExc,
                "create " + getObjectDescriptionInline(),
                ApiConsts.FAIL_ACC_DENIED_STOR_POOL_DFN
            );
        }
        catch (LinStorDataAlreadyExistsException alreadyExistsExc)
        {
            throw asExc(
                alreadyExistsExc,
                getObjectDescription() + " already exists.",
                ApiConsts.FAIL_EXISTS_STOR_POOL_DFN
            );
        }
        catch (SQLException sqlExc)
        {
            throw asSqlExc(
                sqlExc,
                "creating " + getObjectDescriptionInline()
            );
        }
    }

    private Map<String, String> getObjRefs(String storPoolNameStr)
    {
        Map<String, String> objRefs = new TreeMap<>();
        objRefs.put(ApiConsts.KEY_STOR_POOL_DFN, storPoolNameStr);
        return objRefs;
    }

    private Map<String, String> getVariables(String storPoolNameStr)
    {
        Map<String, String> vars = new TreeMap<>();
        vars.put(ApiConsts.KEY_STOR_POOL_NAME, storPoolNameStr);
        return vars;
    }

    private void requireStorPoolDfnChangeAccess()
    {
        try
        {
            apiCtrlAccessors.getStorPoolDfnMapProtection().requireAccess(
                currentAccCtx.get(),
                AccessType.CHANGE
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asAccDeniedExc(
                accDeniedExc,
                "change any storage definitions pools.",
                ApiConsts.FAIL_ACC_DENIED_STOR_POOL_DFN
            );
        }
    }
    @Override
    protected String getObjectDescription()
    {
        return "Storage pool definition: " + currentStorPoolNameStr.get();
    }

    @Override
    protected String getObjectDescriptionInline()
    {
        return getObjectDescriptionInline(currentStorPoolNameStr.get());
    }


    private String getObjectDescriptionInline(String storPoolName)
    {
        return "storage pool definition '" + storPoolName + "'";
    }

    private Props getProps(StorPoolDefinitionData storPoolDfn)
    {
        try
        {
            return storPoolDfn.getProps(currentAccCtx.get());
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asAccDeniedExc(
                accDeniedExc,
                "accessing properties of storage pool definition '" + storPoolDfn.getName().displayValue + "'.",
                ApiConsts.FAIL_ACC_DENIED_STOR_POOL_DFN
            );
        }
    }

    private void updateSatellites(StorPoolDefinitionData storPoolDfn)
    {
        try
        {
            Iterator<StorPool> iterateStorPools = storPoolDfn.iterateStorPools(apiCtx);
            while (iterateStorPools.hasNext())
            {
                StorPool storPool = iterateStorPools.next();
                updateSatellite(storPool);
            }
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asImplError(accDeniedExc);
        }
    }
}
