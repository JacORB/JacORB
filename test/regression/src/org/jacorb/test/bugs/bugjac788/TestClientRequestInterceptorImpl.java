package org.jacorb.test.bugs.bugjac788;

import org.omg.CORBA.Any;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;


/**
 * Purpose: <p> This class implements the ClientRequestInterceptor class.
 *
 * Features: <p> Thread safe
 *
 */
public class TestClientRequestInterceptorImpl extends
    org.omg.CORBA.LocalObject implements
    org.omg.PortableInterceptor.ClientRequestInterceptor {
    // current object request id slot id
    private int requestIdSlotId;

    private static final String CLIENT_REQUEST_INTERCEPTOR_NAME = "MyClientRequestInterceptor";

    /**
     * Purpose: <p> Constructor
     *
     * @param info
     *            ORB init information
     * @param requestIdSlotId
     *            slot id used to store the date of the outgoing request.
     *
     */
    public TestClientRequestInterceptorImpl(
        org.omg.PortableInterceptor.ORBInitInfo info, int requestIdSlotId) {
        this.requestIdSlotId = requestIdSlotId;
    }

    /**
     * Purpose: <p> When an exception occurs, this interception point is called.
     * It allows an Interceptor to query the exception's information before it
     * is raised to the client.
     *
     * @param ri
     *            Client request information
     */
    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest,
        SystemException {

       System.out.println ("### TestClientRequestInterceptorImpl::receive_exception");
        try {
            // Get request_id
            int requestId = 0;
            Any any = ri.get_slot(requestIdSlotId);

            TypeCode tc = any.type();

            if (tc.kind().value() != TCKind._tk_ulong) {

                if (tc.kind().value() == TCKind._tk_null) {
                    // the request invocation is done in the context of an
                    // incoming
                    // request processing.
                   System.out.println ("request_id_slot is not set");
                    // CDMW_INTERNAL_1(FTLogger.GetLogger(),
                    //     "request_id_slot is not set", new Throwable());
                }
                else {
                   throw new org.omg.CORBA.INTERNAL ("Unexpected type stored in request_id_slot");

                   // CDMW_ERROR(FTLogger.GetLogger(),
                   //      "Unexpected type stored in request_id_slot",
                   //      new Throwable());
                   //  org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
                   //      ExceptionMinorCodes.INTERNALFaultToleranceError,
                   //      org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                   //  throw ex;
                }
            }
            else {
                requestId = any.extract_ulong();
            }

        }
        catch (org.omg.PortableInterceptor.InvalidSlot e) {
           e.printStackTrace ();
                   throw new org.omg.CORBA.INTERNAL ("Invalid Slot " + e);
           // // This slot that has not been allocated
           //  CDMW_ERROR(FTLogger.GetLogger(),
           //      "ERROR: InvalidSlot exception raised!", new Throwable());
           //  org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
           //      ExceptionMinorCodes.INTERNALFaultToleranceError,
           //      org.omg.CORBA.CompletionStatus.COMPLETED_NO);
           //  throw ex;
        }
        // catch (cdmw.common.NotFoundException e) {
            // // The request id is not found
            // org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
            //     ExceptionMinorCodes.INTERNALFaultToleranceError,
            //     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            // CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
            // throw ex;
        // }
        catch (SystemException ex) {
           throw ex;
            // CDMW_INTERNAL_1(FTLogger.GetLogger(),
            //     "CORBA System exception raised in receive_exception!\n"
            //         + ex.getMessage(), new Throwable());
            // throw ex;
        }
    }

    /**
     * Purpose: <p> This interception point allows an Interceptor to query the
     * information available when a request results in something other than a
     * normal reply or an exception. For example, a request could result in a
     * retry (for example, a GIOP Reply with a LOCATION_FORWARD status was
     * received); or on asynchronous calls, the reply does not immediately
     * follow the request, but control shall return to the client and an ending
     * interception point shall be called.
     *
     * @param ri
     *            Client request information
     */
    public void receive_other(ClientRequestInfo ri) throws ForwardRequest,
        SystemException {
       System.out.println ("### TestClientRequestInterceptorImpl::receive_other");

        try {
            // Get request_id
            int requestId;
            Any any = ri.get_slot(requestIdSlotId);

            TypeCode tc = any.type();

            if (tc.kind().value() != TCKind._tk_ulong) {

                if (tc.kind().value() == TCKind._tk_null) {
                    // the request invocation is done in the context of an
                    // incoming request processing.
                   System.out.println ("request_id_slot is not set");
                    // CDMW_INTERNAL_1(FTLogger.GetLogger(),
                    //     "request_id_slot is not set", new Throwable());
                }
                else {
                   throw new org.omg.CORBA.INTERNAL ("Unexpected type stored in request_id_slot");
                    // CDMW_ERROR(FTLogger.GetLogger(),
                    //     "Unexpected type stored in request_id_slot",
                    //     new Throwable());
                    // org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
                    //     ExceptionMinorCodes.INTERNALFaultToleranceError,
                    //     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                    // CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
                    // throw ex;
                }
            }
            else {
                requestId = any.extract_ulong();
            }

        }
        catch (org.omg.PortableInterceptor.InvalidSlot e) {
       e.printStackTrace ();
           throw new org.omg.CORBA.INTERNAL ("Invalid Slot " + e);
                // // This slot that has not been allocated
            // CDMW_ERROR(FTLogger.GetLogger(),
            //     "ERROR: InvalidSlot exception raised!", new Throwable());
            // org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
            //     ExceptionMinorCodes.INTERNALFaultToleranceError,
            //     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            //throw e;
        }
        // catch (cdmw.common.NotFoundException e) {
            // // The request id is not found
            // org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
            //     ExceptionMinorCodes.INTERNALFaultToleranceError,
            //     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        //     throw e;
        // }
        catch (SystemException ex) {
            // CDMW_INTERNAL_1(FTLogger.GetLogger(),
            //     "CORBA System exception raised in receive_other!\n"
            //         + ex.getMessage(), new Throwable());
            throw ex;
        }
    }

    /**
     * Purpose: <p> This interception point allows an Interceptor to query the
     * information on a reply after it is returned from the server and before
     * control is returned to the client. Here the date of the outgoing request
     * is reset and T0 of incoming request updated to remove the duration of the
     * outgoing request.
     *
     * @param ri
     *            Client request information
     */
    public void receive_reply(ClientRequestInfo ri)
        throws org.omg.CORBA.SystemException {
       System.out.println ("### TestClientRequestInterceptorImpl::receive_reply ");

        try {
            // Get request_id
            int requestId;
            Any slotData = ri.get_slot(this.requestIdSlotId);

            TypeCode tc = slotData.type();
            System.out.println ("### TestClientRequestInterceptorImpl::receive_reply " + ri + " and slot id " + requestIdSlotId + " and type " + tc.kind().value() + " thread " + Thread.currentThread ());

            if (tc.kind().value() != TCKind._tk_ulong) {

                if (tc.kind().value() == TCKind._tk_null) {
                    // the request invocation is not the result of an incoming
                    // request
                    // processing.
                   System.out.println ("request_id_slot is not set");
                    // CDMW_INTERNAL_1(FTLogger.GetLogger(),
                    //     "request_id_slot is not set", new Throwable());
                }
                else {
                        throw new org.omg.CORBA.INTERNAL ("Unexpected type stored in request_id_slot");
               // CDMW_ERROR(FTLogger.GetLogger(),
               //          "Unexpected type stored in request_id_slot",
               //          new Throwable());
               //      org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
               //          ExceptionMinorCodes.INTERNALFaultToleranceError,
               //          org.omg.CORBA.CompletionStatus.COMPLETED_NO);
               //      CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
               //      throw ex;
                }
            }
            else {
                requestId = slotData.extract_ulong();
               System.out.println ("Calling remove_waiting_response for request_id= " + requestId);

                // CDMW_INTERNAL_1(FTLogger.GetLogger(),
                //     "Calling remove_waiting_response for request_id="
                //         + requestId, new Throwable());
            }

        }
        catch (org.omg.PortableInterceptor.InvalidSlot e) {
            e.printStackTrace ();
           throw new org.omg.CORBA.INTERNAL ("Invalid Slot " + e);
           // This slot that has not been allocated
           //        System.out.println ("ERROR: InvalidSlot exception raised!");
            // CDMW_ERROR(FTLogger.GetLogger(),
            //     "ERROR: InvalidSlot exception raised!", new Throwable());
            // org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
            //     ExceptionMinorCodes.INTERNALFaultToleranceError,
            //     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            // CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
            //throw e;
        }
        // catch (cdmw.common.NotFoundException e) {
        //     // The request id is not found
        //     org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
        //         ExceptionMinorCodes.INTERNALFaultToleranceError,
        //         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        //     CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
        //     throw ex;
        // }
        catch (SystemException ex) {
            // CDMW_INTERNAL_1(FTLogger.GetLogger(),
            //     "CORBA System exception raised in receive_reply!\n"
            //         + ex.getMessage(), new Throwable());
            // CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
            throw ex;
        }

    }

    /**
     * Purpose: <p> This interception point allows an Interceptor to query
     * information during a Time-Independent Invocation (TII) polling get reply
     * sequence.
     *
     * @param ri
     *            Client request information
     *
     */
    public void send_poll(ClientRequestInfo ri)
        throws org.omg.CORBA.SystemException {
    }

    /**
     * Purpose: <p> This interception point allows an Interceptor to query
     * request information and modify the service context before the request is
     * sent to the server. Here, the date of the outgoing request is stored
     * here.
     *
     * @param ri
     *            Client request information
     *
     */
    public void send_request(ClientRequestInfo ri) throws ForwardRequest,
        org.omg.CORBA.SystemException {
       System.out.println ("### TestClientRequestInterceptorImpl::send_request");

        try {
            // Get requestId
            int requestId;
            Any slotData = ri.get_slot(this.requestIdSlotId);

            TypeCode tc = slotData.type();
            System.out.println ("### TestClientRequestInterceptorImpl::send_request tc.kind " + tc.kind ().value () + " and slot id " + this.requestIdSlotId);
            if (tc.kind().value() != TCKind._tk_ulong) {

                if (tc.kind().value() == TCKind._tk_null) {
                    // the request invocation is done in the context of an
                    // incoming
                    // request processing.
                   System.out.println ("request_id_slot is not set");
                    // CDMW_INTERNAL_1(FTLogger.GetLogger(),
                    //     "request_id_slot is not set", new Throwable());
                }
                else {
                   throw new org.omg.CORBA.INTERNAL ("Unexpected type stored in request_id_slot");
                    // CDMW_ERROR(FTLogger.GetLogger(), "Unexpected type stored in request_id_slot",new Throwable());
                    // org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
                    //     ExceptionMinorCodes.INTERNALFaultToleranceError,
                    //     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                    // CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
                    // throw ex;
                }
            }
            else {
                requestId = slotData.extract_ulong();
                System.out.println ("### TestClientRequestInterceptorImpl::send_request requestid " + requestId)   ;
            }

        }
        catch (org.omg.PortableInterceptor.InvalidSlot e) {
           e.printStackTrace ();
           throw new org.omg.CORBA.INTERNAL ("Invalid Slot " + e);
            // // This slot that has not been allocated
            // CDMW_ERROR(FTLogger.GetLogger(),
            //     "ERROR: InvalidSlot exception raised!", new Throwable());
            // org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
            //     ExceptionMinorCodes.INTERNALFaultToleranceError,
            //     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            // CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
            //throw e;
        }
        // catch (cdmw.common.NotFoundException e) {
        //     // The request id is not found
        //     org.omg.CORBA.INTERNAL ex = new org.omg.CORBA.INTERNAL(
        //         ExceptionMinorCodes.INTERNALFaultToleranceError,
        //         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        //     CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
        //     throw ex;
        // }
        catch (SystemException ex) {
            // CDMW_INTERNAL_1(FTLogger.GetLogger(),
            //     "CORBA System exception raised in send_request!\n"
            //         + ex.getMessage(), new Throwable());
            // CDMW_LOG_FUNCTION_EXCEPTION(FTLogger.GetLogger(), ex);
            throw ex;
        }
    }

    /**
     * Purpose: <p> Destroy the interceptor
     *
     */
    public void destroy() throws org.omg.CORBA.SystemException {
    }

    /**
     * Purpose: <p> Return the name of the interceptor
     *
     */
    public String name() throws org.omg.CORBA.SystemException {
//            CLIENT_REQUEST_INTERCEPTOR_NAME);
        return CLIENT_REQUEST_INTERCEPTOR_NAME;
    }
}
