package com.jpos.simulator.core;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOPackager;

import java.io.File;
import java.util.List;

public class ChannelFactory {

    public static ISOChannel createChannel(String xmlFile) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(new File(xmlFile));
        Element root = document.getRootElement();

        // Instantiate Channel
        String channelClass = root.getAttributeValue("class");
        ISOChannel channel = (ISOChannel) Class.forName(channelClass).getDeclaredConstructor().newInstance();

        // Instantiate Packager
        String packagerClass = root.getAttributeValue("packager");
        if (packagerClass != null) {
            ISOPackager packager = (ISOPackager) Class.forName(packagerClass).getDeclaredConstructor().newInstance();
            channel.setPackager(packager);
        }

        // Configure
        Configuration config = new SimpleConfiguration();
        List<Element> properties = root.getChildren("property");
        for (Element prop : properties) {
            String name = prop.getAttributeValue("name");
            String value = prop.getAttributeValue("value");
            config.put(name, value);
        }

        // Configure Packager if it's GenericPackager and packager-config is present
        if (channel.getPackager() instanceof org.jpos.iso.packager.GenericPackager) {
            String packagerConfig = config.get("packager-config");
            if (packagerConfig != null) {
                ((org.jpos.iso.packager.GenericPackager) channel.getPackager()).readFile(packagerConfig);
            }
        }

        if (channel instanceof org.jpos.core.Configurable) {
            ((org.jpos.core.Configurable) channel).setConfiguration(config);
        }

        // Explicitly set port and host for BaseChannel if present in config
        if (channel instanceof org.jpos.iso.BaseChannel) {
            org.jpos.iso.BaseChannel bc = (org.jpos.iso.BaseChannel) channel;
            if (config.get("port") != null) {
                bc.setPort(config.getInt("port"));
            }
            if (config.get("host") != null) {
                bc.setHost(config.get("host"));
            }
        }

        return channel;
    }
}
