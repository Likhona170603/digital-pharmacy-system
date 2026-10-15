package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.factory.CategoryFactory;
import za.ac.cput.digitalpharmacysystem.service.ICategoryService;
import za.ac.cput.digitalpharmacysystem.service.IProductService;

import java.util.List;

/*
 * Asenathi Nini
 * 230452817
 */
@RestController
@RequestMapping("/digitalpharmacysystem/category")
public class CategoryController {

    private final ICategoryService service;
    private final IProductService productService;

    @Autowired
    public CategoryController(ICategoryService service, IProductService productService) {
        this.service = service;
        this.productService = productService;
    }

    private Category validated(Category c) {
        if (c == null) {
            return null;
        }
        return CategoryFactory.createCategory(
                c.getCategoryId(), c.getCategoryName(), c.getDosageForm(), c.getScheduleClassification());
    }

    @PostMapping("/create")
    public ResponseEntity<Category> create(@RequestBody Category category) {
        Category valid = validated(category);
        if (valid == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (service.read(valid.getCategoryId()) != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(service.create(valid), HttpStatus.CREATED);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Category> read(@PathVariable String id) {
        Category category = service.read(id);
        if (category == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<Category> update(@RequestBody Category category) {
        Category valid = validated(category);
        if (valid == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Category updated = service.update(valid);
        if (updated == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (service.read(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!productService.getByCategoryId(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Category>> getAll() {
        List<Category> categories = service.getAll();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
}
