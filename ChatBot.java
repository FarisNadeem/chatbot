package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class ChatBot {

    private static final String WEATHER_API_KEY = "YOUR_OPENWEATHERMAP_API_KEY";
    private static final String WEATHER_BASE_URL = "http://api.openweathermap.org/data/2.5/weather";

    private static final String EXCHANGE_API_KEY = "YOUR_EXCHANGERATE_API_KEY";
    private static final String EXCHANGE_BASE_URL = "https://v6.exchangerate-api.com/v6/" + EXCHANGE_API_KEY + "/latest/USD";

    public static void main(String[] args) {
        ChatBot chatBot = new ChatBot();
        chatBot.start();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the ChatBot! How can I help you today?");

        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            } else if (input.toLowerCase().contains("weather")) {
                System.out.println("Please enter the city name:");
                String city = scanner.nextLine();
                try {
                    String weather = getCurrentWeather(city);
                    System.out.println(weather);
                } catch (IOException | InterruptedException e) {
                    System.out.println("Failed to get weather information.");
                }
            } else if (input.toLowerCase().contains("exchange rate")) {
                System.out.println("Please enter the target currency code (e.g., GEL for Georgian Lari):");
                String currency = scanner.nextLine();
                try {
                    String rate = getExchangeRate(currency);
                    System.out.println(rate);
                } catch (IOException | InterruptedException e) {
                    System.out.println("Failed to get exchange rate information.");
                }
            } else {
                System.out.println("Sorry, I don't understand that command.");
            }
        }

        scanner.close();
        System.out.println("Goodbye!");
    }

    private String getCurrentWeather(String city) throws IOException, InterruptedException {
        String url = WEATHER_BASE_URL + "?q=" + city + "&appid=" + WEATHER_API_KEY + "&units=metric";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        if (response.statusCode() != 200) {
            return "Failed to get weather information: " + jsonObject.get("message").getAsString();
        }

        if (!jsonObject.has("weather") || jsonObject.getAsJsonArray("weather").size() == 0) {
            return "No weather information available for " + city;
        }

        String weatherDescription = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString();
        double temperature = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();

        return String.format("Current weather in %s: %s, Temperature: %.1f°C", city, weatherDescription, temperature);
    }

    private String getExchangeRate(String targetCurrency) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EXCHANGE_BASE_URL))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        if (response.statusCode() != 200) {
            return "Failed to get exchange rate information.";
        }

        if (!jsonObject.has("conversion_rates") || !jsonObject.getAsJsonObject("conversion_rates").has(targetCurrency)) {
            return "No exchange rate information available for " + targetCurrency;
        }

        double exchangeRate = jsonObject.getAsJsonObject("conversion_rates").get(targetCurrency).getAsDouble();

        return String.format("Exchange rate for USD to %s: %.2f", targetCurrency, exchangeRate);
    }
}
