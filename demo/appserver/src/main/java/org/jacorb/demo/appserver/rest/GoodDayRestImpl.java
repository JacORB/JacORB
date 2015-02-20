/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jacorb.demo.appserver.rest;

import javax.inject.Inject;
import javax.ws.rs.PathParam;

import org.jacorb.demo.appserver.ejb.CorbaService;
import org.jboss.logging.Logger;

/**
 * REST Server implementation of {@link org.jacorb.demo.appserver.rest.GoodDayRest}
 */
public class GoodDayRestImpl implements GoodDayRest
{
    private static final Logger logger = Logger.getLogger(GoodDayRestImpl.class.getName());

    @Inject
    CorbaService corbaService;

    @Override
    public String getHelloWorldJSON(@PathParam("name") String name)
    {
        logger.info("name: " + name);
        corbaService.getServer().record_string(name);
        return "{\"result\":\"" + corbaService.getServer().get_string() + "\"}";
    }

    @Override
    public String getHelloWorldJSON()
    {
        return "{\"result\":\"" + corbaService.getServer().get_string() + "\"}";
    }

    @Override
    public String getHelloWorldXML(@PathParam("name") String name)
    {
        logger.info("name: " + name);
        corbaService.getServer().record_string(name);
        return "<xml><result>" + corbaService.getServer().get_string() + "</result></xml>";
    }

    @Override
    public String getHelloWorldXML()
    {
        return "<xml><result>" + corbaService.getServer().get_string() + "</result></xml>";
    }
}
