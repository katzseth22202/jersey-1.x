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

package com.sun.jersey.samples.atomserver;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Naresh
 */
public class SimpleAtomServerTest {

    private HttpServer httpServer;

    private WebResource r;

    @Before
    public void setUp() throws Exception {
        httpServer = Server.startServer();

        Client c = Client.create();
        r = c.resource(Server.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        httpServer.stop();
    }

    /**
     * Test if a WADL document is available at the relative path
     * "application.wadl".
     */
    @Test
    public void testApplicationWadl() {
        String serviceWadl = r.path("application.wadl").
                accept(MediaTypes.WADL).get(String.class);
        assertTrue("Something wrong. Returned wadl length not > 0.", serviceWadl.length() > 0);
    }

    /**
     * Test checks that expected response is seen for a request to the resource
     * "service".
     */
    @Test
    public void testServiceResource() {
        String serviceAtomXml = r.path("service").accept("application/atomserv+xml")
                .get(String.class);
        assertTrue("Returned Atom Server XML length not > 0.", serviceAtomXml.length() > 0);
    }

    /**
     * Test checks that POST, GET, PUT and DELETE work as expected in the
     * collection resource and its sub-resources.
     */
    @Test
    public void testCollectionResource() {
        //create a media link entry
        String pushText = "Something is rotten in the state of Denmark";
        String updatedText = "Hamlet said: Something is rotten in the state of Denmark";
        ClientResponse response = r.path("collection").type(MediaType.TEXT_PLAIN)
                .post(ClientResponse.class, pushText);
        assertEquals("Response status doesn't match the expected status",
                Response.Status.CREATED, response.getResponseStatus());

        //get the ATOM feed
        Feed feed = r.path("collection").accept("application/atom+xml").get(Feed.class);
        int numberOfEntries = feed.getEntries().size();
        List<Entry> feedEntries = feed.getEntries();
        Iterator<Entry> entryIterator = feedEntries.iterator();
        Entry feedEntry = null;
        if(entryIterator.hasNext()) {
          feedEntry = entryIterator.next();
        }
        String entryId = feedEntry.getId();

        //get the entry content
        String entryText = r.path("collection").path(entryId).path("media")
                .accept(MediaType.TEXT_PLAIN).get(String.class);
        assertEquals("Retrieved entry doesn't have the pushed text.",
                pushText, entryText);

        //update the entry
        r.path("collection").path("edit").path(entryId).path("media")
                .type(MediaType.TEXT_PLAIN).put(updatedText);
        entryText = r.path("collection").path(entryId).path("media")
                .accept(MediaType.TEXT_PLAIN).get(String.class);
        assertEquals("Update not reflected in the retrieved entry content.",
                updatedText, entryText);

        // delete the entry
        r.path("collection").path("edit").path(entryId).delete();
        feed = r.path("collection").accept("application/atom+xml").get(Feed.class);
        assertEquals("Looks like the entry didn't get deleted.",
                numberOfEntries - 1, feed.getEntries().size());
    }

}