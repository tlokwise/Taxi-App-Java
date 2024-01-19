window.addEventListener("load", () => {
  if ("geolocation" in navigator) {
    navigator.geolocation.getCurrentPosition(
      //ON SUCCESS
      function (position) {
        const latitude = position.coords.latitude;
        const longitude = position.coords.longitude;
        userCoords = { lat: latitude, lng: longitude };
        //CONVERT COORDINATES INTO ADDRESS
        let url = `https://maps.googleapis.com/maps/api/geocode/json?latlng=${userCoords.lat},${userCoords.lng}&key=${apiKey}`;
        fetch(url)
          .then((response) => response.json())
          .then((data) => {
            if (data.status == "OK") {
              console.log(data);
              const formattedAddress = data.results[0].formatted_address;
              console.log("formated address: " + formattedAddress);
              userLocation = formattedAddress;
            } else {
              console.log("Geocoding failed with status " + data.status);
            }
          })
          .catch((error) => {
            console.log("Error: " + error);
          });
      },
      //ON ERROR
      function (error) {
        switch (error.code) {
          case error.PERMISSION_DENIED:
            alert("PERMISSION DENIED");
            break;
          case error.POSITION_UNAVAILABLE:
            alert("LOCATION UNAVAILABLE");
            break;
          case error.TIMEOUT:
            alert("TIMEOUT");
            break;
          default:
            alert("AN ERROR OCCURED");
            break;
        }
      },
      { enableHighAccuracy: true }
    );
  }
});
