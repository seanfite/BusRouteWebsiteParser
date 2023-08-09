package Assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Sean Fite
//CS 320
//Programming Assignment 1
//Last worked on 2/10/23
//This program reads HTML source code and matches to specific regex to pull city bus info

public class RouteFinder {
	private static Scanner scanner;
	private static String filteredResultsURLInitialization = "";
	private static ArrayList<String> filteredResultsURL = new ArrayList<String>();
	private static ArrayList<Long> busRouteTimeDifferences = new ArrayList<Long>();
	private static Map<String, Map<String, String>> destinationResults = new HashMap<>();
	private static Map<String, Map<String, String>> filteredResults = new HashMap<>();
	private static Map<String, String> destinationBusesMap = new HashMap<>();

	public static char userInput() { // user input for char of destinatoin
		System.out.print("Enter the letter your city begins with: ");
		Scanner scanner = new Scanner(System.in);
		char charInput = Character.toUpperCase(scanner.next().charAt(0)); // format to be case insensitive
		if (!((charInput >= 'A' && charInput <= 'Z') || (charInput >= 'a' && charInput <= 'z'))) {
			throw new IllegalArgumentException("Input must be a letter in the alphabet");
		}
		return charInput;
	}

	public static String userInput2() // second user input for full String destination matching
	{
		System.out.print("\n" + "Please enter your destination: ");
		scanner = new Scanner(System.in);
		String wordInput = scanner.next();
		return wordInput;
	}

	public static String readHTML(String URL) throws MalformedURLException, IOException // read HTML code from website
	{
		String HTMLtext = "";
		URLConnection routes = new URL(URL).openConnection();
		routes.setRequestProperty("user-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		BufferedReader in = new BufferedReader(new InputStreamReader(routes.getInputStream()));
		String inputLine = "";
		while ((inputLine = in.readLine()) != null) {
			HTMLtext += inputLine + "\n"; // store in string
		}
		in.close();
		return HTMLtext;
	}

	public static Map<String, Map<String, String>> getBusRoutesURL(final char destInitial)
			throws MalformedURLException, IOException {
		// method to match userInput1 and return bus routes
		String text = readHTML(IRouteFinder.TRANSIT_WEB_URL);
		String URL = "https://www.communitytransit.org/busservice/schedules/route/";
		boolean result = false;
		String city = "";
		Pattern pattern = Pattern.compile("<h3>(.*?)</h3>|<a href=\"/schedules/route/.*>(.*?)</a>"); // regex or
																										// statement
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			if (matcher.group(1) != null && matcher.group(1).charAt(0) != destInitial) { // if case to only allow
																							// matching with userInput1
				result = false;
			}
			if (matcher.group(1) != null && matcher.group(1).charAt(0) == destInitial) { // if case to match group 1
																							// with destination names
				city = matcher.group(1);
				result = true;
				destinationResults.put(city, new HashMap<>());
				System.out.println("\n" + "Destination: " + city);
			}
			if (matcher.group(2) != null && matcher.group(1) == null && result == true) { // if case to match with route
																							// info
				String route = matcher.group(2);
				route = route.replace("/", "-");
				destinationResults.get(city).put(route, URL + route); // update to hashmap
				System.out.println("Bus Number: " + route);
			}
		}
		return destinationResults;
	}

	public static void FilterDestinationResultsBasedOnInput() throws MalformedURLException, IOException {
		// method to narrow hashmap results to userInput2
		getBusRoutesURL(userInput());
		filteredResultsURLInitialization = "";
		String city = userInput2();
		for (Map.Entry<String, Map<String, String>> entry : destinationResults.entrySet()) {
			if (entry.getKey().contains(city)) {
				filteredResults.put(city, entry.getValue()); // add results to new hashmap
				filteredResultsURLInitialization += entry.getValue().toString(); // create URLs for routes to use later
			}
		}
	}

	public static void URLBuilderForFilteredResults() throws MalformedURLException, IOException { 
		// format URLs from above to work as web address																								
		FilterDestinationResultsBasedOnInput();
		String[] individ = filteredResultsURLInitialization.replace("{", "").replace("}", "").split(",");
		for (int i = 0; i < individ.length; i++) {
			String URL = individ[i].substring(individ[i].indexOf("https://")).replace(" ", "-");
			filteredResultsURL.add(URL);
		}
	}

	public static void busRouteTimesToArray() throws MalformedURLException, IOException, ParseException {
		// method to return time differences of routes
		URLBuilderForFilteredResults();
		long totalTime = 0;
		int count = 0;
		ArrayList<String> arr = new ArrayList<String>();
		for (int i = 0; i < filteredResultsURL.size(); i++) { // for loop to iterate through different bus routes
			String URL = readHTML(filteredResultsURL.get(i));
			Pattern pattern = Pattern.compile(
					"Weekday(.*) st.*|<tr>\\s*<td class=\"text-center\">(.*)</td>|<td class.*>\\s*(.*M)|Saturday(.*) st.*|<label class.*Trip.*\\s*.*>.(.*)\\s*");
			Matcher matcher = pattern.matcher(URL);
			while (matcher.find()) {
				if (matcher.group(1) != null) // matcher 1 is for the Weekday Label for HTML placement setting
				{
					arr.clear();
				}
				if (matcher.group(2) != null) // matcher 2 helps to group route times by row for correct storage
				{
					if (arr.size() != 0) // if statement to find time difference
					{
						String startTime = arr.get(0);
						String endTime = arr.get(arr.size() - 1);
						if (startTime.length() == 4) {
							startTime = "0" + startTime;
						}
						if (endTime.length() == 4) {
							endTime = "0" + endTime;
						}
						SimpleDateFormat format = new SimpleDateFormat("hh:mm");
						Date date1 = format.parse(startTime);
						Date date2 = format.parse(endTime);
						date2 = format.parse(endTime);
						long difference = Math.abs(date2.getTime() - date1.getTime());
						totalTime = difference / (60 * 1000);
						busRouteTimeDifferences.add(totalTime); // add time differences to array
						arr.clear(); // clear array for next iteration
					}
				}
				if (matcher.group(3) != null) // this group matches with route times
				{
					arr.add(matcher.group(3));
				}
				if (matcher.group(4) != null) // this group stops the match process if we reach a weekend tab
				{
					break;
				}
			}
		}
		changeHashmapFormat();
	}

	public static void changeHashmapFormat() {
		// this method changes the format of our hashmap to fit into the next method
		for (Map.Entry<String, Map<String, String>> entry : filteredResults.entrySet()) {
			for (Map.Entry<String, String> innerEntry : entry.getValue().entrySet()) {
				getDestinationBusesMap().put(innerEntry.getKey(), innerEntry.getValue());
			}
		}
	}

	public static Map<String, List<Long>> getBusRouteTripsLengthsInMinutesToAndFromDestination(
			final Map<String, String> destinationBusesMap) throws MalformedURLException, IOException, ParseException {
		// this method outputs our time difference results
		busRouteTimesToArray();
		String routeNum = destinationBusesMap.keySet().iterator().next();
		String city = filteredResults.keySet().iterator().next();
		String comboOf = routeNum + " - To " + city;
		Map<String, List<Long>> map = new HashMap<String, List<Long>>();
		map.put(comboOf, busRouteTimeDifferences);
		System.out.println("\nBus Trip Lengths In Minutes Are: \n" + map);
		playAgain(); // offer user to play again
		return map;
	}

	public static void playAgain() throws MalformedURLException, IOException, ParseException {
		System.out.println("\nDo you want to check different destination? Please type Y to continue or press any other key to exit"); 
		// restarts program if selected																																																											
		scanner = new Scanner(System.in);
		char playAgain = scanner.next().charAt(0);
		if (playAgain == 'y' || playAgain == 'Y') {
			getBusRouteTripsLengthsInMinutesToAndFromDestination(getDestinationBusesMap());
		} else {
			System.exit(0);
		}
	}

	public static Map<String, String> getDestinationBusesMap() {
		// get set method from Client to call program
		return destinationBusesMap;
	}

	public static void setDestinationBusesMap(Map<String, String> destinationBusesMap) {
		RouteFinder.destinationBusesMap = destinationBusesMap;
	}
}
