package com.proofpoint.mysql;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.proofpoint.discovery.client.ServiceAnnouncement;

import static com.proofpoint.configuration.ConfigurationModule.bindConfig;
import static com.proofpoint.discovery.client.DiscoveryBinder.discoveryBinder;
import static com.proofpoint.discovery.client.ServiceAnnouncement.serviceAnnouncement;

public class MainModule
        implements Module
{
    public void configure(Binder binder)
    {
        binder.requireExplicitBindings();
        binder.disableCircularProxies();

        binder.bind(MySqlServer.class).in(Scopes.SINGLETON);
        bindConfig(binder).to(MySqlServerConfig.class);
        discoveryBinder(binder).bindServiceAnnouncement(MySqlServerAnnouncementProvider.class);
    }

    static class MySqlServerAnnouncementProvider implements Provider<ServiceAnnouncement>
    {
        private final MySqlServer mySqlServer;

        @Inject
        MySqlServerAnnouncementProvider(MySqlServer mySqlServer)
        {
            this.mySqlServer = mySqlServer;
        }

        @Override
        public ServiceAnnouncement get()
        {
            return serviceAnnouncement(mySqlServer.getDatabaseName())
                    .addProperty("jdbc", mySqlServer.getJdbcUrl())
                    .build();
        }
    }
}
