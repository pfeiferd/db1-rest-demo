package demo.service;

import demo.service.dto.DTO;

public interface CRUDService<D extends DTO> {
  long create(D d);
  D get(long id);
  D[] getAll();
  void update(D d);
  void remove(long id);
}
