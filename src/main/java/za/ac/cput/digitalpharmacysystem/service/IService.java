package za.ac.cput.digitalpharmacysystem.service;

import java.util.List;

/*
 * Sesona Ntshatsha
 * 240773365

 */

public interface IService<T, ID> {
    T create(T t);
    T read(ID id);
    T update(T t);
    boolean delete(ID id);
    List<T> getAll();
}
