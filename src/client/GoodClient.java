package client;

public class GoodClient {
  public static void main(String args[]) {
    Client client = new Client();

    Thread thread = new Thread(client);
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
