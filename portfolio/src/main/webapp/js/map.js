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

function initMap() {
    //create map
    map = new google.maps.Map(document.getElementById("map"), {
        center: { lat: 37.464528, lng: -81.020985},
        zoom: 3
    });

    loadMarkers();

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

//Creates a marker with an InfoWindow displaying the user's description.
function createMarker(markerData) {
    var marker = new google.maps.Marker({position: {lat: markerData.lat, lng: markerData.lng}, map: map});

    var infoWindow = new google.maps.InfoWindow({content: markerData.description});

    marker.addListener("click", () => {
        infoWindow.open(map, marker);
    });
}
