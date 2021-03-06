package com.linbit.linstor.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class RecreateDb
{
    public static void main(String[] args) throws Exception
    {
        if(args.length < 1)
        {
            System.err.println("Usage: database.cfg <init.sql>");
            return;
        }
        if(!args[0].toLowerCase().endsWith(".cfg"))
        {
           System.err.println("First file must be the database config");
           return;
        }

        Properties props = new Properties();
        try (FileInputStream inStream = new FileInputStream(new File(args[0]))) {
            props.loadFromXML(inStream);
        }

        String url = props.getProperty("connection-url");
        String dir = url.substring("jdbc:derby:".length(), url.indexOf(";create=true"));

        if (!dir.equals("") && !dir.equals("/"))
        {
            ProcessBuilder pb = new ProcessBuilder("rm", "-rf", dir);
            pb.start().waitFor();
        }

        try (PoolingDataSource<PoolableConnection> dataSource = initConnectionProvider(args[0]);
            Connection con = dataSource.getConnection()) {
            if (args.length > 1)
            {
                for (int i = 1; i < args.length; i++)
                {
                    BufferedReader br = new BufferedReader(new FileReader(args[i]));
                    runSql(con, args[i], br);
                }
            }
            else
            {
                try (BufferedReader br =
                        new BufferedReader(new InputStreamReader(
                                RecreateDb.class.getResourceAsStream("/resource/drbd-init-derby.sql")))) {
                    runSql(con, "jar:///resource/drbd-init-derby.sql", br);
                }
            }
        }
    }

    private static PoolingDataSource<PoolableConnection> initConnectionProvider(String cfg) throws InvalidPropertiesFormatException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        Properties dbProps = new Properties();
        try (FileInputStream fis = new FileInputStream(cfg)) {
            dbProps.loadFromXML(fis);
        }

        ConnectionFactory connFactory = new DriverManagerConnectionFactory(dbProps.getProperty("connection-url"), dbProps);
        PoolableConnectionFactory poolConnFactory = new PoolableConnectionFactory(connFactory, null);

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMinIdle(10);
        poolConfig.setMaxIdle(100);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setFairness(true);

        GenericObjectPool<PoolableConnection> connPool = new GenericObjectPool<>(poolConnFactory, poolConfig);

        poolConnFactory.setPool(connPool);

        return new PoolingDataSource<>(connPool);
    }

    private static void runSql(Connection con, String sqlFilePath, BufferedReader br) throws IOException, SQLException
    {
        System.out.println("running sql file: " + sqlFilePath);
        StringBuilder cmdBuilder = new StringBuilder();
        for (String line; (line = br.readLine()) != null;)
        {
            line = line.trim();
            if (!line.startsWith("--"))
            {
                cmdBuilder.append("\n").append(line);
                if (line.endsWith(";"))
                {
                    cmdBuilder.setLength(cmdBuilder.length()-1); // cut the ;
                    String cmd = cmdBuilder.toString();
                    cmdBuilder.setLength(0);
                    try (PreparedStatement stmt = con.prepareStatement(cmd))
                    {
                        stmt.executeUpdate();
                    }
                    catch (Throwable t)
                    {
                        System.err.println(cmd);
                        throw t;
                    }
                }
            }
        }
        con.commit();
    }
}
