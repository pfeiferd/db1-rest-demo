package demo.service.jpa;

import demo.service.dto.DTO;

/**
 * Wandelt zwischen DTO und Entity um – in beide Richtungen.
 * Referenzen auf andere Entities kann fromDTO ohne EntityManager nicht
 * auflösen; das übernimmt der Dienst.
 */
public interface ConversionService<D extends DTO, E> {
  /** Neue, noch nicht gespeicherte Entity – id und Version vergibt JPA. */
  E fromDTO(D d);

  D toDTO(E e);
}
