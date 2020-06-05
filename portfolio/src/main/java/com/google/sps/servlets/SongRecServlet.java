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

import com.google.sps.data.SongRec;
import com.google.sps.utilities.InputCleaner;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.util.Iterator;
import java.lang.String;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Optional;

/** Servlet that returns song recommendations from and adds song recomendations to Datastore. */
@WebServlet("/recs")
public class SongRecServlet extends HttpServlet {

    private final long NUM_RECS_TO_LOAD = 5;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //get the number of songs loaded so far
        int numLoadedRecs = getNumLoadedParam(request, response);
        //sort by number of likes
        Query query = new Query("SongRec").addSort("numLikes", SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        ArrayList<SongRec> songRecs = new ArrayList<SongRec>();
        Iterator<Entity> iter = results.asIterator();

        //get NUM_RECS_TO_LOAD more songs
        for(int i = 0; i < numLoadedRecs + NUM_RECS_TO_LOAD; i++) {
            if(!iter.hasNext()) {
                break;
            }

            Entity entity = iter.next();

            //only add songs that have not been loaded yet
            if(i >= numLoadedRecs) {
                String name = (String) entity.getProperty("name");
                long numLikes = (long) entity.getProperty("numLikes");
                long id = entity.getKey().getId();

                SongRec rec = new SongRec(name, numLikes, id);
                songRecs.add(rec);
            }

        }
        
        response.setContentType("application/json;");
        response.getWriter().println(convertListToJson(songRecs));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Entity songRecEntity = new Entity("SongRec");
        songRecEntity.setProperty("name", InputCleaner.clean(getParameter(request, "name").orElse("")));
        songRecEntity.setProperty("numLikes", 0);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(songRecEntity);
    
        response.sendRedirect("/song-rec.html");

    }

    /**
    * Gets the num-loaded parameter to determine how many recommendations have been loaded already. 
    * @return int, if the user input was valid it returns the parameter, otherwise it returns 0
    */
    private int getNumLoadedParam(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int numLoaded;

        try {
            numLoaded =  Integer.parseInt(getParameter(request, "num-loaded").orElse("0"));
            
            //if user input is negative 
            if(numLoaded < 0) {
                response.getWriter().println("Invalid parameter.");
                numLoaded = 0;
            }

        } catch(NumberFormatException e) {
            //if user input is not a number
            response.getWriter().println("Invalid parameter.");
            numLoaded = 0;
        }

        return numLoaded;
    
    }

    /**
    * This method converts a list of songs to a JSON string.
    * @return String, the list of songs as a JSON string
    */
    private String convertListToJson(ArrayList<SongRec> songRecs) {
        ArrayList<String> jsonSongRecs = new ArrayList<String>();
        Gson gson = new Gson();
        //convert all SongRec objects to JSON
        for(SongRec songRec : songRecs) {
            jsonSongRecs.add(gson.toJson(songRec));
        }

        //convert list to JSON
        return gson.toJson(jsonSongRecs);
    }

    /**
   * @return an Optional of the request parameter
   */
    private Optional<String> getParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return Optional.ofNullable(value);
    }

}
