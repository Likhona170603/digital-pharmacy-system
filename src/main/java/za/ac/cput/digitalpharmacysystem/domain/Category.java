package za.ac.cput.digitalpharmacysystem.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/*
 * [Asenathi Nini]
 * [230452817]
 */
@Entity
public class Category {
    @Id
    private String categoryId;
    private String categoryName;
    private String dosageForm;
    private String scheduleClassification;

    protected Category() {}

    private Category(Builder builder) {
        this.categoryId = builder.categoryId;
        this.categoryName = builder.categoryName;
        this.dosageForm = builder.dosageForm;
        this.scheduleClassification = builder.scheduleClassification;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public String getScheduleClassification() {
        return scheduleClassification;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId='" + categoryId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", dosageForm='" + dosageForm + '\'' +
                ", scheduleClassification='" + scheduleClassification + '\'' +
                '}';
    }

    public static class Builder {
        private String categoryId;
        private String categoryName;
        private String dosageForm;
        private String scheduleClassification;

        public Builder setCategoryId(String categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder setCategoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public Builder setDosageForm(String dosageForm) {
            this.dosageForm = dosageForm;
            return this;
        }

        public Builder setScheduleClassification(String scheduleClassification) {
            this.scheduleClassification = scheduleClassification;
            return this;
        }

        public Builder copy(Category category) {
            this.categoryId = category.categoryId;
            this.categoryName = category.categoryName;
            this.dosageForm = category.dosageForm;
            this.scheduleClassification = category.scheduleClassification;
            return this;
        }

        public Category build() {
            return new Category(this);
        }
    }
}
