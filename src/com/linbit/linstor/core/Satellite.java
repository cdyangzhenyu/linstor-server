package com.linbit.linstor.core;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.SatelliteTransactionMgr;
import com.linbit.ServiceName;
import com.linbit.SystemService;
import com.linbit.SystemServiceStartException;
import com.linbit.WorkerPool;
import com.linbit.fsevent.FileSystemWatch;
import com.linbit.linstor.LinStorException;
import com.linbit.linstor.Node;
import com.linbit.linstor.Node.NodeFlag;
import com.linbit.linstor.Node.NodeType;
import com.linbit.linstor.NodeData;
import com.linbit.linstor.NodeName;
import com.linbit.linstor.ResourceDefinition;
import com.linbit.linstor.ResourceName;
import com.linbit.linstor.SatelliteCoreServices;
import com.linbit.linstor.SatelliteDbDriver;
import com.linbit.linstor.SatelliteDummyStorPoolData;
import com.linbit.linstor.SatellitePeerCtx;
import com.linbit.linstor.StorPoolDefinition;
import com.linbit.linstor.StorPoolDefinitionData;
import com.linbit.linstor.StorPoolName;
import com.linbit.linstor.api.ApiType;
import com.linbit.linstor.api.interfaces.serializer.CtrlStltSerializer;
import com.linbit.linstor.debug.DebugConsole;
import com.linbit.linstor.drbdstate.DrbdEventService;
import com.linbit.linstor.drbdstate.DrbdStateTracker;
import com.linbit.linstor.drbdstate.StateTracker;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.logging.StdErrorReporter;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.netcom.TcpConnector;
import com.linbit.linstor.netcom.TcpConnectorService;
import com.linbit.linstor.netcom.ssl.SslTcpConnectorService;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.propscon.PropsContainer;
import com.linbit.linstor.proto.CommonMessageProcessor;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.AccessType;
import com.linbit.linstor.security.EmptySecurityDbDriver;
import com.linbit.linstor.security.Initializer;
import com.linbit.linstor.security.ObjectProtection;
import com.linbit.linstor.security.Privilege;
import com.linbit.linstor.security.PrivilegeSet;
import com.linbit.linstor.security.SecurityLevel;
import com.linbit.linstor.timer.CoreTimer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * linstor satellite prototype
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public final class Satellite extends LinStor implements Runnable, SatelliteCoreServices
{
    // System module information
    public static final String MODULE = "Satellite";

    public static final String NET_COM_DEFAULT_TYPE = "plain";
    public static final String NET_COM_DEFAULT_ADDR = "::0";
    public static final int NET_COM_DEFAULT_PORT = 3366;

    // TCP Service configuration file
    public static final String NET_COM_CONF_FILE = "satellite_netcom.cfg";
    // Plain TCP Service configuration keys
    public static final String NET_COM_CONF_TYPE_KEY = "type";
    public static final String NET_COM_CONF_BIND_ADDR_KEY = "bind-address";
    public static final String NET_COM_CONF_PORT_KEY = "port";
    public static final String NET_COM_CONF_TYPE_PLAIN = "plain";
    public static final String NET_COM_CONF_TYPE_SSL = "ssl";
    // SSL Service configuration keys
    public static final String NET_COM_CONF_SSL_SERVER_CERT_KEY = "server-certificate";
    public static final String NET_COM_CONF_SSL_TRUST_CERT_KEY = "trusted-certificates";
    public static final String NET_COM_CONF_SSL_KEY_PASS_KEY = "key-passwd";
    public static final String NET_COM_CONF_SSL_KEYSTORE_PASS_KEY = "keystore-passwd";
    public static final String NET_COM_CONF_SSL_TRUST_PASS_KEY = "truststore-passwd";
    public static final String NET_COM_CONF_SSL_PROTOCOL_KEY = "ssl-protocol";

    public static final SatelliteDummyStorPoolData DUMMY_REMOTE_STOR_POOL = new SatelliteDummyStorPoolData();

    private static final String SATELLTE_PROPSCON_INSTANCE_NAME = "STLTCFG";

    // System security context
    private AccessContext sysCtx;

    // Public security context
    private AccessContext publicCtx;

    // Command line arguments
    private String[] args;

    private final StltApiCallHandler apiCallHandler;

    // ============================================================
    // Worker thread pool & message processing dispatcher
    //
    private WorkerPool workerThrPool = null;
    private CommonMessageProcessor msgProc;

    // ============================================================
    // Core system services
    //
    // Map of controllable system services
    private final Map<ServiceName, SystemService> systemServicesMap;

    // Map of connected peers
    private final Map<String, Peer> peerMap;

    // Map of network communications connectors
    private final Map<ServiceName, TcpConnector> netComConnectors;

    // The current API type (e.g ProtoBuf)
    private final ApiType apiType;

    // Satellite configuration properties
    Props stltConf;
    ObjectProtection stltConfProt;

    // Map of all managed nodes
    Map<NodeName, Node> nodesMap;

    // Map of all resource definitions
    Map<ResourceName, ResourceDefinition> rscDfnMap;

    // Map of all storage pools
    Map<StorPoolName, StorPoolDefinition> storPoolDfnMap;

    // Local NodeName received from the currently active controller
    private NodeName localNodeName;

    // The currently connected controller peer (can be null)
    private Peer controllerPeer;

    // File system watch service
    private FileSystemWatch fsWatchSvc;

    // Device manager
    private DeviceManagerImpl devMgr = null;

    // Drbd Event Tracker
    private DrbdEventService drbdEventSvc;

    // Shutdown controls
    private boolean shutdownFinished;
    private ObjectProtection shutdownProt;

    // Lock for major global changes
    public final ReadWriteLock stltConfLock;

    private CtrlStltSerializer interComSerializer;

    private final AtomicLong fullSyncId;
    private boolean currentFullSyncApplied = false;

    private final AtomicLong awaitedUpdateId;

    public Satellite(AccessContext sysCtxRef, AccessContext publicCtxRef, String[] argsRef)
        throws IOException
    {
        // Initialize synchronization
        stltConfLock        = new ReentrantReadWriteLock(true);

        // Initialize security contexts
        sysCtx = sysCtxRef;
        publicCtx = publicCtxRef;

        // Initialize command line arguments
        args = argsRef;

        // Initialize and collect system services
        systemServicesMap = new TreeMap<>();
        {
            CoreTimer timer = super.getTimer();
            systemServicesMap.put(timer.getInstanceName(), timer);
        }
        fsWatchSvc = new FileSystemWatch();
        systemServicesMap.put(fsWatchSvc.getInstanceName(), fsWatchSvc);


        // Initialize LinStor objects maps
        nodesMap = new TreeMap<>();
        rscDfnMap = new TreeMap<>();
        storPoolDfnMap = new TreeMap<>();

        // Initialize network communications connectors map
        netComConnectors = new TreeMap<>();

        // Initialize connected peers map
        peerMap = new TreeMap<>();

        apiType = ApiType.PROTOBUF;

        // initialize noop databases drivers (needed for shutdownProt)
        securityDbDriver = new EmptySecurityDbDriver(sysCtx);
        persistenceDbDriver = new SatelliteDbDriver(sysCtx, nodesMap, rscDfnMap, storPoolDfnMap);

        fullSyncId = new AtomicLong(2); // just don't start with 0 making sure the controller
                                        // mirrors our fullSyncId
        awaitedUpdateId = new AtomicLong(0);

        // Initialize conf props
        try
        {
            SatelliteTransactionMgr transMgr = new SatelliteTransactionMgr();
            stltConf = PropsContainer.getInstance(SATELLTE_PROPSCON_INSTANCE_NAME, transMgr);
            transMgr.commit();
        }
        catch (SQLException exc)
        {
            // not possible
            throw new ImplementationError(exc);
        }

        try
        {
            AccessContext apiCtx = sysCtxRef.clone();
            apiCtx.getEffectivePrivs().enablePrivileges(Privilege.PRIV_SYS_ALL);
            apiCallHandler = new StltApiCallHandler(this, apiType, apiCtx);
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ImplementationError(
                "Satellite's constructor cannot get system privileges",
                accDeniedExc
            );
        }

        // Initialize shutdown controls
        shutdownFinished = false;
        try
        {
            AccessContext initCtx = sysCtx.clone();
            initCtx.getEffectivePrivs().enablePrivileges(Privilege.PRIV_SYS_ALL);

            shutdownProt = ObjectProtection.getInstance(
                initCtx,
                ObjectProtection.buildPath(this, "shutdown"),
                true,
                null
            );
        }
        catch (SQLException sqlExc)
        {
            // cannot happen
            throw new ImplementationError(
                "Creating an ObjectProtection without TransactionManager threw an SQLException",
                sqlExc
            );
        }
        catch (AccessDeniedException accessDeniedExc)
        {
            // cannot happen as the objProt cannot be loaded
            throw new ImplementationError(
                "ObjectProtection instance which should not even exist rejected system's access context. Panic.",
                accessDeniedExc
            );
        }
    }

    public void initialize(ErrorReporter errorLogRef)
    {
        try
        {
            reconfigurationLock.writeLock().lock();

            shutdownFinished = false;
            try
            {
                AccessContext initCtx = sysCtx.clone();
                initCtx.getEffectivePrivs().enablePrivileges(Privilege.PRIV_SYS_ALL);

                // Initialize the error & exception reporting facility
                setErrorLog(initCtx, errorLogRef);

                // Initialize the worker thread pool
                // errorLogRef.logInfo("Starting worker thread pool");
                try
                {
                    int cpuCount = getCpuCount();
                    int thrCount = com.linbit.utils.MathUtils.bounds(MIN_WORKER_COUNT, cpuCount, MAX_CPU_COUNT);
                    int qSize = thrCount * getWorkerQueueFactor();
                    qSize = qSize > MIN_WORKER_QUEUE_SIZE ? qSize : MIN_WORKER_QUEUE_SIZE;
                    setWorkerThreadCount(initCtx, thrCount);
                    setWorkerQueueSize(initCtx, qSize);
                    workerThrPool = WorkerPool.initialize(
                        thrCount, qSize, true, "MainWorkerPool", getErrorReporter(), null
                    );

                    // Initialize the message processor
                    // errorLogRef.logInfo("Initializing API call dispatcher");
                    msgProc = new CommonMessageProcessor(this, workerThrPool);
                }
                catch (AccessDeniedException accDeniedExc)
                {
                    throw new ImplementationError(
                        "Satellite's constructor cannot get system privileges",
                        accDeniedExc
                    );
                }


                // Set CONTROL access for the SYSTEM role on shutdown
                try
                {
                    SatelliteTransactionMgr transMgr = new SatelliteTransactionMgr();
                    shutdownProt.setConnection(transMgr);
                    shutdownProt.addAclEntry(initCtx, sysCtx.getRole(), AccessType.CONTROL);
                    transMgr.commit();
                }
                catch (SQLException sqlExc)
                {
                    // cannot happen
                    throw new ImplementationError(
                        "ObjectProtection without TransactionManager threw an SQLException",
                        sqlExc
                    );
                }


                errorLogRef.logInfo("Initializing test APIs");
                LinStor.loadApiCalls(msgProc, this, this, apiType);


                errorLogRef.logInfo("Initializing StateTracker");
                {
                    AccessContext drbdEventCtx = sysCtx.clone();
                    PrivilegeSet drbdEventPriv = drbdEventCtx.getEffectivePrivs();
                    drbdEventPriv.disablePrivileges(Privilege.PRIV_SYS_ALL);
                    drbdEventPriv.enablePrivileges(Privilege.PRIV_MAC_OVRD, Privilege.PRIV_OBJ_USE);

                    StateTracker stateTracker = new StateTracker();
                    drbdEventSvc = new DrbdEventService(this, stateTracker);

                    systemServicesMap.put(drbdEventSvc.getInstanceName(), drbdEventSvc);
                }

                errorLogRef.logInfo("Initializing device manager");
                {
                    AccessContext devMgrCtx = sysCtx.clone();
                    PrivilegeSet devMgrPriv = devMgrCtx.getEffectivePrivs();
                    devMgrPriv.disablePrivileges(Privilege.PRIV_SYS_ALL);
                    devMgrPriv.enablePrivileges(Privilege.PRIV_MAC_OVRD, Privilege.PRIV_OBJ_USE);
                    devMgr = new DeviceManagerImpl(this, devMgrCtx, this, drbdEventSvc, workerThrPool);

                    systemServicesMap.put(devMgr.getInstanceName(), devMgr);
                }

                // Initialize system services
                startSystemServices(systemServicesMap.values());

                // Initialize the network communications service
                errorLogRef.logInfo("Initializing main network communications service");
                initMainNetComService(initCtx);
            }
            catch (AccessDeniedException accessExc)
            {
                errorLogRef.reportError(
                    new ImplementationError(
                        "The initialization security context does not have all required privileges. " +
                        "Initialization failed.",
                        accessExc
                    )
                );
            }
        }
        finally
        {
            reconfigurationLock.writeLock().unlock();
        }
    }

    @Override
    public void setSecurityLevel(AccessContext accCtx, SecurityLevel newLevel)
        throws AccessDeniedException, SQLException
    {
        SecurityLevel.set(accCtx, newLevel, null, null);
    }

    @Override
    public void run()
    {
        ErrorReporter errLog = getErrorReporter();
        try
        {
            errLog.logInfo("Entering debug console");

            AccessContext privCtx = sysCtx.clone();
            AccessContext debugCtx = sysCtx.clone();
            privCtx.getEffectivePrivs().enablePrivileges(Privilege.PRIV_SYS_ALL);
            debugCtx.getEffectivePrivs().enablePrivileges(Privilege.PRIV_SYS_ALL);

            DebugConsole dbgConsole = createDebugConsole(privCtx, debugCtx, null);
            dbgConsole.stdStreamsConsole(StltDebugConsoleImpl.CONSOLE_PROMPT);
            System.out.println();

            errLog.logInfo("Debug console exited");
        }
        catch (Throwable error)
        {
            getErrorReporter().reportError(error);
        }

        try
        {
            AccessContext shutdownCtx = sysCtx.clone();
            // Just in case that someone removed the access control list entry
            // for the system's role or changed the security type for shutdown,
            // override access controls with the system context's privileges
            shutdownCtx.getEffectivePrivs().enablePrivileges(Privilege.PRIV_OBJ_USE, Privilege.PRIV_MAC_OVRD);
            shutdown(shutdownCtx);
        }
        catch (AccessDeniedException accExc)
        {
            throw new ImplementationError(
                "Cannot shutdown() using the system's security context. " +
                "Suspected removal of privileges from the system context.",
                accExc
            );
        }
    }

    public void shutdown(AccessContext accCtx) throws AccessDeniedException
    {
        shutdownProt.requireAccess(accCtx, AccessType.USE);

        ErrorReporter errLog = getErrorReporter();

        try
        {
            reconfigurationLock.writeLock().lock();
            if (!shutdownFinished)
            {
                errLog.logInfo(
                    String.format(
                        "Shutdown initiated by subject '%s' using role '%s'\n",
                        accCtx.getIdentity(), accCtx.getRole()
                    )
                );

                errLog.logInfo("Shutdown in progress");

                // Shutdown service threads
                stopSystemServices(systemServicesMap.values());

                if (workerThrPool != null)
                {
                    errLog.logInfo("Shutting down worker thread pool");
                    workerThrPool.shutdown();
                    workerThrPool = null;
                }

                errLog.logInfo("Shutdown complete");
            }
            shutdownFinished = true;
        }
        finally
        {
            reconfigurationLock.writeLock().unlock();
        }
    }

    /**
     * Creates a debug console instance for remote use by a connected peer
     *
     * @param accCtx The access context to authorize this API call
     * @param client Connected peer
     * @return New DebugConsole instance
     * @throws AccessDeniedException If the API call is not authorized
     */
    public DebugConsole createDebugConsole(
        AccessContext accCtx,
        AccessContext debugCtx,
        Peer client
    )
        throws AccessDeniedException
    {
        accCtx.getEffectivePrivs().requirePrivileges(Privilege.PRIV_SYS_ALL);
        StltDebugConsoleImpl peerDbgConsole = new StltDebugConsoleImpl(
            this,
            debugCtx,
            systemServicesMap,
            peerMap,
            msgProc
        );
        if (client != null)
        {
            SatellitePeerCtx peerContext = (SatellitePeerCtx) client.getAttachment();
            // Initialize remote debug console
            // FIXME: loadDefaultCommands() should not use System.out and System.err
            //        if the debug console is created for a peer / client
            peerDbgConsole.loadDefaultCommands(System.out, System.err);
            peerContext.setDebugConsole(peerDbgConsole);
        }
        else
        {
            // Initialize local debug console
            peerDbgConsole.loadDefaultCommands(System.out, System.err);
        }

        return peerDbgConsole;
    }

    @Override
    public FileSystemWatch getFsWatch()
    {
        return fsWatchSvc;
    }

    private void initMainNetComService(AccessContext initCtx)
    {
        try
        {
            Properties netComProps = new Properties();
            if (Files.exists(Paths.get(NET_COM_CONF_FILE)))
            {
                try (InputStream propsIn = new FileInputStream(NET_COM_CONF_FILE))
                {
                    netComProps.loadFromXML(propsIn);
                }
                catch (FileNotFoundException fileExc)
                {
                    // this should never happen due to the if (Files.exists(...)), but if it
                    // still happens it can be ignored, as every property has a default-value
                }
            }
            InetAddress addr = InetAddress.getByName(
                netComProps.getProperty(
                    NET_COM_CONF_BIND_ADDR_KEY,
                    NET_COM_DEFAULT_ADDR
                )
            );
            String portProp = netComProps.getProperty(
                NET_COM_CONF_PORT_KEY,
                Integer.toString(NET_COM_DEFAULT_PORT)
            );
            int port = Integer.parseInt(portProp);
            SocketAddress bindAddress = new InetSocketAddress(addr, port);

            TcpConnector netComSvc = null;

            String type = netComProps.getProperty(NET_COM_CONF_TYPE_KEY, NET_COM_DEFAULT_TYPE);
            if (type.equalsIgnoreCase(NET_COM_CONF_TYPE_PLAIN))
            {
                netComSvc = new TcpConnectorService(
                    this,
                    msgProc,
                    bindAddress,
                    publicCtx,
                    initCtx,
                    new StltConnTracker(this, peerMap)
                );
            }
            else
            if (type.equalsIgnoreCase(NET_COM_CONF_TYPE_SSL))
            {
                String sslProtocol = netComProps.getProperty(NET_COM_CONF_SSL_PROTOCOL_KEY);
                String keyStoreFile = netComProps.getProperty(NET_COM_CONF_SSL_SERVER_CERT_KEY);
                String trustStoreFile = netComProps.getProperty(NET_COM_CONF_SSL_TRUST_CERT_KEY);
                char[] keyPasswd = netComProps.getProperty(NET_COM_CONF_SSL_KEY_PASS_KEY).toCharArray();
                char[] keyStorePasswd = netComProps.getProperty(NET_COM_CONF_SSL_KEYSTORE_PASS_KEY).toCharArray();
                char[] trustStorePasswd = netComProps.getProperty(NET_COM_CONF_SSL_TRUST_PASS_KEY).toCharArray();

                try
                {
                    netComSvc = new SslTcpConnectorService(
                        this,
                        msgProc,
                        bindAddress,
                        publicCtx,
                        initCtx,
                        new StltConnTracker(this, peerMap),
                        sslProtocol,
                        keyStoreFile,
                        keyStorePasswd,
                        keyPasswd,
                        trustStoreFile,
                        trustStorePasswd
                    );
                }
                catch (KeyManagementException keyMgmtExc)
                {
                    getErrorReporter().reportError(
                        new LinStorException(
                            "Initialization of the SSLContext failed. See cause for details",
                            keyMgmtExc
                        )
                    );
                }
                catch (UnrecoverableKeyException unrecoverableKeyExc)
                {
                    String errorMsg = "A private or public key for the initialization of SSL encryption could " +
                        "not be loaded";
                    getErrorReporter().reportError(
                        new LinStorException(
                            errorMsg,
                            errorMsg,
                            null,
                            "Check whether the password for the SSL keystores is correct.",
                            null,
                            unrecoverableKeyExc
                        )
                    );
                }
                catch (NoSuchAlgorithmException exc)
                {
                    getErrorReporter().reportError(
                        new LinStorException(
                            String.format(
                                "SSL initialization failed: " +
                                "The SSL/TLS encryption protocol '%s' is not available on this system.",
                                sslProtocol
                            ),
                            "SSL initialization failed.",
                            String.format(
                                "The SSL/TLS protocol '%s' is not available on this system",
                                sslProtocol
                            ),
                            "- Select a supported SSL/TLS protocol in the network communications configuration\n" +
                            "or\n" +
                            "- Enable support for the currently selected SSL/TLS protocol on this system",
                            null,
                            exc
                        )
                    );
                }
                catch (KeyStoreException keyStoreExc)
                {
                    throw new ImplementationError(
                        "Default SSL keystore type could not be found by the KeyStore instance",
                        keyStoreExc
                    );
                }
                catch (CertificateException exc)
                {
                    getErrorReporter().reportError(
                        new LinStorException(
                            "A required SSL certificate could not be loaded",
                            "A required SSL certificate could not be loaded from the keystore files",
                            null,
                            "Ensure that the required SSL certificates are contained in the keystore files.\n" +
                            "Refer to documentation for information on how to setup SSL encryption.",
                            null,
                            exc
                        )
                    );
                }
            }

            if (netComSvc != null)
            {
                try
                {
                    netComConnectors.put(netComSvc.getInstanceName(), netComSvc);
                    systemServicesMap.put(netComSvc.getInstanceName(), netComSvc);
                    netComSvc.start();
                    getErrorReporter().logInfo(
                        String.format(
                            "%s started on port %s:%d",
                            netComSvc.getInstanceName().displayValue,
                            addr, port
                        )
                    );
                }
                catch (SystemServiceStartException sysSvcStartExc )
                {
                    String errorMsg = sysSvcStartExc.getMessage();
                    if (errorMsg == null)
                    {
                        errorMsg = "The initial network communications service failed to start.";
                    }
                    getErrorReporter().reportError(
                        new LinStorException(
                            errorMsg,
                            errorMsg, // description
                            null, // cause
                            null, // correction
                            null, // details
                            sysSvcStartExc // Nested throwable
                        )
                    );

                }
            }
            else
            {
                if (!NET_COM_CONF_TYPE_PLAIN.equalsIgnoreCase(type) &&
                    !NET_COM_CONF_TYPE_SSL.equalsIgnoreCase(type))
                {
                    getErrorReporter().reportError(
                        new LinStorException(
                            // Message
                            String.format(
                                "The property '%s' in configuration file '%s' is misconfigured",
                                NET_COM_CONF_TYPE_KEY, NET_COM_CONF_FILE
                            ),
                            // Description
                            "The initial network communication service can not be started.",
                            // Cause
                            String.format(
                                "The service type is misconfigured.\n" +
                                "The property '%s' must be either '%s' or '%s', but was '%s'",
                                NET_COM_CONF_TYPE_KEY,
                                NET_COM_CONF_TYPE_PLAIN,
                                NET_COM_CONF_TYPE_SSL,
                                type
                            ),
                            // Error details
                            String.format(
                                "The network communication service configuration file is:\n%s",
                                NET_COM_CONF_FILE
                            ),
                            // No nested exception
                            null
                        )
                    );
                }
            }
        }
        catch (IOException ioExc)
        {
            getErrorReporter().reportError(ioExc);
        }
    }

    @Override
    public DrbdStateTracker getDrbdStateTracker()
    {
        return drbdEventSvc;
    }

    @Override
    public DeviceManager getDeviceManager()
    {
        return devMgr;
    }

    public static void main(String[] args)
    {
        System.out.printf(
            "%s, Module %s, Release %s\n",
            Satellite.PROGRAM, Satellite.MODULE, Satellite.VERSION
        );
        printStartupInfo();

        ErrorReporter errorLog = new StdErrorReporter(Satellite.MODULE, "");

        try
        {
            Thread.currentThread().setName("Main");

            // Initialize the Satellite module with the SYSTEM security context
            Initializer sysInit = new Initializer();
            Satellite instance = sysInit.initSatellite(args);

            instance.initialize(errorLog);
            instance.run();
        }
        catch (ImplementationError implError)
        {
            errorLog.reportError(implError);
        }
        catch (IOException ioExc)
        {
            errorLog.reportError(ioExc);
        }
        catch (Throwable error)
        {
            errorLog.reportError(error);
        }

        System.out.println();
    }

    public StltApiCallHandler getApiCallHandler()
    {
        return apiCallHandler;
    }

    public NodeData getLocalNode()
    {
        return (NodeData) nodesMap.get(localNodeName);
    }

    public Peer getControllerPeer()
    {
        return controllerPeer;
    }

    public void setControllerPeer(
        Peer controllerPeerRef,
        UUID nodeUuid,
        String nodeName,
        UUID disklessStorPoolDfnUuid,
        UUID disklessStorPoolUuid
    )
    {
        try
        {
            reconfigurationLock.writeLock().lock();
            nodesMapLock.writeLock().lock();
            rscDfnMapLock.writeLock().lock();
            storPoolDfnMapLock.writeLock().lock();

            controllerPeer = controllerPeerRef;

            AccessContext tmpCtx = sysCtx.clone();
            tmpCtx.getEffectivePrivs().enablePrivileges(Privilege.PRIV_SYS_ALL);

            SatelliteTransactionMgr transMgr = new SatelliteTransactionMgr();
            NodeData localNode;
            try
            {
                disklessStorPoolDfn = StorPoolDefinitionData.getInstanceSatellite(
                    tmpCtx,
                    disklessStorPoolDfnUuid,
                    new StorPoolName(LinStor.DISKLESS_STOR_POOL_NAME),
                    transMgr
                );

                localNodeName = new NodeName(nodeName);

                localNode = NodeData.getInstanceSatellite(
                    sysCtx,
                    nodeUuid,
                    localNodeName,
                    NodeType.SATELLITE,
                    new NodeFlag[] {},
                    disklessStorPoolUuid,
                    transMgr,
                    this
                );
                transMgr.commit();

                nodesMap.clear();
                rscDfnMap.clear();
                storPoolDfnMap.clear();
                // TODO: make sure everything is cleared

                nodesMap.put(localNode.getName(), localNode);
                storPoolDfnMap.put(disklessStorPoolDfn.getName(), disklessStorPoolDfn);
                setControllerPeerToCurrentLocalNode();
            }
            catch (ImplementationError | SQLException | InvalidNameException exc)
            {
                getErrorReporter().reportError(exc);
            }
        }
        catch (AccessDeniedException accDeniedExc)
        {
            throw new ImplementationError(
                "sysCtx does not have enough privileges to call node.setPeer",
                accDeniedExc
            );
        }
        finally
        {
            storPoolDfnMapLock.writeLock().unlock();
            rscDfnMapLock.writeLock().unlock();
            nodesMapLock.writeLock().unlock();
            reconfigurationLock.writeLock().unlock();
        }
    }

    public void setControllerPeerToCurrentLocalNode()
    {
        reconfigurationLock.readLock().lock();
        nodesMapLock.readLock().lock();
        try
        {
            nodesMap.get(localNodeName).setPeer(sysCtx, controllerPeer);
        }
        catch (AccessDeniedException exc)
        {
            getErrorReporter().reportError(exc);
        }
        finally
        {
            nodesMapLock.readLock().unlock();
            reconfigurationLock.readLock().unlock();
        }
    }

    public long getCurrentFullSyncId()
    {
        return fullSyncId.get();
    }

    public long getCurrentAwaitedUpdateId()
    {
        return awaitedUpdateId.get();
    }

    public void awaitedUpdateApplied()
    {
        awaitedUpdateId.incrementAndGet();
    }

    public long getNextFullSyncId()
    {
        long fullSyncId;
        try
        {
            reconfigurationLock.writeLock().lock();
            nodesMapLock.writeLock().lock();
            rscDfnMapLock.writeLock().lock();
            storPoolDfnMapLock.writeLock().lock();

            fullSyncId = this.fullSyncId.incrementAndGet();

            awaitedUpdateId.set(0);
            currentFullSyncApplied = false;
        }
        finally
        {
            storPoolDfnMapLock.writeLock().unlock();
            rscDfnMapLock.writeLock().unlock();
            nodesMapLock.writeLock().unlock();
            reconfigurationLock.writeLock().unlock();
        }
        return fullSyncId;
    }

    public void setFullSyncApplied()
    {
        currentFullSyncApplied = true;
    }

    public boolean isCurrentFullSyncApplied()
    {
        return currentFullSyncApplied;
    }
}
