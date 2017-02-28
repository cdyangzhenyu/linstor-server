package com.linbit.drbdmanage.security;

import com.linbit.drbdmanage.InvalidNameException;

/**
 * Name of a security type
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public final class SecTypeName extends SecBaseName
{
    public SecTypeName(String genName) throws InvalidNameException
    {
        super(genName);
    }
}