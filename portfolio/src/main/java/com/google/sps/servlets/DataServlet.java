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
import java.lang.String;
import com.google.gson.Gson;
import java.util.ArrayList;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private ArrayList<Comment> comments;

    @Override
    public void init() {
        comments = new ArrayList<Comment>();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;");
        response.getWriter().println(convertListToJson(comments));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Comment comment = new Comment(getParameter(request, "name", ""), getParameter(request, "email", ""), getParameter(request, "message", ""));

        // comments.add(comment);

        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("name", comment.getName());
        commentEntity.setProperty("email", comment.getEmail());
        commentEntity.setProperty("message", comment.getMessage());


        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);

    
        response.sendRedirect("/index.html");

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
