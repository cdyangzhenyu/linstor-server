package com.linbit.linstor;

import static com.linbit.linstor.dbdrivers.derby.DerbyConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.junit.Test;

import com.linbit.InvalidNameException;
import com.linbit.TransactionMgr;
import com.linbit.ValueOutOfRangeException;
import com.linbit.linstor.Resource.RscFlags;
import com.linbit.linstor.ResourceDefinition.TransportType;
import com.linbit.linstor.core.LinStor;
import com.linbit.linstor.security.DerbyBase;
import com.linbit.linstor.security.ObjectProtection;
import com.linbit.linstor.stateflags.StateFlagsBits;
import com.linbit.linstor.stateflags.StateFlagsPersistence;
import com.linbit.utils.UuidUtils;

public class ResouceDataDerbyTest extends DerbyBase
{
    private static final String SELECT_ALL_RESOURCES =
        " SELECT " + UUID + ", " + NODE_NAME + ", " + RESOURCE_NAME + ", " + NODE_ID + ", " + RESOURCE_FLAGS +
        " FROM " + TBL_RESOURCES;

    private final NodeName nodeName;
    private final ResourceName resName;
    private final TcpPortNumber resPort;
    private final NodeId nodeId;

    private TransactionMgr transMgr;
    private NodeData node;
    private ResourceDefinitionData resDfn;

    private java.util.UUID resUuid;
    private ObjectProtection objProt;
    private long initFlags;

    private ResourceDataDerbyDriver driver;

    public ResouceDataDerbyTest() throws InvalidNameException, ValueOutOfRangeException
    {
        nodeName = new NodeName("TestNodeName");
        resName = new ResourceName("TestResName");
        resPort = new TcpPortNumber(9001);
        nodeId = new NodeId(13);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        assertEquals(TBL_RESOURCES + " table's column count has changed. Update tests accordingly!", 5, TBL_COL_COUNT_RESOURCES);

        transMgr = new TransactionMgr(getConnection());

        node = NodeData.getInstance(SYS_CTX, nodeName, null, null, transMgr, true, false);
        resDfn = ResourceDefinitionData.getInstance(
            SYS_CTX, resName, resPort, null, "secret", TransportType.IP, transMgr, true, false
        );

        resUuid = randomUUID();
        objProt = ObjectProtection.getInstance(SYS_CTX, ObjectProtection.buildPath(nodeName, resName), true, transMgr);

        initFlags = RscFlags.CLEAN.flagValue;

        driver = (ResourceDataDerbyDriver) LinStor.getResourceDataDatabaseDriver();
    }

    @Test
    public void testPersist() throws Exception
    {
        ResourceData res = new ResourceData(resUuid, SYS_CTX, objProt, resDfn, node, nodeId, initFlags, transMgr);
        driver.create(res, transMgr);

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RESOURCES);
        ResultSet resultSet = stmt.executeQuery();

        assertTrue("Database did not persist resource / resourceDefinition", resultSet.next());
        assertEquals(resUuid, UuidUtils.asUuid(resultSet.getBytes(UUID)));
        assertEquals(nodeName.value, resultSet.getString(NODE_NAME));
        assertEquals(resName.value, resultSet.getString(RESOURCE_NAME));
        assertEquals(nodeId.value, resultSet.getInt(NODE_ID));
        assertEquals(RscFlags.CLEAN.flagValue, resultSet.getLong(RESOURCE_FLAGS));
        assertFalse("Database persisted too many resources / resourceDefinitions", resultSet.next());

        resultSet.close();
        stmt.close();
    }

    @Test
    public void testPersistGetInstance() throws Exception
    {
        ResourceData.getInstance(
            SYS_CTX,
            resDfn,
            node,
            nodeId,
            new RscFlags[] { RscFlags.DELETE },
            transMgr,
            true,
            false
        );

        transMgr.commit();

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RESOURCES);
        ResultSet resultSet = stmt.executeQuery();

        assertTrue("Database did not persist resource / resourceDefinition", resultSet.next());
        // uuid is now != resUuid because getInstance create a new resData object
        assertEquals(nodeName.value, resultSet.getString(NODE_NAME));
        assertEquals(resName.value, resultSet.getString(RESOURCE_NAME));
        assertEquals(nodeId.value, resultSet.getInt(NODE_ID));
        assertEquals(RscFlags.DELETE.flagValue, resultSet.getLong(RESOURCE_FLAGS));
        assertFalse("Database persisted too many resources / resourceDefinitions", resultSet.next());

        resultSet.close();
        stmt.close();
    }

    @Test
    public void testLoad() throws Exception
    {
        ResourceData res = new ResourceData(resUuid, SYS_CTX, objProt, resDfn, node, nodeId, initFlags, transMgr);
        driver.create(res, transMgr);

        ResourceData loadedRes = driver.load(node, resName, true, transMgr);

        assertNotNull("Database did not persist resource / resourceDefinition", loadedRes);
        assertEquals(resUuid, loadedRes.getUuid());
        assertNotNull(loadedRes.getAssignedNode());
        assertEquals(nodeName, loadedRes.getAssignedNode().getName());
        assertNotNull(loadedRes.getDefinition());
        assertEquals(resName, loadedRes.getDefinition().getName());
        assertEquals(nodeId, loadedRes.getNodeId());
        assertEquals(RscFlags.CLEAN.flagValue, loadedRes.getStateFlags().getFlagsBits(SYS_CTX));
    }

    @Test
    public void testLoadGetInstance() throws Exception
    {
        ResourceData loadedRes = ResourceData.getInstance(
            SYS_CTX,
            resDfn,
            node,
            nodeId,
            null,
            transMgr,
            false,
            false
        );
        assertNull(loadedRes);

        ResourceData res = new ResourceData(resUuid, SYS_CTX, objProt, resDfn, node, nodeId, initFlags, transMgr);
        driver.create(res, transMgr);

        loadedRes = ResourceData.getInstance(
            SYS_CTX,
            resDfn,
            node,
            nodeId,
            null,
            transMgr,
            false,
            false
        );

        assertNotNull("Database did not persist resource / resourceDefinition", loadedRes);
        assertEquals(resUuid, loadedRes.getUuid());
        assertNotNull(loadedRes.getAssignedNode());
        assertEquals(nodeName, loadedRes.getAssignedNode().getName());
        assertNotNull(loadedRes.getDefinition());
        assertEquals(resName, loadedRes.getDefinition().getName());
        assertEquals(nodeId, loadedRes.getNodeId());
        assertEquals(RscFlags.CLEAN.flagValue, loadedRes.getStateFlags().getFlagsBits(SYS_CTX));
    }

    @Test
    public void testLoadAll() throws Exception
    {
        ResourceData res = new ResourceData(resUuid, SYS_CTX, objProt, resDfn, node, nodeId, initFlags, transMgr);
        driver.create(res, transMgr);

        List<ResourceData> resList= driver.loadResourceData(SYS_CTX, node, transMgr);

        assertNotNull(resList);
        assertEquals(1, resList.size());
        ResourceData resData = resList.get(0);
        assertNotNull(resData);
        assertEquals(resUuid, resData.getUuid());
        assertNotNull(resData.getAssignedNode());
        assertEquals(nodeName, resData.getAssignedNode().getName());
        assertNotNull(resData.getDefinition());
        assertEquals(resName, resData.getDefinition().getName());
        assertEquals(nodeId, resData.getNodeId());
        assertEquals(RscFlags.CLEAN.flagValue, resData.getStateFlags().getFlagsBits(SYS_CTX));
    }

    @Test
    public void testCache() throws Exception
    {
        ResourceData storedInstance = ResourceData.getInstance(
            SYS_CTX,
            resDfn,
            node,
            nodeId,
            null,
            transMgr,
            true,
            false
        );

        // no clearCaches

        assertEquals(storedInstance, driver.load(node, resName, true, transMgr));
    }

    @Test
    public void testDelete() throws Exception
    {
        ResourceData res = new ResourceData(resUuid, SYS_CTX, objProt, resDfn, node, nodeId, initFlags, transMgr);
        driver.create(res, transMgr);
        driver.delete(res, transMgr);

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RESOURCES);
        ResultSet resultSet = stmt.executeQuery();

        assertFalse(resultSet.next());

        resultSet.close();
        stmt.close();
    }

    @Test
    public void testStateFlagPersistence() throws Exception
    {
        ResourceData res = new ResourceData(resUuid, SYS_CTX, objProt, resDfn, node, nodeId, initFlags, transMgr);
        driver.create(res, transMgr);
        StateFlagsPersistence<ResourceData> stateFlagPersistence = driver.getStateFlagPersistence();
        stateFlagPersistence.persist(res, StateFlagsBits.getMask(RscFlags.DELETE), transMgr);

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RESOURCES);
        ResultSet resultSet = stmt.executeQuery();

        assertTrue(resultSet.next());
        assertEquals(RscFlags.DELETE.flagValue, resultSet.getLong(RESOURCE_FLAGS));

        assertFalse(resultSet.next());

        resultSet.close();
        stmt.close();
    }

    @Test
    public void testEnsureExists() throws Exception
    {
        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RESOURCES);
        ResultSet resultSet = stmt.executeQuery();

        assertFalse(resultSet.next());
        resultSet.close();

        ResourceData res = new ResourceData(resUuid, SYS_CTX, objProt, resDfn, node, nodeId, initFlags, transMgr);
        driver.ensureResExists(SYS_CTX, res, transMgr);

        resultSet = stmt.executeQuery();

        assertTrue(resultSet.next());
        assertFalse(resultSet.next());
        resultSet.close();

        driver.ensureResExists(SYS_CTX, res, transMgr);

        resultSet = stmt.executeQuery();

        assertTrue(resultSet.next());
        assertFalse(resultSet.next());
        resultSet.close();
        stmt.close();
    }

    @Test
    public void testGetInstanceSatelliteCreate() throws Exception
    {
        satelliteMode();

        ResourceData resData = ResourceData.getInstance(
            SYS_CTX,
            resDfn,
            node,
            nodeId,
            new RscFlags[] { RscFlags.CLEAN },
            null,
            true,
            false
        );

        assertEquals(node, resData.getAssignedNode());
        assertEquals(resDfn, resData.getDefinition());
        assertEquals(nodeId, resData.getNodeId());
        assertNotNull(resData.getObjProt());
        assertNotNull(resData.getProps(SYS_CTX));
        assertTrue(resData.getStateFlags().isSet(SYS_CTX, RscFlags.CLEAN));
        assertNotNull(resData.getUuid());

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RESOURCES);
        ResultSet resultSet = stmt.executeQuery();
        assertFalse(resultSet.next());
        resultSet.close();
        stmt.close();
    }

    @Test
    public void testGetInstanceSatelliteNoCreate() throws Exception
    {
        satelliteMode();

        ResourceData resData = ResourceData.getInstance(
            SYS_CTX,
            resDfn,
            node,
            nodeId,
            new RscFlags[] { RscFlags.CLEAN },
            null,
            false,
            false
        );

        assertNull(resData);

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RESOURCES);
        ResultSet resultSet = stmt.executeQuery();
        assertFalse(resultSet.next());
        resultSet.close();
        stmt.close();
    }

    @Test (expected = LinStorDataAlreadyExistsException.class)
    public void testAlreadyExists() throws Exception
    {
        ResourceData res = new ResourceData(resUuid, SYS_CTX, objProt, resDfn, node, nodeId, initFlags, transMgr);
        driver.create(res, transMgr);

        ResourceData.getInstance(SYS_CTX, resDfn, node, nodeId, null, transMgr, false, true);
    }
}
