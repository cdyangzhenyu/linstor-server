package com.linbit.linstor;

import com.linbit.InvalidNameException;
import com.linbit.GenericName;

import com.linbit.Checks;

/**
 * Valid name of a linstor resource
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public class ResourceName extends GenericName
{
    public static final int MIN_LENGTH = 2;
    public static final int MAX_LENGTH = 48;

    public static final byte[] VALID_CHARS = { '_' };
    public static final byte[] VALID_INNER_CHARS = { '-' };

    public ResourceName(String resName) throws InvalidNameException
    {
        super(resName);
        Checks.nameCheck(resName, MIN_LENGTH, MAX_LENGTH, VALID_CHARS, VALID_INNER_CHARS);
    }
}
