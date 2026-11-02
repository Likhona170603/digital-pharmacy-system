package za.ac.cput.digitalpharmacysystem.service;

import za.ac.cput.digitalpharmacysystem.domain.Category;

import java.util.List;

/*
 * Asenathi Nini
 * 230452817
 */
public interface ICategoryService extends IService<Category, String> {

    List<Category> search(String dosageForm, String scheduleClassification);

    List<String> getDosageForms();
}