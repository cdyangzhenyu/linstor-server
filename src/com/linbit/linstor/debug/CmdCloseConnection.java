package com.linbit.linstor.debug;

import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.security.AccessContext;

/**
 * Closes a peer connection
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public class CmdCloseConnection extends BaseDebugCmd
{
    private static final String PRM_CONN_ID = "CONNID";

    private static final Map<String, String> PARAMETER_DESCRIPTIONS = new TreeMap<>();

    static
    {
        PARAMETER_DESCRIPTIONS.put(
            PRM_CONN_ID,
            "The ID of the peer connection"
        );
    }

    public CmdCloseConnection()
    {
        super(
            new String[]
            {
                "ClsCon"
            },
            "Close connection",
            "Closes a peer connection",
            PARAMETER_DESCRIPTIONS,
            null,
            false
        );
    }

    @Override
    public void execute(
        PrintStream debugOut,
        PrintStream debugErr,
        AccessContext accCtx,
        Map<String, String> parameters
    ) throws Exception
    {
        String connId = parameters.get(PRM_CONN_ID);
        if (connId != null)
        {
            Peer peerObj = cmnDebugCtl.getPeer(connId);
            if (peerObj != null)
            {
                peerObj.closeConnection();
                debugOut.printf("Closing the connection to the peer ID '%s'.\n", connId);
            }
            else
            {
                printError(
                    debugErr,
                    "No peer connection was found for the specified ID.",
                    "The ID did not match any registered peer connections.",
                    "Enter the ID of a registered peer connection to disconnect that peer.",
                    "The specified ID was '" + connId + "'"
                );
            }
        }
        else
        {
            printMissingParamError(debugErr, PRM_CONN_ID);
        }
    }
}
