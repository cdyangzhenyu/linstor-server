package com.linbit.linstor;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.UUID;

import com.linbit.ErrorCheck;
import com.linbit.ImplementationError;
import com.linbit.SatelliteTransactionMgr;
import com.linbit.TransactionMap;
import com.linbit.TransactionMgr;
import com.linbit.TransactionObject;
import com.linbit.TransactionSimpleObject;
import com.linbit.linstor.api.pojo.NodePojo;
import com.linbit.linstor.api.pojo.NodePojo.NodeConnPojo;
import com.linbit.linstor.core.LinStor;
import com.linbit.linstor.dbdrivers.interfaces.NodeDataDatabaseDriver;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.propscon.PropsAccess;
import com.linbit.linstor.propscon.PropsContainer;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.AccessType;
import com.linbit.linstor.security.ObjectProtection;
import com.linbit.linstor.stateflags.StateFlags;
import com.linbit.linstor.stateflags.StateFlagsBits;
import com.linbit.linstor.stateflags.StateFlagsPersistence;
import com.linbit.linstor.storage.DisklessDriver;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public class NodeData extends BaseTransactionObject implements Node
{
    // Object identifier
    private final UUID objId;

    // Runtime instance identifier for debug purposes
    private final transient UUID dbgInstanceId;

    // Node name
    private final NodeName clNodeName;

    // State flags
    private final StateFlags<NodeFlag> flags;

    // Node type
    private final TransactionSimpleObject<NodeData, NodeType> nodeType;

    // List of resources assigned to this cluster node
    private final TransactionMap<ResourceName, Resource> resourceMap;

    // List of network interfaces used for replication on this cluster node
    private final TransactionMap<NetInterfaceName, NetInterface> netInterfaceMap;

    // List of storage pools
    private final TransactionMap<StorPoolName, StorPool> storPoolMap;

    // Map to the other endpoint of a node connection (this is NOT necessarily the source!)
    private final TransactionMap<Node, NodeConnection> nodeConnections;

    // Access controls for this object
    private final ObjectProtection objProt;

    // Properties container for this node
    private final Props nodeProps;

    private final NodeDataDatabaseDriver dbDriver;

    private transient Peer peer;

    private transient SatelliteConnection satelliteConnection;

    private TransactionSimpleObject<NodeData, Boolean> deleted;

    private transient StorPoolData disklessStorPool;

    /*
     * Only used by getInstance method
     */
    private NodeData(
        AccessContext accCtx,
        NodeName nameRef,
        NodeType type,
        long initialFlags,
        TransactionMgr transMgr
    )
        throws SQLException, AccessDeniedException
    {
        this(
            accCtx,
            UUID.randomUUID(),
            ObjectProtection.getInstance(
                accCtx,
                ObjectProtection.buildPath(nameRef),
                true,
                transMgr
            ),
            nameRef,
            type,
            initialFlags,
            UUID.randomUUID(),
            transMgr
        );
    }

    /*
     * Used by dbDrivers and tests
     */
    NodeData(
        AccessContext accCtx,
        UUID uuidRef,
        ObjectProtection objProtRef,
        NodeName nameRef,
        NodeType type,
        long initialFlags,
        UUID disklessStorPoolUuid,
        TransactionMgr transMgr
    )
        throws SQLException, AccessDeniedException
    {
        ErrorCheck.ctorNotNull(NodeData.class, NodeName.class, nameRef);

        objId = uuidRef;
        dbgInstanceId = UUID.randomUUID();
        objProt = objProtRef;
        clNodeName = nameRef;
        dbDriver = LinStor.getNodeDataDatabaseDriver();

        resourceMap = new TransactionMap<>(new TreeMap<ResourceName, Resource>(), null);
        netInterfaceMap = new TransactionMap<>(new TreeMap<NetInterfaceName, NetInterface>(), null);
        storPoolMap = new TransactionMap<>(new TreeMap<StorPoolName, StorPool>(), null);
        deleted = new TransactionSimpleObject<>(this, false, null);

        nodeProps = PropsContainer.getInstance(
            PropsContainer.buildPath(nameRef),
            transMgr
        );
        nodeConnections = new TransactionMap<>(new HashMap<Node, NodeConnection>(), null);

        flags = new NodeFlagsImpl(this, objProt, dbDriver.getStateFlagPersistence(), initialFlags);
        if (type == null)
        {
            // Default to creating an AUXILIARY type node
            type = NodeType.AUXILIARY;
        }
        nodeType = new TransactionSimpleObject<NodeData, Node.NodeType>(this, type, dbDriver.getNodeTypeDriver());

        transObjs = Arrays.<TransactionObject> asList(
            flags,
            nodeType,
            objProt,
            resourceMap,
            netInterfaceMap,
            storPoolMap,
            nodeConnections,
            nodeProps,
            deleted
        );
    }

    public static NodeData getInstance(
        AccessContext accCtx,
        NodeName nameRef,
        NodeType type,
        NodeFlag[] flags,
        TransactionMgr transMgr,
        boolean createIfNotExists,
        boolean failIfExists
    )
        throws SQLException, AccessDeniedException, LinStorDataAlreadyExistsException
    {
        NodeData nodeData = null;

        NodeDataDatabaseDriver dbDriver = LinStor.getNodeDataDatabaseDriver();
        nodeData = dbDriver.load(nameRef, false, transMgr);

        if (failIfExists && nodeData != null)
        {
            throw new LinStorDataAlreadyExistsException("The Node already exists");
        }

        if (nodeData == null && createIfNotExists)
        {
            nodeData = new NodeData(
                accCtx,
                nameRef,
                type,
                StateFlagsBits.getMask(flags),
                transMgr
            );
            dbDriver.create(nodeData, transMgr);

            nodeData.disklessStorPool = StorPoolData.getInstance(
                accCtx,
                nodeData,
                LinStor.getDisklessStorPoolDfn(),
                DisklessDriver.class.getSimpleName(),
                transMgr,
                createIfNotExists,
                failIfExists
            );

        }
        if (nodeData != null)
        {
            nodeData.initialized();
            nodeData.setConnection(transMgr);
        }
        return nodeData;
    }

    public static NodeData getInstanceSatellite(
        AccessContext accCtx,
        UUID uuid,
        NodeName nameRef,
        NodeType typeRef,
        NodeFlag[] flags,
        UUID disklessStorPoolUuid,
        SatelliteTransactionMgr transMgr,
        SatelliteCoreServices stltCoreSvcs
    )
        throws ImplementationError
    {
        NodeData nodeData = null;
        NodeDataDatabaseDriver dbDriver = LinStor.getNodeDataDatabaseDriver();
        try
        {
            nodeData = dbDriver.load(nameRef, false, transMgr);
            if (nodeData == null)
            {
                nodeData = new NodeData(
                    accCtx,
                    uuid,
                    ObjectProtection.getInstance(
                        accCtx,
                        "",
                        true,
                        transMgr
                    ),
                    nameRef,
                    typeRef,
                    StateFlagsBits.getMask(flags),
                    disklessStorPoolUuid,
                    transMgr
                );

                nodeData.disklessStorPool = StorPoolData.getInstanceSatellite(
                    accCtx,
                    disklessStorPoolUuid,
                    nodeData,
                    LinStor.getDisklessStorPoolDfn(),
                    DisklessDriver.class.getSimpleName(),
                    transMgr,
                    stltCoreSvcs
                );
            }
            nodeData.initialized();
            nodeData.setConnection(transMgr);
        }
        catch (Exception exc)
        {
            throw new ImplementationError(
                "This method should only be called with a satellite db in background!",
                exc
            );
        }
        return nodeData;
    }


    @Override
    public UUID getUuid()
    {
        checkDeleted();
        return objId;
    }

    @Override
    public NodeName getName()
    {
        checkDeleted();
        return clNodeName;
    }

    @Override
    public Resource getResource(AccessContext accCtx, ResourceName resName)
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);
        return resourceMap.get(resName);
    }

    @Override
    public NodeConnection getNodeConnection(AccessContext accCtx, Node otherNode)
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);
        otherNode.getObjProt().requireAccess(accCtx, AccessType.VIEW);
        return nodeConnections.get(otherNode);
    }

    @Override
    public void setNodeConnection(AccessContext accCtx, NodeConnection nodeConnection)
        throws AccessDeniedException
    {
        checkDeleted();
        Node sourceNode = nodeConnection.getSourceNode(accCtx);
        Node targetNode = nodeConnection.getTargetNode(accCtx);

        sourceNode.getObjProt().requireAccess(accCtx, AccessType.CHANGE);
        targetNode.getObjProt().requireAccess(accCtx, AccessType.CHANGE);

        if (sourceNode == this)
        {
            nodeConnections.put(targetNode, nodeConnection);
        }
        else
        {
            nodeConnections.put(sourceNode, nodeConnection);
        }
    }

    @Override
    public void removeNodeConnection(AccessContext accCtx, NodeConnection nodeConnection)
        throws AccessDeniedException
    {
        checkDeleted();

        Node sourceNode = nodeConnection.getSourceNode(accCtx);
        Node targetNode = nodeConnection.getTargetNode(accCtx);

        sourceNode.getObjProt().requireAccess(accCtx, AccessType.CHANGE);
        targetNode.getObjProt().requireAccess(accCtx, AccessType.CHANGE);

        if (sourceNode == this)
        {
            nodeConnections.remove(targetNode);
        }
        else
        {
            nodeConnections.remove(sourceNode);
        }
    }

    @Override
    public ObjectProtection getObjProt()
    {
        checkDeleted();
        return objProt;
    }

    @Override
    public Props getProps(AccessContext accCtx)
        throws AccessDeniedException
    {
        checkDeleted();
        return PropsAccess.secureGetProps(accCtx, objProt, nodeProps);
    }

    @Override
    public void addResource(AccessContext accCtx, Resource resRef) throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.USE);

        resourceMap.put(resRef.getDefinition().getName(), resRef);
    }

    void removeResource(AccessContext accCtx, Resource resRef) throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.USE);

        resourceMap.remove(resRef.getDefinition().getName());
    }

    @Override
    public int getResourceCount()
    {
        return resourceMap.size();
    }

    @Override
    public Iterator<Resource> iterateResources(AccessContext accCtx)
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);

        return resourceMap.values().iterator();
    }

    @Override
    public NetInterface getNetInterface(AccessContext accCtx, NetInterfaceName niName) throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);

        return netInterfaceMap.get(niName);
    }

    void addNetInterface(AccessContext accCtx, NetInterface niRef) throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.CHANGE);

        netInterfaceMap.put(niRef.getName(), niRef);
    }

    void removeNetInterface(AccessContext accCtx, NetInterface niRef) throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.CHANGE);

        netInterfaceMap.remove(niRef.getName());
    }

    @Override
    public Iterator<NetInterface> iterateNetInterfaces(AccessContext accCtx)
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);

        return netInterfaceMap.values().iterator();
    }

    @Override
    public StorPool getStorPool(AccessContext accCtx, StorPoolName poolName)
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);

        return storPoolMap.get(poolName);
    }

    @Override
    public StorPool getDisklessStorPool(AccessContext accCtx) throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);

        return disklessStorPool;
    }

    void addStorPool(AccessContext accCtx, StorPool pool)
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.CHANGE);

        storPoolMap.put(pool.getName(), pool);
    }

    void removeStorPool(AccessContext accCtx, StorPool pool)
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.CHANGE);

        storPoolMap.remove(pool.getName());
    }


    void setDisklessStorPool(StorPoolData disklessStorPool)
    {
        this.disklessStorPool = disklessStorPool;
    }

    @Override
    public int getStorPoolCount()
    {
        return storPoolMap.size();
    }

    @Override
    public Iterator<StorPool> iterateStorPools(AccessContext accCtx)
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);

        return storPoolMap.values().iterator();
    }

    public void setNodeType(AccessContext accCtx, NodeType newType)
        throws AccessDeniedException, SQLException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.CHANGE);

        nodeType.set(newType);
    }

    @Override
    public NodeType getNodeType(AccessContext accCtx)
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);

        return nodeType.get();
    }

    @Override
    public boolean hasNodeType(AccessContext accCtx, NodeType reqType)
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);

        long reqFlags = reqType.getFlagValue();
        return (nodeType.get().getFlagValue() & reqFlags) == reqFlags;
    }

    @Override
    public StateFlags<NodeFlag> getFlags()
    {
        checkDeleted();
        return flags;
    }

    @Override
    public Peer getPeer(AccessContext accCtx) throws AccessDeniedException
    {
        objProt.requireAccess(accCtx, AccessType.VIEW);
        return peer;
    }

    @Override
    public void setPeer(AccessContext accCtx, Peer peerRef) throws AccessDeniedException
    {
        objProt.requireAccess(accCtx, AccessType.CHANGE);
        peer = peerRef;
    }

    @Override
    public SatelliteConnection getSatelliteConnection(AccessContext accCtx) throws AccessDeniedException
    {
        objProt.requireAccess(accCtx, AccessType.VIEW);
        return satelliteConnection;
    }

    @Override
    public void setSatelliteConnection(AccessContext accCtx, SatelliteConnection satelliteConnectionRef)
        throws AccessDeniedException
    {
        objProt.requireAccess(accCtx, AccessType.CHANGE);
        satelliteConnection = satelliteConnectionRef;
    }

    @Override
    public void markDeleted(AccessContext accCtx) throws AccessDeniedException, SQLException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.CONTROL);
        getFlags().enableFlags(accCtx, NodeFlag.DELETE);
    }

    @Override
    public void delete(AccessContext accCtx)
        throws AccessDeniedException, SQLException
    {
        if (!deleted.get())
        {
            objProt.requireAccess(accCtx, AccessType.CONTROL);

            // preventing ConcurrentModificationException
            ArrayList<NodeConnection> values = new ArrayList<>(nodeConnections.values());
            for (NodeConnection nodeConn : values)
            {
                nodeConn.delete(accCtx);
            }

            objProt.delete(accCtx);
            dbDriver.delete(this, transMgr);

            deleted.set(true);
        }
    }

    public boolean isDeleted()
    {
        return deleted.get();
    }

    private void checkDeleted()
    {
        if (deleted.get())
        {
            throw new ImplementationError("Access to deleted node", null);
        }
    }

    @Override
    public NodeApi getApiData(
        AccessContext accCtx,
        Long fullSyncId,
        Long updateId
    )
        throws AccessDeniedException
    {
        List<NetInterface.NetInterfaceApi> netInterfaces = new ArrayList<>();
        Iterator<NetInterface> itNetInterfaces = iterateNetInterfaces(accCtx);
        while (itNetInterfaces.hasNext())
        {
            NetInterface ni = itNetInterfaces.next();
            netInterfaces.add(ni.getApiData(accCtx));
        }
        List<NodeConnPojo> nodeConns = new ArrayList<>();
        for (NodeConnection nodeConn : nodeConnections.values())
        {
            Node otherNode;

            Node sourceNode = nodeConn.getSourceNode(accCtx);
            if (this.equals(sourceNode))
            {
                otherNode = nodeConn.getTargetNode(accCtx);
            }
            else
            {
                otherNode = sourceNode;
            }
            nodeConns.add(
                new NodeConnPojo(
                    nodeConn.getUuid(),
                    otherNode.getUuid(),
                    otherNode.getName().displayValue,
                    otherNode.getNodeType(accCtx).name(),
                    otherNode.getFlags().getFlagsBits(accCtx),
                    nodeConn.getProps(accCtx).map(),
                    otherNode.getDisklessStorPool(accCtx).getUuid()
                )
            );
        }

        Peer peer = getPeer(accCtx);

        return new NodePojo(
            getUuid(),
            getName().getDisplayName(),
            getNodeType(accCtx).name(),
            getFlags().getFlagsBits(accCtx),
            netInterfaces,
            nodeConns,
            getProps(accCtx).map(),
            peer != null && peer.isConnected(),
            disklessStorPool.getUuid(),
            fullSyncId,
            updateId
        );
    }

    @Override
    public String toString()
    {
        return "Node: '" + clNodeName + "'";
    }

    @Override
    public UUID debugGetVolatileUuid()
    {
        return dbgInstanceId;
    }

    private static final class NodeFlagsImpl extends StateFlagsBits<NodeData, NodeFlag>
    {
        NodeFlagsImpl(NodeData parent, ObjectProtection objProtRef, StateFlagsPersistence<NodeData> persistenceRef, long initialFlags)
        {
            super(objProtRef, parent, StateFlagsBits.getMask(NodeFlag.values()), persistenceRef, initialFlags);
        }
    }
}
