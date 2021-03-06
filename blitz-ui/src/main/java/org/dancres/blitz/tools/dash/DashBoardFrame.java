package org.dancres.blitz.tools.dash;

import net.jini.admin.Administrable;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.entry.Name;
import org.dancres.blitz.remote.StatsAdmin;
import org.dancres.blitz.stats.Stat;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashBoardFrame extends JFrame implements Runnable {
    static final Logger theLogger = Logger.getLogger("org.dancres.blitz.tools.dash.DashBoardFrame");
    private StatsAdmin _admin;
    private DashBoard _dashBoard;
    private Thread _updater;
    private boolean _exitOnClose;

    public DashBoardFrame(final Object roleObject) {
        super();
        getAccessibleContext().setAccessibleName("Blitz dashboard");
        try {
            ServiceItem item = (ServiceItem) roleObject;
            String name = null;
            for (Entry attribute : item.attributeSets) {
                if (attribute instanceof Name) {
                    name = ((Name)attribute).name;
                    break;
                }
            }
            String title = String.format("%s [%s]", StartDashBoard.VER, name);
            setTitle(title);
            Administrable admin = (Administrable) item.service;
            init((StatsAdmin) admin.getAdmin(), false);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public DashBoardFrame(String title, StatsAdmin admin, boolean exitOnClose) {
        super(title);
        init(admin, exitOnClose);
    }

    private void init(StatsAdmin admin, boolean exitOnClose) {

        // System.out.println("Blitz Dashboard: Developed in association with Inca X (www.incax.com)");

        _admin = admin;
        _exitOnClose = exitOnClose;
        setResizable(false);

        if (exitOnClose) {
            try {
                String lfClassName = UIManager.getSystemLookAndFeelClassName();
                String uiClass = "javax.swing.plaf.metal.MetalLookAndFeel";
                if (uiClass.equals(lfClassName)) {
                    MetalLookAndFeel.setCurrentTheme(new BlitzTheme());
                }

                UIManager.setLookAndFeel(lfClassName);
                ImageIcon icon = new ImageIcon(getClass().getResource("images/blitz.gif"));
                setIconImage(icon.getImage());

            } catch (Exception ex) {
                theLogger.log(Level.SEVERE, "Exception in close", ex);
            }
        }

        setSize(880, 140);

        //add listener to intterupt thread on close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeWin();

            }

        });

        getContentPane().add(createUI(), BorderLayout.CENTER);

        _updater = new Thread(this);
        _updater.start();
    }

    public Dimension getPreferredSize() {
        return new Dimension(740, 140);
    }

    private void closeWin() {
        dispose();
        _updater.interrupt();

        if (_exitOnClose) {
            System.exit(0);
        }
    }

    //added so the ServiceUI WindowListener gets notified
    private void postWindowClosingEvent() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    //creates the UI for this application
    private JComponent createUI() {

        _dashBoard = new DashBoard(this);
        _dashBoard.init(_admin);
        return _dashBoard;

    }

    public void run() {
        while (!_updater.isInterrupted()) {
            try {
                //need to be configurable
                Thread.sleep(1500);

                Stat[] stats = _admin.getStats();
                _dashBoard.update(stats);
                //now pass on to main global view

            } catch (InterruptedException ex) {
                // System.out.println("Updater thread exiting");
                return;
            } catch (java.rmi.ConnectException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
                postWindowClosingEvent();

            } catch (java.rmi.NoSuchObjectException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
                postWindowClosingEvent();

            } catch (Exception ex) {
                int ok = JOptionPane.showConfirmDialog(this,
                                                       "" +
                                                       ex.getMessage() +
                                                       "\n\nDo you want to continue monitoring stats?"
                                                          , "Error",
                                                       JOptionPane.YES_NO_OPTION);

                if (ok == JOptionPane.NO_OPTION) {
                    postWindowClosingEvent();
                }
                theLogger.log(Level.INFO, "Exception in update", ex);
            }
        }
    }
}
