package com.linbit.linstor;

import static com.linbit.linstor.dbdrivers.derby.DerbyConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.Test;

import com.linbit.InvalidNameException;
import com.linbit.TransactionMgr;
import com.linbit.ValueOutOfRangeException;
import com.linbit.linstor.ResourceDefinition.TransportType;
import com.linbit.linstor.core.LinStor;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.DerbyBase;
import com.linbit.utils.UuidUtils;

public class ResourceConnectionDataDerbyTest extends DerbyBase
{
    private static final String SELECT_ALL_RES_CON_DFNS =
        " SELECT " + UUID + ", " + NODE_NAME_SRC + ", " +
                     NODE_NAME_DST + ", " + RESOURCE_NAME +
        " FROM " + TBL_RESOURCE_CONNECTIONS;

    private final ResourceName resName;
    private final TcpPortNumber resPort;
    private final NodeName sourceName;
    private final NodeName targetName;

    private TransactionMgr transMgr;

    private java.util.UUID uuid;
    private ResourceDefinitionData resDfn;
    private NodeData nodeSrc;
    private NodeData nodeDst;

    private ResourceConnectionData resCon;

    private ResourceConnectionDataDerbyDriver driver;

    private NodeId nodeIdSrc;
    private NodeId nodeIdDst;

    private ResourceData resSrc;
    private ResourceData resDst;

    public ResourceConnectionDataDerbyTest() throws InvalidNameException, ValueOutOfRangeException
    {
        resName = new ResourceName("testResourceName");
        resPort = new TcpPortNumber(9001);
        sourceName = new NodeName("testNodeSource");
        targetName = new NodeName("testNodeTarget");
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        assertEquals(TBL_RESOURCE_CONNECTIONS + " table's column count has changed. Update tests accordingly!", 4, TBL_COL_COUNT_RESOURCE_CONNECTIONS);

        transMgr = new TransactionMgr(getConnection());

        uuid = randomUUID();

        resDfn = ResourceDefinitionData.getInstance(
            SYS_CTX, resName, resPort, null, "secret", TransportType.IP, transMgr, true, false
        );
        rscDfnMap.put(resDfn.getName(), resDfn);
        nodeSrc = NodeData.getInstance(SYS_CTX, sourceName, null, null, transMgr, true, false);
        nodeDst = NodeData.getInstance(SYS_CTX, targetName, null, null, transMgr, true, false);

        nodeIdSrc = new NodeId(13);
        nodeIdDst = new NodeId(14);

        resSrc = ResourceData.getInstance(SYS_CTX, resDfn, nodeSrc, nodeIdSrc, null, transMgr, true, false);
        resDst = ResourceData.getInstance(SYS_CTX, resDfn, nodeDst, nodeIdDst, null, transMgr, true, false);

        resCon = new ResourceConnectionData(uuid, SYS_CTX, resSrc, resDst, transMgr);
        driver = (ResourceConnectionDataDerbyDriver) LinStor.getResourceConnectionDatabaseDriver();
    }

    @Test
    public void testPersist() throws Exception
    {
        driver.create(resCon, transMgr);

        checkDbPersist(true);
    }

    @Test
    public void testPersistGetInstance() throws Exception
    {
        ResourceConnectionData.getInstance(SYS_CTX, resSrc, resDst, transMgr, true, false);

        checkDbPersist(false);
    }

    @Test
    public void testLoad() throws Exception
    {
        driver.create(resCon, transMgr);

        ResourceConnectionData loadedConDfn = driver.load(resSrc , resDst, true, transMgr);

        checkLoadedConDfn(loadedConDfn, true);
    }

    @Test
    public void testLoadAll() throws Exception
    {
        driver.create(resCon, transMgr);

        List<ResourceConnectionData> cons = driver.loadAllByResource(resSrc, transMgr);

        assertNotNull(cons);

        assertEquals(1, cons.size());

        ResourceConnection loadedConDfn = cons.get(0);
        assertNotNull(loadedConDfn);

        checkLoadedConDfn(loadedConDfn, true);
    }

    @Test
    public void testLoadGetInstance() throws Exception
    {
        driver.create(resCon, transMgr);

        ResourceConnectionData loadedConDfn = ResourceConnectionData.getInstance(
            SYS_CTX,
            resSrc,
            resDst,
            transMgr,
            false,
            false
        );

        checkLoadedConDfn(loadedConDfn, true);
    }

    @Test
    public void testCache() throws Exception
    {
        ResourceConnectionData storedInstance = ResourceConnectionData.getInstance(
            SYS_CTX,
            resSrc,
            resDst,
            transMgr,
            true,
            false
        );

        // no clear-cache

        assertEquals(storedInstance, driver.load(resSrc, resDst, true, transMgr));
    }

    @Test
    public void testDelete() throws Exception
    {
        driver.create(resCon, transMgr);

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RES_CON_DFNS);
        ResultSet resultSet = stmt.executeQuery();

        assertTrue(resultSet.next());
        assertFalse(resultSet.next());
        resultSet.close();

        driver.delete(resCon, transMgr);

        resultSet = stmt.executeQuery();

        assertFalse(resultSet.next());
        resultSet.close();

        stmt.close();
    }

    @Test
    public void testSatelliteCreate() throws Exception
    {
        satelliteMode();
        ResourceConnectionData satelliteConDfn = ResourceConnectionData.getInstance(
            SYS_CTX,
            resSrc,
            resDst,
            null,
            true,
            false
        );

        checkLoadedConDfn(satelliteConDfn, false);

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RES_CON_DFNS);
        ResultSet resultSet = stmt.executeQuery();

        assertFalse(resultSet.next());
        resultSet.close();
        stmt.close();
    }

    @Test
    public void testSatelliteNoCreate() throws Exception
    {
        satelliteMode();

        NodeName srcNodeName2 = new NodeName("OtherSourceNodeName");
        NodeName dstNodeName2 = new NodeName("OtherTargetNodeName");

        NodeData nodeSrc2 = NodeData.getInstance(SYS_CTX, srcNodeName2, null, null, transMgr, true, false);
        NodeData nodeDst2 = NodeData.getInstance(SYS_CTX, dstNodeName2, null, null, transMgr, true, false);

        ResourceData resSrc2 = ResourceData.getInstance(SYS_CTX, resDfn, nodeSrc2, nodeIdSrc, null, transMgr, true, false);
        ResourceData resDst2 = ResourceData.getInstance(SYS_CTX, resDfn, nodeDst2, nodeIdDst, null, transMgr, true, false);

        ResourceConnectionData satelliteConDfn = ResourceConnectionData.getInstance(
            SYS_CTX,
            resSrc2,
            resDst2,
            null,
            false,
            false
        );

        assertNull(satelliteConDfn);

        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RES_CON_DFNS);
        ResultSet resultSet = stmt.executeQuery();

        assertFalse(resultSet.next());
        resultSet.close();
        stmt.close();
    }

    private void checkDbPersist(boolean checkUuid) throws SQLException
    {
        PreparedStatement stmt = transMgr.dbCon.prepareStatement(SELECT_ALL_RES_CON_DFNS);
        ResultSet resultSet = stmt.executeQuery();

        assertTrue(resultSet.next());
        if (checkUuid)
        {
            assertEquals(uuid, UuidUtils.asUuid(resultSet.getBytes(UUID)));
        }
        assertEquals(resName.value, resultSet.getString(RESOURCE_NAME));
        assertEquals(sourceName.value, resultSet.getString(NODE_NAME_SRC));
        assertEquals(targetName.value, resultSet.getString(NODE_NAME_DST));

        assertFalse(resultSet.next());

        resultSet.close();
        stmt.close();
    }

    private void checkLoadedConDfn(ResourceConnection loadedConDfn, boolean checkUuid) throws AccessDeniedException
    {
        assertNotNull(loadedConDfn);
        if (checkUuid)
        {
            assertEquals(uuid, loadedConDfn.getUuid());
        }
        Resource sourceResource = loadedConDfn.getSourceResource(SYS_CTX);
        Resource targetResource = loadedConDfn.getTargetResource(SYS_CTX);

        assertEquals(resName, sourceResource.getDefinition().getName());
        assertEquals(sourceName, sourceResource.getAssignedNode().getName());
        assertEquals(targetName, targetResource.getAssignedNode().getName());
        assertEquals(sourceResource.getDefinition().getName(), targetResource.getDefinition().getName());
    }

    @Test (expected = LinStorDataAlreadyExistsException.class)
    public void testAlreadyExists() throws Exception
    {
        driver.create(resCon, transMgr);

        ResourceConnectionData.getInstance(SYS_CTX, resSrc, resDst, transMgr, false, true);
    }
}
