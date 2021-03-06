package com.linbit.linstor.security;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.linstor.ControllerDatabase;
import com.linbit.linstor.core.Controller;
import com.linbit.linstor.core.ControllerArguments;
import com.linbit.linstor.core.Satellite;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Initializes Controller and Satellite instances with the system's security context
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public final class Initializer
{
    private AccessContext SYSTEM_CTX;
    private AccessContext PUBLIC_CTX;

    public Initializer()
    {
        PrivilegeSet sysPrivs = new PrivilegeSet(Privilege.PRIV_SYS_ALL);

        // Create the system's security context
        SYSTEM_CTX = new AccessContext(
            Identity.SYSTEM_ID,
            Role.SYSTEM_ROLE,
            SecurityType.SYSTEM_TYPE,
            sysPrivs
        );

        PrivilegeSet publicPrivs = new PrivilegeSet();

        PUBLIC_CTX = new AccessContext(
            Identity.PUBLIC_ID,
            Role.PUBLIC_ROLE,
            SecurityType.PUBLIC_TYPE,
            publicPrivs
        );

        try
        {
            AccessContext initCtx = SYSTEM_CTX.clone();
            initCtx.getEffectivePrivs().enablePrivileges(Privilege.PRIV_SYS_ALL);

            // Adjust the type enforcement rules for the SYSTEM domain/type
            SecurityType.SYSTEM_TYPE.addRule(
                initCtx,
                SecurityType.SYSTEM_TYPE, AccessType.CONTROL
            );
        }
        catch (AccessDeniedException accessExc)
        {
            throw new ImplementationError(
                "The built-in SYSTEM security context has insufficient privileges " +
                "to initialize the security subsystem.",
                accessExc
            );
        }
    }

    public Controller initController(ControllerArguments cArgs)
    {
        return new Controller(SYSTEM_CTX, PUBLIC_CTX, cArgs);
    }

    public Satellite initSatellite(String[] args)
        throws IOException
    {
        return new Satellite(SYSTEM_CTX, PUBLIC_CTX, args);
    }

    public static void load(AccessContext accCtx, ControllerDatabase ctrlDb, DbAccessor driver)
        throws SQLException, AccessDeniedException, InvalidNameException
    {
        accCtx.getEffectivePrivs().requirePrivileges(Privilege.PRIV_SYS_ALL);

        SecurityLevel.load(ctrlDb, driver);
        Identity.load(ctrlDb, driver);
        SecurityType.load(ctrlDb, driver);
        Role.load(ctrlDb, driver);
    }
}
