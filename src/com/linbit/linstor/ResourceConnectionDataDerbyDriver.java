package com.linbit.linstor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.TransactionMgr;
import com.linbit.linstor.dbdrivers.DerbyDriver;
import com.linbit.linstor.dbdrivers.derby.DerbyConstants;
import com.linbit.linstor.dbdrivers.interfaces.ResourceConnectionDataDatabaseDriver;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.utils.UuidUtils;

public class ResourceConnectionDataDerbyDriver implements ResourceConnectionDataDatabaseDriver
{
    private static final String TBL_RES_CON_DFN = DerbyConstants.TBL_RESOURCE_CONNECTIONS;

    private static final String UUID = DerbyConstants.UUID;
    private static final String NODE_SRC = DerbyConstants.NODE_NAME_SRC;
    private static final String NODE_DST = DerbyConstants.NODE_NAME_DST;
    private static final String RES_NAME = DerbyConstants.RESOURCE_NAME;

    private static final String SELECT =
        " SELECT " + UUID + ", " + RES_NAME + ", " + NODE_SRC + ", " + NODE_DST  +
        " FROM " + TBL_RES_CON_DFN +
        " WHERE "+ NODE_SRC + " = ? AND " +
                   NODE_DST + " = ? AND " +
                   RES_NAME + " = ?";
    private static final String SELECT_BY_RES_SRC_OR_DST =
        " SELECT " + UUID + ", " + RES_NAME + ", " + NODE_SRC + ", " + NODE_DST  +
        " FROM " + TBL_RES_CON_DFN +
        " WHERE ( " + NODE_SRC + " = ? OR " +
                      NODE_DST + " = ?"+
                   " ) AND " +
                   RES_NAME + " = ?";

    private static final String INSERT =
        " INSERT INTO " + TBL_RES_CON_DFN +
        " (" + UUID + ", " + RES_NAME + ", " + NODE_SRC + ", " + NODE_DST + ")"+
        " VALUES (?, ?, ?, ?)";
    private static final String DELETE =
        " DELETE FROM " + TBL_RES_CON_DFN +
        " WHERE "+ NODE_SRC + " = ? AND " +
                   NODE_DST + " = ? AND " +
                   RES_NAME + " = ?";

    private final AccessContext dbCtx;
    private final ErrorReporter errorReporter;

    private NodeDataDerbyDriver nodeDataDerbyDriver;
    private ResourceDataDerbyDriver resourceDataDerbyDriver;

    public ResourceConnectionDataDerbyDriver(
        AccessContext accCtx,
        ErrorReporter errorReporterRef
    )
    {
        dbCtx = accCtx;
        errorReporter = errorReporterRef;
    }

    public void initialize(NodeDataDerbyDriver nodeDataDerbyDriverRef,ResourceDataDerbyDriver resourceDataDerbyDriverRef)
    {
        nodeDataDerbyDriver = nodeDataDerbyDriverRef;
        resourceDataDerbyDriver = resourceDataDerbyDriverRef;
    }

    @Override
    public ResourceConnectionData load(
        Resource sourceResource,
        Resource targetResource,
        boolean logWarnIfNotExists,
        TransactionMgr transMgr
    )
        throws SQLException
    {
        errorReporter.logTrace("Loading ResourceConnection %s", getId(sourceResource, targetResource));

        ResourceDefinition resDfn = sourceResource.getDefinition();
        if (resDfn != targetResource.getDefinition())
        {
            throw new ImplementationError(
                String.format(
                    "Failed to load ResourceConnection between unrelated resources. %s %s",
                    getResourceTraceId(sourceResource),
                    getResourceTraceId(targetResource)
                ),
                null
            );
        }

        ResourceConnectionData ret = null;
        try (PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT))
        {
            stmt.setString(1, sourceResource.getAssignedNode().getName().value);
            stmt.setString(2, targetResource.getAssignedNode().getName().value);
            stmt.setString(3, sourceResource.getDefinition().getName().value);

            try (ResultSet resultSet = stmt.executeQuery())
            {
                if (resultSet.next())
                {
                    ret = restoreResourceConnection(resultSet, transMgr);
                    // traceLog about loaded from DB|cache in restoreConDfn method
                }
                else
                if (logWarnIfNotExists)
                {
                    errorReporter.logWarning(
                        "ResourceConnection not found in DB %s",
                        getId(sourceResource, targetResource)
                    );
                }
            }
        }
        return ret;
    }

    private ResourceConnectionData restoreResourceConnection(
        ResultSet resultSet,
        TransactionMgr transMgr
    )
        throws SQLException
    {

        NodeName sourceNodeName = null;
        NodeName targetNodeName = null;
        ResourceName resourceName = null;
        try
        {
            sourceNodeName = new NodeName(resultSet.getString(NODE_SRC));
            targetNodeName = new NodeName(resultSet.getString(NODE_DST));
            resourceName = new ResourceName(resultSet.getString(RES_NAME));
        }
        catch (InvalidNameException invalidNameExc)
        {
            String col;
            String format = "The stored %s in table %s could not be restored. ";
            if (sourceNodeName == null)
            {
                col = "SourceNodeName";
                format += "(invalid SourceNodeName=%s, TargetNodeName=%s, ResourceName=%s)";
            }
            else
            if (targetNodeName == null)
            {
                col = "TargetNodeName";
                format += "(SourceNodeName=%s, invalid TargetNodeName=%s, ResourceName=%s)";
            }
            else
            {
                col = "ResourceName";
                format += "(SourceNodeName=%s, TargetNodeName=%s, invalid ResourceName=%s)";
            }

            throw new LinStorSqlRuntimeException(
                String.format(
                    format,
                    col,
                    TBL_RES_CON_DFN,
                    resultSet.getString(NODE_SRC),
                    resultSet.getString(NODE_DST),
                    resultSet.getString(RES_NAME)
                ),
                invalidNameExc
            );
        }

        Node sourceNode = nodeDataDerbyDriver.load(sourceNodeName, true, transMgr);
        Node targetNode = nodeDataDerbyDriver.load(targetNodeName, true, transMgr);
        Resource sourceResource = resourceDataDerbyDriver.load(sourceNode, resourceName, true, transMgr);
        Resource targetResource = resourceDataDerbyDriver.load(targetNode, resourceName, true, transMgr);

        ResourceConnectionData resConData = cacheGet(sourceResource, targetResource);
        if (resConData == null)
        {
            try
            {
                resConData = new ResourceConnectionData(
                    UuidUtils.asUuid(resultSet.getBytes(UUID)),
                    dbCtx,
                    sourceResource,
                    targetResource,
                    transMgr
                );
            }
            catch (AccessDeniedException accDeniedExc)
            {
                DerbyDriver.handleAccessDeniedException(accDeniedExc);
            }
            errorReporter.logTrace("ResourceConnection loaded from DB %s", getId(resConData));
        }
        else
        {
            errorReporter.logTrace("ResourceConnection loaded from cache %s", getId(resConData));
        }

        return resConData;
    }

    @Override
    public List<ResourceConnectionData> loadAllByResource(
        Resource resource,
        TransactionMgr transMgr
    )
        throws SQLException
    {
        errorReporter.logTrace(
            "Loading all ResourceConnections for Resource %s",
            getResourceTraceId(resource)
        );

        List<ResourceConnectionData> connections = new ArrayList<>();
        try (PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_BY_RES_SRC_OR_DST))
        {
            NodeName nodeName = resource.getAssignedNode().getName();
            stmt.setString(1, nodeName.value);
            stmt.setString(2, nodeName.value);
            stmt.setString(3, resource.getDefinition().getName().value);
            try (ResultSet resultSet = stmt.executeQuery())
            {
                while (resultSet.next())
                {
                    ResourceConnectionData conDfn = restoreResourceConnection(
                        resultSet,
                        transMgr
                    );
                    connections.add(conDfn);
                }
            }
        }

        errorReporter.logTrace(
            "%d ResourceConnections loaded for Resource %s",
            connections.size(),
            getResourceDebugId(resource)
        );
        return connections;
    }

    @Override
    public void create(ResourceConnectionData conDfnData, TransactionMgr transMgr) throws SQLException
    {
        errorReporter.logTrace("Creating ResourceConnection %s", getId(conDfnData));
        try (PreparedStatement stmt = transMgr.dbCon.prepareStatement(INSERT))
        {
            NodeName sourceNodeName = conDfnData.getSourceResource(dbCtx).getAssignedNode().getName();
            NodeName targetNodeName = conDfnData.getTargetResource(dbCtx).getAssignedNode().getName();
            ResourceName resName = conDfnData.getSourceResource(dbCtx).getDefinition().getName();

            stmt.setBytes(1, UuidUtils.asByteArray(conDfnData.getUuid()));
            stmt.setString(2, resName.value);
            stmt.setString(3, sourceNodeName.value);
            stmt.setString(4, targetNodeName.value);

            stmt.executeUpdate();

            errorReporter.logTrace("ResourceConnection created s", getId(conDfnData));
        }
        catch (AccessDeniedException accessDeniedExc)
        {
            DerbyDriver.handleAccessDeniedException(accessDeniedExc);
        }
    }

    @Override
    public void delete(ResourceConnectionData conDfnData, TransactionMgr transMgr) throws SQLException
    {
        errorReporter.logTrace("Deleting ResourceConnection %s", getId(conDfnData));
        try
        {
            NodeName sourceNodeName = conDfnData.getSourceResource(dbCtx).getAssignedNode().getName();
            NodeName targetNodeName = conDfnData.getTargetResource(dbCtx).getAssignedNode().getName();
            ResourceName resName = conDfnData.getSourceResource(dbCtx).getDefinition().getName();

            try (PreparedStatement stmt = transMgr.dbCon.prepareStatement(DELETE))
            {
                stmt.setString(1, sourceNodeName.value);
                stmt.setString(2, targetNodeName.value);
                stmt.setString(3, resName.value);

                stmt.executeUpdate();
            }
            errorReporter.logTrace("ResourceConnection deleted %s", getId(conDfnData));
        }
        catch (AccessDeniedException accDeniedExc)
        {
            DerbyDriver.handleAccessDeniedException(accDeniedExc);
        }
    }

    private ResourceConnectionData cacheGet(
        Resource sourceResource,
        Resource targetResource
    )
    {
        ResourceConnectionData ret = null;
        try
        {
            ret = (ResourceConnectionData) sourceResource.getResourceConnection(dbCtx, targetResource);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            DerbyDriver.handleAccessDeniedException(accDeniedExc);
        }
        return ret;
    }

    private String getId(ResourceConnectionData conData)
    {
        String id = null;
        try
        {
            id = getId(
                conData.getSourceResource(dbCtx).getAssignedNode().getName().displayValue,
                conData.getTargetResource(dbCtx).getAssignedNode().getName().displayValue,
                conData.getSourceResource(dbCtx).getDefinition().getName().displayValue
            );
        }
        catch (AccessDeniedException accDeniedExc)
        {
            DerbyDriver.handleAccessDeniedException(accDeniedExc);
        }
        return id;
    }

    private String getId(Resource src, Resource dst)
    {
        return getId(
            src.getAssignedNode().getName().displayValue,
            dst.getAssignedNode().getName().displayValue,
            src.getDefinition().getName().displayValue
        );
    }

    private String getId(String sourceName, String targetName, String resName)
    {
        return "(SourceNode=" + sourceName + " TargetNode=" + targetName + " ResName=" + resName +")";
    }

    private String getResourceTraceId(Resource resource)
    {
        return getResourceId(
            resource.getAssignedNode().getName().value,
            resource.getDefinition().getName().value
        );
    }

    private String getResourceDebugId(Resource resource)
    {
        return getResourceId(
            resource.getAssignedNode().getName().displayValue,
            resource.getDefinition().getName().displayValue
        );
    }

    private String getResourceId(String nodeName, String resourceName)
    {
        return "(NodeName=" + nodeName + " ResName=" + resourceName + ")";
    }
}
