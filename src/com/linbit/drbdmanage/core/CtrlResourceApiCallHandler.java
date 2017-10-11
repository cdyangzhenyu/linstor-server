package com.linbit.drbdmanage.core;

import static com.linbit.drbdmanage.api.ApiConsts.*;
import static com.linbit.drbdmanage.ApiCallRcConstants.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.TransactionMgr;
import com.linbit.ValueOutOfRangeException;
import com.linbit.drbd.md.MdException;
import com.linbit.drbdmanage.ApiCallRc;
import com.linbit.drbdmanage.ApiCallRcImpl;
import com.linbit.drbdmanage.DrbdDataAlreadyExistsException;
import com.linbit.drbdmanage.MinorNumber;
import com.linbit.drbdmanage.NodeData;
import com.linbit.drbdmanage.NodeId;
import com.linbit.drbdmanage.NodeName;
import com.linbit.drbdmanage.Resource.RscFlags;
import com.linbit.drbdmanage.ResourceData;
import com.linbit.drbdmanage.ResourceDefinition;
import com.linbit.drbdmanage.ResourceDefinitionData;
import com.linbit.drbdmanage.ResourceName;
import com.linbit.drbdmanage.Volume.VlmApi;
import com.linbit.drbdmanage.Volume.VlmFlags;
import com.linbit.drbdmanage.VolumeData;
import com.linbit.drbdmanage.VolumeDefinition;
import com.linbit.drbdmanage.VolumeDefinition.VlmDfnApi;
import com.linbit.drbdmanage.VolumeDefinition.VlmDfnFlags;
import com.linbit.drbdmanage.VolumeDefinitionData;
import com.linbit.drbdmanage.VolumeNumber;
import com.linbit.drbdmanage.ApiCallRcImpl.ApiCallRcEntry;
import com.linbit.drbdmanage.ResourceDefinition.RscDfnFlags;
import com.linbit.drbdmanage.netcom.Peer;
import com.linbit.drbdmanage.security.AccessContext;
import com.linbit.drbdmanage.security.AccessDeniedException;
import com.linbit.drbdmanage.security.AccessType;

class CtrlResourceApiCallHandler
{
    private final Controller controller;

    CtrlResourceApiCallHandler(Controller controllerRef)
    {
        controller = controllerRef;
    }

    public ApiCallRc createResourceDefinition(
        AccessContext accCtx,
        Peer client,
        String resourceName,
        Map<String, String> props,
        List<VlmDfnApi> volDescrMap
    )
    {
        /*
         * Usually its better to handle exceptions "close" to their appearance.
         * However, as in this method almost every other line throws an exception,
         * the code would get completely unreadable; thus, unmaintainable.
         *
         * For that reason there is (almost) only one try block with many catches, and
         * those catch blocks handle the different cases (commented as <some>Exc<count> in
         * the try block and a matching "handle <some>Exc<count>" in the catch block)
         */

        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();
        ResourceDefinition rscDfn = null;
        TransactionMgr transMgr = null;

        VolumeNumber volNr = null;
        MinorNumber minorNr = null;
        VolumeDefinition.VlmDfnApi currentVolCrtData = null;

        short peerCount = getAsShort(props, KEY_PEER_COUNT, controller.getDefaultPeerCount());
        int alStripes = getAsInt(props, KEY_AL_STRIPES, controller.getDefaultAlStripes());
        long alStripeSize = getAsLong(props, KEY_AL_SIZE, controller.getDefaultAlSize());

        try
        {
            transMgr = new TransactionMgr(controller.dbConnPool.getConnection()); // sqlExc1

            controller.rscDfnMapProt.requireAccess(accCtx, AccessType.CHANGE); // accDeniedExc1

            RscDfnFlags[] rscDfnInitFlags = null;

            rscDfn = ResourceDefinitionData.getInstance( // sqlExc2, accDeniedExc1 (same as last line), alreadyExistsExc1
                accCtx,
                new ResourceName(resourceName), // invalidNameExc1
                rscDfnInitFlags,
                transMgr,
                true,
                true
            );

            for (VolumeDefinition.VlmDfnApi volCrtData : volDescrMap)
            {
                currentVolCrtData = volCrtData;

                volNr = null;
                minorNr = null;

                volNr = new VolumeNumber(volCrtData.getVolumeNr()); // valOORangeExc1
                minorNr = new MinorNumber(volCrtData.getMinorNr()); // valOORangeExc2

                long size = volCrtData.getSize();

                // getGrossSize performs check and throws exception when something is invalid
                controller.getMetaDataApi().getGrossSize(size, peerCount, alStripes, alStripeSize);
                // mdExc1

                VlmDfnFlags[] vlmDfnInitFlags = null;

                VolumeDefinitionData.getInstance( // mdExc2, sqlExc3, accDeniedExc2, alreadyExistsExc2
                    accCtx,
                    rscDfn,
                    volNr,
                    minorNr,
                    size,
                    vlmDfnInitFlags,
                    transMgr,
                    true,
                    true
                );
            }

            transMgr.commit(); // sqlExc4

            controller.rscDfnMap.put(rscDfn.getName(), rscDfn);

            for (VolumeDefinition.VlmDfnApi volCrtData : volDescrMap)
            {
                ApiCallRcEntry volSuccessEntry = new ApiCallRcEntry();
                volSuccessEntry.setReturnCode(RC_VLM_DFN_CREATED);
                volSuccessEntry.setMessageFormat(
                    String.format(
                        "Volume definition with number %d and minor number %d successfully " +
                            " created in resource definition '%s'.",
                        volCrtData.getVolumeNr(),
                        volCrtData.getMinorNr(),
                        resourceName
                    )
                );
                volSuccessEntry.putVariable(KEY_RSC_DFN, resourceName);
                volSuccessEntry.putVariable(KEY_VLM_NR, Integer.toString(volCrtData.getVolumeNr()));
                volSuccessEntry.putVariable(KEY_MINOR_NR, Integer.toString(volCrtData.getMinorNr()));
                volSuccessEntry.putObjRef(KEY_RSC_DFN, resourceName);
                volSuccessEntry.putObjRef(KEY_VLM_NR, Integer.toString(volCrtData.getVolumeNr()));

                apiCallRc.addEntry(volSuccessEntry);
            }

            ApiCallRcEntry successEntry = new ApiCallRcEntry();

            String successMsg = String.format(
                "Resource definition '%s' successfully created.",
                resourceName
            );
            successEntry.setReturnCode(RC_RSC_DFN_CREATED);
            successEntry.setMessageFormat(successMsg);
            successEntry.putVariable(KEY_RSC_NAME, resourceName);
            successEntry.putVariable(KEY_PEER_COUNT, Short.toString(peerCount));
            successEntry.putVariable(KEY_AL_STRIPES, Integer.toString(alStripes));
            successEntry.putVariable(KEY_AL_SIZE, Long.toString(alStripeSize));
            successEntry.putObjRef(KEY_RSC_DFN, resourceName);

            apiCallRc.addEntry(successEntry);
            controller.getErrorReporter().logInfo(successMsg);
        }
        catch (SQLException sqlExc)
        {
            String errorMessage = String.format(
                "A database error occured while creating the resource definition '%s'.",
                resourceName
            );
            controller.getErrorReporter().reportError(
                sqlExc,
                accCtx,
                client,
                errorMessage
            );

            ApiCallRcEntry entry = new ApiCallRcEntry();
            entry.setReturnCodeBit(RC_RSC_DFN_CRT_FAIL_SQL);
            entry.setMessageFormat(errorMessage);
            entry.setCauseFormat(sqlExc.getMessage());
            entry.putObjRef(KEY_RSC_DFN, resourceName);

            apiCallRc.addEntry(entry);
        }
        catch (AccessDeniedException accExc)
        {
            ApiCallRcEntry entry = new ApiCallRcEntry();
            String action;
            if (rscDfn == null)
            { // handle accDeniedExc1

                action = "create a new resource definition.";
                entry.setReturnCodeBit(RC_RSC_DFN_CRT_FAIL_ACC_DENIED_RSC_DFN);
            }
            else
            { // handle accDeniedExc2
                action = String.format(
                    "create a new volume definition for resource definition '%s'.",
                    resourceName
                );
                entry.setReturnCodeBit(RC_RSC_DFN_CRT_FAIL_ACC_DENIED_VLM_DFN);
                entry.putVariable(KEY_VLM_NR, Integer.toString(currentVolCrtData.getVolumeNr()));
                entry.putVariable(KEY_MINOR_NR, Integer.toString(currentVolCrtData.getMinorNr()));
                entry.putObjRef(KEY_VLM_NR, Integer.toString(currentVolCrtData.getVolumeNr()));
            }
            String errorMessage = String.format(
                "The access context (user: %s, role: %s) has no permission to %s",
                accCtx.subjectId.name.displayValue,
                accCtx.subjectRole.name.displayValue,
                action
            );
            controller.getErrorReporter().reportError(
                accExc,
                accCtx,
                client,
                errorMessage
            );
            entry.setMessageFormat(errorMessage);
            entry.setCauseFormat(accExc.getMessage());
            entry.putVariable(KEY_ID, accCtx.subjectId.name.displayValue);
            entry.putVariable(KEY_ROLE, accCtx.subjectRole.name.displayValue);
            entry.putObjRef(KEY_RSC_DFN, resourceName);

            apiCallRc.addEntry(entry);
        }
        catch (InvalidNameException nameExc)
        {
            // handle invalidNameExc1

            String errorMessage = String.format(
                "The specified resource name '%s' is not a valid.",
                resourceName
            );
            controller.getErrorReporter().reportError(
                nameExc,
                accCtx,
                client,
                errorMessage
            );
            ApiCallRcEntry entry = new ApiCallRcEntry();
            entry.setReturnCodeBit(RC_RSC_DFN_CRT_FAIL_INVLD_RSC_NAME);
            entry.setMessageFormat(errorMessage);
            entry.setCauseFormat(errorMessage);
            entry.putVariable(KEY_RSC_NAME, resourceName);
            entry.putObjRef(KEY_RSC_DFN, resourceName);

            apiCallRc.addEntry(entry);
        }
        catch (ValueOutOfRangeException valOORangeExc)
        {
            ApiCallRcEntry entry = new ApiCallRcEntry();
            String errorMessage;
            if (volNr == null)
            { // handle valOORangeExc1
                errorMessage = String.format(
                    "The specified volume number %d is invalid.",
                    currentVolCrtData.getVolumeNr()
                );
                entry.setReturnCodeBit(RC_RSC_DFN_CRT_FAIL_INVLD_VLM_NR);
            }
            else
            { // handle valOORangeExc2
                errorMessage = String.format(
                    "The specified minor number %d is invalid.",
                    currentVolCrtData.getMinorNr()
                );
                entry.setReturnCodeBit(RC_RSC_DFN_CRT_FAIL_INVLD_MINOR_NR);
                entry.putVariable(KEY_MINOR_NR, Integer.toString(currentVolCrtData.getVolumeNr()));
            }
            controller.getErrorReporter().reportError(
                valOORangeExc,
                accCtx,
                client,
                errorMessage
            );
            entry.setMessageFormat(errorMessage);
            entry.putVariable(KEY_VLM_NR, Integer.toString(currentVolCrtData.getVolumeNr()));
            entry.putVariable(KEY_RSC_DFN, resourceName);
            entry.putObjRef(KEY_RSC_DFN, resourceName);
            entry.putObjRef(KEY_VLM_NR, Integer.toString(currentVolCrtData.getVolumeNr()));

            apiCallRc.addEntry(entry);
        }
        catch (MdException metaDataExc)
        {
            // handle mdExc1 and mdExc2
            String errorMessage = String.format(
                "The specified volume size %d is invalid.",
                currentVolCrtData.getSize()
            );
            controller.getErrorReporter().reportError(
                metaDataExc,
                accCtx,
                client,
                errorMessage
            );
            ApiCallRcEntry entry = new ApiCallRcEntry();
            entry.setReturnCodeBit(RC_RSC_DFN_CRT_FAIL_INVLD_VLM_SIZE);
            entry.setMessageFormat(errorMessage);
            entry.setCauseFormat(metaDataExc.getMessage());
            entry.putVariable(KEY_VLM_NR, Integer.toString(currentVolCrtData.getVolumeNr()));
            entry.putVariable(KEY_RSC_DFN, resourceName);
            entry.putVariable(KEY_VLM_SIZE, Long.toString(currentVolCrtData.getSize()));
            entry.putObjRef(KEY_RSC_DFN, resourceName);
            entry.putObjRef(KEY_VLM_NR, Integer.toString(currentVolCrtData.getVolumeNr()));

            apiCallRc.addEntry(entry);
        }
        catch (DrbdDataAlreadyExistsException alreadyExistsExc)
        {
            ApiCallRcEntry entry = new ApiCallRcEntry();
            String errorMsg;
            if (rscDfn == null)
            {
                // handle alreadyExists1
                errorMsg = String.format(
                    "A resource definition with the name '%s' already exists.",
                    resourceName
                );
                entry.setReturnCodeBit(RC_RSC_DFN_CRT_FAIL_EXISTS_RSC_DFN);
            }
            else
            {
                // handle alreadyExists2
                errorMsg = String.format(
                    "A volume definition with the numer %d already exists in resource definition '%s'.",
                    currentVolCrtData.getVolumeNr(),
                    resourceName
                );
                entry.setReturnCodeBit(RC_RSC_DFN_CRT_FAIL_EXISTS_VLM_DFN);
                entry.putVariable(KEY_VLM_NR, Integer.toString(currentVolCrtData.getVolumeNr()));
                entry.putVariable(KEY_MINOR_NR, Integer.toString(currentVolCrtData.getMinorNr()));
                entry.putObjRef(KEY_VLM_NR, Integer.toString(currentVolCrtData.getVolumeNr()));
            }
            controller.getErrorReporter().reportError(
                alreadyExistsExc,
                accCtx,
                client,
                errorMsg
            );
            entry.setMessageFormat(errorMsg);
            entry.setCauseFormat(alreadyExistsExc.getMessage());
            entry.putVariable(KEY_RSC_NAME, resourceName);
            entry.putObjRef(KEY_RSC_DFN, resourceName);
            apiCallRc.addEntry(entry);
        }

        if (transMgr != null && transMgr.isDirty())
        {
            // not committed -> error occurred
            try
            {
                transMgr.rollback();
            }
            catch (SQLException sqlExc)
            {
                String errorMessage = String.format(
                    "A database error occured while trying to rollback the creation of " +
                        "resource definition '%s'.",
                    resourceName
                );
                controller.getErrorReporter().reportError(
                    sqlExc,
                    accCtx,
                    client,
                    errorMessage
                );

                ApiCallRcEntry entry = new ApiCallRcEntry();
                entry.setReturnCodeBit(RC_RSC_DFN_CRT_FAIL_SQL_ROLLBACK);
                entry.setMessageFormat(errorMessage);
                entry.setCauseFormat(sqlExc.getMessage());
                entry.putObjRef(KEY_RSC_DFN, resourceName);

                apiCallRc.addEntry(entry);
            }
        }
        return apiCallRc;
    }

    public ApiCallRc deleteResourceDefinition(AccessContext accCtx, Peer client, String resNameStr)
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();
        TransactionMgr transMgr = null;
        ResourceName resName = null;
        ResourceDefinitionData resDfn = null;

        try
        {
            controller.rscDfnMapProt.requireAccess(accCtx, AccessType.CHANGE); // accDeniedExc1
            transMgr = new TransactionMgr(controller.dbConnPool.getConnection()); // sqlExc1

            resName = new ResourceName(resNameStr); // invalidNameExc1
            resDfn = ResourceDefinitionData.getInstance( // accDeniedExc2, sqlExc2, dataAlreadyExistsExc1
                accCtx,
                resName,
                null,
                transMgr,
                false,
                false
            );

            if (resDfn != null)
            {
                resDfn.setConnection(transMgr);
                resDfn.markDeleted(accCtx); // accDeniedExc3, sqlExc3

                transMgr.commit(); // sqlExc4

                ApiCallRcEntry entry = new ApiCallRcEntry();
                entry.setReturnCodeBit(RC_RSC_DFN_DELETED);
                entry.setMessageFormat(
                    String.format(
                        "Resource definition '%s' successfully deleted",
                        resNameStr
                    )
                );
                entry.putObjRef(KEY_RSC_DFN, resNameStr);
                entry.putVariable(KEY_RSC_NAME, resNameStr);
                apiCallRc.addEntry(entry);


                // TODO: tell satellites to remove all the corresponding resources
                // TODO: if satellites are finished (or no satellite had such a resource deployed)
                //       remove the rscDfn from the DB
                controller.getErrorReporter().logInfo(
                    "Resource definition '%s' marked to be deleted",
                    resNameStr
                );
            }
            else
            {
                ApiCallRcEntry entry = new ApiCallRcEntry();
                entry.setReturnCodeBit(RC_RSC_DFN_DEL_NOT_FOUND);
                entry.setMessageFormat(
                    String.format(
                        "Resource definition '%s' was not deleted as it was not found",
                        resNameStr
                    )
                );
                entry.putObjRef(KEY_RSC_DFN, resNameStr);
                entry.putVariable(KEY_RSC_NAME, resNameStr);
                apiCallRc.addEntry(entry);

                controller.getErrorReporter().logInfo(
                    "Non existing reource definition '%s' could not be deleted",
                    resNameStr
                );
            }
        }
        catch (AccessDeniedException accDeniedExc)
        {
            String errorMessage = String.format(
                "The access context (user: %s, role: %s) has no permission to " +
                    "delete the resource definition '%s'.",
                accCtx.subjectId.name.displayValue,
                accCtx.subjectRole.name.displayValue,
                resNameStr
            );
            controller.getErrorReporter().reportError(
                accDeniedExc,
                accCtx,
                client,
                errorMessage
            );

            ApiCallRcEntry entry = new ApiCallRcEntry();
            entry.setReturnCodeBit(RC_RSC_DFN_DEL_FAIL_ACC_DENIED_RSC_DFN);
            entry.setMessageFormat(errorMessage);
            entry.setCauseFormat(accDeniedExc.getMessage());
            entry.putObjRef(KEY_RSC_DFN, resNameStr);
            entry.putVariable(KEY_RSC_NAME, resNameStr);

            apiCallRc.addEntry(entry);
        }
        catch (SQLException sqlExc)
        {
            String errorMessge = String.format(
                "A database error occured while deleting the resource definition '%s'.",
                resNameStr
            );
            controller.getErrorReporter().reportError(
                sqlExc,
                accCtx,
                client,
                errorMessge
            );

            ApiCallRcEntry entry = new ApiCallRcEntry();
            entry.setReturnCodeBit(RC_RSC_DFN_DEL_FAIL_SQL);
            entry.setMessageFormat(errorMessge);
            entry.setCauseFormat(sqlExc.getMessage());
            entry.putObjRef(KEY_RSC_DFN, resNameStr);
            entry.putVariable(KEY_RSC_DFN, resNameStr);

            apiCallRc.addEntry(entry);
        }
        catch (InvalidNameException invalidNameExc)
        { // handle invalidNameExc1
            String errorMessage = String.format(
                "The given resource name '%s' is invalid.",
                resNameStr
            );
            controller.getErrorReporter().reportError(
                invalidNameExc,
                accCtx,
                client,
                errorMessage
            );

            ApiCallRcEntry entry = new ApiCallRcEntry();
            entry.setReturnCodeBit(RC_RSC_DFN_DEL_FAIL_INVLD_RSC_NAME);
            entry.setMessageFormat(errorMessage);
            entry.setCauseFormat(invalidNameExc.getMessage());

            entry.putObjRef(KEY_RSC_DFN, resNameStr);
            entry.putVariable(KEY_RSC_NAME, resNameStr);

            apiCallRc.addEntry(entry);
        }
        catch (DrbdDataAlreadyExistsException dataAlreadyExistsExc)
        { // handle drbdAlreadyExistsExc1
            controller.getErrorReporter().reportError(
                new ImplementationError(
                    String.format(
                        ".getInstance was called with failIfExists=false, still threw an AlreadyExistsException "+
                            "(Resource name: %s)",
                        resNameStr
                    ),
                    dataAlreadyExistsExc
                )
            );

            ApiCallRcEntry entry = new ApiCallRcEntry();
            entry.setReturnCodeBit(RC_RSC_DFN_DEL_FAIL_EXISTS_IMPL_ERROR);
            entry.setMessageFormat(
                String.format(
                    "Failed to delete the resource definition '%s' due to an implementation error.",
                    resNameStr
                )
            );
            entry.setCauseFormat(dataAlreadyExistsExc.getMessage());
            entry.putObjRef(KEY_RSC_DFN, resNameStr);
            entry.putVariable(KEY_RSC_NAME, resNameStr);

            apiCallRc.addEntry(entry);
        }

        if (transMgr != null)
        {
            if (transMgr.isDirty())
            {
                try
                {
                    transMgr.rollback();
                }
                catch (SQLException sqlExc)
                {
                    String errorMessage = String.format(
                        "A database error occured while trying to rollback the deletion of "+
                            "resource definition '%s'.",
                        resNameStr
                    );
                    controller.getErrorReporter().reportError(
                        sqlExc,
                        accCtx,
                        client,
                        errorMessage
                    );

                    ApiCallRcEntry entry = new ApiCallRcEntry();
                    entry.setReturnCodeBit(RC_RSC_DFN_DEL_FAIL_SQL_ROLLBACK);
                    entry.setMessageFormat(errorMessage);
                    entry.setCauseFormat(sqlExc.getMessage());
                    entry.putObjRef(KEY_RSC_DFN, resNameStr);

                    apiCallRc.addEntry(entry);
                }
            }
            controller.dbConnPool.returnConnection(transMgr.dbCon);
        }
        return apiCallRc;
    }

    public ApiCallRc createResource(
        AccessContext accCtx,
        Peer client,
        String nodeNameStr,
        String rscNameStr,
        int nodeIdRaw,
        Map<String, String> rscProps,
        List<VlmApi> vlmApiList
    )
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();

        TransactionMgr transMgr = null;

        NodeName nodeName = null;
        ResourceName rscName = null;

        NodeData node = null;
        ResourceDefinitionData rscDfn = null;

        NodeId nodeId = null;

        ResourceData rsc = null;
        VlmApi currentVlmApi = null;
        VolumeNumber volNr = null;
        VolumeDefinition vlmDfn = null;

        try
        {
            transMgr = new TransactionMgr(controller.dbConnPool);

            nodeName = new NodeName(nodeNameStr);
            rscName = new ResourceName(rscNameStr);

            node = NodeData.getInstance( // accDeniedExc1, dataAlreadyExistsExc0
                accCtx,
                nodeName,
                null,
                null,
                transMgr,
                false,
                false
            );
            rscDfn = ResourceDefinitionData.getInstance( // accDeniedExc2, dataAlreadyExistsExc0
                accCtx,
                rscName,
                null,
                transMgr,
                false,
                false
            );

            if (node == null)
            {
                ApiCallRcEntry nodeNotFoundEntry = new ApiCallRcEntry();
                nodeNotFoundEntry.setReturnCode(RC_RSC_CRT_FAIL_NOT_FOUND_NODE);
                nodeNotFoundEntry.setCauseFormat(
                    String.format(
                        "The specified node '%s' could not be found in the database",
                        nodeNameStr
                    )
                );
                nodeNotFoundEntry.setCorrectionFormat(
                    String.format(
                        "Create a node with the name '%s' first.",
                        nodeNameStr
                    )
                );
                nodeNotFoundEntry.putVariable(KEY_NODE_NAME, nodeNameStr);
                nodeNotFoundEntry.putObjRef(KEY_NODE, nodeNameStr);
                nodeNotFoundEntry.putObjRef(KEY_RSC_DFN, rscNameStr);

                apiCallRc.addEntry(nodeNotFoundEntry);
            }
            else
            if (rscDfn == null)
            {
                ApiCallRcEntry rscDfnNotFoundEntry = new ApiCallRcEntry();
                rscDfnNotFoundEntry.setReturnCode(RC_RSC_CRT_FAIL_NOT_FOUND_RSC_DFN);
                rscDfnNotFoundEntry.setCauseFormat(
                    String.format(
                        "The specified resource definition '%s' could not be found in the database",
                        rscNameStr
                    )
                );
                rscDfnNotFoundEntry.setCorrectionFormat(
                    String.format(
                        "Create a resource definition with the name '%s' first.",
                        rscNameStr
                    )
                );
                rscDfnNotFoundEntry.putVariable(KEY_RSC_NAME, rscNameStr);
                rscDfnNotFoundEntry.putObjRef(KEY_NODE, nodeNameStr);
                rscDfnNotFoundEntry.putObjRef(KEY_RSC_DFN, rscNameStr);

                apiCallRc.addEntry(rscDfnNotFoundEntry);
            }
            else
            {
                nodeId = new NodeId(nodeIdRaw);

                RscFlags[] initFlags = null;

                ApiCallRcImpl successApiCallRc = new ApiCallRcImpl();

                rsc = ResourceData.getInstance( // accDeniedExc3, dataAlreadyExistsExc1
                    accCtx,
                    rscDfn,
                    node,
                    nodeId,
                    initFlags,
                    transMgr,
                    true,
                    true
                );

                ApiCallRcEntry rscSuccess = new ApiCallRcEntry();
                String rscSuccessMsg = String.format(
                    "Resource '%s' successfully created on node '%s'",
                    rscNameStr,
                    nodeNameStr
                );
                rscSuccess.setMessageFormat(rscSuccessMsg);
                rscSuccess.setReturnCode(RC_RSC_CREATED);
                rscSuccess.putObjRef(KEY_NODE, nodeNameStr);
                rscSuccess.putObjRef(KEY_RSC_DFN, rscNameStr);
                rscSuccess.putVariable(KEY_NODE_NAME, nodeNameStr);
                rscSuccess.putVariable(KEY_RSC_NAME, rscNameStr);

                successApiCallRc.addEntry(rscSuccess);

                for (VlmApi vlmApi : vlmApiList)
                {
                    currentVlmApi = vlmApi;

                    volNr = null;
                    vlmDfn = null;
                    volNr = new VolumeNumber(vlmApi.getVlmNr());
                    vlmDfn = rscDfn.getVolumeDfn(accCtx, volNr); // accDeniedExc4

                    VlmFlags[] vlmFlags = null;

                    VolumeData.getInstance( // accDeniedExc5, dataAlreadyExistsExc2
                        accCtx,
                        rsc,
                        vlmDfn,
                        vlmApi.getBlockDevice(),
                        vlmApi.getMetaDisk(),
                        vlmFlags,
                        transMgr,
                        true,
                        true
                    );
                    ApiCallRcEntry vlmSuccess = new ApiCallRcEntry();
                    vlmSuccess.setMessageFormat(
                        String.format(
                            "Volume with number %d created successfully on node '%s' for resource '%s'.",
                            vlmApi.getVlmNr(),
                            nodeNameStr,
                            rscNameStr
                        )
                    );
                    vlmSuccess.putVariable(KEY_NODE_NAME, nodeNameStr);
                    vlmSuccess.putVariable(KEY_RSC_NAME, rscNameStr);
                    vlmSuccess.putVariable(KEY_VLM_NR, Integer.toString(vlmApi.getVlmNr()));
                    vlmSuccess.putObjRef(KEY_NODE, nodeNameStr);
                    vlmSuccess.putObjRef(KEY_RSC_DFN, rscNameStr);
                    vlmSuccess.putObjRef(KEY_VLM_NR, Integer.toString(vlmApi.getVlmNr()));

                    successApiCallRc.addEntry(vlmSuccess);
                }

                transMgr.commit();

                // if everything worked fine, just replace the returned rcApiCall with the
                // already filled successApiCallRc. otherwise, this line does not get executed anyways
                apiCallRc = successApiCallRc;
                controller.getErrorReporter().logInfo(rscSuccessMsg);

                // TODO: tell satellite(s) to do their job
                // TODO: if a satellite confirms creation, also log it to controller.info
            }
        }
        catch (SQLException sqlExc)
        {
            String errorMessage = String.format(
                "A database error occured while trying to create the resource '%s' on node '%s'.",
                nodeNameStr,
                rscNameStr
            );
            controller.getErrorReporter().reportError(
                sqlExc,
                accCtx,
                client,
                errorMessage
            );

            ApiCallRcEntry entry = new ApiCallRcEntry();
            entry.setReturnCodeBit(RC_RSC_CRT_FAIL_SQL);
            entry.setMessageFormat(errorMessage);
            entry.setCauseFormat(sqlExc.getMessage());
            entry.putObjRef(KEY_NODE, nodeNameStr);
            entry.putObjRef(KEY_RSC_DFN, rscNameStr);
            entry.putVariable(KEY_NODE_NAME, nodeNameStr);
            entry.putVariable(KEY_RSC_NAME, rscNameStr);

            apiCallRc.addEntry(entry);
        }
        catch (InvalidNameException invalidNameExc)
        {
            ApiCallRcEntry entry = new ApiCallRcEntry();
            String errorMessage;
            if (nodeName == null)
            {
                errorMessage = String.format("Given node name '%s' is invalid.", nodeNameStr);
                entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                entry.setReturnCodeBit(RC_RSC_CRT_FAIL_INVALID_NODE_NAME);
            }
            else
            {
                errorMessage = String.format("Given resource name '%s' is invalid.", rscNameStr);
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                entry.setReturnCodeBit(RC_RSC_CRT_FAIL_INVALID_RSC_NAME);
            }
            controller.getErrorReporter().reportError(
                invalidNameExc,
                accCtx,
                client,
                errorMessage
            );
            entry.setMessageFormat(errorMessage);
            entry.setCauseFormat(invalidNameExc.getMessage());
            entry.putObjRef(KEY_NODE, nodeNameStr);
            entry.putObjRef(KEY_RSC_DFN, rscNameStr);

            apiCallRc.addEntry(entry);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            ApiCallRcEntry entry = new ApiCallRcEntry();
            String action = "Given user has no permission to ";
            if (node == null)
            { // accDeniedExc1
                action += String.format(
                    "access the node '%s'.",
                    nodeNameStr
                );
                entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                entry.setReturnCodeBit(RC_RSC_CRT_FAIL_ACC_DENIED_NODE);
            }
            else
            if (rscDfn == null)
            { // accDeniedExc2
                action += String.format(
                    "access the resource definition '%s'.",
                    rscNameStr
                );
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                entry.setReturnCodeBit(RC_RSC_CRT_FAIL_ACC_DENIED_RSC_DFN);
            }
            else
            if (rsc == null)
            { // accDeniedExc3
                action += String.format(
                    "access the resource '%s' on node '%s'.",
                    rscNameStr,
                    nodeNameStr
                );
                entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                entry.setReturnCodeBit(RC_RSC_CRT_FAIL_ACC_DENIED_RSC);
            }
            else
            if (vlmDfn == null)
            { // accDeniedExc4
                action += String.format(
                    "access the volume definition with volume number %d on resource '%s' on node '%s'.",
                    currentVlmApi.getVlmNr(),
                    rscNameStr,
                    nodeNameStr
                );
                entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                entry.putVariable(KEY_VLM_NR, Integer.toString(currentVlmApi.getVlmNr()));
                entry.putObjRef(KEY_VLM_NR, Integer.toString(currentVlmApi.getVlmNr()));
                entry.setReturnCodeBit(RC_RSC_CRT_FAIL_ACC_DENIED_VLM_DFN);
            }
            else
            { // accDeniedExc5
                action += String.format(
                    "create a new volume with volume number %d on resource '%s' on node '%s'.",
                    currentVlmApi.getVlmNr(),
                    rscNameStr,
                    nodeNameStr
                );
                entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                entry.putVariable(KEY_VLM_NR, Integer.toString(currentVlmApi.getVlmNr()));
                entry.putObjRef(KEY_VLM_NR, Integer.toString(currentVlmApi.getVlmNr()));
                entry.setReturnCodeBit(RC_RSC_CRT_FAIL_ACC_DENIED_VLM);
            }
            controller.getErrorReporter().reportError(
                accDeniedExc,
                accCtx,
                client,
                action
            );
            entry.setCauseFormat(accDeniedExc.getMessage());
            entry.setMessageFormat(action);
            entry.putObjRef(KEY_NODE, nodeNameStr);
            entry.putObjRef(KEY_RSC_DFN, rscNameStr);

            apiCallRc.addEntry(entry);
        }
        catch (DrbdDataAlreadyExistsException dataAlreadyExistsExc)
        {
            String errorMsgFormat;
            ApiCallRcEntry entry = new ApiCallRcEntry();
            // dataAlreadyExistsExc0 cannot happen
            if (rsc == null)
            { // dataAlreadyExistsExc1
                errorMsgFormat = String.format(
                    "Resource '%s' could not be created as it already exists on node '%s'.",
                    rscNameStr,
                    nodeNameStr
                );
                entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                entry.setReturnCodeBit(RC_RSC_CRT_FAIL_EXISTS_RSC);
            }
            else
            { // dataAlreadyExistsExc2
                errorMsgFormat = String.format(
                    "Volume with volume number %d could not be created as it already exists on " +
                        "resource '%s' on node '%s'.",
                    currentVlmApi.getVlmNr(),
                    rscNameStr,
                    nodeNameStr
                );
                entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                entry.putVariable(KEY_VLM_NR, Integer.toString(currentVlmApi.getVlmNr()));
                entry.putObjRef(KEY_VLM_NR, Integer.toString(currentVlmApi.getVlmNr()));
                entry.setReturnCodeBit(RC_RSC_CRT_FAIL_EXISTS_NODE);
            }

            entry.setCauseFormat(dataAlreadyExistsExc.getMessage());
            entry.setMessageFormat(errorMsgFormat);
            entry.putObjRef(KEY_NODE, nodeNameStr);
            entry.putObjRef(KEY_RSC_DFN, rscNameStr);

            apiCallRc.addEntry(entry);
        }
        catch (ValueOutOfRangeException valueOutOfRangeExc)
        {
            String errorMsgFormat;
            ApiCallRcEntry entry = new ApiCallRcEntry();

            if (nodeId == null)
            {
                errorMsgFormat = String.format(
                    "Node id's value %d is out of its valid range (%d - %d)",
                    nodeIdRaw,
                    NodeId.NODE_ID_MIN,
                    NodeId.NODE_ID_MAX
                );
                entry.putVariable(KEY_NODE_ID, Integer.toString(nodeIdRaw));
                entry.setReturnCode(RC_RSC_CRT_FAIL_INVALID_NODE_ID);
            }
            else
            {
                errorMsgFormat = String.format(
                    "Volume number %d is out of its valid range (%d - %d)",
                    currentVlmApi.getVlmNr(),
                    VolumeNumber.VOLUME_NR_MIN,
                    VolumeNumber.VOLUME_NR_MAX
                );
                entry.putVariable(KEY_VLM_NR, Integer.toString(currentVlmApi.getVlmNr()));
                entry.setReturnCode(RC_RSC_CRT_FAIL_INVALID_VLM_NR);
            }
            entry.setCauseFormat(valueOutOfRangeExc.getMessage());
            entry.setMessageFormat(errorMsgFormat);
            entry.putObjRef(KEY_NODE, nodeNameStr);
            entry.putObjRef(KEY_RSC_DFN, rscNameStr);
        }

        if (transMgr != null)
        {
            if (transMgr.isDirty())
            {
                try
                {
                    transMgr.rollback();
                }
                catch (SQLException sqlExc)
                {
                    controller.getErrorReporter().reportError(
                        sqlExc,
                        accCtx,
                        client,
                        String.format(
                            "A database error occured while trying to rollback the creation of resource " +
                                "'%s' on node '%s'.",
                            rscNameStr,
                            nodeNameStr
                        )
                    );

                    ApiCallRcEntry entry = new ApiCallRcEntry();
                    entry.setReturnCodeBit(RC_RSC_CRT_FAIL_SQL_ROLLBACK);
                    entry.setCauseFormat(sqlExc.getMessage());
                    entry.putObjRef(KEY_NODE, nodeNameStr);
                    entry.putObjRef(KEY_RSC_DFN, rscNameStr);

                    apiCallRc.addEntry(entry);
                }
            }
            controller.dbConnPool.returnConnection(transMgr.dbCon);
        }
        return apiCallRc;
    }

    public ApiCallRc deleteResource(
        AccessContext accCtx,
        Peer client,
        String nodeNameStr,
        String rscNameStr
    )
    {
        ApiCallRcImpl apiCallRc = new ApiCallRcImpl();

        TransactionMgr transMgr = null;

        NodeName nodeName = null;
        ResourceName rscName = null;

        NodeData node = null;
        ResourceDefinitionData rscDfn = null;
        ResourceData rscData = null;

        try
        {
            transMgr = new TransactionMgr(controller.dbConnPool);

            nodeName = new NodeName(nodeNameStr);
            rscName = new ResourceName(rscNameStr);

            node = NodeData.getInstance(
                accCtx,
                nodeName,
                null,
                null,
                transMgr,
                false,
                false
            );
            rscDfn = ResourceDefinitionData.getInstance(
                accCtx,
                rscName,
                null,
                transMgr,
                false,
                false
            );
            rscData = ResourceData.getInstance(
                accCtx,
                rscDfn,
                node,
                null,
                null,
                transMgr,
                false,
                false
            );

            if (node == null)
            {
                ApiCallRcEntry nodeNotFoundEntry = new ApiCallRcEntry();
                nodeNotFoundEntry.setReturnCode(RC_RSC_DEL_FAIL_NOT_FOUND_NODE);
                nodeNotFoundEntry.setCauseFormat(
                    String.format(
                        "The specified node '%s' could not be found in the database.",
                        nodeNameStr
                    )
                );
                nodeNotFoundEntry.putVariable(KEY_NODE_NAME, nodeNameStr);
                nodeNotFoundEntry.putObjRef(KEY_NODE, nodeNameStr);
                nodeNotFoundEntry.putObjRef(KEY_RSC_DFN, rscNameStr);

                apiCallRc.addEntry(nodeNotFoundEntry);
            }
            else
            if (rscDfn == null)
            {
                ApiCallRcEntry rscDfnNotFoundEntry = new ApiCallRcEntry();
                rscDfnNotFoundEntry.setReturnCode(RC_RSC_DEL_FAIL_NOT_FOUND_RSC_DFN);
                rscDfnNotFoundEntry.setCauseFormat(
                    String.format(
                        "The specified resource definition '%s' could not be found in the database.",
                        rscNameStr
                    )
                );
                rscDfnNotFoundEntry.putVariable(KEY_RSC_NAME, rscNameStr);
                rscDfnNotFoundEntry.putObjRef(KEY_NODE, nodeNameStr);
                rscDfnNotFoundEntry.putObjRef(KEY_RSC_DFN, rscNameStr);

                apiCallRc.addEntry(rscDfnNotFoundEntry);
            }
            else
            if (rscData == null)
            {
                ApiCallRcEntry rscNotFoundEntry = new ApiCallRcEntry();
                rscNotFoundEntry.setReturnCode(RC_RSC_DEL_NOT_FOUND);
                rscNotFoundEntry.setCauseFormat(
                    String.format(
                        "The specified resource '%s' on node '%s' could not be found in the database.",
                        rscNameStr,
                        nodeNameStr
                    )
                );
                rscNotFoundEntry.putVariable(KEY_RSC_NAME, rscNameStr);
                rscNotFoundEntry.putVariable(KEY_NODE_NAME, nodeNameStr);
                rscNotFoundEntry.putObjRef(KEY_NODE, nodeNameStr);
                rscNotFoundEntry.putObjRef(KEY_RSC_DFN, rscNameStr);

                apiCallRc.addEntry(rscNotFoundEntry);
            }
            else
            {
                rscData.setConnection(transMgr);
                rscData.markDeleted(accCtx);
                transMgr.commit();

                ApiCallRcEntry entry = new ApiCallRcEntry();
                entry.setReturnCodeBit(RC_RSC_DELETED);
                String successMessage = String.format(
                    "Resource '%s' marked to be deleted from node '%s'.",
                    rscNameStr,
                    nodeNameStr
                );
                entry.setMessageFormat(successMessage);
                entry.putObjRef(KEY_NODE, nodeNameStr);
                entry.putObjRef(KEY_RSC_DFN, rscNameStr);
                entry.putObjRef(KEY_NODE_NAME, nodeNameStr);
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                apiCallRc.addEntry(entry);


                // TODO: tell satellites to remove all the corresponding resources
                // TODO: if satellites are finished (or no satellite had such a resource deployed)
                //       remove the rscDfn from the DB
                controller.getErrorReporter().logInfo(successMessage);
            }
        }
        catch (SQLException sqlExc)
        {
            String errorMessage = String.format(
                "A database error occured while trying to delete the resource '%s' on node '%s'.",
                nodeNameStr,
                rscNameStr
            );
            controller.getErrorReporter().reportError(
                sqlExc,
                accCtx,
                client,
                errorMessage
            );

            ApiCallRcEntry entry = new ApiCallRcEntry();
            entry.setReturnCodeBit(RC_RSC_DEL_FAIL_SQL);
            entry.setMessageFormat(errorMessage);
            entry.setCauseFormat(sqlExc.getMessage());
            entry.putObjRef(KEY_NODE, nodeNameStr);
            entry.putObjRef(KEY_RSC_DFN, rscNameStr);
            entry.putVariable(KEY_NODE_NAME, nodeNameStr);
            entry.putVariable(KEY_RSC_NAME, rscNameStr);

            apiCallRc.addEntry(entry);
        }
        catch (InvalidNameException invalidNameExc)
        {
            ApiCallRcEntry entry = new ApiCallRcEntry();
            String errorMessage;
            if (nodeName == null)
            {
                errorMessage = String.format("Given node name '%s' is invalid.", nodeNameStr);
                entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                entry.setReturnCodeBit(RC_RSC_DEL_FAIL_INVALID_NODE_NAME);
            }
            else
            {
                errorMessage = String.format("Given resource name '%s' is invalid.", rscNameStr);
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                entry.setReturnCodeBit(RC_RSC_DEL_FAIL_INVALID_RSC_NAME);
            }
            controller.getErrorReporter().reportError(
                invalidNameExc,
                accCtx,
                client,
                errorMessage
            );
            entry.setMessageFormat(errorMessage);
            entry.setCauseFormat(invalidNameExc.getMessage());
            entry.putObjRef(KEY_NODE, nodeNameStr);
            entry.putObjRef(KEY_RSC_DFN, rscNameStr);

            apiCallRc.addEntry(entry);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            ApiCallRcEntry entry = new ApiCallRcEntry();
            String action = "Given user has no permission to ";
            if (node == null)
            { // accDeniedExc1
                action += String.format(
                    "access the node '%s'.",
                    nodeNameStr
                );
                entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                entry.setReturnCodeBit(RC_RSC_DEL_FAIL_ACC_DENIED_NODE);
            }
            else
            if (rscDfn == null)
            { // accDeniedExc2
                action += String.format(
                    "access the resource definition '%s'.",
                    rscNameStr
                );
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                entry.setReturnCodeBit(RC_RSC_DEL_FAIL_ACC_DENIED_RSC_DFN);
            }
            else
            if (rscData == null)
            { // accDeniedExc3
                action += String.format(
                    "access the resource '%s' on node '%s'.",
                    rscNameStr,
                    nodeNameStr
                );
                entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                entry.setReturnCodeBit(RC_RSC_DEL_FAIL_ACC_DENIED_RSC);
            }
            else
            { // accDeniedExc4
                action += String.format(
                    "delete the resource '%s' on node '%s'.",
                    rscNameStr,
                    nodeNameStr
                );
                entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                entry.putVariable(KEY_RSC_NAME, rscNameStr);
                entry.setReturnCodeBit(RC_RSC_DEL_FAIL_ACC_DENIED_VLM_DFN);
            }
            controller.getErrorReporter().reportError(
                accDeniedExc,
                accCtx,
                client,
                action
            );
            entry.setCauseFormat(accDeniedExc.getMessage());
            entry.setMessageFormat(action);
            entry.putObjRef(KEY_NODE, nodeNameStr);
            entry.putObjRef(KEY_RSC_DFN, rscNameStr);

            apiCallRc.addEntry(entry);
        }
        catch (DrbdDataAlreadyExistsException dataAlreadyExistsExc)
        {
            controller.getErrorReporter().reportError(
                new ImplementationError(
                    String.format(
                        ".getInstance was called with failIfExists=false, still threw an AlreadyExistsException "+
                            "(Node name: %s, resource name: %s)",
                        nodeNameStr,
                        rscNameStr
                    ),
                    dataAlreadyExistsExc
                )
            );

            ApiCallRcEntry entry = new ApiCallRcEntry();
            entry.setReturnCodeBit(RC_RSC_DEL_FAIL_EXISTS_IMPL_ERROR);
            entry.setMessageFormat(
                String.format(
                    "Failed to delete the resource '%s' on node '%s' due to an implementation error.",
                    rscNameStr,
                    nodeNameStr
                )
            );
            entry.setCauseFormat(dataAlreadyExistsExc.getMessage());
            entry.putObjRef(KEY_RSC_DFN, rscNameStr);
            entry.putObjRef(KEY_NODE, nodeNameStr);
            entry.putVariable(KEY_RSC_NAME, rscNameStr);
            entry.putVariable(KEY_NODE_NAME, nodeNameStr);

            apiCallRc.addEntry(entry);
        }

        if (transMgr != null)
        {
            if (transMgr.isDirty())
            {
                try
                {
                    transMgr.rollback();
                }
                catch (SQLException sqlExc)
                {
                    String errorMessage = String.format(
                        "A database error occured while trying to rollback the deletion of " +
                            "resource '%s' on node '%s'.",
                        rscNameStr,
                        nodeNameStr
                    );
                    controller.getErrorReporter().reportError(
                        sqlExc,
                        accCtx,
                        client,
                        errorMessage
                    );

                    ApiCallRcEntry entry = new ApiCallRcEntry();
                    entry.setReturnCodeBit(RC_RSC_DEL_FAIL_SQL_ROLLBACK);
                    entry.setMessageFormat(errorMessage);
                    entry.setCauseFormat(sqlExc.getMessage());
                    entry.putObjRef(KEY_RSC_DFN, rscNameStr);
                    entry.putObjRef(KEY_NODE, nodeNameStr);
                    entry.putVariable(KEY_NODE_NAME, nodeNameStr);
                    entry.putVariable(KEY_RSC_NAME, rscNameStr);

                    apiCallRc.addEntry(entry);
                }
            }
            controller.dbConnPool.returnConnection(transMgr.dbCon);
        }



        return apiCallRc;
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
}