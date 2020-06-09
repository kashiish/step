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
import com.google.appengine.api.users.UserService;  
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.FilterOperator;

/** Servlet that decrements the number of likes for a comment in Datastore. */
@WebServlet("/unlike-comment")
public class UnlikeCommentServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long id = Long.parseLong(request.getParameter("id"));

        //get comment entity corresponding to id from datastore
        Key commentEntityKey = KeyFactory.createKey("Comment", id);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            Entity commentEntity = datastore.get(commentEntityKey);

            //calculate the number of likes using the previous number of likes
            long numLikes = (long) commentEntity.getProperty("numLikes") - 1;

            commentEntity.setProperty("numLikes", numLikes);

            datastore.put(commentEntity);

            deleteSavedLikedComment(datastore, id);

        } catch (EntityNotFoundException e)  {
            response.setContentType("text/html");
            response.getWriter().println("Entity not found.");
        }
    
    }

    /**
    * Filters out UserInfo entity from datastore that has "userId" = the id of the current user and "commentId" = commentId and deletes that entity.
    */
    private void deleteSavedLikedComment(DatastoreService datastore, long commentId) {
        UserService userService = UserServiceFactory.getUserService();

        //Only logged in users have saved liked comments
        if (!userService.isUserLoggedIn()) {
            return;
        }

        String userId = userService.getCurrentUser().getUserId();

        //create a filter
        //need to find entity that has userId of the current user and commentId of the comment the user unliked
        CompositeFilter filter = CompositeFilterOperator.and(FilterOperator.EQUAL.of("userId", userId), FilterOperator.EQUAL.of("commentId", commentId));
        Query query = new Query("Like").setFilter(filter);

        PreparedQuery pq = datastore.prepare(query);
       
        //there should only be one result
        Entity result = pq.asSingleEntity();

        //delete from datastore
        datastore.delete(result.getKey());


    }



}
