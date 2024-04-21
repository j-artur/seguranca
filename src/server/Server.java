package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import lib.Account;
import lib.Action;
import lib.Dbg;
import lib.Dbg.Color;
import lib.ServerMessage;
import lib.User;
import security.AES;
import security.HMAC;
import security.KeyPair;
import security.Keys;
import security.RSA;
import security.RSAKey;
import security.Security;

public class Server implements Runnable {
  public static int PORT = 3000;

  private DatagramSocket socket;
  private DatagramPacket receiveDatagram;
  private DatagramPacket sendPacket;
  private KeyPair rsaKeys;
  private List<User> users;
  private Hashtable<String, Keys> clientKeys;

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

    this.users
        .add(new User("123", "123", "João", "Rua 1", "123456789", 100000));
    this.users
        .add(new User("456", "456", "Maria", "Rua 2", "987654321", 100000));
    this.users
        .add(new User("789", "789", "José", "Rua 3", "123123123", 100000));

    this.clientKeys = new Hashtable<String, Keys>();
  }

  @Override
  public void run() {
    try {
      rsaKeys = RSA.generateKeys();

      socket = new DatagramSocket(PORT);
      InetAddress host = InetAddress.getLocalHost();
      Dbg.log(Color.BLUE, "Server has started at: " + host + ": " + PORT);

      while (true) {
        try {
          ServerMessage message = receiveMessage();

          Dbg.log(Color.BLUE, "Received message from " + message.port() + ": " + message.message());

          String[] parts = message.message().trim().split("@");
          String route = parts[0];
          String[] params = parts.length > 1 ? parts[1].split(";") : new String[0];

          Action action = Action.valueOf(route);

          switch (action) {
            case GetPublicKey:
              sendPublicKey(message.port());
              break;
            case TradeKeys:
              tradeKeys(message.port(), params[0]);
              break;
            case SignIn:
              signIn(params[0], params[1], message.port());
              break;
            case SignUp:
              signUp(params[0], params[1], params[2], params[3], params[4], message.port());
              break;
            case Balance:
              balance(params[0], message.port());
              break;
            case Withdraw:
              withdraw(params[0], Account.valueOf(params[1]), Integer.parseInt(params[2]), message.port());
              break;
            case Deposit:
              deposit(params[0], Account.valueOf(params[1]), Integer.parseInt(params[2]), message.port());
              break;
            case Transfer:
              transfer(params[0], params[1], Integer.parseInt(params[2]), message.port());
              break;
            case Investments:
              investments(params[0], message.port());
              break;
            default:
              continue;
          }
        } catch (Exception e) {
          sendMessage("error@Ação inválida", String.valueOf(receiveDatagram.getPort()));
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

  private void sendPublicKey(String port) throws Exception {
    String response = rsaKeys.publicKey().toString();
    byte[] sendBuffer = response.getBytes();
    sendPacket = new DatagramPacket(
        sendBuffer,
        sendBuffer.length,
        receiveDatagram.getAddress(),
        receiveDatagram.getPort());
    socket.send(sendPacket);
    Dbg.log(Color.CYAN_BRIGHT, "Sending message to " + port + ": " + response);
  }

  private void tradeKeys(String port, String publicKey) throws Exception {
    RSAKey clientPublicKey = RSAKey.fromString(publicKey);
    SecretKey hmacKey = HMAC.generateKey();
    SecretKey aesKey = AES.generateKey();
    Keys keys = new Keys(hmacKey, aesKey, clientPublicKey);

    clientKeys.put(port, keys);

    String message = Keys.toString(hmacKey) + ";" + Keys.toString(aesKey);

    Dbg.log(Color.CYAN_BRIGHT, "Sending message to " + port + ": " + message);

    String response = Security.encryptWithoutHash(
        message,
        clientPublicKey);
    byte[] sendBuffer = response.getBytes();
    sendPacket = new DatagramPacket(
        sendBuffer,
        sendBuffer.length,
        receiveDatagram.getAddress(),
        receiveDatagram.getPort());
    socket.send(sendPacket);

  }

  private void signIn(String cpf, String password, String port) throws Exception {
    Optional<User> user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst();

    if (user.isEmpty() || !user.get().password.equals(password)) {
      sendMessage("false", port);
    } else {
      sendMessage("true", port);
    }
  }

  private void signUp(String name, String cpf, String password, String address, String phone, String port)
      throws Exception {
    users.add(new User(cpf, password, name, address, phone));

    sendMessage("true", port);
  }

  private void balance(String cpf, String port) throws Exception {
    User user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst().orElseThrow();

    sendMessage(user.getCheckingBalance().toString(), port);
  }

  private void withdraw(String cpf, Account account, int value, String port) throws Exception {
    User user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst().orElseThrow();

    try {
      user.withdraw(account, value);
      sendMessage(user.getCheckingBalance().toString(), port);
    } catch (Exception e) {
      sendMessage("error@" + e.getMessage(), port);
    }
  }

  private void deposit(String cpf, Account account, int value, String port) throws Exception {
    User user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst().orElseThrow();

    user.deposit(account, value);
    sendMessage(user.getCheckingBalance().toString(), port);
  }

  private void transfer(String cpf, String targetCpf, int value, String port) throws Exception {
    User user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst().orElseThrow();
    Optional<User> targetUser = users.stream().filter(u -> u.cpf.equals(targetCpf)).findFirst();

    if (targetUser.isEmpty()) {
      sendMessage("error@Conta não encontrada", port);
    } else {
      user.transfer(targetUser.get(), value);
      sendMessage(user.getCheckingBalance().toString(), port);
    }
  }

  private void investments(String cpf, String port) throws Exception {
    User user = users.stream().filter(u -> u.cpf.equals(cpf)).findFirst().orElseThrow();

    int savings = user.getSavingsBalance();
    int fixed = user.getFixedIncomeBalance();

    sendMessage(savings + ";" + fixed, port);
  }

  private void sendMessage(String message, String port) throws Exception {
    Keys keys = clientKeys.get(port);

    Dbg.log(Color.CYAN_BRIGHT, "Sending message to " + port + ": " + message);
    String response = Security.encrypt(message, keys, rsaKeys.privateKey());
    byte[] sendBuffer = response.getBytes();
    sendPacket = new DatagramPacket(
        sendBuffer,
        sendBuffer.length,
        receiveDatagram.getAddress(),
        receiveDatagram.getPort());
    socket.send(sendPacket);
  }

  private ServerMessage receiveMessage() throws Exception {
    byte[] receiveBuffer = new byte[16384];
    receiveDatagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);
    socket.receive(receiveDatagram);

    String port = String.valueOf(receiveDatagram.getPort());
    String message = new String(receiveDatagram.getData()).trim();

    if (!clientKeys.containsKey(port)) {
      if (message.equals(Action.GetPublicKey.toString()))
        return new ServerMessage(message, port);

      return new ServerMessage(Security.decryptWithoutHash(message,
          rsaKeys.privateKey()), port);
    }

    try {
      Keys keys = clientKeys.get(port);
      return new ServerMessage(Security.decrypt(message, keys), port);
    } catch (Exception e) {
      Dbg.log(Color.RED, e.getMessage());
      throw e;
    }
  }
}
