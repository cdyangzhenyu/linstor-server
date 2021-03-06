package com.linbit.linstor;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.ServiceName;
import com.linbit.SingleColumnDatabaseDriver;
import com.linbit.TransactionMgr;
import com.linbit.linstor.Node.NodeType;
import com.linbit.linstor.ResourceDefinition.TransportType;
import com.linbit.linstor.SatelliteConnection.EncryptionType;
import com.linbit.linstor.dbdrivers.DatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.NetInterfaceDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.NodeConnectionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.NodeDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.PropsConDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.ResourceConnectionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.ResourceDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.ResourceDefinitionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.SatelliteConnectionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.StorPoolDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.StorPoolDefinitionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.VolumeConnectionDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.VolumeDataDatabaseDriver;
import com.linbit.linstor.dbdrivers.interfaces.VolumeDefinitionDataDatabaseDriver;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.stateflags.StateFlagsPersistence;

public class SatelliteDbDriver implements DatabaseDriver
{
    public static final ServiceName DFLT_SERVICE_INSTANCE_NAME;

    static
    {
        try
        {
            DFLT_SERVICE_INSTANCE_NAME = new ServiceName("EmptyDatabaseService");
        }
        catch (InvalidNameException nameExc)
        {
            throw new ImplementationError(
                "The builtin default service instance name is not a valid ServiceName",
                nameExc
            );
        }
    }

    private final PropsConDatabaseDriver propsDriver = new SatellitePropDriver();
    private final NodeDataDatabaseDriver nodeDriver = new SatelliteNodeDriver();
    private final ResourceDataDatabaseDriver resourceDriver = new SatelliteResDriver();
    private final ResourceDefinitionDataDatabaseDriver resourceDefinitionDriver = new SatelliteResDfnDriver();
    private final VolumeDataDatabaseDriver volumeDriver = new SatelliteVolDriver();
    private final VolumeDefinitionDataDatabaseDriver volumeDefinitionDriver = new SatelliteVolDfnDriver();
    private final StorPoolDefinitionDataDatabaseDriver storPoolDriver = new SatelliteSpDriver();
    private final StorPoolDataDatabaseDriver storPoolDefinitionDriver = new SatelliteSpdDriver();
    private final NetInterfaceDataDatabaseDriver netInterfaceDriver = new SatelliteNiDriver();
    private final SatelliteConnectionDataDatabaseDriver satelliteConnectionDriver = new SatelliteConnectionDriver();
    private final NodeConnectionDataDatabaseDriver nodeConnectionDriver = new SatelliteNodeConDfnDriver();
    private final ResourceConnectionDataDatabaseDriver resourceConnectionDriver = new SatelliteResConDfnDriver();
    private final VolumeConnectionDataDatabaseDriver volumeConnectionDriver = new SatelliteVolConDfnDriver();

    private final StateFlagsPersistence<?> stateFlagsDriver = new SatelliteFlagDriver();
    private final SingleColumnDatabaseDriver<?, ?> singleColDriver = new SatelliteSingleColDriver<>();

    private final String dbUrl = "NO_OP";

    private final AccessContext dbCtx;
    private final Map<NodeName, Node> nodesMap;
    private final Map<ResourceName, ResourceDefinition> resDfnMap;
    private final Map<StorPoolName, StorPoolDefinition> storPoolDfnMap;

    public SatelliteDbDriver(
        AccessContext privCtx,
        Map<NodeName, Node> nodesMapRef,
        Map<ResourceName, ResourceDefinition> rscDfnMapRef,
        Map<StorPoolName, StorPoolDefinition> storPoolDfnMapRef
    )
    {
        dbCtx = privCtx;
        nodesMap = nodesMapRef;
        resDfnMap = rscDfnMapRef;
        storPoolDfnMap = storPoolDfnMapRef;
    }

    @Override
    public void loadAll(TransactionMgr transMgr)
    {
        // no-op
    }

    @Override
    public ServiceName getDefaultServiceInstanceName()
    {
        return DFLT_SERVICE_INSTANCE_NAME;
    }

    @Override
    public String getDefaultConnectionUrl()
    {
        return dbUrl;
    }

    @Override
    public PropsConDatabaseDriver getPropsDatabaseDriver()
    {
        return propsDriver;
    }

    @Override
    public NodeDataDatabaseDriver getNodeDatabaseDriver()
    {
        return nodeDriver;
    }

    @Override
    public ResourceDataDatabaseDriver getResourceDataDatabaseDriver()
    {
        return resourceDriver;
    }

    @Override
    public ResourceDefinitionDataDatabaseDriver getResourceDefinitionDataDatabaseDriver()
    {
        return resourceDefinitionDriver;
    }

    @Override
    public VolumeDataDatabaseDriver getVolumeDataDatabaseDriver()
    {
        return volumeDriver;
    }

    @Override
    public VolumeDefinitionDataDatabaseDriver getVolumeDefinitionDataDatabaseDriver()
    {
        return volumeDefinitionDriver;
    }

    @Override
    public StorPoolDefinitionDataDatabaseDriver getStorPoolDefinitionDataDatabaseDriver()
    {
        return storPoolDriver;
    }

    @Override
    public StorPoolDataDatabaseDriver getStorPoolDataDatabaseDriver()
    {
        return storPoolDefinitionDriver;
    }

    @Override
    public NetInterfaceDataDatabaseDriver getNetInterfaceDataDatabaseDriver()
    {
        return netInterfaceDriver;
    }

    @Override
    public SatelliteConnectionDataDatabaseDriver getSatelliteConnectionDataDatabaseDriver()
    {
        return satelliteConnectionDriver;
    }

    @Override
    public NodeConnectionDataDatabaseDriver getNodeConnectionDataDatabaseDriver()
    {
        return nodeConnectionDriver;
    }

    @Override
    public ResourceConnectionDataDatabaseDriver getResourceConnectionDataDatabaseDriver()
    {
        return resourceConnectionDriver;
    }

    @Override
    public VolumeConnectionDataDatabaseDriver getVolumeConnectionDataDatabaseDriver()
    {
        return volumeConnectionDriver;
    }

    private class SatellitePropDriver implements PropsConDatabaseDriver
    {
        @Override
        public Map<String, String> load(String instanceName, TransactionMgr transMgr)
        {
            return Collections.emptyMap();
        }

        @Override
        public void persist(String instanceName, String key, String value, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public void persist(String instanceName, Map<String, String> props, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public void remove(String instanceName, String key, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public void remove(String instanceName, Set<String> keys, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public void removeAll(String instanceName, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteNodeDriver implements NodeDataDatabaseDriver
    {
        @SuppressWarnings("unchecked")
        @Override
        public StateFlagsPersistence<NodeData> getStateFlagPersistence()
        {
            return (StateFlagsPersistence<NodeData>) stateFlagsDriver;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SingleColumnDatabaseDriver<NodeData, NodeType> getNodeTypeDriver()
        {
            return (SingleColumnDatabaseDriver<NodeData, NodeType>) singleColDriver;
        }

        @Override
        public void create(NodeData node, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public NodeData load(NodeName nodeName, boolean logWarnIfNotExists, TransactionMgr transMgr)
        {
            return (NodeData) nodesMap.get(nodeName);
        }

        @Override
        public void delete(NodeData node, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteResDriver implements ResourceDataDatabaseDriver
    {
        @SuppressWarnings("unchecked")
        @Override
        public StateFlagsPersistence<ResourceData> getStateFlagPersistence()
        {
            return (StateFlagsPersistence<ResourceData>) stateFlagsDriver;
        }

        @Override
        public void create(ResourceData res, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public ResourceData load(
            Node node,
            ResourceName resourceName,
            boolean logWarnIfNotExists,
            TransactionMgr transMgr
        )

        {
            ResourceData resource = null;
            try
            {
                resource = (ResourceData) node.getResource(dbCtx, resourceName);
            }
            catch (AccessDeniedException accDeniedExc)
            {
                handleAccessDeniedException(accDeniedExc);
            }
            return resource;
        }

        @Override
        public void delete(ResourceData resourceData, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteResDfnDriver implements ResourceDefinitionDataDatabaseDriver
    {
        @SuppressWarnings("unchecked")
        @Override
        public StateFlagsPersistence<ResourceDefinitionData> getStateFlagsPersistence()
        {
            return (StateFlagsPersistence<ResourceDefinitionData>) stateFlagsDriver;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SingleColumnDatabaseDriver<ResourceDefinitionData, TcpPortNumber> getPortDriver()
        {
            return (SingleColumnDatabaseDriver<ResourceDefinitionData, TcpPortNumber>) singleColDriver;
        }

        @Override
        public void create(ResourceDefinitionData resDfn, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public boolean exists(ResourceName resourceName, TransactionMgr transMgr)
        {
            return resDfnMap.containsKey(resourceName);
        }

        @Override
        public ResourceDefinitionData load(
            ResourceName resourceName,
            boolean logWarnIfNotExists,
            TransactionMgr transMgr
        )
        {
            return (ResourceDefinitionData) resDfnMap.get(resourceName);
        }

        @Override
        public void delete(ResourceDefinitionData data, TransactionMgr transMgr)
        {
            // no-op
        }

        @SuppressWarnings("unchecked")
        @Override
        public SingleColumnDatabaseDriver<ResourceDefinitionData, TransportType> getTransportTypeDriver()
        {
            return (SingleColumnDatabaseDriver<ResourceDefinitionData, TransportType>) singleColDriver;
        }
    }

    private class SatelliteVolDriver implements VolumeDataDatabaseDriver
    {
        @SuppressWarnings("unchecked")
        @Override
        public StateFlagsPersistence<VolumeData> getStateFlagsPersistence()
        {
            return (StateFlagsPersistence<VolumeData>) stateFlagsDriver;
        }

        @Override
        public VolumeData load(
            Resource resource,
            VolumeDefinition volumeDefinition,
            boolean logWarnIfNotExists,
            TransactionMgr transMgr
        )
        {
            return (VolumeData) resource.getVolume(volumeDefinition.getVolumeNumber());
        }

        @Override
        public void create(VolumeData vol, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public void delete(VolumeData data, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteVolDfnDriver implements VolumeDefinitionDataDatabaseDriver
    {
        @SuppressWarnings("unchecked")
        @Override
        public StateFlagsPersistence<VolumeDefinitionData> getStateFlagsPersistence()
        {
            return (StateFlagsPersistence<VolumeDefinitionData>) stateFlagsDriver;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SingleColumnDatabaseDriver<VolumeDefinitionData, MinorNumber> getMinorNumberDriver()
        {
            return (SingleColumnDatabaseDriver<VolumeDefinitionData, MinorNumber>) singleColDriver;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SingleColumnDatabaseDriver<VolumeDefinitionData, Long> getVolumeSizeDriver()
        {
            return (SingleColumnDatabaseDriver<VolumeDefinitionData, Long>) singleColDriver;
        }

        @Override
        public void create(VolumeDefinitionData volDfnData, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public VolumeDefinitionData load(
            ResourceDefinition resourceDefinition,
            VolumeNumber volumeNumber,
            boolean logWarnIfNotExists,
            TransactionMgr transMgr
        )

        {
            VolumeDefinitionData volumeDfn = null;
            try
            {
                volumeDfn = (VolumeDefinitionData) resourceDefinition.getVolumeDfn(dbCtx, volumeNumber);
            }
            catch (AccessDeniedException accDeniedExc)
            {
                handleAccessDeniedException(accDeniedExc);
            }
            return volumeDfn;
        }

        @Override
        public void delete(VolumeDefinitionData data, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteSpDriver implements StorPoolDefinitionDataDatabaseDriver
    {
        @Override
        public void create(StorPoolDefinitionData storPoolDefinitionData, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public StorPoolDefinitionData load(
            StorPoolName storPoolName,
            boolean logWarnIfNotExists,
            TransactionMgr transMgr
        )

        {
            return (StorPoolDefinitionData) storPoolDfnMap.get(storPoolName);
        }

        @Override
        public void delete(StorPoolDefinitionData data, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteSpdDriver implements StorPoolDataDatabaseDriver
    {
        @Override
        public StorPoolData load(
            Node node,
            StorPoolDefinition storPoolDefinition,
            boolean logWarnIfNotExists,
            TransactionMgr transMgr
        )

        {
            StorPoolData storPool = null;
            try
            {
                storPool = (StorPoolData) node.getStorPool(dbCtx, storPoolDefinition.getName());
            }
            catch (AccessDeniedException accDeniedExc)
            {
                handleAccessDeniedException(accDeniedExc);
            }
            return storPool;
        }

        @Override
        public void create(StorPoolData storPoolData, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public void delete(StorPoolData data, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public void ensureEntryExists(StorPoolData data, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteConnectionDriver implements SatelliteConnectionDataDatabaseDriver
    {
        @Override
        public SatelliteConnectionData load(
            Node node,
            boolean logWarnIfNotExists,
            TransactionMgr transMgr
        )
            throws SQLException
        {
            SatelliteConnectionData stltConn = null;
            try
            {
                stltConn = (SatelliteConnectionData) node.getSatelliteConnection(dbCtx);
            }
            catch (AccessDeniedException accDeniedExc)
            {
                handleAccessDeniedException(accDeniedExc);
            }
            return stltConn;
        }

        @Override
        public void create(SatelliteConnection satelliteConnectionData, TransactionMgr transMgr) throws SQLException
        {
            // no-op
        }

        @Override
        public void delete(SatelliteConnection satelliteConnectionData, TransactionMgr transMgr) throws SQLException
        {
            // no-op
        }

        @SuppressWarnings("unchecked")
        @Override
        public SingleColumnDatabaseDriver<SatelliteConnectionData, TcpPortNumber> getSatelliteConnectionPortDriver()
        {
            return (SingleColumnDatabaseDriver<SatelliteConnectionData, TcpPortNumber>) singleColDriver;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SingleColumnDatabaseDriver<SatelliteConnectionData, EncryptionType> getSatelliteConnectionTypeDriver()
        {
            return (SingleColumnDatabaseDriver<SatelliteConnectionData, EncryptionType>) singleColDriver;
        }

    }

    private class SatelliteNiDriver implements NetInterfaceDataDatabaseDriver
    {
        @SuppressWarnings("unchecked")
        @Override
        public SingleColumnDatabaseDriver<NetInterfaceData, LsIpAddress> getNetInterfaceAddressDriver()
        {
            return (SingleColumnDatabaseDriver<NetInterfaceData, LsIpAddress>) singleColDriver;
        }

        @Override
        public NetInterfaceData load(
            Node node,
            NetInterfaceName netInterfaceName,
            boolean logWarnIfNotExists,
            TransactionMgr transMgr
        )

        {
            NetInterfaceData netInterface = null;
            try
            {
                netInterface = (NetInterfaceData) node.getNetInterface(dbCtx, netInterfaceName);
            }
            catch (AccessDeniedException accDeniedExc)
            {
                handleAccessDeniedException(accDeniedExc);
            }
            return netInterface;
        }

        @Override
        public void create(NetInterfaceData netInterfaceData, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public void delete(NetInterfaceData data, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteFlagDriver implements StateFlagsPersistence<Object>
    {
        @Override
        public void persist(Object parent, long flags, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteSingleColDriver<NOOP_KEY, NOOP> implements SingleColumnDatabaseDriver<NOOP_KEY, NOOP>
    {
        @Override
        public void update(NOOP_KEY parent, NOOP element, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteNodeConDfnDriver implements NodeConnectionDataDatabaseDriver
    {
        @Override
        public NodeConnectionData load(
            Node sourceNode,
            Node targetNode,
            boolean logWarnIfNotExists,
            TransactionMgr transMgr
        )

        {
            NodeConnectionData nodeConnection = null;
            try
            {
                nodeConnection = (NodeConnectionData) sourceNode.getNodeConnection(dbCtx, targetNode);
            }
            catch (AccessDeniedException accDeniedExc)
            {
                handleAccessDeniedException(accDeniedExc);
            }
            return nodeConnection;
        }

        @Override
        public List<NodeConnectionData> loadAllByNode(
            Node node,
            TransactionMgr transMgr
        )

        {
            return Collections.emptyList();
        }

        @Override
        public void create(NodeConnectionData nodeConDfnData, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public void delete(NodeConnectionData nodeConDfnData, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteResConDfnDriver implements ResourceConnectionDataDatabaseDriver
    {
        @Override
        public ResourceConnectionData load(
            Resource source,
            Resource target,
            boolean logWarnIfNotExists,
            TransactionMgr transMgr
        )

        {
            ResourceConnectionData resourceConnection = null;
            try
            {
                resourceConnection = (ResourceConnectionData) source.getResourceConnection(dbCtx, target);
            }
            catch (AccessDeniedException accDeniedExc)
            {
                handleAccessDeniedException(accDeniedExc);
            }
            return resourceConnection;
        }

        @Override
        public List<ResourceConnectionData> loadAllByResource(
            Resource resource,
            TransactionMgr transMgr
        )

        {
            return Collections.emptyList();
        }

        @Override
        public void create(ResourceConnectionData conDfnData, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public void delete(ResourceConnectionData data, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    private class SatelliteVolConDfnDriver implements VolumeConnectionDataDatabaseDriver
    {
        @Override
        public VolumeConnectionData load(
            Volume sourceVolume,
            Volume targetVolume,
            boolean logWarnIfNotExists,
            TransactionMgr transMgr
        )

        {
            VolumeConnectionData volumeConnection = null;
            try
            {
                volumeConnection = (VolumeConnectionData) sourceVolume.getVolumeConnection(dbCtx, targetVolume);
            }
            catch (AccessDeniedException accDeniedExc)
            {
                handleAccessDeniedException(accDeniedExc);
            }
            return volumeConnection;
        }

        @Override
        public List<VolumeConnectionData> loadAllByVolume(
            Volume volume,
            TransactionMgr transMgr
        )

        {
            return Collections.emptyList();
        }

        @Override
        public void create(VolumeConnectionData conDfnData, TransactionMgr transMgr)
        {
            // no-op
        }

        @Override
        public void delete(VolumeConnectionData conDfnData, TransactionMgr transMgr)
        {
            // no-op
        }
    }

    public void handleAccessDeniedException(AccessDeniedException accDeniedExc)
    {
        throw new ImplementationError(
            "SatelliteDbDriver's accessContext has not enough privileges",
            accDeniedExc
        );
    }
}
