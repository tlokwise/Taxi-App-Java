const apiKey = "AIzaSyC_xPz1x1gIoH33Cq-Yat-j-tQvpZI5B_A";
let mapContainer = document.querySelector("#map");
let submitBtn = document.querySelector("#submit-btn");
let locationInput = document.querySelector("#location-input");
let destinationInput = document.querySelector("#destination-input");
let popup = document.querySelector(".popup");
let map;
let userCoords;
let userLocation;
let locationObject;
let destinationObject;

function initMap() {
  const mapCenter = { lat: -26.04873, lng: 28.15978 }; // South Africa coordinates (center)

  const map = new google.maps.Map(document.getElementById("map"), {
    center: mapCenter,
    zoom: 15,
    disableDefaultUI: true, // Disable all default UI controls
  });

  //FROM AUTO COMPLETE
  const locationAutocomplete = new google.maps.places.Autocomplete(
    locationInput
  );

  // Restrict the search to a specific region
  // restrict to the south africa
  locationAutocomplete.setComponentRestrictions({ country: "za" });

  // Bias the results towards the current map's viewport
  locationAutocomplete.bindTo("bounds", map);

  //event listener for when a place is selected
  locationAutocomplete.addListener("place_changed", function () {
    const place = locationAutocomplete.getPlace();

    console.log("Place Name: " + place.name);
    console.log("Place Address: " + place.formatted_address);
    console.log("Place ID: " + place.place_id);
    console.log("Location: " + place.geometry.location);

    locationObject = {
      name: place.name,
      address: place.formatted_address,
      placeId: place.place_id,
      latitude: place.geometry.location.lat(),
      longitude: place.geometry.location.lng(),
    };
  });

  //TO AUTO COMPLETE
  const destinationAutocomplete = new google.maps.places.Autocomplete(
    destinationInput
  );

  // Restrict the search to a specific region
  // restrict to the south africa
  destinationAutocomplete.setComponentRestrictions({ country: "za" });

  // Bias the results towards the current map's viewport
  destinationAutocomplete.bindTo("bounds", map);

  // Add an event listener for when a place is selected
  destinationAutocomplete.addListener("place_changed", function () {
    const place = destinationAutocomplete.getPlace();

    console.log("Place Name: " + place.name);
    console.log("Place Address: " + place.formatted_address);
    console.log("Place ID: " + place.place_id);
    console.log("Location: " + place.geometry.location);

    destinationObject = {
      name: place.name,
      address: place.formatted_address,
      placeId: place.place_id,
      latitude: place.geometry.location.lat(),
      longitude: place.geometry.location.lng(),
    };
  });
}

submitBtn.addEventListener("click", (e) => {
  e.preventDefault();
  const data = {
    location: locationObject,
    destination: destinationObject,
  };

  const dataToSend = JSON.stringify(data);

  fetch("../DataProcessor", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    body: "data=" + encodeURIComponent(dataToSend),
  })
    .then((response) => {
      if (response.ok) {
        return response.json();
      }
    })
    .then((responseData) => {
      processReponse(responseData, data.destination);
    })
    .catch((error) => {
      console.error(error);
    });
});

function processReponse(responseData, destinationObj) {
  let destinationName = destinationObj.name;

  let locationName = responseData.closestLocation.name;
  let locationLatitude = responseData.closestLocation.latitude;
  let locationLongitude = responseData.closestLocation.longitude;
  let price = responseData.price;

  new google.maps.Marker({
    positition: { lat: locationLatitude, lng: locationLongitude },
    map: map,
    title: locationName,
  });
  //map.setCenter({lat: locationLatitude, lng: locationLongitude}, 18)
  popup.innerHTML = `
  <h2>Marshal</h2>
  <table>
    <tr>
      <td>Location: </td>
      <td>${locationName}</td>
    </tr>
    <tr>
      <td>Destination: </td>
      <td>${destinationName}</td>
    </tr>
    <tr>
      <td>Price: </td>
      <td>${price}</td>
    </tr>
    <tr>
    	<button type="button" class="refactor-btn">Refactor</button>
    </tr>
  </table>
  `;
  popup.style.display = "block";
  console.log(destinationName);
  console.log(locationName);
}

//
