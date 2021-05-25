package com.severell.core.jetty;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.http.AppServer;
import com.severell.core.providers.AppProvider;
import com.severell.core.providers.ServiceProvider;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.net.URL;

public class JettyProvider extends ServiceProvider {

    public JettyProvider(Container c) {
        super(c);
    }

    @Override
    public void register() {
        this.c.singleton(BasicServlet.class, new BasicServlet(this.c));
        this.c.singleton(ServletContextHandler.class, new ServletContextHandler(ServletContextHandler.SESSIONS));

        int minThreads = Integer.parseInt(Config.get("JETTY_MIN_THREADS", "10"));
        int maxThreads = Integer.parseInt(Config.get("JETTY_MAX_THREADS", "50"));
        Server server = new Server(new QueuedThreadPool(maxThreads, minThreads));
        this.c.singleton(Server.class, server);
    }

    @Override
    public void boot() throws Exception {
        AppServer appServer = c.make(AppServer.class);
        if(appServer != null) {
            Server server = c.make(Server.class);
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(Integer.parseInt(appServer.getPort()));
            server.setConnectors(new Connector[] {connector});

            appServer.registerListener((val) -> {
                try {
                    server.start();
                    server.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            ServletContextHandler context = c.make(ServletContextHandler.class);
            context.setContextPath("/");

            SessionHandler sessionHandler = new SessionHandler();
            context.setSessionHandler(sessionHandler);

            ContextHandlerCollection contexts = new ContextHandlerCollection();

            ResourceHandler rh0 = new ResourceHandler();
            rh0.setDirectoriesListed(false);


            ContextHandler context0 = new ContextHandler();
            context0.setContextPath("/static");

            URL baseUrl  = AppProvider.class.getResource( "/compiled" );
            if(baseUrl != null) {
                String basePath = baseUrl.toExternalForm();
                if(Config.isLocal()) {
                    context0.setBaseResource(Resource.newResource(new File("src/main/resources/compiled")));
                } else {
                    context0.setBaseResource(Resource.newResource(basePath));
                }
                context0.setHandler(rh0);
                contexts.addHandler(context0);
            }

            contexts.addHandler(context);

            server.setHandler(contexts);

            BasicServlet defaultServlet = c.make(BasicServlet.class);
            ServletHolder holderPwd = new ServletHolder("default", defaultServlet);

            holderPwd.getRegistration().setMultipartConfig(new MultipartConfigElement("/tmp"));
            context.addServlet(holderPwd, "/*");
        }
    }
}
