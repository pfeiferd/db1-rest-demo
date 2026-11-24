package demo.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/** TCP-Client zum Server, der Zeichenketten umdreht. */
public class Client {
  public static void main(String[] args)
      throws IOException {
    String text = args.length > 0 ? args[0] : "abcd";
    // Verbindungsaufbau
    try (Socket socket =
           new Socket("localhost", 4711)) {
      // Request senden
      new DataOutputStream(socket.getOutputStream())
          .writeUTF(text);
      // Response empfangen
      String result =
          new DataInputStream(socket.getInputStream())
              .readUTF();
      System.out.println(result);
    } // Verbindungsabbau
  }
}
