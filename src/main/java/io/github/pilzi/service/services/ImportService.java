package io.github.pilzi.service.services;

public interface ImportService {
    /**
     * The main function of the importer.
     * It sends requests to the external API and stores seasons and events in the database.
     */
    void importPremierData();
}
