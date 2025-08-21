package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.flightdemo.dtos.FlightDTO;
import dk.cphbusiness.flightdemo.dtos.FlightInfoDTO;
import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {

    public FlightReader() throws IOException {
    }

    public static void main(String[] args) {
        try {
            List<FlightDTO> flightList = getFlightsFromFile("flights.json");
            List<FlightInfoDTO> flightInfoDTOList = getFlightInfoDetails(flightList);
            // flightInfoDTOList.forEach(System.out::println);

            // round-1 call
            double totalLufthansaTime = getTotalFlightTimeForAirline(flightList, "Lufthansa");
            System.out.println("Total Lufthansa flight time (hours): " + totalLufthansaTime);
            System.out.println("----------------------------------");

            // round-2 call
            double averageLufthansaFlightDuration = getAverageFlightTimeForAirline(flightList, "Lufthansa");
            System.out.println("Average Lufthansa flight duration (hours): " + averageLufthansaFlightDuration);
            System.out.println("----------------------------------");

            // round-3 call
            List<FlightInfoDTO> flightsFukuokaHaneda = getFlightsBetween2Airports(flightList, "Fukuoka", "Haneda Airport");
            flightsFukuokaHaneda.forEach(System.out::println);

            // round-4 call: flights leaving before 1 AM
            LocalTime oneAM = LocalTime.of(1, 0);
            List<FlightInfoDTO> flightsBefore1AM = getFlightsLeavingBefore1AM(flightList, oneAM);
            flightsBefore1AM.forEach(System.out::println);
            System.out.println("----------------------------------");

            // round-5 call
            double averageFlightTime = getAverageFlightTime(flightList);
            System.out.println("Average flight time (hours): " + averageFlightTime);
            System.out.println("----------------------------------");

            // round-6 call
            List<FlightInfoDTO> sortedArrivalTimes = getSortedArrivalTimes(flightList);
            sortedArrivalTimes.forEach(System.out::println);

            // round-7 call
            getTotalFlightTimeForEachAirline(flightInfoDTOList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<FlightDTO> getFlightsFromFile(String filename) throws IOException {
        ObjectMapper objectMapper = Utils.getObjectMapper();

        // Deserialize JSON from a file into FlightDTO[]
        FlightDTO[] flightsArray = objectMapper.readValue(Paths.get("flights.json").toFile(), FlightDTO[].class);

        // Convert to a list
        return List.of(flightsArray);
    }

    public static List<FlightInfoDTO> getFlightInfoDetails(List<FlightDTO> flightList) {
        return flightList.stream()
                .map(flight -> {
                    LocalDateTime departure = flight.getDeparture().getScheduled();
                    LocalDateTime arrival = flight.getArrival().getScheduled();
                    Duration duration = Duration.between(departure, arrival);

                    return FlightInfoDTO.builder()
                            .name(flight.getFlight().getNumber())
                            .iata(flight.getFlight().getIata())
                            .airline(flight.getAirline().getName())
                            .duration(duration)
                            .departure(departure)
                            .arrival(arrival)
                            .origin(flight.getDeparture().getAirport())
                            .destination(flight.getArrival().getAirport())
                            .build();
                })
                .toList();
    }

    public static double getTotalFlightTimeForAirline(List<FlightDTO> flightList, String airline) {
        return flightList.stream()
                .filter(flight -> flight.getAirline() != null &&
                        airline.equalsIgnoreCase(flight.getAirline().getName()))
                .mapToDouble(flight -> {
                    LocalDateTime departure = flight.getDeparture().getScheduled();
                    LocalDateTime arrival = flight.getArrival().getScheduled();
                    return Duration.between(departure, arrival).toMinutes() / 60.0;
                })
                .sum();
    }

    public static double getAverageFlightTimeForAirline(List<FlightDTO> flightList, String airline) {
        return flightList.stream()
                .filter(flight -> flight.getAirline() != null && airline.equalsIgnoreCase(flight.getAirline().getName()))
                .mapToDouble(flight -> {
                    LocalDateTime departure = flight.getDeparture().getScheduled();
                    LocalDateTime arrival = flight.getArrival().getScheduled();
                    return Duration.between(departure, arrival).toMinutes() / 60.0;
                })
                .average()
                .orElse(0.0);
    }

    public static List<FlightInfoDTO> getFlightsBetween2Airports(List<FlightDTO> flightList, String airport1, String airport2) {
        return flightList.stream()
                .filter(flight -> flight.getDeparture() != null && flight.getArrival() != null)
                .filter(flight -> {
                    String departure = flight.getDeparture().getAirport();
                    String arrival = flight.getArrival().getAirport();
                    return (airport1.equalsIgnoreCase(departure) && airport2.equalsIgnoreCase(arrival)) ||
                            (airport2.equalsIgnoreCase(departure) && airport1.equalsIgnoreCase(arrival));
                })
                .map(flight -> FlightInfoDTO.builder()
                        .airline(flight.getAirline().getName())
                        .name(flight.getFlight().getNumber())
                        .iata(flight.getFlight().getIata())
                        .origin(flight.getDeparture().getAirport())
                        .destination(flight.getArrival().getAirport())
                        .build())
                .toList();
    }

    public static List<FlightInfoDTO> getFlightsLeavingBefore1AM(List<FlightDTO> flightList, LocalTime time) {
        return flightList.stream()
                .filter(flight -> flight.getDeparture() != null &&
                        flight.getDeparture().getScheduled() != null)
                .filter(flight -> flight.getDeparture().getScheduled().toLocalTime().isBefore(time))
                .map(flight -> FlightInfoDTO.builder()
                        .name(flight.getFlight().getNumber())
                        .iata(flight.getFlight().getIata())
                        .airline(flight.getAirline().getName())
                        .departure(flight.getDeparture().getScheduled())
                        .origin(flight.getDeparture().getAirport())
                        .build())
                .toList();
    }

    public static double getAverageFlightTime(List<FlightDTO> flightList) {
        return flightList.stream()
                .filter(flight -> flight.getDeparture() != null && flight.getArrival() != null)
                .mapToDouble(flight -> {
                    LocalDateTime departure = flight.getDeparture().getScheduled();
                    LocalDateTime arrival = flight.getArrival().getScheduled();
                    return Duration.between(departure, arrival).toMinutes() / 60.0;
                })
                .average()
                .orElse(0.0);
    }

    public static List<FlightInfoDTO> getSortedArrivalTimes(List<FlightDTO> flightList) {
        return flightList.stream()
                .filter(flight -> flight.getArrival() != null && flight.getArrival().getScheduled() != null)
                .sorted((f1, f2) -> f1.getArrival().getScheduled().compareTo(f2.getArrival().getScheduled()))
                .map(flight -> FlightInfoDTO.builder()
                        .name(flight.getFlight().getNumber())
                        .iata(flight.getFlight().getIata())
                        .airline(flight.getAirline().getName())
                        .departure(flight.getDeparture().getScheduled())
                        .arrival(flight.getArrival().getScheduled())
                        .origin(flight.getDeparture().getAirport())
                        .destination(flight.getArrival().getAirport())
                        .build())
                .toList();
    }


    public static void getTotalFlightTimeForEachAirline(List<FlightInfoDTO> flightList) {
        flightList.stream()
                .filter(flight -> flight.getDuration() != null && flight.getAirline() != null)
                .collect(Collectors.groupingBy(
                        FlightInfoDTO::getAirline,
                        Collectors.summingDouble(flight -> flight.getDuration().toMinutes() / 60.0)
                ))
                .forEach((airline, totalHours) -> System.out.println(airline + " " + totalHours + " hours"));
    }
}
