package za.ac.cput.digitalpharmacysystem.service;

import za.ac.cput.digitalpharmacysystem.domain.Product;

import java.util.List;

/*
 * Asenathi Nini
 * 230452817
 */
public interface IProductService extends IService<Product, String> {
    List<Product> getByCategoryId(String categoryId);
}