package com.linbit.linstor.core;

import com.linbit.ExhaustedPoolException;
import com.linbit.ImplementationError;
import com.linbit.TransactionMgr;
import com.linbit.ValueOutOfRangeException;
import com.linbit.drbd.md.MdException;
import com.linbit.linstor.InternalApiConsts;
import com.linbit.linstor.LinStorDataAlreadyExistsException;
import com.linbit.linstor.LinStorException;
import com.linbit.linstor.MinorNumber;
import com.linbit.linstor.Resource;
import com.linbit.linstor.ResourceDefinition;
import com.linbit.linstor.ResourceDefinition.TransportType;
import com.linbit.linstor.ResourceDefinitionData;
import com.linbit.linstor.ResourceName;
import com.linbit.linstor.TcpPortNumber;
import com.linbit.linstor.VolumeDefinition;
import com.linbit.linstor.VolumeDefinition.VlmDfnApi;
import com.linbit.linstor.VolumeDefinitionData;
import com.linbit.linstor.VolumeNumber;
import com.linbit.linstor.VolumeNumberAlloc;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.api.ApiCallRcImpl.ApiCallRcEntry;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.interfaces.serializer.CtrlClientSerializer;
import com.linbit.linstor.api.interfaces.serializer.CtrlStltSerializer;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.propscon.InvalidKeyException;
import com.linbit.linstor.propscon.InvalidValueException;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.AccessType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

class CtrlRscDfnApiCallHandler extends AbsApiCallHandler
{
    private final CtrlClientSerializer clientComSerializer;
    private final ThreadLocal<String> currentRscNameStr = new ThreadLocal<>();

    CtrlRscDfnApiCallHandler(
        ApiCtrlAccessors apiCtrlAccessorsRef,
        CtrlStltSerializer interComSerializer,
        CtrlClientSerializer clientComSerializerRef,
        AccessContext apiCtxRef
    )
    {
        super(
            apiCtrlAccessorsRef,
            apiCtxRef,
            ApiConsts.MASK_RSC_DFN,
            interComSerializer
        );
        super.setNullOnAutoClose(currentRscNameStr);
        clientComSerializer = clientComSerializerRef;
    }

    public ApiCallRc createResourceDefinition(
        AccessContext accCtx,
        Peer client,
        String rscNameStr,
        Integer portInt,
        String secret,
        String transportTypeStr,
        Map<String, String> props,
        List<VlmDfnApi> volDescrMap
    )
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();

        short peerCount = getAsShort(props, ApiConsts.KEY_PEER_COUNT, apiCtrlAccessors.getDefaultPeerCount());
        int alStripes = getAsInt(props, ApiConsts.KEY_AL_STRIPES, apiCtrlAccessors.getDefaultAlStripes());
        long alStripeSize = getAsLong(props, ApiConsts.KEY_AL_SIZE, apiCtrlAccessors.getDefaultAlStripes());

        VolumeNumber volNr;
        try (
            AbsApiCallHandler basicallyThis = setContext(
                accCtx,
                client,
                ApiCallType.CREATE,
                apiCallRc,
                null, // create new transMgr
                rscNameStr
            );
        )
        {
            requireRscDfnMapChangeAccess();
            ResourceDefinitionData rscDfn = createRscDfn(rscNameStr, transportTypeStr, portInt, secret);

            getProps(rscDfn).map().putAll(props);

            List<VolumeDefinitionData> createdVlmDfns = new ArrayList<>();

            for (VolumeDefinition.VlmDfnApi vlmDfnApi : volDescrMap)
            {
//                currentVlmDfnApi = vlmDfnApi;

                volNr = getVlmNr(vlmDfnApi, rscDfn, apiCtx);
                MinorNumber minorNr = getMinor(vlmDfnApi);

                long size = vlmDfnApi.getSize();

                // getGrossSize performs check and throws exception when something is invalid
                checkGrossSize(size, peerCount, alStripes, alStripeSize, volNr, rscNameStr);

                VolumeDefinitionData vlmDfn = createVlmDfn(
                    rscDfn,
                    volNr,
                    minorNr,
                    size
                );

                getProps(vlmDfn).map().putAll(vlmDfnApi.getProps());

                createdVlmDfns.add(vlmDfn);
            }

            commit();

            apiCtrlAccessors.getRscDfnMap().put(rscDfn.getName(), rscDfn);

            for (VolumeDefinitionData vlmDfn : createdVlmDfns)
            {
                ApiCallRcEntry volSuccessEntry = new ApiCallRcEntry();
                volSuccessEntry.setReturnCode(ApiConsts.MASK_VLM_DFN | ApiConsts.CREATED);
                String successMessage = String.format(
                    "Volume definition with number '%d' successfully " +
                        " created in resource definition '%s'.",
                    vlmDfn.getVolumeNumber().value,
                    rscNameStr
                );
                volSuccessEntry.setMessageFormat(successMessage);
                volSuccessEntry.putVariable(ApiConsts.KEY_RSC_DFN, rscNameStr);
                volSuccessEntry.putVariable(ApiConsts.KEY_VLM_NR, Integer.toString(vlmDfn.getVolumeNumber().value));
                volSuccessEntry.putVariable(ApiConsts.KEY_MINOR_NR, Integer.toString(vlmDfn.getMinorNr(apiCtx).value));
                volSuccessEntry.putObjRef(ApiConsts.KEY_RSC_DFN, rscNameStr);
                volSuccessEntry.putObjRef(ApiConsts.KEY_VLM_NR, Integer.toString(vlmDfn.getVolumeNumber().value));

                apiCallRc.addEntry(volSuccessEntry);

                apiCtrlAccessors.getErrorReporter().logInfo(successMessage);
            }

            reportSuccess(rscDfn.getUuid());
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
                getObjectDescriptionInline(rscNameStr),
                getObjRefs(rscNameStr),
                getVariables(rscNameStr),
                apiCallRc,
                accCtx,
                client
            );
        }

        return apiCallRc;
    }

    private MinorNumber getMinor(VlmDfnApi vlmDfnApi) throws ValueOutOfRangeException, LinStorException
    {
        MinorNumber freeMinorNr;
        Integer apiMinor = vlmDfnApi.getMinorNr();
        if (apiMinor == null)
        {
            try
            {
                freeMinorNr = new MinorNumber(apiCtrlAccessors.getFreeMinorNr());
            }
            catch (ValueOutOfRangeException valueOutOfRangExc)
            {
                throw new ImplementationError(
                    "Failed to generate a valid minor number. Tried: '" + apiMinor + "'",
                    valueOutOfRangExc
                );
            }
            catch (ExhaustedPoolException exc)
            {
                throw new LinStorException("Could not found free minor number", exc);
            }
        }
        else
        {
            freeMinorNr = new MinorNumber(apiMinor);
        }
        return freeMinorNr;
    }

    public ApiCallRc modifyRscDfn(
        AccessContext accCtx,
        Peer client,
        UUID rscDfnUuid,
        String rscNameStr,
        Integer portInt,
        Map<String, String> overrideProps,
        Set<String> deletePropKeys
    )
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();

        try (AbsApiCallHandler basicallyThis = setContext(
                accCtx,
                client,
                ApiCallType.MODIFY,
                apiCallRc,
                null, // create new
                rscNameStr
            )
        )
        {
            requireRscDfnMapChangeAccess();

            ResourceName rscName = asRscName(rscNameStr);
            ResourceDefinitionData rscDfn = loadRscDfn(rscName, true);
            if (rscDfnUuid != null && !rscDfnUuid.equals(rscDfn.getUuid()))
            {
                addAnswer(
                    "UUID-check failed",
                    null,
                    "local UUID: " + rscDfn.getUuid().toString() + ", received UUID: " + rscDfnUuid.toString(),
                    null,
                    ApiConsts.FAIL_UUID_RSC_DFN
                );
                throw new ApiCallHandlerFailedException();
            }
            if (portInt != null)
            {
                TcpPortNumber port = asTcpPortNumber(portInt);
                rscDfn.setPort(accCtx, port);
            }
            if (!overrideProps.isEmpty() || !deletePropKeys.isEmpty())
            {
                Map<String, String> map = getProps(rscDfn).map();
                map.putAll(overrideProps);
                for (String delKey : deletePropKeys)
                {
                    map.remove(delKey);
                }
            }

            commit();

            reportSuccess(rscDfn.getUuid());
            updateSatellites(rscDfn);
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
                getObjectDescriptionInline(rscNameStr),
                getObjRefs(rscNameStr),
                getVariables(rscNameStr),
                apiCallRc,
                accCtx,
                client
            );
        }

        return apiCallRc;
    }

    public ApiCallRc deleteResourceDefinition(
        AccessContext accCtx,
        Peer client,
        String rscNameStr
    )
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();

        try (
            AbsApiCallHandler basicallyThis = setContext(
                accCtx,
                client,
                ApiCallType.DELETE,
                apiCallRc,
                null, // create new transMgr
                rscNameStr
            );
        )
        {
            requireRscDfnMapChangeAccess();

            ResourceDefinitionData rscDfn = loadRscDfn(rscNameStr, false);
            if (rscDfn == null)
            {
                addAnswer(
                    getObjectDescription() + " not found.",
                    ApiConsts.WARN_NOT_FOUND
                );
            }
            else
            {
                Iterator<Resource> rscIterator = getPrivilegedRscIterator(rscDfn);
                String successMsg;
                String details;
                UUID rscDfnUuid = rscDfn.getUuid();
                if (rscIterator.hasNext())
                {
                    markDeleted(rscDfn);

                    while (rscIterator.hasNext())
                    {
                        Resource rsc = rscIterator.next();
                        markDeletedPrivileged(rsc);
                    }

                    commit();

                    // notify satellites
                    updateSatellites(rscDfn);

                    successMsg = getObjectDescriptionInlineFirstLetterCaps() + " marked for deletion.";
                    details = getObjectDescriptionInlineFirstLetterCaps() + " UUID is: " + rscDfnUuid;
                }
                else
                {
                    ResourceName rscName = rscDfn.getName();
                    delete(rscDfn);
                    commit();

                    apiCtrlAccessors.getRscDfnMap().remove(rscName);

                    successMsg = getObjectDescriptionInlineFirstLetterCaps() + " deleted.";
                    details = getObjectDescriptionInlineFirstLetterCaps() + " UUID was: " + rscDfnUuid;
                }
                reportSuccess(successMsg, details);
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
                getObjectDescriptionInline(rscNameStr),
                getObjRefs(rscNameStr),
                getVariables(rscNameStr),
                apiCallRc,
                accCtx,
                client
            );
        }

        return apiCallRc;
    }

    void handlePrimaryResourceRequest(
        AccessContext accCtx,
        Peer satellite,
        int msgId,
        String rscNameStr,
        UUID rscUuid
    )
    {
        try (
            AbsApiCallHandler basicallyThis = setContext(
                accCtx,
                satellite,
                ApiCallType.MODIFY,
                null, // apiCallRc
                null, // create new transMgr
                rscNameStr
            )
        )
        {
            Resource res = loadRsc(
                satellite.getNode().getName().displayValue,
                rscNameStr,
                true
            );
            ResourceDefinitionData resDfn = (ResourceDefinitionData) res.getDefinition();

            Props resDfnProps = getProps(resDfn);
            if (resDfnProps.getProp(InternalApiConsts.PROP_PRIMARY_SET) == null)
            {
                resDfnProps.setProp(
                    InternalApiConsts.PROP_PRIMARY_SET,
                    res.getAssignedNode().getName().value
                );

                commit();

                apiCtrlAccessors.getErrorReporter().logTrace(
                    "Primary set for " + satellite.getNode().getName().getDisplayName()
                );

                updateSatellites(resDfn);

                satellite.sendMessage(
                    internalComSerializer
                        .builder(InternalApiConsts.API_PRIMARY_RSC, msgId)
                        .primaryRequest(rscNameStr, res.getUuid().toString())
                        .build()
                );
            }
        }
        catch (InvalidKeyException | InvalidValueException | AccessDeniedException _ignore)  { }
        catch (SQLException sqlExc)
        {
            String errorMessage = String.format(
                "A database error occured while trying to rollback the deletion of "+
                    "resource definition '%s'.",
                rscNameStr
            );
            apiCtrlAccessors.getErrorReporter().reportError(
                sqlExc,
                accCtx,
                satellite,
                errorMessage
            );
        }
    }

    byte[] listResourceDefinitions(int msgId, AccessContext accCtx, Peer client)
    {
        ArrayList<ResourceDefinitionData.RscDfnApi> rscdfns = new ArrayList<>();
        try
        {
            apiCtrlAccessors.getRscDfnMapProtection().requireAccess(accCtx, AccessType.VIEW);// accDeniedExc1
            for (ResourceDefinition rscdfn : apiCtrlAccessors.getRscDfnMap().values())
            {
                try
                {
                    rscdfns.add(rscdfn.getApiData(accCtx));
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
            .builder(ApiConsts.API_LST_RSC_DFN, msgId)
            .resourceDfnList(rscdfns)
            .build();
    }

    private short getAsShort(Map<String, String> props, String key, short defaultValue)
    {
        short ret = defaultValue;
        String value = props.get(key);
        if (value != null)
        {
            try
            {
                ret = Short.parseShort(value);
            }
            catch (NumberFormatException numberFormatExc)
            {
                // ignore and return the default value
            }
        }
        return ret;
    }

    private int getAsInt(Map<String, String> props, String key, int defaultValue)
    {
        int ret = defaultValue;
        String value = props.get(key);
        if (value != null)
        {
            try
            {
                ret = Integer.parseInt(value);
            }
            catch (NumberFormatException numberFormatExc)
            {
                // ignore and return the default value
            }
        }
        return ret;
    }

    private long getAsLong(Map<String, String> props, String key, long defaultValue)
    {
        long ret = defaultValue;
        String value = props.get(key);
        if (value != null)
        {
            try
            {
                ret = Long.parseLong(value);
            }
            catch (NumberFormatException numberFormatExc)
            {
                // ignore and return the default value
            }
        }
        return ret;
    }

    private AbsApiCallHandler setContext(
        AccessContext accCtx,
        Peer peer,
        ApiCallType type,
        ApiCallRcImpl apiCallRc,
        TransactionMgr transMgr,
        String rscNameStr
    )
    {
        super.setContext(
            accCtx,
            peer,
            type,
            apiCallRc,
            transMgr,
            getObjRefs(rscNameStr),
            getVariables(rscNameStr)
        );

        currentRscNameStr.set(rscNameStr);

        return this;
    }

    private void requireRscDfnMapChangeAccess()
    {
        try
        {
            apiCtrlAccessors.getRscDfnMapProtection().requireAccess(
                currentAccCtx.get(),
                AccessType.CHANGE
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asAccDeniedExc(
                accDeniedExc,
                "change any resource definitions.",
                ApiConsts.FAIL_ACC_DENIED_RSC_DFN
            );
        }
    }

    private ResourceDefinitionData createRscDfn(
        String rscNameStr,
        String transportTypeStr,
        Integer portInt,
        String secret
    )
    {
        TransportType transportType;
        if (transportTypeStr == null || transportTypeStr.trim().equals(""))
        {
            transportType = TransportType.IP;
        }
        else
        {
            try
            {
                transportType = TransportType.byValue(transportTypeStr); // TODO needs exception handling
            }
            catch (IllegalArgumentException unknownValueExc)
            {
                throw asExc(
                    unknownValueExc,
                    "The given transport type '" + transportTypeStr + "' is invalid.",
                    ApiConsts.FAIL_INVLD_TRANSPORT_TYPE
                );
            }
        }
        ResourceName rscName = asRscName(rscNameStr);

        if (portInt == null)
        {
            try
            {
                portInt = apiCtrlAccessors.getFreeTcpPort();
            }
            catch (ExhaustedPoolException exc)
            {
                throw asExc(
                    exc,
                    "Could not find free tcp port in range " +
                    TcpPortNumber.PORT_NR_MIN+ " - " + TcpPortNumber.PORT_NR_MAX,
                    ApiConsts.FAIL_INVLD_RSC_PORT // TODO create new RC for this case
                );
            }
        }

        try
        {
            return ResourceDefinitionData.getInstance(
                currentAccCtx.get(),
                rscName,
                asTcpPortNumber(portInt),
                null, // RscDfnFlags
                secret,
                transportType,
                currentTransMgr.get(),
                true, // persist this entry
                true // throw exception if the entry exists
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asAccDeniedExc(
                accDeniedExc,
                "create " + getObjectDescriptionInline(),
                ApiConsts.FAIL_ACC_DENIED_RSC_DFN
            );
        }
        catch (SQLException sqlExc)
        {
            throw asSqlExc(
                sqlExc,
                "creating " + getObjectDescriptionInline()
            );
        }
        catch (LinStorDataAlreadyExistsException exc)
        {
            throw asExc(
                exc,
                getObjectDescription() + " already exists.",
                ApiConsts.FAIL_EXISTS_RSC_DFN
            );
        }
    }

    private TcpPortNumber asTcpPortNumber(int portInt)
    {
        try
        {
            return new TcpPortNumber(portInt);
        }
        catch (ValueOutOfRangeException valOutOfRangeExc)
        {
            throw asExc(
                valOutOfRangeExc,
                "The given tcp port number '" + portInt + "' is invalid.",
                ApiConsts.FAIL_INVLD_RSC_PORT
            );
        }
    }

    private void checkGrossSize(
        long size,
        short peerCount,
        int alStripes,
        long alStripeSize,
        VolumeNumber volNr,
        String rscName
    )
    {
        try
        {
            apiCtrlAccessors.getMetaDataApi().getGrossSize(size, peerCount, alStripes, alStripeSize);
        }
        catch (MdException mdExc)
        {
            throw asExc(
                mdExc,
                "Gross size check failed for volume definition with volume number '" +
                    volNr.value + "' on resource definition '" + rscName + "'.",
                ApiConsts.FAIL_INVLD_VLM_SIZE
            );
        }
    }

    private VolumeDefinitionData createVlmDfn(
        ResourceDefinition rscDfn,
        VolumeNumber volNr,
        MinorNumber minorNr,
        long size
    )
    {
        try
        {
            return VolumeDefinitionData.getInstance(
                currentAccCtx.get(),
                rscDfn,
                volNr,
                minorNr,
                size,
                null, // VlmDfnFlags[]
                currentTransMgr.get(),
                true, // persist this entry
                true // throw exception if the entry exists
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asAccDeniedExc(
                accDeniedExc,
                "create volume definition with number '" + volNr.value + "' on resource definition '" +
                    rscDfn.getName().displayValue + "'",
                ApiConsts.FAIL_ACC_DENIED_VLM_DFN
            );
        }
        catch (LinStorDataAlreadyExistsException alreadyExistsExc)
        {
            throw asExc(
                alreadyExistsExc,
                "Volume definition with number '" + volNr.value + "' on resource definition '" +
                    rscDfn.getName().displayValue + "' already exists.",
                ApiConsts.FAIL_EXISTS_VLM_DFN
            );
        }
        catch (SQLException sqlExc)
        {
            throw asSqlExc(
                sqlExc,
                "creating volume definition with number '" + volNr.value + "' on resource definition '" +
                    rscDfn.getName().displayValue + "'"
            );
        }
        catch (MdException mdExc)
        {
            throw asExc(
                mdExc,
                "Gross size check failed for volume definition with volume number '" +
                    volNr.value + "' on resource definition '" + rscDfn.getName().displayValue + "'.",
                ApiConsts.FAIL_INVLD_VLM_SIZE
            );
        }
    }

    private void delete(ResourceDefinitionData rscDfn)
    {
        try
        {
            rscDfn.delete(currentAccCtx.get());
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asAccDeniedExc(
                accDeniedExc,
                "delete " + getObjectDescriptionInline(),
                ApiConsts.FAIL_ACC_DENIED_RSC_DFN
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

    private void markDeleted(ResourceDefinitionData rscDfn)
    {
        try
        {
            rscDfn.markDeleted(currentAccCtx.get());
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asAccDeniedExc(
                accDeniedExc,
                "mark " + getObjectDescriptionInline() + " as deleted",
                ApiConsts.FAIL_ACC_DENIED_RSC_DFN
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

    private void markDeletedPrivileged(Resource rsc)
    {
        try
        {
            rsc.markDeleted(apiCtx);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asImplError(accDeniedExc);
        }
        catch (SQLException sqlExc)
        {
            throw asSqlExc(
                sqlExc,
                "marking resource '" + rsc.getDefinition().getName().displayValue + "' on node '" +
                    rsc.getAssignedNode().getName().displayValue + "'"
            );
        }
    }

    private Iterator<Resource> getPrivilegedRscIterator(ResourceDefinitionData rscDfn)
    {
        try
        {
            return rscDfn.iterateResource(apiCtx);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw asImplError(accDeniedExc);
        }
    }

    static VolumeNumber getVlmNr(VlmDfnApi vlmDfnApi, ResourceDefinition rscDfn, AccessContext accCtx)
        throws ValueOutOfRangeException, LinStorException
    {
        VolumeNumber vlmNr;
        Integer vlmNrRaw = vlmDfnApi.getVolumeNr();
        if (vlmNrRaw == null)
        {
            try
            {
                int[] occupiedVlmNrs = new int[rscDfn.getVolumeDfnCount(accCtx)];
                Iterator<VolumeDefinition> vlmDfnIt = rscDfn.iterateVolumeDfn(accCtx);
                int idx = 0;
                while (vlmDfnIt.hasNext())
                {
                    VolumeDefinition vlmDfn = vlmDfnIt.next();
                    occupiedVlmNrs[idx] = vlmDfn.getVolumeNumber().value;
                    ++idx;
                }
                Arrays.sort(occupiedVlmNrs);

                vlmNr = VolumeNumberAlloc.getFreeVolumeNumber(occupiedVlmNrs);
            }
            catch (AccessDeniedException accDeniedExc)
            {
                throw new ImplementationError(
                    "ApiCtx does not have enough privileges to iterate vlmDfns",
                    accDeniedExc
                );
            }
            catch (ExhaustedPoolException exhausedPoolExc)
            {
                throw new LinStorException(
                    "No more free volume numbers left in range " + VolumeNumber.VOLUME_NR_MIN + " - " +
                    VolumeNumber.VOLUME_NR_MAX,
                    exhausedPoolExc
                );
            }
        }
        else
        {
            vlmNr = new VolumeNumber(vlmNrRaw);
        }
        return vlmNr;
    }

    @Override
    protected String getObjectDescription()
    {
        return "Resource definition: " + currentRscNameStr.get();
    }

    @Override
    protected String getObjectDescriptionInline()
    {
        return getObjectDescriptionInline(currentRscNameStr.get());
    }

    static String getObjectDescriptionInline(String rscName)
    {
        return "resource definition '" + rscName + "'";
    }

    private Map<String, String> getObjRefs(String rscNameStr)
    {
        Map<String, String> map = new TreeMap<>();
        map.put(ApiConsts.KEY_RSC_DFN, rscNameStr);
        return map;
    }

    private Map<String, String> getVariables(String rscNameStr)
    {
        Map<String, String> map = new TreeMap<>();
        map.put(ApiConsts.KEY_RSC_NAME, rscNameStr);
        return map;
    }
}
