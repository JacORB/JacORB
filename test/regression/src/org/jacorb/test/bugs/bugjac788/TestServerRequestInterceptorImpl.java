package org.jacorb.test.bugs.bugjac788;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;


/**
 * Purpose: <p> This class implements the ServerRequestInterceptor class.
 *
 * Features: <p> Thread safe
 *
 */
public class TestServerRequestInterceptorImpl extends
    org.omg.CORBA.LocalObject implements
    org.omg.PortableInterceptor.ServerRequestInterceptor {

    private static final String SERVER_REQUEST_INTERCEPTOR_NAME = "MyServerRequestInterceptor";

    // This counter is used to give a single id for each incoming request
    // This is what is store in m_request_id_slot_id
    private static int requestCounter = 0;

    // slot id used to store request id
    private int requestIdSlotId;

    private org.omg.CORBA.ORB m_orb = null;

    /**
     * Purpose: <p> Constructor
     *
     * @param info
     *            ORB init information
     * @param request_id_slot_id
     *            slot id used to store request id of the incoming request.
     *
     */
    public TestServerRequestInterceptorImpl(
        org.omg.PortableInterceptor.ORBInitInfo info, int requestIdSlotId) {
        m_orb = org.omg.CORBA.ORB.init();
        this.requestIdSlotId = requestIdSlotId;
    }

    /**
     * Purpose: <p> This interception point allows an Interceptor to query
     * request information after all the information, including operation
     * parameters, are available.
     *
     * @param ri
     *            Server request information
     */
    public void receive_request(ServerRequestInfo arg0) throws ForwardRequest,
        SystemException {
       System.out.println ("### TestServerInterceptor::receive_request");
    }

    /**
     * Purpose: <p> At this interception point, Interceptors must get their
     * service context information from the incoming request and transfer it to
     * PortableInterceptor.Current's slots.
     *
     * @param ri
     *            Server request information
     */
    public void receive_request_service_contexts(
        ServerRequestInfo reqInfo) throws ForwardRequest, SystemException {

        int requestCounter = 0;
        // Increment here M_request_counter because we are sure the request will
        // be treated at application level.
        synchronized(this)
        {
            TestServerRequestInterceptorImpl.requestCounter++;
            requestCounter = TestServerRequestInterceptorImpl.requestCounter;
        }
        org.omg.CORBA.Any slotData = null;
        try {
            slotData = reqInfo.get_slot(this.requestIdSlotId);
            slotData.insert_ulong(requestCounter);

            System.out.println ("TestServerRequestInterceptorImpl::receive_request_service_contexts set_slot m_request_id_slot_id=" + this.requestIdSlotId + " and request counter " + this.requestCounter + " thread " + Thread.currentThread ().toString());
            // CDMW_INTERNAL_1(FTLogger.GetLogger(),
            //     "receive_request_service_contexts set_slot m_request_id_slot_id="
            //         + this.requestIdSlotId, new Throwable());
            /*#####NOT NEEDED??? done above by reference */ reqInfo.set_slot(this.requestIdSlotId, slotData);
        }
        catch (org.omg.PortableInterceptor.InvalidSlot e) {
            // This slot that has not been allocated
           throw new org.omg.CORBA.INTERNAL ("Invalid Slot " + e);
            // CDMW_ERROR(FTLogger.GetLogger(),
            //     "ERROR: InvalidSlot exception raised!", new Throwable());
            // org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
            //     ExceptionMinorCodes.INTERNALFaultToleranceError,
            //     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            // throw ex;
        }

        System.out.println ("receive_request_service_contexts logs=" +requestCounter);

        // log T0 of the request treatement
//?????
////RequestTimeoutLogger.getInstance().enteringRequest(requestCounter);

    }

    /**
     * Purpose: <p> When an exception occurs, this interception point is called.
     * It allows an Interceptor to query the exception information and modify
     * the reply service context before the exception is raised to the client.
     *
     * @param ri
     *            Server request information
     */
    public void send_exception(ServerRequestInfo arg0) throws ForwardRequest,
        SystemException {
           System.out.println ("### TestServerInterceptor::send_exception");
           this.removeCurrentTimeoutRequestLog(arg0);
    }

    /**
     * Purpose: <p> This interception point allows an Interceptor to query the
     * information available when a request results in something other than a
     * normal reply or an exception. A request could result in a retry (for
     * example, a GIOP Reply with a LOCATION_FORWARD status was received).
     *
     * @param ri
     *            Server request information
     */
    public void send_other(ServerRequestInfo reqInfo) throws ForwardRequest,
        SystemException {
           System.out.println ("### TestServerInterceptor::send_other");
        this.removeCurrentTimeoutRequestLog(reqInfo);
    }

    /**
     * Purpose: <p> This interception point allows an Interceptor to query reply
     * information and modify the reply service context after the target
     * operation has been invoked and before the reply is returned to the
     * client.
     *
     * @param ri
     *            Server request information
     */
    public void send_reply(ServerRequestInfo reqInfo) {
System.out.println  ("TestServerRequestInterceptorImpl::send_reply for operation: " + reqInfo.operation());
        // CDMW_LOG_FUNCTION(FTLogger.GetLogger(),  " for operation: " + reqInfo.operation());
        this.removeCurrentTimeoutRequestLog(reqInfo);
    }

    private void removeCurrentTimeoutRequestLog(ServerRequestInfo reqInfo)
        throws SystemException {

        try {
           System.out.println ("TestServerRequestInterceptorImpl::get_slot " + this + "and reqInfo " + reqInfo + " m_request_id_slot_id=" +requestIdSlotId + " thread " + Thread.currentThread ().toString() );

            // CDMW_INTERNAL_1(FTLogger.GetLogger(), "get_slot m_request_id_slot_id="
            //     + this.requestIdSlotId, new Throwable());
            org.omg.CORBA.Any any = reqInfo.get_slot(requestIdSlotId);

            TypeCode tc = any.type();
            System.out.println ("TestServerRequestInterceptorImpl::tc . kind " +tc.kind ().value ());
            int requestId = 0;
            if ((tc.kind().value() != TCKind._tk_ulong)) {
           throw new org.omg.CORBA.INTERNAL ("ERROR: unexpected data returned by get_slot " );

               // CDMW_ERROR(FTLogger.GetLogger(),
               //      "ERROR: unexpected data returned by get_slot",
               //      new Throwable());
               //  org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
               //      ExceptionMinorCodes.INTERNALFaultToleranceError,
               //      org.omg.CORBA.CompletionStatus.COMPLETED_NO);
               //  throw ex;
            }

            requestId = any.extract_ulong(); /* ### What is the point of this line ?? */
        }
        catch (org.omg.PortableInterceptor.InvalidSlot e) {
                throw new org.omg.CORBA.INTERNAL ("InvalidSlot " + e);
       // // This slot that has not been allocated
       //      CDMW_ERROR(FTLogger.GetLogger(),
       //          "ERROR: InvalidSlot exception raised!", new Throwable());
       //      org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
       //          ExceptionMinorCodes.INTERNALFaultToleranceError,
       //          org.omg.CORBA.CompletionStatus.COMPLETED_NO);
       //      CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
       //      throw ex;
        }

        try {
            // set_slot to empty to avoid mistake if the same thread is used
            // to send request outside an incoming request context.
            org.omg.CORBA.Any slotData = m_orb.create_any();
            reqInfo.set_slot(this.requestIdSlotId, slotData);
            System.out.println ("TestServerRequestInterceptorImpl::setting " + reqInfo + " slot for " +requestIdSlotId + " to " +slotData.type ().kind ().value ());

        }
        catch (org.omg.PortableInterceptor.InvalidSlot e) {
                throw new org.omg.CORBA.INTERNAL ("InvalidSlot " + e);
            // // This slot that has not been allocated
            // CDMW_ERROR(FTLogger.GetLogger(),
            //     "ERROR: InvalidSlot exception raised!", new Throwable());
            // org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
            //     ExceptionMinorCodes.INTERNALFaultToleranceError,
            //     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            // CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
            // throw ex;
        }

    }

    /**
     * Purpose: <p> Destroy the interceptor
     *
     */
    public void destroy() {
    }

    /**
     * Purpose: <p> Return the name of the interceptor
     *
     */
    public String name() {
        return SERVER_REQUEST_INTERCEPTOR_NAME;
    }
}
