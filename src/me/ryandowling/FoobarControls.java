package me.ryandowling;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class FoobarControls {
    public void run() {
        InetSocketAddress addr = new InetSocketAddress(8080);
        HttpServer server = null;

        try {
            server = HttpServer.create(addr, 0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        server.createContext("/", new MyHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Listening on 127.0.0.1:8080");
    }
}

class MyHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        OutputStream responseBody = exchange.getResponseBody();
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, 0);

        switch(exchange.getRequestURI().toString()) {
            case "/":
                responseBody.write("Available paths:\n\n/play\n/pause\n/stop\n/next\n/prev\n/nextpause".getBytes());
                break;
            case "/play":
                Runtime.getRuntime().exec("C:\\Program Files (x86)\\foobar2000\\foobar2000.exe /play");
                responseBody.write("foobar now playing".getBytes());
                break;
            case "/pause":
                Runtime.getRuntime().exec("C:\\Program Files (x86)\\foobar2000\\foobar2000.exe /pause");
                responseBody.write("foobar now paused".getBytes());
                break;
            case "/stop":
                Runtime.getRuntime().exec("C:\\Program Files (x86)\\foobar2000\\foobar2000.exe /stop");
                responseBody.write("foobar now stopped".getBytes());
                break;
            case "/next":
                Runtime.getRuntime().exec("C:\\Program Files (x86)\\foobar2000\\foobar2000.exe /next");
                responseBody.write("foobar now playing next song".getBytes());
                break;
            case "/prev":
                Runtime.getRuntime().exec("C:\\Program Files (x86)\\foobar2000\\foobar2000.exe /prev");
                responseBody.write("foobar now playing previous song".getBytes());
                break;
            case "/nextpause":
                Runtime.getRuntime().exec("C:\\Program Files (x86)\\foobar2000\\foobar2000.exe /next");
                Runtime.getRuntime().exec("C:\\Program Files (x86)\\foobar2000\\foobar2000.exe /pause");
                responseBody.write("foobar now playing next song".getBytes());
                break;
            default:
                responseBody.write("Unknown path!".getBytes());
                break;
        }
        responseBody.close();
    }
}