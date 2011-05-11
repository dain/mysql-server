package com.proofpoint.mysql;

import com.proofpoint.configuration.Config;

import javax.validation.constraints.NotNull;

public class MySqlServerConfig
{
    private String databaseName;
    private String username = "root";
    private String password;
    private Integer port;

    @NotNull
    public String getDatabaseName()
    {
        return databaseName;
    }

    @Config("mysql.database")
    public void setDatabaseName(String databaseName)
    {
        this.databaseName = databaseName;
    }

    @NotNull
    public String getUsername()
    {
        return username;
    }

    @Config("mysql.username")
    public void setUsername(String username)
    {
        this.username = username;
    }

    @NotNull
    public String getPassword()
    {
        return password;
    }

    @Config("mysql.password")
    public void setPassword(String password)
    {
        this.password = password;
    }

    public Integer getPort()
    {
        return port;
    }

    @Config("mysql.port")
    public void setPort(Integer port)
    {
        this.port = port;
    }
}
