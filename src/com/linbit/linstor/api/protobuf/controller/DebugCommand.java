package com.linbit.linstor.api.protobuf.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;

import com.linbit.ImplementationError;
import com.linbit.linstor.ControllerPeerCtx;
import com.linbit.linstor.CoreServices;
import com.linbit.linstor.api.protobuf.BaseProtoApiCall;
import com.linbit.linstor.api.protobuf.ProtobufApiCall;
import com.linbit.linstor.core.Controller;
import com.linbit.linstor.debug.DebugConsole;
import com.linbit.linstor.netcom.IllegalMessageStateException;
import com.linbit.linstor.netcom.Message;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.proto.javainternal.MsgDebugCommandOuterClass.MsgDebugCommand;
import com.linbit.linstor.proto.javainternal.MsgDebugReplyOuterClass.MsgDebugReply;
import com.linbit.linstor.security.AccessContext;

/**
 * Pipes a debug command from the peer to the server and the
 * command's information and error reply back to the peer
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
@ProtobufApiCall
public class DebugCommand extends BaseProtoApiCall
{
    private Controller ctrl;
    private CoreServices coreSvcs;

    public DebugCommand(
        Controller ctrlRef,
        CoreServices coreSvcsRef
    )
    {
        super(ctrlRef.getErrorReporter());
        ctrl = ctrlRef;
        coreSvcs = coreSvcsRef;
    }

    @Override
    public String getDescription()
    {
        return "Submits a debug command to an active debug console attached to the peer.\n" +
            "The debug console typically answers with a DebugReply message.\n";
    }

    @Override
    public String getName()
    {
        return DebugCommand.class.getSimpleName();
    }

    @Override
    public void executeImpl(
        AccessContext   accCtx,
        Message         msg,
        int             msgId,
        InputStream     msgDataIn,
        Peer            client
    )
        throws IOException
    {
        try
        {
            ByteArrayOutputStream replyOut = new ByteArrayOutputStream();
            writeProtoMsgHeader(replyOut, msgId, "DebugReply");
            ByteArrayOutputStream debugErr = new ByteArrayOutputStream();
            MsgDebugReply.Builder msgDbgReplyBld = MsgDebugReply.newBuilder();

            ControllerPeerCtx peerContext = (ControllerPeerCtx) client.getAttachment();
            DebugConsole dbgConsole = peerContext.getDebugConsole();
            if (dbgConsole != null)
            {
                MsgDebugCommand msgDbgCmd = MsgDebugCommand.parseDelimitedFrom(msgDataIn);
                ByteArrayInputStream cmdIn = new ByteArrayInputStream(msgDbgCmd.getCmdLine().getBytes());

                ByteArrayOutputStream debugOut = new ByteArrayOutputStream();
                dbgConsole.streamsConsole(
                    "",
                    cmdIn,
                    new PrintStream(debugOut),
                    new PrintStream(debugErr),
                    false
                );

                String[] debugOutData = toLinesArray(debugOut.toByteArray());
                String[] debugErrData = toLinesArray(debugErr.toByteArray());

                for (String line : debugOutData)
                {
                    msgDbgReplyBld.addDebugOut(line);
                }
                for (String line : debugErrData)
                {
                    msgDbgReplyBld.addDebugErr(line);
                }
            }
            else
            {
                msgDbgReplyBld.addDebugErr(
                    "There is no active debug console for this peer"
                );
            }

            MsgDebugReply msgDbgReply = msgDbgReplyBld.build();
            Message reply = client.createMessage();
            {
                msgDbgReply.writeDelimitedTo(replyOut);
                reply.setData(replyOut.toByteArray());
            }
            client.sendMessage(reply);
        }
        catch (IllegalMessageStateException msgExc)
        {
            coreSvcs.getErrorReporter().reportError(
                new ImplementationError(
                    Message.class.getName() + " object returned by the " + Peer.class.getName() +
                    " class has an illegal state",
                    msgExc
                )
            );
        }
    }

    private String[] toLinesArray(byte[] text)
    {
        Deque<String> debugOutLines = new LinkedList<>();
        int offset = 0;
        for (int idx = 0; idx < text.length; ++idx)
        {
            if (text[idx] == '\n')
            {
                debugOutLines.add(new String(text, offset, idx - offset));
                offset = idx + 1;
            }
        }
        String[] strArray = new String[debugOutLines.size()];
        return debugOutLines.toArray(strArray);
    }
}
