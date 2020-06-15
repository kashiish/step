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
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

/** Servlet that deletes a user's like from datastore. */
@WebServlet("/unlike-comment")
public class UnlikeCommentServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long commentId = Long.parseLong(request.getParameter("id"));

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastore.beginTransaction();

        try {
            
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
            datastore.delete(txn, result.getKey());

            txn.commit();

        } finally {
          if (txn.isActive()) {
            txn.rollback();
          }
        }
    
    }

}
