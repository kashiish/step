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

var map;

var openMarker;
var openWindow;

function initMap() {
    //create map
    map = new google.maps.Map(document.getElementById("map"), {
        center: { lat: 37.464528, lng: -81.020985},
        zoom: 3
    });

    loadMarkers();

    map.addListener("click", (event) => {
        createNewMarkerByUser(event.latLng.lat(), event.latLng.lng());
    });

}

//Creates a new marker at the location the user has clicked and opens an info window with a textbox and submit button 
//so the user can submit a description.
function createNewMarkerByUser(lat, lng) {

    //If a user already started editing another marker, remove it
    if(openMarker) {
        openMarker.setMap(null);
    }

    //If the user has an open info window, close that before creating a new marker
    if(openWindow) {
        openWindow.close();
    }

    openMarker = new google.maps.Marker({position: {lat: lat, lng: lng}, map: map});

    var descriptionInputForm = createInputForm(lat, lng);

    var infoWindow = new google.maps.InfoWindow({content: descriptionInputForm});

    // When the user closes the open info window, remove the marker.
    google.maps.event.addListener(infoWindow, "closeclick", () => {
        openMarker.setMap(null);
    });

    infoWindow.open(map, openMarker);
}

//Makes a POST request to the /markers servlet to store a marker
function submitMarker(lat, lng, description) {
    const params = new URLSearchParams();
    params.append("lat", lat);
    params.append("lng", lng);
    params.append("description", description);

    fetch("/markers", {method: "POST", body: params});

}

//Creates a div element with a form containing a textbox and submit button for a user placed marker.
//@return div element
function createInputForm(lat, lng) {
    var container = document.createElement("div");
    var label = document.createElement("p");
    var form = document.createElement("form");
    var textBox = document.createElement("textarea");
    var submitButton = document.createElement("button");

    label.innerHTML = "Description:";
    submitButton.innerHTML = "Submit";

    container.classList.add("marker-form-container");
    label.classList.add("marker-form-label");
    form.classList.add("marker-form");
    textBox.classList.add("marker-textbox");
    submitButton.classList.add("marker-submit");
    submitButton.classList.add("btn-small");

    //require the textbox field
    textBox.setAttribute("required", "");
    textBox.setAttribute("aria-required", "true");
    submitButton.setAttribute("type", "submit");

    form.addEventListener("submit", () => {
        submitMarker(lat, lng, textBox.value);
        createMarker({lat: lat, lng: lng, description: textBox.value});
        //the user has submitted the marker, so there's no longer an open marker
        openMarker.setMap(null);
    })

    form.append(textBox);
    form.append(submitButton);
    container.append(label);
    container.append(form);

    return container;
}

//Loads all stored markers from the server and creates markers to display on the map.
function loadMarkers() {
    fetch("/markers").then(response => response.json()).then((markers) => {
        markers.forEach((marker) => {
            //convert data to JSON
            createMarker(JSON.parse(marker));
        });
    });
}

//Creates a marker with an info window displaying the user's description.
function createMarker(markerData) {
    var marker = new google.maps.Marker({position: {lat: markerData.lat, lng: markerData.lng}, map: map});

    var infoWindow = new google.maps.InfoWindow({content: markerData.description});

    marker.addListener("click", () => {
        //If the user has an open info window, close that before opening a new one
        if(openWindow) {
            openWindow.close();
        }

        //If a user already started editing another marker, remove it
        if(openMarker) {
            openMarker.setMap(null);
        }

        infoWindow.open(map, marker);

        openWindow = infoWindow;
    });
}
