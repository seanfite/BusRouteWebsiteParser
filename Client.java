package Assignment1;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;

//Sean Fite
//CS 320
//Programming Assignment 1
//Last worked on 2/10/23
//This calls the RouteFinder class

public class Client {

	public static void main(String[] args) throws MalformedURLException, IOException, ParseException
	{	
		RouteFinder.getBusRouteTripsLengthsInMinutesToAndFromDestination(RouteFinder.getDestinationBusesMap());
	}
}
