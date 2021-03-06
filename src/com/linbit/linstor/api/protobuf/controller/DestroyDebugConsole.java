package com.linbit.linstor.api.protobuf.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.linbit.ImplementationError;
import com.linbit.linstor.CoreServices;
import com.linbit.linstor.api.ApiConsts;
import com.linbit.linstor.api.protobuf.BaseProtoApiCall;
import com.linbit.linstor.api.protobuf.ProtobufApiCall;
import com.linbit.linstor.core.Controller;
import com.linbit.linstor.netcom.IllegalMessageStateException;
import com.linbit.linstor.netcom.Message;
import com.linbit.linstor.netcom.Peer;
import com.linbit.linstor.proto.javainternal.MsgDebugReplyOuterClass.MsgDebugReply;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;

/**
 * Destroys (frees) the peer's debug console
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
@ProtobufApiCall
public class DestroyDebugConsole extends BaseProtoApiCall
{
    private Controller      ctrl;
    private CoreServices    coreSvcs;

    public DestroyDebugConsole(
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
        return "Detaches a debug console from a peer connection and destroys the debug console object";
    }

    @Override
    public String getName()
    {
        return ApiConsts.API_DSTR_DBG_CNSL;
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
            Message reply = client.createMessage();
            ByteArrayOutputStream replyOut = new ByteArrayOutputStream();
            writeProtoMsgHeader(replyOut, msgId, "DebugReply");

            MsgDebugReply.Builder msgDbgReplyBld = MsgDebugReply.newBuilder();
            try
            {
                ctrl.destroyDebugConsole(accCtx, client);

                msgDbgReplyBld.addDebugOut("Debug console destroyed");
            }
            catch (AccessDeniedException accessExc)
            {
                coreSvcs.getErrorReporter().reportError(accessExc);
                msgDbgReplyBld.addDebugErr(
                    "Error:\n" +
                    "    The request to destroy the debug console was denied.\n" +
                    "Cause:\n    " +
                    accessExc.getMessage() +
                    "\n"
                );
            }

            {
                MsgDebugReply dbgReply = msgDbgReplyBld.build();
                dbgReply.writeDelimitedTo(replyOut);
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
}
