package com.skygazer.weather.service;

public interface DataMigrationService {
    
    void migrateRedisToDatabase();
    
    int migrateWeatherData();
    
    int migrateHourlyForecastData();
    
    int migrateWeeklyForecastData();
    
    MigrationStatus getMigrationStatus();
    
    class MigrationStatus {
        private final int totalRecords;
        private final int migratedRecords;
        private final int failedRecords;
        private final boolean completed;
        private final String message;
        
        public MigrationStatus(int totalRecords, int migratedRecords, int failedRecords, boolean completed, String message) {
            this.totalRecords = totalRecords;
            this.migratedRecords = migratedRecords;
            this.failedRecords = failedRecords;
            this.completed = completed;
            this.message = message;
        }
        
        public int getTotalRecords() {
            return totalRecords;
        }
        
        public int getMigratedRecords() {
            return migratedRecords;
        }
        
        public int getFailedRecords() {
            return failedRecords;
        }
        
        public boolean isCompleted() {
            return completed;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
