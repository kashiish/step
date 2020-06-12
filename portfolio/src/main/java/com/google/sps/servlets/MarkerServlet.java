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

import com.google.sps.data.Marker;
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
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import java.util.Optional;
import java.lang.String;
import com.google.gson.Gson;
import java.util.ArrayList;

/** Handles fetching and saving markers data. */
@WebServlet("/markers")
public class MarkerServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query("Marker");
        PreparedQuery results = datastore.prepare(query);

        ArrayList<Marker> markers = new ArrayList<Marker>();

        for (Entity entity : results.asIterable()) {
            
            markers.add(createMarker(entity));
            
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;");
        response.getWriter().println(convertListToJson(markers));

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //use UTF-8 encoding to support different languages
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("UTF-8 encoding is unsupported by your browser.");
        }
        
        //if the latidude is null, set the default to an invalid latitude
        double lat = Double.parseDouble(getParameter(request, "lat").orElse("100"));
        //if the longitude is null, set the default to an invalid longitude
        double lng = Double.parseDouble(getParameter(request, "lng").orElse("200"));
        String description = Jsoup.clean(getParameter(request, "description").orElse(""), Whitelist.none());

        if(lat < -90 || lat > 90) {
            response.getWriter().println("Invalid value for latitude. Must be in between -90 and 90.");
            response.sendRedirect("/map.html");
            return;
        }

        if(lng < -180  || lng > 180) {
            response.getWriter().println("Invalid value for longitude. Must be in between -180 and 180.");
            response.sendRedirect("/map.html");
            return;
        }

        Marker marker = new Marker(lat, lng, description);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity markerEntity = createMarkerEntity(marker);
        datastore.put(markerEntity);
    }

    /**
    * Creates a new Marker object with the given identity. 
    * @return Marker
    */
    private Marker createMarker(Entity entity) {

        double lat = (double) entity.getProperty("lat");
        double lng = (double) entity.getProperty("lng");
        String description = (String) entity.getProperty("description");

        Marker marker = new Marker(lat, lng, description);

        return marker;

    }

    /**
    * Creates a new Marker entity with data from a user placed marker.
    *  @return Entity
    */
    private Entity createMarkerEntity(Marker marker) {
    

        Entity markerEntity = new Entity("Marker");
        String description = InputCleaner.clean(marker.getDescription());

        markerEntity.setProperty("lat", marker.getLat());
        markerEntity.setProperty("lng", marker.getLng());
        markerEntity.setProperty("description", description);

        return markerEntity;

    }

    /**
    * This method converts a list of markers to a JSON string.
    * @return String, the list of markers as a JSON string
    */
    private String convertListToJson(ArrayList<Marker> markers) {
        ArrayList<String> jsonMarkers = new ArrayList<String>();
        Gson gson = new Gson();
        //convert all Marker objects to JSON
        for(Marker m : markers) {
            jsonMarkers.add(gson.toJson(m));
        }

        //convert list to JSON
        return gson.toJson(jsonMarkers);
    }


    /**
    * @return an Optional of the request parameter
    */
    private Optional<String> getParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return Optional.ofNullable(value);
    }

}
