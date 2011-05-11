package com.proofpoint.mysql;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import com.proofpoint.log.Logger;
import com.proofpoint.node.NodeInfo;
import org.weakref.jmx.Managed;

import javax.annotation.PreDestroy;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.String.format;

public class MySqlServer
{
    private static final Logger log = Logger.get(MySqlServer.class);

    private final MysqldResource mysqlServer;
    private final String jdbcUrl;
    private final String databaseName;

    @Inject
    public MySqlServer(MySqlServerConfig config, NodeInfo nodeInfo)
            throws Exception
    {
        databaseName = config.getDatabaseName();

        File mysqlDir = new File("mysql");
        File dataDir = new File(mysqlDir, "data");
        mysqlServer = new MysqldResource(mysqlDir, dataDir);

        int port;
        if (config.getPort() != null) {
            port = config.getPort();
        }
        else {
            // random port
            ServerSocket socket = new ServerSocket();
            try {
                socket.bind(new InetSocketAddress(0));
                port = socket.getLocalPort();
            }
            finally {
                socket.close();
            }

        }

        ImmutableMap.Builder<String, String> args = ImmutableMap.builder();
        args.put(MysqldResourceI.PORT, Integer.toString(port));
        args.put(MysqldResourceI.INITIALIZE_USER, "true");
        args.put(MysqldResourceI.INITIALIZE_USER_NAME, config.getUsername());
        args.put(MysqldResourceI.INITIALIZE_PASSWORD, config.getPassword());

        mysqlServer.start("mysql", args.build());

        if (!mysqlServer.isRunning()) {
            throw new RuntimeException("MySQL did not start.");
        }

        String localhostJdbcUrl = format("jdbc:mysql://%s:%d/%s?user=%s&password=%s",
                "localhost",
                mysqlServer.getPort(),
                "mysql",
                config.getUsername(),
                config.getPassword());

        Connection connection = null;
        do {
            try {
                connection = DriverManager.getConnection(localhostJdbcUrl);
            }
            catch (SQLException e) {
                log.info("Waiting for mysql to start at " + localhostJdbcUrl);
                Thread.sleep(1000);
            }
        } while (connection == null);

        try {
            String createDatabase = format("create database %s", databaseName);
            log.debug("%s", createDatabase);
            Statement statement = connection.createStatement();
            try {
                statement.execute(createDatabase);
            }
            catch (SQLException ignored) {
            }

            String grant = format("grant ALL on %s.* to '%s'@'%%' identified by '%s'", databaseName, config.getUsername(), config.getPassword());
            log.debug("%s", grant);
            try {
                statement.execute(grant);
            }
            catch (SQLException e) {
                log.error(e);
            }
            statement.close();
        }
        finally {
            try {
                connection.close();
            }
            catch (SQLException ignored) {
            }
        }

        jdbcUrl = format("jdbc:mysql://%s:%d/%s?user=%s&password=%s",
                nodeInfo.getPublicIp().getHostAddress(),
                mysqlServer.getPort(),
                databaseName,
                config.getUsername(),
                config.getPassword());

        connection = DriverManager.getConnection(jdbcUrl);
        try {
            connection.close();
        }
        catch (SQLException ignored) {
        }
        log.info("MySql server listening at %s", jdbcUrl);
    }

    @Managed
    public boolean isRunning()
    {
        return mysqlServer.isRunning();
    }

    @Managed
    public boolean isReadyForConnections()
    {
        return mysqlServer.isReadyForConnections();
    }

    @PreDestroy
    @Managed
    public void shutdown()
    {
        mysqlServer.shutdown();
    }

    @Managed
    public String getMySqlVersion()
    {
        return mysqlServer.getVersion();
    }

    @Managed
    public String getDatabaseName()
    {
        return databaseName;
    }

    @Managed
    public String getJdbcUrl()
    {
        return jdbcUrl;
    }
}
