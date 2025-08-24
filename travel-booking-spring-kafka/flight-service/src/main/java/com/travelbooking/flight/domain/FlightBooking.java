package com.travelbooking.flight.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "flight_bookings")
public class FlightBooking {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String confirmationNumber;

    @Column(nullable = false)
    private UUID travelerId;

    @Column(nullable = false, name = "from_location")
    private String from;

    @Column(nullable = false, name = "to_location")
    private String to;

    @Column(nullable = false)
    private LocalDate departureDate;

    private LocalDate returnDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    protected FlightBooking() {
        // For JPA
    }

    public FlightBooking(UUID id, String confirmationNumber, UUID travelerId, 
                        String from, String to, LocalDate departureDate, 
                        LocalDate returnDate, BigDecimal price) {
        this.id = validateId(id);
        this.confirmationNumber = validateConfirmationNumber(confirmationNumber);
        this.travelerId = validateTravelerId(travelerId);
        this.from = validateFrom(from);
        this.to = validateTo(to);
        this.departureDate = validateDepartureDate(departureDate);
        this.returnDate = validateReturnDate(returnDate, departureDate);
        this.price = validatePrice(price);
    }

    private UUID validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        return id;
    }

    private String validateConfirmationNumber(String confirmationNumber) {
        if (confirmationNumber == null || confirmationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Confirmation number cannot be null or empty");
        }
        return confirmationNumber;
    }

    private UUID validateTravelerId(UUID travelerId) {
        if (travelerId == null) {
            throw new IllegalArgumentException("Traveler id cannot be null");
        }
        return travelerId;
    }

    private String validateFrom(String from) {
        if (from == null || from.trim().isEmpty()) {
            throw new IllegalArgumentException("From location cannot be null or empty");
        }
        return from;
    }

    private String validateTo(String to) {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("To location cannot be null or empty");
        }
        return to;
    }

    private LocalDate validateDepartureDate(LocalDate departureDate) {
        if (departureDate == null) {
            throw new IllegalArgumentException("Departure date cannot be null");
        }
        return departureDate;
    }

    private LocalDate validateReturnDate(LocalDate returnDate, LocalDate departureDate) {
        if (returnDate != null && returnDate.isBefore(departureDate)) {
            throw new IllegalArgumentException("Return date cannot be before departure date");
        }
        return returnDate;
    }

    private BigDecimal validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        return price;
    }

    public UUID getId() {
        return id;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public UUID getTravelerId() {
        return travelerId;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightBooking that = (FlightBooking) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FlightBooking{" +
                "id=" + id +
                ", confirmationNumber='" + confirmationNumber + '\'' +
                ", travelerId=" + travelerId +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", departureDate=" + departureDate +
                ", returnDate=" + returnDate +
                ", price=" + price +
                '}';
    }
}