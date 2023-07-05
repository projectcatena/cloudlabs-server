package com.cloudlabs.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {

    @Override
    @RequestMapping(path = "tunnel")
    protected void handleTunnelRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException {
        // TODO Auto-generated method stub
        super.handleTunnelRequest(request, response);
    }

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request)
            throws GuacamoleException {

        // Create our configuration
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol(request.getHeader("protocol"));
        config.setParameter("hostname", request.getHeader("hostname"));
        config.setParameter("port", request.getHeader("port"));
        config.setParameter("username", request.getHeader("username"));
        config.setParameter("password", request.getHeader("password"));
        config.setParameter("ignore-cert", request.getHeader("ignoreCert"));

        // Connect to guacd - everything is hard-coded here.
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket("localhost", 4822), config);

        // Return a new tunnel which uses the connected socket
        return new SimpleGuacamoleTunnel(socket);
    }
}
