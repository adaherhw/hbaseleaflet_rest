
$(document).ready(function() {
    L.mapbox.accessToken = "pk.eyJ1IjoiaG9ydG9ud29ya3MiLCJhIjoiY2lnbGl0NzMzMDBvd3Q3bTNoc3k3dW10dCJ9.M-c9jLANdBBFMMKetZzeRg";
});

angular.module("carmap", [ "ui.bootstrap" ]).controller("myController", function($scope, $http, $modal) {
    $scope.tripHeaders = [ "Pickup Time", "Trip Duration in seconds", "Trip Miles", "Passenger Count", "Pickup Location", "Dropoff Location", "Route" ];
    $scope.queryButton = function(medallion) {
        $http({
            url: "vin-data?vin=" + medallion
        }).error(function(error) {
            alert("error");
        }).success(function(json) {
            $scope.vinData = $.parseJSON(atob(json.Row[0].Cell[0].$));
            $(".result-vin").show();
        });
    };
    $scope.showMap = function(trip) {
        $modal.open({
            templateUrl : "modal.html",
            size : "lg"
        }).rendered.then(function() {
            var map = L.mapbox.map("map", "hortonworks.ciglit5si01g6uikrovumiiv7", { center: trip["trip:pickup_location"], zoom: 12 });
            var pickupMarker = L.marker(trip["trip:pickup_location"]).addTo(map);
            var dropoffMarker = L.marker(trip["trip:dropoff_location"]).addTo(map);
            pickupMarker.bindPopup("<b>Pickup Location</b><br>[ " + trip["trip:pickup_location"] + " ]");
            dropoffMarker.bindPopup("<b>Dropoff Location</b><br>[ " + trip["trip:dropoff_location"] + " ]");
        });
    };
    $scope.showRoute = function(medallion, trip) {
        $modal.open({
            templateUrl : "modal.html",
            size : "lg"
        }).rendered.then(function() {
            $http({
                url : "trip-data?vin=" + medallion + "&pickupTime=" + trip["trip:pickup_datetime"]
            }).error(function(error) {
                alert("error");
            }).success(function(json) {
                var kmlData = atob(json.Row[0].Cell[0].$);
                var runLayer = omnivore.kml.parse(kmlData);
                var map = L.mapbox.map("map", "hortonworks.ciglit5si01g6uikrovumiiv7", { center: [39.87, -83.05], zoom: 9 });
                map.fitBounds(runLayer.getBounds());
                runLayer.addTo(map);
            });
        });
    };
    function logback(value) {
        $http({ "url" : "logback?value=" + value });
    }
});
