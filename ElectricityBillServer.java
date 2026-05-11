import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpServer;

public class ElectricityBillServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0);

        // HOME PAGE
        server.createContext("/", exchange -> {

            String response = Files.readString(
                    Paths.get("index.html")
            );

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "text/html; charset=UTF-8"
            );

            exchange.sendResponseHeaders(
                    200,
                    response.getBytes("UTF-8").length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes("UTF-8"));

            os.close();
        });

        // CALCULATE BILL
        server.createContext("/calculate", exchange -> {

            InputStream input = exchange.getRequestBody();

            String data = new String(
                    input.readAllBytes(),
                    "UTF-8"
            );

            String[] pairs = data.split("&");

            String name = pairs[0]
                    .split("=")[1]
                    .replace("+", " ");

            int units = Integer.parseInt(
                    pairs[1].split("=")[1]
            );

            double bill = 0;

            if (units <= 100) {

                bill = units * 2;
            }

            else if (units <= 300) {

                bill = (100 * 2)
                        + ((units - 100) * 3.5);
            }

            else {

                bill = (100 * 2)
                        + (200 * 3.5)
                        + ((units - 300) * 5);
            }

            double tax = bill * 0.10;

            double total = bill + tax;

            String response = """
            <html>

            <head>
                <meta charset="UTF-8">
                <title>Electricity Bill</title>
            </head>

            <body style='font-family:Arial;
                         background:#f4f4f4;
                         text-align:center;
                         padding-top:50px;'>

                <div style='background:white;
                            width:400px;
                            margin:auto;
                            padding:30px;
                            border-radius:12px;
                            box-shadow:0 0 10px rgba(0,0,0,0.1);'>

                    <h1>Electricity Bill</h1>

                    <h2>Name: %s</h2>

                    <h2>Units Consumed: %d</h2>

                    <h2>Bill Amount: ₹%.2f</h2>

                    <h2>Tax (10%%): ₹%.2f</h2>

                    <h2>Total Amount: ₹%.2f</h2>

                    <br>

                    <a href='/'
                       style='text-decoration:none;
                              color:white;
                              background:#007bff;
                              padding:10px 15px;
                              border-radius:8px;'>

                        Calculate Again

                    </a>

                </div>

            </body>
            </html>
            """.formatted(name, units, bill, tax, total);

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "text/html; charset=UTF-8"
            );

            exchange.sendResponseHeaders(
                    200,
                    response.getBytes("UTF-8").length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes("UTF-8"));

            os.close();
        });

        server.start();

        System.out.println(
                "Server started at http://localhost:8080"
        );
    }
}