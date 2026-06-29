package gearth.app.protocol.connection.proxy.nitro.os;

import gearth.app.misc.OSValidator;
import gearth.app.protocol.connection.proxy.nitro.os.macos.NitroMacOS;
import gearth.app.protocol.connection.proxy.nitro.os.windows.NitroWindows;
import gearth.app.protocol.connection.proxy.nitro.os.unix.NitroUnix;

public final class NitroOsFunctionsFactory {

    public static NitroOsFunctions create() {
        if (OSValidator.isWindows()) {
            return new NitroWindows();
        }

        if (OSValidator.isUnix()) {
            return new NitroUnix();
        }

        if (OSValidator.isMac()) {
            return new NitroMacOS();
        }

        throw new UnsupportedOperationException("unsupported operating system");
    }
}
