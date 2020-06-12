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
import com.google.sps.data.Comment.CommentBuilder;
import com.google.sps.utilities.InputCleaner;
import com.google.sps.utilities.CommentTranslate;
import com.google.sps.utilities.GoogleTranslate;
import com.google.sps.utilities.FakeTranslate;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.users.UserService;  
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.Detection;
import java.lang.String;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Arrays;
import java.util.Random; 

/** Servlet that returns comments from and adds comments to Datastore. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private final int MAX_COMMENTS_DEFAULT = 5;
    private final String[] sortTypes =  new String[]{"newest", "oldest", "popular"};
    private InputCleaner cleaner;
    private static CommentTranslate translator;

    public void init() {
        //change this to GoogleTranslate when deploying
        translator = new FakeTranslate();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int maxComments = getMaxCommentParam(request, response);
        String sortType = getSortTypeParam(request, response);

        Query query;

        switch(sortType) {
            case "popular":
                query = new Query("Comment").addSort("numLikes", SortDirection.DESCENDING);
                break;
            case "oldest":
                query = new Query("Comment").addSort("timestamp", SortDirection.ASCENDING);
                break;
            default:
                query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        ArrayList<Comment> comments = new ArrayList<Comment>();
        int numComments = 0;
        for (Entity entity : results.asIterable()) {
            
            Comment comment = createComment(entity, datastore);
            
            if(comment == null) {
                continue;
            }

            comments.add(comment);
            numComments++;
            
            if(numComments == maxComments) {
                break;
            }
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;");
        response.getWriter().println(convertListToJson(comments));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long timestamp = System.currentTimeMillis();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        //used to get current user's email to attach to comments
        UserService userService = UserServiceFactory.getUserService();

        // Only logged-in users can write comments
        if (!userService.isUserLoggedIn()) {
            response.sendRedirect("/comments.html");
            return;
        }

        Entity commentEntity = createCommentEntity(request, userService);
        datastore.put(commentEntity);
    
        response.sendRedirect("/comments.html");

    }

    /**
    * Creates a new Comment object with the given entity. If required fields (message, timestamp, and id) are missing, returns null.
    * @return Comment or null
    */
    private Comment createComment(Entity entity, DatastoreService datastore) {
        
        Comment comment;

        String name = (String) entity.getProperty("name");
        String message = (String) entity.getProperty("message");
        String email = (String) entity.getProperty("email");
        long timestamp = (long) entity.getProperty("timestamp");
        long numLikes = (long) entity.getProperty("numLikes");
        long id = entity.getKey().getId();
        boolean isLiked = isCommentLikedByUser(datastore, id);
        String languageCode = translator.detectLanguage(message);
        boolean isAuthor = isUserAuthorOfComment(datastore, id);

        try {
            comment = new CommentBuilder().setName(name)
                                        .setMessage(message)
                                        .setEmail(email)
                                        .setTimestamp(timestamp)
                                        .setNumLikes(numLikes)
                                        .setIsLiked(isLiked)
                                        .setLanguageCode(languageCode)
                                        .setIsAuthor(isAuthor)
                                        .setId(id).build();
        } catch (NullPointerException e) {
            System.out.println("Missing field (message, timestamp, or id) in comment.");
            comment = null;
        }

        return comment;

    }

    /**
    * Determines if the current user is the author of the comment with id. 
    * @return boolean
    */
    private boolean isUserAuthorOfComment(DatastoreService datastore, long id) {

        UserService userService = UserServiceFactory.getUserService();

        // Comments can only be submitted if a user is logged in, so a logged out user can never be the author
        if (!userService.isUserLoggedIn()) {
            return false;
        }

        String userId = userService.getCurrentUser().getUserId();

        Key commentEntityKey = KeyFactory.createKey("Comment", id);

        try {
            Entity commentEntity = datastore.get(commentEntityKey);

            //if the comment's userId matches the current user, then the current user is the author of the comment
            if(commentEntity.getProperty("userId").equals(userId)) {
                return true;
            }
            

        } catch (EntityNotFoundException e)  {
            System.out.println("Entity not found.");
        }

        return false;

    }

    /**
    * Determines if a comment is liked by the current user (if the user is logged in). 
    * @return boolean: if the user is not logged in returns false, if the user is logged in and there is a Like entity matching the userId and commentId returns true
    */
    private boolean isCommentLikedByUser(DatastoreService datastore, long commentId) {
        UserService userService = UserServiceFactory.getUserService();

        // Only save comments for logged-in users 
        if (!userService.isUserLoggedIn()) {
            return false;
        }

        String userId = userService.getCurrentUser().getUserId();

        CompositeFilter filter = CompositeFilterOperator.and(FilterOperator.EQUAL.of("userId", userId), FilterOperator.EQUAL.of("commentId", commentId));
        Query query = new Query("Like").setFilter(filter);

        PreparedQuery pq = datastore.prepare(query);
       
        //there should at most only be 1 result
        Entity result = pq.asSingleEntity();

        //if there was no entity that matched the userId and commentId, then the user has not liked the comment
        if(result == null) {
            return false;
        }

        return true;
    }

    /**
    * Creates a new Comment entity with data from the comment form and UserService. 
    * @return Entity
    */
    private Entity createCommentEntity(HttpServletRequest request, UserService userService) {
        long timestamp = System.currentTimeMillis();

        //use UTF-8 encoding to support different languages
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("encoding failed.");
        }
        
        Entity commentEntity = new Entity("Comment");
        String name = InputCleaner.clean(getParameter(request, "name").orElse("Anonymous"));
        String message = InputCleaner.clean(getParameter(request, "message").orElse(""));
        String email = userService.getCurrentUser().getEmail();
        String userId = userService.getCurrentUser().getUserId();

        commentEntity.setProperty("name", name);
        commentEntity.setProperty("message", message);
        commentEntity.setProperty("email", email);
        commentEntity.setProperty("timestamp", timestamp);
        commentEntity.setProperty("numLikes", 0);
        commentEntity.setProperty("userId", userId);

        return commentEntity;

    }

    /**
    * Gets the sort-type parameter to determine what order to display comments. 
    * @return String, if the user input was valid it returns the parameter, otherwise it returns "newest"
    */
    private String getSortTypeParam(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sortType = getParameter(request, "sort-type").orElse("newest").toLowerCase();

        boolean result = Arrays.stream(sortTypes).anyMatch(sortType::equals);
        
        if (!result) {
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
            
            //if the user selects all comments
            if(maxComments == 0) {
                maxComments = Integer.MAX_VALUE;
            }
            
            //if user input is negative or 0
            if(maxComments < 0) {
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
