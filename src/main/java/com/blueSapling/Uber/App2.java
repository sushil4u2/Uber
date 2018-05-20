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
public class App2 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
//			new com.mysql.jdbc.Driver();
			Class.forName("com.mysql.jdbc.Driver").newInstance();
// conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdatabase?user=testuser&password=testpassword");
			String connectionUrl = "jdbc:mysql://localhost:3306/uberDB";
			String connectionUser = "root";
			String connectionPassword = "";
			conn = DriverManager.getConnection(connectionUrl, connectionUser, connectionPassword);
			stmt = conn.createStatement();
/*			rs = stmt.executeQuery("SELECT * FROM Driver");
			while (rs.next()) {
				String id = rs.getString("Name");
				String Vehicle_No = rs.getString("Vehicle_Name");
				String Vehicle_Name = rs.getString("Vehicle_Name");
				System.out.println("ID: " + id + ", First Name: " + Vehicle_Name
						+ ", Last Name: " + Vehicle_Name);
			}*/
			System.out.println("Select a Customer type:\n1. Silver\t2. Gold\t\t3. Platinum");
			Scanner sc = new Scanner(System.in);
			int userType = sc.nextInt();
			switch(userType) {
				case 1:{
					rs = stmt.executeQuery("select * from Driver where Rating < 4.5 and Available = true and Employed = true order by rand() limit 1");
					break;
				}
				case 2:{
					rs = stmt.executeQuery("select * from Driver where Rating between 4.5 and 4.8 and Available = true and Employed = true order by rand() limit 1");
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
				System.out.println("out if");
				rs = stmt.executeQuery("select * from Driver where Available = true and Employed = true order by rand() limit 1");
			}
			int rides = 0, Vehicle_No = 0;
			Double rating = 0.0;
				if(rs.next() == true) {
				String name = rs.getString("Name");
				Vehicle_No = rs.getInt("Vehicle_No");
				String Vehicle_Name = rs.getString("Vehicle_Name");
				rating = rs.getDouble("Rating");
				double dummyRating = rating;
				rides = rs.getInt("Rides");
				if(rides <= 5)
					dummyRating = 5.0;
				System.out.println("Congrates, cab is booked!\nDriver Name: " + name + ", Rating: "+dummyRating+ ", Vehicle Number: " + Vehicle_No
						+ ", Vehicle Name: " + Vehicle_Name);
			}else System.out.println("No cab found, please try again later.");
			
			System.out.println("Please enter values(saperated by space) of given parameters:\n1. distance (in Km)  2. travel time (in minutes)  3. surge (1-2)  4. waiting time (in minutes)  5. Ride cancelled (Y/N)\n");
			double distance = sc.nextDouble();
			double travel_time = sc.nextDouble();
			double surge = sc.nextDouble();
			double waiting_time = sc.nextDouble();
			String ride_cancelled = sc.nextLine();
			double fare =  0;
			
			if(ride_cancelled == "Y") {
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
			System.out.println("Your fare is: "+fare);
			
			System.out.println("please rate your driver (enter rating 1-5)");
			double rate = sc.nextDouble();
			rate = (rides*rating + rate)/(rides+1);
			rides++;
			stmt.executeUpdate("update Driver set Rides="+rides+", Rating="+rate+" where Vehicle_No="+Vehicle_No+"");
			
			System.out.println("Thanks You");
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
		}
    }
}
