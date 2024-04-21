package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.crypto.SecretKey;

import lib.Account;
import lib.Action;
import lib.Dbg;
import lib.Dbg.Color;
import security.KeyPair;
import security.Keys;
import security.RSA;
import security.RSAKey;
import security.Security;
import server.Server;

public class Client implements Runnable {
  private boolean active = true;
  private DatagramSocket clientSocket = null;
  private InetAddress address;
  private String signedInCpf = null;
  private Keys serverKeys;
  private KeyPair rsaKeys;
  private Dbg dbg = new Dbg();

  public Client() {
  }

  @Override
  public void run() {
    try {
      rsaKeys = RSA.generateKeys();

      clientSocket = new DatagramSocket();
      address = InetAddress.getByName("localhost");

      serverKeys = getServerKeys();

      Dbg.log(Color.CYAN, "Cliente online em: " + address + ": " + clientSocket.getLocalPort());
      while (active) {
        Dbg.log(Color.BLUE, "*** Serviço bancário ***");

        if (signedInCpf == null) {
          Dbg.log(Color.BLUE, "|1| Entrar");
          Dbg.log(Color.BLUE, "|2| Cadastrar");
          Dbg.log(Color.BLUE, "|3| Encerrar");
          String msg = dbg.input(Color.BLUE, "Digite o número da opção desejada:\n");

          switch (msg) {
            case "1":
              signIn();
              break;
            case "2":
              signUp();
              break;
            case "3":
              exit();
              break;
            default:
              Dbg.log(Color.RED, "Comando inválido");
              break;
          }

          continue;
        }

        Dbg.log(Color.BLUE, "|1| Saldo");
        Dbg.log(Color.BLUE, "|2| Saque");
        Dbg.log(Color.BLUE, "|3| Depósito");
        Dbg.log(Color.BLUE, "|4| Transferência");
        Dbg.log(Color.BLUE, "|5| Investimentos");
        Dbg.log(Color.BLUE, "|6| Sair");
        String msg = dbg.input(Color.BLUE, "Digite o número da opção desejada:\n");

        switch (msg) {
          case "1":
            balance();
            break;
          case "2":
            withdraw();
            break;
          case "3":
            deposit();
            break;
          case "4":
            transfer();
            break;
          case "5":
            investments();
            break;
          case "6":
            signOut();
            break;
          default:
            Dbg.log(Color.RED, "Comando inválido");
            break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (clientSocket != null)
        clientSocket.close();
    }
  }

  private void signIn() throws Exception {
    Dbg.log(Color.BLUE, "*** Entrar ***");
    String cpf = dbg.input(Color.BLUE, "Digite o cpf:\n");
    String password = dbg.input(Color.BLUE, "Digite a senha:\n");

    String response = sendMessage(Action.SignIn, new String[] { cpf, password });

    if (response.equals("true")) {
      Dbg.log(Color.GREEN, "Usuário autenticado com sucesso!");
      signedInCpf = cpf;
    } else {
      Dbg.log(Color.RED, "Número da conta e/ou senha incorretos.");
    }
  }

  private void signUp() throws Exception {
    Dbg.log(Color.BLUE, "*** Cadastro ***");
    String name = dbg.input(Color.BLUE, "Digite o nome:\n");
    String cpf = dbg.input(Color.BLUE, "Digite o CPF:\n");
    String password = dbg.input(Color.BLUE, "Digite a senha:\n");
    String address = dbg.input(Color.BLUE, "Digite o endereço:\n");
    String phone = dbg.input(Color.BLUE, "Digite o telefone:\n");

    String response = sendMessage(Action.SignUp, new String[] { name, cpf, password, address, phone });

    if (response.equals("true")) {
      Dbg.log(Color.GREEN, "Usuário cadastrado com sucesso!");
    } else {
      Dbg.log(Color.RED, "Erro ao cadastrar usuário.");
    }
  }

  private void signOut() {
    signedInCpf = null;
  }

  private void balance() throws Exception {
    Dbg.log(Color.BLUE, "*** Saldo ***");

    String response = sendMessage(Action.Balance, new String[] { signedInCpf });

    int balance = Integer.parseInt(response);

    Dbg.log(Color.GREEN, "Saldo: " + display(balance));
  }

  private void withdraw() throws Exception {
    Dbg.log(Color.BLUE, "*** Saque ***");
    Dbg.log(Color.BLUE, "*** Escolha a conta ***");
    Dbg.log(Color.BLUE, "|1| Conta corrente");
    Dbg.log(Color.BLUE, "|2| Conta poupança");
    Dbg.log(Color.BLUE, "|3| Renda fixa");
    String option;
    Account account = null;
    do {
      option = dbg.input(Color.BLUE, "Digite o número da opção desejada:\n");

      switch (option) {
        case "1":
          account = Account.Checking;
          break;
        case "2":
          account = Account.Savings;
          break;
        case "3":
          account = Account.FixedIncome;
          break;
        default:
          Dbg.log(Color.RED, "Opção inválida");
          continue;
      }
    } while (account == null);

    String value = dbg.input(Color.BLUE, "Digite o valor:\n");

    String response = sendMessage(Action.Withdraw, new String[] { signedInCpf, account.toString(), value });

    if (response.startsWith("error")) {
      String[] parts = response.split("@");
      Dbg.log(Color.RED, "Erro: " + parts[1]);
      return;
    }

    int balance = Integer.parseInt(response);

    Dbg.log(Color.GREEN, "Saque realizado com sucesso!");
    Dbg.log(Color.BLUE, "Novo saldo: " + display(balance));
  }

  private void deposit() throws Exception {
    Dbg.log(Color.BLUE, "*** Depósito ***");
    Dbg.log(Color.BLUE, "*** Escolha a conta ***");
    Dbg.log(Color.BLUE, "|1| Conta corrente");
    Dbg.log(Color.BLUE, "|2| Conta poupança");
    Dbg.log(Color.BLUE, "|3| Renda fixa");
    String option;
    Account account = null;
    do {
      option = dbg.input(Color.BLUE, "Digite o número da opção desejada:\n");

      switch (option) {
        case "1":
          account = Account.Checking;
          break;
        case "2":
          account = Account.Savings;
          break;
        case "3":
          account = Account.FixedIncome;
          break;
        default:
          Dbg.log(Color.RED, "Opção inválida");
          continue;
      }
    } while (account == null);

    String value = dbg.input(Color.BLUE, "Digite o valor:\n");

    String response = sendMessage(Action.Deposit, new String[] { signedInCpf, account.toString(), value });

    int balance = Integer.parseInt(response);

    Dbg.log(Color.GREEN, "Depósito realizado com sucesso!");
    Dbg.log(Color.BLUE, "Novo saldo: " + display(balance));
  }

  private void transfer() throws Exception {
    Dbg.log(Color.BLUE, "*** Transferência ***");
    String destinationCpf = dbg.input(Color.BLUE, "Digite o cpf da conta de destino:\n");
    String value = dbg.input(Color.BLUE, "Digite o valor:\n");

    String response = sendMessage(Action.Transfer, new String[] { signedInCpf, destinationCpf, value });

    if (response.startsWith("error")) {
      String[] parts = response.split("@");
      Dbg.log(Color.RED, "Erro: " + parts[1]);
      return;
    }

    int balance = Integer.parseInt(response);

    Dbg.log(Color.GREEN, "Transferência realizada com sucesso!");
    Dbg.log(Color.BLUE, "Novo saldo: " + display(balance));
  }

  private void investments() throws Exception {
    Dbg.log(Color.BLUE, "*** Investimentos ***");

    String response = sendMessage(Action.Investments, new String[] { signedInCpf });

    String[] parts = response.split(";");

    int savings = Integer.parseInt(parts[0]);
    int fixed = Integer.parseInt(parts[1]);

    Dbg.log(Color.GREEN, "Saldo na conta poupança: " + display(savings));
    int savings3Months = yieldSimulation(savings, 3, 0.005f);
    Dbg.log(Color.GREEN, "Saldo na conta poupança após 3 meses: " + display(savings3Months));
    int savings6Months = yieldSimulation(savings, 6, 0.005f);
    Dbg.log(Color.GREEN, "Saldo na conta poupança após 6 meses: " + display(savings6Months));
    int savings12Months = yieldSimulation(savings, 12, 0.005f);
    Dbg.log(Color.GREEN, "Saldo na conta poupança após 12 meses: " + display(savings12Months));
    Dbg.log(Color.GREEN, "Saldo na conta fixa: " + display(fixed));
    int fixed3Months = yieldSimulation(fixed, 3, 0.01f);
    Dbg.log(Color.GREEN, "Saldo na conta fixa após 3 meses: " + display(fixed3Months));
    int fixed6Months = yieldSimulation(fixed, 6, 0.01f);
    Dbg.log(Color.GREEN, "Saldo na conta fixa após 6 meses: " + display(fixed6Months));
    int fixed12Months = yieldSimulation(fixed, 12, 0.01f);
    Dbg.log(Color.GREEN, "Saldo na conta fixa após 12 meses: " + display(fixed12Months));
  }

  private void exit() {
    active = false;
  }

  private String sendMessage(Action action, String[] params) throws Exception {
    String msg = Security.encrypt(action + "@" + String.join(";", params), serverKeys, rsaKeys.privateKey());
    byte[] sendBuffer = msg.getBytes();
    DatagramPacket sendDatagram = new DatagramPacket(sendBuffer, sendBuffer.length, address, Server.PORT);

    clientSocket.send(sendDatagram);

    byte[] receiveBuffer = new byte[16384];

    DatagramPacket receiveDatagram = new DatagramPacket(
        receiveBuffer,
        receiveBuffer.length);
    clientSocket.receive(receiveDatagram);

    String response = Security.decrypt(new String(receiveDatagram.getData()), serverKeys);

    return response;
  }

  private Keys getServerKeys() throws Exception {
    String msg = Action.GetPublicKey.toString();
    byte[] sendBuffer = msg.getBytes();
    DatagramPacket sendDatagram = new DatagramPacket(sendBuffer, sendBuffer.length, address, Server.PORT);

    clientSocket.send(sendDatagram);

    byte[] receiveBuffer = new byte[16384];

    DatagramPacket receiveDatagram = new DatagramPacket(
        receiveBuffer,
        receiveBuffer.length);
    clientSocket.receive(receiveDatagram);

    String response = new String(receiveDatagram.getData()).trim();

    RSAKey serverPublicKey = RSAKey.fromString(response);

    msg = Security.encryptWithoutHash(Action.TradeKeys + "@" + rsaKeys.publicKey(), serverPublicKey);
    sendBuffer = msg.getBytes();
    sendDatagram = new DatagramPacket(sendBuffer, sendBuffer.length, address, Server.PORT);

    clientSocket.send(sendDatagram);

    receiveBuffer = new byte[16384];

    receiveDatagram = new DatagramPacket(
        receiveBuffer,
        receiveBuffer.length);
    clientSocket.receive(receiveDatagram);

    response = Security.decryptWithoutHash(new String(receiveDatagram.getData()).trim(), rsaKeys.privateKey()).trim();

    String[] parts = response.split(";");

    SecretKey hmacKey = Keys.hmacFromString(parts[0]);
    SecretKey aesKey = Keys.aesFromString(parts[1]);

    return new Keys(hmacKey, aesKey, serverPublicKey);
  }

  private String display(int value) {
    return "R$ " + String.format("%,.2f", value / 100.0);
  }

  private int yieldSimulation(int value, int months, float rate) {
    return (int) (value * Math.pow(1 + rate, months));
  }
}
