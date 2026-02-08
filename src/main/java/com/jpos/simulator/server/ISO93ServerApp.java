package com.jpos.simulator.server;

import com.jpos.simulator.core.ChannelFactory;
import com.jpos.simulator.server.listener.ISO93RequestListener;

import org.jpos.iso.ISOServer;
import org.jpos.iso.ServerChannel;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;

public class ISO93ServerApp {
    private static ISOServer server;

    public static void main(String[] args) {
        startServer();
    }

    public static void startServer() {
        try {
            Logger logger = new Logger();
            logger.addListener(new SimpleLogListener(System.out));

            File logDir = new File("log");
            if (!logDir.exists())
                logDir.mkdirs();
            logger.addListener(new SimpleLogListener(new PrintStream(new FileOutputStream("log/ISO93HOST.log", true))));

            String configPath = "src/main/resources/xml/channel/iso93/server.xml";
            ServerChannel channel = (ServerChannel) ChannelFactory.createChannel(configPath);

            if (channel instanceof org.jpos.util.LogSource) {
                ((org.jpos.util.LogSource) channel).setLogger(logger, "iso93-channel");
            }

            int port = 0;
            if (channel instanceof org.jpos.iso.BaseChannel) {
                port = ((org.jpos.iso.BaseChannel) channel).getPort();
            }

            if (port <= 0) {
                throw new Exception("Port not configured for ISO93 Server in " + configPath);
            }

            server = new ISOServer(port, channel, null);
            server.setLogger(logger, "iso93-server");
            server.setConfiguration(new org.jpos.core.SimpleConfiguration());
            server.addISORequestListener(new ISO93RequestListener());

            System.out.println("ISO93 Host Server (Binary) started on port " + port + "...");
            new Thread(server).start();

            // Give it a moment to bind
            Thread.sleep(500);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopServer() {
        if (server != null) {
            System.out.println("Stopping ISO93 Host Server...");
            server.shutdown();
        }
    }
}
