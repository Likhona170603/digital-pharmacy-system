package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.cput.digitalpharmacysystem.domain.Delivery;
import za.ac.cput.digitalpharmacysystem.repository.DeliveryRepository;

import java.util.List;

/*
 * Sesona Ntshatsha
 * 240773365

 */

@Service
public class DeliveryService implements IDeliveryService{
    private final DeliveryRepository repository;

    @Autowired
    public DeliveryService(DeliveryRepository repository) {
        this.repository = repository;


        }

        @Override
        public Delivery create(Delivery delivery) {
            return this.repository.save(delivery);
        }

        @Override
        public Delivery read(String id) {
            return this.repository.findById(id).orElse(null);
        }

        @Override
        public Delivery update(Delivery delivery) {
            if (this.repository.existsById(delivery.getId())) {
                return this.repository.save(delivery);
            }
            return null;
        }

        @Override
        public boolean delete(String id) {
            if (this.repository.existsById(id)) {
                this.repository.deleteById(id);
                return true;
            }
            return false;
        }

        @Override
        public List<Delivery> getAll() {
            return this.repository.findAll();
        }
    }




