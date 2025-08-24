package com.travelbooking.common;

public final class Constants {
    
    private Constants() {
        throw new UnsupportedOperationException("Constants class should not be instantiated");
    }
    
    // Kafka Topics
    public static final class Topics {
        // Flight Service
        public static final String FLIGHT_SERVICE_COMMANDS = "flight-service-commands";
        public static final String FLIGHT_SERVICE_REPLIES = "flight-service-replies";
        
        // Hotel Service
        public static final String HOTEL_SERVICE_COMMANDS = "hotel-service-commands";
        public static final String HOTEL_SERVICE_REPLIES = "hotel-service-replies";
        
        // Car Rental Service
        public static final String CAR_SERVICE_COMMANDS = "car-service-commands";
        public static final String CAR_SERVICE_REPLIES = "car-service-replies";
        
        // Discount Service
        public static final String DISCOUNT_SERVICE_QUERIES = "discount-service-queries";
        public static final String DISCOUNT_SERVICE_REPLIES = "discount-service-replies";
        
        private Topics() {
            throw new UnsupportedOperationException("Topics class should not be instantiated");
        }
    }
    
    // Consumer Groups
    public static final class ConsumerGroups {
        public static final String TRIP_BOOKING_GROUP = "trip-booking-group";
        public static final String FLIGHT_SERVICE_GROUP = "flight-service-group";
        public static final String HOTEL_SERVICE_GROUP = "hotel-service-group";
        public static final String CAR_RENTAL_GROUP = "car-rental-group";
        public static final String DISCOUNT_SERVICE_GROUP = "discount-service-group";
        
        private ConsumerGroups() {
            throw new UnsupportedOperationException("ConsumerGroups class should not be instantiated");
        }
    }
    
    // Timeouts (in milliseconds)
    public static final class Timeouts {
        public static final long DEFAULT_REPLY_TIMEOUT = 30000; // 30 seconds
        public static final long DISCOUNT_QUERY_TIMEOUT = 5000; // 5 seconds
        
        private Timeouts() {
            throw new UnsupportedOperationException("Timeouts class should not be instantiated");
        }
    }
}