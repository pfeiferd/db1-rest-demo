package demo.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** TCP-Server, der Zeichenketten umdreht (Port 4711). */
public class Server {
  public static void main(String[] args)
      throws IOException {
    try (ServerSocket server = new ServerSocket(4711)) {
      System.out.println("Server wartet auf Port 4711 …");
      while (true) {
        // Auf Verbindungsaufbau warten
        try (Socket client = server.accept()) {
          // Request lesen
          String text =
            new DataInputStream(client.getInputStream())
                .readUTF();
          // Response berechnen und senden
          new DataOutputStream(client.getOutputStream())
              .writeUTF(new StringBuilder(text)
                  .reverse().toString());
        } // Verbindungsabbau
      }
    }
  }
}
