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
function sendEmail() {
    var sender = document.getElementById("icon-name").value;
    window.open("mailto:kashisharora@google.com?subject=Hello from " + sender + "!&body=" + document.getElementById("icon-message").value);
}

//Requests content from DataServlet and adds it to the page.
function sayHello() {
    fetch('/data').then(response => response.json()).then((hello) => {
    console.log(hello);
    document.getElementById('message-container').innerText = hello;
  });
}