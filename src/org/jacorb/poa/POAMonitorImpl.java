package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
 
import org.jacorb.poa.except.*;
import org.jacorb.poa.util.*;
import org.jacorb.poa.gui.*;

import org.jacorb.util.Environment;
import org.jacorb.orb.dsi.ServerRequest;

import org.omg.PortableServer.*;
import org.omg.PortableServer.POAManagerPackage.State;

import java.util.Enumeration;

/**
 * This class extends the POA with a monitoring gui. It implements all
 * poa related listener interfaces and will set up and update the gui.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.06, 12/08/99, RT
 */

public class POAMonitorImpl 
    extends POAAdapter 
    implements POAMonitor, LogTrace, POAMonitorController 
{
    private POA poaModel;
    private AOM aomModel;
    private RequestQueue queueModel;
    private RPPoolManager pmModel;

    private POAMonitorView view;
        
    private LogTrace logTrace;
        
    private String prefix;  
    private boolean isSystemId;

    private int aomSize;
    private int queueSize;
    private int poolCount;
    private int poolSize;

    private Thread refreshThread;
    private boolean terminate;
    private boolean aomChanged;
    private boolean queueChanged;
    private boolean pmChanged;

    public void actionCloseView() {
        closeMonitor();
    }

    public void actionDeactivateObject(String oidStr) {
                
        if (poaModel != null) {
            try {
                poaModel.deactivate_object(POAUtil.string_to_objectId(oidStr));
            } catch (Throwable e) {
                printMessage("Exception occurred in deactivateObject() of POAMonitor: "+e);
            }
        }               
    }

    public void actionRemoveRequestFromQueue(String ridStr) {
                
        if (queueModel != null && poaModel != null) {
            try {
                ServerRequest request = queueModel.getElementAndRemove(Integer.parseInt(ridStr));
                if (request == null) throw new ApplicationError("error: rid " + ridStr + " is not contained in queue");
                poaModel.getRequestController().rejectRequest(request, new org.omg.CORBA.OBJ_ADAPTER());
            } catch (Throwable e) {
                printMessage("Exception occurred in removeRequestFromQueue() of POAMonitor: "+e);
            }
        }               
    }

    public StringPair[] actionRetrieveAOMContent() {
                
        if (aomModel != null) {
            try {
                return aomModel != null ? aomModel.deliverContent() : null;
            } catch (Throwable e) {
                printMessage("Exception occurred in retrieveAOMContent() of POAMonitor: "+e);
            }
        }
        return null;
    }
    public StringPair[] actionRetrieveQueueContent() {
                
        if (queueModel != null) {
            try {
                return queueModel.deliverContent();
            } catch (Throwable e) {
                printMessage("Exception occurred in retrieveQueueContent() of POAMonitor: "+e);
            }
        }
        return null;
    }


    public synchronized void changeState(String state) 
    {
        if (view != null) 
        {
            try 
            {
                view._setState(state);
            } catch (Throwable exception) 
            {
                logTrace.printLog(0, "Exception occurred changeSate() of POAMonitor");
                logTrace.printLog(0, exception);
            }
        }
    }


    public synchronized void closeMonitor() {
        if (view != null) {                     
            try {
                terminate = true;
                poaModel._removePOAEventListener(this);
                logTrace.setLogTrace(null);
                POAMonitor newMonitor = (POAMonitor)Class.forName("org.jacorb.poa.POAMonitorLightImpl").newInstance();
                newMonitor.init(poaModel, aomModel, queueModel, pmModel, prefix, isSystemId, logTrace);
                poaModel.setMonitor(newMonitor);
                POAMonitorView tmp = view;
                view = null;
                tmp._destroy();
                                                                
            } catch (Throwable exception) {
                logTrace.printLog(0, "Exception occurred in closeMonitor() of POAMonitorImpl");
                logTrace.printLog(0, exception);
            }
        }
    }
    public void init(POA poa, AOM aom, RequestQueue queue, RPPoolManager pm, 
                     String _prefix, boolean _isSystemId, LogTrace _logTrace) {
        poaModel = poa;
        aomModel = aom;
        queueModel = queue;
        pmModel = pm;
        prefix = prefix;
        isSystemId = _isSystemId;
        logTrace = _logTrace;
    }
    private void initView() {               
        if (view != null) {
            try {
                String name = poaModel._getQualifiedName();
                view._setName(name.equals("") ? POAConstants.ROOT_POA_NAME : 
                              POAConstants.ROOT_POA_NAME+POAConstants.OBJECT_KEY_SEPARATOR+name);

                view._setState(POAUtil.convert(poaModel.getState()));
                                
                view._setPolicyThread(POAUtil.convert(
                                                      poaModel.threadPolicy, THREAD_POLICY_ID.value));
                view._setPolicyLifespan(POAUtil.convert(
                                                        poaModel.lifespanPolicy, LIFESPAN_POLICY_ID.value));
                view._setPolicyIdUniqueness(POAUtil.convert(
                                                            poaModel.idUniquenessPolicy, ID_UNIQUENESS_POLICY_ID.value));
                view._setPolicyIdAssignment(POAUtil.convert(
                                                            poaModel.idAssignmentPolicy, ID_ASSIGNMENT_POLICY_ID.value));
                view._setPolicyServantRetention(POAUtil.convert(
                                                                poaModel.servantRetentionPolicy, SERVANT_RETENTION_POLICY_ID.value));
                view._setPolicyRequestProcessing(POAUtil.convert(
                                                                 poaModel.requestProcessingPolicy, REQUEST_PROCESSING_POLICY_ID.value));
                view._setPolicyImplicitActivation(POAUtil.convert(
                                                                  poaModel.implicitActivationPolicy, IMPLICIT_ACTIVATION_POLICY_ID.value));
                                
                view._initAOMBar(aomModel != null ? 10 : 0, true);
                                
                view._initQueueBar(10, true);
                                
                view._initActiveRequestsBar(poaModel.isSingleThreadModel() ? 1 : Environment.threadPoolMin(),
                                            poaModel.isSingleThreadModel() ? 1 : Environment.threadPoolMax());
                view._initThreadPoolBar(0);
                                
            } catch (Throwable exception) {
                logTrace.printLog(0, "Exception occurred in initView() of POAMonitor");
                logTrace.printLog(0, exception);
            }
        }
    }
    public void objectActivated(byte[] oid, Servant servant, int aom_size) {
        aomSize = aom_size;
        aomChanged = true;
        refreshAOM();
    }
    public void objectDeactivated(byte[] oid, Servant servant, int aom_size) {
        aomSize = aom_size;
        aomChanged = true;
        refreshAOM();           
    }
    public synchronized void openMonitor() {
                
        if (view == null) {
                
            try {
                aomSize = aomModel != null ? aomModel.size() : 0;
                queueSize = queueModel.size();
                poolCount = pmModel.getPoolCount();
                poolSize = pmModel.getPoolSize();

                view = new org.jacorb.poa.gui.poa.POAFrame(this, isSystemId);

                initView();
                logTrace.setLogTrace(this);             
                refreshView();

                poaModel._addPOAEventListener(this);
                                
                view._setVisible(true);
                                
            } catch (Throwable exception) {
                logTrace.printLog(0, "Exception occurred in openMonitor() of POAMonitor");
                logTrace.printLog(0, exception);
            }

            /*                      
                                    terminate = false;
                        
                                    refreshThread = new Thread() {

                                    public void run() {
                                    while (!terminate) {
                                    System.out.println(aomChanged+" "+queueChanged+" "+pmChanged);
                                    if (aomChanged) {
                                    aomChanged = false;
                                    refreshAOM();
                                    }
                                    if (queueChanged) {
                                    queueChanged = false;
                                    refreshQueue();
                                    }
                                    if (pmChanged) {
                                    pmChanged = false;
                                    refreshPM();
                                    }
                                    try {
                                    sleep(4000);
                                    } catch (InterruptedException e) {
                                    }
                                    }
                                    }
                                    };
                                    refreshThread.setDaemon(true);
                                    refreshThread.start();
            */                      
        }
    }
    private synchronized void printException(Throwable e) {
        if (view != null) {
            try {
                view._printMessage("####################################################################");
                view._printMessage("\t"+e);
                view._printMessage("####################################################################");
            } catch (Throwable exception) {
                System.err.println("Exception occurred in _printException() of POAMonitor");
            }
        }
    }
    public void printLog(int mode, byte[] objectId, String message) {
        printMessage(message);
    }
    public void printLog(int mode, ServerRequest request, String message) {
        printMessage(message);
    }
    public void printLog(int mode, ServerRequest request, State state, String message) {
        printMessage(message);
    }
    public void printLog(int mode, String message) {
        printMessage(message);
    }
    public void printLog(int mode, Throwable e) {
        printException(e);
    }
    private synchronized void printMessage(String str) {
        if (view != null) {
            try {
                view._printMessage(Environment.time()+"> "+str);
            } catch (Throwable exception) {
                System.err.println("Exception occurred in _printMessage() of POAMonitor");
            }
        }
    }
    public void processorAddedToPool(RequestProcessor processor, int pool_count, int pool_size) {
        poolCount = pool_count;
        poolSize = pool_size;
        pmChanged = true;
        refreshPM();            
    }
    public void processorRemovedFromPool(RequestProcessor processor, int pool_count, int pool_size) {
        poolCount = pool_count;
        poolSize = pool_size;
        pmChanged = true;
        refreshPM();            
    }
    private /* synchronized */ void refreshAOM() {
        if (view != null) {
            try {
                view._setValueAOMBar(aomSize);
            } catch (Throwable exception) {
                logTrace.printLog(0, "Exception occurred in refreshAOM() of POAMonitor");
                logTrace.printLog(0, exception);
            }
        }
    }
    private /* synchronized */ void refreshPM() {
        if (view != null) {
            try {
                view._setValueActiveRequestsBar(poolSize-poolCount);
                view._setMaxThreadPoolBar(poolSize);
                view._setValueThreadPoolBar(poolCount);
            } catch (Throwable exception) {
                logTrace.printLog(0, "Exception occurred in refreshPM() of POAMonitor");
                logTrace.printLog(0, exception);
            }
        }
    }
    private /* synchronized */ void refreshQueue() {
        if (view != null) {
            try {
                view._setValueQueueBar(queueSize);
            } catch (Throwable exception) {
                logTrace.printLog(0, "Exception occurred in refreshQueue() of POAMonitor");
                logTrace.printLog(0, exception);
            }
        }
    }
    private void refreshView() {
        refreshAOM();
        refreshQueue();
        refreshPM();
    }
    public void requestAddedToQueue(ServerRequest request, int queue_size) {
        queueSize = queue_size;
        queueChanged = true;
        refreshQueue();                         
    }
    public void requestRemovedFromQueue(ServerRequest request, int queue_size) {
        queueSize = queue_size;
        queueChanged = true;
        refreshQueue();         
    }
    public void setLogTrace(LogTrace _logTrace) {
        logTrace = _logTrace;
    }
}







