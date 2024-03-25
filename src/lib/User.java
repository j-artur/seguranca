package lib;

public class User {
  public String cpf;
  public String password;
  public String name;
  public String address;
  public String phone;
  private Integer checkingBalance;
  private Integer savingsBalance;
  private Integer fixedIncomeBalance;

  public User(String cpf, String password, String name, String address, String phone) {
    this.cpf = cpf;
    this.password = password;
    this.name = name;
    this.address = address;
    this.phone = phone;
    this.checkingBalance = 0;
    this.savingsBalance = 0;
    this.fixedIncomeBalance = 0;
  }

  public Integer getCheckingBalance() {
    return this.checkingBalance;
  }

  public Integer getSavingsBalance() {
    return this.savingsBalance;
  }

  public Integer getFixedIncomeBalance() {
    return this.fixedIncomeBalance;
  }

  public void withdraw(Account account, Integer value) throws Exception {
    switch (account) {
      case Checking:
        if (this.checkingBalance < value)
          throw new Exception("Saldo insuficiente");

        this.checkingBalance -= value;
        break;
      case Savings:
        if (this.savingsBalance < value)
          throw new Exception("Saldo insuficiente");

        this.savingsBalance -= value;
        break;
      case FixedIncome:
        if (this.fixedIncomeBalance < value)
          throw new Exception("Saldo insuficiente");

        this.fixedIncomeBalance -= value;
        break;
    }
  }

  public void deposit(Account account, Integer value) {
    switch (account) {
      case Checking:
        this.checkingBalance += value;
        break;
      case Savings:
        this.savingsBalance += value;
        break;
      case FixedIncome:
        this.fixedIncomeBalance += value;
        break;
    }
  }

  public void transfer(User user, Integer value) throws Exception {
    if (this.checkingBalance < value)
      throw new Exception("Saldo insuficiente");

    this.checkingBalance -= value;
    user.deposit(Account.Checking, value);
  }
}
