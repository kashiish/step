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

//Requests song recommendations from SongRecServlet and adds it to the page.
function loadSongRecs() {
    fetch('/recs?num-loaded=0').then(response => response.json()).then((recs) => {
    var recContainer = document.getElementById('rec-container');
    //create a div element for each of the recommendations in the recs array
    var recElems = recs.map(createRecElem);
    //append each recElem to recContainer
    recElems.forEach(function(elem) {
        recContainer.appendChild(elem);
    });

    //append load more button
    document.getElementById('song-rec-form').appendChild(createLoadMoreRecsButton());
  });
}

//Creates a load more song recs button. When the user clicks this, more songs are fetched from the server and added to the page.
function createLoadMoreRecsButton() {
    var button = document.createElement("button");
    button.innerText = "load more";
    
    button.id = "load-more-button";

    button.addEventListener("click", () => {
        loadMoreSongRecs();
        
    });


    return button;
}

//Fetches more song recommendations from the server and adds them to the page. If there are no more song recommendations, 
//it adds a message to the page indicating that there are no more.
function loadMoreSongRecs() {
    fetch('/recs?num-loaded='+getNumLoadedRecs()).then(response => response.json()).then((recs) => {

        var recContainer = document.getElementById('rec-container');

        //if there are no more recommendations
        if(recs.length === 0){
            var noMoreRecs = document.createElement("p");
            noMoreRecs.innerText = "Looks like there are no more!";

            recContainer.append(noMoreRecs);
            //remove load more button
            document.getElementById("load-more-button").style.display = "none";
            return;
        }

        //create a div element for each of the recommendations in the recs array
        var recElems = recs.map(createRecElem);
        //append each recElem to recContainer
        recElems.forEach(function(elem) {
            recContainer.appendChild(elem);
        });

    });
}

//Gets the number of song recommendations already on the page.
function getNumLoadedRecs() {
    var recContainer = document.getElementById("rec-container");
    return recContainer.childElementCount;
}

//Creates a div element for a song recommendation JSON object. Each div contains an element for the name of the song
//recommendation and a plus one button to like a song recommendation.
function createRecElem(rec) {

    var jsonRec = JSON.parse(rec);

    var container = document.createElement("div");
    var recName = document.createElement("p");
    var plusOneButton = document.createElement("button");

    recName.innerText = jsonRec.name;
    plusOneButton.innerHTML = "<i class='material-icons plus-one'>exposure_plus_1</i>";

    container.classList.add("song-rec");
    plusOneButton.classList.add("plus-one-button");

    plusOneButton.addEventListener("click", () => {
        likeSong(jsonRec);

        var plusOneIcon = plusOneButton.querySelector("i");
        plusOneIcon.classList.add("press");
        //once the recommendation is liked, disable the button (can't undo plus one)
        plusOneButton.disabled = true;
    });

    container.appendChild(recName);
    container.appendChild(plusOneButton);

    return container;

}

//Tells the serer to like a song recommendation.
function likeSong(rec) {
    const params = new URLSearchParams();
    params.append('id', rec.id);
    fetch('/like-rec', {method: 'POST', body: params});
}
