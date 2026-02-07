package com.jpos.simulator.core;

import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import java.io.IOException;

@FunctionalInterface
public interface ISOTransaction {
    void execute(ISOChannel channel) throws ISOException, IOException;
}
