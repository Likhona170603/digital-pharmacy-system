package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.stereotype.Service;
import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.repository.CategoryRepository;

import java.util.List;

/*
 * Asenathi Nini
 * 230452817
 */
@Service
public class CategoryService implements ICategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Category create(Category category) {
        return this.repository.save(category);
    }

    @Override
    public Category read(String categoryId) {
        return this.repository.findById(categoryId).orElse(null);
    }

    @Override
    public Category update(Category category) {
        if (this.repository.existsById(category.getCategoryId())) {
            return this.repository.save(category);
        }
        return null;
    }

    @Override
    public boolean delete(String categoryId) {
        if (this.repository.existsById(categoryId)) {
            this.repository.deleteById(categoryId);
            return true;
        }
        return false;
    }

    @Override
    public List<Category> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<Category> search(String dosageForm, String scheduleClassification) {
        boolean hasForm = dosageForm != null && !dosageForm.isBlank();
        boolean hasSchedule = scheduleClassification != null && !scheduleClassification.isBlank();
        if (hasForm && hasSchedule) {
            return this.repository.findByDosageFormIgnoreCaseAndScheduleClassificationIgnoreCase(
                    dosageForm.trim(), scheduleClassification.trim());
        }
        if (hasForm) {
            return this.repository.findByDosageFormIgnoreCase(dosageForm.trim());
        }
        if (hasSchedule) {
            return this.repository.findByScheduleClassificationIgnoreCase(scheduleClassification.trim());
        }
        return this.repository.findAll();
    }

    @Override
    public List<String> getDosageForms() {
        return this.repository.findDistinctDosageForms();
    }
}
