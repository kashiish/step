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

//prevent the page from refreshing when contact form is submitted
var form = document.getElementById("contact-form")
form.addEventListener('submit', (e) => e.preventDefault());

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

//Called when user submits contact form.
function submitContactForm() {
    //If the user already submitted the contact form, remove the response message (email successfully sent or not)
    clearSubmissionMessage();

    const params = new URLSearchParams();
    params.append('name', contactForm.name.value);
    params.append('email', contactForm.email.value);
    params.append('message', contactForm.message.value);

    fetch('/send-email', {method: 'POST', body: params}).then(response => {
        var contactFormContainer = document.getElementById("contact-form-container");
        var resultMessage = document.createElement("p");
        //if email was sent
        if(response.ok) {
            resultMessage.innerText = "Email sent!";
        //there was an error
        } else {
            resultMessage.innerText = "Sorry, there was a problem sending the email.";
        }

        contactFormContainer.appendChild(resultMessage);
        //clear the contact form since not refreshing page
        contactForm.reset(); 
    });
}

//Clears the message under the contact form if the user previously submitted the form
function clearSubmissionMessage() {
    var contactFormContainer = document.getElementById("contact-form-container");
    var submissionMessage = contactFormContainer.querySelector("p");

    if(submissionMessage !== null) {
        contactFormContainer.removeChild(submissionMessage);
    }
}
