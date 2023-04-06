package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.CabRepository;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

import static com.driver.model.TripStatus.CANCELED;
import static com.driver.model.TripStatus.CONFIRMED;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;



	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function

		Customer customer=customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		Cab cab=null;
		Customer customer=customerRepository2.findById(customerId).get();


		Driver driver=null;
		List<Driver>driverList=driverRepository2.findAll();

		for(Driver driver1:driverList){
			Cab cab1=driver1.getCab();
			if(cab1.isAvailable()){
				if(driver==null || driver1.getDriverId() <driver.getDriverId())
				 driver=driver1;
			}
		}
		if(driver==null){
			throw new Exception("No cab available!");
		}

		TripBooking tripBooking=new TripBooking();
		tripBooking.setCustomer(customer);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistIntKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setBill(distanceInKm * driver.getCab().getPerKmRate());

		tripBooking.setDriver(driver);
		tripBooking.setCustomer(customer);

		customer.getTripBookingList().add(tripBooking);

		driver.getTripBookingList().add(tripBooking);

		driverRepository2.save(driver);
		customerRepository2.save(customer);




		return  tripBooking;




	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);


		Driver driver = tripBooking.getDriver();
		driver.getCab().setAvailable(true);

		driverRepository2.save(driver);

		tripBookingRepository2.save(tripBooking);


	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);

		Driver driver = tripBooking.getDriver();
		driver.getCab().setAvailable(true);

		tripBookingRepository2.save(tripBooking);


	}
}
