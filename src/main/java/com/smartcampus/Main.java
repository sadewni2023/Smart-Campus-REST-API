package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main class to launch the Smart Campus API using an embedded Grizzly server.
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // Create a resource config that scans for JAX-RS resources and providers
        // in com.smartcampus package
        final ResourceConfig rc = new SmartCampusApplication();

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) {
        try {
            printBanner();
            final HttpServer server = startServer();
            
            // Add a shutdown hook to handle CTRL+C gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[SHUTDOWN] Stopping Grizzly server...");
                server.shutdownNow();
                System.out.println("[SHUTDOWN] Server stopped safely. Goodbye!");
            }));

            System.out.println(String.format("\n[SUCCESS] Smart Campus API is LIVE at: %sapi/v1", BASE_URI));
            System.out.println("[INFO] Press CTRL+C or Enter to stop the server.\n");
            
            System.in.read();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to start server: " + e.getMessage());
        }
    }

    private static void printBanner() {
        System.out.println("==================================================================");
        System.out.println("  ____  __  __    _    ____ _____    ____    _    __  __ ____  _   _ ____  ");
        System.out.println(" / ___||  \\/  |  / \\  |  _ \\_   _|  / ___|  / \\  |  \\/  |  _ \\| | | / ___| ");
        System.out.println(" \\___ \\| |\\/| | / _ \\ | |_) || |   | |     / _ \\ | |\\/| | |_) | | | \\___ \\ ");
        System.out.println("  ___) | |  | |/ ___ \\|  _ < | |   | |___ / ___ \\| |  | |  __/| |_| |___) |");
        System.out.println(" |____/|_|  |_/_/   \\_\\_| \\_\\|_|    \\____/_/   \\_\\_|  |_|_|    \\___/|____/ ");
        System.out.println("==================================================================");
        System.out.println("             RESTful API v1.0.0 | Academic Year 2025/26           ");
        System.out.println("==================================================================");
    }
}
