package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.flightdemo.dtos.FlightDTO;
import dk.cphbusiness.flightdemo.dtos.FlightInfoDTO;
import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLOutput;
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

            /*
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
            // flightsFukuokaHaneda.forEach(System.out::println);

            // round-4 call: flights leaving before 1 AM
            LocalTime oneAM = LocalTime.of(1, 0);
            List<FlightInfoDTO> flightsBefore1AM = getFlightsLeavingBefore1AM(flightList, oneAM);
            flightsBefore1AM.forEach(System.out::println);
            System.out.println("----------------------------------");


            // round-5 call
            double averageFlightTime = getAverageFlightTime(flightList);
            System.out.println("Average flight time (hours): " + averageFlightTime);

            System.out.println("----------------------------------");

             */

            // round -6 call
            List<FlightInfoDTO> sortedArrivalTimes = getSortedArrivalTimes(flightList);
            // System.out.println("Average flight time (hours): " + sortedArrivalTimes);

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


    public static List<FlightInfoDTO> getFlightsLeavingBefore1AM(List<FlightDTO> flightList, LocalTime time) {
        return flightList.stream()
                .filter(flight -> flight.getDeparture() != null
                        && flight.getDeparture().getScheduled() != null)
                .filter(flight -> flight.getDeparture().getScheduled().toLocalTime().isBefore(time))
                .map(flight -> FlightInfoDTO.builder()
                        .name(flight.getFlight().getNumber())
                        .iata(flight.getFlight().getIata())
                        .airline(flight.getAirline().getName())
                        .departure(flight.getDeparture().getScheduled())
                        .origin(flight.getDeparture().getAirport())
                        .build())
                .collect(Collectors.toList());
    }

    public static double getAverageFlightTime(List<FlightDTO> flightList) {
        double result = flightList.stream()
                .filter(n -> n.getDeparture() != null && n.getArrival() != null)
                .mapToDouble(flight -> {
                    LocalDateTime departure = flight.getDeparture().getScheduled();
                    LocalDateTime arrival = flight.getArrival().getScheduled();
                    return Duration.between(departure, arrival).toMinutes() / 60.0;
                })
                .average().orElse(0.0);
        return result;
    }


    public static List<FlightInfoDTO> getSortedArrivalTimes(List<FlightDTO> flightList) {
        return flightList.stream()
                // Make sure arrival exists
                .filter(flight -> flight.getArrival() != null && flight.getArrival().getScheduled() != null)
                // Sort by arrival time
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
                .collect(Collectors.toList());
    }

    public static void getTotalFlightTimeForEachAirline(List<FlightInfoDTO> flightList) {
        flightList.stream()
                .filter(flight -> flight.getDuration() != null && flight.getAirline() != null)
                .collect(Collectors.groupingBy(
                        FlightInfoDTO::getAirline, // group by airline name
                        Collectors.summingDouble(flight -> flight.getDuration().toMinutes() / 60.0) // sum hours
                )).forEach((x,y) -> System.out.println(x+Duration.ofMinutes(y.longValue())));
    }


}
