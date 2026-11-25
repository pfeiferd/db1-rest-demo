package demo.service;

import java.math.BigDecimal;

import demo.service.dto.Konto;

/** Konten: CRUD (polymorph über Girokonto und Sparkonto) und eine Überweisung. */
public interface KontoService extends CRUDService<Konto> {
  /** Bucht in EINER Transaktion ab und zu – beides oder nichts. */
  void ueberweisen(long vonId, long nachId, BigDecimal betrag);
}
