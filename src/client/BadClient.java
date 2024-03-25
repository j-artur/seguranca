package client;

public class BadClient {
  private static String hmacKey = "i25S9k0gvQxOAVEn74HX9ql0I0fSyJ1RKhful0Eegm8=";

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
