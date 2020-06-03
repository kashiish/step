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

//Initialize parallax (materialize)
document.addEventListener('DOMContentLoaded', function() {
    var elems = document.querySelectorAll('.parallax');
    var instances = M.Parallax.init(elems, 0);
});

//Initialize side navigation (materialize)
document.addEventListener('DOMContentLoaded', function() {
    var elems = document.querySelectorAll('.sidenav');
    var instances = M.Sidenav.init(elems);
});

document.addEventListener('DOMContentLoaded', function() {
    var elems = document.querySelectorAll('select');
    var instances = M.FormSelect.init(elems);
});

//Switches the project description and image to the project that was clicked on. 
function switchProject(elem) {
    //get the name of the project that was clicked
    var project = elem.innerText;
    //get the current project that we see
    var currentActiveProj = document.getElementsByClassName("active-proj")[0];
    var currentActiveProjLink = document.getElementsByClassName("active")[0];
    //remove the active/active-proj classes from the project that we currently see
    //the active/active-proj classes determine whether or not we can see the element
    currentActiveProj.classList.remove("active-proj");
    currentActiveProjLink.classList.remove("active");
    //get the project description/image that we clicked on
    var nextActiveProj = document.getElementById(project);
    //add the active/active-proj classes to the project we clicked so we can see the description/image
    elem.classList.add("active");
    nextActiveProj.classList.add("active-proj");
}

//TEMPORARY
//This function is called when a user submits the contact form. It opens the user's mail client
//with my email in the 'to' field, a subject, and the message they filled in the form. 
//Referenced from: https://stackoverflow.com/questions/7381150/how-to-send-an-email-from-javascript
//After we create a server in week 3, I hope to use that to send an email from the user instead of opening the user's mail client
// function sendEmail() {
//     var sender = document.getElementById("icon-name").value;
//     window.open("mailto:kashisharora@google.com?subject=Hello from " + sender + "!&body=" + document.getElementById("icon-message").value);
// }

//Requests comments from DataServlet and adds it to the page.
function loadComments() {
    fetch('/data?max-comments='+getSelectedMaxComments()).then(response => response.json()).then((comments) => {
    var commentContainer = document.getElementById('comment-container');
    commentContainer.innerHTML = "";
    //create a div element for each of the commments in the comments array
    var commentElems = comments.map(createCommentElem);
    //append each commentElem to commentContainer
    commentElems.forEach(function(elem) {
        commentContainer.appendChild(elem);
    });
  });
}

//Returns the selected option in the "Number of comments" select form.
function getSelectedMaxComments() {
    var select = document.getElementById("max-comments");
    return select.options[select.selectedIndex].value;
}

// Creates a div element for a comment with a name, email, date, and message
// @return div element
function createCommentElem(comment) {
    //convert string to JSON
    var jsonComment = JSON.parse(comment);

    var commentElem = document.createElement("div");
    var name = document.createElement("h6");
    var email = document.createElement("p");
    var date = document.createElement("p")
    var message = document.createElement("p");
    var deleteButton = document.createElement('button');
    var smile = createSmileButton(jsonComment);

    name.innerText = jsonComment.name;
    email.innerText = jsonComment.email;
    date.innerText = convertTime(jsonComment.timestamp);
    message.innerText = jsonComment.message;
    deleteButton.innerHTML = "<i class='material-icons black-icon'>delete</i>";
    
    commentElem.classList.add("comment");
    name.classList.add("comment-name");
    email.classList.add("comment-email");
    date.classList.add("comment-date");
    message.classList.add("comment-message");
    deleteButton.classList.add("delete-comment")


    deleteButton.addEventListener("click", () => {
        deleteComment(jsonComment);

        // Remove the task from the DOM.
        commentElem.remove();
  });

    commentElem.appendChild(name);
    commentElem.appendChild(email);
    commentElem.appendChild(date);
    commentElem.appendChild(message);
    commentElem.appendChild(deleteButton);
    commentElem.appendChild(smile);

    return commentElem;
}

//Creates the smile (like) button for each comment. 
//@return div element container a button and a label which represents the number of likes the comment has
function createSmileButton(comment) {

    var container = document.createElement("div");
    var button = document.createElement("button");
    var numLikesLabel = document.createElement("span");

    numLikesLabel.innerText = comment.numLikes;
    button.innerHTML = "<i class='material-icons smile'>emoji_emotions</i>";

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
    params.append('id', comment.id);
    fetch('/delete-data', {method: 'POST', body: params});
}

//Tells the server to like the comment (increment numLikes)
function likeComment(comment) {
    const params = new URLSearchParams();
    params.append('id', comment.id);
    fetch('/like-comment', {method: 'POST', body: params});
}

//Tells the server to unlike the comment (decrenebt numLikes)
function unlikeComment(comment) {
    const params = new URLSearchParams();
    params.append('id', comment.id);
    fetch('/unlike-comment', {method: 'POST', body: params});
}
