package client;

public class GoodClient {
  private static String hmacKey = "uu1fR7CqcmJdpNqz0/iulN0ppkheDbwpyVbXU3HgLVw=";

  public static void main(String args[]) {
    Client client = new Client(hmacKey);

    Thread thread = new Thread(client);
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
