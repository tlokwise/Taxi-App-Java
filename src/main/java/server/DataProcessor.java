package server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import db.DBConnection;

/**
 * Servlet implementation class DataProcessor
 */
public class DataProcessor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final double EARTH_RADIUS = 6371;
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String data = request.getParameter("data");
		PrintWriter out = response.getWriter();
		
		//PROCESSING JSON DATA
		ObjectMapper mapper = new ObjectMapper();
		
		//parse entire json data
		JsonNode rootNode = mapper.readTree(data);
		
		JsonNode locationObj = rootNode.get("location");
		JsonNode destinationObj = rootNode.get("destination");
		
		String locationName = locationObj.get("name").asText();
		String locationAddress = locationObj.get("address").asText();
		String locationId = locationObj.get("placeId").asText();
		double locationLatitude = locationObj.get("latitude").asDouble();
		double locationLongitude = locationObj.get("longitude").asDouble();
		
		String destinationName = destinationObj.get("name").asText();
		String destinationAddress = destinationObj.get("address").asText();
		String destinationId = destinationObj.get("placeId").asText();
		double destinationLatitude = destinationObj.get("latitude").asDouble();
		double destinationLongitude = destinationObj.get("longitude").asDouble();
		
		//find the closest location available in the db
		double closestLocation = 0;
		String closestLocationName = "";
		String closestLocationAddress = "";
		String closestLocationPlaceId = "";
		double closestLocationLatitude = 0;
		double closestLocationLongitude = 0;
		//DB 
		Connection connection = DBConnection.getConnection();
		
		String queryLocation = "SELECT * FROM locations";
		try {
			PreparedStatement qLocationStatement = connection.prepareStatement(queryLocation);
			
			ResultSet qLocationResults = qLocationStatement.executeQuery();
			
			while(qLocationResults.next()) {
				String resultName = qLocationResults.getString("location_name");
				String resultPlaceId = qLocationResults.getString("place_id");
				String resultAddress = qLocationResults.getString("address");
				double resultLatitude = qLocationResults.getDouble("latitude");
				double resultLongitude = qLocationResults.getDouble("longitude");
				
				double relativeDistance = calculateDistance(locationLatitude, locationLongitude, resultLatitude, resultLongitude);
				
				if(closestLocation == 0) {
					closestLocation = relativeDistance;
				}
				if(relativeDistance <= closestLocation) {
					closestLocation = relativeDistance;
					//update closest location info
					closestLocationName = resultName;
					closestLocationAddress = resultAddress;
					closestLocationPlaceId = resultPlaceId;
					closestLocationLatitude = resultLatitude;
					closestLocationLongitude = resultLongitude;
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//find closest destination from db
		double closestDestination = 0;
		String closestDestinationName = "";
		String closestDestinationAddress = "";
		String closestDestinationPlaceId = "";
		double closestDestinationLatitude = 0;
		double closestDestinationLongitude = 0;
		
		
		String queryDestination = "SELECT * FROM destinations";
		try {
			PreparedStatement qDestinationStatement = connection.prepareStatement(queryDestination);
			
			ResultSet qDestinationResults = qDestinationStatement.executeQuery();
			
			while(qDestinationResults.next()) {
				String resultName = qDestinationResults.getString("destination_name");
				String resultPlaceId = qDestinationResults.getString("place_id");
				String resultAddress = qDestinationResults.getString("address");
				double resultLatitude = qDestinationResults.getDouble("latitude");
				double resultLongitude = qDestinationResults.getDouble("longitude");
				
				double relativeDistance = calculateDistance(destinationLatitude, destinationLongitude, resultLatitude, resultLongitude);
				
				if(closestDestination == 0) {
					closestDestination = relativeDistance;
				}
				if(relativeDistance <= closestDestination) {
					closestDestination = relativeDistance;
					
					//update closest location info
					closestDestinationName = resultName;
					closestDestinationAddress = resultAddress;
					closestDestinationPlaceId = resultPlaceId;
					closestDestinationLatitude = resultLatitude;
					closestDestinationLongitude = resultLongitude;
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		double price = getPrice(closestLocationPlaceId, closestDestinationPlaceId);
		//arraning data in json format
		String sendThis = "{\r\n"
				+ "    \"closestLocation\":{\r\n"
				+ "        \"name\": \" " + closestLocationName + " \",\r\n"
				+ "        \"address\": \" " + closestLocationAddress + " \",\r\n"
				+ "        \"placeId\": \" " + closestLocationPlaceId + " \",\r\n"
				+ "        \"latitude\": " + closestLocationLatitude + ",\r\n"
				+ "        \"longitude\": " + closestLocationLongitude +"\r\n"
				+ "    },\r\n"
				+ "    \"closestDestination\":{\r\n"
				+ "        \"name\": \" " + closestDestinationName + " \",\r\n"
				+ "        \"address\": \" " + closestDestinationAddress + " \",\r\n"
				+ "        \"placeId\": \" " + closestDestinationPlaceId + " \",\r\n"
				+ "        \"latitude\": " + closestDestinationLatitude + " ,\r\n"
				+ "        \"longitude\": " + closestDestinationLongitude + " \r\n"
				+ "    },\r\n"
				+ "    \"price\": " + price + " \r\n"
				+ "}";
		DBConnection.closeConnection();
		response.setContentType("text/plain");
		response.getWriter().write(sendThis);
	}
	
	
	private static double calculateDistance(double locationLat, double locationLng, double destinationLat,
            double destinationLng) {
        locationLat = Math.toRadians(locationLat);
        locationLng = Math.toRadians(locationLng);
        destinationLat = Math.toRadians(destinationLat);
        destinationLng = Math.toRadians(destinationLng);

        // Haversine formula
        double differentLat = destinationLat - locationLat;
        double differentLng = destinationLng - locationLng;
        double a = Math.sin(differentLat / 2) * Math.sin(differentLat / 2) + Math.cos(locationLat)
                * Math.cos(destinationLat) * Math.sin(differentLng / 2) * Math.sin(differentLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        return distance;

    }
	
	private static double getPrice(String locationId, String destinationId) {
		double price = 0;
		Connection connection = DBConnection.getConnection();
		String queryPrice = "SELECT price FROM main_man WHERE location_id = (?) AND destination_id = (?)";
		
		try {
			PreparedStatement pst = connection.prepareStatement(queryPrice);
			pst.setString(1, locationId);
			pst.setString(2, destinationId);
			
			ResultSet queryResults = pst.executeQuery();
			
			while(queryResults.next()) {
				price = queryResults.getDouble("price");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return price;
		
	}
	public void processJson() {
		//PROCESSING JSON DATA
//		ObjectMapper mapper = new ObjectMapper();
//		
//		//parse entire json data
//		JsonNode rootNode = mapper.readTree(data);
//		
//		JsonNode locationObj = rootNode.get("location");
//		JsonNode destinationObj = rootNode.get("destination");
//		
//		String locationName = locationObj.get("name").asText();
//		String locationAddress = locationObj.get("address").asText();
//		String locationId = locationObj.get("placeId").asText();
//		double locationLatitude = locationObj.get("latitude").asDouble();
//		double locationLongitude = locationObj.get("longitude").asDouble();
//		
//		String destinationName = destinationObj.get("name").asText();
//		String destinationAddress = destinationObj.get("address").asText();
//		String destination = destinationObj.get("placeId").asText();
//		double destinationLatitude = destinationObj.get("latitude").asDouble();
//		double destinationLongitude = destinationObj.get("longitude").asDouble();
	}

}
