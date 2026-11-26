package demo.service.jpa;

import java.math.BigDecimal;
import java.util.function.Consumer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;

import demo.service.AccountService;
import demo.service.ConflictException;
import demo.service.NotFoundException;
import demo.service.ServiceException;
import demo.service.dto.Account;
import demo.service.dto.CheckingAccount;
import demo.service.dto.SavingsAccount;

/** Dieselbe Schnittstelle wie AccountJDBCService – mit JPA statt JDBC. */
public class AccountJPAService implements AccountService,
    ConversionService<Account, AccountEntity> {
  private final EntityManagerFactory emf;

  public AccountJPAService(EntityManagerFactory emf) {
    this.emf = emf;
  }

  @Override
  public Account get(long id) {
    return emf.callInTransaction(em -> toDTO(find(em, id)));
  }

  @Override
  public Account[] getAll() {
    return emf.callInTransaction(em -> em
        .createQuery("SELECT a FROM Account a ORDER BY a.id", AccountEntity.class)
        .getResultStream()
        .map(this::toDTO)
        .toArray(Account[]::new));
  }

  @Override
  public long create(Account a) {
    AccountEntity e = fromDTO(a);
    emf.runInTransaction(em -> {
      // Die Referenz auf die Person braucht den EntityManager
      e.setOwner(em.getReference(PersonEntity.class, a.getPersonId()));
      em.persist(e);
    });
    a.setId(e.getId());
    a.setVersion(e.getVersion());
    return a.getId();
  }

  @Override
  public void update(Account a) {
    inTransaction(em -> {
      AccountEntity e = find(em, a.getId());
      // Die Version aus dem DTO darf JPA nicht setzen - also selbst vergleichen
      if (e.getVersion() != a.getVersion()) {
        throw new ConflictException(
            "Konto " + a.getId() + " wurde inzwischen geändert");
      }
      e.setOwner(em.getReference(PersonEntity.class, a.getPersonId()));
      e.setBalance(a.getBalance());
      if (e instanceof CheckingAccountEntity ce) {
        ce.setOverdraftLimit(((CheckingAccount) a).getOverdraftLimit());
      } else {
        ((SavingsAccountEntity) e).setInterestRate(((SavingsAccount) a).getInterestRate());
      }
    });                       // UPDATE ... WHERE version = ? beim Commit
    a.setVersion(a.getVersion() + 1);
  }

  @Override
  public void remove(long id) {
    inTransaction(em -> em.remove(find(em, id)));
  }

  @Override
  public void transfer(long fromId, long toId, BigDecimal amount) {
    inTransaction(em -> {
      AccountEntity from = find(em, fromId);          // polymorph:
      AccountEntity to = find(em, toId);              // Giro- oder Sparkonto
      BigDecimal newBalance = from.getBalance().subtract(amount);
      if (newBalance.compareTo(from.minBalance()) < 0) {
        throw new ServiceException("Deckung reicht nicht");
      }
      from.setBalance(newBalance);                    // zwei UPDATEs beim Commit,
      to.setBalance(to.getBalance().add(amount));     // beide mit Versionsprüfung
    });
  }

  /** Eine Transaktion; Versionskonflikt beim Commit: ConflictException. */
  private void inTransaction(Consumer<EntityManager> work) {
    try {
      emf.runInTransaction(work);         // Rollback bei jeder Exception
    } catch (RollbackException e) {       // Commit gescheitert
      if (e.getCause() instanceof OptimisticLockException) {
        throw new ConflictException("Konto wurde inzwischen geändert");
      }
      throw new ServiceException(e);
    }
  }

  private static AccountEntity find(EntityManager em, long id) {
    AccountEntity e = em.find(AccountEntity.class, id);
    if (e == null) {
      throw new NotFoundException("Konto " + id + " nicht gefunden");
    }
    return e;
  }

  @Override
  public AccountEntity fromDTO(Account a) {           // DTO → Entity, ohne Inhaber(in)
    return a instanceof CheckingAccount ca
        ? new CheckingAccountEntity(null, ca.getBalance(), ca.getOverdraftLimit())
        : new SavingsAccountEntity(null, a.getBalance(), ((SavingsAccount) a).getInterestRate());
  }

  @Override
  public Account toDTO(AccountEntity e) {             // Entity → DTO
    Account a;
    if (e instanceof CheckingAccountEntity ce) {
      CheckingAccount dto = new CheckingAccount();
      dto.setOverdraftLimit(ce.getOverdraftLimit());
      a = dto;
    } else {
      SavingsAccount dto = new SavingsAccount();
      dto.setInterestRate(((SavingsAccountEntity) e).getInterestRate());
      a = dto;
    }
    a.setId(e.getId());
    a.setPersonId(e.getOwner().getId());              // lädt die Person nicht
    a.setBalance(e.getBalance());
    a.setVersion(e.getVersion());
    return a;
  }
}
