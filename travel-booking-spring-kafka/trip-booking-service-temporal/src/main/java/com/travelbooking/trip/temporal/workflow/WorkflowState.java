package com.travelbooking.trip.temporal.workflow;

public class WorkflowState {
    private boolean flightBooked;
    private boolean hotelReserved;
    private boolean carRented;
    private String flightConfirmation;
    private String hotelConfirmation;
    private String carConfirmation;

    public WorkflowState() {
    }

    public WorkflowState(boolean flightBooked, boolean hotelReserved, boolean carRented,
                         String flightConfirmation, String hotelConfirmation, String carConfirmation) {
        this.flightBooked = flightBooked;
        this.hotelReserved = hotelReserved;
        this.carRented = carRented;
        this.flightConfirmation = flightConfirmation;
        this.hotelConfirmation = hotelConfirmation;
        this.carConfirmation = carConfirmation;
    }

    public boolean isFlightBooked() {
        return flightBooked;
    }

    public boolean isHotelReserved() {
        return hotelReserved;
    }

    public boolean isCarRented() {
        return carRented;
    }

    public String getFlightConfirmation() {
        return flightConfirmation;
    }

    public String getHotelConfirmation() {
        return hotelConfirmation;
    }

    public String getCarConfirmation() {
        return carConfirmation;
    }
}