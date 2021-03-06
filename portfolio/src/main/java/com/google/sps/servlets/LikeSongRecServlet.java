// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;


/** Servlet that increments the number of likes of a song recommendation. */
@WebServlet("/like-rec")
public class LikeSongRecServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long id = Long.parseLong(request.getParameter("id"));

        //get song entity corresponding to id from datastore
        Key songRecEntityKey = KeyFactory.createKey("SongRec", id);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity songRecEntity = datastore.get(songRecEntityKey);

            //calculate the number of likes using the previous number of likes
            long numLikes = (long) songRecEntity.getProperty("numLikes") + 1;

            songRecEntity.setProperty("numLikes", numLikes);

            datastore.put(songRecEntity);

        } catch (EntityNotFoundException e)  {
            response.setContentType("text/html");
            response.getWriter().println("Entity not found.");
        }
    
    }

}
