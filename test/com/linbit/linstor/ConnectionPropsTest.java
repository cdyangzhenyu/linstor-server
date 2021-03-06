package com.linbit.linstor;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.linbit.TransactionMgr;
import com.linbit.linstor.Node.NodeType;
import com.linbit.linstor.ResourceDefinition.TransportType;
import com.linbit.linstor.propscon.InvalidKeyException;
import com.linbit.linstor.propscon.InvalidValueException;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.DerbyBase;
import com.linbit.linstor.storage.LvmDriver;

public class ConnectionPropsTest extends DerbyBase
{
    private NodeName nodeName1;
    private NodeName nodeName2;
    private ResourceName resName;
    private TcpPortNumber resDfnPort;
    private TransportType resDfnTransportType;
    private StorPoolName storPoolName;
    private NodeId nodeId1;
    private NodeId nodeId2;
    private VolumeNumber volNr;
    private MinorNumber minor;
    private long volSize;
    private String blockDev1;
    private String metaDisk1;
    private String blockDev2;
    private String metaDisk2;

    private TransactionMgr transMgr;

    private NodeData node1;
    private NodeData node2;
    private ResourceDefinitionData resDfn;
    private ResourceData res1;
    private ResourceData res2;
    private StorPoolDefinitionData storPoolDfn;
    private StorPoolData storPool1;
    private StorPoolData storPool2;
    private VolumeDefinitionData volDfn;
    private VolumeData vol1;
    private VolumeData vol2;

    private NodeConnectionData nodeCon;
    private ResourceConnectionData resCon;
    private VolumeConnectionData volCon;

    private Props nodeConProps;
    private Props resConProps;
    private Props volConProps;

    private PriorityProps conProps;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        nodeName1 = new NodeName("Node1");
        nodeName2 = new NodeName("Node2");
        resName = new ResourceName("ResName");
        resDfnPort = new TcpPortNumber(4242);
        resDfnTransportType = TransportType.IP;
        storPoolName = new StorPoolName("StorPool");
        nodeId1 = new NodeId(1);
        nodeId2 = new NodeId(2);
        volNr = new VolumeNumber(13);
        minor = new MinorNumber(12);
        volSize = 9001;
        blockDev1 = "/dev/vol1/block";
        metaDisk1= "/dev/vol1/meta";
        blockDev2 = "/dev/vol2/block";
        metaDisk2 = "/dev/vol2/meta";

        transMgr = new TransactionMgr(getConnection());

        node1 = NodeData.getInstance(SYS_CTX, nodeName1, NodeType.CONTROLLER, null, transMgr, true, false);
        node2 = NodeData.getInstance(SYS_CTX, nodeName2, NodeType.CONTROLLER, null, transMgr, true, false);

        resDfn = ResourceDefinitionData.getInstance(
            SYS_CTX, resName, resDfnPort, null, "secret", resDfnTransportType, transMgr, true, false
        );

        res1 = ResourceData.getInstance(SYS_CTX, resDfn, node1, nodeId1, null, transMgr, true, false);
        res2 = ResourceData.getInstance(SYS_CTX, resDfn, node2, nodeId2, null, transMgr, true, false);

        storPoolDfn = StorPoolDefinitionData.getInstance(SYS_CTX, storPoolName, transMgr, true, false);

        storPool1 = StorPoolData.getInstance(SYS_CTX, node1, storPoolDfn, LvmDriver.class.getSimpleName(), transMgr, true, false);
        storPool2 = StorPoolData.getInstance(SYS_CTX, node2, storPoolDfn, LvmDriver.class.getSimpleName(), transMgr, true, false);

        volDfn = VolumeDefinitionData.getInstance(SYS_CTX, resDfn, volNr, minor, volSize, null, transMgr, true, false);

        vol1 = VolumeData.getInstance(SYS_CTX, res1, volDfn, storPool1, blockDev1, metaDisk1, null, transMgr, true, false);
        vol2 = VolumeData.getInstance(SYS_CTX, res1, volDfn, storPool2, blockDev2, metaDisk2, null, transMgr, true, false);

        nodeCon = NodeConnectionData.getInstance(SYS_CTX, node1, node2, transMgr, true, false);
        resCon = ResourceConnectionData.getInstance(SYS_CTX, res1, res2, transMgr, true, false);
        volCon = VolumeConnectionData.getInstance(SYS_CTX, vol1, vol2, transMgr, true, false);

        nodeConProps = nodeCon.getProps(SYS_CTX);
        resConProps = resCon.getProps(SYS_CTX);
        volConProps = volCon.getProps(SYS_CTX);

        conProps = new PriorityProps(SYS_CTX, nodeCon, resCon, volCon);
    }

    @Test
    public void test() throws InvalidKeyException, AccessDeniedException, InvalidValueException, SQLException
    {
        String testKey = "testKey";
        String testValue1 = "testValue1";
        String testValue2 = "testValue2";
        String testValue3 = "testValue3";
        String testValue4 = "testValue4";
        assertNull(conProps.getProp(testKey));

        volConProps.setProp(testKey, testValue1);
        assertEquals(testValue1, conProps.getProp(testKey));

        resConProps.setProp(testKey, testValue2);
        assertEquals(testValue1, conProps.getProp(testKey));

        nodeConProps.setProp(testKey, testValue3);
        assertEquals(testValue1, conProps.getProp(testKey));

        volConProps.removeProp(testKey);
        assertEquals(testValue2, conProps.getProp(testKey));

        resConProps.removeProp(testKey);
        assertEquals(testValue3, conProps.getProp(testKey));

        volConProps.setProp(testKey, testValue4);
        assertEquals(testValue4, conProps.getProp(testKey));
    }

}
