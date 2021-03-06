package org.dancres.blitz.remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.InvalidObjectException;

import java.lang.reflect.Method;

import java.rmi.MarshalledObject;

import java.util.Collection;
import java.util.List;

import net.jini.admin.Administrable;

import net.jini.core.transaction.Transaction;

import net.jini.core.entry.Entry;

import net.jini.space.JavaSpace;

import net.jini.core.event.RemoteEventListener;

import net.jini.id.Uuid;

import net.jini.core.constraint.RemoteMethodControl;
import net.jini.core.constraint.MethodConstraints;

import net.jini.security.proxytrust.ProxyTrustIterator;
import net.jini.security.proxytrust.SingletonProxyTrustIterator;

import com.sun.jini.proxy.ConstrainableProxyUtil;

import org.dancres.blitz.mangler.MangledEntry;

import org.dancres.util.ReflectUtil;

/**
   When the remote stub generated by the exporter implements
   RemoteMethodControl, Blitz exports this proxy for Space duties to support
   appropriate constraints etc.
 */
public final class ConstrainableBlitzProxy extends
    BlitzProxy implements RemoteMethodControl {

    /**
       The outer-layer calls all feature Entry.class whilst the server
       calls use MangledEntry so we must translate constraints which will be
       applied against the outer-layer methods to the internal methods.
     */
    private static final Method[] theMethodMapping = {
        ReflectUtil.findMethod(JavaSpace.class, "write",
                               new Class[] {Entry.class, Transaction.class,
                                            long.class}),
        ReflectUtil.findMethod(BlitzServer.class, "write",
                               new Class[] {MangledEntry.class,
                                            Transaction.class, long.class}),
                                                    
        ReflectUtil.findMethod(JavaSpace.class, "read",
                               new Class[] {Entry.class, Transaction.class,
                                            long.class}),
        ReflectUtil.findMethod(BlitzServer.class, "read",
                               new Class[] {MangledEntry.class,
                                            Transaction.class, long.class}),
                                                    
        ReflectUtil.findMethod(JavaSpace.class, "readIfExists",
                               new Class[] {Entry.class, Transaction.class,
                                            long.class}),
        ReflectUtil.findMethod(BlitzServer.class, "readIfExists",
                               new Class[] {MangledEntry.class,
                                            Transaction.class, long.class}),
                                                    
        ReflectUtil.findMethod(JavaSpace.class, "take",
                               new Class[] {Entry.class, Transaction.class,
                                            long.class}),
        ReflectUtil.findMethod(BlitzServer.class, "take",
                               new Class[] {MangledEntry.class,
                                            Transaction.class, long.class}),
                                                    
        ReflectUtil.findMethod(JavaSpace.class, "takeIfExists",
                               new Class[] {Entry.class, Transaction.class,
                                            long.class}),
        ReflectUtil.findMethod(BlitzServer.class, "takeIfExists",
                               new Class[] {MangledEntry.class,
                                            Transaction.class, long.class}),
                                                    
        ReflectUtil.findMethod(JavaSpace.class, "notify",
                               new Class[] {Entry.class, Transaction.class,
                                            RemoteEventListener.class,
                                            long.class,
                                            MarshalledObject.class}),
        ReflectUtil.findMethod(BlitzServer.class, "notify",
                               new Class[] {MangledEntry.class,
                                            Transaction.class,
                                            RemoteEventListener.class,
                                            long.class,
                                            MarshalledObject.class}),
                                                    
        ReflectUtil.findMethod(net.jini.space.JavaSpace05.class,
                               "registerForAvailabilityEvent",
                               new Class[] {Collection.class, Transaction.class,
                                                boolean.class,
                                                RemoteEventListener.class,
                                                long.class,
                                                MarshalledObject.class}),
        ReflectUtil.findMethod(JS05Server.class,
                               "registerForVisibility",
                               new Class[] {MangledEntry[].class,
                                                Transaction.class,
                                                RemoteEventListener.class,
                                                long.class,
                                                MarshalledObject.class,
                                                boolean.class}),
                                                        
        ReflectUtil.findMethod(net.jini.space.JavaSpace05.class, "write",
                               new Class[] {List.class,
                                            Transaction.class,
                                            List.class}),
        ReflectUtil.findMethod(JS05Server.class, "write",
                               new Class[] {List.class,
                                            Transaction.class,
                                            List.class}),
                                                    
        ReflectUtil.findMethod(net.jini.space.JavaSpace05.class, "take",
                               new Class[] {Collection.class,
                                                Transaction.class,
                                                long.class,
                                                long.class}),
        ReflectUtil.findMethod(JS05Server.class, "take",
                               new Class[] {MangledEntry[].class,
                                                Transaction.class,
                                                long.class,
                                                long.class}),
                                                        
        ReflectUtil.findMethod(net.jini.space.JavaSpace05.class, "contents",
                               new Class[] {Collection.class,
                                            Transaction.class,
                                            long.class,
                                            long.class}),
        ReflectUtil.findMethod(EntryViewAdmin.class, "newView",
                               new Class[] {MangledEntry[].class,
                                                Transaction.class,
                                                long.class, boolean.class,
                                                long.class, int.class}),
                                                    
        ReflectUtil.findMethod(Administrable.class, "getAdmin",
                               new Class[] {}),
        ReflectUtil.findMethod(BlitzServer.class, "getAdmin",
                               new Class[] {})
    };

    private static BlitzServer constrainStub(BlitzServer aServer,
                                             MethodConstraints aConstraints) {
        RemoteMethodControl myServer = (RemoteMethodControl) aServer;

        MethodConstraints myStubConstraints =
            ConstrainableProxyUtil.translateConstraints(aConstraints,
                                                        theMethodMapping);

        myServer.setConstraints(myStubConstraints);
        return (BlitzServer) myServer;
    }

    private final MethodConstraints theConstraints;

    ConstrainableBlitzProxy(BlitzServer aBlitz, Uuid aUuid) {
        super(aBlitz, aUuid);
        theConstraints = null;
    }

    private ConstrainableBlitzProxy(BlitzServer aBlitz, Uuid aUuid,
                                    MethodConstraints aConstraints) {
        super(constrainStub(aBlitz, aConstraints), aUuid);
        theConstraints = aConstraints;
    }

    private ProxyTrustIterator getProxyTrustIterator() {
        return new SingletonProxyTrustIterator(theStub);
    }

    public RemoteMethodControl setConstraints(MethodConstraints aConstraints) {
        return new ConstrainableBlitzProxy(theStub, theUuid, aConstraints);
    }

    public MethodConstraints getConstraints() {
        return theConstraints;
    }

    private void readObject(ObjectInputStream anOIS)
        throws IOException, ClassNotFoundException {

        anOIS.defaultReadObject();

        if(! (theStub instanceof RemoteMethodControl) ) {
            throw new InvalidObjectException("space does not implement RemoteMethodControl");
        }
    }
}
