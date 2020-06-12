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

//Initialize select form (materialize)
document.addEventListener("DOMContentLoaded", function() {
    var elems = document.querySelectorAll("select");
    var instances = M.FormSelect.init(elems);
});

//Checks if the user is logged in. If so, it displays the comment form and creates a logout button.
//If not, it adds a message asking the user to login with a login button.
function checkLogin() {
    fetch("/login").then(response => response.json()).then((status) => {
        commentSection = document.getElementById("comment-section");
        if(status.loggedIn) {
            commentSection.insertBefore(createButtonWithLink("Logout", status.url), commentSection.firstChild);
            document.getElementById("comment-form").style.display = "block";
        } else {
            var container = document.createElement("div");
            var message = document.createElement("p");

            message.innerText = "Please login to write comments.";

            container.appendChild(message);
            container.appendChild(createButtonWithLink("Login", status.url));

            container.setAttribute("id", "login-message");

            commentSection.insertBefore(container, document.getElementById("comment-form"));

        }
    });
}

//Creates an anchor tag with button styling. The text and link of the button is determined by the parameters buttonText and url.
function createButtonWithLink(buttonText, url) {
    var button = document.createElement("a");
    button.innerText = buttonText;
    button.classList.add("waves-effect");
    button.classList.add("waves-light");
    button.classList.add("btn-small");
    //add an ID to the button that contains the buttonText in lowercase
    button.setAttribute("id", buttonText.toLowerCase() + "-button");
    button.setAttribute("href", url);

    return button;
}

//Requests comments from DataServlet and adds it to the page.
function loadComments() {
    fetch("/data?max-comments="+getSelection("max-comments")+"&sort-type="+getSelection("sort-type")).then(response => response.json()).then((comments) => {
        var commentContainer = document.getElementById("comment-container");
        commentContainer.innerHTML = "";
        //if there are no comments
        if(comments.length == 0 ) {
            commentContainer.innerText = "Nothing to see here.";
            return;
        }
        //create a div element for each of the commments in the comments array
        var commentElems = comments.map(createCommentElem);
        //append each commentElem to commentContainer
        commentElems.forEach(function(elem) {
            commentContainer.appendChild(elem);
        });
    });
}

//Returns the selected option in select form with id parameter.
function getSelection(id) {
    var select = document.getElementById(id);
    return select.options[select.selectedIndex].value;
}

// Creates a div element for a comment with a name, date, and message
// @return div element
function createCommentElem(comment) {
    //convert string to JSON
    var jsonComment = JSON.parse(comment);

    var commentElem = document.createElement("div");
    var name = document.createElement("h6");
    var email = document.createElement("p");
    var date = document.createElement("p");
    var message = document.createElement("p");
    var smile = createSmileButton(jsonComment);

    name.innerHTML = jsonComment.name;
    email.innerHTML = jsonComment.email;
    date.innerHTML = convertTime(jsonComment.timestamp);
    message.innerHTML = jsonComment.message;
    
    commentElem.classList.add("comment");
    name.classList.add("comment-name");
    email.classList.add("comment-email");
    date.classList.add("comment-date");
    message.classList.add("comment-message");

    commentElem.appendChild(name);
    commentElem.appendChild(email);
    commentElem.appendChild(date);
    commentElem.appendChild(message);


    //if the comment is not in the language of where the user is, create a translate button
    var userLanguageCode = navigator.language.split('-')[0];
    if(jsonComment.languageCode !== userLanguageCode) {
        var translateButton = createTranslateButton(message, jsonComment.message, userLanguageCode);
        commentElem.appendChild(translateButton);
        
    }

    //if the user is the author of the comment, add a delete button
    if(jsonComment.isAuthor) {
         var deleteButton = createDeleteButton(jsonComment, commentElem);
         commentElem.appendChild(deleteButton);
    }

    commentElem.appendChild(smile);

    return commentElem;
}

//Creates a Translate button for a comment. When the message is not translated, the button reads "Translate" and when it's clicked, it translates the message.
//If the message is translated, the button reads "Original message" and when it's clicked, the original message is displayed instead.
//@return button element
function createTranslateButton(messageElem, originalMessage, languageCode) {
    var button = document.createElement("button");
    button.innerText = "Translate";
    button.classList.add("translate-comment");

    button.addEventListener("click", () => {

        if(button.classList.contains("translated")) {

            messageElem.innerText = originalMessage;
            button.innerText = "Translate";
            button.classList.remove("translated");

        } else {

            const params = new URLSearchParams();
            params.append("message", originalMessage);
            params.append("languageCode", languageCode);
            fetch("/translate", {method: "POST", body: params}).then(response => response.text()).then(translation => {
                messageElem.innerText = translation;
                button.innerText = "Original message";
                button.classList.add("translated");
            });
        }

    });

    return button;
}


//Creates the delete button for each comment
//@return button element with an icon
function createDeleteButton(comment, commentElem) {
    var button = document.createElement("button");
    button.innerHTML = "<i class='material-icons black-icon'>delete</i>";
    button.classList.add("delete-comment");

    button.addEventListener("click", () => {
        deleteComment(comment);
        // Remove the task from the DOM.
        commentElem.remove();

        loadComments();
     });

     return button;

}

//Creates the smile (like) button for each comment. 
//@return div element container a button and a label which represents the number of likes the comment has
function createSmileButton(comment) {

    var container = document.createElement("div");
    var button = document.createElement("button");
    var numLikesLabel = document.createElement("span");

    numLikesLabel.innerText = comment.numLikes;
    //add "press" class to icon if comment is liked
    button.innerHTML = "<i class='material-icons smile " + (comment.isLiked ? "press" : "") + "'>emoji_emotions</i>";

    container.classList.add("smile-container");
    button.classList.add("smile-button");
    numLikesLabel.classList.add("num-likes");

    button.addEventListener("click", () => {
        var smileIcon = button.querySelector("i");

        //unliking a liked comment
        if(smileIcon.classList.contains("press")) {
            //update on frontend so we don't have to refresh the page
            numLikesLabel.innerText = parseInt(numLikesLabel.innerText) - 1;
            //update backend
            unlikeComment(comment);

            smileIcon.classList.remove("press");
        //liking
        } else {
            //update on front end so we don't have to refresh the page
            numLikesLabel.innerText = parseInt(numLikesLabel.innerText) + 1;
            //update backend
            likeComment(comment);

            smileIcon.classList.add("press");
        }
    });

    container.appendChild(button);
    container.appendChild(numLikesLabel);

    return container;

}

//This function converts a timestamp (in milliseconds) to a date string in the form MM/DD/YYYY
function convertTime(timestamp) {
    //create a new date object
    var date = new Date(timestamp);
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var year = date.getFullYear();

    return month + "/" + day + "/" + year;
}

//Tells the server to delete the comment.
function deleteComment(comment) {
    const params = new URLSearchParams();
    params.append("id", comment.id);
    fetch("/delete-data", {method: "POST", body: params});
}

//Tells the server to like the comment (increment numLikes)
function likeComment(comment) {
    const params = new URLSearchParams();
    params.append("id", comment.id);
    fetch("/like-comment", {method: "POST", body: params});
}

//Tells the server to unlike the comment (decrement numLikes)
function unlikeComment(comment) {
    const params = new URLSearchParams();
    params.append("id", comment.id);
    fetch("/unlike-comment", {method: "POST", body: params});
}
