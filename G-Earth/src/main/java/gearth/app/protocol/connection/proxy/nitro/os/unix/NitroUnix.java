package gearth.app.protocol.connection.proxy.nitro.os.unix;

import gearth.app.protocol.connection.proxy.nitro.os.NitroOsFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class NitroUnix implements NitroOsFunctions {

    private static final Logger LOG = LoggerFactory.getLogger(NitroUnix.class);

    private static final String PROXY_IGNORE = "discord.com,discordapp.com,canary.discord.com,canary.discordapp.com,github.com,gateway.discord.gg";

    @Override
    public boolean isRootCertificateTrusted(File certificate) {
        return true;
    }

    @Override
    public boolean installRootCertificate(File certificate) {
        return true;
    }

    @Override
    public boolean registerSystemProxy(String host, int port) {
        try {
            final String kwriteconfig = resolveKwriteconfig();
            final String proxyValue = "http://" + host + " " + port;

            writeProxySetting(kwriteconfig, "ProxyType", "1");
            writeProxySetting(kwriteconfig, "httpProxy", proxyValue);
            writeProxySetting(kwriteconfig, "httpsProxy", proxyValue);
            writeProxySetting(kwriteconfig, "NoProxyFor", PROXY_IGNORE);

            notifyKioslave();

            return true;
        } catch (IOException e) {
            LOG.error("Error while registering KDE system proxy", e);
            showError("Could not automatically configure the KDE proxy.\n\n" +
                    "You can set it manually in System Settings > Network > Proxy:\n   " +
                    host + ":" + port);
        }

        return false;
    }

    @Override
    public boolean unregisterSystemProxy() {
        try {
            final String kwriteconfig = resolveKwriteconfig();

            writeProxySetting(kwriteconfig, "ProxyType", "0");

            notifyKioslave();

            return true;
        } catch (IOException e) {
            LOG.error("Error while unregistering KDE system proxy", e);
        }

        return false;
    }

    private void writeProxySetting(String kwriteconfig, String key, String value) throws IOException {
        Runtime.getRuntime().exec(new String[]{
                kwriteconfig, "--file", "kioslaverc", "--group", "Proxy Settings", "--key", key, value
        });
    }

    private void notifyKioslave() {
        try {
            Runtime.getRuntime().exec(new String[]{
                    "dbus-send", "--type=signal", "/KIO/Scheduler",
                    "org.kde.KIO.Scheduler.reparseSlaveConfiguration", "string:"
            });
        } catch (IOException e) {
            LOG.warn("Could not notify KDE of the proxy configuration change", e);
        }
    }

    private String resolveKwriteconfig() {
        return commandExists("kwriteconfig6") ? "kwriteconfig6" : "kwriteconfig5";
    }

    private boolean commandExists(String command) {
        try {
            final Process process = new ProcessBuilder("which", command).start();
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "G-Earth", JOptionPane.ERROR_MESSAGE);
    }
}
