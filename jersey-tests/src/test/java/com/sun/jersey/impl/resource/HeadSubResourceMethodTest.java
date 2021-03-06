/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HeadSubResourceMethodTest extends AbstractResourceTester {
    
    public HeadSubResourceMethodTest(String testName) {
        super(testName);
    }

    @Path("/")
    static public class ResourceGetNoHead {
        @Path("sub")
        @GET
        public String get() {
            return "GET";
        }
    }
        
    public void testGetNoHead() {
        initiateWebApplication(ResourceGetNoHead.class);
        
        ClientResponse response = resource("/sub", false).
                head();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getType());
        assertFalse(response.hasEntity());
    }
    
    @Path("/")
    static public class ResourceGetWithHead { 
        @Path("sub")
        @HEAD
        public Response head() {
            return Response.ok().header("X-TEST", "HEAD").build();
        }
        
        @Path("sub")
        @GET
        public Response get() {
            return Response.ok("GET").header("X-TEST", "GET").build();
        }
    }
    
    public void testGetWithHead() {
        initiateWebApplication(ResourceGetWithHead.class);
        
        ClientResponse response = resource("/sub", false).
                head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals("HEAD", response.getMetadata().getFirst("X-TEST"));
    }
    
    @Path("/")
    static public class ResourceGetWithProduceNoHead { 
        @Path("sub")
        @GET
        @Produces("application/foo")
        public String getFoo() {
            return "FOO";
        }
        
        @Path("sub")
        @GET
        @Produces("application/bar")
        public String getBar() {
            return "BAR";
        }
    }
    
    public void testGetWithProduceNoHead() {
        initiateWebApplication(ResourceGetWithProduceNoHead.class);
        WebResource r = resource("/sub", false);
        
        MediaType foo = MediaType.valueOf("application/foo");
        ClientResponse response = r.accept(foo).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(foo, response.getType());
        
        MediaType bar = MediaType.valueOf("application/bar");
        response = r.accept(bar).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(bar, response.getType());
    }
    
    @Path("/")
    static public class ResourceGetWithProduceWithHead { 
        
        @Path("sub")
        @HEAD
        @Produces("application/foo")
        public Response headFoo() {
            return Response.ok().header("X-TEST", "FOO-HEAD").build();
        }
        
        @Path("sub")
        @GET
        @Produces("application/foo")
        public Response getFoo() {
            return Response.ok("GET", "application/foo").header("X-TEST", "FOO-GET").build();
        }
                
        @Path("sub")
        @HEAD
        @Produces("application/bar")
        public Response headBar() {
            return Response.ok().header("X-TEST", "BAR-HEAD").build();
        }
        
        @Path("sub")
        @GET
        @Produces("application/bar")
        public Response getBar() {
            return Response.ok("GET").header("X-TEST", "BAR-GET").build();
        }
    }
    
    public void testGetWithProduceWithHead() {
        initiateWebApplication(ResourceGetWithProduceWithHead.class);
        WebResource r = resource("/sub", false);
        
        MediaType foo = MediaType.valueOf("application/foo");
        ClientResponse response = r.accept(foo).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(foo, response.getType());
        assertEquals("FOO-HEAD", response.getMetadata().getFirst("X-TEST").toString());
        
        MediaType bar = MediaType.valueOf("application/bar");
        response = r.accept(bar).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(bar, response.getType());        
        assertEquals("BAR-HEAD", response.getMetadata().getFirst("X-TEST").toString());
    }
    
    @Path("/")
    static public class ResourceGetWithProduceNoHeadDifferentSub { 
        @Path("sub1")
        @GET
        @Produces("application/foo")
        public String getFoo() {
            return "FOO";
        }
        
        @Path("sub2")
        @GET
        @Produces("application/bar")
        public String getBar() {
            return "BAR";
        }
    }
    
    public void testGetWithProduceNoHeadDifferentSub() {
        initiateWebApplication(ResourceGetWithProduceNoHeadDifferentSub.class);
        
        MediaType foo = MediaType.valueOf("application/foo");
        ClientResponse response = resource("/sub1", false).accept(foo).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(foo, response.getType());
        
        MediaType bar = MediaType.valueOf("application/bar");
        response = resource("/sub2", false).accept(bar).head();
        assertEquals(200, response.getStatus());
        assertFalse(response.hasEntity());
        assertEquals(bar, response.getType());
    }
}
