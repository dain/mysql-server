package com.proofpoint.mysql;

import com.google.inject.Injector;
import com.proofpoint.bootstrap.Bootstrap;
import com.proofpoint.discovery.client.Announcer;
import com.proofpoint.discovery.client.DiscoveryModule;
import com.proofpoint.event.client.HttpEventModule;
import com.proofpoint.experimental.jmx.JmxHttpModule;
import com.proofpoint.json.JsonModule;
import com.proofpoint.http.server.HttpServerModule;
import com.proofpoint.jaxrs.JaxrsModule;
import com.proofpoint.jmx.JmxModule;
import com.proofpoint.node.NodeModule;
import org.weakref.jmx.guice.MBeanModule;

public class Main
{
    public static void main(String[] args)
            throws Exception
    {
        Bootstrap app = new Bootstrap(
                new NodeModule(),
                new DiscoveryModule(),
                new HttpServerModule(),
                new JsonModule(),
                new JaxrsModule(),
                new MBeanModule(),
                new JmxModule(),
                new JmxHttpModule(),
                new HttpEventModule(),
                new MainModule());

        Injector injector = app.strictConfig().initialize();
        injector.getInstance(Announcer.class).start();
    }
}