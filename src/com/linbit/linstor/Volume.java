package com.linbit.linstor;

import com.linbit.TransactionObject;
import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.stateflags.Flags;
import com.linbit.linstor.stateflags.FlagsHelper;
import com.linbit.linstor.stateflags.StateFlags;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface Volume extends TransactionObject, DbgInstanceUuid
{
    public UUID getUuid();

    public Resource getResource();

    public ResourceDefinition getResourceDefinition();

    public VolumeDefinition getVolumeDefinition();

    public Props getProps(AccessContext accCtx) throws AccessDeniedException;

    public StateFlags<VlmFlags> getFlags();

    public VolumeConnection getVolumeConnection(AccessContext dbCtx, Volume otherVol)
        throws AccessDeniedException;

    public void setVolumeConnection(AccessContext accCtx, VolumeConnection volumeConnection)
        throws AccessDeniedException;

    public void removeVolumeConnection(AccessContext accCtx, VolumeConnection volumeConnection)
        throws AccessDeniedException;

    public StorPool getStorPool(AccessContext accCtx) throws AccessDeniedException;

    public String getBlockDevicePath(AccessContext accCtx) throws AccessDeniedException;

    public String getMetaDiskPath(AccessContext accCtx) throws AccessDeniedException;

    public void markDeleted(AccessContext accCtx) throws AccessDeniedException, SQLException;

    public void setBlockDevicePath(AccessContext accCtx, String path) throws AccessDeniedException;

    public void setMetaDiskPath(AccessContext accCtx, String path) throws AccessDeniedException;

    public void delete(AccessContext accCtx) throws AccessDeniedException, SQLException;

    public enum VlmFlags implements Flags
    {
        CLEAN(1L),
        DELETE(2L);

        public final long flagValue;

        private VlmFlags(long value)
        {
            flagValue = value;
        }

        @Override
        public long getFlagValue()
        {
            return flagValue;
        }

        public static VlmFlags[] restoreFlags(long vlmFlags)
        {
            List<VlmFlags> flagList = new ArrayList<>();
            for (VlmFlags flag : VlmFlags.values())
            {
                if ((vlmFlags & flag.flagValue) == flag.flagValue)
                {
                    flagList.add(flag);
                }
            }
            return flagList.toArray(new VlmFlags[flagList.size()]);
        }

        public static List<String> toStringList(long flagsMask)
        {
            return FlagsHelper.toStringList(VlmFlags.class, flagsMask);
        }

        public static long fromStringList(List<String> listFlags)
        {
            return FlagsHelper.fromStringList(VlmFlags.class, listFlags);
        }
    }

    VlmApi getApiData(AccessContext accCtx) throws AccessDeniedException;

    interface VlmApi
    {
        UUID getVlmUuid();
        UUID getVlmDfnUuid();
        String getStorPoolName();
        UUID getStorPoolUuid();
        String getBlockDevice();
        String getMetaDisk();
        int getVlmNr();
        int getVlmMinorNr();
        long getFlags();
        Map<String, String> getVlmProps();
    }
}
