package demo.service;

import java.math.BigDecimal;

import demo.service.dto.Account;

/** Konten: CRUD (polymorph über Giro- und Sparkonto) und eine Überweisung. */
public interface AccountService extends CRUDService<Account> {
  /** Bucht in EINER Transaktion ab und zu – beides oder nichts. */
  void transfer(long fromId, long toId, BigDecimal amount);
}
