import net.jini.core.entry.Entry
import net.jini.export.Exporter
import net.jini.jeri.BasicJeriExporter
import net.jini.jeri.ProxyTrustILFactory
import net.jini.jeri.tcp.TcpServerEndpoint
import net.jini.lookup.ui.MainUI
import net.jini.security.BasicProxyPreparer
import net.jini.security.ProxyPreparer
import org.dancres.blitz.config.Transient
import org.dancres.blitz.stats.InstanceSwitch
import org.dancres.blitz.stats.OpSwitch
import org.dancres.blitz.stats.Switch
import org.rioproject.config.Component
import org.rioproject.entry.UIDescriptorFactory
import org.rioproject.serviceui.UIFrameFactory

@Component('org.dancres.blitz')
class BlitzConfig {
    boolean syncNotifyOnWrite = true;
    boolean ignoreLogConfig = true

    String name = "Space";
    String persistDir = "${System.getProperty("user.dir")}/target/blitz/data"
    String logDir = "${System.getProperty("user.dir")}/target/blitz/log";

    int maxWriteThreads = 1
    int desiredPendingWrites = 256
    long dbCache = 27262976
    int maxDbTxns = 256
    boolean dumpDbStats = false
    boolean logCkpts = false
    int entryReposCacheSize = 4096;
    int cacheEntriesPerPartition = 32

    def getLoadBackoff() {
        def loadBackoff = [20,50]
        return loadBackoff as int[]
    }

    long entryLeaseBound = 0;
    int notifyLeaseBound = 0;
    long leaseReapInterval = 0;
    int maxOidAllocators = 512;
    int eventgenSaveInterval = 500;
    int eventgenRestartJump = 1000;
    int maxTaskThreads = 8;
    int maxEventProcessors = 4;

    // TODO: Configure TimeBarrierPersistent/Persistent
    Transient storageModel = new Transient();

    long statsDump = 60000;
    boolean compliantDestroy = false

    def getStats() {
        def stats = [new OpSwitch(OpSwitch.ALL_TYPES, OpSwitch.TAKE_OPS, true),
                     new OpSwitch(OpSwitch.ALL_TYPES, OpSwitch.READ_OPS, true),
                     new OpSwitch(OpSwitch.ALL_TYPES, OpSwitch.WRITE_OPS, true),
                     new InstanceSwitch(InstanceSwitch.ALL_TYPES, true)]
        return stats as Switch[]
    }

    Exporter serverExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
                                                     new ProxyTrustILFactory(null, null), false, true);
    ProxyPreparer notifyPreparer = new BasicProxyPreparer();
    ProxyPreparer recoveredNotifyPreparer = new BasicProxyPreparer();
    ProxyPreparer txnPreparer = new BasicProxyPreparer();
    ProxyPreparer recoveredTxnPreparer = new BasicProxyPreparer();
    ProxyPreparer activationIdPreparer = new BasicProxyPreparer();
    ProxyPreparer activationSysPreparer = new BasicProxyPreparer()

    Entry[] getInitialAttrs() {
        String uiClass = 'org.dancres.blitz.tools.dash.DashBoardFrame'
        URL url = new URL("artifact:org.dancres.blitz:blitz-ui:2.2.0")
        def entry = [UIDescriptorFactory.getUIDescriptor(MainUI.ROLE, new UIFrameFactory(url, uiClass))]
        return entry as Entry[]
    }
}