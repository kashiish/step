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

import com.google.sps.data.Comment;
import java.io.IOException;
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
import java.lang.String;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Optional;

/** Servlet that returns comments from and adds comments to Datastore. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private final int MAX_COMMENTS_DEFAULT = 5;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int maxComments = getMaxCommentParam(request, response);
        String sortType = getSortTypeParam(request, response);

        Query query;
        if(sortType.equals("newest")) {
            query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
        } else if(sortType.equals("oldest")) {
            query = new Query("Comment").addSort("timestamp", SortDirection.ASCENDING);
        } else {
            query = new Query("Comment").addSort("numLikes", SortDirection.DESCENDING);
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        ArrayList<Comment> comments = new ArrayList<Comment>();
        int numComments = 0;
        for (Entity entity : results.asIterable()) {
            String name = (String) entity.getProperty("name");
            String message = (String) entity.getProperty("message");
            long timestamp = (long) entity.getProperty("timestamp");
            long numLikes = (long) entity.getProperty("numLikes");
            long id = entity.getKey().getId();

            Comment comment = new Comment(name, message, timestamp, numLikes, id);
            comments.add(comment);
            numComments++;
            
            if(numComments == maxComments) {
                break;
            }
        }
        
        response.setContentType("application/json;");
        response.getWriter().println(convertListToJson(comments));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long timestamp = System.currentTimeMillis();

        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("name", getParameter(request, "name").orElse("Anonymous"));
        commentEntity.setProperty("message", getParameter(request, "message").orElse(""));
        commentEntity.setProperty("timestamp", timestamp);
        commentEntity.setProperty("numLikes", 0);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);
    
        response.sendRedirect("/comments.html");

    }

    /**
    * Gets the sort-type parameter to determine what order to display comments. 
    * @return String, if the user input was valid it returns the parameter, otherwise it returns "newest"
    */
    private String getSortTypeParam(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sortType = getParameter(request, "sort-type").orElse("newest").toLowerCase();

        if(!sortType.equals("newest") && !sortType.equals("oldest") && !sortType.equals("popular")) {
            response.getWriter().println("Invalid value for sort-type.");
            sortType = "newest";
        }

        return sortType;
        
    }

    /**
    * Gets the max-comments parameter to determine how many comments to fetch from Datastore. 
    * @return int, if the user input was valid it returns the parameter, otherwise it returns the MAX_COMMENT_DEFAULT
    */
    private int getMaxCommentParam(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int maxComments;

        try {
            maxComments = Integer.parseInt(getParameter(request, "max-comments").orElse(Integer.toString(MAX_COMMENTS_DEFAULT)));
            
            //if user input is negative or 0
            if(maxComments <= 0) {
                response.getWriter().println("Invalid value for max-comments.");
                maxComments = MAX_COMMENTS_DEFAULT;
            }

        } catch(NumberFormatException e) {
            //if user input is not a number
            response.getWriter().println("Invalid value for max-comments.");
            maxComments = MAX_COMMENTS_DEFAULT;
        }

        return maxComments;
    
    }

    /**
    * This method converts a list of comments to a JSON string.
    * @return String, the list of comments as a JSON string
    */
    private String convertListToJson(ArrayList<Comment> comments) {
        ArrayList<String> jsonComments = new ArrayList<String>();
        Gson gson = new Gson();
        //convert all Comment objects to JSON
        for(Comment c : comments) {
            jsonComments.add(gson.toJson(c));
        }

        //convert list to JSON
        return gson.toJson(jsonComments);
    }

    /**
   * @return an Optional of the request parameter
   */
    private Optional<String> getParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return Optional.ofNullable(value);
    }

}
