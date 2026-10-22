package za.ac.cput.digitalpharmacysystem.factory;

import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.util.Helper;

public class CategoryFactory {

    public static Category createCategory(String categoryId, String categoryName, String dosageForm, String scheduleClassification) {
        if (Helper.isNullOrEmpty(categoryId)) {
            return null;
        }
        if (Helper.isNullOrEmpty(categoryName)) {
            return null;
        }

        return new Category.Builder()
                .setCategoryId(categoryId)
                .setCategoryName(categoryName)
                .setDosageForm(dosageForm)
                .setScheduleClassification(scheduleClassification)
                .build();
    }
}