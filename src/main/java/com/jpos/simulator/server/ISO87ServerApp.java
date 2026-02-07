package com.jpos.simulator.server;

import com.jpos.simulator.core.ChannelFactory;
import com.jpos.simulator.server.listener.ISO87RequestListener;

import org.jpos.iso.ISOServer;
import org.jpos.iso.ServerChannel;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;

public class ISO87ServerApp {
    public static void main(String[] args) {
        try {
            Logger logger = new Logger();
            logger.addListener(new SimpleLogListener(System.out));

            File logDir = new File("log");
            if (!logDir.exists())
                logDir.mkdirs();
            logger.addListener(new SimpleLogListener(new PrintStream(new FileOutputStream("log/ISO87HOST.log", true))));

            String configPath = "src/main/resources/xml/channel/iso87/server.xml";
            ServerChannel channel = (ServerChannel) ChannelFactory.createChannel(configPath);

            if (channel instanceof org.jpos.util.LogSource) {
                ((org.jpos.util.LogSource) channel).setLogger(logger, "iso87-channel");
            }

            int port = 0;
            if (channel instanceof org.jpos.iso.BaseChannel) {
                port = ((org.jpos.iso.BaseChannel) channel).getPort();
            }

            if (port <= 0) {
                throw new Exception("Port not configured for ISO87 Server in " + configPath);
            }

            ISOServer server = new ISOServer(port, channel, null);
            server.setLogger(logger, "iso87-server");
            server.addISORequestListener(new ISO87RequestListener());

            System.out.println("ISO87 Host Server started on port " + port + "...");
            new Thread(server).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
