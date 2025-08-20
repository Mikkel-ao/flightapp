package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.flightdemo.dtos.FlightDTO;
import dk.cphbusiness.flightdemo.dtos.FlightInfoDTO;
import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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
            /*
            flightInfoDTOList.forEach(System.out::println);
            */

            // round-1 call
            double totalLufthansaTime = getTotalFlightTimeForAirline(flightList, "Lufthansa");
            System.out.println("Total Lufthansa flight time (hours): " + totalLufthansaTime);
            // round-2 call
            double averageLufthansaFlightDuration = getAverageFlightTimeForAirline(flightList, "Lufthansa");
            System.out.println("Average Lufthansa flight duration (hours): " + averageLufthansaFlightDuration);
            // round-3 call
            List<FlightInfoDTO> flightsFukuokaHaneda = getFlightsBetween2Airports(flightList, "Fukuoka", "Haneda Airport");
            // flightsFukuokaHaneda.forEach(System.out::println);

            // round-4 call
            LocalDateTime oneAM = LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0));
            // Call the method
            List<FlightInfoDTO> flightsBefore1AM = getFlightsLeavingBefore1AM(flightList, oneAM);
            // Print results
            flightsBefore1AM.forEach(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FlightDTO> getFlightsFromFile(String filename) throws IOException {

        ObjectMapper objectMapper = Utils.getObjectMapper();

        // Deserialize JSON from a file into FlightDTO[]
        FlightDTO[] flightsArray = objectMapper.readValue(Paths.get("flights.json").toFile(), FlightDTO[].class);

        // Convert to a list
        List<FlightDTO> flightsList = List.of(flightsArray);
        return flightsList;
    }

    public static List<FlightInfoDTO> getFlightInfoDetails(List<FlightDTO> flightList) {
        List<FlightInfoDTO> flightInfoList = flightList.stream()
           .map(flight -> {
                LocalDateTime departure = flight.getDeparture().getScheduled();
                LocalDateTime arrival = flight.getArrival().getScheduled();
                Duration duration = Duration.between(departure, arrival);
                FlightInfoDTO flightInfo =
                        FlightInfoDTO.builder()
                            .name(flight.getFlight().getNumber())
                            .iata(flight.getFlight().getIata())
                            .airline(flight.getAirline().getName())
                            .duration(duration)
                            .departure(departure)
                            .arrival(arrival)
                            .origin(flight.getDeparture().getAirport())
                            .destination(flight.getArrival().getAirport())
                            .build();

                return flightInfo;
            })
        .toList();
        return flightInfoList;
    }


    public static double getTotalFlightTimeForAirline(List<FlightDTO> flightList, String airlineName) {
        double result = flightList.stream()
                .filter(n -> n.getAirline() !=null && airlineName.equalsIgnoreCase(n.getAirline().getName()))
                .mapToDouble(flight -> {
                    LocalDateTime departure = flight.getDeparture().getScheduled();
                    LocalDateTime arrival = flight.getArrival().getScheduled();
                    return Duration.between(departure, arrival).toMinutes() / 60;
                })
                .sum();
        return result;
    }

    public static double getAverageFlightTimeForAirline(List<FlightDTO> flightList, String airlineName) {
        double result = flightList.stream()
                .filter(n -> n.getAirline() !=null && airlineName.equalsIgnoreCase(n.getAirline().getName()))
                .mapToDouble(flight -> {
                    LocalDateTime departure = flight.getDeparture().getScheduled();
                    LocalDateTime arrival = flight.getArrival().getScheduled();
                    return Duration.between(departure, arrival).toMinutes() / 60.0;
                })
                .average().orElse(0.0);
        return result;
    }

    public static List<FlightInfoDTO> getFlightsBetween2Airports(List<FlightDTO> flightList, String airport1, String airport2) {
        return flightList.stream()
                .filter(n -> n.getDeparture() != null && n.getArrival() != null)
                .filter(n -> {
                    String departure = n.getDeparture().getAirport();
                    String arrival = n.getArrival().getAirport();
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
                .collect(Collectors.toList());
    }

    public static List<FlightInfoDTO> getFlightsLeavingBefore1AM(List<FlightDTO> flightList, LocalDateTime time) {
        List<FlightInfoDTO> flightInfoList = flightList.stream()
                .filter(flight -> flight.getDeparture() != null
                        && flight.getArrival() != null
                        && flight.getDeparture().getScheduled() != null
                        && flight.getArrival().getScheduled() != null
                        && flight.getAirline() != null
                        && flight.getFlight() != null)
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
                .toList(); // or collect(Collectors.toList()) if using Java <16

        return flightInfoList;
    }
}
