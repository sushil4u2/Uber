package com.blueSapling.Uber;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
	static class Driver{
		String name, vehicle_Name;
		int vehicle_No, rides;
		double rating;
		boolean Available, Employed;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getVehicle_Name() {
			return vehicle_Name;
		}
		public void setVehicle_Name(String vehicle_Name) {
			this.vehicle_Name = vehicle_Name;
		}
		public int getVehicle_No() {
			return vehicle_No;
		}
		public void setVehicle_No(int vehicle_No) {
			this.vehicle_No = vehicle_No;
		}
		public int getRides() {
			return rides;
		}
		public void setRides(int rides) {
			this.rides = rides;
		}
		public double getRating() {
			return rating;
		}
		public void setRating(double rating) {
			this.rating = rating;
		}
		public boolean isAvailable() {
			return Available;
		}
		public void setAvailable(boolean available) {
			Available = available;
		}
		public boolean isEmployed() {
			return Employed;
		}
		public void setEmployed(boolean employed) {
			Employed = employed;
		}

	}
	static Statement getDBConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		String connectionUrl = "jdbc:mysql://localhost:3306/uberDB";
		String connectionUser = "root";
		String connectionPassword = "";
		conn = DriverManager.getConnection(connectionUrl, connectionUser, connectionPassword);
		stmt = conn.createStatement();

		return stmt;
	}

	static Driver getDriver(Statement stmt, int userType) throws SQLException {
		ResultSet rs = null;
		switch(userType) {
		case 1:{
			rs = stmt.executeQuery("select * from Driver where Rating < 4.5 and Available = true and Employed = true order by rand() limit 1");
			break;
		}
		case 2:{
			rs = stmt.executeQuery("select * from Driver where Rating between 4.5 and 4.7 and Available = true and Employed = true order by rand() limit 1");
			break;
		}
		case 3:{
			rs = stmt.executeQuery("select * from Driver where Rating > 4.8 and Available = true and Employed = true order by rand() limit 1");
			if(rs == null)
				rs = stmt.executeQuery("select * from Driver where Rating between 4.5 and 4.8 and Available = true and Employed = true order by rand() limit 1");
			break;
		}
		default:{
			System.out.println("Wrone type of User");
			break;
		}
		}
		if(rs == null) {
			rs = stmt.executeQuery("select * from Driver where Available = true and Employed = true order by rand() limit 1");
		}

		Driver driver = new Driver();
		if(rs.next() == true) {
			driver.setName(rs.getString("Name"));
			driver.setVehicle_No(rs.getInt("Vehicle_No"));
			driver.setVehicle_Name(rs.getString("Vehicle_Name"));
			driver.setRating(rs.getDouble("Rating"));
			driver.setRides(rs.getInt("Rides"));
		}
		return driver;
	}

	static double calculateFare(double distance, double travel_time, double surge, double waiting_time, String ride_cancelled){
		double fare = 0.0;
		if(ride_cancelled.equals("Y")) {
			if(waiting_time >= 4)
				fare = 50;
		}else {
			double travel_time_charge = travel_time, perKm_charge = 8*distance;
			int waiting_charge = 0;
			if(waiting_time > 4) {
				waiting_charge = ((int)waiting_time - 4) * 10;
			}
			fare = waiting_charge + (travel_time_charge + perKm_charge) * surge;
		}
		return fare;
	}

	static void updateDriver(Statement stmt, double rate, int rides, double rating, int Vehicle_No) throws SQLException{
		rate = (rides*rating + rate)/(rides+1);
		rides++;
		if(rides > 5 && rate < 4.0)
			stmt.executeUpdate("update Driver set Rides="+rides+", Rating="+rate+", Employed=false where Vehicle_No="+Vehicle_No+"");
		else stmt.executeUpdate("update Driver set Rides="+rides+", Rating="+rate+" where Vehicle_No="+Vehicle_No+"");

	}

	public static void main( String[] args ) throws SQLException
	{
		Statement stmt = null;
		try {
			stmt = App.getDBConnection();
			System.out.println("Select a Customer type:\n1. Silver\t2. Gold\t\t3. Platinum");
			Scanner sc = new Scanner(System.in);
			int userType = sc.nextInt();

			//Fetching Driver details
			Driver driver = App.getDriver(stmt, userType);

			if(driver != null) {
				String name = driver.getName();
				int Vehicle_No = driver.getVehicle_No();
				String Vehicle_Name = driver.getVehicle_Name();
				double rating = driver.getRating();
				double dummyRating = rating;
				int rides = driver.getRides();
				if(rides <= 5)
					dummyRating = 5.0;
				System.out.println("Congrates, cab is booked!\nDriver Name: " + name + ", Rating: "+dummyRating+ ", Vehicle Number: " + Vehicle_No	+ ", Vehicle Name: " + Vehicle_Name);


				System.out.println("Please enter values(saperated by space) of given parameters:\n1. distance (in Km)  2. travel time (in minutes)  3. surge (1-2)  4. waiting time (in minutes)  5. Ride cancelled (Y/N)\n");
				double distance = sc.nextDouble();
				double travel_time = sc.nextDouble();
				double surge = sc.nextDouble();
				double waiting_time = sc.nextDouble();
				String ride_cancelled = sc.next();

				//calculate fare for the current ride
				double fare = App.calculateFare(distance, travel_time, surge, waiting_time, ride_cancelled);

				System.out.println("Your fare is: "+fare);

				if(ride_cancelled.equals("N")) {
					System.out.println("please rate your driver (enter rating 1-5)");
					double rate = sc.nextDouble();

					//updating driver's details after current ride
					App.updateDriver(stmt, rate, rides, rating, Vehicle_No);
				}
			}else System.out.println("No cab found, please try again later.");

			System.out.println("Thanks You");

		}catch (SQLException e) {
			System.out.println("Database access error occured");
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			Connection conn = stmt.getConnection();
			ResultSet rs = stmt.getResultSet();
			try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
		}
	}
}
