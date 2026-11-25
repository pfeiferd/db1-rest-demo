package demo.service.jpa;

import java.math.BigDecimal;
import java.util.function.Consumer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;

import demo.service.ConflictException;
import demo.service.KontoService;
import demo.service.NotFoundException;
import demo.service.ServiceException;
import demo.service.dto.Girokonto;
import demo.service.dto.Konto;
import demo.service.dto.Sparkonto;

/** Dieselbe Schnittstelle wie KontoJDBCService – mit JPA statt JDBC. */
public class KontoJPAService implements KontoService,
    ConversionService<Konto, KontoEntity> {
  private final EntityManagerFactory emf;

  public KontoJPAService(EntityManagerFactory emf) {
    this.emf = emf;
  }

  @Override
  public Konto get(long id) {
    return emf.callInTransaction(em -> toDTO(find(em, id)));
  }

  @Override
  public Konto[] getAll() {
    return emf.callInTransaction(em -> em
        .createQuery("SELECT k FROM Konto k ORDER BY k.id", KontoEntity.class)
        .getResultStream()
        .map(this::toDTO)
        .toArray(Konto[]::new));
  }

  @Override
  public long create(Konto k) {
    KontoEntity e = fromDTO(k);
    emf.runInTransaction(em -> {
      // Die Referenz auf die Person braucht den EntityManager
      e.setInhaber(em.getReference(PersonEntity.class, k.getPersonId()));
      em.persist(e);
    });
    k.setId(e.getId());
    k.setVersion(e.getVersion());
    return k.getId();
  }

  @Override
  public void update(Konto k) {
    inTransaktion(em -> {
      KontoEntity e = find(em, k.getId());
      // Die Version aus dem DTO darf JPA nicht setzen - also selbst vergleichen
      if (e.getVersion() != k.getVersion()) {
        throw new ConflictException("Konto " + k.getId() + " wurde inzwischen geändert");
      }
      e.setInhaber(em.getReference(PersonEntity.class, k.getPersonId()));
      e.setSaldo(k.getSaldo());
      if (e instanceof GirokontoEntity g) {
        g.setDispo(((Girokonto) k).getDispo());
      } else {
        ((SparkontoEntity) e).setZinssatz(((Sparkonto) k).getZinssatz());
      }
    });                                   // UPDATE ... WHERE version = ? beim Commit
    k.setVersion(k.getVersion() + 1);
  }

  @Override
  public void remove(long id) {
    inTransaktion(em -> em.remove(find(em, id)));
  }

  @Override
  public void ueberweisen(long vonId, long nachId, BigDecimal betrag) {
    inTransaktion(em -> {
      KontoEntity von = find(em, vonId);              // polymorph:
      KontoEntity nach = find(em, nachId);            // Giro- oder Sparkonto
      BigDecimal neu = von.getSaldo().subtract(betrag);
      if (neu.compareTo(von.untergrenze()) < 0) {
        throw new ServiceException("Deckung reicht nicht");
      }
      von.setSaldo(neu);                              // zwei UPDATEs beim Commit,
      nach.setSaldo(nach.getSaldo().add(betrag));     // beide mit Versionsprüfung
    });
  }

  /** Eine Transaktion; ein Versionskonflikt beim Commit wird zur ConflictException. */
  private void inTransaktion(Consumer<EntityManager> arbeit) {
    try {
      emf.runInTransaction(arbeit);       // Rollback bei jeder Exception
    } catch (RollbackException e) {       // Commit gescheitert
      if (e.getCause() instanceof OptimisticLockException) {
        throw new ConflictException("Konto wurde inzwischen geändert");
      }
      throw new ServiceException(e);
    }
  }

  private static KontoEntity find(EntityManager em, long id) {
    KontoEntity e = em.find(KontoEntity.class, id);
    if (e == null) {
      throw new NotFoundException("Konto " + id + " nicht gefunden");
    }
    return e;
  }

  @Override
  public KontoEntity fromDTO(Konto k) {               // DTO → Entity, ohne Inhaber
    return k instanceof Girokonto g
        ? new GirokontoEntity(null, g.getSaldo(), g.getDispo())
        : new SparkontoEntity(null, k.getSaldo(), ((Sparkonto) k).getZinssatz());
  }

  @Override
  public Konto toDTO(KontoEntity e) {                 // Entity → DTO
    Konto k;
    if (e instanceof GirokontoEntity g) {
      Girokonto dto = new Girokonto();
      dto.setDispo(g.getDispo());
      k = dto;
    } else {
      Sparkonto dto = new Sparkonto();
      dto.setZinssatz(((SparkontoEntity) e).getZinssatz());
      k = dto;
    }
    k.setId(e.getId());
    k.setPersonId(e.getInhaber().getId());            // lädt die Person nicht
    k.setSaldo(e.getSaldo());
    k.setVersion(e.getVersion());
    return k;
  }
}
