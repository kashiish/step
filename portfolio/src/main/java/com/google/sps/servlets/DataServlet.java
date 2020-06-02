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

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private final int MAX_COMMENTS_DEFAULT = 5;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int maxComments = getMaxCommentParam(request, response);
        Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        ArrayList<Comment> comments = new ArrayList<Comment>();
        int numComments = 0;
        for (Entity entity : results.asIterable()) {
            String name = (String) entity.getProperty("name");
            String email = (String) entity.getProperty("email");
            String message = (String) entity.getProperty("message");
            long timestamp = (long) entity.getProperty("timestamp");

            Comment comment = new Comment(name, email, message, timestamp);
            comments.add(comment);
            numComments++;
            
            if(numComments == maxComments) break;
        }
        
        response.setContentType("application/json;");
        response.getWriter().println(convertListToJson(comments));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long timestamp = System.currentTimeMillis();

        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("name", getParameter(request, "name", ""));
        commentEntity.setProperty("email", getParameter(request, "email", ""));
        commentEntity.setProperty("message", getParameter(request, "message", ""));
        commentEntity.setProperty("timestamp", timestamp);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);
    
        response.sendRedirect("/index.html");

    }

    /**
    * Gets the max-comments parameter to determine how many comments to fetch from Datastore. 
    * @return int, if the user input was valid it returns the parameter, otherwise it returns the MAX_COMMENT_DEFAULT
    */
    private int getMaxCommentParam(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int maxComments;

        try {
            maxComments = Integer.parseInt(getParameter(request, "max-comments", "5"));
            
            //if user input is negative or 0
            if(maxComments <= 0) {
                response.getWriter().println("Invalid parameter.");
                maxComments = MAX_COMMENTS_DEFAULT;
            }

        } catch(NumberFormatException e) {
            //if user input is not a number
            response.getWriter().println("Invalid parameter.");
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
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
    private String getParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        if (value == null) {
        return defaultValue;
        }
        return value;
    }

}
