package org.jacorb.demo.appserver.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jacorb.demo.appserver.ejb.CorbaService;

/**
 * <p>
 * The servlet is registered and mapped to /PrintIORServlet using the {@linkplain javax.servlet.http.HttpServlet}.
 * The {@link org.jacorb.demo.appserver.ejb.CorbaService} is injected by EJB.
 * </p>
 */
@SuppressWarnings("serial")
@WebServlet("/PrintIOR")
public class PrintIORServlet extends HttpServlet
{

    static String PAGE_HEADER = "<html><head><title>PrintIOR</title></head><body>";

    static String PAGE_FOOTER = "</body></html>";

    /*  @EJB
        CorbaService corbaService;
    */
    @Inject
    CorbaService corbaService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.println(PAGE_HEADER);
        writer.println("<h1>" + corbaService.getIOR() + "</h1>");
        writer.println(PAGE_FOOTER);
        writer.close();
    }
}
