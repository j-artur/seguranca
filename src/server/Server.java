package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lib.Account;
import lib.Action;
import lib.Dbg;
import lib.Dbg.Color;
import lib.User;

public class Server implements Runnable {
  public static int PORT = 3000;

  private DatagramSocket socket;
  private byte[] receiveBuffer;
  private byte[] sendBuffer;
  private Security security;
  private DatagramPacket receiveDatagram;
  private DatagramPacket sendPacket;
  private List<User> users;

  private String hmacKey = "uu1fR7CqcmJdpNqz0/iulN0ppkheDbwpyVbXU3HgLVw=";

  public static void main(String args[]) {
    Server server = new Server();

    Thread t = new Thread(server);
    t.start();

    try {
      t.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public Server() {
    this.users = new ArrayList<User>();
    this.security = new Security(hmacKey);
  }

  @Override
  public void run() {
    try {
      socket = new DatagramSocket(PORT);
      InetAddress host = InetAddress.getLocalHost();
      Dbg.log(Color.BLUE, "Server has started at: " + host + ":" + PORT);

      while (true) {
        String message = receiveMessage();

        Dbg.log(Color.GREEN_BRIGHT, message);

        System.out.println(message == null ? "null" : "tem");

        String[] parts = message.split("@");
        String route = parts[0];
        String[] params = parts[1].split(":");

        try {
          Action action = Action.valueOf(route);

          switch (action) {
            case SignIn:
              signIn(params[0], params[1]);
              break;
            case SignUp:
              signUp(params[0], params[1], params[2], params[3], params[4]);
              break;
            case Balance:
              balance(params[0]);
              break;
            case Withdraw:
              withdraw(params[0], Account.valueOf(params[1]), Integer.parseInt(params[2]));
              break;
            case Deposit:
              deposit(params[0], Account.valueOf(params[1]), Integer.parseInt(params[2]));
              break;
            case Transfer:
              transfer(params[0], params[1], Integer.parseInt(params[2]));
              break;
            case Investments:
              investments(params[0]);
              break;
            default:
              continue;
          }

          Dbg.log(Color.BLUE, "Incoming message: " + message);

        } catch (Exception e) {
          sendMessage("error@Ação inválida");
          continue;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // Fechando o servidor.
      if (socket != null)
        socket.close();
    }
  }

  private void signIn(String cpf, String password) throws Exception {
    Optional<User> user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst();

    if (user.isEmpty() || !user.get().password.equals(password)) {
      sendMessage("false");
    } else {
      sendMessage("true");
    }
  }

  private void signUp(String name, String cpf, String password, String address, String phone) throws Exception {
    users.add(new User(cpf, password, name, address, phone));

    sendMessage("true");
  }

  private void balance(String cpf) throws Exception {
    User user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst().orElseThrow();

    sendMessage(user.getCheckingBalance().toString());
  }

  private void withdraw(String cpf, Account account, int value) throws Exception {
    User user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst().orElseThrow();

    try {
      user.withdraw(account, value);
      sendMessage(user.getCheckingBalance().toString());
    } catch (Exception e) {
      sendMessage("error@" + e.getMessage());
    }
  }

  private void deposit(String cpf, Account account, int value) throws Exception {
    User user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst().orElseThrow();

    user.deposit(account, value);
    sendMessage(user.getCheckingBalance().toString());
  }

  private void transfer(String cpf, String targetCpf, int value) throws Exception {
    User user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst().orElseThrow();
    Optional<User> targetUser = users.stream().filter(u -> u.cpf.equals(targetCpf)).findFirst();

    if (targetUser.isEmpty()) {
      sendMessage("error@Conta não encontrada");
    } else {
      user.transfer(targetUser.get(), value);
      sendMessage(user.getCheckingBalance().toString());
    }
  }

  private void investments(String cpf) throws Exception {
    User user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst().orElseThrow();

    int savings = user.getSavingsBalance();
    int fixed = user.getFixedIncomeBalance();

    sendMessage(savings + ":" + fixed);
  }

  private void sendMessage(String message) throws Exception {
    Dbg.log(Color.CYAN_BRIGHT, "Enviando mensagem... " + message);
    String response = security.encrypt(message);
    sendBuffer = response.getBytes();
    sendPacket = new DatagramPacket(
        sendBuffer,
        sendBuffer.length,
        receiveDatagram.getAddress(),
        receiveDatagram.getPort());
    socket.send(sendPacket);
  }

  private String receiveMessage() throws Exception {
    receiveBuffer = new byte[1024];
    receiveDatagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);
    socket.receive(receiveDatagram);
    receiveBuffer = receiveDatagram.getData();

    try {
      return security.decrypt(new String(receiveBuffer));
    } catch (Exception e) {
      Dbg.log(Color.RED, e.getMessage());
      return null;
    }
  }
}
