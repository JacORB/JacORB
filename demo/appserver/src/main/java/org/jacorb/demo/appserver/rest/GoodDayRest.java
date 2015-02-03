package org.jacorb.demo.appserver.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST Server
 * /rest/.. path defined in {@link org.jacorb.demo.appserver.rest.GoodDayApplication}
 */
@Path("")
public interface GoodDayRest
{
    @POST
    @Path("/json-update/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    String getHelloWorldJSON(@PathParam("name") String name);

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    String getHelloWorldJSON();

    @GET
    @Path("/xml-update/{name}")
    @Produces(MediaType.APPLICATION_XML)
    String getHelloWorldXML(@PathParam("name") String name);

    @GET
    @Path("/xml")
    @Produces(MediaType.APPLICATION_XML)
    String getHelloWorldXML();
}
